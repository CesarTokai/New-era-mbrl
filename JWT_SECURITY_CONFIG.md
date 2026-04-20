# Configuración de Seguridad JWT - Backend

## 🔐 Checklist de Seguridad

### Desarrollo (Local)
- [x] JWT Secret en `application.properties` (permitido solo en desarrollo)
- [x] CORS permitido en `localhost:*` (puertos 3000, 4200, 5173, 5174)
- [x] Token expira en 24 horas
- [x] Base de datos en `localhost:3306`

### Producción ⚠️
- [ ] Cambiar JWT Secret a variable de entorno `JWT_SECRET`
- [ ] Cambiar URL de base de datos a variable de entorno `DB_URL`
- [ ] Cambiar credenciales MySQL a variables de entorno `DB_USER`, `DB_PASSWORD`
- [ ] Cambiar CORS a dominios específicos (no localhost)
- [ ] Habilitar HTTPS en todas las conexiones
- [ ] Configurar credenciales de email en variables de entorno
- [ ] Usar token expiration más corta en producción (2-4 horas)

---

## 📋 Generación de JWT Secret

### Opción 1: Usando OpenSSL (Linux/Mac)
```bash
openssl rand -base64 64
```

Genera algo como:
```
aB3cDeFg/HIjKLmNoPqRsT+UvWxYzAbCdEfGhIjKlMnOpQrStUvWxYzAbCdEfGhIjKlMnOpQrStUvWxYzAbCdE=
```

### Opción 2: Usando PowerShell (Windows)
```powershell
[Convert]::ToBase64String((1..64 | ForEach-Object { [byte](Get-Random -Minimum 0 -Maximum 256) }))
```

### Opción 3: Online
https://generate-random.org/ → Base64 64 bytes

**IMPORTANTE:** La clave debe tener **mínimo 512 bits** (64 caracteres en base64) para HS512

---

## 🚀 Deployment en Producción

### Docker Compose Example
```yaml
version: '3.8'
services:
  backend:
    image: mbrl-backend:latest
    environment:
      JWT_SECRET: "tu-secret-base64-aqui"
      JWT_EXPIRATION: "14400000"  # 4 horas
      DB_URL: "jdbc:mysql://mysql:3306/mbl?serverTimezone=UTC"
      DB_USER: "root"
      DB_PASSWORD: "contraseña-fuerte"
      SPRING_PROFILES_ACTIVE: "prod"
    ports:
      - "8080:8080"
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: "contraseña-fuerte"
      MYSQL_DATABASE: "mbl"
```

### Variables de Entorno (Recomendado)
```bash
export JWT_SECRET="aB3cDeFg/HIjKLmNoPqRsT+UvWxYzAbCdEfGhIjKlMnOpQrStUvWxYzAbCdEfGhIjKlMnOpQrStUvWxYzAbCdE="
export JWT_EXPIRATION="14400000"
export DB_URL="jdbc:mysql://prod-db-host:3306/mbl?serverTimezone=UTC&useSSL=true"
export DB_USER="prod_user"
export DB_PASSWORD="contraseña-fuerte"
```

---

## 🔑 Configuración de CORS en Producción

**Antes (Desarrollo):**
```java
cfg.setAllowedOrigins(Arrays.asList(
  "http://localhost:3000",
  "http://localhost:5173",
  ...
));
```

**Después (Producción):**
```java
cfg.setAllowedOrigins(Arrays.asList(
  "https://muebleria-plaza-reforma.com",
  "https://app.muebleria-plaza-reforma.com"
));
cfg.setAllowCredentials(true);
```

---

## 🧪 Testing del JWT

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Respuesta esperada:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "expiresIn": 86400000,
    "email": "test@example.com",
    "role": "USER"
  },
  "message": "Login exitoso"
}
```

### Usar Token en Petición Protegida
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### Token Expirado
```bash
curl -X GET http://localhost:8080/api/products \
  -H "Authorization: Bearer old-expired-token"
```

**Respuesta esperada (401):**
```json
{
  "success": false,
  "message": "Token inválido o no enviado. Inicia sesión en POST /api/auth/login",
  "status": 401
}
```

---

## 🛡️ Seguridad Adicional

### 1. HTTPS Obligatorio
```yaml
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

### 2. Rate Limiting (Opcionalmente agregar)
```xml
<!-- pom.xml -->
<dependency>
  <groupId>io.github.bucket4j</groupId>
  <artifactId>bucket4j-core</artifactId>
  <version>7.6.0</version>
</dependency>
```

### 3. Monitoreo de Tokens
- [x] JwtAuthFilter registra intentos fallidos
- [x] JwtUtil lanza excepciones específicas
- [ ] Considera agregar métricas de Prometheus

---

## ⚡ Mejoras Aplicadas

✅ **JWT Secret ahora soporta variables de entorno**
- Antes: Hardcodeado en application.properties
- Después: `${JWT_SECRET:default}` con fallback

✅ **JwtAuthFilter mejorado**
- Valida tokens vacíos
- Mejor manejo de excepciones
- Evita race conditions

✅ **JwtUtil más robusto**
- Validación null-safe
- Excepciones específicas (Malformed, Expired, Unsupported)
- Logs más informativos

✅ **SecurityConfig listo para producción**
- CORS configurable por entorno
- Manejo de excepciones de autenticación

---

## 📞 Troubleshooting

### "Token inválido"
1. Verifica que el token tenga formato: `Bearer <token>`
2. Revisa que el header sea exactamente: `Authorization: Bearer ...`
3. Confirma que el token no esté expirado

### "No se pudo cargar usuario"
1. Verifica que el email en el token existe en la BD
2. Confirma que `CustomUserDetailsService` carga correctamente

### "CORS Error"
1. Revisa `SecurityConfig.corsConfigurationSource()`
2. Confirma que el origen del frontend está en `allowedOrigins`

---

## 📚 Referencias

- [JJWT Library](https://github.com/jwtk/jjwt)
- [Spring Security Guide](https://spring.io/guides/gs/securing-web/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc7519)
