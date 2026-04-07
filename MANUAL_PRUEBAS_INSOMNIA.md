# 📝 Insomnia/Postman - Casos de Prueba Manual

## 🧪 Guía para Probar los Endpoints en Insomnia o Postman

---

## 1️⃣ POST /api/auth/register

### ✅ Caso Exitoso: Registrar nuevo cliente

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Juan Pérez",
  "email": "juan.perez@example.com",
  "phone": "+1-555-0123",
  "address": "Calle Principal 123",
  "city": "Ciudad de México",
  "state": "CDMX",
  "postalCode": "06500"
}
```

**Respuesta Esperada (201):**
```json
{
  "success": true,
  "message": "Cliente registrado exitosamente",
  "data": {
    "id": 1,
    "name": "Juan Pérez",
    "email": "juan.perez@example.com"
  }
}
```

---

### ❌ Email Duplicado

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "name": "Otro Usuario",
  "email": "juan.perez@example.com",
  "phone": "+1-555-0124",
  "address": "Calle Secundaria 456",
  "city": "Ciudad de México",
  "state": "CDMX",
  "postalCode": "06500"
}
```

**Respuesta Esperada (400):**
```json
{
  "success": false,
  "message": "El email ya está registrado",
  "statusCode": 400
}
```

---

## 2️⃣ POST /api/auth/login

### ✅ Login Exitoso

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "juan.perez@example.com",
  "password": "password123"
}
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Login exitoso",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_123",
    "type": "Bearer",
    "id": 1,
    "username": "juan",
    "email": "juan.perez@example.com",
    "role": "USER"
  }
}
```

**⚠️ Guardar el token para próximos requests autenticados**

---

### ❌ Contraseña Incorrecta

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "juan.perez@example.com",
  "password": "wrongpassword"
}
```

**Respuesta Esperada (401):**
```json
{
  "success": false,
  "message": "Email o contraseña inválidos",
  "statusCode": 401
}
```

---

## 3️⃣ POST /api/auth/refresh

### ✅ Refrescar Token

```http
POST http://localhost:8080/api/auth/refresh?refreshToken=refresh_token_123
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Token refrescado exitosamente",
  "data": {
    "token": "new_access_token_xyz",
    "type": "Bearer"
  }
}
```

---

## 4️⃣ POST /api/auth/logout

### ✅ Logout Exitoso

```http
POST http://localhost:8080/api/auth/logout?accessToken=token_123&refreshToken=refresh_123
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Logout exitoso",
  "data": null
}
```

---

## 5️⃣ POST /api/auth/change-password

### ✅ Cambiar Contraseña (Requiere Autenticación)

```http
POST http://localhost:8080/api/auth/change-password?userId=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "oldPassword": "password123",
  "newPassword": "newPassword456",
  "confirmPassword": "newPassword456"
}
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Contraseña actualizada exitosamente",
  "data": null
}
```

---

## 6️⃣ GET /api/auth/password-status/{userId}

### ✅ Obtener Estado de Contraseña

```http
GET http://localhost:8080/api/auth/password-status/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Estado de contraseña obtenido",
  "data": {
    "changeRequired": false,
    "daysRemaining": 45,
    "expirationDays": 90
  }
}
```

---

## 7️⃣ POST /api/auth/forgot-password

### ✅ Solicitar Reset de Contraseña

```http
POST http://localhost:8080/api/auth/forgot-password?email=juan.perez@example.com
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Si el email existe, recibirás instrucciones para resetear tu contraseña",
  "data": null
}
```

**⚠️ Nota:** El email se envía (en producción). En pruebas, revisar logs.

---

## 8️⃣ POST /api/auth/reset-password

### ✅ Resetear Contraseña

```http
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{
  "token": "reset_token_abc123xyz",
  "newPassword": "brandNewPassword789",
  "confirmPassword": "brandNewPassword789"
}
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Contraseña reseteada exitosamente",
  "data": null
}
```

---

## 9️⃣ GET /api/auth/validate-reset-token

### ✅ Token Válido

```http
GET http://localhost:8080/api/auth/validate-reset-token?token=reset_token_abc123xyz
```

**Respuesta Esperada (200):**
```json
{
  "success": true,
  "message": "Token válido",
  "data": true
}
```

---

### ❌ Token Expirado/Inválido

```http
GET http://localhost:8080/api/auth/validate-reset-token?token=expired_token
```

**Respuesta Esperada (400):**
```json
{
  "success": false,
  "message": "Token inválido o expirado",
  "statusCode": 400
}
```

---

## 🔐 Headers de Autenticación

Para endpoints protegidos, agrega este header:

```
Authorization: Bearer <JWT_TOKEN_AQUI>
```

Ejemplo con token real:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuIiwiaWF0IjoxNjEyMzQ1Njc4fQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

---

## 📋 Orden Recomendado de Pruebas

### Flujo Completo:

1. **Register** - Crear usuario ✅
2. **Login** - Obtener JWT Token ✅
3. **Get Password Status** - Verificar estado (requiere JWT)
4. **Change Password** - Cambiar contraseña (requiere JWT)
5. **Logout** - Cerrar sesión
6. **Forgot Password** - Solicitar reset
7. **Validate Reset Token** - Validar token
8. **Reset Password** - Resetear contraseña
9. **Login nuevamente** - Con nueva contraseña

---

## 🧪 Flujo de Testeo en Insomnia/Postman

### Paso 1: Configurar Base URL
```
Crear variable de entorno: BASE_URL = http://localhost:8080
Usar en requests: {{BASE_URL}}/api/auth/register
```

### Paso 2: Guardar JWT Token
Después de login, extraer token:
```javascript
// En Test Results
const response = pm.response.json();
pm.environment.set("jwt_token", response.data.token);
pm.environment.set("refresh_token", response.data.refreshToken);
```

### Paso 3: Usar Token en Siguiente Request
```
Authorization: Bearer {{jwt_token}}
```

---

## ✅ Criterios de Aceptación

### Todos los Requests deben retornar:

✅ **status_code**: 200, 201, 400, o 401
✅ **response.success**: true o false
✅ **response.message**: Texto descriptivo
✅ **response.data**: Objeto o null
✅ **response.statusCode**: Número (en errores)

### Ejemplos de Respuesta Exitosa:
```json
{
  "success": true,
  "message": "Operación completada",
  "data": { ... }
}
```

### Ejemplos de Error:
```json
{
  "success": false,
  "message": "Descripción del error",
  "statusCode": 400
}
```

---

## 🛠️ Troubleshooting

| Problema | Solución |
|---|---|
| 401 Unauthorized | Verificar JWT token, rehacerLogin |
| 400 Bad Request | Revisar validación de entrada |
| 500 Server Error | Revisar logs, DB conectada |
| CORS error | Revisar SecurityConfig |
| Timeout | Servidor no corriendo |

---

## 📊 Tabla de Estados HTTP

| Código | Significado | Cuándo |
|---|---|---|
| 200 | OK | Exitoso |
| 201 | Created | Registro exitoso |
| 400 | Bad Request | Validación o error |
| 401 | Unauthorized | Sin token o token inválido |
| 403 | Forbidden | Sin permisos |
| 500 | Server Error | Error interno |

---

✅ **¡Listo para probar en Insomnia/Postman!**

