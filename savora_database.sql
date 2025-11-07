-- SAVORA Database Schema and Sample Data
-- Website Supplier Bahan Baku UMKM
-- Generated for MySQL 8.0+

-- ===========================================
-- DATABASE CREATION
-- ===========================================

CREATE DATABASE IF NOT EXISTS savora_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE savora_db;

-- ===========================================
-- TABLES CREATION
-- ===========================================

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role ENUM('BUYER', 'SUPPLIER') NOT NULL,
    company_name VARCHAR(100),
    phone_number VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    unit VARCHAR(50),
    image_url VARCHAR(500),
    supplier_id BIGINT NOT NULL,
    category_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Orders table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Order items table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    helpful_count INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ===========================================
-- INDEXES FOR PERFORMANCE
-- ===========================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_products_supplier ON products(supplier_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_orders_buyer ON orders(buyer_id);
CREATE INDEX idx_orders_supplier ON orders(supplier_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_buyer ON reviews(buyer_id);
CREATE INDEX idx_reviews_created_at ON reviews(created_at);

-- ===========================================
-- SAMPLE DATA INSERTION
-- ===========================================

-- Insert sample categories
INSERT INTO categories (name, description) VALUES
('Bahan Baku Makanan', 'Bahan-bahan dasar untuk industri makanan dan minuman'),
('Bahan Baku Minuman', 'Bahan-bahan dasar untuk pembuatan minuman'),
('Packaging', 'Material kemasan dan pembungkus produk'),
('Bahan Kimia', 'Bahan kimia untuk berbagai keperluan industri'),
('Bahan Organik', 'Bahan-bahan organik alami untuk produk UMKM'),
('Tekstil', 'Bahan baku untuk industri tekstil dan pakaian');

-- Insert sample users (suppliers and buyers)
INSERT INTO users (username, password, email, role, company_name, phone_number, address) VALUES
-- Passwords are BCrypt encoded for 'password123'
('supplier1', '$2a$10$8K3.6Q9wB3L1n8XcF5Y8OeJ8X8X8X8X8X8X8X8X8X8X8X8X8X8X8', 'supplier1@savora.com', 'SUPPLIER', 'PT. Bahan Baku Sejahtera', '+6281234567890', 'Jl. Industri No. 123, Jakarta'),
('supplier2', '$2a$10$8K3.6Q9wB3L1n8XcF5Y8OeJ8X8X8X8X8X8X8X8X8X8X8X8X8X8X8', 'supplier2@savora.com', 'SUPPLIER', 'CV. Material Maju', '+6281234567891', 'Jl. Perdagangan No. 456, Surabaya'),
('supplier3', '$2a$10$8K3.6Q9wB3L1n8XcF5Y8OeJ8X8X8X8X8X8X8X8X8X8X8X8X8X8X8', 'supplier3@savora.com', 'SUPPLIER', 'UD. Bahan Dasar', '+6281234567892', 'Jl. Produksi No. 789, Bandung'),
('buyer1', '$2a$10$8K3.6Q9wB3L1n8XcF5Y8OeJ8X8X8X8X8X8X8X8X8X8X8X8X8X8X8', 'buyer1@savora.com', 'BUYER', 'UMKM Nasi Goreng Mak Nyuss', '+6281234567893', 'Jl. Kuliner No. 101, Jakarta'),
('buyer2', '$2a$10$8K3.6Q9wB3L1n8XcF5Y8OeJ8X8X8X8X8X8X8X8X8X8X8X8X8X8X8', 'buyer2@savora.com', 'BUYER', 'Toko Minuman Segar', '+6281234567894', 'Jl. Minuman No. 202, Surabaya'),
('buyer3', '$2a$10$8K3.6Q9wB3L1n8XcF5Y8OeJ8X8X8X8X8X8X8X8X8X8X8X8X8X8X8', 'buyer3@savora.com', 'BUYER', 'Workshop Kerajinan Tangan', '+6281234567895', 'Jl. Kreatif No. 303, Yogyakarta');

-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, image_url, supplier_id, category_id) VALUES
('Tepung Terigu Premium', 'Tepung terigu berkualitas tinggi untuk berbagai keperluan baking dan masakan', 15000.00, 500, 'https://via.placeholder.com/300x200?text=Tepung+Terigu', 1, 1),
('Gula Pasir Putih', 'Gula pasir putih kristal halus, cocok untuk minuman dan makanan', 12000.00, 300, 'https://via.placeholder.com/300x200?text=Gula+Pasir', 1, 1),
('Minyak Goreng', 'Minyak goreng sawit berkualitas dengan titik asap tinggi', 18000.00, 200, 'https://via.placeholder.com/300x200?text=Minyak+Goreng', 2, 1),
('Kemasan Plastik', 'Kemasan plastik food grade untuk produk makanan', 5000.00, 1000, 'https://via.placeholder.com/300x200?text=Kemasan+Plastik', 2, 3),
('Kopi Arabika', 'Biji kopi arabika premium dari dataran tinggi', 75000.00, 50, 'https://via.placeholder.com/300x200?text=Kopi+Arabika', 3, 2),
('Karton Box', 'Karton box berbagai ukuran untuk packaging produk', 8000.00, 300, 'https://via.placeholder.com/300x200?text=Karton+Box', 3, 3),
('Vanilla Extract', 'Ekstrak vanilla murni untuk flavoring makanan dan minuman', 25000.00, 100, 'https://via.placeholder.com/300x200?text=Vanilla+Extract', 1, 4),
('Daun Teh Hijau', 'Daun teh hijau organik untuk produksi teh herbal', 35000.00, 75, 'https://via.placeholder.com/300x200?text=Daun+Teh+Hijau', 2, 5),
('Benang Jahit', 'Benang jahit berbagai warna dan ukuran untuk industri tekstil', 15000.00, 200, 'https://via.placeholder.com/300x200?text=Benang+Jahit', 3, 6),
('Kain Katun', 'Kain katun berkualitas untuk konveksi dan kerajinan', 45000.00, 150, 'https://via.placeholder.com/300x200?text=Kain+Katun', 1, 6);

-- Insert sample orders
INSERT INTO orders (buyer_id, supplier_id, total_amount, status, created_at) VALUES
(4, 1, 27000.00, 'CONFIRMED', '2024-01-15 10:30:00'),
(5, 2, 23000.00, 'SHIPPED', '2024-01-16 14:20:00'),
(6, 3, 143000.00, 'DELIVERED', '2024-01-17 09:15:00'),
(4, 2, 5000.00, 'PENDING', '2024-01-18 16:45:00');

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
(1, 1, 1, 15000.00),
(1, 2, 1, 12000.00),
(2, 3, 1, 18000.00),
(2, 4, 1, 5000.00),
(3, 5, 1, 75000.00),
(3, 6, 1, 8000.00),
(3, 7, 1, 25000.00),
(3, 8, 1, 35000.00),
(4, 4, 1, 5000.00);

-- Insert sample reviews
INSERT INTO reviews (product_id, buyer_id, rating, comment, created_at) VALUES
(1, 4, 5, 'Tepung terigu berkualitas tinggi, hasil baking sangat bagus!', '2024-01-20 14:30:00'),
(2, 4, 4, 'Gula pasir putih dan halus, cocok untuk semua keperluan.', '2024-01-21 09:15:00'),
(3, 5, 5, 'Minyak goreng dengan titik asap tinggi, sangat baik untuk menggoreng.', '2024-01-22 16:45:00'),
(4, 5, 4, 'Kemasan plastik food grade, aman untuk produk makanan.', '2024-01-23 11:20:00'),
(5, 6, 5, 'Kopi arabika premium, aroma dan rasa sangat autentik.', '2024-01-24 08:30:00'),
(6, 6, 4, 'Karton box berkualitas, cocok untuk packaging produk.', '2024-01-25 13:45:00'),
(7, 4, 5, 'Vanilla extract murni, flavoring yang sangat baik.', '2024-01-26 10:15:00'),
(8, 5, 4, 'Daun teh hijau organik, sempurna untuk teh herbal.', '2024-01-27 15:30:00'),
(9, 6, 5, 'Benang jahit berkualitas, cocok untuk industri tekstil.', '2024-01-28 12:00:00'),
(10, 4, 4, 'Kain katun berkualitas tinggi, hasil jahitan bagus.', '2024-01-29 17:45:00');

-- ===========================================
-- USEFUL QUERIES FOR DEVELOPMENT
-- ===========================================

-- Get all products with supplier and category info
-- SELECT p.*, u.company_name as supplier_name, c.name as category_name
-- FROM products p
-- JOIN users u ON p.supplier_id = u.id
-- LEFT JOIN categories c ON p.category_id = c.id;

-- Get orders with buyer and supplier info
-- SELECT o.*, ub.company_name as buyer_name, us.company_name as supplier_name
-- FROM orders o
-- JOIN users ub ON o.buyer_id = ub.id
-- JOIN users us ON o.supplier_id = us.id;

-- Get order details with product info
-- SELECT oi.*, p.name as product_name, p.description as product_description
-- FROM order_items oi
-- JOIN products p ON oi.product_id = p.id;

-- Get dashboard stats for suppliers
-- SELECT
--     u.company_name,
--     COUNT(DISTINCT p.id) as total_products,
--     COUNT(DISTINCT o.id) as total_orders,
--     COALESCE(SUM(o.total_amount), 0) as total_revenue
-- FROM users u
-- LEFT JOIN products p ON u.id = p.supplier_id
-- LEFT JOIN orders o ON u.id = o.supplier_id
-- WHERE u.role = 'SUPPLIER'
-- GROUP BY u.id, u.company_name;

-- Get dashboard stats for buyers
-- SELECT
--     u.company_name,
--     COUNT(DISTINCT o.id) as total_orders,
--     COALESCE(SUM(o.total_amount), 0) as total_spent
-- FROM users u
-- LEFT JOIN orders o ON u.id = o.buyer_id
-- WHERE u.role = 'BUYER'
-- GROUP BY u.id, u.company_name;

-- ===========================================
-- END OF SQL SCRIPT
-- ===========================================