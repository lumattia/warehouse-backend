# 🏭 Multi-Tenant SaaS Demo

[![Java](https://img.shields.io/badge/Java-26-007396?style=for-the-badge&logo=java&logoColor=white)](https://jdk.java.net/26/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Auth0](https://img.shields.io/badge/Auth0-Security-EB5424?style=for-the-badge&logo=auth0&logoColor=white)](https://auth0.com/)

## 📖 Introducción
Este proyecto es una **demo técnica** que demuestra la transición de arquitecturas empresariales desde **.NET 8** hacia el ecosistema de **Java 26**. El objetivo es exponer un sistema de gestión multi-inquilino con aislamiento de datos (Multi-tenancy) y seguridad integrada, aplicando las capacidades más recientes de Spring Boot.

> **🚧 Nota sobre el estado del proyecto:** Esta no es la versión final. El sistema se encuentra en desarrollo activo y próximamente se realizará el despliegue en la nube.
> 
> **Nota de Privacidad:** Por políticas de mantenimiento de esta demo, **todos los usuarios registrados se eliminan automáticamente cada 24 horas**.

---

## 🏗️ Arquitectura y Desafíos Técnicos

### 1. Gestión de Tenants y Aislamiento de Datos
El núcleo del sistema es su capacidad multi-inquilino, asegurando que cada cliente opere en su propio contexto lógico:
* **Filtros Automáticos (Hibernate Filters):** A diferencia de la implementación manual en cada repositorio, aquí se utilizan filtros nativos de Hibernate para inyectar el `tenant_id` de forma global y automática en las consultas.
* **Unicidad Compuesta por Tenant:** Se utiliza la configuración `@UniqueConstraint(columnNames = {"sku", "tenant_id"})`. Esto permite que el identificador comercial (SKU) sea único dentro de cada cliente, permitiendo la coexistencia de códigos idénticos entre diferentes empresas.

### 2. Evolución del Acceso a Datos
Este proyecto refleja una evolución en la gestión de persistencia basada en mi experiencia previa:
* **De lo Manual a lo Declarativo:** Mientras que en **.NET 8** la lógica de aislamiento se gestionaba comúnmente a través de un *Base Repository Pattern*, en esta demo de **Java 26** se ha implementado un enfoque declarativo. Esto centraliza el control de acceso y reduce la verbosidad del código.

### 3. Ecosistema Fullstack
Este backend está diseñado para alimentar una interfaz moderna y reactiva. Puedes encontrar el código del cliente aquí:
* 🖥️ **Front-end Repository:** [warehouse-frontend](https://github.com/lumattia/warehouse-frontend)

---

## 💻 Comparativa Técnica: .NET 8 vs Java 26

| Característica | .NET 8 (Experiencia Previa) | Java 26 (Esta Demo) |
| :--- | :--- | :--- |
| **ORM** | Entity Framework Core | Spring Data JPA / Hibernate |
| **Aislamiento de Datos** | Base Repository Pattern | Hibernate Filters (Automático) |
| **DI Container** | `IServiceCollection` | Spring IoC Container |
| **Consultas** | LINQ | Java Streams / JPQL |
| **Seguridad** | ASP.NET Identity | Spring Security + Auth0 |

---

## 🛠️ Detalles de Implementación

### Proyecciones Eficientes
Uso de interfaces de proyección para optimizar el rendimiento de la API y evitar el sobrecoste de carga de objetos:
```java
public interface IdName<T> {
    T getId();
    String getName();
}
