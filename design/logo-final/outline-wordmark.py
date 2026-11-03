from fontTools.ttLib import TTCollection
from fontTools.pens.svgPathPen import SVGPathPen

TTC = '/System/Library/Fonts/Supplemental/Songti.ttc'
FONT_INDEX = 6  # Songti SC Regular
TEXT = '花在哪'
SIZE = 78
TRACK = 2

col = TTCollection(TTC)
font = col.fonts[FONT_INDEX]
upem = font['head'].unitsPerEm
cmap = font.getBestCmap()
gs = font.getGlyphSet()
scale = SIZE / upem

parts = []
cursor = 0.0
for ch in TEXT:
    gname = cmap[ord(ch)]
    pen = SVGPathPen(gs)
    gs[gname].draw(pen)
    d = pen.getCommands()
    parts.append((cursor, d))
    cursor += gs[gname].width * scale + TRACK

width = cursor - TRACK
print('upem', upem, 'width', width, 'glyphs', len(parts))
with open('wordmark-paths.svgfrag', 'w') as f:
    for x, d in parts:
        f.write(f'<path transform="translate({x:.3f} 0) scale({scale:.6f} {-scale:.6f})" d="{d}"/>\n')
with open('wordmark.svg', 'w') as f:
    f.write(f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width+2:.2f} 110">\n')
    f.write('<g fill="#1A2018">\n')
    for x, d in parts:
        f.write(f'  <path transform="translate({x+1:.3f} 90) scale({scale:.6f} {-scale:.6f})" d="{d}"/>\n')
    f.write('</g>\n</svg>\n')
print('done')
