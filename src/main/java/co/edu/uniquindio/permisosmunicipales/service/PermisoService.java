package co.edu.uniquindio.permisosmunicipales.service;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.Documento;
import co.edu.uniquindio.permisosmunicipales.model.Permiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoPermiso;
import co.edu.uniquindio.permisosmunicipales.repository.CiudadanoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;
import co.edu.uniquindio.permisosmunicipales.service.dto.Comprobante;
import co.edu.uniquindio.permisosmunicipales.service.dto.ResultadoRadicacion;
import co.edu.uniquindio.permisosmunicipales.service.dto.ResultadoRegla;
import co.edu.uniquindio.permisosmunicipales.service.dto.SolicitudRadicacion;
import co.edu.uniquindio.permisosmunicipales.service.exception.ReglaNegocioException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PermisoService {

    private final PermisoRepository permisoRepository;
    private final CiudadanoRepository ciudadanoRepository;
    private final ReglasRadicacion reglas;
    private final GeneradorRadicado generadorRadicado;
    private final AuditoriaService auditoriaService;
    private final Clock reloj; // inyectable para poder probar RN-06 con fechas fijas

    public PermisoService(PermisoRepository permisoRepository, CiudadanoRepository ciudadanoRepository,
                          ReglasRadicacion reglas, GeneradorRadicado generadorRadicado,
                          AuditoriaService auditoriaService, Clock reloj) {
        this.permisoRepository = permisoRepository;
        this.ciudadanoRepository = ciudadanoRepository;
        this.reglas = reglas;
        this.generadorRadicado = generadorRadicado;
        this.auditoriaService = auditoriaService;
        this.reloj = reloj;
    }

    public ResultadoRadicacion radicar(SolicitudRadicacion solicitud) {
        Ciudadano ciudadano = ciudadanoRepository
                .buscarPorNumeroIdentificacion(solicitud.getNumeroIdentificacionCiudadano())
                .orElseThrow(() -> new ReglaNegocioException("RN-NOENCONTRADO",
                        "No existe un ciudadano con esa identificación"));

        List<ResultadoRegla> resultados = reglas.evaluarTodas(
                ciudadano, solicitud.getTipoPermiso(), solicitud.getDocumentos(),
                solicitud.isDocumentosLegiblesYCompletos(), solicitud.isDocumentosConFirmasYSellos(),
                permisoRepository);

        boolean todoCumple = resultados.stream().allMatch(ResultadoRegla::isCumple);
        if (!todoCumple) {
            // Nada se persiste: igual que "La solicitud no se ha registrado.
            // No se generó un número de radicado."
            return ResultadoRadicacion.fallida(resultados);
        }

        LocalDate fechaRadicacion = LocalDate.now(reloj);
        LocalDate fechaLimite = fechaRadicacion.plusDays(15);

        int anio = fechaRadicacion.getYear();
        long consecutivo = permisoRepository.siguienteConsecutivo(anio);
        String numeroRadicado = generadorRadicado.generar(anio, consecutivo);

        Permiso permiso = new Permiso(null, ciudadano, numeroRadicado, solicitud.getTipoPermiso(),
                solicitud.getOtroTipoPermiso(), solicitud.getAsunto(), solicitud.getDireccionPredio(),
                fechaRadicacion, fechaLimite, EstadoPermiso.RADICADO, solicitud.getFuncionario());

        for (Documento documento : solicitud.getDocumentos()) {
            permiso.agregarDocumento(documento);
        }

        permisoRepository.guardar(permiso);
        auditoriaService.registrar(permiso, solicitud.getFuncionario());

        String descripcionTipo = permiso.getOtroTipoPermiso() != null
                ? permiso.getOtroTipoPermiso()
                : permiso.getTipoPermiso().toString();

        String documentoSolicitante = ciudadano.getTipoIdentificacion() + " " + ciudadano.getNumeroIdentificacion();

        List<Comprobante.DocumentoRecibido> documentosRecibidos = permiso.getDocumentos().stream()
                .map(d -> new Comprobante.DocumentoRecibido(d.getNombreArchivo(), d.getTamanoBytes()))
                .collect(Collectors.toList());

        Comprobante comprobante = new Comprobante(numeroRadicado, ciudadano.getNombreCompleto(),
                documentoSolicitante, descripcionTipo, fechaRadicacion, fechaLimite,
                documentosRecibidos, solicitud.getFuncionario().getNombreCompleto());

        return ResultadoRadicacion.exitosa(resultados, comprobante);
    }

    // ¡Expone dependencia interna! Provisional Para el MVP
    public PermisoRepository getPermisoRepository() { return permisoRepository; }
}