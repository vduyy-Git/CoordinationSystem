// ReliefCatalog.jsx - React Component for Humanitarian Catalog in Ngu Hanh Son, Da Nang
import React, { useState } from 'react';

export const CATALOG_ITEMS = [
  { id: 1, name: 'Túi Thuốc Y Tế Thiết Yếu F0 & Sơ Cứu Bão Lũ', category: 'medical', quantity: '850 túi', unit: 'Bộ chuẩn Y Tế', ward: 'Tất cả 4 Phường', urgent: true },
  { id: 2, name: 'Gạo Trắng ST25 Dự Trữ Cấp Phát Khẩn Cấp', category: 'food', quantity: '12.5 tấn', unit: 'Bao 10kg & 25kg', ward: 'Kho K20 & Hòa Quý', urgent: false },
  { id: 3, name: 'Nước Khoáng Đóng Chai Kháng Khuẩn (500ml & 5L)', category: 'water', quantity: '3.400 thùng', unit: 'Thùng 24 chai', ward: 'UBND Quận & P. Mỹ An', urgent: true },
  { id: 4, name: 'Áo Phao Cứu Sinh & Phao Tròn Tiêu Chuẩn', category: 'safety', quantity: '950 chiếc', unit: 'Cái', ward: 'Hòa Hải & Hòa Quý', urgent: false },
  { id: 5, name: 'Đèn Pin Sạc Năng Lượng & Sạc Dự Phòng 20.000mAh', category: 'supplies', quantity: '420 bộ', unit: 'Bộ kèm dây sạc', ward: 'Vùng trũng Hòa Quý', urgent: false },
  { id: 6, name: 'Gói Dinh Dưỡng Cho Trẻ Em & Mẹ Sau Sinh', category: 'medical', quantity: '280 hộp', unit: 'Combo Sữa & Bột', ward: 'P. Mỹ An & Khuê Mỹ', urgent: true }
];

export default function ReliefCatalog({ onSelectRequest }) {
  const [activeCategory, setActiveCategory] = useState('all');

  const filtered = activeCategory === 'all'
    ? CATALOG_ITEMS
    : CATALOG_ITEMS.filter(item => item.category === activeCategory);

  return (
    <div style={{ background: '#FFFFFF', borderRadius: '14px', padding: '24px', border: '1px solid #E5E7EB', boxShadow: '0 4px 16px rgba(0,0,0,0.06)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px', flexWrap: 'wrap', gap: '12px' }}>
        <div>
          <h3 style={{ fontSize: '1.25rem', fontWeight: '800', color: '#081424', margin: '0 0 4px 0' }}>
            📦 Danh Mục Nguồn Lực &amp; Tiếp Tế Sẵn Có
          </h3>
          <p style={{ margin: 0, fontSize: '0.82rem', color: '#6B7280' }}>
            Dữ liệu hàng cứu trợ tại các kho trung chuyển Quận Ngũ Hành Sơn
          </p>
        </div>
        <div style={{ display: 'flex', gap: '6px' }}>
          {[
            { id: 'all', label: 'Tất cả' },
            { id: 'medical', label: '🏥 Y tế' },
            { id: 'food', label: '🍚 Lương thực' },
            { id: 'water', label: '💧 Nước sạch' },
            { id: 'safety', label: '🦺 Cứu hộ' }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveCategory(tab.id)}
              style={{
                padding: '6px 12px',
                borderRadius: '20px',
                border: activeCategory === tab.id ? '1px solid #D4AF37' : '1px solid #E5E7EB',
                background: activeCategory === tab.id ? '#081424' : '#F9FAFB',
                color: activeCategory === tab.id ? '#F3E5AB' : '#374151',
                fontSize: '0.78rem',
                fontWeight: '700',
                cursor: 'pointer'
              }}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '14px' }}>
        {filtered.map(item => (
          <div key={item.id} style={{
            background: '#FAF7F2',
            border: '1px solid #E5DCCB',
            borderRadius: '10px',
            padding: '14px',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'space-between'
          }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                <span style={{ fontSize: '0.72rem', color: '#6B7280' }}>📍 {item.ward}</span>
                {item.urgent && (
                  <span style={{ background: '#FEE2E2', color: '#DC2626', fontSize: '0.68rem', fontWeight: '800', padding: '2px 6px', borderRadius: '4px' }}>
                    Ưu tiên cấp
                  </span>
                )}
              </div>
              <h4 style={{ fontSize: '0.92rem', fontWeight: '700', color: '#1F2937', margin: '0 0 8px 0', lineHeight: 1.35 }}>
                {item.name}
              </h4>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '12px', paddingTop: '10px', borderTop: '1px solid #E5E7EB' }}>
              <div>
                <span style={{ fontSize: '0.72rem', color: '#6B7280' }}>Sẵn sàng: </span>
                <strong style={{ color: '#2E6B28', fontSize: '0.88rem' }}>{item.quantity}</strong>
              </div>
              <button
                onClick={() => onSelectRequest && onSelectRequest(item)}
                style={{
                  background: '#081424',
                  color: '#D4AF37',
                  border: '1px solid #D4AF37',
                  borderRadius: '6px',
                  padding: '5px 10px',
                  fontSize: '0.75rem',
                  fontWeight: '700',
                  cursor: 'pointer'
                }}
              >
                Nhận hỗ trợ
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
