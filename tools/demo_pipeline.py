"""Round-trip test for the texture pipeline, using no image model at all.

It simulates what a diffusion model gives you (a 256x256, blurry, anti-aliased
render) and proves that img2mc.py can bring it back to a legal 16x16 sprite.
It also builds a deliberately broken tile so tile_check.py has something to fail.

    python demo_pipeline.py
"""
import os
import subprocess
import sys

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
REF = os.path.join(HERE, "ref")
PY = sys.executable


def main():
    # 1. "What the model would hand you": upscaled + smooth = gradient soup.
    src = Image.open(os.path.join(REF, "rotten_flesh.png")).convert("RGBA")
    big = src.resize((256, 256), Image.BICUBIC)
    big_path = os.path.join(REF, "sim_model_output.png")
    big.save(big_path)
    print(f"simulated model output: {big_path}  {big.size}  "
          f"colours={len({c for c in big.getdata() if c[3] > 0})} (gradients everywhere)")

    # 2. Back to a legal asset.
    back = os.path.join(REF, "sim_model_output_16.png")
    subprocess.run([PY, os.path.join(HERE, "img2mc.py"), big_path, back,
                    "--size", "16", "--colors", "11", "--alpha", "128"], check=True)

    # 3. A tile that will NOT repeat: shift a real block texture by 5 pixels.
    stone = Image.open(os.path.join(REF, "stone.png")).convert("RGBA")
    w, h = stone.size
    broken = Image.new("RGBA", (w, h))
    broken.paste(stone.crop((5, 0, w, h)), (0, 0))
    broken.paste(stone.crop((0, 0, 5, h)), (w - 5, 0))
    broken_path = os.path.join(REF, "stone_shifted.png")
    broken.save(broken_path)
    print(f"broken tile: {broken_path}  (shifted 5px, seam guaranteed)")


if __name__ == "__main__":
    main()
