# 🎵 MusicMatch

Aplicación para conectar músicos según sus intereses, instrumentos y estilos musicales.

---

## 📦 Contenido del release

Este paquete incluye:

- `backend-image.tar` → Imagen Docker del backend  
- `MusicMatch-oficial.apk` → Aplicación Android  
- `docker-compose.yml` → Orquestación de servicios  

---

## ⚙️ Requisitos

- Docker o Docker Desktop  
- Docker Compose  

---

## 🚀 Instalación

### 1. Descargar

Descargar y descomprimir el release.

---

### 2. Cargar imagen Docker

Windows (PowerShell):

```powershell
docker load -i backend-image.tar
```

Linux / Mac:

```bash
docker load < backend-image.tar
```

---

### 3. Levantar servicios

```bash
docker-compose up
```

---

## 🗄️ Base de datos

- El esquema se crea automáticamente con Hibernate  
- Los datos iniciales (instrumentos, estilos, ciudades) se cargan desde el backend al arrancar  

⚠️ Solo se insertan si la base de datos está vacía  

Reset completo:

```bash
docker-compose down -v
docker-compose up
```

---

## 📱 App Android

Instalar:

```
MusicMatch-oficial.apk
```

---

## 🔗 Backend

```
http://tu-ip:8080
```

---

## 📁 Estructura

```
MusicMatch/
├── backend/
├── mobile/
├── infrastructure/
│   └── docker-compose.yml
├── README.md
```

---

## ⚠️ Problemas comunes

PowerShell error:

```powershell
docker load -i backend-image.tar
```

Datos no cargados:

```bash
docker-compose down -v
docker-compose up
```

---

## 📌 Notas

TFG – MusicMatch  

Tecnologías:

- Spring Boot  
- Android  
- Docker  
- GitHub Actions  

---

## 👨‍💻 Autor

Sergio Sánchez