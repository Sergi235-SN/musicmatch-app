# 🎵 MusicMatch

**MusicMatch** es una aplicación orientada a conectar músicos según sus intereses, instrumentos, estilos musicales y nivel de experiencia.  
El proyecto está compuesto por un **backend desarrollado con Spring Boot**, una **aplicación Android** y una infraestructura de despliegue basada en **Docker**.

---

## 📌 Descripción general

La aplicación permite que los usuarios puedan:

- registrarse y verificar su cuenta por correo,
- configurar su perfil musical,
- buscar otros músicos por filtros,
- recibir coincidencias,
- iniciar conversaciones con otros usuarios compatibles.

Esta versión está preparada para su distribución y despliegue como parte del **Trabajo de Fin de Grado (TFG)**.

---

## 📦 Contenido del release

Este paquete incluye:

- `backend-image.tar` → imagen Docker del backend
- `MusicMatch-oficial.apk` → aplicación Android
- `docker-compose.yml` → orquestación de servicios
- `README.md` → guía de instalación y configuración

---

## ⚙️ Requisitos previos

Para ejecutar el proyecto se necesita:

- **Docker** o **Docker Desktop**
- **Docker Compose**
- un dispositivo Android o emulador para instalar la APK
- conexión de red entre el dispositivo Android y el equipo donde se ejecuta el backend

---

## 🚀 Puesta en marcha

### 1. Descargar y descomprimir

Descargar el release y descomprimir su contenido en una carpeta local.

### 2. Cargar la imagen Docker del backend

#### Windows (PowerShell)

    docker load -i backend-image.tar

#### Linux / macOS

    docker load < backend-image.tar

### 3. Configurar variables de entorno

Antes de levantar los servicios, conviene revisar las variables de entorno, especialmente si se va a usar correo real o si el despliegue no es local.

Las variables pueden definirse:

- en el archivo `docker-compose.yml`,
- en un archivo `.env`,
- o directamente en el entorno del sistema.

### 4. Levantar los servicios

    docker-compose up

Si se desea ejecutar en segundo plano:

    docker-compose up -d

---

## 🔐 Configuración principal del backend

El backend admite configuración externa mediante variables de entorno.

---

## 🗄️ Variables de base de datos

### `DB_URL`
URL de conexión a la base de datos MySQL.

Ejemplo:

    DB_URL=jdbc:mysql://localhost:3306/musicmatch

### `DB_USER`
Usuario de la base de datos.

    DB_USER=musicuser

### `DB_PASS`
Contraseña de la base de datos.

    DB_PASS=musicpass

---

## 🧩 Configuración JPA / Hibernate

### `SHOW_SQL`
Activa o desactiva la impresión de consultas SQL en consola.

    SHOW_SQL=true

o

    SHOW_SQL=false

> El esquema de la base de datos se genera automáticamente mediante Hibernate.

---

## 🔑 Configuración JWT

### `JWT_SECRET`
Clave secreta usada para firmar los tokens JWT.

**Debe configurarse obligatoriamente en un entorno real** y tener longitud suficiente.

Ejemplo:

    JWT_SECRET=una_clave_larga_segura_de_al_menos_32_caracteres

### `JWT_ACCESS_EXPIRATION`
Duración del access token en milisegundos.

Valor habitual:

    JWT_ACCESS_EXPIRATION=900000

Equivale a 15 minutos.

### `JWT_REFRESH_EXPIRATION`
Duración del refresh token en milisegundos.

Valor habitual:

    JWT_REFRESH_EXPIRATION=604800000

Equivale a 7 días.

---

## ✉️ Configuración del correo

MusicMatch puede funcionar de dos maneras distintas con respecto al envío de correos:

### Opción 1: modo con SMTP real

En este modo, la aplicación **envía correos reales** de:

- verificación de cuenta,
- recuperación de contraseña.

Para ello hay que activar:

    MAIL_ENABLED=true

Y además configurar correctamente el servidor SMTP.

Variables necesarias:

### `SPRING_MAIL_HOST`
Servidor SMTP.

Ejemplo:

    SPRING_MAIL_HOST=smtp.gmail.com

### `SPRING_MAIL_PORT`
Puerto SMTP.

Ejemplo:

    SPRING_MAIL_PORT=587

### `SPRING_MAIL_USERNAME`
Cuenta de correo desde la que se enviarán los mensajes.

Ejemplo:

    SPRING_MAIL_USERNAME=tu_correo@gmail.com

### `SPRING_MAIL_PASSWORD`
Contraseña o clave de aplicación del correo.

Ejemplo:

    SPRING_MAIL_PASSWORD=tu_clave_de_aplicacion

### Variables SMTP adicionales

    SPRING_MAIL_SMTP_AUTH=true
    SPRING_MAIL_SMTP_STARTTLS=true
    SPRING_MAIL_CONNECTION_TIMEOUT=5000
    SPRING_MAIL_TIMEOUT=3000
    SPRING_MAIL_WRITE_TIMEOUT=5000

### `APP_MAIL_FROM`
Dirección que aparecerá como remitente.

Ejemplo:

    APP_MAIL_FROM=no-reply@musicmatch.com

---

### Opción 2: modo sin SMTP

En este modo, la aplicación **no envía correos reales**.

Se activa así:

    MAIL_ENABLED=false

Cuando SMTP está desactivado:

- no se intenta conectar a un proveedor de correo,
- el backend usa un servicio de pruebas o mock,
- los correos no llegan al usuario,
- resulta útil para desarrollo local o pruebas internas.

Este modo es recomendable cuando:

- todavía no se dispone de una cuenta SMTP configurada,
- se está probando el backend en local,
- solo se quiere validar la lógica sin enviar emails reales.

#### ¿Cómo se obtienen entonces los enlaces de verificación o recuperación?

Cuando `MAIL_ENABLED=false`, el backend imprime en consola el enlace correspondiente de verificación de cuenta o restablecimiento de contraseña.

Si la aplicación se está ejecutando con Docker, estos enlaces pueden consultarse en los **logs del contenedor del backend**.

Si se ha arrancado con:

    docker-compose up

los enlaces se verán directamente en la terminal.

Si se ha arrancado en segundo plano con:

    docker-compose up -d

pueden consultarse con:

    docker-compose logs -f

o bien solo los del backend con:

    docker-compose logs -f backend

En este modo, el usuario final no recibe ningún correo, por lo que el proceso de verificación o recuperación depende de consultar manualmente los logs del backend.

---

## 🔗 URLs de verificación y recuperación

Estas variables determinan a qué dirección se envía al usuario cuando pulsa el botón del correo.

### `APP_VERIFY_EMAIL_URL_BASE`
URL base para verificar el correo.

Ejemplo:

    APP_VERIFY_EMAIL_URL_BASE=http://localhost:8080/api/auth/verify-email?token=

### `APP_RESET_PASSWORD_URL_BASE`
URL base para restablecer la contraseña.

Ejemplo:

    APP_RESET_PASSWORD_URL_BASE=http://localhost:8080/api/auth/reset-password-page?token=

> En un despliegue real, estas URLs deben apuntar a una IP o dominio accesible por el usuario.

---

## 🗄️ Base de datos

### Creación del esquema

La base de datos se configura automáticamente mediante Hibernate al arrancar la aplicación.

### Carga de datos iniciales

El backend incluye un inicializador que inserta automáticamente:

- instrumentos,
- estilos musicales,
- ciudades.

Estos datos **solo se insertan si la base de datos está vacía**.

### Reinicio completo de la base de datos

Si se desea eliminar volúmenes y reconstruir el estado inicial:

    docker-compose down -v
    docker-compose up

---

## 📱 Aplicación Android

La aplicación cliente se encuentra en el archivo:

`MusicMatch-oficial.apk`

Para instalarla, basta con transferirla al dispositivo Android y ejecutar la instalación.

### Configuración de conexión

Al iniciar la aplicación, el usuario debe introducir la **IP del equipo donde se está ejecutando el backend**, para que la app pueda conectarse correctamente.

Ejemplo:

    192.168.1.35

El backend responderá por defecto en:

    http://192.168.1.35:8080

> El dispositivo Android y el equipo servidor deben estar en la misma red local, salvo que el backend se haya publicado externamente.

---

## 🔍 Comprobación del backend

Endpoint de prueba:

    GET /api/auth/ping

Ejemplo completo:

    http://192.168.1.35:8080/api/auth/ping

Si responde `ok`, el backend está funcionando correctamente.

---

## 📁 Estructura del proyecto

    MusicMatch/
    ├── backend/
    ├── mobile/
    ├── infrastructure/
    │   └── docker-compose.yml
    ├── .gitignore
    ├── LICENSE
    └── README.md

---

## ⚠️ Problemas comunes

### 1. Error al cargar la imagen Docker

En Windows PowerShell:

    docker load -i backend-image.tar

En Linux/macOS:

    docker load < backend-image.tar

### 2. La app Android no conecta con el backend

Comprobar:

- que Docker esté levantado,
- que el backend esté corriendo en el puerto `8080`,
- que la IP introducida en la app sea correcta,
- que el móvil y el servidor estén en la misma red,
- que no haya firewall bloqueando el puerto.

### 3. No se reciben correos de verificación o recuperación

Comprobar:

- que `MAIL_ENABLED=true`,
- que las credenciales SMTP sean correctas,
- que el proveedor de correo permita acceso SMTP,
- que `APP_VERIFY_EMAIL_URL_BASE` y `APP_RESET_PASSWORD_URL_BASE` estén bien configuradas.

### 4. En modo sin SMTP no llega ningún correo

Esto es el comportamiento esperado cuando:

    MAIL_ENABLED=false

En ese caso, los enlaces de verificación y recuperación deben consultarse manualmente en los logs del backend:

    docker-compose logs -f backend

### 5. Los datos iniciales no aparecen

Si ya existían datos previos en la base de datos, el inicializador no volverá a insertarlos.

Para forzar un reinicio completo:

    docker-compose down -v
    docker-compose up

### 6. La verificación por correo o el cambio de contraseña no funciona

Revisar:

- `APP_VERIFY_EMAIL_URL_BASE`
- `APP_RESET_PASSWORD_URL_BASE`

Estas variables deben apuntar a una dirección válida y accesible desde el correo recibido por el usuario.

---

## 🛠️ Tecnologías empleadas

- **Spring Boot**
- **Spring Security**
- **Spring Data JPA / Hibernate**
- **MySQL**
- **Android (Jetpack Compose)**
- **Docker**
- **Docker Compose**
- **JWT**
- **GitHub Actions**

---

## 📚 Contexto académico

Este proyecto ha sido desarrollado como parte de un **Trabajo de Fin de Grado (TFG)** del ciclo **Desarrollo de Aplicaciones Multiplataforma (DAM)**, integrando conocimientos relacionados con:

- desarrollo backend y frontend,
- autenticación segura,
- persistencia de datos,
- despliegue en contenedores,
- integración de servicios,
- diseño de una arquitectura cliente-servidor.

---

## 👨‍💻 Autor

**Sergio Sánchez**