(function () {
  const api=window.CoordAPI;
  if (!api || !api.requireSession('../index.html')) return;
  const esc=v=>String(v||'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const dte=v=>v?new Date(v).toLocaleDateString('vi-VN'):'';

  async function loadRequests() {
    const [rows,requestItems]=await Promise.all([api.get('/assistance-requests'),api.get('/request-items')]);
    const body=document.querySelector('#adminRequestsTable tbody'); if(!body) return;
    const itemsByRequest=new Map();
    requestItems.forEach(line=>{
      const id=line.request?.id;
      if(id==null)return;
      if(!itemsByRequest.has(String(id)))itemsByRequest.set(String(id),[]);
      itemsByRequest.get(String(id)).push(`${line.item?.name||'Vật tư'}: ${line.requestedQuantity} ${line.item?.unit?.symbol||''}`.trim());
    });
    const priorityLabels={URGENT:'🚨 Khẩn cấp',HIGH:'🔴 Cao',NORMAL:'🟡 Thường',LOW:'⚪ Thấp'};
    const statusLabels={PENDING:'Chờ duyệt',APPROVED:'Đã duyệt',PARTIALLY_APPROVED:'Duyệt một phần',ALLOCATING:'Đang phân bổ',FULFILLING:'Đang giao',COMPLETED:'Hoàn thành',REJECTED:'Từ chối',CANCELLED:'Đã hủy'};
    const processed=new Set(['APPROVED','PARTIALLY_APPROVED','ALLOCATING','FULFILLING','COMPLETED']);
    const tabs=Array.from(document.querySelectorAll('[data-request-filter]'));
    const search=document.getElementById('adminRequestSearch');
    const prioritySelect=document.getElementById('adminRequestPriority');
    let activeFilter='all';
    const counts={
      all:rows.length,
      urgent:rows.filter(row=>String(row.priority).toUpperCase()==='URGENT').length,
      pending:rows.filter(row=>row.status==='PENDING').length,
      processed:rows.filter(row=>processed.has(String(row.status).toUpperCase())).length,
      rejected:rows.filter(row=>row.status==='REJECTED').length
    };
    tabs.forEach(tab=>{
      tab.textContent=`${tab.dataset.label} (${(counts[tab.dataset.requestFilter]||0).toLocaleString('en-US')})`;
      tab.addEventListener('click',()=>{
        activeFilter=tab.dataset.requestFilter;
        tabs.forEach(other=>other.classList.toggle('active',other===tab));
        render();
      });
    });
    const statValues=document.querySelectorAll('.stat-row .stat-val');
    [counts.urgent,counts.pending,rows.filter(row=>row.status==='ALLOCATING').length,rows.filter(row=>row.status==='COMPLETED').length]
      .forEach((value,index)=>{if(statValues[index])statValues[index].textContent=value.toLocaleString('en-US');});

    function render(){
      const query=normalizeFilterValue(search?.value);
      const selectedPriority=(prioritySelect?.value||'').toUpperCase();
      const filtered=rows.filter(row=>{
        const status=String(row.status||'').toUpperCase();
        if(activeFilter==='urgent'&&String(row.priority).toUpperCase()!=='URGENT')return false;
        if(activeFilter==='pending'&&status!=='PENDING')return false;
        if(activeFilter==='processed'&&!processed.has(status))return false;
        if(activeFilter==='rejected'&&status!=='REJECTED')return false;
        if(selectedPriority&&String(row.priority).toUpperCase()!==selectedPriority)return false;
        const searchable=[row.requestCode,row.title,row.description,row.deliveryAddress,row.recipient?.fullName,row.recipient?.phone]
          .map(normalizeFilterValue).join(' ');
        return !query||searchable.includes(query);
      });
      body.innerHTML=filtered.map(row=>{
        const priorityCode=String(row.priority||'').toUpperCase();
        const statusCode=String(row.status||'').toUpperCase();
        const items=(itemsByRequest.get(String(row.id))||[]).join(', ')||'Chưa khai báo vật tư';
        const rowStyle=priorityCode==='URGENT'&&statusCode==='PENDING'?' style="background:rgba(217,64,80,0.05);"':'';
        return `<tr${rowStyle}><td style="font-family:monospace;color:var(--gold-1);font-weight:700;">${esc(row.requestCode)}</td>
          <td style="font-weight:700;">${esc(row.recipient?.fullName||'—')}<div style="font-size:0.75rem;color:var(--text-light);">📱 ${esc(row.recipient?.phone||'Chưa có SĐT')}</div></td>
          <td>${esc(row.title)}</td><td><span class="badge">${esc(priorityLabels[priorityCode]||priorityCode||'—')}</span></td>
          <td>${esc(items)}</td><td><span class="badge">${esc(statusLabels[statusCode]||statusCode||'—')}</span></td><td>${dte(row.createdAt)||'—'}</td>
          <td>${statusCode==='PENDING'?`<button class="btn btn-success btn-sm" onclick="adminDecideRequest(${row.id},'approve')">Duyệt</button> <button class="btn btn-danger btn-sm" onclick="adminDecideRequest(${row.id},'reject')">Từ chối</button>`:''}</td></tr>`;
      }).join('')||'<tr><td colspan="8">Không tìm thấy yêu cầu phù hợp.</td></tr>';
    }
    search?.addEventListener('input',render);
    prioritySelect?.addEventListener('change',render);
    render();
  }
  window.adminDecideRequest=async(id,action)=>{try{await api.post(`/assistance-requests/${id}/${action}`,{});await loadRequests();}catch(e){api.error(e);}};

  function normalizeFilterValue(value) {
    return String(value??'').normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[đĐ]/g,'d').toLocaleLowerCase('vi-VN');
  }
  const citizenGroupTerms={
    elderly:['cao tuoi','nguoi gia','elderly','senior'],
    children:['tre em','thieu nien','child'],
    disability:['khuyet tat','disability'],
    poor:['ho ngheo','ngheo','kho khan','can ngheo','poor'],
    pregnant:['mang thai','co thai','thai san','pregnant']
  };
  function matchesCitizenGroup(user,group) {
    if(!group||group==='all') return true;
    const value=normalizeFilterValue(user.beneficiaryGroup);
    return (citizenGroupTerms[group]||[]).some(term=>value.includes(term));
  }
  function bindCitizenFilters(users,requests) {
    const body=document.querySelector('#citizensTable tbody');
    if(!body) return;
    const searchInput=document.getElementById('citizenSearch');
    const wardSelect=document.getElementById('citizenWardFilter');
    const prioritySelect=document.getElementById('citizenPriorityFilter');
    const tabs=Array.from(document.querySelectorAll('.tab-btn[data-group]'));
    const requestsByUser=new Map();
    (Array.isArray(requests)?requests:[]).forEach(request=>{
      const userId=request.recipient?.id;
      if(userId==null) return;
      const key=String(userId);
      if(!requestsByUser.has(key)) requestsByUser.set(key,new Set());
      if(request.priority) requestsByUser.get(key).add(String(request.priority).toUpperCase());
    });
    let activeGroup='all';

    const wards=[...new Set(users.map(user=>String(user.ward||'').trim()).filter(Boolean))]
      .sort((a,b)=>a.localeCompare(b,'vi'));
    if(wardSelect) wardSelect.innerHTML='<option value="">Tất cả khu vực / Tổ dân phố</option>'+wards.map(ward=>`<option value="${esc(ward)}">${esc(ward)}</option>`).join('');

    const groups=['elderly','children','disability','poor','pregnant'];
    const groupCounts=Object.fromEntries(groups.map(group=>[group,users.filter(user=>matchesCitizenGroup(user,group)).length]));
    tabs.forEach(tab=>{
      const group=tab.dataset.group;
      const count=group==='all'?users.length:(groupCounts[group]||0);
      tab.textContent=`${tab.dataset.label} (${count.toLocaleString('en-US')})`;
      tab.addEventListener('click',()=>{
        activeGroup=group;
        tabs.forEach(item=>item.classList.toggle('active',item===tab));
        render();
      });
    });
    const statValues=document.querySelectorAll('.stat-row .stat-val');
    [users.length,groupCounts.elderly,groupCounts.children,groupCounts.disability].forEach((count,index)=>{
      if(statValues[index]) statValues[index].textContent=count.toLocaleString('en-US');
    });

    function render() {
      const query=normalizeFilterValue(searchInput?.value);
      const selectedWard=wardSelect?.value||'';
      const selectedPriority=(prioritySelect?.value||'').toUpperCase();
      const filtered=users.filter(user=>{
        if(!matchesCitizenGroup(user,activeGroup)) return false;
        if(selectedWard&&String(user.ward||'').trim()!==selectedWard) return false;
        if(selectedPriority&&!(requestsByUser.get(String(user.id))||new Set()).has(selectedPriority)) return false;
        if(query){
          const searchable=[user.fullName,user.citizenId,user.phone,user.email,user.address,user.ward,user.beneficiaryGroup,user.situationDescription]
            .map(normalizeFilterValue).join(' ');
          if(!searchable.includes(query)) return false;
        }
        return true;
      });
      body.innerHTML=filtered.map(user=>{
        const initials=(user.fullName||'?').trim().split(/\s+/).filter(Boolean).slice(-2).map(part=>part[0]).join('').toLocaleUpperCase('vi-VN')||'?';
        const isActive=String(user.status).toUpperCase()==='ACTIVE';
        return `<tr>
          <td><div class="avatar avatar-sm avatar-brown">${esc(initials)}</div></td>
          <td style="font-weight:700;">${esc(user.fullName||'Chưa cập nhật')}<div style="font-size:0.75rem;color:var(--text-light);">📱 ${esc(user.phone||'Chưa có SĐT')}</div></td>
          <td style="font-family:monospace;font-size:0.82rem;">${esc(user.citizenId||'—')}</td>
          <td>${esc(user.beneficiaryGroup||'Chưa phân nhóm')}</td>
          <td>${esc(user.ward||'—')}</td>
          <td>${esc(user.situationDescription||'Chưa khai báo')}</td>
          <td><span class="badge ${isActive?'badge-approved':'badge-pending'}">${isActive?'Đang hoạt động':esc(user.status||'Không rõ')}</span></td>
          <td>—</td>
        </tr>`;
      }).join('')||'<tr><td colspan="8">Không tìm thấy người dân phù hợp bộ lọc.</td></tr>';
    }
    searchInput?.addEventListener('input',render);
    wardSelect?.addEventListener('change',render);
    prioritySelect?.addEventListener('change',render);
    document.getElementById('citizenSearchButton')?.addEventListener('click',render);
    render();
  }

  function accountInitial(fullName) {
    const name=String(fullName||'').replace(/\s*\([^)]*\)\s*/g,' ').trim();
    const words=name.split(/\s+/).filter(Boolean);
    return (words.at(-1)||'?').charAt(0).toLocaleUpperCase('vi-VN');
  }
  function bindAccountFilters(users,roles,availableRoles) {
    const body=document.querySelector('#accountsTable tbody');
    if(!body) return;
    const searchInput=document.getElementById('accountSearch');
    const roleSelect=document.getElementById('accountRoleFilter');
    const statusSelect=document.getElementById('accountStatusFilter');
    const createRoleSelect=document.getElementById('createAccountRole');
    const createForm=document.getElementById('createAccountForm');
    if(createRoleSelect)createRoleSelect.innerHTML=availableRoles.map(role=>`<option value="${role.id}">${esc(role.roleName)}${role.description?` — ${esc(role.description)}`:''}</option>`).join('');
    if(createForm)createForm.onsubmit=async event=>{
      event.preventDefault();
      const formData=new FormData(createForm);
      try{
        const created=await api.post('/users',{fullName:String(formData.get('fullName')).trim(),email:String(formData.get('email')).trim(),
          phone:String(formData.get('phone')||'').trim(),password:String(formData.get('password')),status:'ACTIVE'});
        try{await api.post('/user-roles',{user:{id:created.id},role:{id:Number(formData.get('roleId'))}});}
        catch(error){await api.del(`/users/${created.id}`).catch(()=>{});throw error;}
        createForm.reset();
        const modal=document.getElementById('addUserModal');if(modal)modal.style.display='none';
        await loadUsers(false);
      }catch(error){api.error(error);}
    };
    const statValues=document.querySelectorAll('.stat-row .stat-val');
    const statuses=users.map(user=>String(user.status||'').toUpperCase());
    const pendingStatuses=new Set(['PENDING','PENDING_VERIFICATION','UNVERIFIED']);
    [
      users.length,
      statuses.filter(status=>status==='ACTIVE').length,
      statuses.filter(status=>status==='LOCKED').length,
      statuses.filter(status=>pendingStatuses.has(status)).length
    ].forEach((count,index)=>{if(statValues[index])statValues[index].textContent=count.toLocaleString('en-US');});

    function render() {
      const query=normalizeFilterValue(searchInput?.value);
      const selectedRole=(roleSelect?.value||'').toUpperCase();
      const selectedStatus=(statusSelect?.value||'').toUpperCase();
      const filtered=users.filter(user=>{
        const userRoles=roles.get(user.id)||[];
        if(selectedRole&&!userRoles.some(role=>String(role).toUpperCase()===selectedRole)) return false;
        if(selectedStatus&&String(user.status||'').toUpperCase()!==selectedStatus) return false;
        if(query){
          const searchable=[user.fullName,user.citizenId,user.phone,...userRoles].map(normalizeFilterValue).join(' ');
          if(!searchable.includes(query)) return false;
        }
        return true;
      });
      body.innerHTML=filtered.map(user=>{
        const userStatus=String(user.status||'');
        const actionStatus=userStatus.toUpperCase()==='ACTIVE'?'LOCKED':'ACTIVE';
        return `<tr><td><div class="avatar avatar-sm avatar-dark">${esc(accountInitial(user.fullName))}</div></td><td style="white-space:normal;word-break:break-word;min-width:120px;max-width:200px;">${esc(user.fullName)}</td><td>${esc(user.citizenId)}</td><td>${esc(user.phone)}</td>
          <td>${(roles.get(user.id)||[]).map(esc).join(', ')}</td><td>${esc(userStatus)}</td><td>${dte(user.createdAt)}</td><td><button class="btn btn-ghost btn-sm" onclick="toggleUserStatus(${user.id},'${actionStatus}')">${actionStatus==='INACTIVE'?'Khóa':'Mở khóa'}</button></td></tr>`;
      }).join('')||'<tr><td colspan="8">Không tìm thấy tài khoản phù hợp bộ lọc.</td></tr>';
    }
    searchInput?.addEventListener('input',render);
    roleSelect?.addEventListener('change',render);
    statusSelect?.addEventListener('change',render);
    document.getElementById('accountFilterButton')?.addEventListener('click',render);
    render();
  }

  async function loadUsers(citizensOnly=false) {
    const [users,assignments,availableRoles]=await Promise.all([api.get('/users'),api.get('/user-roles'),citizensOnly?Promise.resolve([]):api.get('/roles')]);
    const roles=new Map(); assignments.forEach(row=>{const id=row.user?.id;if(!roles.has(id))roles.set(id,[]);roles.get(id).push(row.role?.roleName);});
    const body=document.querySelector('.admin-table tbody'); if(!body) return;
    const list=users.filter(u=>!citizensOnly || (roles.get(u.id)||[]).includes('USER'));
    if(citizensOnly){
      let requests=[];
      try{requests=await api.get('/assistance-requests');}catch(_){/* Citizen text, area and group filters still work if request priorities are unavailable. */}
      bindCitizenFilters(list,requests);
      return;
    }
    bindAccountFilters(list,roles,availableRoles);
  }
  window.toggleUserStatus=async(id,status)=>{try{const u=await api.get(`/users/${id}`);await api.put(`/users/${id}`,{...u,status});location.reload();}catch(e){api.error(e);}};

  async function loadResources() {
    const [items,inventory,donations,points]=await Promise.all([
      api.get('/items'),api.get('/inventory/summary'),api.get('/donations'),api.get('/collection-points')
    ]);
    const body=document.querySelector('#adminResourcesTable tbody'); if(!body)return;
    const inventoryByItem=new Map(inventory.map(row=>[String(row.itemId),row]));
    const providersById=new Map();
    donations.forEach(donation=>{
      const donor=donation.donor;
      if(!donor)return;
      const id=String(donor.id??donor.email??donor.fullName);
      if(!providersById.has(id))providersById.set(id,{donor,count:0});
      providersById.get(id).count++;
    });
    const providers=[...providersById.values()];
    const stockFor=item=>inventoryByItem.get(String(item.id))||{};
    const outOfStock=items.filter(item=>Number(stockFor(item).available||0)<=0).length;
    const statValues=document.querySelectorAll('.stat-row .stat-val');
    [items.length,providers.length,points.length,outOfStock].forEach((count,index)=>{if(statValues[index])statValues[index].textContent=count.toLocaleString('en-US');});
    const tabs=Array.from(document.querySelectorAll('[data-resource-view]'));
    const header=document.querySelector('#adminResourcesTable thead');
    const caption=document.querySelector('#adminResourcesTable caption');
    const cardTitle=document.querySelector('.card-header .card-title');
    function render(view='inventory'){
      tabs.forEach(tab=>tab.classList.toggle('active',tab.dataset.resourceView===view));
      if(view==='providers'){
        header.innerHTML='<tr><th>Đơn vị / Người cung cấp</th><th>Email</th><th>Số điện thoại</th><th>Số lượt quyên góp</th></tr>';
        caption.textContent='Danh sách đơn vị đã có quyên góp trong hệ thống';
        if(cardTitle)cardTitle.textContent='🏢 Đơn vị cung cấp từ dữ liệu quyên góp';
        body.innerHTML=providers.map(({donor,count})=>`<tr><td>${esc(donor.fullName||'—')}</td><td>${esc(donor.email||'—')}</td><td>${esc(donor.phone||'—')}</td><td>${count}</td></tr>`).join('')||'<tr><td colspan="4">Chưa có đơn vị cung cấp trong dữ liệu.</td></tr>';
        return;
      }
      if(view==='points'){
        header.innerHTML='<tr><th>Tên điểm</th><th>Địa chỉ</th><th>Người liên hệ</th><th>Số điện thoại</th><th>Trạng thái</th></tr>';
        caption.textContent='Danh sách điểm tập kết lấy từ hệ thống';
        if(cardTitle)cardTitle.textContent='📍 Điểm tập kết';
        body.innerHTML=points.map(point=>`<tr><td>${esc(point.name)}</td><td>${esc(point.address)}</td><td>${esc(point.contactPerson||'—')}</td><td>${esc(point.phone||'—')}</td><td>${esc(point.status||'—')}</td></tr>`).join('')||'<tr><td colspan="5">Chưa có điểm tập kết.</td></tr>';
        return;
      }
      header.innerHTML='<tr><th>Tên vật tư</th><th>Danh mục</th><th>Đơn vị</th><th>Đã tiếp nhận</th><th>Đã giữ chỗ</th><th>Còn khả dụng</th><th>Tỷ lệ còn</th><th>Thao tác</th></tr>';
      caption.textContent='Bảng tồn kho và khả năng đáp ứng vật tư';
      if(cardTitle)cardTitle.textContent='📦 Chi tiết tồn kho và tình trạng khả dụng';
      body.innerHTML=items.map(item=>{
        const stock=stockFor(item);
        const accepted=Number(stock.accepted||0),reserved=Number(stock.reserved||0),available=Number(stock.available||0);
        const percent=accepted?Math.round(available/accepted*100):0;
        return `<tr><td>${esc(item.name)}</td><td>${esc(item.category?.name||stock.category||'—')}</td><td>${esc(item.unit?.symbol||stock.unit||'—')}</td>
          <td>${accepted}</td><td>${reserved}</td><td>${available}</td><td>${percent}%</td><td><a class="btn btn-ghost btn-sm" href="allocations.html">Điều phối</a></td></tr>`;
      }).join('')||'<tr><td colspan="8">Chưa có vật tư trong danh mục.</td></tr>';
    }
    tabs.forEach(tab=>tab.addEventListener('click',()=>render(tab.dataset.resourceView)));
    render();
  }

  async function loadServices() {
    const [rows,registrations,items,inventory,categories,units]=await Promise.all([
      api.get('/admin/services'),api.get('/admin/service-registrations'),api.get('/items'),api.get('/inventory/summary'),api.get('/categories'),api.get('/units')
    ]);
    const body=document.querySelector('#adminServicesTable tbody'); if(!body)return;
    body.innerHTML=rows.map(s=>`<tr><td>${esc(s.name)}</td><td>${esc(s.serviceType)}</td><td>${esc(s.description)}</td><td>${esc(s.serviceArea||'—')}</td><td>${esc(s.status)}</td><td>${esc(s.contactPhone||'—')}</td></tr>`).join('')||'<tr><td colspan="6">Chưa có dịch vụ.</td></tr>';
    const cards=document.getElementById('adminServiceCards');
    if(cards) cards.innerHTML=rows.map(service=>`<div class="card"><div class="card-body" style="text-align:center;">
      <div style="font-size:2.2rem;margin-bottom:8px;">🏛</div><div style="font-weight:800;font-size:1rem;">${esc(service.name)}</div>
      <div style="font-size:.8rem;color:var(--text-light);margin-top:4px;">${esc(service.description||'')}</div><div class="divider-gold"></div>
      <div style="display:flex;justify-content:space-between;font-size:.82rem;"><span>${esc(service.serviceArea||'Toàn địa bàn')}</span><span class="badge">${esc(service.status||'—')}</span></div>
    </div></div>`).join('')||'<div class="card"><div class="card-body">Chưa có dịch vụ đang hoạt động.</div></div>';
    const equipmentBody=document.querySelector('#adminEquipmentTable tbody');
    const inventoryByItem=new Map(inventory.map(row=>[String(row.itemId),row]));
    const equipment=items.filter(item=>/thiết bị|cứu nạn|y tế/i.test(item.category?.name||''));
    if(equipmentBody) equipmentBody.innerHTML=equipment.map(item=>{
      const stock=inventoryByItem.get(String(item.id))||{};
      return `<tr><td>${esc(item.name)}</td><td>${esc(item.category?.name||'—')}</td><td>${Number(stock.accepted||0)}</td><td>${Number(stock.available||0)}</td><td>${esc(item.unit?.symbol||'—')}</td></tr>`;
    }).join('')||'<tr><td colspan="5">Chưa có vật tư trong nhóm thiết bị / cứu nạn.</td></tr>';
    let section=document.getElementById('admin-service-registrations');
    if(!section){section=document.createElement('section');section.id='admin-service-registrations';body.closest('.card').insertAdjacentElement('afterend',section);}
    section.innerHTML=`<div class="card" style="margin-top:18px;"><div class="card-body" style="overflow-x:auto;"><h2 style="margin-bottom:14px;">Đăng ký dịch vụ của người dân</h2><table class="admin-table"><caption>Bảng đăng ký sử dụng dịch vụ</caption><thead><tr><th>Người dân</th><th>Dịch vụ</th><th>Nội dung</th><th>Ngày đăng ký</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>${registrations.map(r=>`<tr><td>${esc(r.user?.fullName)}</td><td>${esc(r.service?.name)}</td><td>${esc(r.requestDetails)}</td><td>${dte(r.createdAt)}</td><td>${esc(r.status)}</td><td>${r.status==='PENDING'?`<button onclick="setServiceRegistrationStatus(${r.id},'APPROVED')">Duyệt</button> <button onclick="setServiceRegistrationStatus(${r.id},'REJECTED')">Từ chối</button>`:r.status==='APPROVED'?`<button onclick="setServiceRegistrationStatus(${r.id},'COMPLETED')">Hoàn tất</button>`:''}</td></tr>`).join('')||'<tr><td colspan="6">Chưa có đăng ký dịch vụ.</td></tr>'}</tbody></table></div></div>`;
    window.setServiceRegistrationStatus=async(id,status)=>{try{await api.request(`/admin/service-registrations/${id}/status?value=${status}`,{method:'PUT'});await loadServices();}catch(error){api.error(error);}};
    const form=document.getElementById('adminServiceForm'); if(!form)return;
    const typeSelect=document.getElementById('adminResourceType');
    const serviceFields=document.getElementById('adminServiceFields');
    const equipmentFields=document.getElementById('adminEquipmentFields');
    const categorySelect=form.querySelector('[name="categoryId"]');
    const unitSelect=form.querySelector('[name="unitId"]');
    categorySelect.innerHTML=categories.map(category=>`<option value="${category.id}">${esc(category.name)}</option>`).join('');
    unitSelect.innerHTML=units.map(unit=>`<option value="${unit.id}">${esc(unit.unitName)} (${esc(unit.symbol)})</option>`).join('');
    typeSelect.onchange=()=>{
      const equipment=typeSelect.value==='EQUIPMENT';
      serviceFields.hidden=equipment;
      equipmentFields.hidden=!equipment;
    };
    form.onsubmit=async event=>{
      event.preventDefault();
      const data=new FormData(form);
      try{
        if(typeSelect.value==='SERVICE'){
          await api.post('/admin/services',{serviceType:'SOCIAL_SERVICE',name:String(data.get('name')).trim(),description:String(data.get('description')||''),serviceArea:String(data.get('serviceArea')||'').trim(),contactPhone:String(data.get('contactPhone')||'').trim(),status:'ACTIVE'});
        }else{
          await api.post('/items',{name:String(data.get('name')).trim(),description:String(data.get('description')||''),category:{id:Number(data.get('categoryId'))},unit:{id:Number(data.get('unitId'))},status:'ACTIVE'});
        }
        document.getElementById('addModal').style.display='none';form.reset();typeSelect.dispatchEvent(new Event('change'));await loadServices();
      }catch(error){api.error(error);}
    };
    typeSelect.dispatchEvent(new Event('change'));
  }

  async function loadDashboard() {
    const [summary,requests,allocations,donations,inventory,points,currentUser]=await Promise.all([
      api.get('/dashboard/summary'),api.get('/assistance-requests'),api.get('/allocations'),api.get('/donations'),
      api.get('/inventory/summary'),api.get('/collection-points'),api.get('/auth/me')
    ]);
    const closed=new Set(['COMPLETED','REJECTED','CANCELLED']);
    const activeRequests=requests.filter(row=>!closed.has(String(row.status).toUpperCase()));
    const stats={
      adminRegisteredCount:summary.beneficiaries||0,
      adminRequestsCount:activeRequests.length,
      adminAllocationsCount:allocations.length,
      adminUrgentCount:requests.filter(row=>String(row.priority).toUpperCase()==='URGENT').length
    };
    Object.entries(stats).forEach(([id,value])=>{const node=document.getElementById(id);if(node)node.textContent=Number(value).toLocaleString('en-US');});
    const priorityCounts={URGENT:0,HIGH:0,NORMAL:0,LOW:0};
    requests.forEach(row=>{const priority=String(row.priority||'').toUpperCase();if(Object.hasOwn(priorityCounts,priority))priorityCounts[priority]++;});
    const maxPriority=Math.max(1,...Object.values(priorityCounts));
    const priorityTargets={URGENT:'adminUrgentStat',HIGH:'adminHighStat',NORMAL:'adminNormalStat',LOW:'adminLowStat'};
    Object.entries(priorityCounts).forEach(([priority,count])=>{
      const node=document.getElementById(priorityTargets[priority]);if(node)node.textContent=count.toLocaleString('en-US');
      const bar=document.querySelector(`[data-priority-bar="${priority}"]`);if(bar)bar.style.width=`${Math.round(count/maxPriority*100)}%`;
    });
    const collectionPoints=document.getElementById('adminCollectionPointsList');
    if(collectionPoints)collectionPoints.innerHTML=points.map(point=>`<div style="padding:12px;border:1px solid var(--beige-dark);border-radius:var(--radius-sm);background:#fff;">
      <div style="font-weight:700;">📍 ${esc(point.name)}</div><div style="font-size:.8rem;color:var(--text-light);margin-top:4px;">${esc(point.address)}</div>
      <div style="font-size:.78rem;margin-top:6px;">${esc(point.contactPerson||'')} ${esc(point.phone||'')} · ${esc(point.status||'')}</div></div>`).join('')||'<div>Chưa có điểm tập kết.</div>';
    const recent=document.getElementById('adminRecentActivityList');
    if(recent){
      const latest=[...requests].sort((a,b)=>new Date(b.createdAt||0)-new Date(a.createdAt||0)).slice(0,6);
      recent.innerHTML=latest.map(row=>`<div style="display:flex;gap:10px;align-items:flex-start;padding:10px 0;border-bottom:1px solid var(--beige-dark);">
        <div class="avatar avatar-sm avatar-dark">${esc(String(row.priority||'').toUpperCase()==='URGENT'?'!':'R')}</div><div style="flex:1;">
        <div style="font-weight:600;font-size:.84rem;">${esc(row.requestCode)} — ${esc(row.title)}</div>
        <div style="font-size:.75rem;color:var(--text-light);">${esc(row.deliveryAddress||'—')} · ${dte(row.createdAt)||'—'}</div></div>
        <span class="badge">${esc(row.priority||row.status||'—')}</span></div>`).join('')||'<div>Chưa có yêu cầu hỗ trợ.</div>';
    }
    const providers=new Set(donations.map(row=>row.donor?.id??row.donor?.email).filter(value=>value!=null));
    const resourceStats={adminStockItemCount:inventory.length,adminPointCount:points.length,adminProviderCount:providers.size};
    Object.entries(resourceStats).forEach(([id,value])=>{const node=document.getElementById(id);if(node)node.textContent=Number(value).toLocaleString('en-US');});
    const user=currentUser?.user||currentUser;
    const name=user?.fullName||user?.email;
    if(name){
      const sidebar=document.getElementById('adminSidebarName');if(sidebar)sidebar.textContent=name;
      const welcome=document.getElementById('adminWelcomeName');if(welcome)welcome.textContent=name;
      const avatar=document.getElementById('adminSidebarAvatar');if(avatar){const words=String(name).trim().split(/\s+/).filter(Boolean);avatar.textContent=(words.at(-1)||'?').charAt(0).toLocaleUpperCase('vi-VN');}
    }
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
    const [rows,items,shipmentItems]=await Promise.all([api.get('/allocations'),api.get('/allocation-items'),api.get('/shipment-items')]);
    const body=document.querySelector('.admin-table tbody');if(!body)return;
    body.innerHTML=rows.map(a=>{
      const allocationLines=items.filter(i=>i.allocation?.id===a.id);
      const details=allocationLines.map(i=>`${i.donationItem?.item?.name||i.donationItem?.itemName||'Vật tư'}: ${i.quantity}`).join(', ')||'Chưa có vật tư';
      const sources=[...new Set(allocationLines.map(i=>i.donationItem?.donation?.donationCode).filter(Boolean))].join(', ')||'—';
      const linkedShipments=shipmentItems.filter(i=>i.allocationItem?.allocation?.id===a.id).map(i=>i.shipment).filter(Boolean);
      const trackingCodes=[...new Set(linkedShipments.map(s=>s.trackingCode).filter(Boolean))].join(', ')||'Chưa có vận đơn';
      const shipmentStatuses=[...new Set(linkedShipments.map(s=>s.status).filter(Boolean))];
      const status=shipmentStatuses.join(', ')||a.status||'—';
      const updatedAt=linkedShipments.map(s=>s.createdAt).filter(Boolean).sort().at(-1)||a.allocatedAt;
      return `<tr><td>${esc(a.allocationCode)}</td><td>${esc(a.request?.requestCode||a.request?.title)}</td><td>${esc(details)}</td>
        <td>${esc(sources)}</td><td>${esc(trackingCodes)}</td><td>${esc(status)}</td><td>${dte(updatedAt)||'—'}</td></tr>`;
    }).join('')||'<tr><td colspan="7">Chưa có bản ghi phân bổ.</td></tr>';
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
