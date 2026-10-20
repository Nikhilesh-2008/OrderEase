-- OrderEase Database Schema
-- Smart Canteen Management System with Multi-Canteen Campus Support

CREATE DATABASE IF NOT EXISTS orderease;
USE orderease;

-- 1. Users Table (Authentication & Role Management)
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CUSTOMER', 'KITCHEN', 'ADMIN') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    password_hash VARCHAR(64)
);

-- 2. Customer Table (Customer Profile linked to users)
CREATE TABLE IF NOT EXISTS customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    user_id INT UNIQUE,
    CONSTRAINT fk_customer_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 3. Menu Table (Canteen Food Items with Canteen & Category)
CREATE TABLE IF NOT EXISTS menu (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    available BIT(1) DEFAULT 1,
    canteen VARCHAR(100) DEFAULT 'Snacks & Shakes Corner',
    category VARCHAR(50) DEFAULT 'Fast Food'
);

-- 4. Orders Table (Order Header & Status Tracking)
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    token INT,
    customer_id INT,
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PLACED',
    canteen VARCHAR(100) DEFAULT 'General',
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE SET NULL
);

-- 5. Order Items Table (Order Line Items)
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_menu FOREIGN KEY (item_id) REFERENCES menu(item_id) ON DELETE RESTRICT
);

-- College Canteen Menu Seed Data
INSERT IGNORE INTO menu (item_id, item_name, price, available, canteen, category) VALUES
-- Canteen 1: Snacks & Shakes Corner (Fast Food, Milkshakes, Snacks)
(1, 'Crispy Veg Burger', 80.00, 1, 'Snacks & Shakes Corner', 'Fast Food'),
(2, 'Cheese Burger', 95.00, 1, 'Snacks & Shakes Corner', 'Fast Food'),
(3, 'French Fries', 60.00, 1, 'Snacks & Shakes Corner', 'Snacks'),
(4, 'Peri Peri Fries', 75.00, 1, 'Snacks & Shakes Corner', 'Snacks'),
(5, 'Veg Grilled Sandwich', 70.00, 1, 'Snacks & Shakes Corner', 'Fast Food'),
(6, 'Paneer Tikka Roll', 85.00, 1, 'Snacks & Shakes Corner', 'Fast Food'),
(7, 'Crispy Samosa (2 pcs)', 30.00, 1, 'Snacks & Shakes Corner', 'Snacks'),
(8, 'Veg Puff', 25.00, 1, 'Snacks & Shakes Corner', 'Snacks'),
(9, 'Chocolate Thickshake', 80.00, 1, 'Snacks & Shakes Corner', 'Milkshakes'),
(10, 'Oreo Milkshake', 90.00, 1, 'Snacks & Shakes Corner', 'Milkshakes'),
(11, 'Mango Milkshake', 80.00, 1, 'Snacks & Shakes Corner', 'Milkshakes'),
(12, 'Strawberry Milkshake', 85.00, 1, 'Snacks & Shakes Corner', 'Milkshakes'),

-- Canteen 2: Chai & Juice Bar (Tea, Coffee, Fresh Juices)
(13, 'Special Masala Chai', 15.00, 1, 'Chai & Juice Bar', 'Tea & Coffee'),
(14, 'Ginger Elaichi Tea', 20.00, 1, 'Chai & Juice Bar', 'Tea & Coffee'),
(15, 'South Indian Filter Coffee', 25.00, 1, 'Chai & Juice Bar', 'Tea & Coffee'),
(16, 'Cold Coffee with Ice Cream', 55.00, 1, 'Chai & Juice Bar', 'Tea & Coffee'),
(17, 'Fresh Watermelon Juice', 40.00, 1, 'Chai & Juice Bar', 'Fresh Juices'),
(18, 'Mosambi (Sweet Lime) Juice', 50.00, 1, 'Chai & Juice Bar', 'Fresh Juices'),
(19, 'Fresh Orange Juice', 50.00, 1, 'Chai & Juice Bar', 'Fresh Juices'),
(20, 'Lemon Mint Cooler', 35.00, 1, 'Chai & Juice Bar', 'Fresh Juices'),
(21, 'Pineapple Juice', 45.00, 1, 'Chai & Juice Bar', 'Fresh Juices'),

-- Canteen 3: Food Court - Meals & Tiffins (Tiffins, Biryanis, Chapati, Fast Food & Meals)
(22, 'Idli Sambar (3 pcs)', 40.00, 1, 'Food Court (Meals & Tiffins)', 'Tiffins'),
(23, 'Crispy Masala Dosa', 60.00, 1, 'Food Court (Meals & Tiffins)', 'Tiffins'),
(24, 'Puri Bhaji (3 pcs)', 50.00, 1, 'Food Court (Meals & Tiffins)', 'Tiffins'),
(25, 'Veg Dum Biryani', 110.00, 1, 'Food Court (Meals & Tiffins)', 'Biryanis'),
(26, 'Chicken Dum Biryani', 150.00, 1, 'Food Court (Meals & Tiffins)', 'Biryanis'),
(27, 'Chapati with Paneer Curry', 90.00, 1, 'Food Court (Meals & Tiffins)', 'Chapati & Meals'),
(28, 'Chapati with Mixed Veg Curry', 80.00, 1, 'Food Court (Meals & Tiffins)', 'Chapati & Meals'),
(29, 'Veg Fried Rice with Manchurian', 100.00, 1, 'Food Court (Meals & Tiffins)', 'Fast Food & Rice'),
(30, 'Egg Fried Rice', 90.00, 1, 'Food Court (Meals & Tiffins)', 'Fast Food & Rice'),
(31, 'Full South Indian Meals Thali', 110.00, 1, 'Food Court (Meals & Tiffins)', 'Chapati & Meals');
