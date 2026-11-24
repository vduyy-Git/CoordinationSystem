(function () {
  const api = window.CoordAPI;
  if (!api || !api.requireSession('../index.html')) return;
  const page = location.pathname.split('/').pop().replace('.html', '');
  let profile = null;
  let services = [];

  const escapeHtml = value => String(value || '').replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
  const date = value => value ? new Date(value).toLocaleDateString('vi-VN') : '';
  const priority = value => {
    const text = String(value || '').toUpperCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
    if (text.includes('URGENT') || text.includes('KHAN')) return 'URGENT';
    if (text.includes('HIGH') || text.includes('CAO')) return 'HIGH';
    if (text.includes('LOW') || text.includes('THAP')) return 'LOW';
    return 'NORMAL';
  };

  async function loadProfile() {
    profile = await api.get('/profile');
    document.querySelectorAll('.sidebar-user').forEach(node => {
      const name = node.querySelector('strong, .sidebar-user-name');
      if (name) name.textContent = profile.fullName || '';
    });
    const displayName = profile.fullName || 'Người dân';
    const initials = displayName.split(/\s+/).filter(Boolean).slice(-2).map(part => part[0]).join('').toLocaleUpperCase('vi-VN') || 'ND';
    document.querySelectorAll('.sidebar-avatar, #citizenSidebarAvatar').forEach(node => node.textContent = initials);
    document.querySelectorAll('.sidebar-user-role').forEach(node => node.textContent = profile.ward ? `Người dân (${profile.ward})` : 'Người dân');
    renderProfile();
    populateProfileForm();
    return profile;
  }

  function renderProfile() {
    if (page !== 'profile' || !profile) return;
    const fullName = profile.fullName || 'Chưa cập nhật họ tên';
    const initials = fullName.split(/\s+/).filter(Boolean).slice(-2).map(part => part[0]).join('').toLocaleUpperCase('vi-VN') || 'ND';
    document.querySelectorAll('[data-profile-name]').forEach(node => node.textContent = fullName);
    document.querySelectorAll('[data-profile-initials]').forEach(node => node.textContent = initials);
    document.querySelector('[data-profile-citizen-id]').textContent = profile.citizenId || 'Chưa cập nhật';
    document.querySelector('[data-profile-address]').textContent = profile.address || 'Chưa cập nhật địa chỉ';
    document.querySelector('[data-profile-phone]').textContent = profile.phone || 'Chưa cập nhật số điện thoại';
    document.querySelector('[data-profile-group]').textContent = profile.beneficiaryGroup || 'Chưa cập nhật';
    const status = document.querySelector('[data-profile-status]');
    if (status) status.textContent = profile.citizenId ? 'Đã cập nhật CCCD' : 'Chưa cập nhật CCCD';
    const role = document.querySelector('.sidebar-user-role');
    if (role) role.textContent = profile.ward ? `Người dân (${profile.ward})` : 'Người dân';
  }

  function showProfileMessage(message, isError = false) {
    const node = document.querySelector('#profileMessage');
    if (!node) return;
    node.hidden = false;
    node.textContent = message;
    node.style.background = isError ? 'var(--red-pale)' : 'var(--moss-pale)';
    node.style.color = isError ? 'var(--red)' : 'var(--moss-dark)';
  }

  async function loadDashboard() {
    const [summary, notices, shipments, requests] = await Promise.all([
      api.get('/dashboard/summary'), api.get('/notifications/mine'), api.get('/shipments/mine'), api.get('/assistance-requests/mine')
    ]);
    const vals = document.querySelectorAll('.stat-val');
    if (vals.length >= 4) {
      const closedStatuses = new Set(['COMPLETED', 'REJECTED', 'CANCELLED', 'DELIVERED', 'RETURNED']);
      vals[0].textContent = requests.filter(r => !closedStatuses.has(String(r.status).toUpperCase())).length;
      vals[1].textContent = summary.requests || 0;
      vals[2].textContent = summary.shipmentsByStatus?.DELIVERED || 0;
      vals[3].textContent = notices.length;
    }
    const welcomeName = document.querySelector('#citizenWelcomeName');
    if (welcomeName) welcomeName.textContent = profile?.fullName || 'Người dân';
    const welcomeSummary = document.querySelector('#citizenWelcomeSummary');
    if (welcomeSummary) welcomeSummary.textContent = [profile?.ward, profile?.address].filter(Boolean).join(' · ') || 'Hãy cập nhật địa chỉ trong hồ sơ để nhận hỗ trợ chính xác.';

    const requestList = document.querySelector('#citizenRequestsList');
    if (requestList) requestList.innerHTML = requests.map(r => `<div style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid var(--beige-dark);gap:12px;">
      <div><div style="font-weight:700;">${escapeHtml(r.title || 'Yêu cầu hỗ trợ')}</div><div style="font-size:0.75rem;color:var(--text-light);">${escapeHtml(r.requestCode || '')} · ${date(r.createdAt)}</div></div>
      <span class="badge">${escapeHtml(r.status || '—')}</span></div>`).join('') || '<p>Bạn chưa gửi yêu cầu hỗ trợ nào.</p>';

    const noticeList = document.querySelector('#citizenDashboardNotices');
    if (noticeList) noticeList.innerHTML = notices.slice(0, 3).map(n => `<div style="padding:10px 0;border-bottom:1px solid var(--beige-dark);">
      <div style="font-weight:700;color:var(--moss-dark);font-size:0.9rem;">${escapeHtml(n.title)}</div>
      <div style="font-size:0.78rem;color:var(--text-light);margin-top:2px;">${escapeHtml(n.message)} · ${date(n.createdAt)}</div></div>`).join('') || '<p>Chưa có thông báo.</p>';

    const current = shipments.find(s => !['DELIVERED','CANCELLED','RETURNED'].includes(String(s.status).toUpperCase())) || shipments[0];
    const shipmentTitle = document.querySelector('#citizenShipmentTitle');
    const shipmentStatus = document.querySelector('#citizenShipmentStatus');
    const shipmentDetails = document.querySelector('#citizenShipmentDetails');
    if (current) {
      if (shipmentTitle) shipmentTitle.textContent = `Mã chuyến: ${current.shipmentCode || current.id}`;
      if (shipmentStatus) shipmentStatus.textContent = current.status || 'Đang xử lý';
      if (shipmentDetails) shipmentDetails.innerHTML = `<p><strong>Địa chỉ giao:</strong> ${escapeHtml(current.deliveryAddress || 'Chưa có địa chỉ')}</p>
        <p><strong>Mã theo dõi:</strong> ${escapeHtml(current.trackingCode || 'Chưa có')}</p>
        <p><strong>Ngày tạo:</strong> ${date(current.createdAt) || '—'}</p>
        <a href="tracking.html" class="btn btn-outline btn-sm" style="margin-top:10px;">Xem lộ trình chi tiết</a>`;
    } else if (shipmentDetails) {
      shipmentDetails.innerHTML = '<p>Hiện chưa có chuyến giao hỗ trợ.</p>';
      if (shipmentStatus) shipmentStatus.textContent = 'Chưa có chuyến giao';
    }
  }

  async function loadNeeds() {
    const requests = await api.get('/assistance-requests/mine');
    const tbody = document.querySelector('.citizen-table tbody');
    if (tbody && page === 'needs') tbody.innerHTML = requests.map(r => `<tr>
      <td style="font-weight:700;">${escapeHtml(r.title)}</td><td>${escapeHtml(r.description)}</td><td>${escapeHtml(r.priority)}</td>
      <td>${date(r.createdAt)}</td><td><span class="badge badge-pending">${escapeHtml(r.status)}</span></td>
      <td>${r.status === 'PENDING' ? `<button class="btn btn-danger btn-sm" onclick="cancelCitizenRequest(${r.id})">Hủy</button>` : ''}</td>
    </tr>`).join('') || '<tr><td colspan="6">Chưa có nhu cầu được gửi.</td></tr>';
  }

  async function loadRequests() {
    const requests = await api.get('/assistance-requests/mine');
    const card = document.querySelector('.grid-3 > div > .card:last-child .card-body');
    if (card && page === 'requests') card.innerHTML = requests.filter(r=>r.status==='PENDING').map(r => `<div style="padding:10px 0;border-bottom:1px solid var(--beige-dark);">
      <div style="display:flex;justify-content:space-between;"><strong>${escapeHtml(r.requestCode)}</strong><span class="badge badge-pending">${escapeHtml(r.status)}</span></div>
      <div>${escapeHtml(r.title)}</div><button class="btn btn-danger btn-sm" onclick="cancelCitizenRequest(${r.id})">Hủy đơn</button></div>`).join('') || '<p>Không có yêu cầu chờ xử lý.</p>';
  }

  async function loadTracking() {
    const [requests, shipments] = await Promise.all([api.get('/assistance-requests/mine'), api.get('/shipments/mine')]);
    const tbody = document.querySelector('.citizen-table tbody');
    if (tbody && page === 'tracking') tbody.innerHTML = shipments.map(s=>`<tr>
      <td>${escapeHtml(s.shipmentCode)}</td><td>${escapeHtml(s.deliveryAddress)}</td><td>${escapeHtml(s.trackingCode)}</td>
      <td>${date(s.createdAt)}</td><td><span class="badge badge-delivering">${escapeHtml(s.status)}</span></td>
      <td><button class="btn btn-ghost btn-sm" onclick="showCitizenShipment(${s.id})">Xem</button></td></tr>`).join('') ||
      requests.map(r=>`<tr><td>${escapeHtml(r.requestCode)}</td><td>${escapeHtml(r.title)}</td><td>—</td><td>${date(r.createdAt)}</td><td>${escapeHtml(r.status)}</td><td>—</td></tr>`).join('') || '<tr><td colspan="6">Chưa có yêu cầu hay chuyến giao.</td></tr>';
  }

  async function loadFeedback() {
    const [notices, shipments] = await Promise.all([api.get('/notifications/mine'), api.get('/shipments/mine')]);
    const cards = document.querySelectorAll('.grid-3 > .card .card-body');
    if (cards[0] && page === 'feedback') cards[0].innerHTML = notices.map(n=>`<div style="padding:12px 0;border-bottom:1px solid var(--beige-dark);">
      <strong>${escapeHtml(n.title)}</strong><p>${escapeHtml(n.message)}</p><small>${date(n.createdAt)}</small></div>`).join('') || '<p>Chưa có thông báo mới.</p>';
    const select = document.querySelector('#confirmSection form select');
    if (select) {
      const delivered = shipments.filter(s=>s.status==='DELIVERED');
      select.innerHTML = delivered.map(s=>`<option value="${s.id}">${escapeHtml(s.shipmentCode)} — ${escapeHtml(s.deliveryAddress)}</option>`).join('');
      if (!delivered.length) select.innerHTML = '<option value="">Chưa có chuyến giao cần xác nhận</option>';
    }
  }

  async function cancelCitizenRequest(id) {
    try { await api.post(`/assistance-requests/${id}/cancel`, {}); location.reload(); }
    catch (error) { api.error(error); }
  }
  window.cancelCitizenRequest = cancelCitizenRequest;
  window.showCitizenShipment = async id => {
    try { const events=await api.get(`/shipments/${id}/events`); alert(events.map(e=>`${e.status} · ${e.createdAt || ''}`).join('\n') || 'Chưa có sự kiện giao hàng.'); }
    catch(error) { api.error(error); }
  };

  function bindRequestForms() {
    if (page !== 'requests' && page !== 'needs') return;
    const form = document.querySelector('form');
    if (!form) return;
    form.onsubmit = async event => {
      event.preventDefault();
      const inputs=form.querySelectorAll('input[type="text"]'); const selects=form.querySelectorAll('select'); const textarea=form.querySelector('textarea');
      const isNeeds=page === 'needs';
      const title=isNeeds ? selects[0].value : inputs[0].value.trim();
      const desc=isNeeds ? `${inputs[0].value.trim()} — ${textarea?.value || ''}` : `${selects[0].value}. ${textarea?.value || ''}`;
      try {
        await api.post('/assistance-requests',{title:title.slice(0,200),description:desc,
          deliveryAddress:profile?.address || inputs[isNeeds ? 1 : 1]?.value || 'Địa chỉ đã đăng ký',
          priority:priority(isNeeds ? selects[1]?.value : selects[1]?.value)});
        alert('Đã gửi yêu cầu hỗ trợ.'); location.reload();
      } catch(error) { api.error(error); }
    };
  }

  function profileFormFields(form) {
    return {
      fullName:form.querySelector('#profileFullName'), citizenId:form.querySelector('#profileCitizenId'),
      phone:form.querySelector('#profilePhone'), birthDate:form.querySelector('#profileBirthDate'),
      address:form.querySelector('#profileAddress'), ward:form.querySelector('#profileWard'),
      beneficiaryGroup:form.querySelector('#profileBeneficiaryGroup'), situationDescription:form.querySelector('#profileSituation')
    };
  }

  function populateProfileForm() {
    if (page !== 'profile' || !profile) return;
    const form=document.querySelector('#profileForm');
    if (!form) return;
    const fields=profileFormFields(form);
    fields.fullName.value=profile.fullName||''; fields.citizenId.value=profile.citizenId||'';
    fields.phone.value=profile.phone||''; fields.birthDate.value=profile.birthDate ? String(profile.birthDate).slice(0,10) : '';
    fields.address.value=profile.address||''; fields.ward.value=profile.ward||'';
    fields.beneficiaryGroup.value=profile.beneficiaryGroup||''; fields.situationDescription.value=profile.situationDescription||'';
  }

  function bindProfile() {
    if (page !== 'profile') return;
    const form=document.querySelector('#profileForm');
    if (!form) return;
    form.addEventListener('submit', async e=>{
      e.preventDefault();
      if (!profile) return showProfileMessage('Chưa tải được hồ sơ. Hãy tải lại trang rồi thử lại.', true);
      const fields=profileFormFields(form);
      try {
        const updated=await api.put('/profile',{fullName:fields.fullName.value.trim(),email:profile.email,phone:fields.phone.value.trim(),citizenId:fields.citizenId.value.trim(),
          birthDate:fields.birthDate.value||null,address:fields.address.value.trim(),ward:fields.ward.value.trim(),beneficiaryGroup:fields.beneficiaryGroup.value.trim(),
          situationDescription:fields.situationDescription.value.trim(),avatar:profile.avatar});
        profile=updated || {...profile,fullName:fields.fullName.value.trim(),phone:fields.phone.value.trim(),citizenId:fields.citizenId.value.trim(),
          birthDate:fields.birthDate.value||null,address:fields.address.value.trim(),ward:fields.ward.value.trim(),beneficiaryGroup:fields.beneficiaryGroup.value.trim(),
          situationDescription:fields.situationDescription.value.trim()};
        renderProfile(); showProfileMessage('Đã cập nhật hồ sơ.');
      }
      catch(error) { showProfileMessage(error?.message || 'Không thể cập nhật hồ sơ.', true); }
    });
  }

  window.registerService = async function (name) {
    try {
      if (!services.length) services = await api.get('/services');
      const service=services.find(s=>s.name.toLowerCase()===name.toLowerCase()) || services.find(s=>s.name.toLowerCase().includes(name.toLowerCase().slice(0,12)));
      if (!service) throw new Error('Dịch vụ này chưa được khai báo trong danh mục hệ thống.');
      await api.post('/service-registrations',{serviceId:service.id}); alert('Đã gửi đăng ký dịch vụ.');
    } catch(error) { api.error(error); }
  };

  async function bindFeedback() {
    if (page !== 'feedback') return;
    const form=document.querySelector('#confirmSection form');
    if (!form) return;
    form.onsubmit=async e=>{
      e.preventDefault(); const selects=form.querySelectorAll('select'); const textarea=form.querySelector('textarea');
      if (!selects[0].value) return alert('Chưa có chuyến giao cần xác nhận.');
      try { await api.post('/feedback',{shipmentId:Number(selects[0].value),receivedBy:profile?.fullName || 'Người nhận',
        deliveryCondition:['INTACT','WET_PACKAGING','SHORTAGE'][selects[1].selectedIndex] || 'INTACT',rating:5,comment:textarea?.value || ''});
        alert('Đã ghi nhận xác nhận và phản hồi.'); location.href='tracking.html'; }
      catch(error) { api.error(error); }
    };
  }

  bindProfile();
  (async function init() {
    try {
      await loadProfile();
      if (page === 'dashboard') await loadDashboard();
      if (page === 'needs') await loadNeeds();
      if (page === 'requests') await loadRequests();
      if (page === 'tracking') await loadTracking();
      if (page === 'feedback') await loadFeedback();
      bindRequestForms(); await bindFeedback();
    } catch(error) {
      if (page === 'profile') showProfileMessage(error?.message || 'Không thể tải hồ sơ.', true);
      else api.error(error);
    }
  })();})();
