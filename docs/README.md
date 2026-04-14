# MBRL - Mueblería e-Commerce API

Sistema completo de gestión de tienda de muebles con autenticación JWT, gestión de inventario y reportes.

## 🚀 Requisitos previos

- Java 17+
- MySQL 8.0+
- Maven 3.8+

## 📋 Configuración Inicial

### 1. Crear Base de Datos MySQL

```bash
# Abrir MySQL CLI
mysql -u root -p

# Ejecutar en MySQL:
CREATE DATABASE mbl CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mbl;
```

### 2. Configurar Credenciales en `application.properties`

**Ruta del archivo:**
```
src/main/resources/application.properties
```

**Actualizar con tus credenciales:**
```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/mbl?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=TU_CONTRASEÑA_MYSQL_AQUI
```

> **⚠️ IMPORTANTE:** Reemplaza `TU_CONTRASEÑA_MYSQL_AQUI` con tu contraseña de MySQL

### 3. Cargar Script SQL (Opcional - para datos iniciales)

Si tienes el archivo `MBRL.sql`:

```bash
mysql -u root -p mbl < src/db/MBRL.sql
```

## 🏗️ Compilar y Ejecutar

### Compilar el proyecto:
```bash
./mvnw clean compile
```

### Ejecutar tests:
```bash
./mvnw test
```

### Ejecutar la aplicación:
```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en: **http://localhost:8080**

## 📚 Endpoints Principales

### Autenticación (Público)
- `POST /api/auth/register` - Registrar nuevo cliente
- `POST /api/auth/login` - Login y obtener JWT token

### Productos (Autenticado)
- `GET /api/products` - Listar productos
- `GET /api/products/{id}` - Obtener producto
- `GET /api/products/low-stock` - Productos con stock bajo (Admin)
- `POST /api/products` - Crear producto (Admin)
- `PUT /api/products/{id}` - Actualizar producto (Admin)
- `DELETE /api/products/{id}` - Eliminar producto (Admin)

### Órdenes
- `POST /api/orders` - Crear orden
- `GET /api/orders/{id}` - Obtener orden
- `GET /api/orders` - Listar órdenes (Admin ve todas, User sus órdenes)
- `PUT /api/orders/{id}/status` - Actualizar estado (Admin)

### Clientes (Admin)
- `GET /api/customers` - Listar clientes
- `GET /api/customers/{id}` - Obtener cliente
- `POST /api/customers` - Crear cliente
- `PUT /api/customers/{id}` - Actualizar cliente

### Reportes (Admin)
- `GET /api/reports/sales?startDate=2026-04-01&endDate=2026-04-30` - Ventas por fecha
- `GET /api/reports/top-products?limit=10&startDate=2026-04-01&endDate=2026-04-30` - Productos top
- `GET /api/reports/low-stock` - Alertas stock
- `GET /api/reports/profit?startDate=2026-04-01&endDate=2026-04-30` - Ganancia
- `GET /api/reports/dashboard?startDate=2026-04-01&endDate=2026-04-30` - Dashboard KPIs

### Inventario (Admin)
- `GET /api/inventory/movements/{productId}` - Historial movimientos
- `POST /api/inventory/adjust` - Ajuste manual stock
- `POST /api/inventory/add-stock` - Agregar stock
- `POST /api/inventory/remove-stock` - Remover stock

## 🔐 Autenticación JWT

### Obtener Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer"
  }
}
```

### Usar Token en Requests
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

## 🗂️ Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/mx/mbrl/
│   │   ├── controller/           # REST Controllers
│   │   ├── service/              # Business Logic
│   │   ├── repository/           # JPA Repositories
│   │   ├── entity/               # JPA Entities
│   │   ├── dto/                  # DTOs (Data Transfer Objects)
│   │   ├── security/             # JWT & Spring Security
│   │   └── config/               # Configurations
│   └── resources/
│       └── application.properties # Configuration
└── test/
    └── java/com/mx/mbrl/
        └── MbrlApplicationTests.java
```

## 🗄️ Base de Datos

**Tablas principales:**
- `users` - Usuarios del sistema (admin/user)
- `customers` - Clientes/tiendas
- `products` - Catálogo de muebles
- `categories` - Categorías de productos
- `brands` - Marcas
- `orders` - Órdenes de compra
- `order_items` - Items por orden
- `inventory_movements` - Historial de inventario
- `payments` - Pagos
- `reviews` - Reseñas

## 🔧 Configuración Avanzada

### Cambiar puerto
```properties
server.port=8081
```

### Cambiar dialecto Hibernate
```properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

### Deshabilitar mostrar SQL
```properties
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```

## 🐛 Resolución de Problemas

### Error: "Access denied for user 'root'@'localhost'"
1. Verificar contraseña MySQL
2. Asegurar que MySQL está corriendo: `mysql -u root -p`
3. Crear base de datos: `CREATE DATABASE mbl;`

### Error: "Public Key Retrieval is not allowed"
✅ **Ya configurado en `application.properties`** con:
```
&allowPublicKeyRetrieval=true
```

### Error: "Unable to determine Dialect"
✅ **Ya configurado en `application.properties`** con:
```
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

## 📊 Roles de Usuario

- **ADMIN**: Acceso total (gestión de productos, órdenes, reportes)
- **USER**: Acceso limitado (ver productos, crear órdenes)

## 🔐 Seguridad

- ✅ Contraseñas hasheadas con BCrypt
- ✅ JWT con expiración de 24 horas
- ✅ CORS configurado para localhost:3000 y localhost:4200
- ✅ CSRF deshabilitado (stateless)
- ✅ @PreAuthorize en endpoints sensibles

## 📝 Ejemplos de Uso

### Registrar cliente
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "phone": "5551234567",
    "address": "Calle 123",
    "city": "CDMX",
    "state": "CDMX",
    "postalCode": "28001"
  }'
```

### Crear orden
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "customerId": 1,
    "shippingAddress": "Calle 456",
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "unitPrice": 150.00
      }
    ]
  }'
```

## 📧 Contacto

Para soporte, contacta al equipo de desarrollo.

---

**Última actualización:** 2026-04-07
**Versión:** 1.0.0

