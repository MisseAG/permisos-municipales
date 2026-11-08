package co.edu.uniquindio.permisosmunicipales.util;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.Funcionario;
import co.edu.uniquindio.permisosmunicipales.model.Permiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoPermiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoIdentificacion;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;
import co.edu.uniquindio.permisosmunicipales.repository.CiudadanoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga los CSV de /resources/data al arrancar la app, para tener datos
 * de prueba sin depender de una base de datos real.
 */
public final class SeedLoader {

    private SeedLoader() { } // utilidad estática, no se instancia

    public static void cargarDatosIniciales(CiudadanoRepository ciudadanoRepo,
                                            PermisoRepository permisoRepo) {
        Map<String, Ciudadano> ciudadanosPorIdentificacion = cargarCiudadanos(ciudadanoRepo);
        Map<String, Funcionario> funcionariosPorUsuario = cargarFuncionarios();
        cargarPermisos(permisoRepo, ciudadanosPorIdentificacion, funcionariosPorUsuario);
    }

    private static Map<String, Ciudadano> cargarCiudadanos(CiudadanoRepository repo) {
        Map<String, Ciudadano> resultado = new HashMap<>();
        for (String[] fila : leerCsv("/data/ciudadanos.csv")) {
            Long id = Long.parseLong(fila[0]);
            TipoIdentificacion tipo = TipoIdentificacion.valueOf(fila[1]);
            String otroTipo = fila[2].isBlank() ? null : fila[2];
            String numeroIdentificacion = fila[3];
            String nombres = fila[4];
            String apellidos = fila[5];
            String email = fila[6];
            String telefono = fila[7];
            String direccion = fila[8];
            EstadoCiudadano estado = EstadoCiudadano.valueOf(fila[9]);

            Ciudadano ciudadano = new Ciudadano(id, tipo, otroTipo, numeroIdentificacion,
                    nombres, apellidos, email, telefono, direccion, estado);
            repo.guardar(ciudadano);
            resultado.put(numeroIdentificacion, ciudadano);
        }
        return resultado;
    }

    private static Map<String, Funcionario> cargarFuncionarios() {
        // No hay FuncionarioRepository: solo se necesitan para enlazarlos a Permiso.usuarioOperador
        Map<String, Funcionario> resultado = new HashMap<>();
        for (String[] fila : leerCsv("/data/funcionarios.csv")) {
            Long id = Long.parseLong(fila[0]);
            String usuario = fila[1];
            Funcionario funcionario = new Funcionario(id, usuario, fila[2], fila[3]);
            resultado.put(usuario, funcionario);
        }
        return resultado;
    }

    private static void cargarPermisos(PermisoRepository repo,
                                       Map<String, Ciudadano> ciudadanosPorIdentificacion,
                                       Map<String, Funcionario> funcionariosPorUsuario) {
        for (String[] fila : leerCsv("/data/permisos.csv")) {
            Long id = Long.parseLong(fila[0]);
            Ciudadano ciudadano = ciudadanosPorIdentificacion.get(fila[1]);
            if (ciudadano == null) {
                throw new IllegalStateException(
                        "permisos.csv referencia un ciudadano inexistente: " + fila[1]);
            }
            String numeroRadicado = fila[2];
            TipoPermiso tipoPermiso = TipoPermiso.valueOf(fila[3]);
            String otroTipoPermiso = fila[4].isBlank() ? null : fila[4];
            String asunto = fila[5];
            String direccionPredio = fila[6].isBlank() ? null : fila[6];
            LocalDate fechaRadicacion = LocalDate.parse(fila[7]);
            LocalDate fechaLimiteRespuesta = LocalDate.parse(fila[8]);
            EstadoPermiso estado = EstadoPermiso.valueOf(fila[9]);
            Funcionario funcionario = funcionariosPorUsuario.get(fila[10]);
            if (funcionario == null) {
                throw new IllegalStateException(
                        "permisos.csv referencia un funcionario inexistente: " + fila[10]);
            }

            Permiso permiso = new Permiso(id, ciudadano, numeroRadicado, tipoPermiso,
                    otroTipoPermiso, asunto, direccionPredio, fechaRadicacion,
                    fechaLimiteRespuesta, estado, funcionario);
            permiso.setEstado(estado); // redundante con el constructor, pero explícito a propósito
            repo.guardar(permiso);
        }
    }

    private static String[][] leerCsv(String rutaEnClasspath) {
        try (InputStream in = SeedLoader.class.getResourceAsStream(rutaEnClasspath)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró el recurso: " + rutaEnClasspath);
            }
            try (BufferedReader lector = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return lector.lines()
                        .skip(1) // salta el encabezado
                        .filter(linea -> !linea.isBlank())
                        .map(linea -> linea.split(";", -1)) // -1 conserva campos vacíos al final
                        .toArray(String[][]::new);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error leyendo " + rutaEnClasspath, e);
        }
    }
}