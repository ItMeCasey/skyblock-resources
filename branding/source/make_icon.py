"""Draws the Skyblock Resources icon as 32x32 pixel art and writes it at several sizes.

A diamond cube frame with empty sides, in the style of the CurseForge logo.
Run from the repository root: python branding/source/make_icon.py
Writes branding/icon/icon_<size>.png, icon.svg and skyblock_logo.png (for the jar).
"""
import math
from pathlib import Path

from PIL import Image

N = 32
OUT = Path(__file__).resolve().parent.parent / "icon"

BG = (24, 22, 38)
BG_EDGE = (46, 42, 70)
BG_TOP = (58, 54, 88)
TEAL = (74, 226, 255)
TEAL_HI = (205, 252, 255)
TEAL_LO = (28, 120, 150)
GLASS = [(36, 70, 90), (30, 60, 80), (24, 50, 70)]
STONE = [(104, 104, 112), (88, 88, 96), (124, 124, 132)]
STONE_DARK = (58, 58, 66)
DIRT = [(139, 94, 52), (122, 82, 45), (150, 104, 60)]
GRASS = (109, 187, 69)
GOLD = (242, 179, 61)

px = [[None] * N for _ in range(N)]


def put(x, y, c):
    if 0 <= x < N and 0 <= y < N:
        px[y][x] = c


def in_poly(x, y, pts):
    inside = False
    for i in range(len(pts)):
        x1, y1 = pts[i]
        x2, y2 = pts[(i + 1) % len(pts)]
        if (y1 > y) != (y2 > y) and x < (x2 - x1) * (y - y1) / (y2 - y1) + x1:
            inside = not inside
    return inside


# Background tile with rounded corners and a lit top edge.
R = 4
for y in range(N):
    for x in range(N):
        cx = min(max(x, R - 0.5), N - R - 0.5)
        cy = min(max(y, R - 0.5), N - R - 0.5)
        if math.hypot(x - cx, y - cy) <= R:
            px[y][x] = BG
for y in range(N):
    for x in range(N):
        if px[y][x] is None:
            continue
        edge = any(
            not (0 <= x + dx < N and 0 <= y + dy < N) or px[y + dy][x + dx] is None
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))
        )
        if edge:
            px[y][x] = BG_TOP if y < N // 2 else BG_EDGE

# Isometric cube: top, left and right faces. Coordinates in pixels.
T, L, Rt, C = (16, 4), (4, 10), (28, 10), (16, 16)
BL, BR, B = (4, 22), (28, 22), (16, 28)
TOP = [T, Rt, C, L]
LEFT = [L, C, B, BL]
RIGHT = [C, Rt, BR, B]

# Glass faces, dimmer toward the bottom.
for y in range(N):
    for x in range(N):
        fx, fy = x + 0.5, y + 0.5
        if in_poly(fx, fy, TOP):
            put(x, y, (44, 84, 106))
        elif in_poly(fx, fy, LEFT):
            put(x, y, GLASS[0] if fy < 18 else GLASS[1])
        elif in_poly(fx, fy, RIGHT):
            put(x, y, GLASS[2] if fy < 18 else GLASS[1])

# Glass edges drawn over everything so the cube reads as glass.
def line(a, b, c):
    (x1, y1), (x2, y2) = a, b
    steps = int(max(abs(x2 - x1), abs(y2 - y1)))
    for i in range(steps + 1):
        t = i / steps
        put(round(x1 + (x2 - x1) * t), round(y1 + (y2 - y1) * t), c)


for a, b in ((T, Rt), (Rt, BR), (BR, B), (B, BL), (BL, L), (L, T), (C, L), (C, Rt), (C, B)):
    line(a, b, TEAL)
# Highlight along the top-left edges.
line(T, L, TEAL_HI)
for x, y in ((8, 14), (9, 13), (10, 12)):
    put(x, y, TEAL_HI)

# Gold sparkle, top right.
for x, y in ((26, 4), (25, 5), (27, 5), (26, 6)):
    put(x, y, GOLD)

img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
for y in range(N):
    for x in range(N):
        if px[y][x] is not None:
            img.putpixel((x, y), px[y][x] + (255,))

OUT.mkdir(parents=True, exist_ok=True)
for size in (32, 64, 128, 256, 512, 1024):
    img.resize((size, size), Image.NEAREST).save(OUT / f"icon_{size}.png", optimize=True)
img.resize((512, 512), Image.NEAREST).save(OUT / "skyblock_logo.png", optimize=True)

rects = []
for y in range(N):
    for x in range(N):
        c = px[y][x]
        if c is not None:
            rects.append(f'<rect x="{x}" y="{y}" width="1.02" height="1.02" fill="#{c[0]:02x}{c[1]:02x}{c[2]:02x}"/>')
(OUT / "icon.svg").write_text(
    f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {N} {N}" shape-rendering="crispEdges">'
    + "".join(rects) + "</svg>\n",
    encoding="utf-8",
)
print("wrote", OUT)
