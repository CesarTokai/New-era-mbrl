drop database mbl;

Create database mbl;
use mbl;

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(100) NOT NULL UNIQUE,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. CATEGORÍAS Y MARCAS
-- =====================================================

CREATE TABLE categories (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT,
                            parent_category_id BIGINT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (parent_category_id) REFERENCES categories(id) ON DELETE SET NULL
);

CREATE TABLE brands (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        name VARCHAR(100) NOT NULL UNIQUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. PRODUCTOS
-- =====================================================

CREATE TABLE products (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          brand_id BIGINT,
                          category_id BIGINT,

    -- Precios
                          price DECIMAL(10,2) NOT NULL,
                          cost_price DECIMAL(10,2) NOT NULL,

    -- Inventario
                          stock INT NOT NULL DEFAULT 0,
                          min_stock INT DEFAULT 5,
                          image_url VARCHAR(500),

    -- Atributos físicos
                          color VARCHAR(100),
                          material VARCHAR(100),
                          dimensions VARCHAR(255),

    -- Control
                          is_active BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL,
                          FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,

                          INDEX idx_category (category_id),
                          INDEX idx_brand (brand_id),
                          INDEX idx_stock (stock),
                          INDEX idx_price (price)
);

-- =====================================================
-- 3b. IMÁGENES DE PRODUCTO (máximo 10 por producto)
-- =====================================================

CREATE TABLE product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product (product_id)
);

-- =====================================================
-- 4. HISTORIAL DE PRECIOS (AUDITORÍA)
-- =====================================================

CREATE TABLE price_history (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               product_id BIGINT NOT NULL,
                               old_price DECIMAL(10,2),
                               new_price DECIMAL(10,2) NOT NULL,
                               changed_by BIGINT,
                               changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                               FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                               FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL,

                               INDEX idx_product (product_id),
                               INDEX idx_date (changed_at)
);

-- =====================================================
-- 5. MOVIMIENTOS DE INVENTARIO
-- =====================================================

CREATE TABLE inventory_movements (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     product_id BIGINT NOT NULL,
                                     movement_type ENUM('ENTRADA', 'SALIDA', 'AJUSTE', 'VENTA') NOT NULL,
                                     quantity INT NOT NULL,
                                     reference_type VARCHAR(50),  -- 'ORDER', 'MANUAL', 'ADJUSTMENT'
                                     reference_id BIGINT,
                                     notes TEXT,
                                     created_by BIGINT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                     FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                                     FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,

                                     INDEX idx_product (product_id),
                                     INDEX idx_date (created_at),
                                     INDEX idx_type (movement_type)
);

-- =====================================================
-- 6. CLIENTES
-- =====================================================

CREATE TABLE customers (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT,
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255),
                           phone VARCHAR(20),
                           address TEXT,
                           city VARCHAR(100),
                           state VARCHAR(100),
                           postal_code VARCHAR(20),

    -- Estadísticas
                           total_orders INT DEFAULT 0,
                           total_spent DECIMAL(12,2) DEFAULT 0,
                           last_order_date TIMESTAMP,

                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                           FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

                           INDEX idx_email (email),
                           INDEX idx_phone (phone)
);

-- =====================================================
-- 7. ÓRDENES Y VENTAS
-- =====================================================

CREATE TABLE orders (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        customer_id BIGINT NOT NULL,
                        user_id BIGINT,  -- Quién registró la venta

                        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        delivery_date TIMESTAMP,

    -- Totales
                        subtotal DECIMAL(12,2) NOT NULL DEFAULT 0,
                        tax DECIMAL(12,2) DEFAULT 0,
                        total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,

    -- Estado
                        status ENUM('PENDIENTE', 'CONFIRMADA', 'ENVIADA', 'ENTREGADA', 'CANCELADA') DEFAULT 'PENDIENTE',

    -- Información de entrega
                        shipping_address TEXT,
                        notes TEXT,

                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

                        INDEX idx_customer (customer_id),
                        INDEX idx_date (order_date),
                        INDEX idx_status (status)
);

CREATE TABLE order_items (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,

                             quantity INT NOT NULL,
                             unit_price DECIMAL(10,2) NOT NULL,  -- Precio que se pagó
                             cost_price DECIMAL(10,2) NOT NULL,  -- Costo al momento de la venta
                             total_price DECIMAL(12,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,

                             FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                             FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,

                             INDEX idx_order (order_id),
                             INDEX idx_product (product_id)
);

-- =====================================================
-- 8. PAGOS
-- =====================================================

CREATE TABLE payments (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          order_id BIGINT NOT NULL UNIQUE,

                          amount DECIMAL(10,2) NOT NULL,
                          payment_method ENUM('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'OTRO') NOT NULL,
                          transaction_id VARCHAR(255),
                          status ENUM('PENDIENTE', 'COMPLETADO', 'FALLIDO') DEFAULT 'PENDIENTE',

                          payment_date TIMESTAMP,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,

                          INDEX idx_status (status),
                          INDEX idx_date (payment_date)
);

-- =====================================================
-- 9. DEVOLUCIONES
-- =====================================================

CREATE TABLE returns (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         order_id BIGINT NOT NULL,
                         product_id BIGINT NOT NULL,

                         quantity INT NOT NULL,
                         reason VARCHAR(255),
                         refund_amount DECIMAL(10,2),

                         status ENUM('SOLICITADA', 'APROBADA', 'RECHAZADA', 'COMPLETADA') DEFAULT 'SOLICITADA',

                         requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         resolved_at TIMESTAMP,

                         FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                         FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,

                         INDEX idx_order (order_id),
                         INDEX idx_status (status)
);

-- =====================================================
-- 10. RESEÑAS Y VALORACIONES
-- =====================================================

CREATE TABLE reviews (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         product_id BIGINT NOT NULL,
                         customer_id BIGINT NOT NULL,
                         order_id BIGINT,

                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT,
                         is_approved BOOLEAN DEFAULT FALSE,

                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                         FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
                         FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE,
                         FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL,

                         INDEX idx_product (product_id),
                         INDEX idx_rating (rating)
);

-- =====================================================
-- 11. AUDITORÍA GENERAL
-- =====================================================

CREATE TABLE audit_logs (
                            id BIGINT PRIMARY KEY AUTO_INCREMENT,
                            table_name VARCHAR(100) NOT NULL,
                            operation ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
                            record_id BIGINT,
                            user_id BIGINT,
                            old_values JSON,
                            new_values JSON,
                            operation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

                            INDEX idx_table (table_name),
                            INDEX idx_date (operation_date),
                            INDEX idx_user (user_id)
);

-- =====================================================
-- 12. TRIGGER: REGISTRAR CAMBIOS DE PRECIO
-- =====================================================

DELIMITER //
CREATE TRIGGER tr_product_price_change
    BEFORE UPDATE ON products
    FOR EACH ROW
BEGIN
    IF OLD.price != NEW.price THEN
    INSERT INTO price_history (product_id, old_price, new_price, changed_at)
    VALUES (NEW.id, OLD.price, NEW.price, NOW());
END IF;
END //
DELIMITER ;

-- =====================================================
-- 13. TRIGGER: REGISTRAR MOVIMIENTO AL VENDER
-- =====================================================

DELIMITER //
CREATE TRIGGER tr_order_item_inventory
    AFTER INSERT ON order_items
    FOR EACH ROW
BEGIN
    UPDATE products SET stock = stock - NEW.quantity WHERE id = NEW.product_id;

    INSERT INTO inventory_movements (product_id, movement_type, quantity, reference_type, reference_id)
    VALUES (NEW.product_id, 'VENTA', NEW.quantity, 'ORDER', NEW.order_id);
END //
DELIMITER ;

-- =====================================================
-- 14. DATOS INICIALES
-- =====================================================

-- Usuario admin (contraseña: admin123 - debes hashearla con BCrypt)
INSERT INTO users (username, email, password, role) VALUES
    ('admin', 'admin@muebles.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrJ7Z6qR8ZxUxVZ5YqX6qX6qX6qX6q', 'ADMIN');

-- Marcas comunes
INSERT INTO brands (name) VALUES
                              ('Marca Propia'),
                              ('Otros');

-- Categorías
INSERT INTO categories (name, description) VALUES
                                               ('Sofás', 'Sofás y sillones'),
                                               ('Comedores', 'Mesas y sillas de comedor'),
                                               ('Dormitorios', 'Camas y muebles de dormitorio'),
                                               ('Oficina', 'Muebles de oficina'),
                                               ('Accesorios', 'Accesorios y complementos');

-- =====================================================
-- 15. VISTAS ÚTILES PARA REPORTES
-- =====================================================

-- Vista: Margen de ganancia por producto
CREATE VIEW v_product_margins AS
SELECT
    p.id,
    p.name,
    b.name AS brand,
    c.name AS category,
    p.price,
    p.cost_price,
    (p.price - p.cost_price) AS margen_unitario,
    ROUND(((p.price - p.cost_price) / p.price * 100), 2) AS margen_pct,
    p.stock,
    p.is_active
FROM products p
         LEFT JOIN brands b ON p.brand_id = b.id
         LEFT JOIN categories c ON p.category_id = c.id;

-- Vista: Ganancia por venta
CREATE VIEW v_sales_profit AS
SELECT
    o.id AS order_id,
    o.order_date,
    c.name AS customer_name,
    p.name AS product_name,
    oi.quantity,
    oi.unit_price,
    oi.cost_price,
    (oi.unit_price - oi.cost_price) AS margen_unitario,
    ((oi.unit_price - oi.cost_price) * oi.quantity) AS margen_total,
    oi.total_price
FROM order_items oi
         JOIN orders o ON oi.order_id = o.id
         JOIN products p ON oi.product_id = p.id
         JOIN customers c ON o.customer_id = c.id;

-- Vista: Stock bajo
CREATE VIEW v_low_stock AS
SELECT
    id,
    name,
    stock,
    min_stock,
    (min_stock - stock) AS cantidad_faltante
FROM products
WHERE stock <= min_stock AND is_active = TRUE;

-- =====================================================
-- SETTINGS
-- =====================================================

CREATE TABLE settings (
    `key` VARCHAR(100) PRIMARY KEY,
    `value` LONGTEXT NOT NULL,
    updated_at DATETIME
);
ORDER BY cantidad_faltante DESC;