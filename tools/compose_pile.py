"""Build a "broken chips" sprite by pasting real vanilla fragments together.

Why not a model: a diffusion model has no stable visual prior for "a heap of
jagged chips" - it either draws a crystal cluster or a 3D cube. But vanilla
already contains properly hand-shaded angular pieces; cutting them up and
piling them is deterministic, licence-clean (A class) and reviewable.

    python compose_pile.py --out dregs.png --palette amethyst_shard.png
"""
import argparse
import os

from PIL import Image


def lum(c):
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]


def load_ramp(path):
    im = Image.open(path).convert("RGBA")
    return sorted({c for c in im.get_flattened_data() if c[3] > 0}, key=lum)


def recolor(im, ramp, band):
    """Map an opaque sprite onto a slice of the ramp, so each chip sits at its own value."""
    used = sorted({c for c in im.get_flattened_data() if c[3] > 0}, key=lum)
    if not used:
        return im
    lo = int(band[0] * (len(ramp) - 1))
    hi = int(band[1] * (len(ramp) - 1))
    sub = ramp[lo:hi + 1] or ramp
    n_u, n_t = len(used), len(sub)
    remap = {}
    for i, c in enumerate(used):
        t = sub[round(i * (n_t - 1) / max(1, n_u - 1))]
        remap[c] = t
    out = Image.new("RGBA", im.size, (0, 0, 0, 0))
    for y in range(im.height):
        for x in range(im.width):
            c = im.getpixel((x, y))
            if c[3] > 0:
                out.putpixel((x, y), remap[c])
    return out


def chip(path, w, h, ramp, band):
    im = Image.open(path).convert("RGBA")
    im = im.crop(im.getbbox())
    im = recolor(im, ramp, band)
    return im.resize((w, h), Image.NEAREST)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", required=True)
    ap.add_argument("--palette", required=True)
    ap.add_argument("--flint", required=True)
    ap.add_argument("--shard", required=True)
    a = ap.parse_args()

    ramp = load_ramp(a.palette)

    # Bottom->top stacking order. Each chip gets a different value band so the
    # pile reads as separate pieces instead of one blob.
    pieces = [
        # bottom layer: darker, wider, overlaps into one heap silhouette
        (a.flint, 11, 7, (0.05, 0.40), 2, 8),
        (a.flint, 7, 6, (0.10, 0.50), 8, 7),
        (a.shard, 8, 5, (0.15, 0.55), 1, 6),
        # middle layer
        (a.flint, 7, 6, (0.40, 0.80), 5, 5),
        (a.shard, 6, 5, (0.45, 0.85), 10, 4),
        # top layer: brightest, reads as the lit face of the heap
        (a.flint, 6, 5, (0.70, 1.00), 4, 3),
        (a.shard, 4, 4, (0.80, 1.00), 9, 2),
        # stray chips at the edges
        (a.shard, 3, 3, (0.30, 0.60), 0, 12),
        (a.flint, 3, 2, (0.35, 0.65), 12, 11),
    ]

    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for path, w, h, band, x, y in pieces:
        out.alpha_composite(chip(path, w, h, ramp, band), (x, y))

    os.makedirs(os.path.dirname(os.path.abspath(a.out)), exist_ok=True)
    out.save(a.out)
    print(f"wrote {a.out}  opaque={sum(1 for c in out.get_flattened_data() if c[3] > 0)}")


if __name__ == "__main__":
    main()
