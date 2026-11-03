from fontTools.ttLib import TTCollection
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.boundsPen import BoundsPen

col = TTCollection('/System/Library/Fonts/Supplemental/Songti.ttc')
font = col.fonts[6]
upem = font['head'].unitsPerEm
cmap = font.getBestCmap()
gs = font.getGlyphSet()
ch = '花'
gname = cmap[ord(ch)]
pen = SVGPathPen(gs)
gs[gname].draw(pen)
d = pen.getCommands()
bp = BoundsPen(gs)
gs[gname].draw(bp)
xMin,yMin,xMax,yMax = bp.bounds
size = 96
s = size/upem
# center in 256
tx = 128 - (xMin+xMax)/2*s
ty = 128 + (yMin+yMax)/2*s  # because y flips; baseline translation
print('bounds', bp.bounds, 'tx',tx,'ty',ty)
open('flower-paths.svgfrag','w').write(f'<path transform="translate({tx:.3f} {ty:.3f}) scale({s:.6f} {-s:.6f})" d="{d}"/>\n')
