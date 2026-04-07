# IMPLEMENTACIÓN - getRelatedProducts en ProductService

## Resumen
Se ha implementado exitosamente el método `getRelatedProducts` en `ProductService` que busca y retorna productos relacionados a un producto específico. La búsqueda se basa en la categoría y palabras clave del nombre, ordenando los resultados por relevancia (ventas y calificaciones).

---

## Cambios Realizados

### 1. **ProductService.java** 
Ubicación: `src/main/java/com/mx/mbrl/service/ProductService.java`

**Método agregado:**
```java
@Transactional(readOnly = true)
public List<Product> getRelatedProducts(Long productId, int limit) {
    // Busca productos relacionados basados en:
    // 1. Misma categoría
    // 2. Palabras clave en el nombre
    // 3. Excluye el producto actual
    // 4. Ordena por ventas (70%) y calificaciones (30%)
    // 5. Retorna máximo 'limit' productos
}
```

**Inyecciones agregadas:**
- `OrderItemRepository orderItemRepository` - Para contar ventas
- `ReviewRepository reviewRepository` - Para obtener calificaciones

---

### 2. **ProductRepository.java**
Ubicación: `src/main/java/com/mx/mbrl/repository/ProductRepository.java`

**Métodos agregados:**
```java
@Query("SELECT p FROM Product p WHERE p.isActive = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
List<Product> findByNameContainsIgnoreCase(@Param("keyword") String keyword);

@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true AND p.id != :productId")
List<Product> findByCategoryIdExcludingProduct(@Param("categoryId") Long categoryId, @Param("productId") Long productId);
```

---

### 3. **OrderItemRepository.java**
Ubicación: `src/main/java/com/mx/mbrl/repository/OrderItemRepository.java`

**Métodos agregados:**
```java
@Query("SELECT oi.product.id, SUM(oi.quantity) as totalSold FROM OrderItem oi WHERE oi.product.id IN :productIds GROUP BY oi.product.id")
List<Object[]> countSalesByProductIds(@Param("productIds") List<Long> productIds);

@Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
Long countTotalSalesByProductId(@Param("productId") Long productId);
```

---

### 4. **ReviewRepository.java**
Ubicación: `src/main/java/com/mx/mbrl/repository/ReviewRepository.java`

**Métodos agregados:**
```java
@Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
Long countApprovedReviewsByProductId(@Param("productId") Long productId);

@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
Double getAverageRatingByProductId(@Param("productId") Long productId);
```

---

### 5. **ProductController.java**
Ubicación: `src/main/java/com/mx/mbrl/controller/ProductController.java`

**Endpoint agregado:**
```java
@GetMapping("/{id}/related")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<ApiResponse<List<ProductResponseDTO>>> getRelatedProducts(
    @PathVariable Long id,
    @RequestParam(defaultValue = "5") int limit)
```

**URL de acceso:**
```
GET /api/products/{id}/related?limit=5
```

---

### 6. **ProductServiceTest.java** (NUEVO)
Ubicación: `src/test/java/com/mx/mbrl/service/ProductServiceTest.java`

**Tests implementados:**
- `testGetRelatedProductsSameCategory()` - Verifica búsqueda por categoría
- `testGetRelatedProductsExcludesCurrentProduct()` - Verifica exclusión del producto actual
- `testGetRelatedProductsWithLimit()` - Verifica limitación de resultados
- `testGetRelatedProductsSortedByRelevance()` - Verifica ordenamiento por relevancia

---

## Flujo del Algoritmo

```
┌─────────────────────────────────────────────────┐
│ 1. getRelatedProducts(productId, limit)          │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
        ┌──────────────────────────────┐
        │ 2. Obtener producto actual    │
        │ (validar existencia/activo)   │
        └────────────────┬─────────────┘
                         │
                    ┌────┴────┐
                    ▼         ▼
        ┌─────────────────┐ ┌──────────────────┐
        │ 3A. Productos de│ │ 3B. Productos con│
        │ la categoría    │ │ palabras clave   │
        └────────┬────────┘ └────────┬─────────┘
                 │                   │
                 └────────┬──────────┘
                          │
                          ▼
        ┌──────────────────────────────┐
        │ 4. Eliminar duplicados       │
        └────────────────┬─────────────┘
                         │
                         ▼
        ┌──────────────────────────────┐
        │ 5. Calcular puntuación:      │
        │ (Ventas × 0.7) +             │
        │ (Rating × 0.3)               │
        └────────────────┬─────────────┘
                         │
                         ▼
        ┌──────────────────────────────┐
        │ 6. Ordenar descendentemente  │
        └────────────────┬─────────────┘
                         │
                         ▼
        ┌──────────────────────────────┐
        │ 7. Limitar a 'limit' items   │
        └────────────────┬─────────────┘
                         │
                         ▼
        ┌──────────────────────────────┐
        │ Retornar lista de productos  │
        └──────────────────────────────┘
```

---

## Ejemplos de Uso

### Desde Java
```java
// Inyectar ProductService
@Autowired
private ProductService productService;

// Obtener 5 productos relacionados
List<Product> relatedProducts = productService.getRelatedProducts(1L, 5);
```

### Desde REST API
```bash
# Obtener 5 productos relacionados (default)
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/products/1/related

# Obtener 10 productos relacionados
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/products/1/related?limit=10
```

### Respuesta Exitosa (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 2,
      "name": "Laptop Stand",
      "price": 49.99,
      "category": "Electronics",
      "stock": 25,
      "isActive": true
    },
    {
      "id": 3,
      "name": "Computer Gaming Desktop",
      "price": 1299.99,
      "category": "Electronics",
      "stock": 5,
      "isActive": true
    }
  ],
  "message": "Productos relacionados obtenidos exitosamente"
}
```

### Respuesta Error (404 Not Found)
```json
{
  "success": false,
  "data": null,
  "message": "Producto no encontrado con ID: 999",
  "statusCode": 404
}
```

---

## Características Principales

✅ **Búsqueda Multi-Criterio**
- Por categoría
- Por palabras clave en nombre

✅ **Ranking Inteligente**
- 70% peso: Cantidad de ventas
- 30% peso: Calificación promedio

✅ **Manejo de Datos**
- Excluye producto actual
- Elimina duplicados
- Limita resultados

✅ **Rendimiento**
- Transacción readOnly
- Queries JPQL optimizadas
- Índices recomendados en BD

✅ **Seguridad**
- Requiere autenticación
- Validaciones de entrada
- Manejo de excepciones

✅ **Observabilidad**
- Logging detallado
- Trazabilidad de operaciones

---

## Consideraciones de Base de Datos

Para optimizar el rendimiento, se recomienda crear índices en:

```sql
-- Índices en Products
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_name ON products(name(100));
CREATE INDEX idx_products_is_active ON products(is_active);

-- Índices en OrderItems
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- Índices en Reviews
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_is_approved ON reviews(is_approved);
```

---

## Extensiones Futuras

💡 **Posibles mejoras:**
1. Cache de resultados con Redis/Memcached
2. Filtros por rango de precio
3. Historial de compras del usuario (ML)
4. Recomendaciones personalizadas
5. Análisis de tendencias temporales
6. Integración con algoritmos de ML

---

## Archivos Modificados/Creados

| Archivo | Tipo | Cambios |
|---------|------|---------|
| ProductService.java | Modificado | Agregado método `getRelatedProducts` |
| ProductRepository.java | Modificado | 2 métodos @Query |
| OrderItemRepository.java | Modificado | 2 métodos @Query |
| ReviewRepository.java | Modificado | 2 métodos @Query |
| ProductController.java | Modificado | Endpoint GET /{id}/related |
| ProductServiceTest.java | **NUEVO** | 4 tests unitarios |
| GET_RELATED_PRODUCTS_DOCUMENTATION.md | **NUEVO** | Documentación técnica |

---

## Validación

Para validar la implementación:

```bash
# 1. Compilar
mvnw clean compile

# 2. Ejecutar pruebas
mvnw test -Dtest=ProductServiceTest

# 3. Ejecutar aplicación
mvnw spring-boot:run

# 4. Probar endpoint
curl -H "Authorization: Bearer {token}" \
  http://localhost:8080/api/products/1/related?limit=5
```

---

**Implementación completada ✓**

