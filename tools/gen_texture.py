"""Generate washed_rotten_flesh.png by *deriving* it from the vanilla sprite.

Why derive instead of draw freehand:
  A vanilla-plus item must be recognisable as "the same thing, changed".
  Drawing a new blob breaks that, and freehand 16x16 always comes out too
  symmetrical and too light. So we take vanilla rotten_flesh.png, keep its
  silhouette and its internal value structure pixel for pixel, and only
  remap the colour ramp (desaturated + cooled) and drop three water beads
  into the darkest crevices.

Measured facts about the vanilla reference (tools/inspect_ref.py):
  11 colours, 119 opaque pixels, horizontal symmetry 40.6%,
  luminance 23..156, rarest colour is the brightest (n=1).
Those numbers are the target; sprite_check.py verifies them.
"""
import os
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
REF = os.path.join(HERE, "ref", "rotten_flesh.png")
OUT = os.path.join(
    HERE, "..", "src", "main", "resources", "assets", "gleanings",
    "textures", "item", "washed_rotten_flesh.png",
)

# How far to pull each vanilla colour towards neutral grey, and how much
# blue to add to the top of the ramp (the "still wet" cue).
DESATURATE = 0.42
COOL_TOP_STEPS = 3
COOL_SHIFT = (6, 7, 12)

WATER = (121, 154, 168, 255)


def luminance(c):
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]


def washed(c):
    lum = luminance(c)
    out = [int(round(v + (lum - v) * DESATURATE)) for v in c[:3]]
    return tuple(max(0, min(255, v)) for v in out)


def main() -> None:
    base = Image.open(REF).convert("RGBA")
    w, h = base.size
    src = list(base.getdata())

    # Ordered palette of the reference, dark -> light. Same index = same shape.
    palette = sorted({c for c in src if c[3] > 0}, key=luminance)
    index_of = {c: i for i, c in enumerate(palette)}
    n = len(palette)
    print(f"reference palette: {n} colours, luminance {luminance(palette[0]):.0f}"
          f"..{luminance(palette[-1]):.0f}")

    remap = {}
    for i, c in enumerate(palette):
        r, g, b = [c[0], c[1], c[2]]
        r, g, b = washed((r, g, b))
        if i >= n - COOL_TOP_STEPS:
            r = max(0, min(255, r + COOL_SHIFT[0]))
            g = max(0, min(255, g + COOL_SHIFT[1]))
            b = max(0, min(255, b + COOL_SHIFT[2]))
        remap[c] = (r, g, b, 255)

    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    px = out.load()
    for y in range(h):
        for x in range(w):
            c = src[y * w + x]
            if c[3] == 0:
                continue
            px[x, y] = remap[c]

    # Water beads: the three darkest interior pixels, pushed apart so they
    # don't clump. Deterministic, so the sprite is reproducible from source.
    candidates = []
    for y in range(1, h - 1):
        for x in range(1, w - 1):
            c = src[y * w + x]
            if c[3] == 0:
                continue
            if index_of[c] <= 1:
                candidates.append((index_of[c], y, x))
    candidates.sort()
    picked = []
    for _, y, x in candidates:
        if all(abs(y - py) + abs(x - px_) >= 4 for py, px_ in picked):
            picked.append((y, x))
        if len(picked) == 3:
            break
    for y, x in picked:
        px[x, y] = WATER
    print("water beads at", picked)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    out.save(OUT)
    print("wrote", os.path.normpath(OUT), os.path.getsize(OUT), "bytes")


if __name__ == "__main__":
    main()
