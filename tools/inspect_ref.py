import os, sys
from PIL import Image
from collections import Counter

REF = r"C:\mcdev\mods\gleanings\tools\ref"

def lum(c):
    r,g,b = c[:3]
    return 0.2126*r + 0.7152*g + 0.0722*b

def dump(name):
    p = os.path.join(REF, name + ".png")
    im = Image.open(p).convert("RGBA")
    px = list(im.getdata())
    cnt = Counter(px)
    opaque = [(c,n) for c,n in cnt.items() if c[3] > 0]
    opaque.sort(key=lambda t: lum(t[0]))
    print("=== %s  (%dx%d)  colors=%d  opaque=%d ===" % (name, im.width, im.height, len(opaque), sum(n for _,n in opaque)))
    letters = "123456789abcdefg"
    m = {}
    for i,(c,n) in enumerate(opaque):
        m[c] = letters[i] if i < len(letters) else "?"
        print("  %s  #%02x%02x%02x  a=%3d  n=%3d  lum=%5.1f" % (m[c], c[0],c[1],c[2],c[3], n, lum(c)))
    print("  --- map (dark -> light) ---")
    for y in range(im.height):
        row = ""
        for x in range(im.width):
            c = px[y*im.width + x]
            row += "." if c[3] == 0 else m[c]
        print("  |" + row + "|")
    # symmetry score
    w,h = im.size
    same = 0; total = 0
    for y in range(h):
        for x in range(w):
            a = px[y*w+x]; b = px[y*w+(w-1-x)]
            total += 1
            if a == b: same += 1
    print("  horizontal symmetry: %.1f%%" % (100.0*same/total))
    print()

for n in ["rotten_flesh","beef","leather","rabbit_hide"]:
    dump(n)
