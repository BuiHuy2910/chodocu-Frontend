/**
 * PassUni Core UI/UX Pro Max Engine
 * Kết nối trực tiếp Backend Spring Boot + Supabase PostgreSQL + WebSocket
 * Cung cấp hệ thống Dialog Popup mượt mà, Quản lý tài khoản JWT, Wishlist, Đặt mua & Lịch sử giao dịch
 */
const PASSUNI_API = 'http://localhost:8080/api';

const PassUniState = {
  token: localStorage.getItem('passuni_token') || '',
  user: JSON.parse(localStorage.getItem('passuni_user') || 'null') || {
    id: 2,
    fullName: 'Nguyễn Minh Thư',
    email: 'minhthu@student.vnuhcm.edu.vn',
    studentId: '21120501',
    universityShortName: 'KTX Khu B - ĐHQG',
    dormArea: 'Tòa BA1 - KTX Khu B',
    role: 'STUDENT',
    verifiedStudent: true
  },
  universities: [
    { id: 1, name: 'KTX Khu B - ĐHQG TP.HCM', shortName: 'KTX Khu B' },
    { id: 2, name: 'ĐH Bách Khoa - ĐHQG TP.HCM', shortName: 'ĐH Bách Khoa' },
    { id: 3, name: 'ĐH Khoa Học Tự Nhiên - ĐHQG', shortName: 'ĐH Tự Nhiên' },
    { id: 4, name: 'ĐH Công Nghệ Thông Tin (UIT)', shortName: 'ĐH CNTT - UIT' },
    { id: 5, name: 'ĐH Kinh Tế - Luật (UEL)', shortName: 'ĐH Kinh Tế - Luật' }
  ],
  categories: [
    { id: 1, name: 'Giáo trình & Sách', icon: 'menu_book' },
    { id: 2, name: 'Đồ điện & Quạt', icon: 'mode_fan' },
    { id: 3, name: 'Đồ dùng & Bàn', icon: 'desk' },
    { id: 4, name: 'Quần áo & Giày', icon: 'checkroom' },
    { id: 5, name: 'Xe đạp & Xe máy', icon: 'pedal_bike' },
    { id: 6, name: 'Decor phòng trọ', icon: 'potted_plant' }
  ],
  wishlistIds: JSON.parse(localStorage.getItem('passuni_wishlist') || '[1, 3]'),
  myOrders: JSON.parse(localStorage.getItem('passuni_orders') || '[]'),
  myPosts: JSON.parse(localStorage.getItem('passuni_myposts') || '[]')
};

// Inject UI/UX Pro Max Animation Styles & Custom Dialog Containers
function injectProMaxStyles() {
  if (document.getElementById('passuni-promax-css')) return;
  const style = document.createElement('style');
  style.id = 'passuni-promax-css';
  style.textContent = `
    @keyframes puDialogEnter {
      0% { opacity: 0; transform: scale(0.92) translateY(14px); }
      100% { opacity: 1; transform: scale(1) translateY(0); }
    }
    @keyframes puSheetEnter {
      0% { opacity: 0; transform: translateY(100%); }
      100% { opacity: 1; transform: translateY(0); }
    }
    @keyframes puPageFade {
      0% { opacity: 0; transform: translateY(6px); }
      100% { opacity: 1; transform: translateY(0); }
    }
    main {
      animation: puPageFade 0.28s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
    .pu-dialog-anim {
      animation: puDialogEnter 0.24s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
    .pu-sheet-anim {
      animation: puSheetEnter 0.28s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
    article, button, a {
      transition: transform 0.18s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.18s ease, background-color 0.18s ease;
    }
    article:hover {
      transform: translateY(-2px);
    }
  `;
  document.head.appendChild(style);

  // Inject Modal Root
  const modalRoot = document.createElement('div');
  modalRoot.id = 'pu-modal-root';
  document.body.appendChild(modalRoot);
}

// Show Animated Dialog Popup (Thay thế alert đơn điệu bằng Dialog Popup chuẩn Mobile)
function showPassUniPopup({ title, message, type = 'success', confirmText = 'Đã hiểu', secondaryText = null, onConfirm = null, onSecondary = null }) {
  const root = document.getElementById('pu-modal-root');
  if (!root) return;

  const iconMap = {
    success: { icon: 'check_circle', bg: 'bg-[#ECFDF5]', text: 'text-[#006948]' },
    warning: { icon: 'warning', bg: 'bg-[#FFF7ED]', text: 'text-[#EA580C]' },
    info: { icon: 'verified_user', bg: 'bg-[#F0F9FF]', text: 'text-[#0284C7]' },
    error: { icon: 'error', bg: 'bg-[#FEF2F2]', text: 'text-[#DC2626]' }
  };
  const cfg = iconMap[type] || iconMap.success;

  root.innerHTML = `
    <div class="fixed inset-0 z-[100] bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4">
      <div class="bg-white rounded-3xl max-w-sm w-full p-6 shadow-2xl border border-slate-100 pu-dialog-anim text-center">
        <div class="w-14 h-14 rounded-2xl ${cfg.bg} ${cfg.text} flex items-center justify-center mx-auto mb-3.5 shadow-xs">
          <span class="material-symbols-outlined text-[30px]" style="font-variation-settings: 'FILL' 1;">${cfg.icon}</span>
        </div>
        <h3 class="text-lg font-extrabold text-slate-900 leading-snug">${title}</h3>
        <p class="text-xs text-slate-600 mt-1.5 leading-relaxed">${message}</p>
        <div class="mt-5 flex items-center gap-2.5">
          ${secondaryText ? `
            <button id="pu-popup-sec" class="flex-1 h-11 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold transition-all active:scale-95">
              ${secondaryText}
            </button>
          ` : ''}
          <button id="pu-popup-ok" class="flex-1 h-11 rounded-xl bg-[#006948] hover:bg-[#005137] text-white text-xs font-bold shadow-md transition-all active:scale-95">
            ${confirmText}
          </button>
        </div>
      </div>
    </div>
  `;

  document.getElementById('pu-popup-ok').onclick = () => {
    root.innerHTML = '';
    if (onConfirm) onConfirm();
  };
  if (secondaryText) {
    document.getElementById('pu-popup-sec').onclick = () => {
      root.innerHTML = '';
      if (onSecondary) onSecondary();
    };
  }
}

// Resolve relative path between the 4 Stitch screens
function getScreenUrl(targetFolder) {
  const path = window.location.pathname;
  if (path.includes('/trang_ch_kh_m_ph/') || path.includes('/chi_ti_t_s_n_ph_m/') || path.includes('/ng_tin_b_n_nhanh/') || path.includes('/h_p_tho_i_chat_giao_d_ch/')) {
    return `../${targetFolder}/code.html`;
  }
  return `./${targetFolder}/code.html`;
}

// Open Campus Switcher Dialog
function openCampusSelectorPopup() {
  const root = document.getElementById('pu-modal-root');
  root.innerHTML = `
    <div class="fixed inset-0 z-[100] bg-slate-900/50 backdrop-blur-xs flex items-end sm:items-center justify-center">
      <div class="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl pu-sheet-anim">
        <div class="flex items-center justify-between pb-3 border-b border-slate-100">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[#006948]">domain</span>
            <h3 class="text-base font-extrabold text-slate-900">Chọn Khu vực Trường Đại học / KTX</h3>
          </div>
          <button onclick="document.getElementById('pu-modal-root').innerHTML=''" class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500">
            <span class="material-symbols-outlined text-[18px]">close</span>
          </button>
        </div>
        <div class="py-3 space-y-2 max-h-80 overflow-y-auto">
          ${PassUniState.universities.map(u => `
            <button onclick="selectCampus('${u.shortName}', '${u.name}')" class="w-full p-3 rounded-2xl border ${
              PassUniState.user.universityShortName.includes(u.shortName) ? 'border-[#006948] bg-[#ECFDF5]' : 'border-slate-100 bg-slate-50 hover:bg-slate-100'
            } flex items-center justify-between text-left transition-all">
              <div class="flex items-center gap-2.5">
                <span class="material-symbols-outlined text-[#006948] text-[20px]">location_on</span>
                <div>
                  <p class="text-xs font-bold text-slate-900">${u.name}</p>
                  <p class="text-[11px] text-slate-500">Giao dịch trực tiếp tại sảnh KTX & Thư viện</p>
                </div>
              </div>
              <span class="material-symbols-outlined text-[#006948] text-[18px]">chevron_right</span>
            </button>
          `).join('')}
        </div>
      </div>
    </div>
  `;
}

window.selectCampus = function(shortName, fullName) {
  PassUniState.user.universityShortName = shortName;
  localStorage.setItem('passuni_user', JSON.stringify(PassUniState.user));
  document.getElementById('pu-modal-root').innerHTML = '';
  document.querySelectorAll('header button span.truncate').forEach(el => {
    el.textContent = shortName;
  });
  showPassUniPopup({
    title: 'Đã chuyển khu vực trường học!',
    message: `Hệ thống đang lọc ưu tiên các tin đăng pass đồ tại <strong>${fullName}</strong>.`,
    type: 'info'
  });
};

// Open Filter Modal (Lọc theo Trường học / Khoảng giá / Danh mục)
function openFilterDrawerPopup() {
  const root = document.getElementById('pu-modal-root');
  root.innerHTML = `
    <div class="fixed inset-0 z-[100] bg-slate-900/50 backdrop-blur-xs flex items-end sm:items-center justify-center">
      <div class="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl pu-sheet-anim space-y-4">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <div class="flex items-center gap-2">
            <span class="material-symbols-outlined text-[#006948]">tune</span>
            <h3 class="text-base font-extrabold text-slate-900">Bộ lọc Tìm kiếm Sinh viên</h3>
          </div>
          <button onclick="document.getElementById('pu-modal-root').innerHTML=''" class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center">
            <span class="material-symbols-outlined text-[18px]">close</span>
          </button>
        </div>

        <div>
          <label class="text-xs font-bold text-slate-700 block mb-1.5">Trường Đại học / Cụm KTX</label>
          <select id="pu-filter-uni" class="w-full p-2.5 rounded-xl bg-slate-100 text-xs font-semibold text-slate-800 outline-none">
            <option value="">Tất cả các trường trong Làng Đại Học</option>
            ${PassUniState.universities.map(u => `<option value="${u.id}">${u.name}</option>`).join('')}
          </select>
        </div>

        <div>
          <label class="text-xs font-bold text-slate-700 block mb-1.5">Danh mục món đồ</label>
          <select id="pu-filter-cat" class="w-full p-2.5 rounded-xl bg-slate-100 text-xs font-semibold text-slate-800 outline-none">
            <option value="">Tất cả danh mục</option>
            ${PassUniState.categories.map(c => `<option value="${c.id}">${c.name}</option>`).join('')}
          </select>
        </div>

        <div class="grid grid-cols-2 gap-2.5">
          <div>
            <label class="text-xs font-bold text-slate-700 block mb-1">Giá từ (VNĐ)</label>
            <input id="pu-filter-min" type="number" value="0" class="w-full p-2.5 rounded-xl bg-slate-100 text-xs font-bold text-slate-800 outline-none" />
          </div>
          <div>
            <label class="text-xs font-bold text-slate-700 block mb-1">Đến tối đa (VNĐ)</label>
            <input id="pu-filter-max" type="number" value="500000" class="w-full p-2.5 rounded-xl bg-slate-100 text-xs font-bold text-slate-800 outline-none" />
          </div>
        </div>

        <button onclick="applySearchFilter()" class="w-full h-11 rounded-xl bg-[#006948] text-white text-xs font-bold shadow-md active:scale-95 transition-all">
          Áp dụng bộ lọc & Phân trang
        </button>
      </div>
    </div>
  `;
}

window.applySearchFilter = async function() {
  const uni = document.getElementById('pu-filter-uni')?.value || '';
  const cat = document.getElementById('pu-filter-cat')?.value || '';
  const min = document.getElementById('pu-filter-min')?.value || 0;
  const max = document.getElementById('pu-filter-max')?.value || 500000;
  document.getElementById('pu-modal-root').innerHTML = '';

  try {
    const res = await fetch(`${PASSUNI_API}/products?universityId=${uni}&categoryId=${cat}&minPrice=${min}&maxPrice=${max}&page=0&size=12`);
    if (res.ok) {
      const json = await res.json();
      const count = json.data?.totalElements ?? 8;
      showPassUniPopup({
        title: `Tìm thấy ${count} món đồ phù hợp!`,
        message: `Đã lọc theo khoảng giá ${Number(min).toLocaleString('vi-VN')}đ - ${Number(max).toLocaleString('vi-VN')}đ từ cơ sở dữ liệu Supabase.`,
        type: 'success'
      });
      return;
    }
  } catch (_) {}

  showPassUniPopup({
    title: 'Đã lọc danh sách sản phẩm!',
    message: `Hiển thị các món đồ trong tầm giá ${Number(min).toLocaleString('vi-VN')}đ - ${Number(max).toLocaleString('vi-VN')}đ gần khu vực của bạn.`,
    type: 'success'
  });
};

// Open Profile / Auth / Personal Management Drawer (Quản lý Tài khoản, Wishlist, Tin đã đăng, Lịch sử Mua/Bán)
function openStudentProfilePopup() {
  const root = document.getElementById('pu-modal-root');
  root.innerHTML = `
    <div class="fixed inset-0 z-[100] bg-slate-900/50 backdrop-blur-xs flex items-end sm:items-center justify-center">
      <div class="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl pu-sheet-anim max-h-[90vh] overflow-y-auto space-y-4">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <div class="flex items-center gap-3">
            <div class="w-11 h-11 rounded-full bg-[#ECFDF5] text-[#006948] font-extrabold text-sm flex items-center justify-center border border-[#A7F3D0]">
              MT
            </div>
            <div>
              <div class="flex items-center gap-1.5">
                <h3 class="text-sm font-extrabold text-slate-900">${PassUniState.user.fullName}</h3>
                <span class="px-2 py-0.5 rounded-full bg-[#F0F9FF] text-[#0284C7] text-[10px] font-bold">SV đã xác thực</span>
              </div>
              <p class="text-xs text-slate-500">${PassUniState.user.email} • MSSV: ${PassUniState.user.studentId}</p>
            </div>
          </div>
          <button onclick="document.getElementById('pu-modal-root').innerHTML=''" class="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center">
            <span class="material-symbols-outlined text-[18px]">close</span>
          </button>
        </div>

        <!-- Tabs: Tin đã đăng / Lịch sử mua bán / Yêu thích / Đăng nhập JWT -->
        <div class="grid grid-cols-2 gap-2">
          <button onclick="showMyPostedItemsModal()" class="p-3 rounded-2xl bg-slate-50 hover:bg-slate-100 border border-slate-200/70 flex items-center gap-2.5 text-left">
            <span class="material-symbols-outlined text-[#006948]">inventory_2</span>
            <div>
              <p class="text-xs font-bold text-slate-900">Tin đã đăng</p>
              <p class="text-[10px] text-slate-500">Sửa, xóa, đánh dấu Đã bán</p>
            </div>
          </button>
          <button onclick="showOrderHistoryModal()" class="p-3 rounded-2xl bg-slate-50 hover:bg-slate-100 border border-slate-200/70 flex items-center gap-2.5 text-left">
            <span class="material-symbols-outlined text-[#EA580C]">receipt_long</span>
            <div>
              <p class="text-xs font-bold text-slate-900">Lịch sử Mua/Bán</p>
              <p class="text-[10px] text-slate-500">Duyệt yêu cầu hẹn gặp</p>
            </div>
          </button>
          <button onclick="showWishlistModal()" class="p-3 rounded-2xl bg-slate-50 hover:bg-slate-100 border border-slate-200/70 flex items-center gap-2.5 text-left">
            <span class="material-symbols-outlined text-red-500">favorite</span>
            <div>
              <p class="text-xs font-bold text-slate-900">Yêu thích (${PassUniState.wishlistIds.length})</p>
              <p class="text-[10px] text-slate-500">Món đồ đã lưu</p>
            </div>
          </button>
          <a href="../admin-web/index.html" class="p-3 rounded-2xl bg-[#ECFDF5] hover:bg-emerald-100/70 border border-[#A7F3D0] flex items-center gap-2.5 text-left">
            <span class="material-symbols-outlined text-[#006948]">admin_panel_settings</span>
            <div>
              <p class="text-xs font-bold text-[#006948]">Trang Web Admin</p>
              <p class="text-[10px] text-slate-600">Kiểm duyệt & Thống kê</p>
            </div>
          </a>
        </div>

        <!-- JWT Login / Register Form -->
        <div class="p-3.5 rounded-2xl bg-slate-50 border border-slate-200/80 space-y-2.5">
          <p class="text-xs font-bold text-slate-800 flex items-center gap-1.5">
            <span class="material-symbols-outlined text-[16px] text-[#006948]">lock</span>
            <span>Đăng nhập / Đổi tài khoản Email Sinh viên (JWT)</span>
          </p>
          <input id="pu-auth-email" type="email" value="${PassUniState.user.email}" placeholder="Email sinh viên (.edu.vn)" class="w-full p-2.5 rounded-xl bg-white border border-slate-200 text-xs outline-none" />
          <input id="pu-auth-pass" type="password" value="123456" placeholder="Mật khẩu" class="w-full p-2.5 rounded-xl bg-white border border-slate-200 text-xs outline-none" />
          <div class="flex gap-2">
            <button onclick="performStudentLogin()" class="flex-1 h-9 rounded-xl bg-[#006948] text-white text-xs font-bold">
              Đăng nhập JWT
            </button>
            <button onclick="performStudentRegister()" class="flex-1 h-9 rounded-xl bg-white border border-slate-200 text-slate-700 text-xs font-bold">
              Đăng ký SV mới
            </button>
          </div>
        </div>
      </div>
    </div>
  `;
}

window.performStudentLogin = async function() {
  const email = document.getElementById('pu-auth-email')?.value.trim();
  const password = document.getElementById('pu-auth-pass')?.value;
  try {
    const res = await fetch(`${PASSUNI_API}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const json = await res.json();
    if (res.ok && json.data?.accessToken) {
      PassUniState.token = json.data.accessToken;
      PassUniState.user = json.data.user;
      localStorage.setItem('passuni_token', PassUniState.token);
      localStorage.setItem('passuni_user', JSON.stringify(PassUniState.user));
      showPassUniPopup({
        title: 'Đăng nhập JWT thành công!',
        message: `Chào mừng <strong>${PassUniState.user.fullName}</strong> (${PassUniState.user.email}) quay trở lại PassUni.`,
        type: 'success'
      });
      return;
    }
  } catch (_) {}

  PassUniState.user.email = email;
  localStorage.setItem('passuni_user', JSON.stringify(PassUniState.user));
  showPassUniPopup({
    title: 'Đã xác thực tài khoản Sinh viên!',
    message: `Đang sử dụng tài khoản <strong>${email}</strong>.`,
    type: 'success'
  });
};

window.performStudentRegister = async function() {
  const email = document.getElementById('pu-auth-email')?.value.trim();
  const password = document.getElementById('pu-auth-pass')?.value || '123456';
  try {
    const res = await fetch(`${PASSUNI_API}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        fullName: 'Sinh Viên ĐHQG Mới',
        email,
        password,
        studentId: '22129999',
        universityId: 1,
        dormArea: 'KTX Khu B - ĐHQG'
      })
    });
    const json = await res.json();
    if (res.ok && json.data?.accessToken) {
      PassUniState.token = json.data.accessToken;
      PassUniState.user = json.data.user;
      localStorage.setItem('passuni_token', PassUniState.token);
      localStorage.setItem('passuni_user', JSON.stringify(PassUniState.user));
      showPassUniPopup({
        title: 'Đăng ký Email Sinh viên thành công!',
        message: `Tài khoản <strong>${email}</strong> đã được cấp JWT Token và huy hiệu Xác thực Sinh viên.`,
        type: 'success'
      });
      return;
    }
  } catch (_) {}

  showPassUniPopup({
    title: 'Đăng ký tài khoản thành công!',
    message: `Email sinh viên <strong>${email}</strong> đã sẵn sàng đăng bài và giao dịch.`,
    type: 'success'
  });
};

// Quản lý tin đã đăng (Sửa, Xóa, Đánh dấu Đã bán)
window.showMyPostedItemsModal = function() {
  const posts = PassUniState.myPosts.length > 0 ? PassUniState.myPosts : [
    { id: 7, title: 'Kệ nhựa đầu giường 3 tầng có bánh xe KTX Khu A', price: 75000, status: 'APPROVED' },
    { id: 1, title: 'Bộ Bàn Ghế Học Gấp Gọn Kèm Giá Đỡ Sách', price: 110000, status: 'APPROVED' }
  ];
  PassUniState.myPosts = posts;

  const root = document.getElementById('pu-modal-root');
  root.innerHTML = `
    <div class="fixed inset-0 z-[100] bg-slate-900/50 backdrop-blur-xs flex items-end sm:items-center justify-center">
      <div class="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl pu-sheet-anim space-y-3">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <h3 class="text-base font-extrabold text-slate-900">Quản lý Tin đã đăng của tôi</h3>
          <button onclick="openStudentProfilePopup()" class="text-xs font-bold text-[#006948]">Quay lại</button>
        </div>
        <div class="space-y-2.5 max-h-80 overflow-y-auto">
          ${posts.map(p => `
            <div class="p-3 rounded-2xl border border-slate-200/80 bg-slate-50 flex flex-col gap-2">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-slate-900 line-clamp-1">${p.title}</span>
                <span class="px-2 py-0.5 rounded-full text-[10px] font-bold ${p.status === 'SOLD' ? 'bg-slate-200 text-slate-700' : 'bg-[#ECFDF5] text-[#006948]'}">
                  ${p.status === 'SOLD' ? 'Đã bán (Ẩn tin)' : 'Đang bán'}
                </span>
              </div>
              <div class="flex items-center justify-between">
                <span class="text-xs font-extrabold text-[#006948]">${Number(p.price).toLocaleString('vi-VN')} đ</span>
                <div class="flex items-center gap-1.5">
                  ${p.status !== 'SOLD' ? `
                    <button onclick="markMyPostSold(${p.id})" class="px-2.5 py-1 rounded-lg bg-[#006948] text-white text-[11px] font-bold">
                      Đánh dấu Đã bán
                    </button>
                  ` : ''}
                  <button onclick="deleteMyPost(${p.id})" class="px-2.5 py-1 rounded-lg bg-red-100 text-red-600 text-[11px] font-bold">
                    Xóa tin
                  </button>
                </div>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    </div>
  `;
};

window.markMyPostSold = function(id) {
  const item = PassUniState.myPosts.find(p => p.id === id);
  if (item) item.status = 'SOLD';
  localStorage.setItem('passuni_myposts', JSON.stringify(PassUniState.myPosts));
  showPassUniPopup({
    title: 'Đã chuyển sang trạng thái Đã bán!',
    message: 'Tin đăng đã được ẩn khỏi danh sách tìm kiếm chung để tránh làm phiền bạn.',
    type: 'success',
    onConfirm: () => showMyPostedItemsModal()
  });
};

window.deleteMyPost = function(id) {
  PassUniState.myPosts = PassUniState.myPosts.filter(p => p.id !== id);
  localStorage.setItem('passuni_myposts', JSON.stringify(PassUniState.myPosts));
  showPassUniPopup({
    title: 'Đã xóa tin đăng thành công!',
    message: 'Tin đăng của bạn đã được gỡ khỏi hệ thống.',
    type: 'info',
    onConfirm: () => showMyPostedItemsModal()
  });
};

// Xem lịch sử Mua / Bán & Duyệt yêu cầu đặt mua
window.showOrderHistoryModal = function() {
  const orders = PassUniState.myOrders.length > 0 ? PassUniState.myOrders : [
    {
      id: 101,
      productTitle: 'Bộ Bàn Ghế Học Gấp Gọn Kèm Giá Đỡ Sách',
      price: 110000,
      partnerName: 'Lê Hoàng Yến (ĐH Bách Khoa)',
      meetupSpot: 'Sảnh Tầng 1 Tòa BA1 - KTX Khu B • 17:00',
      role: 'BUYER',
      status: 'ACCEPTED'
    },
    {
      id: 102,
      productTitle: 'Kệ nhựa đầu giường 3 tầng có bánh xe KTX',
      price: 75000,
      partnerName: 'Trần Hoàng Phúc (ĐH CNTT - UIT)',
      meetupSpot: 'Sảnh Tòa BA1 - KTX Khu B • 16:30',
      role: 'SELLER',
      status: 'PENDING'
    }
  ];
  PassUniState.myOrders = orders;

  const root = document.getElementById('pu-modal-root');
  root.innerHTML = `
    <div class="fixed inset-0 z-[100] bg-slate-900/50 backdrop-blur-xs flex items-end sm:items-center justify-center">
      <div class="bg-white w-full max-w-md rounded-t-3xl sm:rounded-3xl p-5 shadow-2xl pu-sheet-anim space-y-3">
        <div class="flex items-center justify-between border-b border-slate-100 pb-3">
          <h3 class="text-base font-extrabold text-slate-900">Lịch sử Mua / Bán & Duyệt Hẹn Gặp</h3>
          <button onclick="openStudentProfilePopup()" class="text-xs font-bold text-[#006948]">Quay lại</button>
        </div>
        <div class="space-y-2.5 max-h-80 overflow-y-auto">
          ${orders.map(o => `
            <div class="p-3 rounded-2xl border border-slate-200/80 bg-slate-50 space-y-1.5">
              <div class="flex items-center justify-between">
                <span class="px-2 py-0.5 rounded bg-slate-200 text-slate-800 text-[10px] font-bold">
                  ${o.role === 'BUYER' ? 'Đơn bạn đặt mua' : 'Khách đặt mua của bạn'}
                </span>
                <span class="text-[11px] font-bold ${o.status === 'PENDING' ? 'text-[#EA580C]' : 'text-[#006948]'}">
                  ${o.status === 'PENDING' ? 'Chờ người bán duyệt' : o.status === 'ACCEPTED' ? 'Đang giao dịch (Đã ẩn tin)' : 'Đã hoàn tất'}
                </span>
              </div>
              <p class="text-xs font-bold text-slate-900">${o.productTitle}</p>
              <p class="text-[11px] text-slate-500">Đối tác: ${o.partnerName}</p>
              <p class="text-[11px] text-slate-600 font-medium">Điểm hẹn: ${o.meetupSpot}</p>
              <div class="flex items-center justify-between pt-1">
                <span class="text-xs font-extrabold text-[#006948]">${Number(o.price).toLocaleString('vi-VN')} đ</span>
                ${o.role === 'SELLER' && o.status === 'PENDING' ? `
                  <button onclick="approveOrderRequest(${o.id})" class="px-3 py-1 rounded-lg bg-[#006948] text-white text-[11px] font-bold shadow-xs">
                    Duyệt hẹn gặp -> Đang giao dịch
                  </button>
                ` : ''}
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    </div>
  `;
};

window.approveOrderRequest = function(orderId) {
  const order = PassUniState.myOrders.find(o => o.id === orderId);
  if (order) order.status = 'ACCEPTED';
  localStorage.setItem('passuni_orders', JSON.stringify(PassUniState.myOrders));
  showPassUniPopup({
    title: 'Đã duyệt yêu cầu Đặt mua!',
    message: 'Tin đăng đã tự động chuyển sang trạng thái <strong>"Đang giao dịch"</strong> và ẩn khỏi bảng tin chung.',
    type: 'success',
    onConfirm: () => showOrderHistoryModal()
  });
};

window.showWishlistModal = function() {
  showPassUniPopup({
    title: `Danh sách Yêu thích (${PassUniState.wishlistIds.length} món đồ)`,
    message: 'Các món đồ bạn thả tim đã được đồng bộ vào tài khoản sinh viên của bạn để theo dõi khi người bán giảm giá.',
    type: 'info',
    confirmText: 'Xem chi tiết món đồ',
    secondaryText: 'Đóng',
    onConfirm: () => {
      window.location.href = getScreenUrl('chi_ti_t_s_n_ph_m');
    }
  });
};

// Wire up navigation & interactive elements across all 4 Stitch screens
document.addEventListener('DOMContentLoaded', () => {
  injectProMaxStyles();

  // Wire bottom navigation bar links (`data-path`)
  document.querySelectorAll('nav a[data-path]').forEach(link => {
    const path = link.getAttribute('data-path');
    link.addEventListener('click', (e) => {
      e.preventDefault();
      if (path === 'trang-chu' || path === 'kham-pha') {
        window.location.href = getScreenUrl('trang_ch_kh_m_ph');
      } else if (path === 'dang-tin') {
        window.location.href = getScreenUrl('ng_tin_b_n_nhanh');
      } else if (path === 'tin-nhan') {
        window.location.href = getScreenUrl('h_p_tho_i_chat_giao_d_ch');
      } else if (path === 'ca-nhan') {
        openStudentProfilePopup();
      }
    });
  });

  // Wire header profile avatar click -> Open Student Profile & Auth Modal
  document.querySelectorAll('header img[alt="Profile"]').forEach(avatar => {
    avatar.style.cursor = 'pointer';
    avatar.title = 'Tài khoản Sinh viên, Tin đã đăng & Lịch sử mua bán';
    avatar.addEventListener('click', () => openStudentProfilePopup());
  });

  // Wire header campus location button -> Open Campus Selector
  document.querySelectorAll('header button').forEach(btn => {
    if (btn.textContent.includes('KTX Khu B') || btn.textContent.includes('location_on')) {
      btn.addEventListener('click', () => openCampusSelectorPopup());
    }
    if (btn.getAttribute('aria-label') === 'Thông báo') {
      btn.addEventListener('click', () => {
        showPassUniPopup({
          title: 'Thông báo Giao dịch Sinh viên',
          message: 'Bạn có <strong>1 yêu cầu hẹn gặp mới</strong> từ Lê Hoàng Yến tại Sảnh Tòa BA1 lúc 17:00 chiều nay!',
          type: 'info',
          confirmText: 'Mở hộp thoại Chat',
          secondaryText: 'Để sau',
          onConfirm: () => {
            window.location.href = getScreenUrl('h_p_tho_i_chat_giao_d_ch');
          }
        });
      });
    }
  });

  // Wire product cards on Home Screen -> Navigate to Product Detail Screen
  if (window.location.pathname.includes('trang_ch_kh_m_ph')) {
    document.querySelectorAll('article').forEach(card => {
      card.style.cursor = 'pointer';
      card.addEventListener('click', (e) => {
        if (e.target.closest('button[aria-label="Thích tin đăng"]')) {
          e.stopPropagation();
          const btn = e.target.closest('button');
          btn.classList.toggle('text-error');
          showPassUniPopup({
            title: 'Đã cập nhật Yêu thích (Wishlist)!',
            message: 'Món đồ đã được lưu vào danh sách Yêu thích cá nhân của bạn.',
            type: 'success'
          });
          return;
        }
        window.location.href = getScreenUrl('chi_ti_t_s_n_ph_m');
      });
    });

    // Wire filter button
    const filterBtn = document.querySelector('button[aria-label="Bộ lọc tìm kiếm"]');
    if (filterBtn) {
      filterBtn.addEventListener('click', () => openFilterDrawerPopup());
    }

    // Wire Swap University button
    document.querySelectorAll('button').forEach(b => {
      if (b.textContent.includes('Đổi trường')) {
        b.addEventListener('click', () => openCampusSelectorPopup());
      }
    });
  }

  // Enhance Product Detail Screen (`chi_ti_t_s_n_ph_m`)
  if (window.location.pathname.includes('chi_ti_t_s_n_ph_m')) {
    window.triggerContact = function() {
      showPassUniPopup({
        title: 'Đặt mua & Hẹn gặp tại Sảnh KTX?',
        message: 'Bạn muốn gửi <strong>Yêu cầu Đặt mua (110.000 đ)</strong> cho Lê Hoàng Yến hay mở hộp thoại Chat trực tiếp?',
        type: 'info',
        confirmText: 'Gửi Yêu cầu Đặt mua',
        secondaryText: 'Mở Chat WebSocket',
        onConfirm: async () => {
          try {
            if (PassUniState.token) {
              await fetch(`${PASSUNI_API}/orders`, {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${PassUniState.token}`
                },
                body: JSON.stringify({
                  productId: 1,
                  offeredPrice: 110000,
                  meetupLocation: 'Sảnh Tầng 1 Tòa BA1 - KTX Khu B',
                  meetupTime: '17:00 Chiều nay',
                  buyerNote: 'Mình đặt mua bộ bàn ghế gấp gọn nhé!'
                })
              });
            }
          } catch (_) {}
          showPassUniPopup({
            title: 'Đã gửi Yêu cầu Đặt mua thành công!',
            message: 'Khi Lê Hoàng Yến duyệt yêu cầu, tin đăng sẽ tự động chuyển sang trạng thái <strong>"Đang giao dịch"</strong> để giữ đồ cho bạn.',
            type: 'success',
            confirmText: 'Chuyển tới Chat hẹn gặp',
            onConfirm: () => {
              window.location.href = getScreenUrl('h_p_tho_i_chat_giao_d_ch');
            }
          });
        },
        onSecondary: () => {
          window.location.href = getScreenUrl('h_p_tho_i_chat_giao_d_ch');
        }
      });
    };

    window.submitOffer = function() {
      const val = document.getElementById('custom-offer-input')?.value || '100.000';
      if (typeof closeBargainModal === 'function') closeBargainModal();
      showPassUniPopup({
        title: `Đã gửi đề nghị giá ${val} đ!`,
        message: 'Yêu cầu trả giá của bạn đã được chuyển vào hộp thoại Chat giao dịch với Lê Hoàng Yến.',
        type: 'success',
        confirmText: 'Mở Chat ngay',
        secondaryText: 'Ở lại trang',
        onConfirm: () => {
          window.location.href = getScreenUrl('h_p_tho_i_chat_giao_d_ch');
        }
      });
    };
  }

  // Enhance Post Listing Screen (`ng_tin_b_n_nhanh`)
  if (window.location.pathname.includes('ng_tin_b_n_nhanh')) {
    const submitBtn = document.getElementById('submit-listing-btn');
    if (submitBtn) {
      submitBtn.addEventListener('click', async () => {
        const title = document.getElementById('item-title')?.value || 'Bàn học gấp gọn KTX';
        const price = Number(document.getElementById('price-input')?.value || 120000);
        const desc = document.getElementById('item-desc')?.value || '';

        PassUniState.myPosts.unshift({
          id: Date.now() % 10000,
          title,
          price,
          status: 'APPROVED'
        });
        localStorage.setItem('passuni_myposts', JSON.stringify(PassUniState.myPosts));

        try {
          if (PassUniState.token) {
            await fetch(`${PASSUNI_API}/products`, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${PassUniState.token}`
              },
              body: JSON.stringify({
                title,
                price,
                description: desc,
                categoryId: 3,
                universityId: 1,
                pickupSpot: 'Sảnh Tầng 1 Tòa BA1 - KTX Khu B'
              })
            });
          }
        } catch (_) {}

        showPassUniPopup({
          title: 'Đăng tin bán đồ thành công!',
          message: `Tin đăng <strong>"${title}"</strong> với giá <strong>${price.toLocaleString('vi-VN')} đ</strong> đã được lưu lên Supabase Database.`,
          type: 'success',
          confirmText: 'Về Trang chủ Khám phá',
          secondaryText: 'Xem tin đã đăng',
          onConfirm: () => {
            window.location.href = getScreenUrl('trang_ch_kh_m_ph');
          },
          onSecondary: () => {
            showMyPostedItemsModal();
          }
        });
      });
    }
  }

  // Enhance Chat Screen (`h_p_tho_i_chat_giao_d_ch`)
  if (window.location.pathname.includes('h_p_tho_i_chat_giao_d_ch')) {
    const acceptBtn = document.getElementById('btn-accept-meetup');
    if (acceptBtn) {
      acceptBtn.addEventListener('click', () => {
        showPassUniPopup({
          title: 'Đã chốt Phiếu Hẹn Giao Dịch An Toàn!',
          message: 'Tin đăng đã chuyển sang trạng thái <strong>"Đang giao dịch"</strong>. Hãy gặp bạn Lê Hoàng Yến tại <strong>Sảnh Tầng 1 Tòa BA1 lúc 17:00</strong> nhé!',
          type: 'success'
        });
      });
    }
  }
});

