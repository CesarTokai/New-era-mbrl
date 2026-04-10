# MBRL - APIs Completo

## Base URL
```
http://localhost:8080
```

---

## 🔐 AUTENTICACIÓN (AUTH)

**Base Path:** `/api/auth`

### 1. Registrar Usuario
```
POST /api/auth/register
Content-Type: application/json
```
**Request Body:**
```json
{
  "email": "usuario@example.com",
  "password": "password123",
  "name": "Juan Pérez",
  "phone": "5551234567",
  "address": "Calle Principal 123",
  "city": "México",
  "state": "CDMX",
  "postalCode": "06600"
}
```
**Response (201):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "user": { "id": 1, "email": "usuario@example.com", "role": "USER" },
    "name": "Juan Pérez",
    "email": "usuario@example.com",
    "phone": "5551234567",
    "address": "Calle Principal 123",
    "city": "México",
    "state": "CDMX",
    "postalCode": "06600"
  },
  "message": "Cliente registrado exitosamente"
}
```

### 2. Login (Obtener JWT)
```
POST /api/auth/login
Content-Type: application/json
```
**Request Body:**
```json
{
  "email": "usuario@example.com",
  "password": "password123"
}
```
**Response (200):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "refresh_token_here",
    "type": "Bearer",
    "id": 1,
    "username": "usuario",
    "email": "usuario@example.com",
    "role": "USER",
    "expiresIn": 86400000
  },
  "message": "Login exitoso"
}
```

### 3. Refrescar Access Token
```
POST /api/auth/refresh?refreshToken=<refresh_token>
```
**Response (200):** Similar a login, con nuevo accessToken

### 4. Logout
```
POST /api/auth/logout?accessToken=<access_token>&refreshToken=<refresh_token>
```
**Response (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Logout exitoso"
}
```

### 5. Cambiar Contraseña (Autenticado)
```
POST /api/auth/change-password?userId=<user_id>
Content-Type: application/json
Authorization: Bearer <access_token>
```
**Request Body:**
```json
{
  "oldPassword": "password123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

### 6. Estado de Contraseña (Autenticado)
```
GET /api/auth/password-status/<userId>
Authorization: Bearer <access_token>
```
**Response (200):**
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

### 7. Solicitar Reset de Contraseña
```
POST /api/auth/forgot-password?email=usuario@example.com
```

### 8. Validar Token de Reset
```
GET /api/auth/validate-reset-token?token=<reset_token>
```

### 9. Reset de Contraseña
```
POST /api/auth/reset-password
Content-Type: application/json
```
**Request Body:**
```json
{
  "token": "<reset_token>",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

---

## 📦 PRODUCTOS (ADMIN SOLO)

**Base Path:** `/api/products`  
**Autenticación:** Requerida (Bearer Token)  
**Rol Requerido:** USER o ADMIN (lectura), ADMIN (escritura)

### 1. Obtener Todos los Productos
```
GET /api/products
Authorization: Bearer <access_token>
```
**Response (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Silla Ejecutiva",
      "description": "Silla cómoda para oficina",
      "price": 450.50,
      "costPrice": 250.00,
      "stock": 15,
      "minStock": 5,
      "imageUrl": "http://...",
      "brand": "Herman Miller",
      "category": "Muebles",
      "isActive": true
    }
  ],
  "message": "Productos obtenidos exitosamente"
}
```

### 2. Obtener Producto por ID
```
GET /api/products/<id>
Authorization: Bearer <access_token>
```

### 3. Obtener Productos con Stock Bajo (ADMIN)
```
GET /api/products/low-stock
Authorization: Bearer <access_token>
```

### 4. Obtener Productos Relacionados
```
GET /api/products/<id>/related?limit=5
Authorization: Bearer <access_token>
```

### 5. Crear Producto (ADMIN)
```
POST /api/products
Content-Type: application/json
Authorization: Bearer <access_token>
```
**Request Body:**
```json
{
  "name": "Silla Ejecutiva",
  "description": "Silla cómoda para oficina",
  "price": 450.50,
  "costPrice": 250.00,
  "stock": 15,
  "minStock": 5,
  "imageUrl": "http://...",
  "categoryId": 1,
  "brandId": 1
}
```

### 6. Actualizar Producto (ADMIN)
```
PUT /api/products/<id>
Content-Type: application/json
Authorization: Bearer <access_token>
```

### 7. Eliminar Producto (ADMIN)
```
DELETE /api/products/<id>
Authorization: Bearer <access_token>
```

---

## 👥 CLIENTES (ADMIN SOLO)

**Base Path:** `/api/customers`  
**Autenticación:** Requerida (Bearer Token)  
**Rol Requerido:** ADMIN

### 1. Obtener Todos los Clientes
```
GET /api/customers
Authorization: Bearer <access_token>
```

### 2. Obtener Cliente por ID
```
GET /api/customers/<id>
Authorization: Bearer <access_token>
```

### 3. Crear Cliente
```
POST /api/customers
Content-Type: application/json
Authorization: Bearer <access_token>
```
**Request Body:**
```json
{
  "email": "cliente@example.com",
  "password": "password123",
  "name": "María García",
  "phone": "5559876543",
  "address": "Av. Principal 456",
  "city": "Monterrey",
  "state": "NL",
  "postalCode": "64000"
}
```

### 4. Actualizar Cliente
```
PUT /api/customers/<id>
Content-Type: application/json
Authorization: Bearer <access_token>
```

### 5. Obtener Estadísticas del Cliente
```
GET /api/customers/<id>/stats
Authorization: Bearer <access_token>
```

---

## 📦 INVENTARIO (ADMIN SOLO)

**Base Path:** `/api/inventory`  
**Autenticación:** Requerida (Bearer Token)  
**Rol Requerido:** ADMIN

### 1. Obtener Movimientos de Inventario
```
GET /api/inventory/movements/<productId>
Authorization: Bearer <access_token>
```

### 2. Obtener Stock Disponible
```
GET /api/inventory/available-stock/<productId>
Authorization: Bearer <access_token>
```

### 3. Ajustar Stock
```
POST /api/inventory/adjust?productId=<id>&newQuantity=<qty>&reason=<reason>
Authorization: Bearer <access_token>
```

### 4. Agregar Stock
```
POST /api/inventory/add-stock?productId=<id>&quantity=<qty>&referenceType=MANUAL&notes=<notes>
Authorization: Bearer <access_token>
```

### 5. Remover Stock
```
POST /api/inventory/remove-stock?productId=<id>&quantity=<qty>&reason=<reason>
Authorization: Bearer <access_token>
```

---

## 📝 ÓRDENES (ADMIN SOLO)

**Base Path:** `/api/orders`  
**Autenticación:** Requerida (Bearer Token)  
**Rol Requerido:** ADMIN

### 1. Obtener Todas las Órdenes
```
GET /api/orders
Authorization: Bearer <access_token>
```

### 2. Obtener Orden por ID
```
GET /api/orders/<id>
Authorization: Bearer <access_token>
```

### 3. Obtener Órdenes por Cliente
```
GET /api/orders/customer/<customerId>
Authorization: Bearer <access_token>
```

### 4. Crear Orden
```
POST /api/orders
Content-Type: application/json
Authorization: Bearer <access_token>
```
**Request Body:**
```json
{
  "customerId": 1,
  "orderDate": "2024-04-10",
  "items": [
    {
      "productId": 1,
      "quantity": 2,
      "unitPrice": 450.50
    }
  ]
}
```

### 5. Actualizar Orden
```
PUT /api/orders/<id>
Content-Type: application/json
Authorization: Bearer <access_token>
```

### 6. Cambiar Estado de Orden
```
PATCH /api/orders/<id>/status?status=SHIPPED
Authorization: Bearer <access_token>
```

---

## 📊 REPORTES (ADMIN SOLO)

**Base Path:** `/api/reports`  
**Autenticación:** Requerida (Bearer Token)  
**Rol Requerido:** ADMIN

### 1. Reporte de Ventas
```
GET /api/reports/sales?startDate=2024-01-01&endDate=2024-04-10
Authorization: Bearer <access_token>
```

### 2. Reporte de Productos
```
GET /api/reports/products?startDate=2024-01-01&endDate=2024-04-10
Authorization: Bearer <access_token>
```

### 3. Reporte de Alertas de Stock Bajo
```
GET /api/reports/low-stock-alerts
Authorization: Bearer <access_token>
```

---

## 🛠️ CONFIGURACIÓN DE FRONTEND (IMPORTANTE)

### AxiosConfig.js - Configuración para enviar JWT

Para que funcione el login y tengas acceso a los endpoints protegidos, **debes configurar Axios para enviar el token JWT en TODAS las solicitudes autenticadas**:

```javascript
// AxiosConfig.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

// Interceptor para agregar el token a cada solicitud
api.interceptors.request.use((config) => {
  // Obtener el token del localStorage
  const token = localStorage.getItem('accessToken');
  
  if (token) {
    // Agregar el token al header Authorization con formato Bearer
    config.headers.Authorization = `Bearer ${token}`;
  }
  
  return config;
});

// Interceptor para manejar respuestas
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Si es error 401, el token expiró
    if (error.response?.status === 401) {
      // Limpiar token y redirigir al login
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### auth.js - Guardar el token después del login

```javascript
// auth.js
import api from './AxiosConfig';

export const login = async (email, password) => {
  try {
    const response = await api.post('/api/auth/login', {
      email,
      password
    });
    
    const { data } = response.data; // Extraer el data de la respuesta
    
    // IMPORTANTE: Guardar ambos tokens en localStorage
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      id: data.id,
      email: data.email,
      role: data.role,
      username: data.username
    }));
    
    return data;
  } catch (error) {
    throw error;
  }
};
```

### Uso en componentes Vue

```javascript
// AdminDashboard.vue o similar
import api from '@/api/AxiosConfig';

export default {
  methods: {
    async fetchFurniture() {
      try {
        // ✅ CORRECTO: Usar /api/products en lugar de /furniture/
        const response = await api.get('/api/products');
        this.furniture = response.data.data;
      } catch (error) {
        console.error('Error:', error);
      }
    },
    
    async getCategories() {
      try {
        // ✅ Este endpoint debería ser /api/categories si existe
        // Si no existe, necesitas crear este endpoint o usar /api/products
        const response = await api.get('/api/products');
        this.categories = response.data.data;
      } catch (error) {
        console.error('Error:', error);
      }
    }
  },
  mounted() {
    this.fetchFurniture();
    this.getCategories();
  }
}
```

---

## ❌ PROBLEMAS Y SOLUCIONES

### Error 403 Forbidden

**Causa:** El token JWT no se está enviando correctamente.

**Solución:**
1. Verifica que Axios tenga el interceptor para agregar `Authorization: Bearer <token>`
2. Verifica que el token se esté guardando en localStorage después del login
3. Verifica que el header sea exactamente `Authorization: Bearer <token>` (con espacio)
4. Verifica que el usuario tenga el rol correcto (ADMIN para endpoints de administración)

### Error CORS

**Solución:** El backend ya permite CORS desde:
- `http://localhost:3000`, `4200`, `5173`
- `http://192.168.10.51:3000`, `4200`, `5173`
- `http://127.0.0.1:3000`, `4200`, `5173`

Si usas localhost, cambia en el frontend:
```javascript
const api = axios.create({
  baseURL: 'http://localhost:8080'
});
```

### Error 404 en rutas

**Verifica que uses las rutas correctas:**
- ❌ `/furniture/` → Esto no existe
- ✅ `/api/products/` → Correcto
- ❌ `/furniture/categories` → Esto no existe
- ✅ `/api/products/` → Usa esta para listar productos

---

## 📋 PERMISOS POR ROL

### Rol USER
- ✅ Login / Logout
- ✅ Ver productos
- ✅ Ver productos relacionados
- ✅ Cambiar contraseña

### Rol ADMIN
- ✅ Todo lo que puede USER
- ✅ Gestionar productos (CRUD)
- ✅ Gestionar clientes (CRUD)
- ✅ Ver inventario
- ✅ Ajustar stock
- ✅ Crear órdenes
- ✅ Ver reportes

---

## 🚀 PRUEBAS RECOMENDADAS

1. **Registro:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@test.com","password":"pass123","name":"Test User","phone":"5551234567","address":"Calle 1","city":"Mexico","state":"CDMX","postalCode":"06000"}'
   ```

2. **Login:**
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"test@test.com","password":"pass123"}'
   ```

3. **Usar token en siguiente petición:**
   ```bash
   curl -X GET http://localhost:8080/api/products \
     -H "Authorization: Bearer <accessToken>"
   ```

