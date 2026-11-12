"""Measure a 16x16 sprite against the numbers vanilla item textures actually hit.

"Looks bad" is not actionable; these are:
  colours      <= 12      (vanilla 5..11)
  fill         100..135   (vanilla 108..128 of 256)
  symmetry     <= 55%     (vanilla rotten_flesh 40.6%, leather 75.8%)
  bright share <= 8%      (vanilla highlights are rare flecks, not areas)
  dark share   >= 22%     (vanilla silhouettes carry their own shadow)

Usage: python sprite_check.py <sprite.png> [<other.png> ...]
"""
import os
import sys
from collections import Counter

from PIL import Image


def luminance(c):
    return 0.2126 * c[0] + 0.7152 * c[1] + 0.0722 * c[2]


def report(path):
    im = Image.open(path).convert("RGBA")
    w, h = im.size
    px = list(im.getdata())
    opaque = [c for c in px if c[3] > 0]
    counts = Counter(opaque)
    palette = sorted(counts, key=luminance)
    n = len(palette)
    total = len(opaque)

    bright = sum(counts[c] for c in palette[-2:]) / total * 100 if total else 0
    dark = sum(counts[c] for c in palette[:2]) / total * 100 if total else 0

    same = sum(
        1 for y in range(h) for x in range(w) if px[y * w + x] == px[y * w + (w - 1 - x)]
    )
    symmetry = same / (w * h) * 100

    # Border-blur: how many edge pixels are fully transparent (vanilla silhouettes
    # do touch the canvas border; a sprite floating 2px inside looks like a sticker).
    edge = [px[y * w + x] for y in range(h) for x in range(w) if x in (0, w - 1) or y in (0, h - 1)]
    edge_opaque = sum(1 for c in edge if c[3] > 0)

    print(f"--- {os.path.basename(path)} ---")
    print(f"  colours      {n:3d}   {'OK' if n <= 12 else 'FAIL (>12)'}")
    print(f"  fill         {total:3d}   {'OK' if 100 <= total <= 135 else 'FAIL (want 100..135)'}")
    print(f"  symmetry     {symmetry:5.1f}%  {'OK' if symmetry <= 55 else 'FAIL (>55%, reads as a sticker)'}")
    print(f"  bright share {bright:5.1f}%  {'OK' if bright <= 8 else 'FAIL (highlight is an area, not a fleck)'}")
    print(f"  dark share   {dark:5.1f}%  {'OK' if dark >= 22 else 'FAIL (no shadow, looks flat)'}")
    print(f"  border px    {edge_opaque:3d}")
    print()


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    for p in sys.argv[1:]:
        report(p)
