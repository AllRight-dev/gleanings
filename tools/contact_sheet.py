import os
from PIL import Image, ImageDraw

T = r"C:\mcdev\mods\gleanings\tools"
S = 6
items = [
    ("vanilla rotten_flesh", os.path.join(T, "ref", "rotten_flesh.png")),
    ("v1 (mine, bad)",       os.path.join(T, "ref", "candidate_v1_flat.png")),
    ("v2 (derived)",         r"C:\mcdev\mods\gleanings\src\main\resources\assets\gleanings\textures\item\washed_rotten_flesh.png"),
    ("vanilla leather",      os.path.join(T, "ref", "leather.png")),
]
pad, top = 8, 18
cell = 16 * S
W = len(items) * (cell + pad) + pad
H = cell + pad + top + 4
sheet = Image.new("RGBA", (W, H), (34, 34, 38, 255))
d = ImageDraw.Draw(sheet)
for i, (name, path) in enumerate(items):
    im = Image.open(path).convert("RGBA").resize((cell, cell), Image.NEAREST)
    x = pad + i * (cell + pad)
    y = top
    # checkerboard so transparency is visible
    for cy in range(0, cell, S * 2):
        for cx in range(0, cell, S * 2):
            shade = 58 if ((cx // (S * 2)) + (cy // (S * 2))) % 2 == 0 else 46
            d.rectangle([x + cx, y + cy, x + cx + S * 2 - 1, y + cy + S * 2 - 1], fill=(shade, shade, shade, 255))
    sheet.alpha_composite(im, (x, y))
    d.text((x, 4), name, fill=(220, 220, 220, 255))
out = os.path.join(T, "contact_sheet.png")
sheet.save(out)
print("wrote", out, sheet.size)
