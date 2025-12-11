-- V1__init_schema.sql

-- 1. 创建用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100)
);

-- 2. 创建订单表 (关联用户)
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL,
    amount NUMERIC(10, 2),
    user_id BIGINT REFERENCES users(id) -- 外键关联
);

-- 3. 插入一点初始化数据
INSERT INTO users (username, email) VALUES ('admin', 'admin@example.com');