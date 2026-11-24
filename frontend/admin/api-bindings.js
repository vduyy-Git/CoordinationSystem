(function () {
  const api=window.CoordAPI;
  if (!api || !api.requireSession('../index.html')) return;
  const esc=v=>String(v||'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const dte=v=>v?new Date(v).toLocaleDateString('vi-VN'):'';

  async function loadRequests() {
    const rows=await api.get('/assistance-requests');
    const body=document.querySelector('.admin-table tbody'); if(!body) return;
    body.innerHTML=rows.map(r=>`<tr><td>${esc(r.requestCode)}</td><td>${esc(r.recipient?.fullName)}<div>${esc(r.recipient?.phone)}</div></td>
      <td>${esc(r.title)}</td><td><span class="badge">${esc(r.priority)}</span></td><td>—</td><td>${esc(r.status)}</td><td>${dte(r.createdAt)}</td>
      <td>${r.status==='PENDING'?`<button class="btn btn-success btn-sm" onclick="adminDecideRequest(${r.id},'approve')">Duyệt</button> <button class="btn btn-danger btn-sm" onclick="adminDecideRequest(${r.id},'reject')">Từ chối</button>`:''}</td></tr>`).join('');
  }
  window.adminDecideRequest=async(id,action)=>{try{await api.post(`/assistance-requests/${id}/${action}`,{});await loadRequests();}catch(e){api.error(e);}};

  async function loadUsers(citizensOnly=false) {
    const [users,assignments]=await Promise.all([api.get('/users'),api.get('/user-roles')]);
    const roles=new Map(); assignments.forEach(row=>{const id=row.user?.id;if(!roles.has(id))roles.set(id,[]);roles.get(id).push(row.role?.roleName);});
    const body=document.querySelector('.admin-table tbody'); if(!body) return;
    const list=users.filter(u=>!citizensOnly || (roles.get(u.id)||[]).includes('USER'));
    body.innerHTML=list.map(u=>citizensOnly?`<tr><td>${esc(u.fullName)}<div>${esc(u.phone)}</div></td><td>${esc(u.citizenId)}</td><td>${esc(u.beneficiaryGroup)}</td>
      <td>${esc(u.ward)}</td><td>${esc(u.situationDescription)}</td><td>${esc(u.status)}</td><td>${u.status==='ACTIVE'?'<span class="badge badge-approved">Đang hoạt động</span>':'<span class="badge badge-pending">'+esc(u.status)+'</span>'}</td>
      <td></td></tr>`:`<tr><td>${esc((u.fullName||'?').slice(0,2))}</td><td>${esc(u.fullName)}</td><td>${esc(u.email)}</td><td>${esc(u.phone)}</td>
      <td>${(roles.get(u.id)||[]).map(esc).join(', ')}</td><td>${esc(u.status)}</td><td>${dte(u.createdAt)}</td><td><button class="btn btn-ghost btn-sm" onclick="toggleUserStatus(${u.id},'${u.status==='ACTIVE'?'INACTIVE':'ACTIVE'}')">${u.status==='ACTIVE'?'Khóa':'Mở khóa'}</button></td></tr>`).join('');
  }
  window.toggleUserStatus=async(id,status)=>{try{const u=await api.get(`/users/${id}`);await api.put(`/users/${id}`,{...u,status});location.reload();}catch(e){api.error(e);}};

  async function loadResources() {
    const rows=await api.get('/inventory/summary'); const body=document.querySelector('.admin-table tbody'); if(!body)return;
    body.innerHTML=rows.map(i=>`<tr><td>${esc(i.itemName)}</td><td>${esc(i.category)}</td><td>${esc(i.unit)}</td><td>${i.accepted}</td><td>${i.reserved}</td>
      <td>${i.available}</td><td>${i.accepted?Math.round(Number(i.available)/Number(i.accepted)*100):0}%</td><td><a class="btn btn-ghost btn-sm" href="allocations.html">Điều phối</a></td></tr>`).join('');
  }

  async function loadServices() {
    const [rows,registrations]=await Promise.all([api.get('/admin/services'),api.get('/admin/service-registrations')]); const body=document.querySelector('.admin-table tbody'); if(!body)return;
    body.innerHTML=rows.map(s=>`<tr><td>${esc(s.name)}</td><td>${esc(s.serviceType)}</td><td>${esc(s.description)}</td><td>${esc(s.serviceArea)}</td><td>${esc(s.status)}</td><td></td></tr>`).join('');
    let section=document.getElementById('admin-service-registrations');
    if(!section){section=document.createElement('section');section.id='admin-service-registrations';body.closest('table').parentElement.insertAdjacentElement('afterend',section);}
    section.innerHTML=`<h2>Đăng ký dịch vụ của người dân</h2><table class="admin-table"><thead><tr><th>Người dân</th><th>Dịch vụ</th><th>Nội dung</th><th>Ngày đăng ký</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>${registrations.map(r=>`<tr><td>${esc(r.user?.fullName)}</td><td>${esc(r.service?.name)}</td><td>${esc(r.requestDetails)}</td><td>${dte(r.createdAt)}</td><td>${esc(r.status)}</td><td>${r.status==='PENDING'?`<button onclick="setServiceRegistrationStatus(${r.id},'APPROVED')">Duyệt</button> <button onclick="setServiceRegistrationStatus(${r.id},'REJECTED')">Từ chối</button>`:r.status==='APPROVED'?`<button onclick="setServiceRegistrationStatus(${r.id},'COMPLETED')">Hoàn tất</button>`:''}</td></tr>`).join('')}</tbody></table>`;
    window.setServiceRegistrationStatus=async(id,status)=>{try{await api.request(`/admin/service-registrations/${id}/status?value=${status}`,{method:'PUT'});await loadServices();}catch(error){api.error(error);}};
    const form=document.querySelector('#addModal form'); if(!form)return;
    form.onsubmit=async e=>{e.preventDefault();const fields=form.querySelectorAll('select,input,textarea');try{
      await api.post('/admin/services',{serviceType:fields[0].selectedIndex===0?'SOCIAL_SERVICE':'EQUIPMENT',name:fields[1].value.trim(),description:fields[2].value,status:'ACTIVE'});
      document.getElementById('addModal').style.display='none';form.reset();await loadServices();
    }catch(error){api.error(error);}};
  }

  async function loadDashboard() {
    const [summary,requests]=await Promise.all([api.get('/dashboard/summary'),api.get('/assistance-requests')]);
    const values=document.querySelectorAll('.stat-val,.stat-value,.kpi-value');
    const pending=requests.filter(row=>row.status==='PENDING');
    if(values.length>=4){values[0].textContent=summary.beneficiaries||0;values[1].textContent=pending.length;values[2].textContent=summary.allocations;values[3].textContent=pending.filter(row=>String(row.priority).toUpperCase()==='URGENT').length;}
  }

  async function bindReports() {
    const summary=await api.get('/reports/summary');
    const values=document.querySelectorAll('.stat-val,.stat-value');
    if(values.length>=4){values[0].textContent=summary.donations;values[1].textContent=summary.shipmentsByStatus?.DELIVERED||0;values[2].textContent=summary.beneficiaries||0;values[3].textContent=summary.allocations;}
    document.querySelectorAll('.stat-delta').forEach(node=>node.hidden=true);
    Array.from(document.querySelectorAll('button')).forEach(button=>{
      const text=button.textContent.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g,'');
      if(!text.includes('bao cao')&&!text.includes('xuat')&&!text.includes('danh sach'))return;
      button.onclick=null;
      button.addEventListener('click',async event=>{
        event.preventDefault();
        const dataset=text.includes('quyen gop')?'donations':text.includes('yeu cau')?'requests':text.includes('phan bo')||text.includes('cap phat')?'allocations':text.includes('van chuyen')||text.includes('giao nhan')?'shipments':text.includes('thu huong')?'beneficiaries':text.includes('tong hop')||text.includes('xuat file bao cao')?'summary':'requests';
        try{await api.download(`/reports/export?dataset=${dataset}&format=csv`,`coordination-${dataset}.csv`);}catch(error){api.error(error);}
      });
    });
  }

  async function loadAllocations() {
    const [rows,items]=await Promise.all([api.get('/allocations'),api.get('/allocation-items')]);
    const body=document.querySelector('.admin-table tbody');if(!body)return;
    body.innerHTML=rows.map(a=>{const details=items.filter(i=>i.allocation?.id===a.id).map(i=>`${i.donationItem?.item?.name||i.donationItem?.itemName||'Vật tư'}: ${i.quantity}`).join(', ');
      return `<tr><td>${esc(a.allocationCode)}</td><td>${esc(a.request?.requestCode||a.request?.title)}</td><td>${esc(details)}</td><td>${esc(a.status)}</td><td>${dte(a.allocatedAt)}</td><td><a class="btn btn-ghost btn-sm" href="../src/pages/allocations.html">Mở điều phối</a></td></tr>`;
    }).join('')||'<tr><td colspan="6">Chưa có bản ghi phân bổ.</td></tr>';
  }
  (async function init(){
    try{
      const page=location.pathname.split('/').pop();
      if(page==='requests.html') await loadRequests();
      else if(page==='accounts.html') await loadUsers(false);
      else if(page==='citizens.html') await loadUsers(true);
      else if(page==='resources.html') await loadResources();
      else if(page==='services.html') await loadServices();
      else if(page==='allocations.html') await loadAllocations();
      else if(page==='dashboard.html') await loadDashboard();
      else if(page==='reports.html') await bindReports();
    }catch(error){api.error(error);}
  })();})();
