# Thương hiệu Chợ Đồ Cũ

**Chợ Đồ Cũ — "Cũ người mới ta"**: chợ đồ cũ cho sinh viên trao đổi trong trường.

![Logo](logo-full.svg)

## Logo

Túi mua sắm màu cam, giữa túi là biểu tượng tái chế (3 mũi tên đuổi nhau) và kính lúp,
đặt trên nền tròn xanh lá. Ý nghĩa: mua bán đồ cũ (túi), tái sử dụng (mũi tên),
tìm được món hợp ý (kính lúp).

| File | Dùng ở đâu |
|---|---|
| `logo-mark.svg` | Biểu tượng tròn — dùng cho mọi nơi cần logo vuông |
| `logo-full.svg` | Biểu tượng + chữ "CHỢ ĐỒ CŨ" + khẩu hiệu (slide, tài liệu, README) |
| `favicon.svg` | Biểu tượng tab trình duyệt của trang admin |
| `logo-mark-512.png`, `logo-mark-192.png` | Bản PNG nền trong suốt (ảnh đại diện, store, apple-touch-icon) |

Trong app Android, logo là vector (không phải ảnh PNG):

| File trong `mobile-app/app/src/main/res/drawable/` | Vai trò |
|---|---|
| `ic_logo_mark.xml` | Logo trong app (`AppLogoMark`, `AppWordmark`, `AppBrandHeader` trong `ui/components/AppLogo.kt`) |
| `ic_launcher_foreground.xml` | Lớp trước của icon launcher (nằm trong vùng an toàn 66dp) |
| `ic_launcher_background.xml` | Nền xanh của icon launcher |
| `ic_launcher_monochrome.xml` | Icon một màu cho "themed icons" của Android 13+ |

Trang admin dùng bản sao của các file này trong `chodocu-admin/assets/brand/`.

## Màu sắc

| Tên | Mã | Dùng cho |
|---|---|---|
| Xanh thương hiệu | `#0D6531` | Nền logo, nút chính, tiêu đề |
| Cam thương hiệu | `#FE6B1D` | Thân túi, điểm nhấn, giá tiền nổi bật |
| Xanh đậm | `#0C3622` | Viền, quai túi |
| Xanh lá | `#7CB83F` | Mũi tên tái chế |

## Sửa logo

Mọi file logo được **sinh tự động** từ `make_logo.py` (một bộ hình học duy nhất cho
cả Android và SVG) — không sửa tay các file sinh ra.

```powershell
# Chạy ở thư mục gốc repo chodocu-frontend
python docs/brand/make_logo.py mobile-app/app/src/main/res/drawable docs/brand
```

Sau khi chạy lại:

1. Xuất lại PNG (Chrome headless, nền trong suốt), ví dụ 512px:
   tạo trang HTML chỉ chứa `<img src="logo-mark.svg" width="512" height="512">` rồi chạy
   `chrome --headless=new --default-background-color=00000000 --window-size=512,512 --screenshot=logo-mark-512.png trang.html`.
2. Chép `logo-mark.svg`, `logo-full.svg`, `favicon.svg` và 2 file PNG sang `chodocu-admin/assets/brand/`.

## Quy tắc

- Không dùng ảnh AI, ảnh stock hay ảnh mẫu tạo tự động trong app và trang admin.
  Thiếu ảnh sản phẩm → biểu tượng trung tính; thiếu avatar → chữ cái đầu của tên.
- Giữ khoảng trống quanh logo tối thiểu bằng 1/4 đường kính logo.
- Không kéo méo, không đổi màu từng phần của logo.
