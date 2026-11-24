USE CoordinationDB;
GO

-- Idempotent seed script for CoordinationDB (roles, catalog, core & extended demo data).
-- Demo login password for every seeded account: DemoPass123!
SET NOCOUNT ON;
SET XACT_ABORT ON;

IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
   OR OBJECT_ID(N'dbo.users', N'U') IS NULL
   OR OBJECT_ID(N'dbo.units', N'U') IS NULL
   OR OBJECT_ID(N'dbo.categories', N'U') IS NULL
   OR OBJECT_ID(N'dbo.items', N'U') IS NULL
    THROW 51000, 'Chua co cac bang trong CoordinationDB. Hay chay sql/Coordination.sql truoc.', 1;

BEGIN TRY
    BEGIN TRANSACTION;

    -- 1. Roles mặc định
    INSERT dbo.roles (role_name, description)
    SELECT seed.role_name, seed.description
    FROM (VALUES
        ('USER', N'Người dùng thông thường'),
        ('ADMIN', N'Quản trị viên hệ thống'),
        ('COORDINATOR', N'Người điều phối vật tư')
    ) seed(role_name, description)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.roles existing WHERE existing.role_name = seed.role_name);

    -- 2. Đơn vị tính
    INSERT dbo.units (unit_name, symbol)
    SELECT seed.unit_name, seed.symbol
    FROM (VALUES
        (N'Thùng', N'thùng'),
        (N'Kilogram', N'kg'),
        (N'Cái', N'cái'),
        (N'Hộp', N'hộp'),
        (N'Chai', N'chai'),
        (N'Bộ', N'bộ'),
        (N'Lít', N'lít'),
        (N'Chiếc', N'chiếc')
    ) seed(unit_name, symbol)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.units existing WHERE existing.symbol = seed.symbol);

    -- 3. Danh mục vật tư
    INSERT dbo.categories (name, description)
    SELECT seed.name, seed.description
    FROM (VALUES
        (N'Thực phẩm & Lương thực', N'Gạo, mì ăn liền và thực phẩm khô dự trữ phòng bão lũ'),
        (N'Nước sạch & Đồ uống', N'Nước uống tinh khiết đóng chai và nước sát khuẩn'),
        (N'Y tế & Cứu thương', N'Túi sơ cấp cứu, thuốc men thiết yếu và thiết bị y tế lưu động'),
        (N'Thiết bị & Cứu nạn', N'Ca-nô, áo phao, máy bơm chìm và đèn pin cứu hộ bão lũ'),
        (N'Quần áo & Đồ dùng an sinh', N'Chăn màn, áo ấm và đồ dùng thiết yếu cho người dân')
    ) seed(name, description)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.categories existing WHERE existing.name = seed.name);

    -- 4. Danh mục vật tư mẫu
    INSERT dbo.items (category_id, unit_id, name, description)
    SELECT c.id, u.id, seed.name, seed.description
    FROM (VALUES
        (N'Gạo trắng ST25', N'Gạo đóng bao 25kg tiêu chuẩn an sinh xã hội Quận Ngũ Hành Sơn', N'Thực phẩm & Lương thực', N'kg'),
        (N'Mì tôm Hảo Hảo', N'Mì tôm ăn liền đóng thùng 30 gói, dự trữ phòng chống thiên tai', N'Thực phẩm & Lương thực', N'thùng'),
        (N'Nước khoáng Lavie 500ml', N'Nước khoáng tinh khiết đóng thùng 24 chai phục vụ vùng ngập mặn/ngập úng', N'Nước sạch & Đồ uống', N'chai'),
        (N'Áo phao cứu sinh đạt chuẩn', N'Áo phao xốp có còi báo hiệu, đạt chuẩn PCTT & TKCN', N'Thiết bị & Cứu nạn', N'cái'),
        (N'Túi thuốc sơ cấp cứu & Vitamin', N'Bộ túi thuốc gia đình gồm bông băng gạc, cồn đỏ, panadol, vitamin C', N'Y tế & Cứu thương', N'bộ'),
        (N'Máy bơm chìm hút ngập lũ', N'Máy bơm công suất lớn giải cứu các điểm ngập úng ven sông Cổ Cò', N'Thiết bị & Cứu nạn', N'chiếc'),
        (N'Đèn pin sạc đội đầu & Pin', N'Đèn pin LED chống thấm nước kèm pin sạc dự phòng cứu nạn ban đêm', N'Thiết bị & Cứu nạn', N'cái'),
        (N'Ca-nô cứu nạn vỏ nhôm', N'Ca-nô cứu hộ tốc độ cao phục vụ sơ tán dân ven biển Non Nước và ven sông', N'Thiết bị & Cứu nạn', N'chiếc')
    ) seed(name, description, category_name, unit_symbol)
    JOIN dbo.categories c ON c.name = seed.category_name
    JOIN dbo.units u ON u.symbol = seed.unit_symbol
    WHERE NOT EXISTS (SELECT 1 FROM dbo.items existing WHERE existing.name = seed.name);

    DECLARE @PasswordHash VARCHAR(255) = '$2a$10$918AlOSAbceZRDUuOR7XDOK93g2aQTq3TDV47uYpcQlt5ySTdnpLe';
    DECLARE @AdminRoleId BIGINT = (SELECT TOP (1) id FROM dbo.roles WHERE role_name = 'ADMIN');
    DECLARE @CoordinatorRoleId BIGINT = (SELECT TOP (1) id FROM dbo.roles WHERE role_name = 'COORDINATOR');
    DECLARE @UserRoleId BIGINT = (SELECT TOP (1) id FROM dbo.roles WHERE role_name = 'USER');
    DECLARE @RiceItemId BIGINT = (SELECT TOP (1) id FROM dbo.items WHERE name = N'Gạo trắng ST25' ORDER BY id);
    DECLARE @NoodleItemId BIGINT = (SELECT TOP (1) id FROM dbo.items WHERE name = N'Mì tôm Hảo Hảo' ORDER BY id);
    DECLARE @WaterItemId BIGINT = (SELECT TOP (1) id FROM dbo.items WHERE name = N'Nước khoáng Lavie 500ml' ORDER BY id);
    DECLARE @LifeJacketItemId BIGINT = (SELECT TOP (1) id FROM dbo.items WHERE name = N'Áo phao cứu sinh đạt chuẩn' ORDER BY id);
    DECLARE @MedicalItemId BIGINT = (SELECT TOP (1) id FROM dbo.items WHERE name = N'Túi thuốc sơ cấp cứu & Vitamin' ORDER BY id);

    IF @AdminRoleId IS NULL OR @CoordinatorRoleId IS NULL OR @UserRoleId IS NULL
        THROW 51001, 'Khong the khoi tao cac role mac dinh.', 1;
    IF @RiceItemId IS NULL OR @NoodleItemId IS NULL OR @WaterItemId IS NULL
       OR @LifeJacketItemId IS NULL OR @MedicalItemId IS NULL
        THROW 51002, 'Khong the khoi tao danh muc vat tu mau.', 1;

    -- 5. Tài khoản demo cốt lõi
    IF EXISTS (SELECT 1 FROM dbo.users WHERE email = 'demo.admin@coordination.local')
        UPDATE dbo.users SET password = @PasswordHash, status = 'ACTIVE'
        WHERE email = 'demo.admin@coordination.local';
    ELSE
        INSERT dbo.users (full_name, email, password, phone, status)
        VALUES (N'Nguyễn Duy (Admin BCH)', 'demo.admin@coordination.local', @PasswordHash, '0900000001', 'ACTIVE');

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'demo.coordinator@coordination.local')
        INSERT dbo.users (full_name, email, password, phone, status)
        VALUES (N'Trần Văn Bình (Điều phối viên)', 'demo.coordinator@coordination.local', @PasswordHash, '0900000002', 'ACTIVE');

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'demo.donor@coordination.local')
        INSERT dbo.users (full_name, email, password, phone, status)
        VALUES (N'Hội Thiện Nguyện TP. Đà Nẵng', 'demo.donor@coordination.local', @PasswordHash, '0900000003', 'ACTIVE');

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'demo.recipient@coordination.local')
        INSERT dbo.users (full_name, email, password, phone, status)
        VALUES (N'Lê Thị Hoa (Người dân P. Khuê Mỹ)', 'demo.recipient@coordination.local', @PasswordHash, '0900000004', 'ACTIVE');

    DECLARE @AdminId BIGINT = (SELECT id FROM dbo.users WHERE email = 'demo.admin@coordination.local');
    DECLARE @CoordinatorId BIGINT = (SELECT id FROM dbo.users WHERE email = 'demo.coordinator@coordination.local');
    DECLARE @DonorId BIGINT = (SELECT id FROM dbo.users WHERE email = 'demo.donor@coordination.local');
    DECLARE @RecipientId BIGINT = (SELECT id FROM dbo.users WHERE email = 'demo.recipient@coordination.local');

    UPDATE dbo.users
    SET citizen_id = '040123456789', birth_date = '1990-01-01',
        address = N'20 Đường Mẫu, Quận 1, TP. Hồ Chí Minh', ward = N'Phường Mẫu',
        beneficiary_group = N'Hộ gia đình khó khăn',
        situation_description = N'Dữ liệu mẫu để minh họa hồ sơ người thụ hưởng'
    WHERE id = @RecipientId;

    INSERT dbo.user_roles (user_id, role_id, assigned_by)
    SELECT @AdminId, @AdminRoleId, NULL
    WHERE NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @AdminId AND role_id = @AdminRoleId);

    INSERT dbo.user_roles (user_id, role_id, assigned_by)
    SELECT @CoordinatorId, @CoordinatorRoleId, @AdminId
    WHERE NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @CoordinatorId AND role_id = @CoordinatorRoleId);

    INSERT dbo.user_roles (user_id, role_id, assigned_by)
    SELECT @DonorId, @UserRoleId, @AdminId
    WHERE NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @DonorId AND role_id = @UserRoleId);

    INSERT dbo.user_roles (user_id, role_id, assigned_by)
    SELECT @RecipientId, @UserRoleId, @AdminId
    WHERE NOT EXISTS (SELECT 1 FROM dbo.user_roles WHERE user_id = @RecipientId AND role_id = @UserRoleId);

    -- 6. Đối tác vận chuyển & Điểm tiếp nhận cốt lõi
    IF NOT EXISTS (SELECT 1 FROM dbo.shipping_partners WHERE partner_name = N'Đội Xung Kích Cứu Nạn & Hậu Cần Quận Ngũ Hành Sơn')
        INSERT dbo.shipping_partners (partner_name, phone, website, status)
        VALUES (N'Đội Xung Kích Cứu Nạn & Hậu Cần Quận Ngũ Hành Sơn', '02363847234', N'https://danang.gov.vn', 'ACTIVE');

    DECLARE @PartnerId BIGINT = (SELECT TOP (1) id FROM dbo.shipping_partners WHERE partner_name LIKE N'%Ngũ Hành Sơn%' OR partner_name = N'Demo Logistics' ORDER BY id);

    IF NOT EXISTS (SELECT 1 FROM dbo.collection_points WHERE name = N'UBND Quận Ngũ Hành Sơn — Trung Tâm Điều Phối Khẩn Cấp')
        INSERT dbo.collection_points (name, address, contact_person, phone, start_date, status)
        VALUES (N'UBND Quận Ngũ Hành Sơn — Trung Tâm Điều Phối Khẩn Cấp', N'486 Lê Văn Hiến, Phường Khuê Mỹ, Quận Ngũ Hành Sơn, TP. Đà Nẵng',
                N'Trần Văn Bình', '02363847234', CONVERT(date, SYSDATETIME()), 'ACTIVE');

    IF NOT EXISTS (SELECT 1 FROM dbo.collection_points WHERE name = N'Kho Lương Thực Di Tích K20')
        INSERT dbo.collection_points (name, address, contact_person, phone, start_date, status)
        VALUES (N'Kho Lương Thực Di Tích K20', N'Khu Di tích K20, Phường Khuê Mỹ, Quận Ngũ Hành Sơn, TP. Đà Nẵng',
                N'Nguyễn Văn An', '0905123456', CONVERT(date, SYSDATETIME()), 'ACTIVE');

    IF NOT EXISTS (SELECT 1 FROM dbo.collection_points WHERE name = N'Trạm Y Tế Phường Mỹ An')
        INSERT dbo.collection_points (name, address, contact_person, phone, start_date, status)
        VALUES (N'Trạm Y Tế Phường Mỹ An', N'Đường Châu Thị Vĩnh Tế, Phường Mỹ An, Quận Ngũ Hành Sơn, TP. Đà Nẵng',
                N'Bác sĩ Trực ban', '02363847115', CONVERT(date, SYSDATETIME()), 'ACTIVE');

    IF NOT EXISTS (SELECT 1 FROM dbo.collection_points WHERE name = N'Điểm Tránh Trú Bão Ven Sông Hòa Quý')
        INSERT dbo.collection_points (name, address, contact_person, phone, start_date, status)
        VALUES (N'Điểm Tránh Trú Bão Ven Sông Hòa Quý', N'Đường Võ Chí Công, Phường Hòa Quý, Quận Ngũ Hành Sơn, TP. Đà Nẵng',
                N'Lê Tấn Lực', '0913987654', CONVERT(date, SYSDATETIME()), 'ACTIVE');

    IF NOT EXISTS (SELECT 1 FROM dbo.collection_points WHERE name = N'Trạm Ca-nô Cứu Nạn Non Nước — Hòa Hải')
        INSERT dbo.collection_points (name, address, contact_person, phone, start_date, status)
        VALUES (N'Trạm Ca-nô Cứu Nạn Non Nước — Hòa Hải', N'Đường Trường Sa, Phường Hòa Hải, Quận Ngũ Hành Sơn, TP. Đà Nẵng',
                N'Đội cứu hộ Non Nước', '0905678901', CONVERT(date, SYSDATETIME()), 'ACTIVE');

    -- 7. Quy trình mẫu hoàn chỉnh (Donation -> Request -> Allocation -> Shipment -> Delivery Confirmation)
    IF NOT EXISTS (SELECT 1 FROM dbo.donations WHERE donation_code = 'DN-NHS-2026-001')
        INSERT dbo.donations (donor_id, donation_code, pickup_address, donation_type, note,
                              status, approved_by, approved_at)
        VALUES (@DonorId, 'DN-NHS-2026-001', N'Hội Thiện Nguyện TP. Đà Nẵng, Hải Châu, TP. Đà Nẵng',
                'DONATED', N'Quyên góp cứu trợ bão lũ Quận Ngũ Hành Sơn: 100 kg Gạo trắng ST25', 'APPROVED',
                @CoordinatorId, SYSDATETIME());

    DECLARE @CompleteDonationId BIGINT = (SELECT id FROM dbo.donations WHERE donation_code = 'DN-NHS-2026-001');
    IF @CompleteDonationId IS NULL
        SET @CompleteDonationId = (SELECT TOP (1) id FROM dbo.donations WHERE donation_code = 'DN-DEMO-COMPLETE-001');

    IF @CompleteDonationId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.donation_items WHERE donation_id = @CompleteDonationId AND item_id = @RiceItemId)
        INSERT dbo.donation_items (donation_id, item_id, offered_quantity, accepted_quantity,
                                   reserved_quantity, delivered_quantity, item_condition, status)
        VALUES (@CompleteDonationId, @RiceItemId, 100, 100, 0, 20, 'NEW', 'ACCEPTED');

    DECLARE @CompleteDonationItemId BIGINT =
        (SELECT TOP (1) id FROM dbo.donation_items WHERE donation_id = @CompleteDonationId AND item_id = @RiceItemId ORDER BY id);

    IF @CompleteDonationItemId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.supply_movements WHERE donation_item_id = @CompleteDonationItemId
                   AND movement_type = 'ACCEPTED' AND reference_type = 'DONATION' AND reference_id = @CompleteDonationId)
        INSERT dbo.supply_movements (donation_item_id, movement_type, quantity, to_location_type,
                                     reference_type, reference_id, created_by)
        VALUES (@CompleteDonationItemId, 'ACCEPTED', 100, 'AVAILABLE', 'DONATION', @CompleteDonationId, @CoordinatorId);

    IF NOT EXISTS (SELECT 1 FROM dbo.assistance_requests WHERE request_code = 'RQ-NHS-2026-001')
        INSERT dbo.assistance_requests (recipient_id, request_code, title, description, delivery_address,
                                        priority, status, approved_by, approved_at)
        VALUES (@RecipientId, 'RQ-NHS-2026-001', N'Hỗ trợ gạo an sinh cho hộ gia đình vùng ngập',
                N'Gia đình khó khăn tại Phường Khuê Mỹ cần gạo và nước uống trong đợt mưa lũ',
                N'Tổ 12, Đường Lê Văn Hiến, Phường Khuê Mỹ, Quận Ngũ Hành Sơn, TP. Đà Nẵng', 'HIGH', 'COMPLETED',
                @CoordinatorId, SYSDATETIME());

    DECLARE @CompleteRequestId BIGINT = (SELECT id FROM dbo.assistance_requests WHERE request_code = 'RQ-NHS-2026-001');
    IF @CompleteRequestId IS NULL
        SET @CompleteRequestId = (SELECT TOP (1) id FROM dbo.assistance_requests WHERE request_code = 'RQ-DEMO-COMPLETE-001');

    IF @CompleteRequestId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.request_items WHERE request_id = @CompleteRequestId AND item_id = @RiceItemId)
        INSERT dbo.request_items (request_id, item_id, requested_quantity, approved_quantity, note)
        VALUES (@CompleteRequestId, @RiceItemId, 20, 20, N'Cứu trợ đợt 1');

    DECLARE @CompleteRequestItemId BIGINT =
        (SELECT TOP (1) id FROM dbo.request_items WHERE request_id = @CompleteRequestId AND item_id = @RiceItemId ORDER BY id);

    IF NOT EXISTS (SELECT 1 FROM dbo.allocations WHERE allocation_code = 'AL-NHS-2026-001')
        INSERT dbo.allocations (allocation_code, request_id, coordinator_id, status, note)
        VALUES ('AL-NHS-2026-001', @CompleteRequestId, @CoordinatorId, 'COMPLETED', N'Phân bổ từ Kho Lương Thực K20');

    DECLARE @CompleteAllocationId BIGINT =
        (SELECT id FROM dbo.allocations WHERE allocation_code = 'AL-NHS-2026-001');
    IF @CompleteAllocationId IS NULL
        SET @CompleteAllocationId = (SELECT TOP (1) id FROM dbo.allocations WHERE allocation_code = 'AL-DEMO-COMPLETE-001');

    IF @CompleteAllocationId IS NOT NULL AND @CompleteRequestItemId IS NOT NULL AND @CompleteDonationItemId IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM dbo.allocation_items WHERE allocation_id = @CompleteAllocationId
                   AND request_item_id = @CompleteRequestItemId AND donation_item_id = @CompleteDonationItemId)
        INSERT dbo.allocation_items (allocation_id, request_item_id, donation_item_id, quantity)
        VALUES (@CompleteAllocationId, @CompleteRequestItemId, @CompleteDonationItemId, 20);

    IF @CompleteDonationItemId IS NOT NULL AND @CompleteAllocationId IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM dbo.supply_movements WHERE donation_item_id = @CompleteDonationItemId
                   AND movement_type = 'RESERVED' AND reference_type = 'ALLOCATION' AND reference_id = @CompleteAllocationId)
        INSERT dbo.supply_movements (donation_item_id, movement_type, quantity, from_location_type,
                                     to_location_type, to_location_id, reference_type, reference_id, created_by)
        VALUES (@CompleteDonationItemId, 'RESERVED', 20, 'AVAILABLE', 'ALLOCATION',
                @CompleteAllocationId, 'ALLOCATION', @CompleteAllocationId, @CoordinatorId);

    DECLARE @Now DATETIME2 = SYSDATETIME();
    DECLARE @BookedAt DATETIME2 = DATEADD(hour, -2, @Now);
    DECLARE @PickedUpAt DATETIME2 = DATEADD(minute, -90, @Now);
    DECLARE @InTransitAt DATETIME2 = DATEADD(minute, -60, @Now);
    DECLARE @OutForDeliveryAt DATETIME2 = DATEADD(minute, -30, @Now);

    IF NOT EXISTS (SELECT 1 FROM dbo.shipments WHERE shipment_code = 'SH-NHS-2026-001')
        INSERT dbo.shipments (shipment_code, partner_id, tracking_code, tracking_url, pickup_address,
                              delivery_address, status, created_at, picked_up_at, delivered_at)
        VALUES ('SH-NHS-2026-001', @PartnerId, 'NHS-TRACK-001',
                N'https://danang.gov.vn/tracking/NHS-TRACK-001',
                N'Kho Lương Thực Di Tích K20, Phường Khuê Mỹ, Quận Ngũ Hành Sơn, TP. Đà Nẵng',
                N'Tổ 12, Đường Lê Văn Hiến, Phường Khuê Mỹ, Quận Ngũ Hành Sơn, TP. Đà Nẵng',
                'DELIVERED', @BookedAt, @PickedUpAt, @Now);

    DECLARE @ShipmentId BIGINT = (SELECT id FROM dbo.shipments WHERE shipment_code = 'SH-NHS-2026-001');
    IF @ShipmentId IS NULL
        SET @ShipmentId = (SELECT TOP (1) id FROM dbo.shipments WHERE shipment_code = 'SH-DEMO-COMPLETE-001');

    IF @ShipmentId IS NOT NULL AND @CompleteAllocationId IS NOT NULL
    BEGIN
        DECLARE @ShipmentItemId BIGINT = (SELECT TOP (1) id FROM dbo.shipment_items
                                          WHERE shipment_id = @ShipmentId AND allocation_item_id =
                                                (SELECT TOP (1) id FROM dbo.allocation_items WHERE allocation_id = @CompleteAllocationId)
                                          ORDER BY id);
        IF @ShipmentItemId IS NULL
            INSERT dbo.shipment_items (shipment_id, allocation_item_id, quantity)
            SELECT @ShipmentId, id, 20 FROM dbo.allocation_items WHERE allocation_id = @CompleteAllocationId
              AND request_item_id = @CompleteRequestItemId AND donation_item_id = @CompleteDonationItemId;
    END

    IF @ShipmentId IS NOT NULL AND @CompleteDonationItemId IS NOT NULL
       AND NOT EXISTS (SELECT 1 FROM dbo.supply_movements WHERE donation_item_id = @CompleteDonationItemId
                   AND movement_type = 'DELIVERED' AND reference_type = 'SHIPMENT' AND reference_id = @ShipmentId)
        INSERT dbo.supply_movements (donation_item_id, movement_type, quantity, from_location_type,
                                     from_location_id, to_location_type, to_location_id,
                                     reference_type, reference_id, created_by)
        VALUES (@CompleteDonationItemId, 'DELIVERED', 20, 'ALLOCATION', @CompleteAllocationId,
                'RECIPIENT', @RecipientId, 'SHIPMENT', @ShipmentId, @CoordinatorId);

    IF @ShipmentId IS NOT NULL
    BEGIN
        INSERT dbo.shipment_events (shipment_id, status, description, location, updated_by, created_at)
        SELECT @ShipmentId, event_data.status, event_data.description, event_data.location, @CoordinatorId, event_data.created_at
        FROM (VALUES
            ('BOOKED', N'Đơn vận chuyển cứu trợ đã được lập tại Sở Chỉ huy Quận', N'486 Lê Văn Hiến, P. Khuê Mỹ', @BookedAt),
            ('PICKED_UP', N'Đội cứu hộ xung kích đã nhận hàng cứu trợ', N'Kho Lương Thực Di Tích K20', @PickedUpAt),
            ('IN_TRANSIT', N'Ca-nô / Xe chuyên dụng đang di chuyển trong địa bàn Phường Khuê Mỹ', N'Tuyến Lê Văn Hiến — Nam Kỳ Khởi Nghĩa', @InTransitAt),
            ('OUT_FOR_DELIVERY', N'Đang bàn giao tận tay hộ dân khó khăn', N'Tổ 12, P. Khuê Mỹ, Q. Ngũ Hành Sơn', @OutForDeliveryAt),
            ('DELIVERED', N'Đã bàn giao đầy đủ lương thực cứu trợ', N'Tổ 12, P. Khuê Mỹ, Q. Ngũ Hành Sơn', @Now)
        ) AS event_data(status, description, location, created_at)
        WHERE NOT EXISTS (SELECT 1 FROM dbo.shipment_events existing
                          WHERE existing.shipment_id = @ShipmentId AND existing.status = event_data.status);
    END

    IF @ShipmentId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.delivery_confirmations WHERE shipment_id = @ShipmentId)
        INSERT dbo.delivery_confirmations (shipment_id, recipient_id, received_by, confirmation_code, note, confirmed_at)
        VALUES (@ShipmentId, @RecipientId, N'Lê Thị Hoa', 'NHS-RECV-001',
                N'Đã nhận đủ 20kg gạo hỗ trợ an sinh', @Now);

    -- Các bản ghi PENDING / APPROVED mẫu cho luồng duyệt & phân bổ
    IF NOT EXISTS (SELECT 1 FROM dbo.donations WHERE donation_code = 'DN-NHS-2026-002')
        INSERT dbo.donations (donor_id, donation_code, pickup_address, donation_type, note, status)
        VALUES (@DonorId, 'DN-NHS-2026-002', N'Đại lý Phân phối Nước giải khát Đà Nẵng',
                'DONATED', N'Ủng hộ 100 thùng Nước khoáng Lavie cho người dân vùng trũng ven sông', 'PENDING');

    IF NOT EXISTS (SELECT 1 FROM dbo.assistance_requests WHERE request_code = 'RQ-NHS-2026-002')
        INSERT dbo.assistance_requests (recipient_id, request_code, title, description, delivery_address, priority, status)
        VALUES (@RecipientId, 'RQ-NHS-2026-002', N'Xin hỗ trợ mì tôm và nước sạch khẩn cấp',
                N'Khu vực ngập nước ven sông Cổ Cò, nước giếng bị ô nhiễm không dùng được',
                N'Tổ 4, Ven sông Cổ Cò, Phường Hòa Quý, Quận Ngũ Hành Sơn, TP. Đà Nẵng', 'URGENT', 'PENDING');

    DECLARE @PendingRequestId BIGINT = (SELECT id FROM dbo.assistance_requests WHERE request_code = 'RQ-NHS-2026-002');
    IF @PendingRequestId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.request_items WHERE request_id = @PendingRequestId AND item_id = @NoodleItemId)
        INSERT dbo.request_items (request_id, item_id, requested_quantity, approved_quantity, note)
        VALUES (@PendingRequestId, @NoodleItemId, 5, 0, N'Ưu tiên hộ già neo đơn');

    IF NOT EXISTS (SELECT 1 FROM dbo.assistance_requests WHERE request_code = 'RQ-NHS-2026-003')
        INSERT dbo.assistance_requests (recipient_id, request_code, title, description, delivery_address,
                                        priority, status, approved_by, approved_at)
        VALUES (@RecipientId, 'RQ-NHS-2026-003', N'Hỗ trợ áo phao và thuốc men gia đình',
                N'Hộ gia đình có người cao tuổi cần túi thuốc dự phòng và 4 áo phao cứu sinh',
                N'Tổ 8, Phường Mỹ An, Quận Ngũ Hành Sơn, TP. Đà Nẵng', 'HIGH', 'APPROVED',
                @CoordinatorId, SYSDATETIME());

    IF NOT EXISTS (SELECT 1 FROM dbo.assistance_requests WHERE request_code = 'RQ-DEMO-READY-001')
        INSERT dbo.assistance_requests (recipient_id, request_code, title, description, delivery_address,
                                        priority, status, approved_by, approved_at)
        VALUES (@RecipientId, 'RQ-DEMO-READY-001', N'Yêu cầu mẫu sẵn sàng phân bổ',
                N'Yêu cầu được duyệt để thử quy trình phân bổ vật tư từ kho.',
                N'20 Đường Mẫu, Phường Mẫu, TP. Đà Nẵng', 'NORMAL', 'APPROVED',
                @CoordinatorId, SYSDATETIME());

    DECLARE @ReadyRequestId BIGINT = (SELECT id FROM dbo.assistance_requests WHERE request_code = 'RQ-DEMO-READY-001');
    IF @ReadyRequestId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.request_items WHERE request_id = @ReadyRequestId AND item_id = @RiceItemId)
        INSERT dbo.request_items (request_id, item_id, requested_quantity, approved_quantity, note)
        VALUES (@ReadyRequestId, @RiceItemId, 30, 30, N'Dữ liệu mẫu: sẵn sàng phân bổ');

    -- 8. Dữ liệu mở rộng (người dân, điều phối viên, yêu cầu, quyên góp, dịch vụ an sinh, điểm tiếp nhận)
    DECLARE @ExtraUsers TABLE (
        full_name NVARCHAR(150) NOT NULL,
        email VARCHAR(150) NOT NULL,
        phone VARCHAR(20) NULL,
        citizen_id VARCHAR(20) NULL,
        birth_date DATE NULL,
        address NVARCHAR(500) NULL,
        ward NVARCHAR(100) NULL,
        beneficiary_group NVARCHAR(100) NULL,
        situation_description NVARCHAR(MAX) NULL,
        status VARCHAR(30) NOT NULL,
        role_name VARCHAR(50) NOT NULL
    );

    INSERT INTO @ExtraUsers
        (full_name, email, phone, citizen_id, birth_date, address, ward, beneficiary_group, situation_description, status, role_name)
    VALUES
        (N'Trần Văn Hiếu', 'demo.citizen.hieu@coordination.local', '0901110101', '040000000101', '1952-04-18', N'12 Nguyễn Duy Trinh, tổ 3', N'Phường Hòa Hải', N'Người cao tuổi', N'Sống một mình, cần hỗ trợ lương thực và thuốc huyết áp.', 'ACTIVE', 'USER'),
        (N'Lê Thị Hồng', 'demo.citizen.hong@coordination.local', '0901110102', '040000000102', '1968-09-02', N'45 Lê Văn Hiến, tổ 5', N'Phường Khuê Mỹ', N'Người khuyết tật', N'Hạn chế vận động, cần hỗ trợ đi lại và thuốc thiết yếu.', 'ACTIVE', 'USER'),
        (N'Phạm Minh Duy', 'demo.citizen.duy@coordination.local', '0901110103', '040000000103', '2014-03-11', N'8 Trần Đại Nghĩa, tổ 2', N'Phường Hòa Quý', N'Trẻ em có hoàn cảnh khó khăn', N'Học sinh thuộc hộ khó khăn, cần đồ dùng học tập.', 'ACTIVE', 'USER'),
        (N'Bùi Thị Lan', 'demo.citizen.lan@coordination.local', '0901110104', '040000000104', '1995-07-26', N'21 Châu Thị Vĩnh Tế, tổ 4', N'Phường Mỹ An', N'Phụ nữ mang thai', N'Đang mang thai tháng thứ bảy, cần khám sức khỏe định kỳ.', 'ACTIVE', 'USER'),
        (N'Nguyễn Văn Bình', 'demo.citizen.binh@coordination.local', '0901110105', '040000000105', '1981-12-05', N'6 Lê Quang Đạo, tổ 1', N'Phường Mỹ An', N'Hộ nghèo', N'Lao động thời vụ, thu nhập không ổn định, gia đình có ba người phụ thuộc.', 'ACTIVE', 'USER'),
        (N'Võ Thị Mai', 'demo.citizen.mai@coordination.local', '0901110106', '040000000106', '1948-01-30', N'19 Non Nước, tổ 7', N'Phường Hòa Hải', N'Cao tuổi', N'Cần gạo, nước sạch và hỗ trợ kiểm tra sức khỏe.', 'ACTIVE', 'USER'),
        (N'Đặng Minh Khang', 'demo.citizen.khang@coordination.local', '0901110107', '040000000107', '2012-11-14', N'72 Trần Hưng Đạo, tổ 6', N'Phường Hòa Quý', N'Trẻ em', N'Gia đình bị ngập, cần sách vở và quần áo học sinh.', 'ACTIVE', 'USER'),
        (N'Hoàng Thị Cúc', 'demo.citizen.cuc@coordination.local', '0901110108', '040000000108', '1974-06-09', N'5 Mai Đăng Chơn, tổ 8', N'Phường Hòa Quý', N'Khác', N'Nhà bị tốc mái sau mưa lớn, cần vật liệu che chắn tạm thời.', 'ACTIVE', 'USER'),
        (N'Phan Văn Tâm', 'demo.citizen.tam@coordination.local', '0901110109', '040000000109', '1960-02-21', N'31 Lê Văn Hiến, tổ 9', N'Phường Khuê Mỹ', N'Hộ gia đình khó khăn', N'Gia đình có người bệnh dài ngày, cần lương thực và thuốc cơ bản.', 'LOCKED', 'USER'),
        (N'Trần Quốc An', 'demo.coordinator.an@coordination.local', '0901110201', NULL, '1987-08-12', N'UBND phường Hòa Hải', N'Phường Hòa Hải', NULL, NULL, 'ACTIVE', 'COORDINATOR');

    INSERT dbo.users (full_name, email, password, phone, citizen_id, birth_date, address, ward,
                      beneficiary_group, situation_description, status)
    SELECT u.full_name, u.email, @PasswordHash, u.phone, u.citizen_id, u.birth_date, u.address, u.ward,
           u.beneficiary_group, u.situation_description, u.status
    FROM @ExtraUsers u
    WHERE NOT EXISTS (SELECT 1 FROM dbo.users existing WHERE existing.email = u.email);

    INSERT dbo.user_roles (user_id, role_id, assigned_by)
    SELECT u.id, CASE seed.role_name WHEN 'ADMIN' THEN @AdminRoleId WHEN 'COORDINATOR' THEN @CoordinatorRoleId ELSE @UserRoleId END, @AdminId
    FROM dbo.users u
    JOIN @ExtraUsers seed ON seed.email = u.email
    WHERE NOT EXISTS (
        SELECT 1 FROM dbo.user_roles existing
        WHERE existing.user_id = u.id
          AND existing.role_id = CASE seed.role_name WHEN 'ADMIN' THEN @AdminRoleId WHEN 'COORDINATOR' THEN @CoordinatorRoleId ELSE @UserRoleId END
    );

    DECLARE @ExtraRequests TABLE (
        request_code VARCHAR(50) NOT NULL,
        recipient_email VARCHAR(150) NOT NULL,
        title NVARCHAR(200) NOT NULL,
        description NVARCHAR(MAX) NULL,
        delivery_address NVARCHAR(500) NOT NULL,
        priority VARCHAR(30) NOT NULL,
        status VARCHAR(30) NOT NULL
    );
    INSERT INTO @ExtraRequests (request_code, recipient_email, title, description, delivery_address, priority, status)
    VALUES
        ('RQ-EXT-2026-001', 'demo.citizen.hieu@coordination.local', N'Cần nước sạch cho người cao tuổi', N'Nguồn nước sinh hoạt bị ảnh hưởng sau mưa lớn.', N'12 Nguyễn Duy Trinh, tổ 3, Phường Hòa Hải', 'URGENT', 'PENDING'),
        ('RQ-EXT-2026-002', 'demo.citizen.hong@coordination.local', N'Hỗ trợ thuốc và dụng cụ y tế', N'Cần túi sơ cứu và phương tiện hỗ trợ di chuyển.', N'45 Lê Văn Hiến, tổ 5, Phường Khuê Mỹ', 'HIGH', 'PENDING'),
        ('RQ-EXT-2026-003', 'demo.citizen.duy@coordination.local', N'Đồ dùng học tập cho học sinh', N'Gia đình cần lương thực và vật dụng học tập cơ bản.', N'8 Trần Đại Nghĩa, tổ 2, Phường Hòa Quý', 'NORMAL', 'PENDING'),
        ('RQ-EXT-2026-004', 'demo.citizen.lan@coordination.local', N'Khám sức khỏe cho thai phụ', N'Đăng ký hỗ trợ khám sức khỏe và túi thuốc cơ bản.', N'21 Châu Thị Vĩnh Tế, tổ 4, Phường Mỹ An', 'HIGH', 'APPROVED'),
        ('RQ-EXT-2026-005', 'demo.citizen.binh@coordination.local', N'Lương thực cho hộ khó khăn', N'Gia đình cần gạo trong thời gian chưa thể đi làm.', N'6 Lê Quang Đạo, tổ 1, Phường Mỹ An', 'URGENT', 'APPROVED'),
        ('RQ-EXT-2026-006', 'demo.citizen.mai@coordination.local', N'Áo phao cho người cao tuổi', N'Khu dân cư ven biển cần thêm áo phao dự phòng.', N'19 Non Nước, tổ 7, Phường Hòa Hải', 'HIGH', 'PENDING'),
        ('RQ-EXT-2026-007', 'demo.citizen.khang@coordination.local', N'Quần áo và sách vở học sinh', N'Đồ dùng học tập bị hư hỏng do ngập.', N'72 Trần Hưng Đạo, tổ 6, Phường Hòa Quý', 'NORMAL', 'REJECTED'),
        ('RQ-EXT-2026-008', 'demo.citizen.cuc@coordination.local', N'Bạt che mái nhà', N'Nhà bị tốc mái, cần vật liệu che chắn tạm thời.', N'5 Mai Đăng Chơn, tổ 8, Phường Hòa Quý', 'HIGH', 'PENDING'),
        ('RQ-EXT-2026-009', 'demo.citizen.tam@coordination.local', N'Gạo hỗ trợ gia đình có người bệnh', N'Gia đình cần gạo và vật tư thiết yếu.', N'31 Lê Văn Hiến, tổ 9, Phường Khuê Mỹ', 'NORMAL', 'CANCELLED');

    INSERT dbo.assistance_requests (recipient_id, request_code, title, description, delivery_address,
                                    priority, status, approved_by, approved_at)
    SELECT u.id, seed.request_code, seed.title, seed.description, seed.delivery_address, seed.priority, seed.status,
           CASE WHEN seed.status = 'APPROVED' THEN @CoordinatorId ELSE NULL END,
           CASE WHEN seed.status = 'APPROVED' THEN SYSDATETIME() ELSE NULL END
    FROM @ExtraRequests seed
    JOIN dbo.users u ON u.email = seed.recipient_email
    WHERE NOT EXISTS (SELECT 1 FROM dbo.assistance_requests current_request WHERE current_request.request_code = seed.request_code);

    DECLARE @ExtraRequestLines TABLE (
        request_code VARCHAR(50) NOT NULL,
        item_id BIGINT NOT NULL,
        requested_quantity DECIMAL(12,2) NOT NULL,
        approved_quantity DECIMAL(12,2) NOT NULL,
        note NVARCHAR(MAX) NULL
    );
    INSERT INTO @ExtraRequestLines (request_code, item_id, requested_quantity, approved_quantity, note)
    VALUES
        ('RQ-EXT-2026-001', @WaterItemId, 24, 0, N'Nước uống dùng trong hai ngày'),
        ('RQ-EXT-2026-002', @MedicalItemId, 2, 0, N'Túi sơ cứu gia đình'),
        ('RQ-EXT-2026-003', @RiceItemId, 10, 0, N'Gạo hỗ trợ gia đình'),
        ('RQ-EXT-2026-004', @MedicalItemId, 1, 1, N'Ưu tiên thai phụ'),
        ('RQ-EXT-2026-005', @RiceItemId, 20, 20, N'Gạo hỗ trợ hộ khó khăn'),
        ('RQ-EXT-2026-006', @LifeJacketItemId, 2, 0, N'Áo phao đúng kích cỡ'),
        ('RQ-EXT-2026-007', @RiceItemId, 5, 0, N'Dữ liệu mẫu cho yêu cầu bị từ chối'),
        ('RQ-EXT-2026-008', @NoodleItemId, 3, 0, N'Nhu yếu phẩm trong thời gian sửa mái'),
        ('RQ-EXT-2026-009', @RiceItemId, 10, 0, N'Yêu cầu đã được người dân hủy');

    INSERT dbo.request_items (request_id, item_id, requested_quantity, approved_quantity, note)
    SELECT request.id, seed.item_id, seed.requested_quantity, seed.approved_quantity, seed.note
    FROM @ExtraRequestLines seed
    JOIN dbo.assistance_requests request ON request.request_code = seed.request_code
    WHERE NOT EXISTS (SELECT 1 FROM dbo.request_items existing WHERE existing.request_id = request.id AND existing.item_id = seed.item_id);

    DECLARE @ExtraDonations TABLE (
        donation_code VARCHAR(50) NOT NULL,
        donation_type VARCHAR(30) NOT NULL,
        pickup_address NVARCHAR(500) NOT NULL,
        note NVARCHAR(MAX) NULL,
        status VARCHAR(30) NOT NULL
    );
    INSERT INTO @ExtraDonations (donation_code, donation_type, pickup_address, note, status)
    VALUES
        ('DN-EXT-2026-001', 'DONATED', N'12 Trần Phú, Hải Châu, Đà Nẵng', N'Ủng hộ nước uống đóng chai.', 'PENDING'),
        ('DN-EXT-2026-002', 'SPONSORED', N'Kho tiếp nhận Khuê Mỹ, Đà Nẵng', N'Bổ sung gạo và nước uống cho kho an sinh.', 'APPROVED'),
        ('DN-EXT-2026-003', 'PROVIDED', N'Trạm y tế phường Mỹ An, Đà Nẵng', N'Túi sơ cứu phục vụ các hộ dân.', 'PENDING');

    INSERT dbo.donations (donor_id, donation_code, pickup_address, donation_type, note, status, approved_by, approved_at)
    SELECT @DonorId, seed.donation_code, seed.pickup_address, seed.donation_type, seed.note, seed.status,
           CASE WHEN seed.status = 'APPROVED' THEN @CoordinatorId ELSE NULL END,
           CASE WHEN seed.status = 'APPROVED' THEN SYSDATETIME() ELSE NULL END
    FROM @ExtraDonations seed
    WHERE NOT EXISTS (SELECT 1 FROM dbo.donations existing WHERE existing.donation_code = seed.donation_code);

    DECLARE @ExtraDonationLines TABLE (
        donation_code VARCHAR(50) NOT NULL,
        item_id BIGINT NOT NULL,
        offered_quantity DECIMAL(12,2) NOT NULL,
        accepted_quantity DECIMAL(12,2) NOT NULL,
        item_condition VARCHAR(50) NOT NULL
    );
    INSERT INTO @ExtraDonationLines (donation_code, item_id, offered_quantity, accepted_quantity, item_condition)
    VALUES
        ('DN-EXT-2026-001', @WaterItemId, 80, 0, 'NEW'),
        ('DN-EXT-2026-002', @RiceItemId, 100, 100, 'NEW'),
        ('DN-EXT-2026-002', @WaterItemId, 120, 120, 'NEW'),
        ('DN-EXT-2026-003', @MedicalItemId, 12, 0, 'NEW');

    INSERT dbo.donation_items (donation_id, item_id, offered_quantity, accepted_quantity,
                               reserved_quantity, delivered_quantity, item_condition, status)
    SELECT donation.id, seed.item_id, seed.offered_quantity, seed.accepted_quantity, 0, 0,
           seed.item_condition, CASE WHEN seed.accepted_quantity > 0 THEN 'ACCEPTED' ELSE 'PENDING' END
    FROM @ExtraDonationLines seed
    JOIN dbo.donations donation ON donation.donation_code = seed.donation_code
    WHERE NOT EXISTS (SELECT 1 FROM dbo.donation_items existing WHERE existing.donation_id = donation.id AND existing.item_id = seed.item_id);

    INSERT dbo.supply_movements (donation_item_id, movement_type, quantity, to_location_type,
                                 reference_type, reference_id, created_by)
    SELECT line.id, 'ACCEPTED', line.accepted_quantity, 'AVAILABLE', 'DONATION', donation.id, @CoordinatorId
    FROM dbo.donation_items line
    JOIN dbo.donations donation ON donation.id = line.donation_id
    WHERE donation.donation_code = 'DN-EXT-2026-002'
      AND line.accepted_quantity > 0
      AND NOT EXISTS (SELECT 1 FROM dbo.supply_movements movement
                      WHERE movement.donation_item_id = line.id AND movement.movement_type = 'ACCEPTED'
                        AND movement.reference_type = 'DONATION' AND movement.reference_id = donation.id);

    DECLARE @ExtraServices TABLE (
        name NVARCHAR(150) NOT NULL,
        service_type VARCHAR(50) NOT NULL,
        description NVARCHAR(MAX) NOT NULL,
        service_area NVARCHAR(150) NOT NULL,
        contact_phone VARCHAR(20) NOT NULL
    );
    INSERT INTO @ExtraServices (name, service_type, description, service_area, contact_phone)
    VALUES
        (N'Khám sức khỏe lưu động', 'HEALTHCARE', N'Khám sức khỏe cơ bản và tư vấn sử dụng thuốc tại hộ gia đình.', N'Ngũ Hành Sơn', '02363847115'),
        (N'Cấp nước sạch dã chiến', 'CLEAN_WATER', N'Cấp nước sinh hoạt cho khu vực bị ngập hoặc gián đoạn nguồn nước.', N'Ngũ Hành Sơn', '02363847234'),
        (N'Mượn máy phát điện', 'EQUIPMENT', N'Đăng ký mượn thiết bị phát điện khi mất điện kéo dài.', N'Ngũ Hành Sơn', '0905123456'),
        (N'Lều bạt cứu trợ', 'TEMPORARY_SHELTER', N'Hỗ trợ lều bạt tạm thời cho hộ có nhà bị hư hỏng.', N'Ngũ Hành Sơn', '0905123456'),
        (N'Xe lăn cơ động', 'MOBILITY', N'Hỗ trợ mượn xe lăn cho người hạn chế khả năng đi lại.', N'Ngũ Hành Sơn', '02363847115'),
        (N'Gói sách vở trẻ em', 'EDUCATION', N'Cấp sách vở và đồ dùng học tập cho trẻ em thuộc hộ khó khăn.', N'Ngũ Hành Sơn', '02363847234');

    INSERT dbo.social_services (name, description, service_type, service_area, contact_phone, status)
    SELECT seed.name, seed.description, seed.service_type, seed.service_area, seed.contact_phone, 'ACTIVE'
    FROM @ExtraServices seed
    WHERE NOT EXISTS (SELECT 1 FROM dbo.social_services current_service WHERE current_service.name = seed.name);

    INSERT dbo.service_registrations (service_id, user_id, request_details, status)
    SELECT service.id, user_record.id, N'Đăng ký mẫu từ bộ dữ liệu mở rộng.', 'PENDING'
    FROM (VALUES
        (N'Khám sức khỏe lưu động', 'demo.citizen.hieu@coordination.local'),
        (N'Xe lăn cơ động', 'demo.citizen.hong@coordination.local'),
        (N'Gói sách vở trẻ em', 'demo.citizen.duy@coordination.local')
    ) seed(service_name, email)
    JOIN dbo.social_services service ON service.name = seed.service_name
    JOIN dbo.users user_record ON user_record.email = seed.email
    WHERE NOT EXISTS (SELECT 1 FROM dbo.service_registrations existing
                      WHERE existing.service_id = service.id AND existing.user_id = user_record.id);

    INSERT dbo.collection_points (name, address, contact_person, phone, start_date, status)
    SELECT seed.name, seed.address, seed.contact_person, seed.phone, CONVERT(date, SYSDATETIME()), 'ACTIVE'
    FROM (VALUES
        (N'Điểm tiếp nhận Hòa Hải', N'12 Nguyễn Duy Trinh, Phường Hòa Hải, Đà Nẵng', N'Trần Quốc An', '0901110201'),
        (N'Kho dự phòng Mỹ An', N'21 Châu Thị Vĩnh Tế, Phường Mỹ An, Đà Nẵng', N'Bùi Thị Lan', '0901110104'),
        (N'Điểm sơ tán Hòa Quý', N'72 Trần Hưng Đạo, Phường Hòa Quý, Đà Nẵng', N'Đặng Minh Khang', '0901110107')
    ) seed(name, address, contact_person, phone)
    WHERE NOT EXISTS (SELECT 1 FROM dbo.collection_points existing WHERE existing.name = seed.name);

    COMMIT TRANSACTION;
    PRINT 'Da seed du lieu CoordinationDB.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT u.id AS user_id, u.full_name, u.email, u.phone, u.status, r.role_name
FROM dbo.users u
JOIN dbo.user_roles ur ON ur.user_id = u.id
JOIN dbo.roles r ON r.id = ur.role_id
WHERE u.email IN (
    'demo.admin@coordination.local',
    'demo.coordinator@coordination.local',
    'demo.donor@coordination.local',
    'demo.recipient@coordination.local',
    'demo.coordinator.an@coordination.local'
)
   OR u.email LIKE 'demo.citizen.%@coordination.local'
ORDER BY u.email;

SELECT d.donation_code, d.id AS donation_id, di.id AS donation_item_id,
       di.offered_quantity, di.accepted_quantity, di.reserved_quantity, di.delivered_quantity
FROM dbo.donations d
JOIN dbo.donation_items di ON di.donation_id = d.id
WHERE d.donation_code IN ('DN-DEMO-COMPLETE-001', 'DN-DEMO-PENDING-001',
                          'DN-NHS-2026-001', 'DN-NHS-2026-002',
                          'DN-EXT-2026-001', 'DN-EXT-2026-002', 'DN-EXT-2026-003')
ORDER BY d.donation_code;

SELECT r.request_code, r.id AS request_id, ri.id AS request_item_id,
       ri.requested_quantity, ri.approved_quantity
FROM dbo.assistance_requests r
JOIN dbo.request_items ri ON ri.request_id = r.id
WHERE r.request_code IN ('RQ-DEMO-COMPLETE-001', 'RQ-DEMO-PENDING-001', 'RQ-DEMO-READY-001',
                         'RQ-NHS-2026-001', 'RQ-NHS-2026-002', 'RQ-NHS-2026-003')
   OR r.request_code LIKE 'RQ-EXT-2026-%'
ORDER BY r.request_code;

SELECT shipment_code, id AS shipment_id, status, tracking_code
FROM dbo.shipments
WHERE shipment_code IN ('SH-DEMO-COMPLETE-001', 'SH-NHS-2026-001');
GO
