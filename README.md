# Rincón Sur - Sistema de Conteo y Gestión de Accesos

## Descripción del Proyecto
Rincón Sur es una aplicación nativa para Android, desarrollada con Kotlin y Jetpack Compose. Su propósito es llevar el control del flujo de personas que ingresan a una tienda. El sistema permite a los encargados registrar de manera eficiente si el cliente viene de visita o va de tránsito, su rango etario, compañía y uso de bolsas. La información se gestiona a través de microservicios Spring Boot y se almacena en bases de datos MySQL. La app se comunica con ellos mediante Retrofit, a través de un API Gateway y un servidor Eureka.

Los microservicios están en otro repositorio: [https://github.com/Elkriu/MicroServicios-Rincon-Sur.git]

## Equipo de Desarrollo (Duoc UC)
* **Cristopher Bustamante**
* **José Recabarren**
* **Brandon Banda**

## Funcionalidades Implementadas
* **Gestión de Sesiones por Puerta:** Permite abrir una sesión (por ejemplo, Acceso A1), registrar datos y finalizarla. Los datos se guardan en el microservicio de visitantes.
* **Registro de Tráfico Ágil:** Interfaz táctil optimizada para registrar datos rápidamente sin generar demoras en la entrada. Incluye la opción de anular el último registro ingresado al instante en caso de error.
* **Sistema de Roles:** La aplicación adapta su interfaz según el usuario. El Observador tiene acceso solo a las herramientas de conteo, el Jefe de Tienda puede visualizar estadísticas, y el Administrador cuenta con control total del sistema.
* **Gestión de Usuarios:** Pantalla exclusiva para el Administrador que permite visualizar la lista del personal, modificar los roles asignados y activar o suspender cuentas directamente desde la aplicación.
* **Gestión de Accesos:** Permite administrar la disponibilidad de los accesos de la tienda.
* **Dashboard:** Resumen diario, gráficos de distribución por categorías y detalle de los registros, con opción de editar y eliminar registros.
* **Recuperación de Contraseña:** Envío de un código de 6 dígitos, verificación del código y cambio de contraseña.
* **Arquitectura y Conexión a Servicios:** El proyecto está estructurado bajo el patrón MVVM, con repositorios que consumen la API mediante Retrofit. Los errores de conexión y del servidor se manejan de forma centralizada.

## Tecnologías Utilizadas
* **Lenguaje:** Kotlin
* **Interfaz Gráfica:** Jetpack Compose (Material Design 3)
* **Arquitectura:** MVVM (Model-View-ViewModel) implementando `StateFlow`
* **Consumo de API:** Retrofit, Gson y OkHttp
* **Backend:** Microservicios Spring Boot con MySQL, Eureka Server y API Gateway
* **Pruebas:** JUnit, MockK y kotlinx-coroutines-test

## Endpoints Utilizados
La app solo llama al API Gateway (puerto 8080), que enruta cada petición al microservicio correspondiente usando Eureka.

**Usuarios** (`/api/usuarios`, `/api/roles`)
* `POST /api/usuarios/login`
* `POST /api/usuarios`
* `GET /api/usuarios`
* `GET /api/usuarios/{id}`
* `PUT /api/usuarios/{id}`
* `PUT /api/usuarios/{id}/estado`
* `PUT /api/usuarios/{id}/rol`
* `PUT /api/usuarios/{id}/contrasena`
* `POST /api/usuarios/recuperacion/solicitar`
* `POST /api/usuarios/recuperacion/verificar`
* `POST /api/usuarios/recuperacion/restablecer`
* `GET /api/roles`
* `POST /api/usuarios/{id}/foto` y `GET /api/usuarios/{id}/foto`

**Visitantes** (`/api/sesiones`, `/api/registros`)
* `GET /api/sesiones`, `GET /api/sesiones/estadisticas`, `GET /api/sesiones/{id}`, `GET /api/sesiones/activas`
* `POST /api/sesiones`
* `PUT /api/sesiones/{id}/finalizar`
* `GET /api/sesiones/{id}/registros`
* `POST /api/sesiones/{id}/registros`
* `DELETE /api/sesiones/{id}/registros/ultimo`
* `PUT /api/registros/{id}` y `DELETE /api/registros/{id}`

**Administración** (`/api/accesos`)
* `GET /api/accesos`
* `PUT /api/accesos/{nombre}/disponibilidad`

**Dashboard** (`/api/dashboard`)
* `GET /api/dashboard/fechas`
* `GET /api/dashboard/resumen`
* `GET /api/dashboard/distribucion`
* `GET /api/dashboard/registros`

## Pasos para Ejecutar el Proyecto
1. Levantar los microservicios siguiendo el README del repositorio de microservicios: base de datos en Laragon, Eureka, los cuatro microservicios y el gateway, en ese orden. Comprobar en `http://localhost:8761` que los servicios aparecen registrados.
2. Descargar el archivo `.zip` de la app o clonar este repositorio en el equipo local.
3. Abrir el proyecto utilizando **Android Studio**.
4. Esperar a que Gradle sincronice y descargue las dependencias necesarias.
5. Iniciar un emulador de Android (se recomienda API 24 o superior). La app llega al PC mediante `10.0.2.2`.
6. Presionar el botón de **Run** en Android Studio (o utilizar el atajo `Shift + F10`) para compilar e instalar la aplicación.

Si se usa un dispositivo físico, hay que cambiar la IP en `RetrofitClient` por la IP del PC en la red local y permitir el puerto 8080 en el firewall.

### Credenciales de Acceso
Para facilitar las pruebas, la base de datos de usuarios cuenta con una cuenta de Administrador:
* **Correo:** admin@rinconsur.cl
* **Contraseña:** Rincor.2026!

## Pruebas Unitarias
Pruebas de lógica de los ViewModels con JUnit, MockK y kotlinx-coroutines-test. Se ejecutan con `./gradlew testDebugUnitTest` o desde Android Studio, con clic derecho sobre la carpeta de pruebas y `Run Tests`.
