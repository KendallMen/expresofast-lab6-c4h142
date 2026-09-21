# ExpresoFast - Laboratorio 6

**Curso:** IF0009 - Desarrollo de Software IV  
**Semestre:** II-2026  
**Laboratorio:** Plataforma Full-Stack de Logistica, Parte II  
**Carnet:** C4H142  
**Estudiante:** Kendall Méndez Calderón

## Alcance

ExpresoFast es una API REST para gestionar envios, vehiculos, conductores y empresas logisticas. Esta version agrega autenticacion JWT, control RBAC, DTOs validados, bitacora de auditoria y un frontend protegido.

## Requisitos

- Java 25 instalado (compatible con la configuracion actual del proyecto).
- Maven Wrapper incluido (`mvnw.cmd`).
- SQL Server Developer Edition en `localhost:1433`.
- Un navegador moderno.

## Configuracion de base de datos

1. Cree o use la base `ExpresoFast_C4H142_II2026` en SQL Server.
2. Ejecute en SSMS, en este orden:
   - `database/01_schema_lab5.sql`
   - `database/02_schema_lab6_extension.sql`
   - `database/03_data_seeds.sql`
3. Copie `application.properties.template` como `src/main/resources/application.properties`.
4. Configure el usuario y la contrasena de SQL Server y una clave JWT aleatoria de al menos 256 bits.
5. Para una base ya existente puede usar temporalmente `spring.jpa.hibernate.ddl-auto=update`; para una entrega reproducible se recomienda `validate`.

## Usuarios de prueba

Todos usan la contrasena `Password123!`.

| Usuario | Rol |
| --- | --- |
| `admin` | `ROLE_ADMIN` |
| `operador1` | `ROLE_OPERADOR` |
| `conductor1` | `ROLE_CONDUCTOR` |

Las contrasenas se almacenan como hashes BCrypt en `database/03_data_seeds.sql`.

## Ejecucion del backend

Desde la raiz del proyecto:

```cmd
mvnw.cmd test
mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Ejecucion del frontend

Con el backend encendido, sirva la carpeta `frontend` desde un servidor local. Por ejemplo, con la extension Live Server de VS Code abra `frontend/login.html`, o use cualquier servidor estatico equivalente. No abra el HTML directamente con `file://`, porque el navegador puede bloquear las solicitudes CORS.

## Endpoints principales

- `POST /api/auth/login` - publico.
- `GET /api/envios/optimizados` - cualquier rol autenticado.
- `POST /api/envios` - admin u operador.
- `PATCH /api/envios/{id}/estado` - admin o conductor.
- `GET /api/envios/{id}/bitacora` - admin u operador.
- `/api/vehiculos/**` - admin

## Pruebas y cobertura
Ejecutar pruebas: `mvn clean test`

Ejecutar suite completa con verificación de cobertura: `mvn clean verify`

Ver reporte de cobertura: abrir `backend/target/site/jacoco/index.html` en el navegador.

Umbral mínimo exigido: 85% de instrucciones cubiertas en el paquete `business`.

