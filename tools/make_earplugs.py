"""Draw the earplugs icon: two wool-stuffed plugs on a cord, 16x16.

Procedural rather than hand-placed ASCII, because two discs plus a cord is a
shape you can state exactly - centre, radius, brightness - and then tune by
changing numbers instead of re-drawing pixels.

    python make_earplugs.py --out earplugs.png
"""
import argparse
import os

from PIL import Image

OUTLINE = (74, 53, 36, 255)
BODY = (226, 213, 186, 255)      # wool cream
HIGHLIGHT = (246, 240, 226, 255)
CORD = (104, 100, 96, 255)

PLUGS = [
    (4, 4, 3.2),
    (11, 11, 3.2),
]
CORD_FROM = (4, 8)
CORD_TO = (11, 8)


def disc(px, cx, cy, radius):
    for dy in range(-4, 5):
        for dx in range(-4, 5):
            dist = (dx * dx + dy * dy) ** 0.5
            x, y = cx + dx, cy + dy
            if not (0 <= x < 16 and 0 <= y < 16):
                continue
            if dist > radius + 0.35:
                continue
            if dist > radius - 0.95:
                px[x, y] = OUTLINE
            elif dx <= 0 and dy <= 0 and dist < radius - 1.8:
                px[x, y] = HIGHLIGHT
            else:
                px[x, y] = BODY


def cord(px):
    x0, y0 = CORD_FROM
    x1, y1 = CORD_TO
    steps = max(abs(x1 - x0), abs(y1 - y0))
    for i in range(steps + 1):
        x = round(x0 + (x1 - x0) * i / steps)
        y = round(y0 + (y1 - y0) * i / steps)
        if 0 <= x < 16 and 0 <= y < 16 and px[x, y][3] == 0:
            px[x, y] = CORD


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", required=True)
    a = ap.parse_args()

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    cord(px)
    for cx, cy, r in PLUGS:
        disc(px, cx, cy, r)

    os.makedirs(os.path.dirname(os.path.abspath(a.out)), exist_ok=True)
    img.save(a.out)
    opaque = sum(1 for c in img.get_flattened_data() if c[3] > 0)
    print(f"wrote {a.out}  opaque={opaque}")


if __name__ == "__main__":
    main()
