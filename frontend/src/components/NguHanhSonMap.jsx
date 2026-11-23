// NguHanhSonMap.jsx - React Component for Relief Coordination in Ngu Hanh Son District, Da Nang
import React, { useState, useEffect, useRef } from 'react';

export const NGU_HANH_SON_CENTER = [16.0050, 108.2496];
export const NGU_HANH_SON_BOUNDS = [
  [15.955, 108.205], // Southwest
  [16.060, 108.280]  // Northeast
];

export const RELIEF_HUBS = [
  {
    id: 'hub-1',
    name: 'UBND Quận Ngũ Hành Sơn — Trung Tâm Điều Phối Khẩn Cấp',
    ward: 'khuemy',
    wardName: 'Phường Khuê Mỹ',
    address: '486 Lê Văn Hiến, P. Khuê Mỹ, Q. Ngũ Hành Sơn, TP. Đà Nẵng',
    coords: [16.0142, 108.2435],
    type: 'command',
    typeLabel: 'Sở Chỉ Huy An Sinh',
    icon: '🏛️',
    status: 'Đang trực ban 24/7',
    stock: '15.8 tấn gạo & thực phẩm khô · 2.400 bình nước · 600 phao cứu sinh',
    hotline: '0236.3847.234',
    capacity: 92,
    badgeColor: '#D4AF37'
  },
  {
    id: 'hub-2',
    name: 'Điểm Tập Kết Lương Thực & An Sinh Di Tích K20',
    ward: 'khuemy',
    wardName: 'Phường Khuê Mỹ',
    address: 'Khu Di tích Cách mạng K20, P. Khuê Mỹ, Q. Ngũ Hành Sơn, TP. Đà Nẵng',
    coords: [16.0225, 108.2380],
    type: 'supplies',
    typeLabel: 'Kho Lương Thực & Nhu Yếu Phẩm',
    icon: '📦',
    status: 'Sẵn sàng cấp phát',
    stock: '8.5 tấn gạo dự trữ · 1.200 thùng mì tôm · 300 đèn pin sạc dự phòng',
    hotline: '0905.123.456',
    capacity: 78,
    badgeColor: '#2A5FAA'
  },
  {
    id: 'hub-3',
    name: 'Trạm Y Tế & Cứu Trợ Lưu Động Phường Mỹ An',
    ward: 'myan',
    wardName: 'Phường Mỹ An',
    address: 'Đường Bà Huyện Thanh Quan, P. Mỹ An, Q. Ngũ Hành Sơn, TP. Đà Nẵng',
    coords: [16.0405, 108.2415],
    type: 'medical',
    typeLabel: 'Trạm Cứu Trợ Y Tế',
    icon: '🏥',
    status: 'Trực cấp cứu & cấp phát thuốc',
    stock: '450 túi thuốc chuẩn Bộ Y Tế · 20 máy thở oxy mini · 1.500 bông băng sát trùng',
    hotline: '0236.3950.115',
    capacity: 85,
    badgeColor: '#2E6B28'
  },
  {
    id: 'hub-4',
    name: 'Điểm Tránh Trú Bão Lũ & Sơ Tán Ven Sông Cổ Cò — Hòa Quý',
    ward: 'hoaquy',
    wardName: 'Phường Hòa Quý',
    address: 'Trường THCS Huỳnh Bá Chánh / Nhà Văn Hóa P. Hòa Quý, Q. Ngũ Hành Sơn',
    coords: [15.9865, 108.2285],
    type: 'shelter',
    typeLabel: 'Điểm Tránh Trú & Sơ Tán',
    icon: '⛺',
    status: 'Vùng trũng xung yếu — Đã sẵn sàng',
    stock: 'Sức chứa 350 người · 800 chăn ấm · 4 ca-nô xung kích cứu hộ ngập lụt',
    hotline: '0913.456.789',
    capacity: 65,
    badgeColor: '#B02A3A'
  },
  {
    id: 'hub-5',
    name: 'Trạm Cứu Hộ Ven Biển & Làng Đá Non Nước — Hòa Hải',
    ward: 'hoahai',
    wardName: 'Phường Hòa Hải',
    address: 'Đường Trường Sa / Làng nghề Non Nước, P. Hòa Hải, Q. Ngũ Hành Sơn',
    coords: [15.9780, 108.2560],
    type: 'rescue',
    typeLabel: 'Đội Phản Ứng Nhanh & Ca-nô',
    icon: '🚤',
    status: 'Túc trực ca-nô tuần tra',
    stock: '3 ca-nô cứu hộ tốc độ cao · 500 áo phao · 2 máy phát điện công suất lớn',
    hotline: '0236.3836.114',
    capacity: 95,
    badgeColor: '#00838F'
  }
];

export default function NguHanhSonMap() {
  const [selectedWard, setSelectedWard] = useState('all');
  const [selectedType, setSelectedType] = useState('all');
  const [activeHub, setActiveHub] = useState(RELIEF_HUBS[0]);
  const mapRef = useRef(null);
  const leafletInstance = useRef(null);
  const markersRef = useRef([]);

  const filteredHubs = RELIEF_HUBS.filter(hub => {
    const matchWard = selectedWard === 'all' || hub.ward === selectedWard;
    const matchType = selectedType === 'all' || hub.type === selectedType;
    return matchWard && matchType;
  });

  useEffect(() => {
    if (typeof window === 'undefined' || !window.L || !mapRef.current) return;

    if (!leafletInstance.current) {
      const map = window.L.map(mapRef.current, {
        center: NGU_HANH_SON_CENTER,
        zoom: 13,
        minZoom: 12,
        maxZoom: 17,
        maxBounds: NGU_HANH_SON_BOUNDS,
        maxBoundsViscosity: 1.0 // Strictly locked to Ngu Hanh Son
      });

      window.L.tileLayer('https://mt{s}.google.com/vt/lyrs=m&x={x}&y={y}&z={z}', {
        attribution: '&copy; Google Maps | Quận Ngũ Hành Sơn, TP. Đà Nẵng',
        subdomains: ['0', '1', '2', '3'],
        maxZoom: 20
      }).addTo(map);

      // Draw subtle boundary box for Ngu Hanh Son
      window.L.rectangle(NGU_HANH_SON_BOUNDS, {
        color: '#D4AF37',
        weight: 1.5,
        fillColor: '#153658',
        fillOpacity: 0.05,
        dashArray: '4, 4'
      }).addTo(map);

      leafletInstance.current = map;
    }

    const map = leafletInstance.current;

    // Clear old markers
    markersRef.current.forEach(m => map.removeLayer(m));
    markersRef.current = [];

    // Add markers for filtered hubs
    filteredHubs.forEach(hub => {
      const customIcon = window.L.divIcon({
        className: 'custom-hub-marker',
        html: `
          <div style="background:#081424; border:2px solid ${hub.badgeColor}; color:#fff; border-radius:50%; width:38px; height:38px; display:flex; align-items:center; justify-content:center; box-shadow:0 4px 12px rgba(0,0,0,0.35); font-size:1.15rem; cursor:pointer;">
            ${hub.icon}
          </div>
        `,
        iconSize: [38, 38],
        iconAnchor: [19, 19]
      });

      const marker = window.L.marker(hub.coords, { icon: customIcon }).addTo(map);
      marker.bindPopup(`
        <div style="font-family:inherit; min-width:210px; color:#1F2421;">
          <div style="font-weight:700; color:#081424; font-size:0.92rem; margin-bottom:4px;">${hub.name}</div>
          <div style="font-size:0.75rem; color:#6B705C; margin-bottom:6px;">📍 ${hub.wardName}</div>
          <div style="background:#F4F1DE; padding:6px 8px; border-radius:6px; font-size:0.78rem; line-height:1.4; margin-bottom:8px;">
            <strong>Nguồn lực:</strong> ${hub.stock}
          </div>
          <div style="display:flex; justify-content:space-between; align-items:center; font-size:0.8rem;">
            <span style="color:#2E6B28; font-weight:700;">📞 ${hub.hotline}</span>
            <span style="background:${hub.badgeColor}; color:#fff; padding:2px 6px; border-radius:4px; font-size:0.7rem;">${hub.typeLabel}</span>
          </div>
        </div>
      `);

      marker.on('click', () => {
        setActiveHub(hub);
      });

      markersRef.current.push(marker);
    });
  }, [filteredHubs]);

  const handleSelectHub = (hub) => {
    setActiveHub(hub);
    if (leafletInstance.current) {
      leafletInstance.current.setView(hub.coords, 14, { animate: true });
    }
  };

  return (
    <div className="nhs-map-component" style={{
      background: 'linear-gradient(180deg, rgba(28, 62, 98, 0.88) 0%, rgba(46, 88, 126, 0.78) 100%)',
      backdropFilter: 'blur(20px)',
      WebkitBackdropFilter: 'blur(20px)',
      borderRadius: '18px',
      border: '1.5px solid rgba(212,175,55,0.45)',
      boxShadow: '0 20px 48px rgba(8,20,36,0.35)',
      padding: '24px',
      color: '#F9F3E8',
      fontFamily: 'inherit'
    }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px', marginBottom: '20px' }}>
        <div>
          <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', background: 'rgba(212,175,55,0.15)', border: '1px solid rgba(212,175,55,0.4)', padding: '4px 12px', borderRadius: '30px', fontSize: '0.8rem', color: '#D4AF37', fontWeight: '700', marginBottom: '8px' }}>
            <span>🗺️</span> ĐỊA BÀN TRỌNG ĐIỂM: QUẬN NGŨ HÀNH SƠN, TP. ĐÀ NẴNG
          </div>
          <h3 style={{ fontSize: '1.4rem', fontWeight: '800', margin: '4px 0', color: '#F3E5AB' }}>
            Bản Đồ Số Điều Phối An Sinh Thực Địa
          </h3>
          <p style={{ fontSize: '0.86rem', color: 'rgba(249,243,232,0.75)', margin: 0 }}>
            Chỉ hiển thị phạm vi hành chính Quận Ngũ Hành Sơn (4 Phường: Mỹ An · Khuê Mỹ · Hòa Quý · Hòa Hải)
          </p>
        </div>

        {/* Quick stat */}
        <div style={{ display: 'flex', gap: '12px' }}>
          <div style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(212,175,55,0.25)', borderRadius: '10px', padding: '10px 16px', textAlign: 'center' }}>
            <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#D4AF37' }}>4 Phường</div>
            <div style={{ fontSize: '0.72rem', color: 'rgba(249,243,232,0.7)' }}>Phạm vi kết nối</div>
          </div>
          <div style={{ background: 'rgba(255,255,255,0.06)', border: '1px solid rgba(74,122,66,0.4)', borderRadius: '10px', padding: '10px 16px', textAlign: 'center' }}>
            <div style={{ fontSize: '1.25rem', fontWeight: '800', color: '#4A7A42' }}>5 Điểm Hub</div>
            <div style={{ fontSize: '0.72rem', color: 'rgba(249,243,232,0.7)' }}>Trực ban an sinh</div>
          </div>
        </div>
      </div>

      {/* Ward Filter Bar */}
      <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '12px', marginBottom: '16px' }}>
        {[
          { id: 'all', label: 'Tất cả Quận Ngũ Hành Sơn' },
          { id: 'myan', label: 'Phường Mỹ An' },
          { id: 'khuemy', label: 'Phường Khuê Mỹ' },
          { id: 'hoaquy', label: 'Phường Hòa Quý (Ven sông)' },
          { id: 'hoahai', label: 'Phường Hòa Hải (Ven biển)' }
        ].map(ward => (
          <button
            key={ward.id}
            onClick={() => setSelectedWard(ward.id)}
            style={{
              padding: '8px 16px',
              borderRadius: '24px',
              border: selectedWard === ward.id ? '1.5px solid #D4AF37' : '1px solid rgba(255,255,255,0.15)',
              background: selectedWard === ward.id ? 'linear-gradient(135deg, #D4AF37, #B58A28)' : 'rgba(255,255,255,0.06)',
              color: selectedWard === ward.id ? '#081424' : '#F9F3E8',
              fontWeight: '700',
              fontSize: '0.82rem',
              cursor: 'pointer',
              whiteSpace: 'nowrap',
              transition: 'all 0.2s ease'
            }}
          >
            {ward.label}
          </button>
        ))}
      </div>

      {/* Main Grid: Map + Side Hub Panel */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.8fr) minmax(0, 1fr)', gap: '16px' }}>
        {/* Leaflet Map Box */}
        <div style={{ position: 'relative' }}>
          <div
            ref={mapRef}
            id="nhs-leaflet-map"
            style={{
              height: '460px',
              borderRadius: '12px',
              border: '1px solid rgba(212,175,55,0.3)',
              overflow: 'hidden',
              boxShadow: 'inset 0 0 20px rgba(0,0,0,0.5)'
            }}
          />
          <div style={{
            position: 'absolute',
            bottom: '12px',
            left: '12px',
            zIndex: 400,
            background: 'rgba(8,20,36,0.88)',
            border: '1px solid rgba(212,175,55,0.4)',
            borderRadius: '8px',
            padding: '6px 12px',
            fontSize: '0.74rem',
            color: '#F9F3E8'
          }}>
            🔒 Phạm vi khoá: Quận Ngũ Hành Sơn · 15.95°N - 16.06°N
          </div>
        </div>

        {/* Selected Hub Detail Panel */}
        <div style={{
          background: 'rgba(255,255,255,0.05)',
          border: '1px solid rgba(212,175,55,0.25)',
          borderRadius: '12px',
          padding: '18px',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'space-between'
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '12px' }}>
              <span style={{
                background: activeHub.badgeColor,
                color: '#fff',
                fontSize: '0.72rem',
                fontWeight: '700',
                padding: '4px 10px',
                borderRadius: '6px'
              }}>
                {activeHub.icon} {activeHub.typeLabel}
              </span>
              <span style={{ fontSize: '0.75rem', color: '#4A7A42', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '4px' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#4A7A42', display: 'inline-block' }}></span>
                {activeHub.status}
              </span>
            </div>

            <h4 style={{ fontSize: '1.05rem', fontWeight: '800', color: '#F3E5AB', marginBottom: '8px', lineHeight: 1.35 }}>
              {activeHub.name}
            </h4>

            <div style={{ fontSize: '0.8rem', color: 'rgba(249,243,232,0.8)', marginBottom: '12px', display: 'flex', alignItems: 'flex-start', gap: '6px' }}>
              <span>📍</span> <span>{activeHub.address}</span>
            </div>

            <div style={{
              background: 'rgba(8,20,36,0.6)',
              border: '1px solid rgba(255,255,255,0.08)',
              borderRadius: '8px',
              padding: '12px',
              marginBottom: '14px'
            }}>
              <div style={{ fontSize: '0.76rem', fontWeight: '700', color: '#D4AF37', textTransform: 'uppercase', marginBottom: '6px' }}>
                📦 Nguồn lực sẵn sàng tại trạm:
              </div>
              <div style={{ fontSize: '0.82rem', lineHeight: '1.45', color: '#FFFFFF' }}>
                {activeHub.stock}
              </div>
            </div>

            <div style={{ marginBottom: '12px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.76rem', marginBottom: '4px', color: 'rgba(249,243,232,0.8)' }}>
                <span>Mức dự trữ hiện thời:</span>
                <span style={{ fontWeight: '700', color: '#D4AF37' }}>{activeHub.capacity}% khả dụng</span>
              </div>
              <div style={{ width: '100%', height: '6px', background: 'rgba(255,255,255,0.1)', borderRadius: '3px', overflow: 'hidden' }}>
                <div style={{ width: `${activeHub.capacity}%`, height: '100%', background: 'linear-gradient(90deg, #4A7A42, #D4AF37)', borderRadius: '3px' }}></div>
              </div>
            </div>
          </div>

          <div>
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '10px 12px',
              background: 'rgba(74,122,66,0.15)',
              border: '1px solid rgba(74,122,66,0.3)',
              borderRadius: '8px',
              marginBottom: '10px'
            }}>
              <span style={{ fontSize: '0.8rem', color: '#F9F3E8' }}>Đường dây nóng trạm:</span>
              <strong style={{ color: '#F3E5AB', fontSize: '0.95rem' }}>📞 {activeHub.hotline}</strong>
            </div>

            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                onClick={() => {
                  if (typeof window.openLoginModal === 'function') {
                    window.openLoginModal('citizen');
                  }
                }}
                style={{
                  flex: 1,
                  padding: '10px',
                  borderRadius: '8px',
                  border: 'none',
                  background: 'linear-gradient(135deg, #D4AF37, #B58A28)',
                  color: '#081424',
                  fontWeight: '700',
                  fontSize: '0.84rem',
                  cursor: 'pointer'
                }}
              >
                🚨 Đề Nghị Cứu Trợ Tại Điểm Này
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Quick station selector pills */}
      <div style={{ marginTop: '16px', display: 'flex', gap: '8px', overflowX: 'auto', paddingTop: '4px' }}>
        {filteredHubs.map(hub => (
          <button
            key={hub.id}
            onClick={() => handleSelectHub(hub)}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              border: activeHub.id === hub.id ? '1px solid #D4AF37' : '1px solid rgba(255,255,255,0.1)',
              background: activeHub.id === hub.id ? 'rgba(212,175,55,0.15)' : 'rgba(255,255,255,0.03)',
              color: activeHub.id === hub.id ? '#F3E5AB' : 'rgba(249,243,232,0.7)',
              fontSize: '0.78rem',
              cursor: 'pointer',
              whiteSpace: 'nowrap'
            }}
          >
            {hub.icon} {hub.name.split('—')[0]}
          </button>
        ))}
      </div>
    </div>
  );
}
