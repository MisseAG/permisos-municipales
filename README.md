# Permisos Municipales

Aplicación de escritorio desarrollada con JavaFX para gestionar ciudadanos y
permisos municipales.

## Requisitos

- JDK 11 o superior.
- IntelliJ IDEA (recomendado) o un IDE compatible con proyectos Maven.
- Maven Wrapper incluido en el repositorio (`mvnw.cmd` para Windows).

## Cómo ejecutar la aplicación

1. Abra el proyecto como un proyecto Maven.
2. Espere a que Maven descargue las dependencias.
3. Ejecute la clase `app/Launcher.java`, ubicada en:

   `src/main/java/co/edu/uniquindio/permisosmunicipales/app/Launcher.java`

   En IntelliJ IDEA, abra el archivo y seleccione **Run 'Launcher.main()'**.
4. La aplicación se abrirá con el título **Permisos Municipales** y cargará
   inicialmente la vista de ciudadanos.

Para comprobar el proyecto y ejecutar las pruebas desde Windows:

```powershell
.\mvnw.cmd test
```

## Usuarios demo

La aplicación no implementa autenticación ni solicita contraseña. Los
funcionarios demo se cargan desde `src/main/resources/data/funcionarios.csv` y
se utilizan para asociar los permisos radicados:

| Usuario | Nombre |
|---|---|
| `jperez` | Juliana Pérez |
| `mrodriguez` | Mauricio Rodríguez |

## Datos iniciales

Al iniciar, `SeedLoader` carga automáticamente datos de prueba desde los
recursos incluidos en el proyecto:

- `src/main/resources/data/ciudadanos.csv`: datos de ciudadanos quemados
  (incluidos directamente en el repositorio para la demostración).
- `src/main/resources/data/funcionarios.csv`: funcionarios demo.
- `src/main/resources/data/permisos.csv`: permisos de ejemplo.

Estos datos se cargan en repositorios en memoria; por lo tanto, los cambios
realizados durante la ejecución no se persisten al cerrar la aplicación.
