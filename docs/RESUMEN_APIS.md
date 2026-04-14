# MBRL - Resumen de APIs

## 1. REGISTRO DE CLIENTE
**Endpoint:** `POST /api/auth/register`

### Datos Necesarios:
```json
{
  "name": "Juan Pérez",           // Requerido
  "email": "juan@example.com",    // Requerido
  "password": "SecurePass123!",   // Requerido (mín. 8 caracteres)
  "phone": "+52 5555551234"       // Opcional
}
```

### Respuesta Exitosa (201):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Juan Pérez",
    "email": "juan@example.com",
    "phone": "+52 5555551234"
  },
  "message": "Cliente registrado exitosamente"
}
```

### Respuesta Error (400):
```json
{
  "success": false,
  "data": null,
  "message": "Error de validación",
  "errorCode": 400
}
```

---

## 2. INICIAR SESIÓN
**Endpoint:** `POST /api/auth/login`

### Datos Necesarios:
```json
{
  "email": "juan@example.com",    // Requerido
  "password": "SecurePass123!"    // Requerido
}
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "message": "Login exitoso"
}
```

### Respuesta Error (401):
```json
{
  "success": false,
  "data": null,
  "message": "Credenciales inválidas",
  "errorCode": 401
}
```

---

## 3. REFRESCAR TOKEN
**Endpoint:** `POST /api/auth/refresh`

### Datos Necesarios:
```
Query Parameter:
- refreshToken: Token JWT (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "message": "Token refrescado exitosamente"
}
```

### Respuesta Error (401):
```json
{
  "success": false,
  "data": null,
  "message": "Refresh token inválido o expirado",
  "errorCode": 401
}
```

---

## 4. CERRAR SESIÓN
**Endpoint:** `POST /api/auth/logout`
**Autenticación:** Requerida (Bearer Token)

### Datos Necesarios:
```
Query Parameters (opcionales):
- accessToken: Token a invalidar
- refreshToken: Token a invalidar
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Logout exitoso"
}
```

### Respuesta Error (400):
```json
{
  "success": false,
  "data": null,
  "message": "Error al procesar logout",
  "errorCode": 400
}
```

---

## 5. CAMBIAR CONTRASEÑA
**Endpoint:** `POST /api/auth/change-password?userId=1`
**Autenticación:** Requerida (Bearer Token)

### Datos Necesarios:
```
Query Parameter:
- userId: ID del usuario (requerido)

Body:
{
  "currentPassword": "OldPass123!",    // Requerido
  "newPassword": "NewPass456!",        // Requerido (mín. 8 caracteres)
  "confirmPassword": "NewPass456!"     // Requerido (debe coincidir)
}
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Contraseña actualizada exitosamente"
}
```

### Respuesta Error (400):
```json
{
  "success": false,
  "data": null,
  "message": "Error de validación",
  "errorCode": 400
}
```

---

## 6. OBTENER ESTADO DE CONTRASEÑA
**Endpoint:** `GET /api/auth/password-status/{userId}`
**Autenticación:** Requerida (Bearer Token)

### Datos Necesarios:
```
Path Parameter:
- userId: ID del usuario (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": {
    "changeRequired": false,
    "daysRemaining": 45,
    "expirationDays": 90
  },
  "message": "Estado de contraseña obtenido"
}
```

### Respuesta Error (400):
```json
{
  "success": false,
  "data": null,
  "message": "Error al obtener estado",
  "errorCode": 400
}
```

---

## 7. SOLICITAR RESET DE CONTRASEÑA
**Endpoint:** `POST /api/auth/forgot-password?email=juan@example.com`

### Datos Necesarios:
```
Query Parameter:
- email: Email del usuario (requerido, formato email)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Si el email existe, recibirás instrucciones para resetear tu contraseña"
}
```

**Nota:** Siempre devuelve 200 sin revelar si el email existe (medida de seguridad)

---

## 8. VALIDAR TOKEN DE RESET
**Endpoint:** `GET /api/auth/validate-reset-token?token=abc123def456...`

### Datos Necesarios:
```
Query Parameter:
- token: Token de reset (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": true,
  "message": "Token válido"
}
```

### Respuesta Error (400):
```json
{
  "success": false,
  "data": null,
  "message": "Token inválido o expirado",
  "errorCode": 400
}
```

---

## 9. RESETEAR CONTRASEÑA
**Endpoint:** `POST /api/auth/reset-password`

### Datos Necesarios:
```json
{
  "token": "abc123def456...",         // Requerido (token válido)
  "newPassword": "NewPass789!",       // Requerido (mín. 8 caracteres)
  "confirmPassword": "NewPass789!"    // Requerido (debe coincidir)
}
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Contraseña reseteada exitosamente"
}
```

### Respuesta Error (400):
```json
{
  "success": false,
  "data": null,
  "message": "Error de validación o token inválido",
  "errorCode": 400
}
```

---

# PRODUCTOS - GESTIÓN DE CATÁLOGO

## 10. LISTAR TODOS LOS PRODUCTOS
**Endpoint:** `GET /api/products`
**Autenticación:** Requerida (USER, ADMIN)

### Datos Necesarios:
```
Sin parámetros
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Sofá de Cuero",
      "description": "Sofá cómodo de cuero premium",
      "price": 1299.99,
      "costPrice": 800.00,
      "stock": 15,
      "minStock": 5,
      "imageUrl": "https://...",
      "brand": "Marca Premium",
      "category": "Sofás",
      "isActive": true
    }
  ],
  "message": "Productos obtenidos exitosamente"
}
```

---

## 11. OBTENER PRODUCTO POR ID
**Endpoint:** `GET /api/products/{id}`
**Autenticación:** Requerida (USER, ADMIN)

### Datos Necesarios:
```
Path Parameter:
- id: ID del producto (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sofá de Cuero",
    "description": "Sofá cómodo de cuero premium",
    "price": 1299.99,
    "costPrice": 800.00,
    "stock": 15,
    "minStock": 5,
    "imageUrl": "https://...",
    "brand": "Marca Premium",
    "category": "Sofás",
    "isActive": true
  },
  "message": "Producto obtenido exitosamente"
}
```

---

## 12. OBTENER PRODUCTOS CON STOCK BAJO
**Endpoint:** `GET /api/products/low-stock`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Sin parámetros
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "id": 3,
      "name": "Silla de Escritorio",
      "price": 199.99,
      "stock": 2,
      "minStock": 10
    }
  ],
  "message": "Productos con stock bajo"
}
```

---

## 13. OBTENER PRODUCTOS RELACIONADOS
**Endpoint:** `GET /api/products/{id}/related?limit=5`
**Autenticación:** Requerida (USER, ADMIN)

### Datos Necesarios:
```
Path Parameter:
- id: ID del producto (requerido)

Query Parameter:
- limit: Cantidad de productos (default: 5)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "id": 2,
      "name": "Sofá de Tela",
      "price": 899.99,
      "stock": 8,
      "category": "Sofás"
    }
  ],
  "message": "Productos relacionados obtenidos exitosamente"
}
```

---

## 14. CREAR PRODUCTO
**Endpoint:** `POST /api/products`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```json
{
  "name": "Sofá Moderno",                    // Requerido
  "description": "Sofá de diseño moderno",   // Requerido
  "price": 1599.99,                          // Requerido
  "costPrice": 900.00,                       // Requerido
  "stock": 20,                               // Requerido
  "minStock": 5,                             // Requerido
  "imageUrl": "https://...",                 // Opcional
  "brandId": 1,                              // Requerido
  "categoryId": 1                            // Requerido
}
```

### Respuesta Exitosa (201):
```json
{
  "success": true,
  "data": {
    "id": 10,
    "name": "Sofá Moderno",
    "price": 1599.99,
    "stock": 20,
    "isActive": true
  },
  "message": "Producto creado exitosamente"
}
```

---

## 15. ACTUALIZAR PRODUCTO
**Endpoint:** `PUT /api/products/{id}`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Path Parameter:
- id: ID del producto (requerido)

Body: (mismos campos que crear)
{
  "name": "Sofá Moderno v2",
  "price": 1699.99,
  ...
}
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": {
    "id": 10,
    "name": "Sofá Moderno v2",
    "price": 1699.99
  },
  "message": "Producto actualizado exitosamente"
}
```

---

## 16. ELIMINAR PRODUCTO
**Endpoint:** `DELETE /api/products/{id}`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Path Parameter:
- id: ID del producto (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Producto eliminado exitosamente"
}
```

---

# INVENTARIO - GESTIÓN DE STOCK

## 17. OBTENER MOVIMIENTOS DE INVENTARIO
**Endpoint:** `GET /api/inventory/movements/{productId}`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Path Parameter:
- productId: ID del producto (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "productId": 1,
      "movementType": "PURCHASE",
      "quantity": 10,
      "reason": "Compra inicial",
      "reference": "PO-123",
      "createdAt": "2026-04-08T10:00:00"
    }
  ],
  "message": "Movimientos obtenidos exitosamente"
}
```

---

## 18. AJUSTAR STOCK MANUALMENTE
**Endpoint:** `POST /api/inventory/adjust`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- productId: ID del producto (requerido)
- newQuantity: Nueva cantidad de stock (requerido)
- reason: Motivo del ajuste (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Stock ajustado exitosamente"
}
```

---

## 19. AGREGAR STOCK
**Endpoint:** `POST /api/inventory/add-stock`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- productId: ID del producto (requerido)
- quantity: Cantidad a agregar (requerido)
- referenceType: Tipo de referencia (default: MANUAL)
- notes: Notas adicionales (opcional)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Stock agregado exitosamente"
}
```

---

## 20. REMOVER STOCK
**Endpoint:** `POST /api/inventory/remove-stock`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- productId: ID del producto (requerido)
- quantity: Cantidad a remover (requerido)
- reason: Motivo de la remoción (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Stock removido exitosamente"
}
```

---

## 21. OBTENER STOCK DISPONIBLE
**Endpoint:** `GET /api/inventory/available-stock/{productId}`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Path Parameter:
- productId: ID del producto (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": 15,
  "message": "Stock obtenido exitosamente"
}
```

---

# ÓRDENES - GESTIÓN DE COMPRAS

## 22. CREAR ORDEN
**Endpoint:** `POST /api/orders`
**Autenticación:** Requerida (USER, ADMIN)

### Datos Necesarios:
```json
{
  "customerId": 1,                    // Requerido
  "shippingAddress": "Calle 123",     // Requerido
  "items": [
    {
      "productId": 1,                 // Requerido
      "quantity": 2,                  // Requerido
      "unitPrice": 1299.99            // Requerido
    }
  ]
}
```

### Respuesta Exitosa (201):
```json
{
  "success": true,
  "data": {
    "id": 100,
    "customerId": 1,
    "status": "PENDING",
    "totalAmount": 2599.98,
    "createdAt": "2026-04-08T10:00:00"
  },
  "message": "Orden creada exitosamente"
}
```

---

## 23. OBTENER ORDEN POR ID
**Endpoint:** `GET /api/orders/{id}`
**Autenticación:** Requerida (USER, ADMIN)

### Datos Necesarios:
```
Path Parameter:
- id: ID de la orden (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": {
    "id": 100,
    "customerId": 1,
    "status": "PENDING",
    "totalAmount": 2599.98,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "unitPrice": 1299.99
      }
    ],
    "createdAt": "2026-04-08T10:00:00"
  },
  "message": "Orden obtenida exitosamente"
}
```

---

## 24. LISTAR ÓRDENES
**Endpoint:** `GET /api/orders`
**Autenticación:** Requerida (USER, ADMIN)

### Datos Necesarios:
```
Query Parameter:
- customerId: Filtrar por cliente (opcional)

Nota: ADMIN ve todas las órdenes, USER solo sus órdenes
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "id": 100,
      "customerId": 1,
      "status": "PENDING",
      "totalAmount": 2599.98
    }
  ],
  "message": "Órdenes obtenidas exitosamente"
}
```

---

## 25. ACTUALIZAR ESTADO DE ORDEN
**Endpoint:** `PUT /api/orders/{id}/status`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Path Parameter:
- id: ID de la orden (requerido)

Query Parameter:
- status: Nuevo estado (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Estado de orden actualizado exitosamente"
}
```

---

## 26. CANCELAR ORDEN
**Endpoint:** `DELETE /api/orders/{id}`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Path Parameter:
- id: ID de la orden (requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": null,
  "message": "Orden cancelada exitosamente"
}
```

---

# REPORTES - ANÁLISIS Y MÉTRICAS

## 27. REPORTE DE VENTAS
**Endpoint:** `GET /api/reports/sales?startDate=2026-04-01&endDate=2026-04-30`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- startDate: Fecha inicial (formato: YYYY-MM-DD, requerido)
- endDate: Fecha final (formato: YYYY-MM-DD, requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "date": "2026-04-08",
      "totalSales": 10000.00,
      "ordersCount": 5,
      "averageOrderValue": 2000.00
    }
  ],
  "message": "Reporte de ventas generado exitosamente"
}
```

---

## 28. PRODUCTOS MÁS VENDIDOS
**Endpoint:** `GET /api/reports/top-products?limit=10&startDate=2026-04-01&endDate=2026-04-30`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- limit: Cantidad de productos (default: 10)
- startDate: Fecha inicial (formato: YYYY-MM-DD, requerido)
- endDate: Fecha final (formato: YYYY-MM-DD, requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "productId": 1,
      "productName": "Sofá de Cuero",
      "quantitySold": 50,
      "totalRevenue": 64999.50,
      "rank": 1
    }
  ],
  "message": "Reporte de productos top generado exitosamente"
}
```

---

## 29. REPORTE DE STOCK BAJO
**Endpoint:** `GET /api/reports/low-stock`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Sin parámetros
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": [
    {
      "productId": 3,
      "productName": "Silla de Escritorio",
      "currentStock": 2,
      "minStock": 10,
      "stockStatus": "CRITICAL"
    }
  ],
  "message": "Reporte de stock bajo generado exitosamente"
}
```

---

## 30. CALCULAR GANANCIA POR PERÍODO
**Endpoint:** `GET /api/reports/profit?startDate=2026-04-01&endDate=2026-04-30`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- startDate: Fecha inicial (formato: YYYY-MM-DD, requerido)
- endDate: Fecha final (formato: YYYY-MM-DD, requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": 25000.00,
  "message": "Ganancia calculada exitosamente"
}
```

---

## 31. CALCULAR INGRESOS POR PERÍODO
**Endpoint:** `GET /api/reports/revenue?startDate=2026-04-01&endDate=2026-04-30`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- startDate: Fecha inicial (formato: YYYY-MM-DD, requerido)
- endDate: Fecha final (formato: YYYY-MM-DD, requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": 100000.00,
  "message": "Ingresos calculados exitosamente"
}
```

---

## 32. MÉTRICAS DE DASHBOARD
**Endpoint:** `GET /api/reports/dashboard?startDate=2026-04-01&endDate=2026-04-30`
**Autenticación:** Requerida (ADMIN)

### Datos Necesarios:
```
Query Parameters:
- startDate: Fecha inicial (formato: YYYY-MM-DD, requerido)
- endDate: Fecha final (formato: YYYY-MM-DD, requerido)
```

### Respuesta Exitosa (200):
```json
{
  "success": true,
  "data": {
    "totalSales": 100000.00,
    "totalProfit": 25000.00,
    "ordersCount": 50,
    "averageOrderValue": 2000.00,
    "topProduct": "Sofá de Cuero",
    "lowStockAlerts": 5,
    "customerCount": 20
  },
  "message": "Métricas de dashboard generadas exitosamente"
}
```

---

# TABLA RESUMEN COMPLETA

| # | Endpoint | Método | Autenticación | Descripción |
|---|----------|--------|---------------|-------------|
| 1 | `/api/auth/register` | POST | No | Registrar nuevo cliente |
| 2 | `/api/auth/login` | POST | No | Iniciar sesión (obtener tokens) |
| 3 | `/api/auth/refresh` | POST | No | Refrescar access token |
| 4 | `/api/auth/logout` | POST | Sí | Cerrar sesión (invalidar tokens) |
| 5 | `/api/auth/change-password` | POST | Sí | Cambiar contraseña autenticado |
| 6 | `/api/auth/password-status/{id}` | GET | Sí | Ver estado de contraseña |
| 7 | `/api/auth/forgot-password` | POST | No | Solicitar reset de contraseña |
| 8 | `/api/auth/validate-reset-token` | GET | No | Validar token de reset |
| 9 | `/api/auth/reset-password` | POST | No | Resetear contraseña con token |
| 10 | `/api/products` | GET | Sí | Listar todos los productos |
| 11 | `/api/products/{id}` | GET | Sí | Obtener producto por ID |
| 12 | `/api/products/low-stock` | GET | Sí (ADMIN) | Productos con stock bajo |
| 13 | `/api/products/{id}/related` | GET | Sí | Obtener productos relacionados |
| 14 | `/api/products` | POST | Sí (ADMIN) | Crear producto |
| 15 | `/api/products/{id}` | PUT | Sí (ADMIN) | Actualizar producto |
| 16 | `/api/products/{id}` | DELETE | Sí (ADMIN) | Eliminar producto |
| 17 | `/api/inventory/movements/{id}` | GET | Sí (ADMIN) | Obtener movimientos de inventario |
| 18 | `/api/inventory/adjust` | POST | Sí (ADMIN) | Ajustar stock manualmente |
| 19 | `/api/inventory/add-stock` | POST | Sí (ADMIN) | Agregar stock |
| 20 | `/api/inventory/remove-stock` | POST | Sí (ADMIN) | Remover stock |
| 21 | `/api/inventory/available-stock/{id}` | GET | Sí (ADMIN) | Obtener stock disponible |
| 22 | `/api/orders` | POST | Sí | Crear orden |
| 23 | `/api/orders/{id}` | GET | Sí | Obtener orden por ID |
| 24 | `/api/orders` | GET | Sí | Listar órdenes |
| 25 | `/api/orders/{id}/status` | PUT | Sí (ADMIN) | Actualizar estado de orden |
| 26 | `/api/orders/{id}` | DELETE | Sí (ADMIN) | Cancelar orden |
| 27 | `/api/reports/sales` | GET | Sí (ADMIN) | Reporte de ventas |
| 28 | `/api/reports/top-products` | GET | Sí (ADMIN) | Productos más vendidos |
| 29 | `/api/reports/low-stock` | GET | Sí (ADMIN) | Reporte de stock bajo |
| 30 | `/api/reports/profit` | GET | Sí (ADMIN) | Calcular ganancia |
| 31 | `/api/reports/revenue` | GET | Sí (ADMIN) | Calcular ingresos |
| 32 | `/api/reports/dashboard` | GET | Sí (ADMIN) | Métricas de dashboard |

---

## RESUMEN ESTADÍSTICAS

- **Total de APIs:** 32
- **APIs de Autenticación:** 9
- **APIs de Productos:** 7
- **APIs de Inventario:** 5
- **APIs de Órdenes:** 5
- **APIs de Reportes:** 6

### Por Tipo de Autenticación
- **Públicas (sin autenticación):** 3 (register, login, reset-password)
- **Autenticadas (USER/ADMIN):** 29
- **Solo ADMIN:** 17
