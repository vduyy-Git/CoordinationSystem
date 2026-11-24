(function () {
  const api = window.CoordAPI;
  if (!api || !location.pathname.endsWith('/src/pages/admin.html') || !api.requireSession('../../index.html')) return;

  const esc = value => String(value ?? '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
  const panel = key => document.getElementById(`panel-${key}`);
  const field = (label, name, type = 'text', required = true) => `<label style="display:grid;gap:6px;font-size:.86rem;">${label}<input name="${name}" type="${type}" ${required ? 'required' : ''} style="padding:10px;border-radius:6px;border:1px solid #ccc;"></label>`;
  const formBox = (title, fields, action) => `<form data-action="${action}" class="glass-card" style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:12px;padding:16px;margin-bottom:16px;"><strong style="grid-column:1/-1">${title}</strong>${fields}<button class="btn-gold" type="submit">Thêm</button></form>`;
  const table = (headers, rows) => `<div class="glass-card" style="overflow:auto;padding:14px"><table class="data-table"><thead><tr>${headers.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>${rows || `<tr><td colspan="${headers.length}">Chưa có dữ liệu</td></tr>`}</tbody></table></div>`;

  async function loadUsers() {
    const [users, assignments, roles, applications] = await Promise.all([api.get('/users'), api.get('/user-roles'), api.get('/roles'), api.get('/admin/staff-registrations')]);
    const roleByUser = new Map();
    assignments.forEach(row => { const id = row.user?.id; roleByUser.set(id, [...(roleByUser.get(id) || []), row.role?.roleName]); });
    panel('users').innerHTML = `<div style="font-weight:800;font-size:1.05rem;margin-bottom:14px">Quản lý tài khoản (${users.length})</div>
      <h3>Hồ sơ nhân viên chờ duyệt</h3>${table(['Họ tên','Email','Phòng ban','Ngày gửi','Trạng thái','Thao tác'],applications.map(a=>`<tr><td>${esc(a.fullName)}</td><td>${esc(a.email)}</td><td>${esc(a.department)}</td><td>${esc(a.createdAt)}</td><td>${esc(a.status)}</td><td>${a.status==='PENDING'?`<button data-staff-decision="${a.id}" data-action="approve">Duyệt</button> <button data-staff-decision="${a.id}" data-action="reject">Từ chối</button>`:''}</td></tr>`).join(''))}
      ${formBox('Tạo tài khoản', `${field('Họ tên','fullName')}${field('Email','email','email')}${field('Điện thoại','phone','tel',false)}${field('Mật khẩu (tối thiểu 8 ký tự)','password','password')}<label style="display:grid;gap:6px">Vai trò<select name="roleId" required>${roles.map(r=>`<option value="${r.id}">${esc(r.roleName)}</option>`).join('')}</select></label>`, 'create-user')}
      ${table(['Họ tên','Email','Điện thoại','Vai trò','Trạng thái','Thao tác'], users.map(u=>`<tr><td>${esc(u.fullName)}</td><td>${esc(u.email)}</td><td>${esc(u.phone)}</td><td>${(roleByUser.get(u.id)||[]).map(esc).join(', ')}</td><td>${esc(u.status)}</td><td><button class="btn-outline" data-toggle-user="${u.id}" data-status="${u.status==='ACTIVE'?'INACTIVE':'ACTIVE'}">${u.status==='ACTIVE'?'Khóa':'Mở khóa'}</button></td></tr>`).join(''))}`;
    panel('users').querySelector('[data-action="create-user"]').onsubmit = async event => {
      event.preventDefault(); const data = Object.fromEntries(new FormData(event.currentTarget));
      try { const created = await api.post('/users', { fullName:data.fullName, email:data.email, phone:data.phone, password:data.password, status:'ACTIVE' });
        await api.post('/user-roles', { user:{id:created.id}, role:{id:Number(data.roleId)} }); await loadUsers();
      } catch (error) { api.error(error); }
    };
    panel('users').querySelectorAll('[data-toggle-user]').forEach(button => button.onclick = async () => {
      try { const user = await api.get(`/users/${button.dataset.toggleUser}`); await api.put(`/users/${user.id}`, {...user,status:button.dataset.status}); await loadUsers(); }
      catch (error) { api.error(error); }
    });
    panel('users').querySelectorAll('[data-staff-decision]').forEach(button => button.onclick = async () => {
      try { await api.post(`/admin/staff-registrations/${button.dataset.staffDecision}/${button.dataset.action}`,{}); await loadUsers(); }
      catch (error) { api.error(error); }
    });
  }

  async function loadItems() {
    const [categories, units, items] = await Promise.all([api.get('/categories'), api.get('/units'), api.get('/items')]);
    const catOptions = categories.map(c=>`<option value="${c.id}">${esc(c.name)}</option>`).join('');
    const unitOptions = units.map(u=>`<option value="${u.id}">${esc(u.unitName)} (${esc(u.symbol)})</option>`).join('');
    panel('items').innerHTML = `<div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(260px,1fr));gap:14px;margin-bottom:16px">
      ${formBox('Thêm danh mục',`${field('Tên danh mục','name')}${field('Mô tả','description','text',false)}`,'create-category')}
      ${formBox('Thêm đơn vị tính',`${field('Tên đơn vị','unitName')}${field('Ký hiệu','symbol')}`,'create-unit')}
      ${formBox('Thêm vật tư',`${field('Tên vật tư','name')}<label>Danh mục<select name="categoryId" required>${catOptions}</select></label><label>Đơn vị<select name="unitId" required>${unitOptions}</select></label>${field('Mô tả','description','text',false)}`,'create-item')}
      </div><h3>Danh mục và đơn vị</h3>${table(['Danh mục','Mô tả','Thao tác'],categories.map(c=>`<tr><td>${esc(c.name)}</td><td>${esc(c.description)}</td><td><button data-edit-category="${c.id}">Sửa</button> <button data-delete-category="${c.id}">Xóa</button></td></tr>`).join(''))}
      ${table(['Đơn vị','Ký hiệu','Thao tác'],units.map(u=>`<tr><td>${esc(u.unitName)}</td><td>${esc(u.symbol)}</td><td><button data-edit-unit="${u.id}">Sửa</button> <button data-delete-unit="${u.id}">Xóa</button></td></tr>`).join(''))}
      <h3>Vật tư</h3>${table(['Tên vật tư','Danh mục','Đơn vị','Mô tả','Trạng thái','Thao tác'],items.map(i=>`<tr><td>${esc(i.name)}</td><td>${esc(i.category?.name)}</td><td>${esc(i.unit?.symbol)}</td><td>${esc(i.description)}</td><td>${esc(i.status)}</td><td><button data-edit-item="${i.id}">Sửa</button> <button data-delete-item="${i.id}">Xóa</button></td></tr>`).join(''))}`;
    const root = panel('items');
    root.querySelector('[data-action="create-category"]').onsubmit = e => submitForm(e, async d => api.post('/categories',{name:d.name,description:d.description}));
    root.querySelector('[data-action="create-unit"]').onsubmit = e => submitForm(e, async d => api.post('/units',{unitName:d.unitName,symbol:d.symbol}));
    root.querySelector('[data-action="create-item"]').onsubmit = e => submitForm(e, async d => api.post('/items',{name:d.name,category:{id:Number(d.categoryId)},unit:{id:Number(d.unitId)},description:d.description,status:'ACTIVE'}));
    root.querySelectorAll('[data-edit-category]').forEach(b=>b.onclick=()=>editFields(`/categories/${b.dataset.editCategory}`,['name','description'],loadItems));
    root.querySelectorAll('[data-edit-unit]').forEach(b=>b.onclick=()=>editFields(`/units/${b.dataset.editUnit}`,['unitName','symbol'],loadItems));
    root.querySelectorAll('[data-edit-item]').forEach(b=>b.onclick=async()=>{try{const item=await api.get(`/items/${b.dataset.editItem}`);const name=prompt('Tên vật tư',item.name);if(name===null)return;const description=prompt('Mô tả',item.description||'');if(description===null)return;await api.put(`/items/${item.id}`,{...item,name,description});await loadItems();}catch(e){api.error(e);}});
    root.querySelectorAll('[data-delete-category]').forEach(b=>b.onclick=()=>deleteRow(`/categories/${b.dataset.deleteCategory}`,loadItems));
    root.querySelectorAll('[data-delete-unit]').forEach(b=>b.onclick=()=>deleteRow(`/units/${b.dataset.deleteUnit}`,loadItems));
    root.querySelectorAll('[data-delete-item]').forEach(b=>b.onclick=()=>deleteRow(`/items/${b.dataset.deleteItem}`,loadItems));
  }

  async function loadPoints() {
    const rows=await api.get('/collection-points');
    panel('points').innerHTML=`${formBox('Thêm điểm tập kết',`${field('Tên điểm','name')}${field('Địa chỉ','address')}${field('Người liên hệ','contactPerson','text',false)}${field('Điện thoại','phone','tel',false)}${field('Ngày bắt đầu','startDate','date',false)}`,'create-point')}${table(['Tên điểm','Địa chỉ','Liên hệ','Điện thoại','Trạng thái','Thao tác'],rows.map(p=>`<tr><td>${esc(p.name)}</td><td>${esc(p.address)}</td><td>${esc(p.contactPerson)}</td><td>${esc(p.phone)}</td><td>${esc(p.status)}</td><td><button data-edit-point="${p.id}">Sửa</button> <button data-delete-point="${p.id}">Xóa</button></td></tr>`).join(''))}`;
    const root=panel('points');root.querySelector('[data-action="create-point"]').onsubmit=e=>submitForm(e,d=>api.post('/collection-points',{...d,status:'ACTIVE'}));
    root.querySelectorAll('[data-edit-point]').forEach(b=>b.onclick=async()=>{try{const p=await api.get(`/collection-points/${b.dataset.editPoint}`);const name=prompt('Tên điểm',p.name);if(name===null)return;const address=prompt('Địa chỉ',p.address);if(address===null)return;await api.put(`/collection-points/${p.id}`,{...p,name,address});await loadPoints();}catch(e){api.error(e);}});
    root.querySelectorAll('[data-delete-point]').forEach(b=>b.onclick=()=>deleteRow(`/collection-points/${b.dataset.deletePoint}`,loadPoints));
  }

  async function loadPartners() {
    const rows=await api.get('/shipping-partners');
    panel('partners').innerHTML=`${formBox('Thêm đối tác vận chuyển',`${field('Tên đối tác','partnerName')}${field('Điện thoại','phone','tel',false)}${field('Website','website','url',false)}${field('API endpoint','apiEndpoint','url',false)}`,'create-partner')}${table(['Đối tác','Điện thoại','Website','Trạng thái','Thao tác'],rows.map(p=>`<tr><td>${esc(p.partnerName)}</td><td>${esc(p.phone)}</td><td>${esc(p.website)}</td><td>${esc(p.status)}</td><td><button data-edit-partner="${p.id}">Sửa</button> <button data-delete-partner="${p.id}">Xóa</button></td></tr>`).join(''))}`;
    const root=panel('partners');root.querySelector('[data-action="create-partner"]').onsubmit=e=>submitForm(e,d=>api.post('/shipping-partners',{...d,status:'ACTIVE'}));
    root.querySelectorAll('[data-edit-partner]').forEach(b=>b.onclick=async()=>{try{const p=await api.get(`/shipping-partners/${b.dataset.editPartner}`);const name=prompt('Tên đối tác',p.partnerName);if(name===null)return;const phone=prompt('Điện thoại',p.phone||'');if(phone===null)return;const website=prompt('Website',p.website||'');if(website===null)return;await api.put(`/shipping-partners/${p.id}`,{...p,partnerName:name,phone,website});await loadPartners();}catch(e){api.error(e);}});
    root.querySelectorAll('[data-delete-partner]').forEach(b=>b.onclick=()=>deleteRow(`/shipping-partners/${b.dataset.deletePartner}`,loadPartners));
  }

  async function loadReports() {
    const r=await api.get('/reports/summary');
    panel('reports').innerHTML=`<div class="grid-3" style="gap:14px;margin-bottom:18px">${[['Yêu cầu hỗ trợ',r.requests],['Quyên góp',r.donations],['Phân bổ',r.allocations],['Chuyến giao',r.shipments],['Xác nhận giao',r.deliveryConfirmations]].map(([label,value])=>`<div class="glass-card" style="padding:20px"><div class="stat-value">${Number(value)||0}</div><div>${label}</div></div>`).join('')}</div>
      <div class="glass-card" style="padding:20px"><h3>Tải báo cáo CSV</h3><div style="display:flex;gap:10px;flex-wrap:wrap">${[['donations','Quyên góp'],['requests','Yêu cầu hỗ trợ'],['allocations','Phân bổ'],['inventory','Tồn kho'],['shipments','Vận chuyển'],['beneficiaries','Hộ thụ hưởng'],['summary','Tổng hợp']].map(([key,label])=>`<button class="btn-gold" data-report="${key}">${label}</button>`).join('')}</div></div>`;
    panel('reports').querySelectorAll('[data-report]').forEach(b=>b.onclick=async()=>{try{await api.download(`/reports/export?dataset=${b.dataset.report}&format=csv`,`coordination-${b.dataset.report}.csv`);}catch(e){api.error(e);}});
  }

  async function submitForm(event, action) { event.preventDefault();const form=event.currentTarget;const data=Object.fromEntries(new FormData(form));try{await action(data);await form.reset();await reloadActive();}catch(error){api.error(error);} }
  async function editFields(path, names, reload) { try{const row=await api.get(path);const updated={...row};for(const name of names){const value=prompt(name,row[name]||'');if(value===null)return;updated[name]=value;}await api.put(path,updated);await reload();}catch(error){api.error(error);} }
  async function deleteRow(path,reload) { if(!confirm('Xóa mục này?'))return;try{await api.del(path);await reload();}catch(error){api.error(error);} }
  async function reloadActive() { const active=document.querySelector('.admin-tab.active');if(active)await window.switchAdmin(active.dataset.panel,active); }
  const loaders={users:loadUsers,items:loadItems,points:loadPoints,partners:loadPartners,reports:loadReports};
  window.switchAdmin=async function(key,button){document.querySelectorAll('.admin-tab').forEach(t=>t.classList.remove('active'));document.querySelectorAll('.admin-panel').forEach(p=>p.classList.remove('active'));button?.classList.add('active');if(!button){button=document.querySelector(`.admin-tab[onclick*="'${key}'"]`);button?.classList.add('active');}panel(key)?.classList.add('active');try{await loaders[key]?.();}catch(error){api.error(error);}};
  document.querySelectorAll('.admin-tab').forEach((button,index)=>{const key=['users','items','points','partners','reports'][index];button.dataset.panel=key;});
  const isAdmin=(api.user()?.roles||[]).includes('ADMIN');
  if(!isAdmin){document.querySelector('.admin-tab[data-panel="users"]')?.remove();panel('users')?.remove();}
  const firstKey=isAdmin?'users':'items';
  window.switchAdmin(firstKey,document.querySelector(`.admin-tab[data-panel="${firstKey}"]`));
})();
