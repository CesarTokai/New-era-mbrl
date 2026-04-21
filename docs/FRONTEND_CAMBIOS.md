# Cambios requeridos en el Frontend

## Rutas de Muebles/Productos

Reemplaza todas las llamadas de `/furniture` por `/api/products`:

| Antes ❌ | Después ✅ |
|---|---|
| `GET /furniture` | `GET /api/products` |
| `GET /furniture/{id}` | `GET /api/products/{id}` |
| `POST /furniture` | `POST /api/products` |
| `PUT /furniture/{id}` | `PUT /api/products/{id}` |
| `DELETE /furniture/{id}` | `DELETE /api/products/{id}` |

> **Nota:** `/furniture/categories` y `/furniture/brands` **NO cambian**, siguen igual.

---

## Rutas que NO cambian

```
GET  /furniture/categories       ✅ igual
GET  /furniture/categories/{id}  ✅ igual
POST /furniture/categories       ✅ igual

GET  /furniture/brands           ✅ igual
GET  /furniture/brands/{id}      ✅ igual
POST /furniture/brands           ✅ igual
```

---

## Nuevos endpoints disponibles

```
GET /api/products/low-stock        → Productos con stock bajo
GET /api/products/{id}/related     → Productos relacionados
```

---

## Body para crear/editar producto (sin cambios)

```json
{
  "name": "string (requerido)",
  "description": "string",
  "price": 0.00,
  "costPrice": 0.00,
  "stock": 0,
  "minStock": 5,
  "imageUrl": "string",
  "brandId": 1,
  "categoryId": 1
}
```

---

## Autenticación

El token **debe enviarse en el header** de cada request protegido:

```
Authorization: Bearer <token>
```

