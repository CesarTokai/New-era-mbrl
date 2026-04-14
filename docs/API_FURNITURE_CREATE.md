# API - Crear Mueble (Furniture)

## 📌 Endpoint

```
POST http://localhost:8080/furniture/
```

---

## 🔐 Autenticación

**Requerida:** Sí ✓  
**Header requerido:**
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

**Roles permitidos:** `USER`, `ADMIN`

---

## 📤 REQUEST (Lo que envías)

### Estructura del Body

```json
{
  "name": "Silla Ergonómica Premium",
  "description": "Silla de oficina con soporte lumbar ajustable",
  "price": 1500.00,
  "costPrice": 800.00,
  "stock": 25,
  "minStock": 5,
  "imageUrl": "https://cdn.example.com/images/silla-ergonomica.jpg",
  "brandId": 1,
  "categoryId": 3
}
```

### Detalles de campos

| Campo | Tipo | Requerido | Validaciones |
|-------|------|-----------|--------------|
| `name` | String | ✅ Sí | Máx 255 caracteres, no puede estar vacío |
| `description` | String | ❌ No | Máx 1000 caracteres |
| `price` | Decimal | ✅ Sí | Mayor a 0 (precio de venta) |
| `costPrice` | Decimal | ✅ Sí | Mayor a 0 (precio costo) |
| `stock` | Integer | ❌ No | Por defecto 0, no puede ser negativo |
| `minStock` | Integer | ❌ No | Por defecto 5, no puede ser negativo |
| `imageUrl` | String | ❌ No | Máx 500 caracteres, URL de la imagen |
| `brandId` | Long | ❌ No | ID de la marca (opcional) |
| `categoryId` | Long | ✅ Sí | ID de la categoría existente |

---

## 📥 RESPONSE (Lo que recibes)

### Éxito (201 Created)

```json
{
  "success": true,
  "data": {
    "id": 42,
    "name": "Silla Ergonómica Premium",
    "description": "Silla de oficina con soporte lumbar ajustable",
    "price": 1500.00,
    "costPrice": 800.00,
    "stock": 25,
    "minStock": 5,
    "imageUrl": "https://cdn.example.com/images/silla-ergonomica.jpg",
    "brandId": 1,
    "brandName": "IKEA",
    "categoryId": 3,
    "categoryName": "Sillas",
    "isActive": true
  },
  "message": "Producto creado exitosamente",
  "status": 201
}
```

### Error - Validación (400 Bad Request)

```json
{
  "success": false,
  "message": "La categoría es obligatoria",
  "status": 400
}
```

### Error - No autorizado (401 Unauthorized)

```json
{
  "success": false,
  "message": "Token inválido o expirado. Inicia sesión nuevamente.",
  "status": 401
}
```

### Error - Servidor (500 Internal Server Error)

```json
{
  "success": false,
  "message": "Error creando producto",
  "status": 500
}
```

---

## 🧪 Ejemplo completo - Frontend (Vue.js)

### 1️⃣ Servicio (`src/services/furniture.js`)

```javascript
import axios from '../config/AxiosConfig';

export async function createFurniture(furnitureData) {
  console.log('📤 ENVIANDO: POST /furniture/', furnitureData);
  
  const response = await axios.post('/furniture/', {
    name: furnitureData.name,
    description: furnitureData.description,
    price: parseFloat(furnitureData.price),
    costPrice: parseFloat(furnitureData.costPrice),
    stock: parseInt(furnitureData.stock) || 0,
    minStock: parseInt(furnitureData.minStock) || 5,
    imageUrl: furnitureData.imageUrl,
    brandId: furnitureData.brandId ? parseInt(furnitureData.brandId) : null,
    categoryId: parseInt(furnitureData.categoryId)
  });
  
  console.log('📥 RESPUESTA:', response.data);
  return response.data;
}
```

### 2️⃣ Componente (Vue.js)

```vue
<script setup>
import * as furnitureService from '../services/furniture';

const handleSubmit = async () => {
  const formData = {
    name: 'Silla Ergonómica',
    description: 'Cómoda silla para oficina',
    price: 1500,
    costPrice: 800,
    stock: 25,
    minStock: 5,
    imageUrl: 'https://example.com/image.jpg',
    categoryId: 3
  };

  try {
    console.log('🚀 Iniciando envío de datos...');
    const result = await furnitureService.createFurniture(formData);
    
    console.log('✅ Mueble creado con ID:', result.data.id);
    console.log('Respuesta completa:', result);
    
  } catch (error) {
    console.error('❌ Error al crear mueble:', error);
  }
};
</script>
```

---

## 🔍 Qué verás en los LOGS del Backend

Cuando envíes la petición, verás en la consola del backend:

```
╔════════════════════════════════════════════════════════════
║ POST /furniture - CREANDO NUEVO PRODUCTO
╠════════════════════════════════════════════════════════════
║ Datos recibidos:
║   - Nombre: Silla Ergonómica Premium
║   - Descripción: Silla de oficina con soporte lumbar ajustable
║   - Precio venta: $1500.00
║   - Precio costo: $800.00
║   - Stock: 25
║   - Stock mínimo: 5
║   - Imagen URL: https://cdn.example.com/images/silla-ergonomica.jpg
║   - Marca ID: 1
║   - Categoría ID: 3
╠════════════════════════════════════════════════════════════
║ ✅ ÉXITO - Producto creado:
║   - ID: 42
║   - Nombre: Silla Ergonómica Premium
╚════════════════════════════════════════════════════════════
```

---

## ⚠️ Errores comunes

| Error | Causa | Solución |
|-------|-------|----------|
| 401 Unauthorized | Token expirado o no enviado | Asegúrate de tener `Authorization: Bearer <token>` en headers |
| 400 Bad Request | Falta categoryId | categoryId es obligatorio, asegúrate de seleccionar una categoría |
| 400 Bad Request | Precio <= 0 | price debe ser > 0 |
| 400 Bad Request | Nombre vacío | name no puede estar vacío |
| 500 Internal Server | Categoría no existe | Verifica que categoryId exista en la BD |

---

## 📋 Checklist de prueba

```
✅ ¿El token JWT está en localStorage?
✅ ¿El Authorization header se envía correctamente?
✅ ¿La categoría con categoryId existe en la base de datos?
✅ ¿El nombre del mueble tiene al menos 1 carácter?
✅ ¿El price es mayor a 0?
✅ ¿El costPrice es mayor a 0?
✅ ¿Los logs del backend muestran los datos recibidos?
✅ ¿La respuesta contiene status 201?
```


