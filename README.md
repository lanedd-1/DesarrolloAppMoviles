# Rincón Sur - Sistema de Conteo y Gestión de Accesos

## Descripción del Proyecto
Rincón Sur es una aplicación nativa para Android, desarrollada con Kotlin y Jetpack Compose. Su propósito es llevar el control del flujo de personas que ingresan a una tienda. El sistema permite a los encargados registrar de manera eficiente si el cliente viene de visita o va de tránsito, su rango etario, compañía y uso de bolsas. Toda la información se gestiona en tiempo real y se almacena localmente en el dispositivo.

## Equipo de Desarrollo (Duoc UC)
* **Cristopher Bustamante**
* **José Recabarren**
* **Brandon Banda**

## Funcionalidades Implementadas
* **Gestión de Sesiones por Puerta:** Permite abrir una sesión (por ejemplo, Acceso A1), registrar datos, pausar y retomar el conteo. El sistema utiliza subconsultas SQL para determinar automáticamente si una sesión está "En Curso" o "En Pausa".
* **Registro de Tráfico Ágil:** Interfaz táctil optimizada para registrar datos rápidamente sin generar demoras en la entrada. Incluye la opción de anular el último registro ingresado al instante en caso de error.
* **Sistema de Roles:** La aplicación adapta su interfaz según el usuario. El Observador tiene acceso solo a las herramientas de conteo, el Jefe de Tienda puede visualizar estadísticas, y el Administrador cuenta con control total del sistema.
* **Gestión de Usuarios:** Pantalla exclusiva para el Administrador que permite visualizar la lista del personal, modificar los roles asignados y activar o suspender cuentas directamente desde la aplicación.
* **Persistencia de Datos (SQLite):** La información de sesiones, registros y usuarios se almacena de forma local utilizando SQLite. El proyecto está estructurado bajo el patrón de arquitectura MVVM y utiliza BCrypt para la encriptación de contraseñas.

## Tecnologías Utilizadas
* **Lenguaje:** Kotlin
* **Interfaz Gráfica:** Jetpack Compose (Material Design 3)
* **Arquitectura:** MVVM (Model-View-ViewModel) implementando `StateFlow`
* **Base de Datos:** SQLite (mediante `SQLiteOpenHelper`)

## Pasos para Ejecutar el Proyecto
1. Descargar el archivo `.zip` del proyecto o clonar el repositorio en el equipo local.
2. Abrir el proyecto utilizando **Android Studio**.
3. Esperar a que Gradle sincronice y descargue las dependencias necesarias.
4. Conectar un dispositivo físico mediante USB (con depuración activada) o iniciar un emulador de Android (se recomienda API 24 o superior).
5. Presionar el botón de **Run** en Android Studio (o utilizar el atajo `Shift + F10`) para compilar e instalar la aplicación.

### Credenciales de Acceso
El sistema cuenta con datos precargados en la base de datos para facilitar las pruebas. Puede acceder con la siguiente cuenta de Administrador:
* **Correo:** admin@rinconsur.cl
* **Contraseña:** Rincor.2026!
