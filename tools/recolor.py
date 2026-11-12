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
    ap.add_argument("--palette", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--darken", type=float, default=1.0)
    a = ap.parse_args()

    src = Image.open(a.src).convert("RGBA")
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
        remap[c] = (r, g, b, 255)

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
