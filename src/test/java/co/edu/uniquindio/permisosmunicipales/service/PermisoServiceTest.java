package co.edu.uniquindio.permisosmunicipales.service;

import co.edu.uniquindio.permisosmunicipales.model.*;
import co.edu.uniquindio.permisosmunicipales.model.enums.*;
import co.edu.uniquindio.permisosmunicipales.repository.*;
import co.edu.uniquindio.permisosmunicipales.repository.memory.*;
import co.edu.uniquindio.permisosmunicipales.service.dto.ResultadoRadicacion;
import co.edu.uniquindio.permisosmunicipales.service.dto.SolicitudRadicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PermisoServiceTest {

    private CiudadanoRepository ciudadanoRepo;
    private PermisoRepository permisoRepo;
    private PermisoService permisoService;
    private Funcionario funcionario;
    private final Clock relojFijo = Clock.fixed(
            Instant.parse("2026-11-10T09:00:00Z"), ZoneId.of("America/Bogota"));

    @BeforeEach
    void preparar() {
        ciudadanoRepo = new InMemoryCiudadanoRepository();
        permisoRepo = new InMemoryPermisoRepository();
        permisoService = new PermisoService(permisoRepo, ciudadanoRepo,
                new ReglasRadicacion(), new GeneradorRadicado(),
                new AuditoriaService(relojFijo), relojFijo);
        funcionario = new Funcionario(1L, "jperez", "Juliana", "Pérez");
    }

    private Ciudadano crearCiudadano(String numeroId, EstadoCiudadano estado) {
        Ciudadano c = new Ciudadano(null, TipoIdentificacion.CC, null, numeroId,
                "Nombre", "Apellido", "correo@mail.com", "3000000000", "Calle 1", estado);
        return ciudadanoRepo.guardar(c);
    }

    private Documento documento(TipoDocumentoAdjunto tipo) {
        return new Documento(null, tipo, null, "archivo.pdf", "/ruta/archivo.pdf",
                "PDF", 1024L, java.time.LocalDateTime.now());
    }

    // Caso 1 — RN-01
    // G: ciudadano INACTIVO
    // W: intenta radicar un permiso
    // T: RN-01 NO CUMPLE y no se crea el permiso
    @Test
    void ciudadanoInactivo_noPermiteRadicar() {
        Ciudadano c = crearCiudadano("1000000001", EstadoCiudadano.INACTIVO);

        SolicitudRadicacion solicitud = new SolicitudRadicacion(c.getNumeroIdentificacion(),
                TipoPermiso.AMPLIACION, null, "Asunto", "Dirección",
                List.of(documento(TipoDocumentoAdjunto.FORMULARIO_SOLICITUD),
                        documento(TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD),
                        documento(TipoDocumentoAdjunto.PLANOS_PROYECTO)),
                true, true, funcionario);

        ResultadoRadicacion resultado = permisoService.radicar(solicitud);

        assertFalse(resultado.isExitosa());
        assertFalse(resultado.getResultados().stream()
                .filter(r -> r.getCodigo().equals("RN-01")).findFirst().get().isCumple());
        assertTrue(permisoRepo.buscarPorCiudadano(c.getId()).isEmpty());
    }

    // Caso 2 — RN-02
    // G: ciudadano ACTIVO con un permiso RADICADO del mismo tipo
    // W: intenta radicar otro del mismo tipo
    // T: RN-02 NO CUMPLE y no se crea el segundo permiso
    @Test
    void permisoDuplicadoDelMismoTipo_noPermiteRadicar() {
        Ciudadano c = crearCiudadano("1000000002", EstadoCiudadano.ACTIVO);
        List<Documento> docs = List.of(
                documento(TipoDocumentoAdjunto.FORMULARIO_SOLICITUD),
                documento(TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD),
                documento(TipoDocumentoAdjunto.PLANOS_PROYECTO));

        // primera radicación: debe quedar RADICADA sin problema
        ResultadoRadicacion primera = permisoService.radicar(new SolicitudRadicacion(
                c.getNumeroIdentificacion(), TipoPermiso.AMPLIACION, null, "Primera", "Dirección",
                docs, true, true, funcionario));
        assertTrue(primera.isExitosa());

        // segunda, mismo ciudadano y mismo tipo: debe bloquear
        ResultadoRadicacion segunda = permisoService.radicar(new SolicitudRadicacion(
                c.getNumeroIdentificacion(), TipoPermiso.AMPLIACION, null, "Segunda", "Dirección",
                docs, true, true, funcionario));

        assertFalse(segunda.isExitosa());
        assertFalse(segunda.getResultados().stream()
                .filter(r -> r.getCodigo().equals("RN-02")).findFirst().get().isCumple());
    }

    // Caso 3 — RN-03
    // G: ciudadano ACTIVO, sin duplicado, pero faltan documentos obligatorios
    // W: intenta radicar con la lista incompleta
    // T: RN-03 NO CUMPLE y no se crea el permiso
    @Test
    void documentosObligatoriosIncompletos_noPermiteRadicar() {
        Ciudadano c = crearCiudadano("1000000003", EstadoCiudadano.ACTIVO);

        SolicitudRadicacion solicitud = new SolicitudRadicacion(c.getNumeroIdentificacion(),
                TipoPermiso.AMPLIACION, null, "Asunto", "Dirección",
                List.of(documento(TipoDocumentoAdjunto.FORMULARIO_SOLICITUD)), // faltan 2 de los 3 obligatorios
                true, true, funcionario);

        ResultadoRadicacion resultado = permisoService.radicar(solicitud);

        assertFalse(resultado.isExitosa());
        assertFalse(resultado.getResultados().stream()
                .filter(r -> r.getCodigo().equals("RN-03")).findFirst().get().isCumple());
    }

    // Caso 4 — RN-04
    // G: documentos completos, pero el funcionario NO confirma legibilidad/firmas
    // W: intenta confirmar la radicación
    // T: RN-04 NO CUMPLE y no se crea el permiso
    @Test
    void sinVerificacionManualDelFuncionario_noPermiteRadicar() {
        Ciudadano c = crearCiudadano("1000000004", EstadoCiudadano.ACTIVO);
        List<Documento> docs = List.of(
                documento(TipoDocumentoAdjunto.FORMULARIO_SOLICITUD),
                documento(TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD),
                documento(TipoDocumentoAdjunto.PLANOS_PROYECTO));

        SolicitudRadicacion solicitud = new SolicitudRadicacion(c.getNumeroIdentificacion(),
                TipoPermiso.AMPLIACION, null, "Asunto", "Dirección", docs,
                false, true, funcionario); // legiblesYCompletos = false

        ResultadoRadicacion resultado = permisoService.radicar(solicitud);

        assertFalse(resultado.isExitosa());
        assertFalse(resultado.getResultados().stream()
                .filter(r -> r.getCodigo().equals("RN-04")).findFirst().get().isCumple());
    }

    // Caso 5 — Camino feliz (RN-05, RN-06, RN-07 implícitas)
    // G: ciudadano ACTIVO, sin duplicado, documentos completos y verificados
    // W: confirma la radicación
    // T: se genera un radicado único, estado RADICADO, y fecha límite = radicación + 15 días
    @Test
    void solicitudValida_generaRadicadoYFechaLimiteCorrecta() {
        Ciudadano c = crearCiudadano("1000000005", EstadoCiudadano.ACTIVO);
        List<Documento> docs = List.of(
                documento(TipoDocumentoAdjunto.FORMULARIO_SOLICITUD),
                documento(TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD),
                documento(TipoDocumentoAdjunto.PLANOS_PROYECTO));

        ResultadoRadicacion resultado = permisoService.radicar(new SolicitudRadicacion(
                c.getNumeroIdentificacion(), TipoPermiso.AMPLIACION, null, "Asunto", "Dirección",
                docs, true, true, funcionario));

        assertTrue(resultado.isExitosa());
        assertNotNull(resultado.getComprobante());
        assertTrue(resultado.getComprobante().getNumeroRadicado().startsWith("RAD-2026-"));

        Permiso permiso = permisoRepo.buscarPorCiudadano(c.getId()).get(0);
        assertEquals(EstadoPermiso.RADICADO, permiso.getEstado());
        assertEquals(permiso.getFechaRadicacion().plusDays(15), permiso.getFechaLimiteRespuesta());
    }

    // Caso 6 — RN-05 (unicidad del consecutivo)
    // G: dos ciudadanos distintos radican en el mismo año
    // W: ambas solicitudes son válidas y se confirman
    // T: cada una recibe un número de radicado distinto, sin colisión
    @Test
    void dosRadicacionesEnElMismoAnio_generanConsecutivosDistintos() {
        Ciudadano c1 = crearCiudadano("1000000006", EstadoCiudadano.ACTIVO);
        Ciudadano c2 = crearCiudadano("1000000007", EstadoCiudadano.ACTIVO);
        List<Documento> docs = List.of(
                documento(TipoDocumentoAdjunto.FORMULARIO_SOLICITUD),
                documento(TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD),
                documento(TipoDocumentoAdjunto.PLANOS_PROYECTO));

        ResultadoRadicacion r1 = permisoService.radicar(new SolicitudRadicacion(
                c1.getNumeroIdentificacion(), TipoPermiso.AMPLIACION, null, "A1", "Dir1",
                docs, true, true, funcionario));
        ResultadoRadicacion r2 = permisoService.radicar(new SolicitudRadicacion(
                c2.getNumeroIdentificacion(), TipoPermiso.AMPLIACION, null, "A2", "Dir2",
                docs, true, true, funcionario));

        assertTrue(r1.isExitosa());
        assertTrue(r2.isExitosa());
        assertNotEquals(r1.getComprobante().getNumeroRadicado(), r2.getComprobante().getNumeroRadicado());
    }
}