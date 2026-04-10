# 📡 MBRL - APIs Resumidas

## 🔐 AUTENTICACIÓN (9 APIs)

| # | Endpoint | Método | Auth | Datos Necesarios | Datos de Respuesta |
|---|----------|--------|------|------------------|--------------------|
| 1 | `/api/auth/register` | POST | ❌ | name, email, password, phone | id, name, email |
| 2 | `/api/auth/login` | POST | ❌ | email, password | accessToken, refreshToken, id, role |
| 3 | `/api/auth/refresh` | POST | ❌ | refreshToken (query) | accessToken, refreshToken |
| 4 | `/api/auth/logout` | POST | ✅ | accessToken, refreshToken (query opt) | null |
| 5 | `/api/auth/change-password` | POST | ✅ | userId (query), oldPassword, newPassword, confirmPassword | null |
| 6 | `/api/auth/password-status/{userId}` | GET | ✅ | userId (path) | changeRequired, daysRemaining, expirationDays |
| 7 | `/api/auth/forgot-password` | POST | ❌ | email (query) | null (siempre 200 por seguridad) |
| 8 | `/api/auth/validate-reset-token` | GET | ❌ | token (query) | boolean (true/false) |
| 9 | `/api/auth/reset-password` | POST | ❌ | token, newPassword, confirmPassword | null |

---

## 🛍️ PRODUCTOS (7 APIs)

| # | Endpoint | Método | Auth | Datos Necesarios | Datos de Respuesta |
|---|----------|--------|------|------------------|--------------------|
| 10 | `/api/products` | GET | ✅ | - | [id, name, price, stock, category] |
| 11 | `/api/products/{id}` | GET | ✅ | id (path) | id, name, description, price, stock, imageUrl |
| 12 | `/api/products/low-stock` | GET | ✅ ADMIN | - | [id, name, price, stock, minStock] |
| 13 | `/api/products/{id}/related` | GET | ✅ | id (path), limit (query opt, def:5) | [relacionados] |
| 14 | `/api/products` | POST | ✅ ADMIN | name, description, price, costPrice, stock, minStock, brandId, categoryId | id, name, price, stock |
| 15 | `/api/products/{id}` | PUT | ✅ ADMIN | id (path), todos los campos anteriores | id, name, price |
| 16 | `/api/products/{id}` | DELETE | ✅ ADMIN | id (path) | null |

---

## 📦 INVENTARIO (5 APIs)

| # | Endpoint | Método | Auth | Datos Necesarios | Datos de Respuesta |
|---|----------|--------|------|------------------|--------------------|
| 17 | `/api/inventory/movements/{productId}` | GET | ✅ ADMIN | productId (path) | [id, movementType, quantity, reason, createdAt] |
| 18 | `/api/inventory/adjust` | POST | ✅ ADMIN | productId, newQuantity, reason (query) | null |
| 19 | `/api/inventory/add-stock` | POST | ✅ ADMIN | productId, quantity, referenceType opt, notes opt (query) | null |
| 20 | `/api/inventory/remove-stock` | POST | ✅ ADMIN | productId, quantity, reason (query) | null |
| 21 | `/api/inventory/available-stock/{productId}` | GET | ✅ ADMIN | productId (path) | cantidad (integer) |

---

## 🛒 ÓRDENES (5 APIs)

| # | Endpoint | Método | Auth | Datos Necesarios | Datos de Respuesta |
|---|----------|--------|------|------------------|--------------------|
| 22 | `/api/orders` | POST | ✅ | customerId, shippingAddress, items[{productId, quantity, unitPrice}] | id, status, totalAmount, createdAt |
| 23 | `/api/orders/{id}` | GET | ✅ | id (path) | id, customerId, status, totalAmount, items |
| 24 | `/api/orders` | GET | ✅ | customerId opt (query) | [órdenes] |
| 25 | `/api/orders/{id}/status` | PUT | ✅ ADMIN | id (path), status (query) PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED | null |
| 26 | `/api/orders/{id}` | DELETE | ✅ ADMIN | id (path) | null |

---

## 📊 REPORTES (6 APIs)

| # | Endpoint | Método | Auth | Datos Necesarios | Datos de Respuesta |
|---|----------|--------|------|------------------|--------------------|
| 27 | `/api/reports/sales` | GET | ✅ ADMIN | startDate, endDate (YYYY-MM-DD query) | [date, totalSales, ordersCount, averageOrderValue] |
| 28 | `/api/reports/top-products` | GET | ✅ ADMIN | startDate, endDate, limit opt (query) | [productId, productName, quantitySold, totalRevenue, rank] |
| 29 | `/api/reports/low-stock` | GET | ✅ ADMIN | - | [productId, productName, currentStock, minStock, stockStatus] |
| 30 | `/api/reports/profit` | GET | ✅ ADMIN | startDate, endDate (query) | ganancia (decimal) |
| 31 | `/api/reports/revenue` | GET | ✅ ADMIN | startDate, endDate (query) | ingresos (decimal) |
| 32 | `/api/reports/dashboard` | GET | ✅ ADMIN | startDate, endDate (query) | totalSales, totalProfit, ordersCount, topProduct, lowStockAlerts |

---

## 📌 NOTAS IMPORTANTES

### Códigos de Estado HTTP
- `200` → Éxito (GET, POST exitoso, PUT, DELETE)
- `201` → Creado (POST /register)
- `400` → Error de validación
- `401` → No autenticado / Credenciales inválidas
- `403` → Acceso denegado (falta rol ADMIN)
- `404` → No encontrado

### Autenticación
- ✅ = Requiere Bearer Token: `Authorization: Bearer {accessToken}`
- ✅ ADMIN = Solo rol ADMIN
- ❌ = Sin autenticación

### Tipos de Datos Comunes
- **Contraseña:** Mínimo 8 caracteres
- **Email:** Formato válido (usuario@dominio.com)
- **Fechas:** YYYY-MM-DD
- **Dinero:** 2 decimales máximo
- **Tokens:** JWT (eyJ...)

### Token JWT
- **Access Token:** Expira en 24 horas (86400000 ms)
- **Refresh Token:** Expira en 7 días
- Usar `/api/auth/refresh` para renovar antes de expirar

---

## 🔑 Ejemplo de Flujo Completo

```
1. REGISTRO
   POST /api/auth/register
   {"name": "Juan", "email": "juan@test.com", "password": "Pass123!", "phone": "+52123"}
   ✓ Respuesta: {id: 1, ...}

2. LOGIN
   POST /api/auth/login
   {"email": "juan@test.com", "password": "Pass123!"}
   ✓ Respuesta: {accessToken: "eyJ...", refreshToken: "eyJ...", id: 1, role: "USER"}

3. USAR API PROTEGIDA
   GET /api/products
   Header: "Authorization: Bearer eyJ..."
   ✓ Respuesta: [{id: 1, name: "Sofá", price: 1299.99}]

4. CREAR ORDEN
   POST /api/orders
   Header: "Authorization: Bearer eyJ..."
   Body: {customerId: 1, shippingAddress: "Calle 123", items: [{productId: 1, quantity: 1, unitPrice: 1299.99}]}
   ✓ Respuesta: {id: 100, totalAmount: 1299.99, status: "PENDING"}

5. LOGOUT
   POST /api/auth/logout
   Header: "Authorization: Bearer eyJ..."
   Query: ?accessToken=eyJ...&refreshToken=eyJ...
   ✓ Respuesta: {success: true, message: "Logout exitoso"}
```

---

## 📈 Estadísticas

- **Total APIs:** 32
- **Públicas:** 3 (register, login, reset-password)
- **Autenticadas:** 29
- **Solo ADMIN:** 17
- **Respuesta estándar:** ApiResponse<T> con {success, data, message, errorCode}

---

## 🚀 Configuración en application.properties

```properties
# JWT (ya configurado con clave segura)
jwt.secret=mySecretKeyForJWTTokenGenerationThatIsNowMuchLongerAndBetterToEnsureHasAtLeast512BitsForHS512AlgorithmSecurityStandard
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# Base de Datos
spring.datasource.url=jdbc:mysql://localhost:3306/mbl
spring.datasource.username=root
spring.datasource.password=Tokai

# Email (configurar con tu SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-contraseña-app

# Reset Password URL
app.reset-password-url=http://localhost:3000/reset-password
```

---

**Última actualización:** 2026-04-09
**Estado:** ✅ Completamente funcional

