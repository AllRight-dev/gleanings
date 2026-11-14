"""Stamp a condensing orb onto the three bow pull frames.

Vanilla's draw animation swaps through bow_pulling_0/1/2: the limbs flex and the
string comes back. The orb has to grow with them, or it reads as a separate
overlay instead of something the bow is gathering.

    python stamp_pull_orbs.py
"""
import os

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ITEM = os.path.join(HERE, "..", "src", "main", "resources", "assets", "gleanings", "textures", "item")
REF = os.path.join(HERE, "ref")

ORB_X, ORB_Y = 6, 9

# (source name, output name, radius, core, rim, sparks)
PULLS = [
    ("bow_pulling_0", "resonance_bow_pulling_0", 1, (40, 66, 112), None, 0),
    ("bow_pulling_1", "resonance_bow_pulling_1", 2, (44, 76, 132), (76, 124, 182), 1),
    ("bow_pulling_2", "resonance_bow_pulling_2", 2, (52, 90, 152), (104, 160, 214), 2),
]

SPARK_AT = [(-3, -1), (2, 2)]


def stamp(img, radius, core, rim, sparks):
    px = img.load()
    for dy in range(-radius, radius + 1):
        for dx in range(-radius, radius + 1):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist > radius + 0.35:
                continue
            x, y = ORB_X + dx, ORB_Y + dy
            if not (0 <= x < 16 and 0 <= y < 16):
                continue
            colour = rim if (rim and dist > radius - 0.9) else core
            px[x, y] = (colour[0], colour[1], colour[2], 255)
    for i in range(sparks):
        sx, sy = SPARK_AT[i % len(SPARK_AT)]
        x, y = ORB_X + sx, ORB_Y + sy
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = (120, 170, 225, 255)
    return img


def main():
    for src_name, out_name, radius, core, rim, sparks in PULLS:
        base = Image.open(os.path.join(REF, f"v_{src_name}.png")).convert("RGBA")
        # Re-derive the amethyst version from vanilla, so re-runs are idempotent.
        pal = Image.open(os.path.join(REF, "amethyst_shard.png")).convert("RGBA")
        target = sorted({c for c in pal.get_flattened_data() if c[3] > 0},
                        key=lambda c: 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2])
        used = sorted({c for c in base.get_flattened_data() if c[3] > 0},
                      key=lambda c: 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2])
        remap = {}
        for i, c in enumerate(used):
            t = target[round(i * (len(target) - 1) / max(1, len(used) - 1))]
            remap[c] = (t[0], t[1], t[2], 255)
        tinted = Image.new("RGBA", base.size, (0, 0, 0, 0))
        for y in range(16):
            for x in range(16):
                c = base.getpixel((x, y))
                if c[3] > 0:
                    tinted.putpixel((x, y), remap[c])

        out = stamp(tinted, radius, core, rim, sparks)
        out.save(os.path.join(ITEM, out_name + ".png"))
        print(f"{out_name}.png  radius={radius} sparks={sparks}")


if __name__ == "__main__":
    main()
