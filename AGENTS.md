# AGENTS.md

## Estado actual del proyecto
- Proyecto Maven con wrapper (`mvnw`, `mvnw.cmd`) y Spring Boot `3.5.5` sobre Java `17`; ver `pom.xml`.
- El código de aplicación todavía es mínimo: solo existe el bootstrap `src/main/java/com/mx/mbrl/MbrlApplication.java` con `@SpringBootApplication`.
- Las únicas pruebas actuales son de arranque de contexto en `src/test/java/com/mx/mbrl/MbrlApplicationTests.java` (`@SpringBootTest`).
- `src/main/resources/application.properties` solo define `spring.application.name=mbrl`; no hay configuración de base de datos, seguridad, perfiles ni logging.
- `src/main/resources/static/` y `src/main/resources/templates/` existen pero están vacíos.

## Stack e implicaciones reales
- Dependencias activas: Web, Data JPA, Security, Validation, MySQL runtime y Lombok; todo sale de `pom.xml`.
- No hay controladores, servicios, repositorios, entidades ni clases de configuración todavía. Antes de extender el sistema, crea esos paquetes y manténlos bajo `com.mx.mbrl` para que `@SpringBootApplication` los escanee sin configuración extra.
- `spring-boot-starter-security` aparece dos veces en `pom.xml`. Si tocas dependencias, consolida esa duplicación en vez de añadir otra entrada similar.
- Lombok está habilitado también como annotation processor en `maven-compiler-plugin`; si agregas DTOs/entidades con Lombok, no hace falta configurar otro procesador.

## Flujo de trabajo útil para agentes
- En Windows PowerShell usa el wrapper: `.\mvnw.cmd test`, `.\mvnw.cmd spring-boot:run`, `.\mvnw.cmd clean package`.
- En shells tipo Unix usa `./mvnw` con los mismos goals.
- En este workspace, `.\mvnw.cmd test` falla ahora mismo porque `JAVA_HOME` no está definido correctamente; confirma Java antes de reportar fallos de código.
- `target/` ya existe por compilaciones previas, pero está ignorado en `.gitignore`; no lo edites ni lo uses como fuente de verdad.

## Convenciones observables del repo
- Sigue la indentación existente con tabulaciones en los archivos Java y XML (`pom.xml`, `MbrlApplication.java`).
- `HELP.md` es el archivo boilerplate generado por Spring Initializr; úsalo solo como referencia genérica del stack, no como documentación del dominio.
- Como solo hay un test de contexto, cualquier nueva funcionalidad importante debería venir con pruebas enfocadas por capa (por ejemplo, MVC/JPA) en vez de depender solo de `contextLoads()`.

## Puntos de integración a considerar al implementar
- Si añades persistencia real, tendrás que completar `application.properties` con datasource/JPA para MySQL; hoy no hay URL, usuario ni contraseña.
- Si añades endpoints HTTP sin configuración de seguridad explícita, recuerda que el starter de Security ya está presente y condicionará el acceso.
- Si introduces vistas server-side, `templates/` es el sitio previsto; si construyes frontend estático, usa `static/`.

## Archivos clave a leer primero
- `pom.xml`
- `src/main/java/com/mx/mbrl/MbrlApplication.java`
- `src/main/resources/application.properties`
- `src/test/java/com/mx/mbrl/MbrlApplicationTests.java`
- `.gitignore`

## REGLA MUY IMPORTANTE PARA AGENTES
- NO HAGAS ARCHIVOS MD (Markdown) EN EL REPO. SI NO SE TE SOLICITA , NO LO HAGAS Y NO HAGAS 
- ARCHIVOS ESCRITOS EN FORMATO DE DOCUMENTACIÓN. SOLO HAZ CÓDIGOS FUENTE EN JAVA, XML O PROPERTIES. SI SE TE SOLICITA DOCUMENTACIÓN, HAZLA EN FORMATO DE COMENTARIOS DENTRO DE LOS ARCHIVOS JAVA O XML, PERO NUNCA COMO ARCHIVOS SEPARADOS. SI SE TE SOLICITA UN RESUMEN O EXPLICACIÓN, HAZLO COMO COMENTARIOS EN EL CÓDIGOS FUENTE, NO COMO ARCHIVOS MD.