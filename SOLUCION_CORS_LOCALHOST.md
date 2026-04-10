# 🔗 Solución: CORS y Acceso desde Localhost

## Problema Reportado

```
Access to XMLHttpRequest at 'http://localhost:8080/api/auth/login' 
from origin 'http://192.168.10.51:5173' has been blocked by CORS policy
```

**Significa:** Frontend en IP `192.168.10.51:5173` no puede acceder a backend en `localhost:8080`

---

## ✅ Solución: Cambiar el frontend a localhost

### Opción 1: RECOMENDADA - Usa localhost en todo

**En tu máquina local:**
```bash
# Backend (Java)
http://localhost:8080

# Frontend (Vue/Vite)
http://localhost:5173
```

**Configuración del Frontend (AxiosConfig.js o similar):**
```javascript
const API_BASE_URL = 'http://localhost:8080';

export default {
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
};
```

**Ventaja:** Sin problemas de CORS, ambos en localhost

---

### Opción 2: Si NECESITAS usar IP 192.168.10.51

**Backend ya está configurado para aceptar:**
- `http://192.168.10.51:5173`
- `http://192.168.10.51:3000`
- `http://192.168.10.51:4200`

**Pero el Frontend debe apuntar a IP también:**

**En tu Frontend (AxiosConfig.js):**
```javascript
// CAMBIAR ESTO:
const API_BASE_URL = 'http://localhost:8080';

// A ESTO:
const API_BASE_URL = 'http://192.168.10.51:8080';
```

**Luego accede desde:**
```
http://192.168.10.51:5173
```

**⚠️ Requisito:** El backend debe estar escuchando en la IP `192.168.10.51`, no solo en `localhost`

---

## 🔧 Configurar Backend para escuchar en una IP específica

Si usas IP `192.168.10.51`, edita `application.properties`:

```properties
# Escuchar en todas las interfaces (0.0.0.0)
server.address=0.0.0.0
server.port=8080

# O especifica la IP exacta:
# server.address=192.168.10.51
# server.port=8080
```

---

## ✨ Recomendación FINAL

### Para Desarrollo Local
Usa **localhost** en todo:

```
Frontend:  http://localhost:5173
Backend:   http://localhost:8080
```

**AxiosConfig.js:**
```javascript
import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para agregar token
axiosInstance.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default axiosInstance;
```

---

## 🚀 Backend CORS ya está bien configurado

En `SecurityConfig.java` (líneas 87-96):

```java
corsConfig.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:4200",
    "http://localhost:5173",           // ✅ Vite
    "http://192.168.10.51:5173",       // ✅ Vite remoto
    "http://192.168.10.51:3000",       // ✅ Node remoto
    "http://192.168.10.51:4200",       // ✅ Angular remoto
    "http://127.0.0.1:5173",           // ✅ Loopback
    "http://127.0.0.1:3000",
    "http://127.0.0.1:4200"
));
corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
```

Si necesitas agregar más orígenes, edita esta línea y reinicia.

---

## 📝 Pasos Rápidos

### 1️⃣ Cambiar Frontend a localhost

**En tu archivo de configuración de Axios (AxiosConfig.js, main.js, .env):**

```javascript
// Cambiar de:
const API_URL = 'http://192.168.10.51:8080';

// A:
const API_URL = 'http://localhost:8080';
```

### 2️⃣ Reinicia el Frontend
```bash
# Si usas Vite:
npm run dev

# Si usas Vue CLI:
npm run serve

# Si usas cualquier otro:
Recarga en navegador: http://localhost:5173
```

### 3️⃣ Verifica que Backend esté en localhost
```bash
# En tu terminal de backend, deberías ver:
Started MbrlApplication in X.XXX seconds
tomcat started on port(s): 8080
```

### 4️⃣ Prueba desde navegador

Abre la consola del navegador (F12) y ejecuta:

```javascript
// En la consola:
fetch('http://localhost:8080/api/products', {
  headers: {
    'Authorization': 'Bearer tu-token-aqui'
  }
})
.then(r => r.json())
.then(d => console.log(d));
```

Si funciona sin errores CORS ✅, todo está bien.

---

## 🔍 Troubleshooting CORS

### Error: "No 'Access-Control-Allow-Origin' header"

**Verificar:**
1. Backend está corriendo en `http://localhost:8080` (no en IP)
2. Frontend llama a `http://localhost:8080` (no a IP)
3. Ambos usan http (no https)
4. Puerto es el correcto (8080 backend, 5173 frontend)

### Error: "CORS policy: The value of the 'Access-Control-Allow-Credentials' header"

**Solución:**
Asegúrate que en Axios tienes:
```javascript
axios.defaults.withCredentials = false;  // O false si no usas cookies
```

### Request bloqueado después de cambiar a localhost

**Solución:**
1. Limpia cache del navegador (Ctrl+Shift+Del)
2. Recarga la página (Ctrl+F5)
3. Abre Developer Tools y busca el error exacto en la consola

---

## 📞 Resumen Rápido

| Escenario | Problema | Solución |
|-----------|----------|----------|
| Frontend 192.168.10.51 → Backend localhost | ❌ CORS | Cambiar Frontend a localhost |
| Frontend localhost → Backend 192.168.10.51 | ❌ CORS | Cambiar Backend escuche en 0.0.0.0 |
| Frontend localhost → Backend localhost | ✅ Funciona | Sin cambios necesarios |
| Frontend 192.168.10.51 → Backend 192.168.10.51 | ✅ Funciona | Ambos usan IP |

**Recomendación:** Usa siempre `localhost` para desarrollo local. Mucho más simple ✨

---

## Configuración del Frontend (Completa)

```javascript
// AxiosConfig.js o utils/api.js
import axios from 'axios';

const API_BASE_URL = process.env.VUE_APP_API_URL || 'http://localhost:8080';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Request Interceptor - Agregar token
axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Response Interceptor - Manejar errores
axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Token expirado - redirigir a login
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
```

**En tu .env.local:**
```
VUE_APP_API_URL=http://localhost:8080
```

**En tu .env.production:**
```
VUE_APP_API_URL=https://api.tudominio.com
```

---

¡Ahora debe funcionar sin problemas de CORS! 🎉

