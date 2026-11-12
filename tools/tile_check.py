"""Measure whether a 16x16 block texture will tile without a *visible* seam.

Two false starts are baked into this file's history, and both are worth knowing:

1. "The edge columns must be similar" is wrong. Vanilla stone's left and right
   columns differ more than 100% of its interior column pairs, yet stone walls
   look fine. High-frequency noise does not read as a line.
2. Comparing the seam to interior detail without low-pass filtering is wrong
   for the same reason: noise swamps the measurement.

What actually reads as a seam is a **low-frequency** step: a brightness or hue
jump that persists along the whole edge. So: tile 3x3, blur the tiled image,
crop the centre tile, and only then compare the seam step to the interior steps.
Blurring after tiling (not before) is what makes the edge honest - a blur run on
a single tile would smear the border into itself and hide the very thing we test.

    python tile_check.py stone.png [more.png ...]
"""
import os
import sys

import numpy as np
from PIL import Image, ImageFilter


def lowpass_centre(im, radius=2):
    """Tile 3x3, blur, return the centre tile. Edge pixels then have real neighbours."""
    w, h = im.size
    tiled = Image.new("RGB", (w * 3, h * 3))
    for ty in range(3):
        for tx in range(3):
            tiled.paste(im, (tx * w, ty * h))
    blurred = tiled.filter(ImageFilter.BoxBlur(radius))
    return np.asarray(blurred.crop((w, h, w * 2, h * 2))).astype(np.float32)


def analyse(path, radius=2):
    im = Image.open(path).convert("RGB")
    a = lowpass_centre(im, radius)

    out = []
    for axis in (1, 0):
        interior = np.abs(np.diff(a, axis=axis)).mean(axis=tuple(x for x in (0, 1, 2) if x != axis))
        seam = np.abs(np.take(a, 0, axis=axis) - np.take(a, -1, axis=axis)).mean()
        if interior.size == 0 or interior.max() <= 1e-6:
            out.append((float(seam), 0.0))
            continue
        # Compare against the LOUDEST interior step, not a percentile: there are
        # only ~15 interior samples, so "percentile 100" is what a single random
        # sample hits 1 time in 16. Ratio to the max is stable at this sample size.
        ratio = float(seam / interior.max())
        out.append((float(seam), ratio))

    worst = max(r for _, r in out)
    score = max(0.0, min(100.0, 100.0 * (1.0 - max(0.0, worst - 1.0))))
    return out, score


def main(paths):
    for p in paths:
        res, score = analyse(p)
        (h_seam, h_pct), (v_seam, v_pct) = res
        verdict = "OK" if score >= 80 else "FAIL (low-frequency step at the edge)"
        print(f"--- {os.path.basename(p)} ---")
        print(f"  horizontal seam step {h_seam:6.2f}  ratio-to-max {h_pct:5.2f}")
        print(f"  vertical   seam step {v_seam:6.2f}  ratio-to-max {v_pct:5.2f}")
        print(f"  tileability          {score:5.1f}/100  {verdict}")
        print()


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    main(sys.argv[1:])
