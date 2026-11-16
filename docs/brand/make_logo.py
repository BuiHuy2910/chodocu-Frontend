"""
Sinh logo Chợ Đồ Cũ (túi mua sắm cam + mũi tên tái chế xanh + kính lúp) theo ảnh mẫu.
Cùng một bộ hình học xuất ra:
  - Android VectorDrawable: ic_logo_mark.xml, ic_launcher_foreground.xml, ic_launcher_monochrome.xml
  - SVG: logo-mark.svg, favicon.svg, logo-full.svg

Chạy từ thư mục gốc repo chodocu-frontend:
    python docs/brand/make_logo.py mobile-app/app/src/main/res/drawable docs/brand
"""
import math
import os
import sys

GREEN_BG = '#0D6531'     # nền tròn (trùng brand_green)
ORANGE = '#FE6B1D'       # thân túi (trùng brand_orange)
ORANGE_SHADE = '#E2530F' # mặt bên túi
ORANGE_RIM = '#FF8A45'   # dải sáng ở miệng túi
DARK = '#0C3622'         # viền, quai túi
LEAF = '#7CB83F'         # mũi tên tái chế
WHITE = '#FFFFFF'

CENTER = (54.0, 54.0)


def fmt(v):
    text = f'{v:.2f}'.rstrip('0').rstrip('.')
    return '0' if text == '-0' else text


def p(point):
    return f'{fmt(point[0])},{fmt(point[1])}'


def lerp(a, b, t):
    return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)


def unit(a, b):
    dx, dy = b[0] - a[0], b[1] - a[1]
    length = math.hypot(dx, dy)
    return (dx / length, dy / length)


def add(a, v, k=1.0):
    return (a[0] + v[0] * k, a[1] + v[1] * k)


# ---------------------------------------------------------------------------
# Hình học
# ---------------------------------------------------------------------------

class Shape:
    def __init__(self, d, fill=None, stroke=None, width=None, cap=None, join=None):
        self.d, self.fill, self.stroke, self.width, self.cap, self.join = d, fill, stroke, width, cap, join


def rounded_polygon(points, radii):
    """Đa giác bo góc bằng đường cong bậc hai (bán kính xấp xỉ tại mỗi đỉnh)."""
    n = len(points)
    parts = []
    for i in range(n):
        prev_pt, cur, next_pt = points[i - 1], points[i], points[(i + 1) % n]
        r = radii[i]
        start = add(cur, unit(cur, prev_pt), r)
        end = add(cur, unit(cur, next_pt), r)
        parts.append((start, cur, end))
    d = f'M{p(parts[0][2])}'
    for start, cur, end in parts[1:] + parts[:1]:
        d += f'L{p(start)}Q{p(cur)} {p(end)}'
    return d + 'Z', parts


# Thân túi: hơi loe ở đáy giống ảnh mẫu
BAG = [(32, 42), (76, 42), (79, 84), (29, 84)]
BAG_RADII = [2.5, 2.5, 5, 5]
BAG_PATH, BAG_CORNERS = rounded_polygon(BAG, BAG_RADII)


def bag_side_path():
    """Mặt bên phải của túi (tối hơn) đi theo đúng góc bo của thân túi."""
    _, tr, tr_end = BAG_CORNERS[1]
    tr_start = BAG_CORNERS[1][0]
    br_start, br, br_end = BAG_CORNERS[2]
    return (f'M{p((69, 42))}L{p(tr_start)}Q{p(tr)} {p(tr_end)}'
            f'L{p(br_start)}Q{p(br)} {p(br_end)}L{p((71.5, 84))}Z')


# Dải sáng ở miệng túi (mép ngoài nằm dưới đường viền)
BAG_RIM = 'M32,42H76L76.25,45.5H31.75Z'

HANDLES = [
    'M40,47V35A10,10 0 0 1 60,35V47',
    'M48,47V35A10,10 0 0 1 68,35V47',
]


def sparkle(cx, cy, a):
    c = (cx, cy)
    return (f'M{p((cx, cy - a))}Q{p(c)} {p((cx + a, cy))}Q{p(c)} {p((cx, cy + a))}'
            f'Q{p(c)} {p((cx - a, cy))}Q{p(c)} {p((cx, cy - a))}Z')


SPARKLES = [sparkle(79, 28, 5), sparkle(86.5, 40, 2.6), sparkle(25, 37, 3.4)]
SQUIGGLES = [
    'M22,50c-3.2,3 3.2,6 0,9c-3.2,3 3.2,6 0,9',
    'M87,53c-3.2,3 3.2,6 0,9c-3.2,3 3.2,6 0,9',
]

# Biểu tượng tái chế: 3 mũi tên gấp ở 3 góc tam giác đều, đuổi nhau theo chiều kim đồng hồ
RECYCLE_CENTER = (54.0, 65.0)
RECYCLE_R = 16.5           # bán kính đường tròn ngoại tiếp tam giác
BODY_W = 4.2               # bề rộng thân mũi tên
OUTLINE = 1.1              # viền tối quanh mũi tên
HEAD_HALF = 4.6            # nửa bề rộng đầu mũi tên


def recycle_vertices():
    cx, cy = RECYCLE_CENTER
    return [(cx + RECYCLE_R * math.cos(math.radians(a)), cy + RECYCLE_R * math.sin(math.radians(a)))
            for a in (-90, 30, 150)]


def recycle_arrows():
    """Trả về [(đường thân, đường thân để vẽ viền, tam giác đầu mũi tên)] cho 3 mũi tên."""
    v = recycle_vertices()
    arrows = []
    for k in range(3):
        prev_pt, cur, next_pt = v[k - 1], v[k], v[(k + 1) % 3]
        tail = lerp(prev_pt, cur, 0.58)
        bend_in = lerp(prev_pt, cur, 0.80)
        bend_out = lerp(cur, next_pt, 0.20)
        body_end = lerp(cur, next_pt, 0.24)
        head_base = lerp(cur, next_pt, 0.21)
        tip = lerp(cur, next_pt, 0.46)

        u_in = unit(prev_pt, cur)
        tail_outline = add(tail, u_in, -OUTLINE)  # viền phủ cả đầu đuôi (đuôi cắt bằng)
        body = f'M{p(tail)}L{p(bend_in)}Q{p(cur)} {p(bend_out)}L{p(body_end)}'
        body_outline = f'M{p(tail_outline)}L{p(bend_in)}Q{p(cur)} {p(bend_out)}L{p(body_end)}'

        u = unit(cur, next_pt)
        normal = (-u[1], u[0])
        head = (f'M{p(add(head_base, normal, HEAD_HALF))}L{p(tip)}'
                f'L{p(add(head_base, normal, -HEAD_HALF))}Z')
        arrows.append((body, body_outline, head))
    return arrows


def lens_path(r):
    cx, cy = RECYCLE_CENTER
    return f'M{p((cx - r, cy))}a{fmt(r)},{fmt(r)} 0 1 1 {fmt(2 * r)},0a{fmt(r)},{fmt(r)} 0 1 1 {fmt(-2 * r)},0Z'


CIRCLE_BG = 'M54,0a54,54 0 1 1 0,108a54,54 0 1 1 0,-108Z'


def emblem(color_mode='color', decorations=True):
    """Danh sách hình (chưa có nền) theo thứ tự vẽ."""
    shapes = []
    mono = color_mode == 'mono'
    ink = '#000000' if mono else DARK

    if decorations and not mono:
        for d in SQUIGGLES:
            shapes.append(Shape(d, stroke=ORANGE, width=2.2, cap='round', join='round'))
        for d in SPARKLES:
            shapes.append(Shape(d, fill=ORANGE))

    for d in HANDLES:
        shapes.append(Shape(d, stroke=ink, width=3.4, cap='round', join='round'))

    if mono:
        shapes.append(Shape(BAG_PATH, stroke=ink, width=3.2, join='round'))
    else:
        shapes.append(Shape(BAG_PATH, fill=ORANGE))
        shapes.append(Shape(BAG_RIM, fill=ORANGE_RIM))
        shapes.append(Shape(bag_side_path(), fill=ORANGE_SHADE))
        shapes.append(Shape(BAG_PATH, stroke=ink, width=3, join='round'))

    arrows = recycle_arrows()
    if mono:
        for body, _, head in arrows:
            shapes.append(Shape(body, stroke=ink, width=BODY_W, cap='butt', join='round'))
            shapes.append(Shape(head, fill=ink, stroke=ink, width=0.8, join='round'))
        shapes.append(Shape(lens_path(3.8), stroke=ink, width=1.4))
    else:
        for _, body_outline, head in arrows:
            shapes.append(Shape(body_outline, stroke=DARK, width=BODY_W + 2 * OUTLINE, cap='butt', join='round'))
            shapes.append(Shape(head, fill=DARK, stroke=DARK, width=2 * OUTLINE, join='round'))
        for body, _, head in arrows:
            shapes.append(Shape(body, stroke=LEAF, width=BODY_W, cap='butt', join='round'))
            shapes.append(Shape(head, fill=LEAF))
        shapes.append(Shape(lens_path(3.8), fill=WHITE, stroke=DARK, width=1.3))
    return shapes


# ---------------------------------------------------------------------------
# Xuất Android VectorDrawable
# ---------------------------------------------------------------------------

def android_path(s, indent):
    attrs = [f'android:pathData="{s.d}"']
    attrs.append(f'android:fillColor="{s.fill}"' if s.fill else 'android:fillColor="#00000000"')
    if s.stroke:
        attrs.append(f'android:strokeColor="{s.stroke}"')
        attrs.append(f'android:strokeWidth="{fmt(s.width)}"')
        if s.cap:
            attrs.append(f'android:strokeLineCap="{s.cap}"')
        if s.join:
            attrs.append(f'android:strokeLineJoin="{s.join}"')
    pad = ' ' * indent
    return f'{pad}<path\n' + '\n'.join(f'{pad}    {a}' for a in attrs) + ' />'


def android_vector(comment, shapes, background=None, scale=1.0):
    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        f'<!-- {comment} File sinh tự động từ docs/brand (make_logo.py), không sửa tay. -->',
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    android:width="108dp"',
        '    android:height="108dp"',
        '    android:viewportWidth="108"',
        '    android:viewportHeight="108">',
    ]
    if background:
        lines.append(android_path(Shape(CIRCLE_BG, fill=background), 4))
    indent = 4
    if scale != 1.0:
        lines.append(f'    <group android:pivotX="54" android:pivotY="54" '
                     f'android:scaleX="{fmt(scale)}" android:scaleY="{fmt(scale)}">')
        indent = 8
    lines.extend(android_path(s, indent) for s in shapes)
    if scale != 1.0:
        lines.append('    </group>')
    lines.append('</vector>')
    return '\n'.join(lines) + '\n'


# ---------------------------------------------------------------------------
# Xuất SVG
# ---------------------------------------------------------------------------

def svg_path(s):
    attrs = [f'd="{s.d}"', f'fill="{s.fill or "none"}"']
    if s.stroke:
        attrs += [f'stroke="{s.stroke}"', f'stroke-width="{fmt(s.width)}"']
        if s.cap:
            attrs.append(f'stroke-linecap="{s.cap}"')
        if s.join:
            attrs.append(f'stroke-linejoin="{s.join}"')
    return '<path ' + ' '.join(attrs) + '/>'


def svg_mark_body(shapes, background=True):
    out = []
    if background:
        out.append(svg_path(Shape(CIRCLE_BG, fill=GREEN_BG)))
    out.extend(svg_path(s) for s in shapes)
    return out


def svg_mark(title):
    body = '\n  '.join(svg_mark_body(emblem()))
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" role="img" aria-label="{title}">\n'
            f'  <title>{title}</title>\n  {body}\n</svg>\n')


def svg_full():
    """Logo đầy đủ: biểu tượng, tên app, khẩu hiệu (xếp dọc như ảnh mẫu)."""
    body = '\n    '.join(svg_mark_body(emblem()))
    font = "'Be Vietnam Pro','Segoe UI',Roboto,Arial,sans-serif"
    return f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 320 300" role="img" aria-label="Chợ Đồ Cũ - Cũ người mới ta">
  <title>Chợ Đồ Cũ - Cũ người mới ta</title>
  <g transform="translate(90 8) scale(1.2963)">
    {body}
  </g>
  <text x="160" y="232" text-anchor="middle" font-family="{font}" font-size="40" font-weight="800" letter-spacing="1" fill="{GREEN_BG}">CHỢ ĐỒ CŨ</text>
  <text x="160" y="268" text-anchor="middle" font-family="{font}" font-size="20" font-weight="500" fill="{DARK}">Cũ người mới ta</text>
</svg>
'''


def main():
    drawable_dir, svg_dir = sys.argv[1], sys.argv[2]
    os.makedirs(svg_dir, exist_ok=True)

    files = {
        os.path.join(drawable_dir, 'ic_logo_mark.xml'): android_vector(
            'Logo Chợ Đồ Cũ: túi mua sắm + mũi tên tái chế + kính lúp trên nền tròn xanh.',
            emblem(), background=GREEN_BG),
        os.path.join(drawable_dir, 'ic_launcher_foreground.xml'): android_vector(
            'Lớp trước của icon launcher (nằm gọn trong vùng an toàn 66dp).',
            emblem(), scale=0.8),
        os.path.join(drawable_dir, 'ic_launcher_monochrome.xml'): android_vector(
            'Icon một màu cho giao diện "themed icons" của Android 13+.',
            emblem('mono'), scale=0.8),
        os.path.join(svg_dir, 'logo-mark.svg'): svg_mark('Logo Chợ Đồ Cũ'),
        os.path.join(svg_dir, 'favicon.svg'): svg_mark('Chợ Đồ Cũ'),
        os.path.join(svg_dir, 'logo-full.svg'): svg_full(),
    }
    for path, content in files.items():
        with open(path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(content)
        print('wrote', path)


if __name__ == '__main__':
    main()
