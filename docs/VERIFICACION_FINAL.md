# ✅ VERIFICACIÓN FINAL - Solución 403

## 🔍 PASO 1: Verificar Backend está corriendo

```bash
# Abre PowerShell en la carpeta del proyecto y ejecuta:
.\mvnw.cmd spring-boot:run

# O compila y corre:
.\mvnw.cmd clean package
java -jar target/mbrl-0.0.1-SNAPSHOT.jar
```

El servidor debe escuchar en `http://localhost:8080`

---

## 🔍 PASO 2: Prueba de Login desde Terminal

```bash
# Registrar usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@test.com",
    "password":"Pass123!",
    "name":"Test User",
    "phone":"5551234567",
    "address":"Calle 1",
    "city":"Mexico",
    "state":"CDMX",
    "postalCode":"06000"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Pass123!"}'

# Respuesta esperada:
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "...",
    "id": 1,
    "email": "test@test.com",
    "role": "USER"
  }
}
```

---

## 🔍 PASO 3: Prueba de Endpoint Protegido

```bash
# Copiar el accessToken de la respuesta anterior
# Luego ejecutar:

curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer eyJ..."

# Debería retornar:
{
  "success": true,
  "data": [
    // lista de productos
  ],
  "message": "Productos obtenidos exitosamente"
}
```

---

## 🔍 PASO 4: Verificar Frontend

### 4.1 Abre DevTools (F12) → Console

Después de hacer login, ejecuta:

```javascript
// Verificar token guardado
console.log(localStorage.getItem('accessToken'))
// Debe retornar algo como: eyJhbGciOiJIUzUxMiJ9...

// Verificar datos del usuario
console.log(localStorage.getItem('user'))
// Debe retornar: {"id":1,"email":"test@test.com","role":"USER"}
```

### 4.2 Abre DevTools → Network

Haz una solicitud a `/api/products` y verifica:

1. **Status:** Debe ser `200` (no 403)
2. **Headers → Authorization:** Debe mostrar `Bearer eyJ...`
3. **Response:** Debe mostrar lista de productos

Si ves **403 en status**: El token NO se está enviando.
Revisa que AxiosConfig tenga el interceptor correcto.

---

## 🔍 PASO 5: Checklist de Configuración Frontend

- [ ] **AxiosConfig.js** existe en `src/api/AxiosConfig.js`
- [ ] Tiene interceptor que agrega `Authorization` header
- [ ] **LoginView.vue** guarda `accessToken` en localStorage
- [ ] **AdminDashboard.vue** usa `/api/products` (no `/furniture/`)
- [ ] No hay errores en console del navegador
- [ ] Network tab muestra `Authorization: Bearer ...`

---

## 🔍 PASO 6: Si aún hay error 403

### Causa 1: Token no se envía
- Verifica que AxiosConfig.js esté siendo importado correctamente
- Verifica que el interceptor esté agregando el header
- En Network tab, busca `Authorization` header

### Causa 2: Usuario no es ADMIN
- Algunos endpoints requieren rol ADMIN
- Para crear/editar/eliminar productos, necesitas ADMIN
- Cambiar rol en base de datos:
  ```sql
  UPDATE users SET role = 'ADMIN' WHERE email = 'test@test.com';
  ```

### Causa 3: Token expirado
- Token válido por 24 horas
- Si ves error 401, hacer login nuevamente
- El interceptor de respuesta lo hace automáticamente

### Causa 4: Rutas incorrectas
- ❌ `/furniture/` → No existe
- ✅ `/api/products/` → Correcto
- Verifica que AdminDashboard esté usando rutas `/api/...`

---

## 📊 TABLA DE ERRORES

| Error | Causa | Solución |
|-------|-------|----------|
| 403 | Token no se envía | Agregar interceptor a Axios |
| 403 | Usuario no es ADMIN | UPDATE users SET role = 'ADMIN' |
| 401 | Token expirado | Hacer login nuevamente |
| 404 | Ruta incorrecta | Usar `/api/products` no `/furniture/` |
| CORS | Origen no permitido | Agregar origen en SecurityConfig.java |

---

## 📝 ARCHIVOS A MODIFICAR EN TU PROYECTO

### 1. src/api/AxiosConfig.js (Copiar este contenido)
```javascript
import axios from 'axios';

const api = axios.create({ baseURL: 'http://localhost:8080' });

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

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

### 2. src/views/LoginView.vue (Método login)
```javascript
async handleLogin() {
  try {
    const response = await api.post('/api/auth/login', {
      email: this.email,
      password: this.password
    });

    const jwtData = response.data.data;

    // GUARDAR TOKENS
    localStorage.setItem('accessToken', jwtData.accessToken);
    localStorage.setItem('refreshToken', jwtData.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      id: jwtData.id,
      email: jwtData.email,
      role: jwtData.role
    }));

    this.$router.push('/admin');
  } catch (error) {
    console.error('Error:', error);
  }
}
```

### 3. src/views/AdminDashboard.vue (Métodos)
```javascript
async fetchFurniture() {
  try {
    // CORRECTO
    const response = await api.get('/api/products');
    this.furniture = response.data.data;
  } catch (error) {
    console.error('Error:', error);
  }
}

async getCategories() {
  try {
    // Extraer categorías de productos
    const response = await api.get('/api/products');
    const cats = [...new Set(response.data.data.map(p => p.category))];
    this.categories = cats;
  } catch (error) {
    console.error('Error:', error);
  }
}
```

---

## 🚀 RESUMEN DE LA SOLUCIÓN

| Antes (❌) | Después (✅) |
|-----------|-----------|
| Sin interceptor Axios | Axios con interceptor que agrega token |
| Token no guardado en localStorage | Token guardado después del login |
| Rutas `/furniture/` | Rutas `/api/products/` |
| Error 403 en todas las solicitudes | Solicitudes funcionan con 200 OK |

---

## 📞 PRÓXIMOS PASOS

1. **Copia el contenido de AxiosConfig.js** a tu proyecto
2. **Actualiza LoginView.vue** para guardar tokens
3. **Actualiza AdminDashboard.vue** para usar rutas `/api/`
4. **Prueba desde el navegador**
5. **Verifica en DevTools Console y Network**

Si todo está en orden, el error 403 desaparecerá y tendrás acceso a todos los endpoints.

**¿Necesitas ayuda con algún paso específico? Pregunta.**

