// AuthModal.jsx - React Component for Full Login and Registration in Ngu Hanh Son, Da Nang
import React, { useState } from 'react';

export const NGU_HANH_SON_WARDS = [
  { id: 'myan', name: 'Phường Mỹ An' },
  { id: 'khuemy', name: 'Phường Khuê Mỹ' },
  { id: 'hoaquy', name: 'Phường Hòa Quý' },
  { id: 'hoahai', name: 'Phường Hòa Hải' }
];

export default function AuthModal({ isOpen, onClose, initialRole = 'citizen', initialMode = 'login' }) {
  const [mode, setMode] = useState(initialMode); // 'login' | 'register'
  const [role, setRole] = useState(initialRole); // 'citizen' | 'admin'
  const [showPassword, setShowPassword] = useState(false);

  // Citizen Registration state
  const [citizenForm, setCitizenForm] = useState({
    fullName: '',
    phone: '',
    idCard: '',
    ward: 'khuemy',
    address: '',
    priorityGroup: 'normal',
    password: '',
    confirmPassword: '',
    agreed: false
  });

  // Admin Registration state
  const [adminForm, setAdminForm] = useState({
    fullName: '',
    email: '',
    department: 'UBND Quận Ngũ Hành Sơn',
    authCode: '',
    password: '',
    confirmPassword: ''
  });

  // Login state
  const [loginForm, setLoginForm] = useState({
    identifier: '',
    password: '',
    remember: false
  });

  if (!isOpen) return null;

  const handleLoginSubmit = (e) => {
    e.preventDefault();
    if (role === 'citizen') {
      window.location.href = 'citizen/dashboard.html';
    } else {
      window.location.href = 'admin/dashboard.html';
    }
  };

  const handleRegisterSubmit = (e) => {
    e.preventDefault();
    alert(`Đăng ký thành công tài khoản ${role === 'citizen' ? 'Người dân' : 'Cán bộ'} tại Quận Ngũ Hành Sơn! Vui lòng đăng nhập.`);
    setMode('login');
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100%',
      height: '100%',
      background: 'rgba(8, 20, 36, 0.75)',
      backdropFilter: 'blur(8px)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 9999,
      padding: '16px'
    }} onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}>
      <div style={{
        background: '#081424',
        border: '1.5px solid rgba(212, 175, 55, 0.45)',
        boxShadow: '0 24px 60px rgba(0, 0, 0, 0.65)',
        borderRadius: '16px',
        width: '100%',
        maxWidth: '520px',
        color: '#F9F3E8',
        overflow: 'hidden',
        animation: 'modalSlideIn 0.25s ease'
      }}>
        {/* Header */}
        <div style={{
          background: 'linear-gradient(135deg, rgba(8,20,36,0.98), rgba(18,42,70,0.95))',
          padding: '20px 24px',
          borderBottom: '1px solid rgba(212,175,55,0.3)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'flex-start'
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
              <img src="src/assets/logo-transparent.png" alt="CoordSystem" style={{ height: '32px' }} />
              <span style={{ fontSize: '0.78rem', color: '#D4AF37', fontWeight: '700', textTransform: 'uppercase' }}>
                Quận Ngũ Hành Sơn · TP. Đà Nẵng
              </span>
            </div>
            <h3 style={{ margin: '4px 0 2px 0', fontSize: '1.25rem', color: '#F3E5AB', fontWeight: '800' }}>
              {mode === 'login' ? '🔑 Đăng Nhập Hệ Thống' : '📝 Đăng Ký Tài Khoản Mới'}
            </h3>
            <p style={{ margin: 0, fontSize: '0.78rem', color: 'rgba(249,243,232,0.7)' }}>
              Cổng Điều phối An sinh Xã hội & Cứu trợ trực tuyến
            </p>
          </div>
          <button onClick={onClose} style={{
            background: 'rgba(255,255,255,0.08)',
            border: 'none',
            color: '#F9F3E8',
            width: '32px',
            height: '32px',
            borderRadius: '50%',
            cursor: 'pointer',
            fontSize: '1rem'
          }}>✕</button>
        </div>

        <div style={{ padding: '20px 24px', maxHeight: '80vh', overflowY: 'auto' }}>
          {/* Main Mode Switcher: Login vs Register */}
          <div style={{
            display: 'flex',
            background: 'rgba(255,255,255,0.06)',
            padding: '4px',
            borderRadius: '10px',
            border: '1px solid rgba(212,175,55,0.25)',
            marginBottom: '16px'
          }}>
            <button
              onClick={() => setMode('login')}
              style={{
                flex: 1,
                padding: '9px 12px',
                borderRadius: '8px',
                border: 'none',
                background: mode === 'login' ? 'linear-gradient(135deg, #D4AF37, #B58A28)' : 'transparent',
                color: mode === 'login' ? '#081424' : '#F9F3E8',
                fontWeight: '700',
                fontSize: '0.85rem',
                cursor: 'pointer'
              }}
            >
              🔑 Đăng Nhập
            </button>
            <button
              onClick={() => setMode('register')}
              style={{
                flex: 1,
                padding: '9px 12px',
                borderRadius: '8px',
                border: 'none',
                background: mode === 'register' ? 'linear-gradient(135deg, #D4AF37, #B58A28)' : 'transparent',
                color: mode === 'register' ? '#081424' : '#F9F3E8',
                fontWeight: '700',
                fontSize: '0.85rem',
                cursor: 'pointer'
              }}
            >
              📝 Đăng Ký Tài Khoản
            </button>
          </div>

          {/* Role Switcher: Citizen vs Admin */}
          <div style={{ display: 'flex', gap: '8px', marginBottom: '18px' }}>
            <button
              onClick={() => setRole('citizen')}
              style={{
                flex: 1,
                padding: '8px 12px',
                borderRadius: '8px',
                border: role === 'citizen' ? '1.5px solid #4A7A42' : '1px solid rgba(255,255,255,0.12)',
                background: role === 'citizen' ? 'rgba(74,122,66,0.3)' : 'rgba(255,255,255,0.03)',
                color: role === 'citizen' ? '#86efac' : 'rgba(249,243,232,0.7)',
                fontWeight: '700',
                fontSize: '0.82rem',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px'
              }}
            >
              <span>👤</span> Người Dân Cần Hỗ Trợ
            </button>
            <button
              onClick={() => setRole('admin')}
              style={{
                flex: 1,
                padding: '8px 12px',
                borderRadius: '8px',
                border: role === 'admin' ? '1.5px solid #3b82f6' : '1px solid rgba(255,255,255,0.12)',
                background: role === 'admin' ? 'rgba(59,130,246,0.25)' : 'rgba(255,255,255,0.03)',
                color: role === 'admin' ? '#93c5fd' : 'rgba(249,243,232,0.7)',
                fontWeight: '700',
                fontSize: '0.82rem',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '6px'
              }}
            >
              <span>🏛</span> Cán Bộ Quản Lý / Điều Phối
            </button>
          </div>

          {/* 1. LOGIN FORM */}
          {mode === 'login' && (
            <form onSubmit={handleLoginSubmit}>
              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '0.8rem', marginBottom: '6px', color: '#F3E5AB' }}>
                  {role === 'citizen' ? 'Số điện thoại hoặc CCCD/Định danh' : 'Email công vụ (@danang.gov.vn)'}
                </label>
                <input
                  type={role === 'citizen' ? 'text' : 'email'}
                  required
                  placeholder={role === 'citizen' ? 'Ví dụ: 0912345678 hoặc 048xxxxxxxxx' : 'canbo@danang.gov.vn'}
                  value={loginForm.identifier}
                  onChange={(e) => setLoginForm({ ...loginForm, identifier: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: '8px',
                    border: '1px solid rgba(212,175,55,0.3)',
                    background: 'rgba(255,255,255,0.06)',
                    color: '#fff',
                    fontSize: '0.88rem'
                  }}
                />
              </div>

              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '0.8rem', marginBottom: '6px', color: '#F3E5AB' }}>
                  Mật khẩu
                </label>
                <div style={{ position: 'relative' }}>
                  <input
                    type={showPassword ? 'text' : 'password'}
                    required
                    placeholder="Nhập mật khẩu..."
                    value={loginForm.password}
                    onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                    style={{
                      width: '100%',
                      padding: '10px 38px 10px 14px',
                      borderRadius: '8px',
                      border: '1px solid rgba(212,175,55,0.3)',
                      background: 'rgba(255,255,255,0.06)',
                      color: '#fff',
                      fontSize: '0.88rem'
                    }}
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    style={{
                      position: 'absolute',
                      right: '10px',
                      top: '50%',
                      transform: 'translateY(-50%)',
                      background: 'none',
                      border: 'none',
                      color: 'rgba(249,243,232,0.6)',
                      cursor: 'pointer'
                    }}
                  >
                    {showPassword ? '🙈' : '👁️'}
                  </button>
                </div>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px', fontSize: '0.8rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', color: 'rgba(249,243,232,0.8)' }}>
                  <input
                    type="checkbox"
                    checked={loginForm.remember}
                    onChange={(e) => setLoginForm({ ...loginForm, remember: e.target.checked })}
                  />
                  Ghi nhớ đăng nhập
                </label>
                <a href="#forgot" onClick={(e) => { e.preventDefault(); alert('Vui lòng liên hệ Trạm điều phối phường gần nhất để cấp lại mật khẩu.'); }} style={{ color: '#D4AF37', textDecoration: 'none' }}>
                  Quên mật khẩu?
                </a>
              </div>

              <button
                type="submit"
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '8px',
                  border: 'none',
                  background: role === 'citizen'
                    ? 'linear-gradient(135deg, #4A7A42, #2E6B28)'
                    : 'linear-gradient(135deg, #D4AF37, #B58A28)',
                  color: role === 'citizen' ? '#FFFFFF' : '#081424',
                  fontWeight: '800',
                  fontSize: '0.92rem',
                  cursor: 'pointer',
                  boxShadow: '0 4px 14px rgba(0,0,0,0.3)'
                }}
              >
                👉 {role === 'citizen' ? 'Đăng Nhập Cổng Người Dân' : 'Đăng Nhập Cổng Quản Lý Admin'}
              </button>
            </form>
          )}

          {/* 2. CITIZEN REGISTRATION FORM */}
          {mode === 'register' && role === 'citizen' && (
            <form onSubmit={handleRegisterSubmit}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Họ và tên *</label>
                  <input
                    type="text"
                    required
                    placeholder="Nguyễn Văn A"
                    value={citizenForm.fullName}
                    onChange={(e) => setCitizenForm({ ...citizenForm, fullName: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Số điện thoại *</label>
                  <input
                    type="tel"
                    required
                    placeholder="0912345678"
                    value={citizenForm.phone}
                    onChange={(e) => setCitizenForm({ ...citizenForm, phone: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '12px', marginBottom: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Số CCCD / Mã Định danh (12 số)</label>
                  <input
                    type="text"
                    required
                    placeholder="048xxxxxxxx"
                    value={citizenForm.idCard}
                    onChange={(e) => setCitizenForm({ ...citizenForm, idCard: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Phường cư trú (Q. NHS) *</label>
                  <select
                    value={citizenForm.ward}
                    onChange={(e) => setCitizenForm({ ...citizenForm, ward: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: '#081424', color: '#fff', fontSize: '0.84rem' }}
                  >
                    {NGU_HANH_SON_WARDS.map(w => (
                      <option key={w.id} value={w.id}>{w.name}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div style={{ marginBottom: '12px' }}>
                <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Địa chỉ chi tiết (Tổ dân phố, tên đường) *</label>
                <input
                  type="text"
                  required
                  placeholder="Ví dụ: Tổ 15, Đường Lưu Quang Vũ, P. Hòa Quý"
                  value={citizenForm.address}
                  onChange={(e) => setCitizenForm({ ...citizenForm, address: e.target.value })}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                />
              </div>

              <div style={{ marginBottom: '12px' }}>
                <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Đối tượng an sinh ưu tiên</label>
                <select
                  value={citizenForm.priorityGroup}
                  onChange={(e) => setCitizenForm({ ...citizenForm, priorityGroup: e.target.value })}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: '#081424', color: '#fff', fontSize: '0.84rem' }}
                >
                  <option value="flood">Hộ vùng trũng ngập lụt ven sông Cổ Cò / Đô Lương</option>
                  <option value="poor">Hộ nghèo / Hộ cận nghèo của Phường</option>
                  <option value="elderly">Người cao tuổi neo đơn / Người khuyết tật</option>
                  <option value="normal">Gia đình khó khăn đột xuất / Bão lũ</option>
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Mật khẩu *</label>
                  <input
                    type="password"
                    required
                    placeholder="Mật khẩu ít nhất 6 ký tự"
                    value={citizenForm.password}
                    onChange={(e) => setCitizenForm({ ...citizenForm, password: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Xác nhận mật khẩu *</label>
                  <input
                    type="password"
                    required
                    placeholder="Nhập lại mật khẩu"
                    value={citizenForm.confirmPassword}
                    onChange={(e) => setCitizenForm({ ...citizenForm, confirmPassword: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
              </div>

              <div style={{ marginBottom: '16px' }}>
                <label style={{ display: 'flex', alignItems: 'flex-start', gap: '8px', fontSize: '0.76rem', color: 'rgba(249,243,232,0.85)', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    required
                    checked={citizenForm.agreed}
                    onChange={(e) => setCitizenForm({ ...citizenForm, agreed: e.target.checked })}
                    style={{ marginTop: '2px' }}
                  />
                  <span>Tôi cam kết thông tin cư trú tại <strong>Quận Ngũ Hành Sơn</strong> là trung thực để phục vụ công tác điều phối cứu trợ chính xác.</span>
                </label>
              </div>

              <button
                type="submit"
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '8px',
                  border: 'none',
                  background: 'linear-gradient(135deg, #4A7A42, #2E6B28)',
                  color: '#FFFFFF',
                  fontWeight: '800',
                  fontSize: '0.92rem',
                  cursor: 'pointer'
                }}
              >
                ✅ Hoàn Tất Đăng Ký Tài Khoản Người Dân
              </button>
            </form>
          )}

          {/* 3. ADMIN REGISTRATION FORM */}
          {mode === 'register' && role === 'admin' && (
            <form onSubmit={handleRegisterSubmit}>
              <div style={{ marginBottom: '12px' }}>
                <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Họ và tên cán bộ *</label>
                <input
                  type="text"
                  required
                  placeholder="Ví dụ: Nguyễn Văn Duy"
                  value={adminForm.fullName}
                  onChange={(e) => setAdminForm({ ...adminForm, fullName: e.target.value })}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                />
              </div>

              <div style={{ marginBottom: '12px' }}>
                <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Email công vụ (*@danang.gov.vn) *</label>
                <input
                  type="email"
                  required
                  placeholder="canbo.nhs@danang.gov.vn"
                  value={adminForm.email}
                  onChange={(e) => setAdminForm({ ...adminForm, email: e.target.value })}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '12px', marginBottom: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Cơ quan / Đơn vị công tác</label>
                  <select
                    value={adminForm.department}
                    onChange={(e) => setAdminForm({ ...adminForm, department: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: '#081424', color: '#fff', fontSize: '0.84rem' }}
                  >
                    <option value="UBND Quận Ngũ Hành Sơn">UBND Quận Ngũ Hành Sơn</option>
                    <option value="Ban PCTT & TKCN Quận NHS">Ban PCTT & TKCN Quận</option>
                    <option value="UBND Phường Mỹ An">UBND Phường Mỹ An</option>
                    <option value="UBND Phường Khuê Mỹ">UBND Phường Khuê Mỹ</option>
                    <option value="UBND Phường Hòa Quý">UBND Phường Hòa Quý</option>
                    <option value="UBND Phường Hòa Hải">UBND Phường Hòa Hải</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Mã PIN ủy quyền *</label>
                  <input
                    type="password"
                    required
                    placeholder="Mã PIN 6 số"
                    value={adminForm.authCode}
                    onChange={(e) => setAdminForm({ ...adminForm, authCode: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Mật khẩu quản trị *</label>
                  <input
                    type="password"
                    required
                    placeholder="Tối thiểu 8 ký tự"
                    value={adminForm.password}
                    onChange={(e) => setAdminForm({ ...adminForm, password: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.78rem', marginBottom: '4px', color: '#F3E5AB' }}>Nhập lại mật khẩu *</label>
                  <input
                    type="password"
                    required
                    placeholder="Khớp với mật khẩu"
                    value={adminForm.confirmPassword}
                    onChange={(e) => setAdminForm({ ...adminForm, confirmPassword: e.target.value })}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid rgba(212,175,55,0.3)', background: 'rgba(255,255,255,0.06)', color: '#fff', fontSize: '0.84rem' }}
                  />
                </div>
              </div>

              <button
                type="submit"
                style={{
                  width: '100%',
                  padding: '12px',
                  borderRadius: '8px',
                  border: 'none',
                  background: 'linear-gradient(135deg, #D4AF37, #B58A28)',
                  color: '#081424',
                  fontWeight: '800',
                  fontSize: '0.92rem',
                  cursor: 'pointer'
                }}
              >
                🏛 Đăng Ký Cán Bộ Điều Phối
              </button>
            </form>
          )}

          {/* Quick Demo Access Mode */}
          <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid rgba(212,175,55,0.2)', textAlign: 'center' }}>
            <div style={{ fontSize: '0.74rem', color: 'rgba(249,243,232,0.65)', textTransform: 'uppercase', fontWeight: '700', marginBottom: '8px' }}>
              Trải nghiệm nhanh không cần mật khẩu (Demo):
            </div>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                onClick={() => { window.location.href = 'citizen/dashboard.html'; }}
                style={{
                  flex: 1,
                  padding: '8px',
                  borderRadius: '6px',
                  border: '1px solid rgba(74,122,66,0.6)',
                  background: 'rgba(74,122,66,0.15)',
                  color: '#86efac',
                  fontSize: '0.8rem',
                  fontWeight: '700',
                  cursor: 'pointer'
                }}
              >
                👤 Vào Cổng Người Dân
              </button>
              <button
                onClick={() => { window.location.href = 'admin/dashboard.html'; }}
                style={{
                  flex: 1,
                  padding: '8px',
                  borderRadius: '6px',
                  border: '1px solid rgba(212,175,55,0.6)',
                  background: 'rgba(212,175,55,0.15)',
                  color: '#F3E5AB',
                  fontSize: '0.8rem',
                  fontWeight: '700',
                  cursor: 'pointer'
                }}
              >
                🏛 Vào Cổng Admin
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
