-- DESTRUCTIVE RESET: running this file deletes the existing CoordinationDB and all its data.
USE master;
GO

IF DB_ID(N'CoordinationDB') IS NOT NULL
BEGIN
    ALTER DATABASE CoordinationDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE CoordinationDB;
END;
GO

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
    citizen_id VARCHAR(20),
    birth_date DATE,
    address NVARCHAR(500),
    ward NVARCHAR(100),
    beneficiary_group NVARCHAR(100),
    situation_description NVARCHAR(MAX),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    updated_at DATETIME2,

    CONSTRAINT CK_users_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED')),

    CONSTRAINT CK_users_citizen_id
        CHECK (citizen_id IS NULL OR (LEN(citizen_id) = 12 AND citizen_id NOT LIKE '%[^0-9]%'))
);
GO

CREATE UNIQUE INDEX UX_users_citizen_id ON users(citizen_id) WHERE citizen_id IS NOT NULL;
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
    note NVARCHAR(MAX),
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
    delivery_condition VARCHAR(30),
    rating INT,
    confirmed_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT FK_delivery_confirmations_shipments
        FOREIGN KEY (shipment_id) REFERENCES shipments(id),

    CONSTRAINT FK_delivery_confirmations_users
        FOREIGN KEY (recipient_id) REFERENCES users(id)
);
GO

-- 22. SOCIAL SERVICES AND CITIZEN REGISTRATIONS
CREATE TABLE social_services (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(150) NOT NULL,
    description NVARCHAR(MAX),
    service_type VARCHAR(50) NOT NULL,
    service_area NVARCHAR(150),
    contact_phone VARCHAR(20),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT CK_social_services_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);
GO

CREATE TABLE service_registrations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    service_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    request_details NVARCHAR(MAX),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_service_registrations_service FOREIGN KEY (service_id) REFERENCES social_services(id),
    CONSTRAINT FK_service_registrations_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT CK_service_registrations_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED'))
);
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_service_registrations_user_created' AND object_id = OBJECT_ID('dbo.service_registrations'))
    CREATE INDEX IX_service_registrations_user_created ON service_registrations(user_id, created_at DESC);
GO

CREATE TABLE staff_registrations (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    department NVARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by BIGINT,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    reviewed_at DATETIME2,
    CONSTRAINT FK_staff_registrations_reviewer FOREIGN KEY (reviewed_by) REFERENCES users(id),
    CONSTRAINT CK_staff_registrations_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
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

-- Indexes for common ownership, status, and audit queries
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_donations_donor_created' AND object_id = OBJECT_ID('dbo.donations'))
    CREATE INDEX IX_donations_donor_created ON donations(donor_id, created_at DESC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_donations_status_created' AND object_id = OBJECT_ID('dbo.donations'))
    CREATE INDEX IX_donations_status_created ON donations(status, created_at DESC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_donation_items_donation' AND object_id = OBJECT_ID('dbo.donation_items'))
    CREATE INDEX IX_donation_items_donation ON donation_items(donation_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_donation_items_item' AND object_id = OBJECT_ID('dbo.donation_items'))
    CREATE INDEX IX_donation_items_item ON donation_items(item_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_assistance_requests_recipient_created' AND object_id = OBJECT_ID('dbo.assistance_requests'))
    CREATE INDEX IX_assistance_requests_recipient_created ON assistance_requests(recipient_id, created_at DESC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_assistance_requests_status_created' AND object_id = OBJECT_ID('dbo.assistance_requests'))
    CREATE INDEX IX_assistance_requests_status_created ON assistance_requests(status, created_at DESC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_request_items_request' AND object_id = OBJECT_ID('dbo.request_items'))
    CREATE INDEX IX_request_items_request ON request_items(request_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_allocations_request' AND object_id = OBJECT_ID('dbo.allocations'))
    CREATE INDEX IX_allocations_request ON allocations(request_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_allocation_items_request_item' AND object_id = OBJECT_ID('dbo.allocation_items'))
    CREATE INDEX IX_allocation_items_request_item ON allocation_items(request_item_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_allocation_items_donation_item' AND object_id = OBJECT_ID('dbo.allocation_items'))
    CREATE INDEX IX_allocation_items_donation_item ON allocation_items(donation_item_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_shipments_status_created' AND object_id = OBJECT_ID('dbo.shipments'))
    CREATE INDEX IX_shipments_status_created ON shipments(status, created_at DESC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_shipment_items_shipment' AND object_id = OBJECT_ID('dbo.shipment_items'))
    CREATE INDEX IX_shipment_items_shipment ON shipment_items(shipment_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_shipment_events_shipment_created' AND object_id = OBJECT_ID('dbo.shipment_events'))
    CREATE INDEX IX_shipment_events_shipment_created ON shipment_events(shipment_id, created_at DESC);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IX_supply_movements_item_created' AND object_id = OBJECT_ID('dbo.supply_movements'))
    CREATE INDEX IX_supply_movements_item_created ON supply_movements(donation_item_id, created_at DESC);
GO
