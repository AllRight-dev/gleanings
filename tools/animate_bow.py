"""Build an animated bow icon: a dark blue orb pulsing inside the bow, in the hotbar.

Minecraft animates any texture that has a .png.mcmeta next to it. The texture
becomes a vertical strip of frames stacked top to bottom; frame 0 is the plain
bow, later frames add the orb.

    python animate_bow.py --src resonance_bow.png --out resonance_bow.png
"""
import argparse
import os

from PIL import Image

# Where the orb sits on the vanilla bow silhouette (16x16): the gap inside the arc.
ORB_X, ORB_Y = 6, 9

# radius, core colour, rim colour, extra sparks per frame
FRAMES = [
    (0, None, None, 0),
    (1, (34, 58, 102), (62, 104, 158), 0),
    (2, (44, 76, 132), (84, 136, 194), 1),
    (2, (52, 90, 152), (104, 160, 214), 1),
    (1, (34, 58, 102), (62, 104, 158), 0),
]

SPARK_POSITIONS = [(-3, -1), (3, 1)]


def draw_orb(frame, radius, core, rim, sparks):
    px = frame.load()
    if radius <= 0:
        return frame
    for dy in range(-radius, radius + 1):
        for dx in range(-radius, radius + 1):
            dist = (dx * dx + dy * dy) ** 0.5
            if dist > radius + 0.35:
                continue
            x, y = ORB_X + dx, ORB_Y + dy
            if not (0 <= x < 16 and 0 <= y < 16):
                continue
            colour = rim if dist > radius - 0.9 else core
            px[x, y] = (colour[0], colour[1], colour[2], 255)
    for i in range(sparks):
        sx, sy = SPARK_POSITIONS[i % len(SPARK_POSITIONS)]
        x, y = ORB_X + sx, ORB_Y + sy
        if 0 <= x < 16 and 0 <= y < 16:
            px[x, y] = (120, 170, 225, 255)
    return frame


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--src", required=True)
    ap.add_argument("--out", required=True)
    a = ap.parse_args()

    base = Image.open(a.src).convert("RGBA")
    if base.size != (16, 16):
        raise SystemExit(f"expected a 16x16 source, got {base.size}")

    strip = Image.new("RGBA", (16, 16 * len(FRAMES)), (0, 0, 0, 0))
    for i, (radius, core, rim, sparks) in enumerate(FRAMES):
        frame = base.copy()
        strip.paste(draw_orb(frame, radius, core, rim, sparks), (0, i * 16))
    strip.save(a.out)

    mcmeta = os.path.splitext(a.out)[0] + ".png.mcmeta"
    with open(mcmeta, "w", encoding="utf-8", newline="\n") as f:
        f.write('{\n  "animation": {\n    "frametime": 2\n  }\n}\n')

    print(f"wrote {a.out}  {strip.size[0]}x{strip.size[1]}  frames={len(FRAMES)}")
    print(f"wrote {mcmeta}")


if __name__ == "__main__":
    main()
