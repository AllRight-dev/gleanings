"""Render a 16x16 block texture tiled 4x4 at 8x, so a seam is impossible to miss.

Why this exists: three attempts at a numeric "seam detector" all failed
calibration (see tile_check.py header). At 16x16 the honest test is visual -
tile it, look at it. This builds the picture you look at.

    python tile_sheet.py stone.png gradient.png ...
"""
import os
import sys

from PIL import Image

SCALE = 8
TILES = 4
LABEL = 14


def sheet(paths, out):
    cells = []
    for p in paths:
        im = Image.open(p).convert("RGBA")
        small = Image.new("RGBA", (16 * TILES, 16 * TILES))
        for ty in range(TILES):
            for tx in range(TILES):
                small.paste(im, (tx * 16, ty * 16))
        cells.append((os.path.basename(p), small.resize(
            (16 * TILES * SCALE, 16 * TILES * SCALE), Image.NEAREST)))

    cw = cells[0][1].width
    pad = 10
    W = len(cells) * (cw + pad) + pad
    H = cells[0][1].height + LABEL + pad
    out_im = Image.new("RGBA", (W, H), (28, 28, 32, 255))
    from PIL import ImageDraw
    d = ImageDraw.Draw(out_im)
    for i, (name, img) in enumerate(cells):
        x = pad + i * (cw + pad)
        out_im.alpha_composite(img, (x, LABEL))
        d.text((x, 2), name, fill=(225, 225, 225, 255))
    out_im.save(out)
    print("wrote", out, out_im.size)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    root = os.path.dirname(os.path.abspath(__file__))
    sheet(sys.argv[1:], os.path.join(root, "tiling_sheet.png"))
