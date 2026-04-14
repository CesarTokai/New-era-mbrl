# SOLUCIÓN: Login Fallido

## ¿Cuál es el problema?

Los usuarios que fueron registrados **ANTES de las correcciones** tienen guardada la contraseña por defecto `"defaultPassword123"` en la base de datos, no la contraseña que intentaste usar.

## ¿Por qué sucedió?

En el código anterior de `AuthService.java` línea 50, se tenía:
```java
user.setPassword(passwordEncoder.encode("defaultPassword123"));
```

Esto ignoraba la contraseña que enviabas en el registro. **YA FUE CORREGIDO**.

## Solución

### OPCIÓN 1: Limpiar la base de datos y registrarse de nuevo (RECOMENDADO)

1. Ejecuta una de estas sentencias SQL para eliminar los usuarios antiguos:

```sql
-- Opción A: Eliminar usuarios específicos
DELETE FROM customers WHERE user_id IN (SELECT id FROM users WHERE email IN ('juan@example.com', 'cgonzalez@tokai.com'));
DELETE FROM users WHERE email IN ('juan@example.com', 'cgonzalez@tokai.com');

-- O Opción B: Limpiar toda la tabla de usuarios
DELETE FROM customers;
DELETE FROM users;
```

2. **Reinicia la aplicación** (aunque no es obligatorio)

3. **Registra nuevamente** los usuarios con las credenciales correctas:
```json
{
  "name": "Juan Pérez",
  "email": "juan@example.com",
  "password": "SecurePass123!",
  "phone": "+52 5555551234"
}
```

4. **Ahora sí podrás hacer login** con esa misma contraseña

### OPCIÓN 2: Cambiar la contraseña en la base de datos (TEMPORAL)

Si necesitas usar los usuarios existentes, puedes actualizar directamente en la BD. Pero necesitas un hash de BCrypt válido:

```sql
-- Hash de "testPassword123!" en BCrypt
UPDATE users SET password = '$2a$10$fvRmzrfXrHhbXfEBNI6V..cXHZuZn.sZvXN8WJ.QCxJfXrF6N6pxW' 
WHERE email = 'juan@example.com';
```

**Luego haz login con contraseña: `testPassword123!`**

## Verificación

Una vez registrado nuevamente, intenta:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "password": "SecurePass123!"
  }'
```

Deberías recibir:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "id": 1,
    "username": "juan",
    "email": "juan@example.com",
    "role": "USER"
  },
  "message": "Login exitoso"
}
```

## CORS ya está configurado

La configuración CORS ya está en lugar correcto en `SecurityConfig.java` y permite:
- `http://localhost:3000` (Vue dev)
- `http://localhost:5173` (Vite dev)
- `http://192.168.10.51:5173` (Acceso desde otra máquina)
- Y más...

Si necesitas agregar más orígenes, edita la línea 87-96 de `SecurityConfig.java`.

---

# SOLUCIÓN COMPLETA: Error 403 Forbidden después de Login

## 🔴 PROBLEMA NUEVO
Después de iniciar sesión exitosamente, recibes error **403 (Forbidden)** en:
```
GET http://localhost:8080/furniture/ 403 (Forbidden)
GET http://localhost:8080/furniture/categories 403 (Forbidden)  
POST http://localhost:8080/furniture/categories 403 (Forbidden)
```

## ✅ CAUSAS RAÍZ

1. **Frontend NO envía JWT token en header `Authorization`**
2. **Rutas del frontend son incorrectas** (`/furniture/` no existe, usar `/api/products/`)
3. **Token no se guarda** en localStorage después del login

## 🛠️ SOLUCIÓN EN 3 PASOS

### PASO 1: Configurar AxiosConfig.js

Tu Axios debe agregar el JWT token automáticamente:

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080'
});

// Interceptor: Agregar token a cada solicitud
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    console.log('✅ Token agregado al header');
  } else {
    console.log('⚠️  No hay token en localStorage');
  }
  return config;
});

// Interceptor: Manejar errores 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### PASO 2: En LoginView.vue, guardar los tokens

```javascript
// LoginView.vue - Método login
async handleLogin() {
  try {
    const response = await this.doPost('/api/auth/login', {
      email: this.email,
      password: this.password
    });

    const jwtResponse = response.data.data;

    // ✅ IMPORTANTE: Guardar AMBOS tokens
    localStorage.setItem('accessToken', jwtResponse.accessToken);
    localStorage.setItem('refreshToken', jwtResponse.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      id: jwtResponse.id,
      email: jwtResponse.email,
      role: jwtResponse.role
    }));

    console.log('✅ Login exitoso. Tokens guardados.');
    this.$router.push('/admin');
  } catch (error) {
    console.error('❌ Error en login:', error);
  }
}
```

### PASO 3: En AdminDashboard.vue, usar rutas CORRECTAS

```javascript
// AdminDashboard.vue
async fetchFurniture() {
  try {
    // ✅ CORRECTO: /api/products
    // ❌ INCORRECTO: /furniture/
    const response = await this.doGet('/api/products');
    this.furniture = response.data.data;
  } catch (error) {
    console.error('Error cargando productos:', error);
  }
}

async getCategories() {
  try {
    // Extraer categorías de productos
    const response = await this.doGet('/api/products');
    const categories = [...new Set(response.data.data.map(p => p.category))];
    this.categories = categories;
  } catch (error) {
    console.error('Error cargando categorías:', error);
  }
}
```

## 📋 MAPEO DE RUTAS CORRECTAS

| ❌ INCORRECTO | ✅ CORRECTO | 
|---|---|
| `/furniture/` | `/api/products` |
| `/furniture/categories` | `/api/products` (extraer categorías) |
| `POST /furniture/categories` | `POST /api/products` |
| `PUT /furniture/{id}` | `PUT /api/products/{id}` |
| `DELETE /furniture/{id}` | `DELETE /api/products/{id}` |

## 🔍 VERIFICACIÓN EN CONSOLE

```javascript
// Verificar que el token está guardado
localStorage.getItem('accessToken')
// Debe retornar: "eyJhbGciOiJIUzUxMiJ9..."

// Verificar que Axios lo agrega
// Abre DevTools → Network → cualquier solicitud → Headers
// Debe ver: Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

## 📌 ROLES Y PERMISOS

- **USER**: Puede ver productos (LOGIN)
- **ADMIN**: Puede ver + crear/editar/eliminar productos

Los usuarios registrados son **USER** por defecto. Para cambiar a ADMIN:

```sql
-- En MySQL
UPDATE users SET role = 'ADMIN' WHERE email = 'tu@email.com';
```

## ⚠️ CHECKLIST FINAL

- [ ] AxiosConfig.js tiene interceptor que agrega Authorization header
- [ ] LoginView.vue guarda accessToken en localStorage
- [ ] AdminDashboard.vue usa /api/products (no /furniture/)
- [ ] Console muestra "Token agregado al header" (no "No hay token")
- [ ] Network tab muestra Authorization: Bearer ... en headers
- [ ] No ves error 403 más (debería funcionar)

**Si aún ves 403:** Verifica que tu usuario sea ADMIN (ver ROLES Y PERMISOS)



