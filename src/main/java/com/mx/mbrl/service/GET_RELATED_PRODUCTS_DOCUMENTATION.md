# Método getRelatedProducts - Documentación

## Descripción
El método `getRelatedProducts` en la clase `ProductService` busca y retorna productos relacionados a un producto específico. La búsqueda se basa en dos criterios principales:

1. **Misma Categoría**: Productos que pertenecen a la misma categoría que el producto actual.
2. **Palabras Clave en el Nombre**: Productos que contienen palabras clave similares en su nombre.

## Firma del Método
```java
@Transactional(readOnly = true)
public List<Product> getRelatedProducts(Long productId, int limit)
```

### Parámetros
- **productId** (Long): ID del producto para el cual se buscan productos relacionados
- **limit** (int): Número máximo de productos relacionados a retornar

### Retorno
- **List<Product>**: Lista de productos relacionados ordenados por relevancia

## Algoritmo

### Paso 1: Obtener el Producto Actual
- Se valida que el producto existe
- Se valida que el producto está activo (no eliminado)

### Paso 2: Buscar Productos de la Misma Categoría
```java
if (currentProduct.getCategory() != null) {
    List<Product> sameCategory = productRepository
        .findByCategoryIdExcludingProduct(currentProduct.getCategory().getId(), productId);
    relatedProducts.addAll(sameCategory);
}
```

### Paso 3: Buscar por Palabras Clave
- Se divide el nombre del producto en palabras
- Se buscan productos que contengan estas palabras (palabras > 2 caracteres)
- Se evitan duplicados

```java
String[] keywords = currentProduct.getName().split("\\s+");
for (String keyword : keywords) {
    if (keyword.length() > 2) { 
        List<Product> keywordMatches = productRepository.findByNameContainsIgnoreCase(keyword);
        // Filtrar duplicados...
    }
}
```

### Paso 4: Eliminar Duplicados
- Se crea un Map para eliminar productos duplicados

### Paso 5: Calcular Puntuación de Relevancia
Cada producto recibe una puntuación basada en:
- **70% - Ventas Totales**: Suma de cantidades vendidas (de OrderItem)
- **30% - Calificación Promedio**: Promedio de ratings de reseñas aprobadas (de Review)

```
Score = (Total Ventas × 0.7) + (Rating Promedio × 0.3)
```

### Paso 6: Ordenar y Limitar
- Los productos se ordenan por puntuación en orden descendente
- Se limitan los resultados al número especificado

## Métodos de Repositorio Agregados

### ProductRepository
```java
@Query("SELECT p FROM Product p WHERE p.isActive = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
List<Product> findByNameContainsIgnoreCase(@Param("keyword") String keyword);

@Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true AND p.id != :productId")
List<Product> findByCategoryIdExcludingProduct(@Param("categoryId") Long categoryId, @Param("productId") Long productId);
```

### OrderItemRepository
```java
@Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
Long countTotalSalesByProductId(@Param("productId") Long productId);
```

### ReviewRepository
```java
@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId AND r.isApproved = true")
Double getAverageRatingByProductId(@Param("productId") Long productId);
```

## Ejemplos de Uso

### Básico
```java
// Obtener 5 productos relacionados al producto con ID 1
List<Product> relatedProducts = productService.getRelatedProducts(1L, 5);
```

### En un Controlador REST
```java
@GetMapping("/products/{id}/related")
public ResponseEntity<List<ProductResponseDTO>> getRelatedProducts(
    @PathVariable Long id,
    @RequestParam(defaultValue = "5") int limit) {
    
    List<Product> products = productService.getRelatedProducts(id, limit);
    List<ProductResponseDTO> dtos = products.stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
    
    return ResponseEntity.ok(dtos);
}
```

## Ventajas

1. **Búsqueda Multi-criterio**: Combina categoría y palabras clave
2. **Ranking Inteligente**: Ordena por relevancia basada en ventas y ratings
3. **Excluye Duplicados**: Evita productos repetidos en los resultados
4. **Transacción ReadOnly**: Mejora el rendimiento para operaciones de lectura
5. **Logging Detallado**: Facilita el debugging y monitoreo

## Casos de Uso

- **Recomendaciones en Página de Producto**: Mostrar productos similares
- **Sugerencias de Compra Cruzada**: Recomendar productos relacionados al carrito
- **Búsqueda Inteligente**: Mejorar la experiencia de descubrimiento de productos

## Notas de Rendimiento

- Las queries JPQL se optimizan automáticamente por Hibernate
- Los Map internos evitan duplicados en memoria
- El Limit reduce el número de operaciones de cálculo de puntuación
- Considerar índices en las tablas: `products.name`, `products.category_id`

## Extensiones Futuras

- Agregar filtros por rango de precio
- Considerar el historial de compras del usuario
- Añadir factores de popularidad temporal
- Implementar machine learning para recomendaciones personalizadas

