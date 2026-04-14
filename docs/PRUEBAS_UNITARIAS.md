# 📋 Pruebas Unitarias - AuthController

## 📊 Cobertura de Pruebas

Este archivo de pruebas (`AuthControllerTest.java`) cubre **todos los 9 endpoints** del AuthController con **24 casos de prueba** que cubren:

- ✅ Casos exitosos
- ✅ Casos de error
- ✅ Validaciones
- ✅ Autenticación y autorización
- ✅ Manejo de excepciones

---

## 🎯 Endpoints Probados

### 1. **POST /api/auth/register** (3 pruebas)
- ✅ `testRegisterSuccess` - Registro exitoso
- ✅ `testRegisterFailureDuplicateEmail` - Email duplicado
- ✅ `testRegisterFailureValidation` - Validación de campos

**Casos cubiertos:**
- Creación de cliente exitosa (HTTP 201)
- Email ya registrado (HTTP 400)
- Campos vacíos/inválidos (HTTP 400)

---

### 2. **POST /api/auth/login** (3 pruebas)
- ✅ `testLoginSuccess` - Login exitoso
- ✅ `testLoginFailureInvalidCredentials` - Credenciales inválidas
- ✅ `testLoginFailureUserNotFound` - Usuario no encontrado

**Casos cubiertos:**
- Login correcto con token JWT (HTTP 200)
- Credenciales incorrectas (HTTP 401)
- Usuario inexistente (HTTP 401)

---

### 3. **POST /api/auth/refresh** (2 pruebas)
- ✅ `testRefreshTokenSuccess` - Refrescar token exitoso
- ✅ `testRefreshTokenFailureInvalidToken` - Token inválido

**Casos cubiertos:**
- Nuevo access token generado (HTTP 200)
- Refresh token expirado/inválido (HTTP 401)

---

### 4. **POST /api/auth/logout** (3 pruebas)
- ✅ `testLogoutSuccess` - Logout exitoso
- ✅ `testLogoutWithoutParams` - Sin parámetros
- ✅ `testLogoutFailure` - Error en servicio

**Casos cubiertos:**
- Logout con tokens (HTTP 200)
- Logout sin parámetros (HTTP 200)
- Error en blacklist (HTTP 400)

---

### 5. **POST /api/auth/change-password** (3 pruebas)
- ✅ `testChangePasswordSuccess` - Cambio exitoso
- ✅ `testChangePasswordFailureWrongOldPassword` - Contraseña antigua incorrecta
- ✅ `testChangePasswordUnauthorized` - Sin autenticación

**Casos cubiertos:**
- Cambio exitoso (requiere autenticación, HTTP 200)
- Contraseña anterior inválida (HTTP 400)
- No autenticado (HTTP 401)

---

### 6. **GET /api/auth/password-status/{userId}** (4 pruebas)
- ✅ `testGetPasswordStatusSuccess` - Estado obtenido exitosamente
- ✅ `testGetPasswordStatusChangeRequired` - Cambio requerido
- ✅ `testGetPasswordStatusUnauthorized` - Sin autenticación
- ✅ `testGetPasswordStatusUserNotFound` - Usuario no encontrado

**Casos cubiertos:**
- Estado válido (HTTP 200)
- Expiración próxima (HTTP 200)
- No autenticado (HTTP 401)
- Usuario inexistente (HTTP 400)

---

### 7. **POST /api/auth/forgot-password** (2 pruebas)
- ✅ `testForgotPasswordSuccess` - Solicitud exitosa
- ✅ `testForgotPasswordEmailNotFound` - Email no encontrado (respuesta segura)

**Casos cubiertos:**
- Email registrado (HTTP 200)
- Email no existe (HTTP 200 - sin revelar información)

---

### 8. **POST /api/auth/reset-password** (3 pruebas)
- ✅ `testResetPasswordSuccess` - Reset exitoso
- ✅ `testResetPasswordFailureInvalidToken` - Token inválido
- ✅ `testResetPasswordFailureMismatchedPasswords` - Contraseñas no coinciden

**Casos cubiertos:**
- Reset exitoso (HTTP 200)
- Token expirado/inválido (HTTP 400)
- Contraseñas no coinciden (HTTP 400)

---

### 9. **GET /api/auth/validate-reset-token** (3 pruebas)
- ✅ `testValidateResetTokenSuccess` - Token válido
- ✅ `testValidateResetTokenInvalid` - Token inválido
- ✅ `testValidateResetTokenExpired` - Token expirado

**Casos cubiertos:**
- Validación correcta (HTTP 200, true)
- Token inválido (HTTP 200, false)
- Token expirado (HTTP 400)

---

## 🔧 Cómo Ejecutar las Pruebas

### Opción 1: Desde la línea de comandos
```bash
# Ejecutar solo las pruebas del AuthController
mvn test -Dtest=AuthControllerTest

# Ejecutar con salida detallada
mvn test -Dtest=AuthControllerTest -v

# Ejecutar con cobertura
mvn clean test jacoco:report -Dtest=AuthControllerTest
```

### Opción 2: Desde IntelliJ IDEA
1. Click derecho en `AuthControllerTest.java`
2. Seleccionar **"Run AuthControllerTest"**
3. O presionar **Ctrl + Shift + F10**

### Opción 3: Ejecutar una prueba específica
```bash
mvn test -Dtest=AuthControllerTest#testLoginSuccess
mvn test -Dtest=AuthControllerTest#testRegisterSuccess
```

---

## 📈 Tecnologías Utilizadas

| Herramienta | Propósito |
|---|---|
| **JUnit 5** | Framework de pruebas |
| **Mockito** | Mock de dependencias |
| **MockMvc** | Testing de endpoints HTTP |
| **@DisplayName** | Nombres descriptivos |
| **@WithMockUser** | Simular usuario autenticado |
| **@CrossOrigin CSRF** | Seguridad en pruebas |

---

## ✨ Características de las Pruebas

### 1. **Mockeo de Dependencias**
```java
@MockBean
private AuthService authService;

@MockBean
private PasswordResetService passwordResetService;
```

### 2. **Datos de Prueba Reutilizables**
```java
@BeforeEach
void setUp() {
    // Inicialización de datos
    customerRequestDTO = new CustomerRequestDTO();
    // ...
}
```

### 3. **Pruebas Parametrizadas**
Cada endpoint tiene múltiples casos:
- Caso exitoso (Happy path)
- Errores de validación
- Errores de negocio
- Errores de autorización

### 4. **Verificación de Llamadas**
```java
verify(authService, times(1)).login(any(LoginRequestDTO.class));
verify(authService, never()).register(any());
```

### 5. **Seguridad**
- `@WithMockUser` para autenticación
- `with(csrf())` para protección CSRF
- Validación de roles

---

## 📊 Matriz de Cobertura

| Endpoint | Exitoso | Fallo | Validación | Autenticación | Total |
|---|:---:|:---:|:---:|:---:|:---:|
| POST /register | ✅ | ✅ | ✅ | - | 3 |
| POST /login | ✅ | ✅ | ✅ | - | 3 |
| POST /refresh | ✅ | ✅ | - | - | 2 |
| POST /logout | ✅ | ✅ | ✅ | - | 3 |
| POST /change-password | ✅ | ✅ | ✅ | ✅ | 3 |
| GET /password-status | ✅ | ✅ | ✅ | ✅ | 4 |
| POST /forgot-password | ✅ | ✅ | - | - | 2 |
| POST /reset-password | ✅ | ✅ | ✅ | - | 3 |
| GET /validate-reset-token | ✅ | ✅ | ✅ | - | 3 |
| **TOTAL** | **9** | **9** | **8** | **4** | **24** |

---

## 🎯 Convenciones Usadas

### Nombres de Pruebas
```
test[Acción][Escenario][Resultado]

Ejemplos:
- testLoginSuccess
- testChangePasswordFailureWrongOldPassword
- testValidateResetTokenExpired
```

### Estructura AAA (Arrange-Act-Assert)
```java
void testExample() {
    // ARRANGE: Preparar datos
    when(service.method()).thenReturn(expected);
    
    // ACT: Ejecutar acción
    mockMvc.perform(post("/api/endpoint"))
    
    // ASSERT: Verificar resultados
    .andExpect(status().isOk())
}
```

---

## 🔍 Ejemplo de Prueba

```java
@Test
@DisplayName("Login exitoso")
void testLoginSuccess() throws Exception {
    // Arrange
    when(authService.login(any(LoginRequestDTO.class)))
        .thenReturn(jwtResponse);

    // Act & Assert
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.token").exists());

    verify(authService, times(1))
        .login(any(LoginRequestDTO.class));
}
```

---

## 🚀 Próximos Pasos

Para mejorar aún más la cobertura:

1. **Agregar pruebas de integración** - Probar con base de datos real
2. **Pruebas de seguridad** - XSS, SQL Injection, etc.
3. **Pruebas de rendimiento** - Load testing
4. **Cobertura de código** - Usar Jacoco para medir %
5. **Pruebas de DTOs** - Validaciones de campos
6. **Test de TokenBlacklist** - Si existe ese servicio

---

## 💡 Notas Importantes

- ✅ Todas las pruebas usan `@WithMockUser` para endpoints protegidos
- ✅ Se validan mensajes de respuesta y códigos HTTP
- ✅ Se verifica que se llamen los métodos correctos
- ✅ Se manejan excepciones correctamente
- ✅ Siguiendo patrones de Spring Boot Testing

---

**Generado:** 2026-04-07
**Pruebas Total:** 24
**Endpoints Cubiertos:** 9/9 (100%)

