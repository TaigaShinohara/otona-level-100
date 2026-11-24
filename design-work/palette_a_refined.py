# Palette proposals for Otona Level 100 — one comparison image.
# Draws the app's home screen (same layout as the real one) in 4 palettes.
from PIL import Image, ImageDraw, ImageFont

FB = "/usr/share/fonts/opentype/noto/NotoSansCJK-Bold.ttc"
FR = "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc"

def font(path, size):
    return ImageFont.truetype(path, size, index=2)  # JP face in the TTC

def hx(c):
    return tuple(int(c[i:i+2], 16) for i in (1, 3, 5))

WHITE = (255, 255, 255)
BG = (247, 247, 247)
TEXT = (60, 60, 60)
GREY = (119, 119, 119)
CARD_BORDER = (229, 229, 229)

PALETTES = [
    ("A1", "深めネイビー", "Aを少しだけ深く・静かに", "#2C4A8A", "#21386A", "#7FB2E5", "#E9EEF7"),
    ("A2", "ミッドナイト＋金", "もっと深く、差し色は金", "#22355E", "#182747", "#C9A86A", "#ECEEF3"),
]

def rr(d, box, rad, **kw):
    d.rounded_rectangle(box, radius=rad, **kw)

def draw_phone(img, x, y, w, h, pal):
    _, _, _, primary, dark, accent, tint = pal
    P, D, A, T = hx(primary), hx(dark), hx(accent), hx(tint)
    d = ImageDraw.Draw(img)
    # phone frame
    rr(d, [x, y, x + w, y + h], 28, fill=BG, outline=(200, 200, 200), width=2)
    m = 14  # inner margin
    # header card
    hx0, hy0, hx1, hy1 = x + m, y + m, x + w - m, y + int(h * 0.60)
    rr(d, [hx0, hy0, hx1, hy1], 22, fill=P)
    pad = 18
    # speech bubble
    bub = "今日も5問、いってみよう！"
    bf = font(FB, 17)
    tw = d.textlength(bub, font=bf)
    rr(d, [hx0 + pad, hy0 + 16, hx0 + pad + tw + 22, hy0 + 50], 14, fill=WHITE)
    d.text((hx0 + pad + 11, hy0 + 24), bub, font=bf, fill=D)
    d.text((hx0 + pad, hy0 + 62), "オトナレベル100", font=font(FB, 15), fill=WHITE)
    d.text((hx0 + pad, hy0 + 84), "Lv. 1", font=font(FB, 52), fill=WHITE)
    d.text((hx0 + pad, hy0 + 146), "見習いオトナ", font=font(FB, 20), fill=WHITE)
    # progress bar: dark track, accent fill
    by = hy0 + 178
    rr(d, [hx0 + pad, by, hx1 - pad, by + 13], 7, fill=D)
    rr(d, [hx0 + pad, by, hx0 + pad + int((hx1 - hx0 - 2 * pad) * 0.22), by + 13], 7, fill=A)
    d.text((hx0 + pad, by + 19), "あと正解 約5問 でレベルアップ", font=font(FR, 12), fill=WHITE)
    # start button (chunky: dark edge underneath)
    btn_y = hy1 + 16
    rr(d, [hx0, btn_y + 5, hx1, btn_y + 57], 16, fill=D)
    rr(d, [hx0, btn_y, hx1, btn_y + 52], 16, fill=P)
    label = "今日の5問をはじめる"
    lf = font(FB, 18)
    lw = d.textlength(label, font=lf)
    d.text((x + w / 2 - lw / 2, btn_y + 14), label, font=lf, fill=WHITE)
    d.text((hx0 + 2, btn_y + 66), "1セット5問・約1分。レベルは下がりません", font=font(FR, 12), fill=GREY)
    # tab bar
    tb_y = y + h - 58
    rr(d, [hx0, tb_y, hx1, tb_y + 44], 22, fill=WHITE, outline=CARD_BORDER, width=2)
    seg = (hx1 - hx0) / 3
    rr(d, [hx0 + 4, tb_y + 4, hx0 + seg - 4, tb_y + 40], 18, fill=T)
    tf = font(FB, 15)
    for i, t in enumerate(["ホーム", "復習", "カテゴリ"]):
        twd = d.textlength(t, font=tf)
        col = A if i == 0 else GREY
        d.text((hx0 + seg * i + seg / 2 - twd / 2, tb_y + 12), t, font=tf, fill=col)

def main():
    W, H = 1560, 720
    img = Image.new("RGB", (W, H), WHITE)
    d = ImageDraw.Draw(img)
    d.text((60, 36), "A案ネイビー — 「もう少しオトナっぽく」版", font=font(FB, 34), fill=TEXT)
    d.text((60, 88), "元のA（#3B5BDB）より彩度を落として深くした2段階。左が少しだけ、右がしっかり深め＋金の差し色", font=font(FR, 19), fill=GREY)
    pw, ph = 660, 490
    positions = [(60, 150), (840, 150)]
    for pal, (px, py) in zip(PALETTES, positions):
        code, name, mood, primary, dark, accent, tint = pal
        d.text((px, py), f"{code}案", font=font(FB, 26), fill=hx(primary))
        nw = d.textlength(f"{code}案", font=font(FB, 26))
        d.text((px + nw + 14, py + 4), f"{name} — {mood}", font=font(FB, 21), fill=TEXT)
        draw_phone(img, px, py + 44, pw, ph - 110, pal)
        # swatches
        sy = py + ph - 52
        chips = [("メイン", primary), ("濃い色", dark), ("アクセント", accent), ("淡い色", tint)]
        cx = px
        for label, c in chips:
            rr(d, [cx, sy, cx + 34, sy + 34], 9, fill=hx(c), outline=(210, 210, 210), width=1)
            d.text((cx + 42, sy - 2), label, font=font(FR, 13), fill=GREY)
            d.text((cx + 42, sy + 15), c, font=font(FB, 14), fill=TEXT)
            cx += 162
    out = "/home/hatch/workspace/otona-level/design-work/palette-a-refined.png"
    img.save(out)
    print("saved", out)

main()
