USE CoordinationDB;
GO

SET NOCOUNT ON;
SET XACT_ABORT ON;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @PasswordHash VARCHAR(255) = '$2a$10$918AlOSAbceZRDUuOR7XDOK93g2aQTq3TDV47uYpcQlt5ySTdnpLe';
    DECLARE @AdminRoleId BIGINT = (SELECT TOP (1) id FROM dbo.roles WHERE role_name = 'ADMIN');
    DECLARE @CoordinatorRoleId BIGINT = (SELECT TOP (1) id FROM dbo.roles WHERE role_name = 'COORDINATOR');
    DECLARE @UserRoleId BIGINT = (SELECT TOP (1) id FROM dbo.roles WHERE role_name = 'USER');
    DECLARE @RiceItemId BIGINT = (SELECT TOP (1) id FROM dbo.items WHERE name LIKE N'%Gạo%' ORDER BY id);
    DECLARE @NoodleItemId BIGINT = (SELECT TOP (1) id FROM dbo.items WHERE name LIKE N'%Mì%' ORDER BY id);

    IF @AdminRoleId IS NULL OR @CoordinatorRoleId IS NULL OR @UserRoleId IS NULL
        THROW 51000, 'Chua co cac role mac dinh. Hay chay sql/Coordination.sql truoc.', 1;
    IF @RiceItemId IS NULL OR @NoodleItemId IS NULL
        THROW 51001, 'Chua co vat tu mau. Hay chay sql/Coordination.sql truoc.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE email = 'demo.admin@coordination.local')
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

    -- Completed example: 100 kg of rice donated, 20 kg allocated and delivered.
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

    -- Pending examples for testing coordinator approval endpoints.
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

    -- Approved but unallocated request for testing allocation creation.
    IF NOT EXISTS (SELECT 1 FROM dbo.assistance_requests WHERE request_code = 'RQ-NHS-2026-003')
        INSERT dbo.assistance_requests (recipient_id, request_code, title, description, delivery_address,
                                        priority, status, approved_by, approved_at)
        VALUES (@RecipientId, 'RQ-NHS-2026-003', N'Hỗ trợ áo phao và thuốc men gia đình',
                N'Hộ gia đình có người cao tuổi cần túi thuốc dự phòng và 4 áo phao cứu sinh',
                N'Tổ 8, Phường Mỹ An, Quận Ngũ Hành Sơn, TP. Đà Nẵng', 'HIGH', 'APPROVED',
                @CoordinatorId, SYSDATETIME());

    DECLARE @ReadyRequestId BIGINT = (SELECT id FROM dbo.assistance_requests WHERE request_code = 'RQ-DEMO-READY-001');
    IF NOT EXISTS (SELECT 1 FROM dbo.request_items WHERE request_id = @ReadyRequestId AND item_id = @RiceItemId)
        INSERT dbo.request_items (request_id, item_id, requested_quantity, approved_quantity, note)
        VALUES (@ReadyRequestId, @RiceItemId, 30, 30, N'Dữ liệu mẫu: sẵn sàng phân bổ');

    COMMIT TRANSACTION;
    PRINT 'Da seed du lieu mau CoordinationDB.';
END TRY
BEGIN CATCH
    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;
    THROW;
END CATCH;
GO

SELECT id AS user_id, email, status
FROM dbo.users
WHERE email IN (
    'demo.admin@coordination.local',
    'demo.coordinator@coordination.local',
    'demo.donor@coordination.local',
    'demo.recipient@coordination.local'
)
ORDER BY email;

SELECT d.donation_code, d.id AS donation_id, di.id AS donation_item_id,
       di.offered_quantity, di.accepted_quantity, di.reserved_quantity, di.delivered_quantity
FROM dbo.donations d
JOIN dbo.donation_items di ON di.donation_id = d.id
WHERE d.donation_code IN ('DN-DEMO-COMPLETE-001', 'DN-DEMO-PENDING-001')
ORDER BY d.donation_code;

SELECT r.request_code, r.id AS request_id, ri.id AS request_item_id,
       ri.requested_quantity, ri.approved_quantity
FROM dbo.assistance_requests r
JOIN dbo.request_items ri ON ri.request_id = r.id
WHERE r.request_code IN ('RQ-DEMO-COMPLETE-001', 'RQ-DEMO-PENDING-001', 'RQ-DEMO-READY-001')
ORDER BY r.request_code;

SELECT shipment_code, id AS shipment_id, status, tracking_code
FROM dbo.shipments
WHERE shipment_code = 'SH-DEMO-COMPLETE-001';
GO
