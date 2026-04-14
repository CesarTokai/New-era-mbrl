# MBRL — Documentación del Proyecto

## Descripción General
Backend REST para un sistema de gestión de mueblería.  
Gestiona productos, inventario, órdenes, clientes, marcas, categorías, imágenes y reportes.

---

## Stack Tecnológico
| Tecnología | Versión |
|---|---|
| Java | 17 |
| Spring Boot | 3.5.5 |
| Spring Security | Incluido en Boot |
| Spring Data JPA | Incluido en Boot |
| MySQL | Runtime driver |
| Lombok | Annotation processor |
| JWT | Autenticación stateless |
| Maven Wrapper | `mvnw` / `mvnw.cmd` |

---

## Estructura de Paquetes
```
com.mx.mbrl
├── MbrlApplication.java        ← Bootstrap (@SpringBootApplication + @EnableScheduling)
├── config/
│   ├── SecurityConfig.java     ← Filtros JWT, CORS, rate limit, rutas públicas/protegidas
│   └── WebMvcConfig.java       ← Sirve imágenes locales desde /uploads/images/**
├── controller/                 ← Controladores REST
├── dto/                        ← Objetos de transferencia (Request/Response)
├── entity/                     ← Entidades JPA (tablas MySQL)
├── repository/                 ← Interfaces JPA Repository
├── security/                   ← JWT util, filtros de autenticación y rate limiting
└── service/                    ← Lógica de negocio
```

---

## Endpoints API

### Autenticación — `/api/auth` (Público)
| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Registro de nuevo cliente |
| POST | `/api/auth/login` | Login, retorna access + refresh token |
| POST | `/api/auth/refresh` | Renueva el access token |
| POST | `/api/auth/logout` | Invalida tokens (blacklist) |
| POST | `/api/auth/forgot-password` | Envía email de reset de contraseña |
| GET  | `/api/auth/validate-reset-token` | Valida token de reset |
| POST | `/api/auth/reset-password` | Cambia contraseña con token |
| POST | `/api/auth/change-password` | Cambia contraseña (requiere auth) |
| GET  | `/api/auth/password-status/{userId}` | Estado de expiración de contraseña |

### Productos — `/furniture` (Requiere JWT)
| Método | Ruta | Descripción |
|---|---|---|
| GET    | `/furniture` | Lista todos los productos activos |
| GET    | `/furniture/{id}` | Obtiene un producto por ID |
| POST   | `/furniture` | Crea nuevo producto |
| PUT    | `/furniture/{id}` | Actualiza producto |
| DELETE | `/furniture/{id}` | Soft-delete (isActive = false) |

### Marcas — `/furniture/brands` (Requiere JWT)
| Método | Ruta | Descripción |
|---|---|---|
| GET    | `/furniture/brands` | Lista todas las marcas |
| GET    | `/furniture/brands/{id}` | Obtiene marca por ID |
| POST   | `/furniture/brands` | Crea marca |
| PUT    | `/furniture/brands/{id}` | Actualiza marca |
| DELETE | `/furniture/brands/{id}` | Elimina marca |

### Categorías — `/furniture/categories` (Requiere JWT)
| Método | Ruta | Descripción |
|---|---|---|
| GET    | `/furniture/categories` | Lista todas las categorías |
| GET    | `/furniture/categories/{id}` | Obtiene categoría por ID |
| POST   | `/furniture/categories` | Crea categoría (admite categoría padre) |
| PUT    | `/furniture/categories/{id}` | Actualiza categoría |
| DELETE | `/furniture/categories/{id}` | Elimina categoría |

### Órdenes — `/api/orders` (Requiere JWT)
| Método | Ruta | Descripción |
|---|---|---|
| POST   | `/api/orders` | Crea orden con ítems |
| GET    | `/api/orders` | Lista órdenes (ADMIN: todas; USER: propias) |
| GET    | `/api/orders/{id}` | Obtiene orden por ID |
| PUT    | `/api/orders/{id}/status` | Actualiza estado de la orden |
| DELETE | `/api/orders/{id}` | Cancela orden |

### Inventario — `/api/inventory` (Requiere JWT)
| Método | Ruta | Descripción |
|---|---|---|
| GET  | `/api/inventory/movements/{productId}` | Historial de movimientos |
| GET  | `/api/inventory/available-stock/{productId}` | Stock disponible |
| POST | `/api/inventory/adjust` | Ajuste manual de stock |
| POST | `/api/inventory/add-stock` | Entrada de stock |
| POST | `/api/inventory/remove-stock` | Salida de stock |

### Imágenes — `/api/images` (Requiere JWT)
| Método | Ruta | Descripción |
|---|---|---|
| POST   | `/api/images/upload` | Sube imagen (JPG/PNG/GIF/WEBP, máx 5MB) |
| DELETE | `/api/images/{fileName}` | Elimina imagen del disco |
| GET    | `/uploads/images/**` | Acceso público a imágenes (sin auth) |

### Reportes — `/api/reports` (Requiere JWT)
| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/reports/sales` | Reporte de ventas por rango de fechas |
| GET | `/api/reports/top-products` | Productos más vendidos |
| GET | `/api/reports/low-stock` | Alertas de stock bajo |
| GET | `/api/reports/profit` | Ganancia neta por período |
| GET | `/api/reports/revenue` | Ingresos brutos por período |
| GET | `/api/reports/dashboard` | Métricas combinadas para dashboard |

---

## Entidades Principales
| Entidad | Tabla | Descripción |
|---|---|---|
| `User` | `users` | Usuarios del sistema (roles: USER, ADMIN). Contraseña expira 90 días. |
| `Customer` | `customers` | Clientes registrados |
| `Product` | `products` | Productos con precio venta, costo, stock, marca y categoría |
| `Brand` | `brands` | Marcas de productos |
| `Category` | `categories` | Categorías con soporte de jerarquía (parentCategory) |
| `Order` | `orders` | Pedidos con estado y colección de ítems |
| `OrderItem` | `order_items` | Líneas de detalle de una orden |
| `InventoryMovement` | `inventory_movements` | Auditoría de entradas/salidas de stock |
| `Payment` | `payments` | Pagos asociados a órdenes |
| `Review` | `reviews` | Reseñas de productos |
| `RefreshToken` | `refresh_tokens` | Tokens de renovación JWT |
| `BlacklistedToken` | `blacklisted_tokens` | Tokens revocados (logout) |
| `PasswordResetToken` | `password_reset_tokens` | Tokens de reset de contraseña por email |
| `PasswordHistory` | `password_history` | Historial para evitar reutilización |
| `AuditLog` | `audit_logs` | Registro de acciones del sistema |
| `PriceHistory` | `price_history` | Historial de precios de productos |
| `Return` | `returns` | Devoluciones de pedidos |

---

## Seguridad
- **Autenticación:** JWT Bearer Token (stateless, sin sesión HTTP).
- **Access token:** expira en `jwt.expiration` ms (default 24h).
- **Refresh token:** expira en `jwt.refresh-expiration` ms (default 7 días).
- **Logout:** agrega el token a `BlacklistedToken` para invalidarlo.
- **Rate limiting:** `RateLimiterFilter` corre antes del filtro JWT.
- **CORS:** permite orígenes `localhost:3000/4200/5173/5174` y equivalentes `127.0.0.1`.
- **Contraseñas:** BCrypt + expiración cada 90 días + historial de contraseñas.
- **Rutas públicas:** register, login, forgot/reset password, GET /uploads/**, Swagger.

---

## Configuración (`application.properties`)
```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/mbl?serverTimezone=UTC
spring.datasource.username=<usuario>
spring.datasource.password=<contraseña>
spring.jpa.hibernate.ddl-auto=update

# Servidor
server.port=8080

# JWT
jwt.secret=<clave-mínimo-512-bits>
jwt.expiration=86400000        # 24h en ms
jwt.refresh-expiration=604800000  # 7d en ms

# Imágenes
app.upload.dir=uploads/images
app.upload.max-size=5242880    # 5MB
app.base-url=http://localhost:8080

# Email (reset de contraseña)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<email>
spring.mail.password=<app-password>
```

---

## Respuesta Estándar API
Todos los endpoints usan el wrapper `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "Descripción del resultado",
  "data": { ... },
  "status": 200
}
```

---

## Comandos Útiles
```cmd
# Windows
.\mvnw.cmd spring-boot:run
.\mvnw.cmd test
.\mvnw.cmd clean package

# Unix/Mac
./mvnw spring-boot:run
./mvnw test
./mvnw clean package
```

---

## Notas Importantes
- El **soft-delete** de productos solo marca `isActive = false`; no borra el registro.
- `ProductService.getRelatedProducts()` calcula relevancia con peso 70% ventas / 30% calificación promedio.
- Las imágenes se guardan en disco (`uploads/images/`) con nombre UUID; se sirven como recurso estático.
- `@EnableScheduling` está activo para tareas de limpieza de tokens expirados.
- La base de datos se llama `mbl` (no `mbrl`) según la URL en `application.properties`.

