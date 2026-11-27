#!/usr/bin/env python3
"""Mock: home screen with the '今日は何の日' seasonal card (design proposal)."""
from PIL import Image, ImageDraw, ImageFont

SANS = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"
SANSB = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"

def font(size, bold=False):
    return ImageFont.truetype(SANSB if bold else SANS, size)

W, H = 900, 1500
BG = (245, 246, 250)
MID = (0x22, 0x35, 0x5E)
MID_D = (0x18, 0x27, 0x47)
GOLD = (0xC9, 0xA8, 0x6A)
WHITE = (255, 255, 255)
INK = (0x24, 0x24, 0x24)
SUB = (0x5A, 0x5A, 0x5A)
SEASON = (0xC2, 0x18, 0x5B)      # seasonal accent (pink)
SEASON_BG = (0xFC, 0xE7, 0xF1)
TEAL = (0x12, 0xA5, 0x94)

img = Image.new("RGB", (W, H), BG)
d = ImageDraw.Draw(img)

def card(x, y, w, h, fill, radius=36):
    d.rounded_rectangle([x, y, x + w, y + h], radius=radius, fill=fill)

def text(x, y, s, size, color=INK, bold=False, anchor="la"):
    d.text((x, y), s, font=font(size, bold), fill=color, anchor=anchor)

# --- Midnight panel (existing home) ---
card(40, 40, W - 80, 430, MID)
d.ellipse([90, 110, 240, 260], fill=(0x58, 0xCC, 0x02))          # owl placeholder
d.ellipse([125, 145, 155, 175], fill=WHITE); d.ellipse([180, 145, 210, 175], fill=WHITE)
d.ellipse([133, 153, 147, 167], fill=INK); d.ellipse([188, 153, 202, 167], fill=INK)
text(280, 120, "今日も5問、いってみよう！", 34, WHITE, bold=True)
text(90, 300, "Lv. 12", 54, WHITE, bold=True)
text(330, 322, "駆け出しオトナ", 32, WHITE)
d.rounded_rectangle([90, 400, W - 130, 428], radius=14, fill=MID_D)
d.rounded_rectangle([90, 400, 560, 428], radius=14, fill=GOLD)
text(90, 500 - 24, "", 10, WHITE)

# --- Seasonal card (NEW) ---
cy = 530
card(40, cy, W - 80, 400, WHITE)
d.rounded_rectangle([40, cy, W - 40, cy + 400], radius=36, outline=SEASON, width=4)
# calendar icon
ix, iy = 80, cy + 45
d.rounded_rectangle([ix, iy, ix + 92, iy + 92], radius=14, outline=SEASON, width=6)
d.rectangle([ix, iy + 26, ix + 92, iy + 32], fill=SEASON)
text(ix + 46, iy + 40, "27", 40, SEASON, bold=True, anchor="ma")
text(ix + 120, iy + 2, "今日は何の日", 40, INK, bold=True)
# period chip
chip_x = ix + 120
d.rounded_rectangle([chip_x, iy + 58, chip_x + 330, iy + 100], radius=21, fill=SEASON_BG)
text(chip_x + 165, iy + 66, "お歳暮シーズン 〜12/20", 28, SEASON, bold=True, anchor="ma")
# one-liner
text(80, cy + 175, "お歳暮は、贈るなら12月上旬までが目安。", 33, INK)
text(80, cy + 225, "のしの表書きは「お歳暮」、水引は紅白の", 33, INK)
text(80, cy + 275, "蝶結びが基本です。", 33, INK)
d.rounded_rectangle([80, cy + 330, 560, cy + 378], radius=24, fill=SEASON_BG)
text(320, cy + 339, "今日の5問に 季節の1問 入り", 27, SEASON, bold=True, anchor="ma")

# --- Start button ---
by = cy + 450
d.rounded_rectangle([40, by, W - 40, by + 110], radius=28, fill=MID)
text(W // 2, by + 30, "今日の5問をはじめる", 38, WHITE, bold=True, anchor="ma")

# --- Bookshelf hint ---
sy = by + 170
card(40, sy, W - 80, 210, WHITE)
text(80, sy + 30, "ジャンルの本棚", 32, INK, bold=True)
for r in range(2):
    yy = sy + 100 + r * 55
    for b in range(10):
        xx = 90 + b * 72
        col = TEAL if (r == 0 and b < 2) else (0xC9, 0xCF, 0xD8)
        if r == 0 and b < 2:
            d.rounded_rectangle([xx, yy - 34, xx + 34, yy], radius=5, fill=col)
        else:
            d.rounded_rectangle([xx, yy - 34, xx + 34, yy], radius=5, outline=col, width=3)
    d.rectangle([80, yy + 2, W - 120, yy + 10], fill=(0x92, 0x6C, 0x42))

img.save("/home/hatch/workspace/otona-level/design-work/seasonal-mock.png")
print("saved")
