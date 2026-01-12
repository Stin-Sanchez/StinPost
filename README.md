# 🛒 Sistema de Gestión de Inventario y Ventas (POS)

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white)

> Una solución sencilla para la administración de productos, control de stock inteligente y gestión de ventas, construida  en Java.

## 📖 Descripción

Este proyecto es una aplicación web completa diseñada para pequeñas  empresas que requieren gestionar su inventario y flujo de ventas de manera eficiente. 

A diferencia de un CRUD básico, este sistema implementa lógica de negocio real, como **cálculo automático de estados de stock**, alertas de inventario bajo y manejo de imágenes de productos, todo orquestado bajo una arquitectura modular y escalable.

## 🚀 Características Principales

* **Gestión de Productos Avanzada:**
    * CRUD completo con carga de imágenes.
    * **Lógica de Stock Inteligente:** El sistema cambia automáticamente el estado del producto (Disponible, Stock Bajo, Casi Agotado, Agotado) en tiempo real según las existencias y los umbrales definidos.
* **Control de Inventario:**
    * Alertas visuales para reposición de mercancía.
    * Validaciones de negocio (fechas de expiración, stocks mínimos).
* **Gestión de Usuarios:**
    * Administración de roles y accesos (Admin/Vendedor).
* **Interfaz Responsiva:**
    * Diseño adaptable a móviles y tablets usando Bootstrap 5.

## 🛠️ Stack Tecnológico

El proyecto está construido utilizando un stack moderno y empresarial:

* **Backend:** Java 17, Spring Boot 3 (Web, Data JPA, Validation).
* **Frontend:** Thymeleaf (Motor de plantillas), HTML5, CSS3, JavaScript (Vanilla), Bootstrap 5.
* **Base de Datos:** MySQL.
* **Herramientas:** Maven (Gestión de dependencias), Git/GitHub (Control de versiones).
* **Arquitectura:** MVC.

## 📸 Capturas de Pantalla


<img width="1376" height="702" alt="image" src="https://github.com/user-attachments/assets/013e8478-db13-4ed8-ab93-3314297c29f2" />
<img width="1376" height="695" alt="image" src="https://github.com/user-attachments/assets/1b26b7dd-2bd4-4522-b562-ef3a0ff471b3" />

## 🔧 Instalación y Despliegue

Sigue estos pasos para correr el proyecto en tu entorno local:

1.  **Clonar el repositorio**
2.  **Configurar la Base de Datos:**
    * Crea una base de datos en MySQL llamada `stin_post_db` (o el nombre que tengas en properties).
    * Actualiza el archivo `src/main/resources/application.properties` con tus credenciales:
        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/tu_base_de_datos
        spring.datasource.username=tu_usuario
        spring.datasource.password=tu_password
        ```
3.  **Ejecutar la aplicación:**
    ```bash
    ./mvnw spring-boot:run
    ```
4.  **Acceso:**
    * Abre tu navegador en: `http://localhost:8080`

## 🤝 Autor

**Sting Sánchez**
* *Backend Developer en formación | Entusiasta de Java*
* [LinkedIn](https://www.linkedin.com/in/stin-s%C3%A1nchez/)


---
*Este proyecto es parte de mi portafolio profesional para demostrar habilidades en desarrollo Full Stack con Java.*
