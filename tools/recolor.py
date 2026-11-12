"""Recolour a vanilla sprite onto another vanilla sprite's palette.

Same principle as gen_texture.py: for a variant of an existing item, never draw
freehand - keep the silhouette and the light/dark structure, swap the colour ramp.
Here the target ramp comes from a reference sprite, so the result sits inside the
same palette family as the block it is made of.

    python recolor.py --src ref/clay_ball.png --palette ref/amethyst_shard.png --out out.png
    python recolor.py --src ... --palette ... --out ... --darken 0.55
"""
import argparse
import os

from PIL import Image


def lum(c):
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", required=True)
    ap.add_argument("--palette", default=None, help="reference sprite to take the ramp from")
    ap.add_argument("--colors", default=None, help="explicit ramp, '#rrggbb,#rrggbb,...' (dark to light)")
    ap.add_argument("--out", required=True)
    ap.add_argument("--darken", type=float, default=1.0)
    ap.add_argument("--specular", type=float, default=0.0,
                    help="blend the brightest tones towards white, 0..1 - adds a highlight")
    a = ap.parse_args()

    src = Image.open(a.src).convert("RGBA")
    if a.colors:
        target = []
        for chunk in a.colors.split(","):
            chunk = chunk.strip().lstrip("#")
            target.append((int(chunk[0:2], 16), int(chunk[2:4], 16), int(chunk[4:6], 16), 255))
        target.sort(key=lum)
    else:
        pal_img = Image.open(a.palette).convert("RGBA")
        target = sorted({c for c in pal_img.get_flattened_data() if c[3] > 0}, key=lum)
    if not target:
        raise SystemExit("empty palette")

    used = sorted({c for c in src.get_flattened_data() if c[3] > 0}, key=lum)
    n_t, n_u = len(target), len(used)
    remap = {}
    for i, c in enumerate(used):
        t = target[round(i * (n_t - 1) / max(1, n_u - 1))]
        r, g, b = (min(255, max(0, int(v * a.darken))) for v in t[:3])
        if a.specular > 0 and n_u > 1 and i >= n_u * 0.75:
            k = a.specular * ((i - n_u * 0.75) / max(1e-6, n_u * 0.25))
            r = int(r + (255 - r) * k)
            g = int(g + (255 - g) * k)
            b = int(b + (255 - b) * k)
        remap[c] = (min(255, r), min(255, g), min(255, b), 255)

    out = Image.new("RGBA", src.size, (0, 0, 0, 0))
    for y in range(src.height):
        for x in range(src.width):
            c = src.getpixel((x, y))
            if c[3] > 0:
                out.putpixel((x, y), remap[c])

    os.makedirs(os.path.dirname(os.path.abspath(a.out)), exist_ok=True)
    out.save(a.out)
    print(f"{os.path.basename(a.src)} -> {os.path.basename(a.out)}  "
          f"{n_u} tones onto {n_t} palette colours")


if __name__ == "__main__":
    main()
