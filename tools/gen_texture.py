"""Generate the 16x16 item texture for washed_rotten_flesh.

Deterministic: the sprite is written out as ASCII so the art is reviewable in git
instead of being a binary blob nobody can diff.
"""
import os
from PIL import Image

PALETTE = {
    "o": (46, 33, 26, 255),     # outline
    "b": (156, 132, 116, 255),  # scrubbed flesh
    "l": (184, 163, 148, 255),  # highlight
    "d": (122, 99, 85, 255),    # shadow
    "w": (134, 169, 181, 255),  # water stain
}

SPRITE = [
    "................",
    ".....oooooo.....",
    "...oobbbbbboo...",
    "..obbblllbbbbo..",
    ".obblllllbbbbbo.",
    ".obbbwllbbwbbbo.",
    "obbbbbbbbbbbbbbo",
    "obbdbbbbbbdbbdbo",
    "obbbbdbbbwbbbbbo",
    "obbbbbbbbdbbbo..",
    ".obbbbdbbbbbbbo.",
    ".obbwbbbbdbbbbo.",
    "..obbbbbbbbbbo..",
    "...oobbbbbboo...",
    ".....oooooo.....",
    "................",
]

OUT = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "src", "main", "resources", "assets", "gleanings", "textures", "item", "washed_rotten_flesh.png",
)


def main() -> None:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(SPRITE):
        assert len(row) == 16, f"row {y} has {len(row)} chars"
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            px[x, y] = PALETTE[ch]
    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    img.save(OUT)
    print("wrote", OUT, os.path.getsize(OUT), "bytes")


if __name__ == "__main__":
    main()
