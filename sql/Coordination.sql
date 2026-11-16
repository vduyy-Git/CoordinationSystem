CREATE DATABASE CoordinationDB;
GO

USE CoordinationDB;
GO
-- 1. USERS
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar NVARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2,

    CONSTRAINT CK_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);
GO

-- 2. ROLES
CREATE TABLE roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description NVARCHAR(255)
);
GO

-- 3. USER_ROLES
CREATE TABLE user_roles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_by BIGINT,
    assigned_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_user_roles_users
        FOREIGN KEY (user_id) REFERENCES users(id),

    CONSTRAINT FK_user_roles_roles
        FOREIGN KEY (role_id) REFERENCES roles(id),

    CONSTRAINT FK_user_roles_assigned_by
        FOREIGN KEY (assigned_by) REFERENCES users(id),

    CONSTRAINT UQ_user_roles
        UNIQUE (user_id, role_id)
);
GO

-- Khởi tạo các quyền mặc định
INSERT INTO roles (role_name, description)
VALUES
('USER', N'Người dùng thông thường'),
('ADMIN', N'Quản trị viên hệ thống'),
('COORDINATOR', N'Người điều phối vật tư');
GO

-- 4. CATEGORIES
CREATE TABLE categories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX),
    parent_id BIGINT,

    CONSTRAINT FK_categories_parent
        FOREIGN KEY (parent_id) REFERENCES categories(id)
);
GO

-- 5. UNITS
CREATE TABLE units (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    unit_name NVARCHAR(100) NOT NULL,
    symbol NVARCHAR(20) NOT NULL UNIQUE
);
GO

-- 6. ITEMS
CREATE TABLE items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    category_id BIGINT NOT NULL,
    unit_id BIGINT NOT NULL,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT FK_items_categories
        FOREIGN KEY (category_id) REFERENCES categories(id),

    CONSTRAINT FK_items_units
        FOREIGN KEY (unit_id) REFERENCES units(id),

    CONSTRAINT CK_items_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

-- 7. DONATIONS
CREATE TABLE donations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    donation_code VARCHAR(50) NOT NULL UNIQUE,
    pickup_address NVARCHAR(500) NOT NULL,
    donation_type VARCHAR(30) NOT NULL DEFAULT 'DONATED',
    note NVARCHAR(MAX),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    approved_at DATETIME2,

    CONSTRAINT FK_donations_donor
        FOREIGN KEY (donor_id) REFERENCES users(id),

    CONSTRAINT FK_donations_approver
        FOREIGN KEY (approved_by) REFERENCES users(id),

    CONSTRAINT CK_donations_type
        CHECK (donation_type IN ('DONATED', 'SPONSORED', 'PROVIDED')),

    CONSTRAINT CK_donations_status
        CHECK (status IN (
            'PENDING', 'APPROVED', 'REJECTED',
            'CANCELLED', 'COMPLETED'
        ))
);
GO

-- 8. DONATION_ITEMS
CREATE TABLE donation_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    donation_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    offered_quantity DECIMAL(12,2) NOT NULL,
    accepted_quantity DECIMAL(12,2) NOT NULL DEFAULT 0,
    reserved_quantity DECIMAL(12,2) NOT NULL DEFAULT 0,
    delivered_quantity DECIMAL(12,2) NOT NULL DEFAULT 0,
    item_condition VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT FK_donation_items_donations
        FOREIGN KEY (donation_id) REFERENCES donations(id),

    CONSTRAINT FK_donation_items_items
        FOREIGN KEY (item_id) REFERENCES items(id),

    CONSTRAINT CK_donation_items_quantity
        CHECK (
            offered_quantity > 0
            AND accepted_quantity >= 0
            AND reserved_quantity >= 0
            AND delivered_quantity >= 0
            AND reserved_quantity + delivered_quantity <= accepted_quantity
        )
);
GO

-- 9. DONATION_IMAGES
CREATE TABLE donation_images (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    donation_id BIGINT NOT NULL,
    image_url NVARCHAR(1000) NOT NULL,
    file_name NVARCHAR(255),
    uploaded_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_donation_images_donations
        FOREIGN KEY (donation_id) REFERENCES donations(id)
);
GO

-- 10. ASSISTANCE_REQUESTS
CREATE TABLE assistance_requests (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    request_code VARCHAR(50) NOT NULL UNIQUE,
    title NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX),
    delivery_address NVARCHAR(500) NOT NULL,
    priority VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    approved_at DATETIME2,

    CONSTRAINT FK_assistance_requests_recipient
        FOREIGN KEY (recipient_id) REFERENCES users(id),

    CONSTRAINT FK_assistance_requests_approver
        FOREIGN KEY (approved_by) REFERENCES users(id),

    CONSTRAINT CK_assistance_requests_priority
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'URGENT')),

    CONSTRAINT CK_assistance_requests_status
        CHECK (status IN (
            'PENDING', 'APPROVED', 'PARTIALLY_APPROVED',
            'REJECTED', 'ALLOCATING', 'FULFILLING',
            'COMPLETED', 'CANCELLED'
        ))
);
GO

-- 11. REQUEST_ITEMS
CREATE TABLE request_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    request_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    requested_quantity DECIMAL(12,2) NOT NULL,
    approved_quantity DECIMAL(12,2) NOT NULL DEFAULT 0,
    note NVARCHAR(MAX),

    CONSTRAINT FK_request_items_requests
        FOREIGN KEY (request_id) REFERENCES assistance_requests(id),

    CONSTRAINT FK_request_items_items
        FOREIGN KEY (item_id) REFERENCES items(id),

    CONSTRAINT CK_request_items_quantity
        CHECK (
            requested_quantity > 0
            AND approved_quantity >= 0
            AND approved_quantity <= requested_quantity
        )
);
GO

-- 12. ALLOCATIONS
CREATE TABLE allocations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    allocation_code VARCHAR(50) NOT NULL UNIQUE,
    request_id BIGINT NOT NULL,
    coordinator_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    allocated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    note NVARCHAR(MAX),

    CONSTRAINT FK_allocations_requests
        FOREIGN KEY (request_id) REFERENCES assistance_requests(id),

    CONSTRAINT FK_allocations_coordinator
        FOREIGN KEY (coordinator_id) REFERENCES users(id),

    CONSTRAINT CK_allocations_status
        CHECK (status IN (
            'PENDING', 'APPROVED', 'CANCELLED',
            'IN_PROGRESS', 'COMPLETED'
        ))
);
GO

-- 13. ALLOCATION_ITEMS
CREATE TABLE allocation_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    allocation_id BIGINT NOT NULL,
    request_item_id BIGINT NOT NULL,
    donation_item_id BIGINT NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,

    CONSTRAINT FK_allocation_items_allocations
        FOREIGN KEY (allocation_id) REFERENCES allocations(id),

    CONSTRAINT FK_allocation_items_request_items
        FOREIGN KEY (request_item_id) REFERENCES request_items(id),

    CONSTRAINT FK_allocation_items_donation_items
        FOREIGN KEY (donation_item_id) REFERENCES donation_items(id),

    CONSTRAINT CK_allocation_items_quantity
        CHECK (quantity > 0)
);
GO
-- 14. SHIPPING_PARTNERS
CREATE TABLE shipping_partners (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    partner_name NVARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    website NVARCHAR(500),
    api_endpoint NVARCHAR(1000),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT CK_shipping_partners_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

-- 15. SHIPMENTS
CREATE TABLE shipments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    shipment_code VARCHAR(50) NOT NULL UNIQUE,
    partner_id BIGINT,
    tracking_code VARCHAR(100),
    tracking_url NVARCHAR(1000),
    pickup_address NVARCHAR(500) NOT NULL,
    delivery_address NVARCHAR(500) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    picked_up_at DATETIME2,
    delivered_at DATETIME2,

    CONSTRAINT FK_shipments_partners
        FOREIGN KEY (partner_id) REFERENCES shipping_partners(id),

    CONSTRAINT CK_shipments_status
        CHECK (status IN (
            'PENDING', 'BOOKED', 'PICKED_UP',
            'IN_TRANSIT', 'OUT_FOR_DELIVERY',
            'DELIVERED', 'FAILED', 'CANCELLED', 'RETURNED'
        ))
);
GO

-- 16. SHIPMENT_ITEMS
CREATE TABLE shipment_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    allocation_item_id BIGINT NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,

    CONSTRAINT FK_shipment_items_shipments
        FOREIGN KEY (shipment_id) REFERENCES shipments(id),

    CONSTRAINT FK_shipment_items_allocation_items
        FOREIGN KEY (allocation_item_id) REFERENCES allocation_items(id),

    CONSTRAINT CK_shipment_items_quantity
        CHECK (quantity > 0)
);
GO

-- 17. SHIPMENT_EVENTS
CREATE TABLE shipment_events (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    description NVARCHAR(MAX),
    location NVARCHAR(500),
    updated_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_shipment_events_shipments
        FOREIGN KEY (shipment_id) REFERENCES shipments(id),

    CONSTRAINT FK_shipment_events_users
        FOREIGN KEY (updated_by) REFERENCES users(id)
);
GO

-- 18. DELIVERY_CONFIRMATIONS
CREATE TABLE delivery_confirmations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    shipment_id BIGINT NOT NULL UNIQUE,
    recipient_id BIGINT NOT NULL,
    received_by NVARCHAR(150) NOT NULL,
    confirmation_code VARCHAR(100),
    image_url NVARCHAR(1000),
    note NVARCHAR(MAX),
    confirmed_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_delivery_confirmations_shipments
        FOREIGN KEY (shipment_id) REFERENCES shipments(id),

    CONSTRAINT FK_delivery_confirmations_users
        FOREIGN KEY (recipient_id) REFERENCES users(id)
);
GO

-- 19. COLLECTION_POINTS
CREATE TABLE collection_points (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    address NVARCHAR(500) NOT NULL,
    contact_person NVARCHAR(150),
    phone VARCHAR(20),
    start_date DATE,
    end_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT CK_collection_points_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'CLOSED'))
);
GO

-- 20. SUPPLY_MOVEMENTS
CREATE TABLE supply_movements (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    donation_item_id BIGINT NOT NULL,
    movement_type VARCHAR(40) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    from_location_type VARCHAR(30),
    from_location_id BIGINT,
    to_location_type VARCHAR(30),
    to_location_id BIGINT,
    reference_type VARCHAR(40),
    reference_id BIGINT,
    created_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_supply_movements_donation_items
        FOREIGN KEY (donation_item_id) REFERENCES donation_items(id),

    CONSTRAINT FK_supply_movements_users
        FOREIGN KEY (created_by) REFERENCES users(id),

    CONSTRAINT CK_supply_movements_quantity
        CHECK (quantity > 0)
);
GO

-- 21. ATTACHMENTS
CREATE TABLE attachments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    owner_type VARCHAR(50) NOT NULL,
    owner_id BIGINT NOT NULL,
    file_url NVARCHAR(1000) NOT NULL,
    file_type VARCHAR(50),
    uploaded_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_attachments_users
        FOREIGN KEY (uploaded_by) REFERENCES users(id)
);
GO

-- -- Tài khoản
-- CREATE INDEX IX_users_email
-- ON users(email);

-- -- Nguồn cung
-- CREATE INDEX IX_donations_donor
-- ON donations(donor_id);

-- CREATE INDEX IX_donations_status
-- ON donations(status);

-- CREATE INDEX IX_donation_items_donation
-- ON donation_items(donation_id);

-- CREATE INDEX IX_donation_items_item
-- ON donation_items(item_id);

-- -- Ảnh minh chứng
-- CREATE INDEX IX_donation_images_donation
-- ON donation_images(donation_id);

-- -- Yêu cầu hỗ trợ
-- CREATE INDEX IX_assistance_requests_recipient
-- ON assistance_requests(recipient_id);

-- CREATE INDEX IX_assistance_requests_status
-- ON assistance_requests(status);

-- CREATE INDEX IX_request_items_request
-- ON request_items(request_id);

-- -- Phân bổ
-- CREATE INDEX IX_allocations_request
-- ON allocations(request_id);

-- CREATE INDEX IX_allocation_items_donation
-- ON allocation_items(donation_item_id);

-- CREATE INDEX IX_allocation_items_request
-- ON allocation_items(request_item_id);

-- -- Vận chuyển
-- CREATE INDEX IX_shipments_status
-- ON shipments(status);

-- CREATE INDEX IX_shipments_tracking
-- ON shipments(tracking_code);

-- CREATE INDEX IX_shipment_events_shipment
-- ON shipment_events(shipment_id, created_at);

-- CREATE INDEX IX_shipment_items_shipment
-- ON shipment_items(shipment_id);

-- -- Lịch sử biến động vật tư
-- CREATE INDEX IX_supply_movements_donation_item
-- ON supply_movements(donation_item_id, created_at);
-- GO

-- Đơn vị tính
INSERT INTO units (unit_name, symbol)
VALUES
(N'Thùng', N'thùng'),
(N'Kilogram', N'kg'),
(N'Cái', N'cái'),
(N'Hộp', N'hộp'),
(N'Chai', N'chai'),
(N'Bộ', N'bộ'),
(N'Lít', N'lít');

-- Danh mục
INSERT INTO categories (name, description)
VALUES
(N'Thực phẩm', N'Các loại thực phẩm thiết yếu'),
(N'Nước uống', N'Nước uống và đồ uống'),
(N'Y tế', N'Vật tư và dụng cụ y tế'),
(N'Quần áo', N'Quần áo và đồ dùng cá nhân'),
(N'Đồ dùng sinh hoạt', N'Các vật dụng thiết yếu');

-- Vật tư mẫu
INSERT INTO items (category_id, unit_id, name, description)
VALUES
(1, 1, N'Mì ăn liền', N'Mì ăn liền đóng thùng'),
(1, 2, N'Gạo', N'Gạo đóng bao'),
(2, 5, N'Nước uống đóng chai', N'Nước uống đóng chai'),
(3, 4, N'Khẩu trang y tế', N'Khẩu trang y tế đóng hộp'),
(4, 6, N'Quần áo', N'Quần áo sử dụng được');
GO