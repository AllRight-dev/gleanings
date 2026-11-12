"""Deterministic post-process: turn any generated image into a Minecraft-ready sprite.

A diffusion model hands you a 512x512 picture with gradients, anti-aliasing and
soft alpha. Minecraft wants 16x16, <=12 flat colours, binary alpha. This is that
conversion, and it is fully deterministic - the same input always yields the same
pixels, so a model can be swapped without the asset pipeline changing.

    python img2mc.py in.png out.png --size 16 --colors 11 --alpha 128

After running it, verify with sprite_check.py / tile_check.py. "The model drew it"
is not acceptance; the metrics are.
"""
import argparse
import os

from PIL import Image


def luminance(c):
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]


def match_ramp(out, ramp_path):
    """Force the output's value distribution onto a reference sprite's.

    Quantising only fixes *how many* colours there are; it does not fix whether
    the dark/bright split looks like vanilla. This maps colour ranks: the i-th
    darkest output colour takes the i-th darkest reference colour's luminance,
    keeping every hue direction intact. That is what makes a smooth render read
    as painted pixel art instead of a blurred photo.
    """
    ref = Image.open(ramp_path).convert("RGBA")
    ref_pal = sorted({c for c in ref.getdata() if c[3] > 0}, key=luminance)
    if not ref_pal:
        return out

    used = sorted({c for c in out.getdata() if c[3] > 0}, key=luminance)
    if not used:
        return out

    remap = {}
    for i, c in enumerate(used):
        target = ref_pal[round(i * (len(ref_pal) - 1) / max(1, len(used) - 1))]
        # Keep the output's own hue/saturation, adopt the reference's brightness.
        lum_c, lum_t = luminance(c), luminance(target)
        k = lum_t / lum_c if lum_c > 1e-6 else 1.0
        remap[c] = (max(0, min(255, int(c[0] * k))),
                    max(0, min(255, int(c[1] * k))),
                    max(0, min(255, int(c[2] * k))),
                    255)

    px = out.load()
    w, h = out.size
    for y in range(h):
        for x in range(w):
            c = px[x, y]
            if c[3] and c in remap:
                px[x, y] = remap[c]
    return out


def key_background(im, tolerance=26):
    """Flood-fill the backdrop away, starting from the border.

    Image models do not produce transparency - they produce a white or pastel
    backdrop. Without this step the "transparent background" you asked for
    becomes a white square in the inventory. Only connected-to-border pixels
    are removed, so a light patch *inside* the subject survives.
    """
    from collections import deque

    im = im.convert("RGBA")
    w, h = im.size
    px = im.load()

    corners = [px[0, 0], px[w - 1, 0], px[0, h - 1], px[w - 1, h - 1]]
    bg = tuple(sum(c[i] for c in corners) // 4 for i in range(3))

    def near(c):
        return (abs(c[0] - bg[0]) <= tolerance and
                abs(c[1] - bg[1]) <= tolerance and
                abs(c[2] - bg[2]) <= tolerance)

    seen = [[False] * w for _ in range(h)]
    q = deque()
    for x in range(w):
        for y in (0, h - 1):
            if not seen[y][x] and near(px[x, y]):
                seen[y][x] = True
                q.append((x, y))
    for y in range(h):
        for x in (0, w - 1):
            if not seen[y][x] and near(px[x, y]):
                seen[y][x] = True
                q.append((x, y))

    while q:
        x, y = q.popleft()
        px[x, y] = (0, 0, 0, 0)
        for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if 0 <= nx < w and 0 <= ny < h and not seen[ny][nx] and near(px[nx, ny]):
                seen[ny][nx] = True
                q.append((nx, ny))

    cleared = sum(1 for y in range(h) for x in range(w) if px[x, y][3] == 0)
    print(f"  keyed background #{bg[0]:02x}{bg[1]:02x}{bg[2]:02x}, cleared {100.0*cleared/(w*h):.0f}% of pixels")
    return im


def keep_largest_blob(im):
    """Drop everything except the biggest connected opaque region.

    Flood-filling the backdrop leaves a rim behind when the model drew a vignette:
    corners differ from the averaged background colour, so they survive, the
    bounding box stays the full canvas, and autocrop does nothing. Keeping only the
    largest blob kills the rim and makes the crop meaningful.
    """
    from collections import deque

    w, h = im.size
    px = im.load()
    seen = [[False] * w for _ in range(h)]
    best = []

    for sy in range(h):
        for sx in range(w):
            if seen[sy][sx] or px[sx, sy][3] == 0:
                continue
            comp = []
            q = deque([(sx, sy)])
            seen[sy][sx] = True
            while q:
                x, y = q.popleft()
                comp.append((x, y))
                for nx, ny in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
                    if 0 <= nx < w and 0 <= ny < h and not seen[ny][nx] and px[nx, ny][3] > 0:
                        seen[ny][nx] = True
                        q.append((nx, ny))
            if len(comp) > len(best):
                best = comp

    keep = set(best)
    for y in range(h):
        for x in range(w):
            if px[x, y][3] > 0 and (x, y) not in keep:
                px[x, y] = (0, 0, 0, 0)
    return im


def autocrop(im, pad=2):
    """Trim to the subject's bounding box before downscaling.

    A model asked for "subject fills 45% of the canvas" does exactly that, in a
    480x480 picture. Downscaling that to 16x16 leaves the subject 8 pixels wide.
    Cropping to the alpha bbox first is what makes the fill ratio legal.
    """
    im = keep_largest_blob(im)
    bbox = im.getbbox()
    if not bbox:
        return im
    left, top, right, bottom = bbox
    return im.crop((max(0, left - pad), max(0, top - pad),
                    min(im.width, right + pad), min(im.height, bottom + pad)))


def convert(src, dst, size=16, colors=12, alpha_threshold=128, margin=0, ramp=None, key_bg=False, crop=False):
    im = Image.open(src).convert("RGBA")
    if key_bg:
        im = key_background(im)
    if crop:
        im = autocrop(im)

    # Optional inset: crops a border the model may have drawn around the subject.
    if margin:
        w, h = im.size
        im = im.crop((margin, margin, w - margin, h - margin))

    # Nearest for pixel-art sources, LANCZOS for smooth model output.
    resample = Image.NEAREST if im.width <= size * 2 else Image.LANCZOS
    im = im.resize((size, size), resample)

    px = im.load()
    mask = Image.new("L", (size, size), 0)
    mp = mask.load()
    for y in range(size):
        for x in range(size):
            mp[x, y] = 255 if px[x, y][3] >= alpha_threshold else 0

    # Quantise only what survives, so transparent pixels cannot steal palette slots.
    flat = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fpx = flat.load()
    for y in range(size):
        for x in range(size):
            if mp[x, y]:
                fpx[x, y] = (px[x, y][0], px[x, y][1], px[x, y][2], 255)

    q = flat.convert("RGB").quantize(colors=colors, method=Image.MEDIANCUT, dither=Image.NONE)
    out = q.convert("RGBA")
    op = out.load()
    for y in range(size):
        for x in range(size):
            if mp[x, y]:
                op[x, y] = (op[x, y][0], op[x, y][1], op[x, y][2], 255)
            else:
                op[x, y] = (0, 0, 0, 0)

    if ramp:
        out = match_ramp(out, ramp)

    os.makedirs(os.path.dirname(os.path.abspath(dst)), exist_ok=True)
    out.save(dst)
    print(f"{os.path.basename(src)} -> {os.path.basename(dst)}  {size}x{size}  "
          f"colours<={colors}  opaque={sum(1 for c in out.getdata() if c[3])}")


if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("src")
    p.add_argument("dst")
    p.add_argument("--size", type=int, default=16)
    p.add_argument("--colors", type=int, default=12)
    p.add_argument("--alpha", type=int, default=128, dest="alpha_threshold")
    p.add_argument("--margin", type=int, default=0)
    p.add_argument("--ramp", default=None, help="reference sprite whose value ramp to copy")
    p.add_argument("--crop", action="store_true", help="trim to the subject bbox before downscaling")
    p.add_argument("--key-bg", action="store_true", help="flood-fill the backdrop away from the border")
    a = p.parse_args()
    convert(a.src, a.dst, a.size, a.colors, a.alpha_threshold, a.margin, a.ramp, a.key_bg, a.crop)
