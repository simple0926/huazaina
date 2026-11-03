from PIL import Image, ImageDraw
import os

PAPER = (241, 238, 224, 255)
INK = (26, 32, 24, 255)
CREAM = (241, 238, 224, 255)
RED = (47, 90, 67, 255)

def rounded(draw, box, r, fill):
    draw.rounded_rectangle(box, radius=r, fill=fill)

def composition(size, padding_ratio, bg=True):
    S = size
    img = Image.new('RGBA', (S, S), PAPER if bg else (0,0,0,0))
    d = ImageDraw.Draw(img)
    pad = int(S * padding_ratio)
    card = (pad, pad + int(S*0.012), S - pad, S - pad)
    w = card[2] - card[0]
    rounded(d, card, int(w * 0.20), INK)
    # ledger rules
    lw = max(2, int(w * 0.026))
    x1 = card[0] + int(w * 0.18)
    x2 = card[0] + int(w * 0.74)
    for i, fy in enumerate((0.30, 0.42, 0.54)):
        y = card[1] + int(w * fy)
        d.rounded_rectangle((x1, y, x1 + int(w * (0.42 if i < 2 else 0.28)), y + lw),
                            radius=lw//2, fill=CREAM)
    # seal
    seal = int(w * 0.155)
    sx = card[2] - int(w * 0.15) - seal // 2
    sy = card[3] - int(w * 0.15) - seal // 2
    d.rounded_rectangle((sx, sy, sx + seal, sy + seal), radius=int(seal*0.16), fill=RED)
    # cut a paper gap around seal for the "stamp over card" feel
    return img

def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, 'PNG')

root = os.path.join(os.path.dirname(__file__), '..', 'android', 'res')

# legacy launcher icon: paper background, card at 62%
for name, size in [('mdpi',48), ('hdpi',72), ('xhdpi',96), ('xxhdpi',144), ('xxxhdpi',192)]:
    img = composition(size, 0.19, bg=True)
    save(img, os.path.join(root, 'mipmap-' + name, 'ic_launcher.png'))
    save(img, os.path.join(root, 'mipmap-' + name, 'ic_launcher_round.png'))

# adaptive foreground: transparent, content in central 66%
fg = composition(432, 0.24, bg=False)
save(fg, os.path.join(root, 'mipmap-xxxhdpi', 'ic_launcher_fg.png'))
for name, size in [('mdpi',108), ('hdpi',162), ('xhdpi',216), ('xxhdpi',324)]:
    save(fg.resize((size, size), Image.LANCZOS), os.path.join(root, 'mipmap-' + name, 'ic_launcher_fg.png'))

# preview sheet
prev = Image.new('RGBA', (620, 300), (255,255,255,255))
prev.alpha_composite(composition(256, 0.19, True), (20, 22))
for i, s in enumerate((96, 72, 48)):
    prev.alpha_composite(composition(s, 0.19, True), (300 + i*105, 60))
prev.save(os.path.join(os.path.dirname(__file__), '..', 'shots', 'icon-preview.png'))
print('icons ok')
