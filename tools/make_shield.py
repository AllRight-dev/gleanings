"""Draw the heavy shield icon: a tall kite shield in dark steel, with the core as a boss.

Vanilla has no item texture for a shield (it is drawn by a block-entity renderer),
so there is nothing to derive from. Stating the silhouette as a mask and shading by
distance-to-edge gives a controllable shape that reads at 16px.

    python make_shield.py --out heavy_shield.png
"""
import argparse
import os

from PIL import Image

MASK = [
    "................",
    "..XXXXXXXXXXXX..",
    ".XXXXXXXXXXXXXX.",
    ".XXXXXXXXXXXXXX.",
    ".XXXXXXXXXXXXXX.",
    ".XXXXXXXXXXXXXX.",
    ".XXXXXXXXXXXXXX.",
    "..XXXXXXXXXXXX..",
    "..XXXXXXXXXXXX..",
    "...XXXXXXXXXX...",
    "....XXXXXXXX....",
    ".....XXXXXX.....",
    "......XXXX......",
    ".......XX.......",
    "................",
    "................",
]

RIM = (18, 22, 30, 255)
FACE = (43, 50, 64, 255)
FACE_LIGHT = (64, 74, 92, 255)
BOSS_DARK = (26, 30, 40, 255)
BOSS = (70, 82, 104, 255)
BOSS_LIGHT = (139, 155, 178, 255)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--out", required=True)
    a = ap.parse_args()

    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()

    width = len(MASK)
    for y in range(width):
        for x in range(16):
            if MASK[y][x] != "X":
                continue
            # border = any 4-neighbour outside the mask
            edge = False
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if not (0 <= nx < 16 and 0 <= ny < 16) or MASK[ny][nx] != "X":
                    edge = True
                    break
            if edge:
                px[x, y] = RIM
            elif x < 8:
                px[x, y] = FACE_LIGHT
            else:
                px[x, y] = FACE

    # Boss: the heavy core, sitting in the middle of the face.
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            dist = (dx * dx + dy * dy) ** 0.5
            x, y = 7 + dx, 6 + dy
            if not (0 <= x < 16 and 0 <= y < 16):
                continue
            if dist > 2.4:
                continue
            if dist > 1.5:
                px[x, y] = BOSS_DARK
            elif dx <= 0 and dy <= 0:
                px[x, y] = BOSS_LIGHT
            else:
                px[x, y] = BOSS

    os.makedirs(os.path.dirname(os.path.abspath(a.out)), exist_ok=True)
    img.save(a.out)
    print(f"wrote {a.out}  opaque={sum(1 for c in img.get_flattened_data() if c[3] > 0)}")


if __name__ == "__main__":
    main()
