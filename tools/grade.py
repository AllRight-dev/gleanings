"""Tone-curve a finished 16x16 sprite: shrink the subject and pull the highlights down.

Two of the sprite_check failures are actually fixable without redrawing:
  fill too high  -> scale the subject down a notch, it becomes "a pile" not "a wall"
  bright share   -> gamma the highlights down; a highlight should be a fleck, not an area

    python grade.py in.png out.png --scale 0.88 --gamma 1.35 --colors 9
"""
import argparse
import os

from PIL import Image


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("src")
    ap.add_argument("dst")
    ap.add_argument("--scale", type=float, default=1.0)
    ap.add_argument("--gamma", type=float, default=1.0)
    ap.add_argument("--colors", type=int, default=9)
    a = ap.parse_args()

    im = Image.open(a.src).convert("RGBA")
    w, h = im.size

    if a.scale != 1.0:
        nw, nh = max(1, round(w * a.scale)), max(1, round(h * a.scale))
        small = im.resize((nw, nh), Image.LANCZOS)
        canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        canvas.alpha_composite(small, ((w - nw) // 2, (h - nh) // 2))
        im = canvas

    px = im.load()
    for y in range(h):
        for x in range(w):
            c = px[x, y]
            if c[3] == 0:
                continue
            r, g, b = (min(255, max(0, round(255 * (v / 255.0) ** a.gamma))) for v in c[:3])
            px[x, y] = (r, g, b, 255)

    # Re-quantise so the gamma ramp lands back on a small palette.
    flat = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            c = px[x, y]
            if c[3] > 0:
                flat.putpixel((x, y), (c[0], c[1], c[2], 255))
    q = flat.convert("RGB").quantize(colors=a.colors, method=Image.MEDIANCUT, dither=Image.NONE).convert("RGBA")
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        for x in range(w):
            if px[x, y][3] > 0:
                c = q.getpixel((x, y))
                out.putpixel((x, y), (c[0], c[1], c[2], 255))

    os.makedirs(os.path.dirname(os.path.abspath(a.dst)), exist_ok=True)
    out.save(a.dst)
    print(f"{os.path.basename(a.src)} -> {os.path.basename(a.dst)}  scale={a.scale} gamma={a.gamma} "
          f"opaque={sum(1 for c in out.get_flattened_data() if c[3] > 0)}")


if __name__ == "__main__":
    main()
