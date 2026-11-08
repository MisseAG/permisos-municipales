package co.edu.uniquindio.permisosmunicipales.service;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.Documento;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoPermiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoDocumentoAdjunto;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;
import co.edu.uniquindio.permisosmunicipales.service.exception.ReglaNegocioException;

import co.edu.uniquindio.permisosmunicipales.service.dto.ResultadoRegla;

import java.util.*;


public class ReglasRadicacion {

    private static final Map<TipoPermiso, Set<TipoDocumentoAdjunto>> DOCUMENTOS_OBLIGATORIOS =
            new EnumMap<>(TipoPermiso.class);
    static {
        DOCUMENTOS_OBLIGATORIOS.put(TipoPermiso.CONSTRUCCION_OBRA_NUEVA, EnumSet.of(
                TipoDocumentoAdjunto.FORMULARIO_SOLICITUD,
                TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD,
                TipoDocumentoAdjunto.CERTIFICADO_LIBERTAD_TRADICION,
                TipoDocumentoAdjunto.PLANOS_PROYECTO,
                TipoDocumentoAdjunto.MEMORIA_CALCULOS));
        DOCUMENTOS_OBLIGATORIOS.put(TipoPermiso.AMPLIACION, EnumSet.of(
                TipoDocumentoAdjunto.FORMULARIO_SOLICITUD,
                TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD,
                TipoDocumentoAdjunto.PLANOS_PROYECTO));
        DOCUMENTOS_OBLIGATORIOS.put(TipoPermiso.MODIFICACION, EnumSet.of(
                TipoDocumentoAdjunto.FORMULARIO_SOLICITUD,
                TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD,
                TipoDocumentoAdjunto.PLANOS_PROYECTO));
        DOCUMENTOS_OBLIGATORIOS.put(TipoPermiso.DEMOLICION, EnumSet.of(
                TipoDocumentoAdjunto.FORMULARIO_SOLICITUD,
                TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD,
                TipoDocumentoAdjunto.CERTIFICADO_LIBERTAD_TRADICION));
        DOCUMENTOS_OBLIGATORIOS.put(TipoPermiso.URBANIZACION, EnumSet.of(
                TipoDocumentoAdjunto.FORMULARIO_SOLICITUD,
                TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD,
                TipoDocumentoAdjunto.CERTIFICADO_LIBERTAD_TRADICION,
                TipoDocumentoAdjunto.PLANOS_PROYECTO));
        DOCUMENTOS_OBLIGATORIOS.put(TipoPermiso.PARCELACION, EnumSet.of(
                TipoDocumentoAdjunto.FORMULARIO_SOLICITUD,
                TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD,
                TipoDocumentoAdjunto.CERTIFICADO_LIBERTAD_TRADICION,
                TipoDocumentoAdjunto.PLANOS_PROYECTO));
        DOCUMENTOS_OBLIGATORIOS.put(TipoPermiso.OTRO, EnumSet.of(
                TipoDocumentoAdjunto.FORMULARIO_SOLICITUD,
                TipoDocumentoAdjunto.COPIA_DOCUMENTO_IDENTIDAD));
    }

    public void validarCiudadanoActivo(Ciudadano ciudadano) {
        if (ciudadano.getEstado() != EstadoCiudadano.ACTIVO) {
            throw new ReglaNegocioException("RN-01",
                    "El ciudadano " + ciudadano.getNumeroIdentificacion() + " no está activo");
        }
    }

    public void validarSinDuplicado(Ciudadano ciudadano, TipoPermiso tipoPermiso,
                                    PermisoRepository permisoRepository) {
        if (permisoRepository.existeRadicadoActivo(ciudadano.getId(), tipoPermiso)) {
            throw new ReglaNegocioException("RN-02",
                    "Ya existe un permiso RADICADO de tipo " + tipoPermiso + " para este ciudadano");
        }
    }

    public void validarDocumentosObligatorios(TipoPermiso tipoPermiso, List<Documento> documentos) {
        Set<TipoDocumentoAdjunto> obligatorios = DOCUMENTOS_OBLIGATORIOS.get(tipoPermiso);
        Set<TipoDocumentoAdjunto> aportados = EnumSet.noneOf(TipoDocumentoAdjunto.class);
        documentos.forEach(d -> aportados.add(d.getTipoDocumentoAdjunto()));

        for (TipoDocumentoAdjunto requerido : obligatorios) {
            if (!aportados.contains(requerido)) {
                throw new ReglaNegocioException("RN-03",
                        "Falta el documento obligatorio: " + requerido);
            }
        }
    }

    public void validarVerificacionManual(boolean legiblesYCompletos, boolean conFirmasYSellos) {
        if (!legiblesYCompletos) {
            throw new ReglaNegocioException("RN-04",
                    "Debe confirmar que todos los documentos son legibles y están completos");
        }
        if (!conFirmasYSellos) {
            throw new ReglaNegocioException("RN-04",
                    "Debe confirmar que todos los documentos tienen las firmas/sellos requeridos");
        }
    }


    public List<ResultadoRegla> evaluarTodas(Ciudadano ciudadano, TipoPermiso tipoPermiso,
                                             List<Documento> documentos,
                                             boolean documentosLegiblesYCompletos,
                                             boolean documentosConFirmasYSellos,
                                             PermisoRepository permisoRepository) {
        List<ResultadoRegla> resultados = new ArrayList<>();
        resultados.add(evaluarRN01(ciudadano));
        resultados.add(evaluarRN02(ciudadano, tipoPermiso, permisoRepository));
        resultados.add(evaluarRN03(tipoPermiso, documentos));
        resultados.add(evaluarRN04(documentosLegiblesYCompletos, documentosConFirmasYSellos));
        return resultados;
    }

    public ResultadoRegla evaluarRN01(Ciudadano ciudadano) {
        try {
            validarCiudadanoActivo(ciudadano);
            return new ResultadoRegla("RN-01", true, "Ciudadano activo", "Estado consultado");
        } catch (ReglaNegocioException e) {
            return new ResultadoRegla("RN-01", false, "Ciudadano activo", e.getMessage());
        }
    }

    public ResultadoRegla evaluarRN02(Ciudadano ciudadano, TipoPermiso tipoPermiso,
                                       PermisoRepository permisoRepository) {
        try {
            validarSinDuplicado(ciudadano, tipoPermiso, permisoRepository);
            return new ResultadoRegla("RN-02", true, "Validación automática", "Comprobación del sistema");
        } catch (ReglaNegocioException e) {
            return new ResultadoRegla("RN-02", false, "Validación automática", e.getMessage());
        }
    }

    public ResultadoRegla evaluarRN03(TipoPermiso tipoPermiso, List<Documento> documentos) {
        try {
            validarDocumentosObligatorios(tipoPermiso, documentos);
            return new ResultadoRegla("RN-03", true, "Validación automática", "Comprobación del sistema");
        } catch (ReglaNegocioException e) {
            return new ResultadoRegla("RN-03", false, "Validación automática", e.getMessage());
        }
    }

    public ResultadoRegla evaluarRN04(boolean legiblesYCompletos, boolean conFirmasYSellos) {
        try {
            validarVerificacionManual(legiblesYCompletos, conFirmasYSellos);
            return new ResultadoRegla("RN-04", true, "Revisión del funcionario", "Legibilidad, firmas y sellos");
        } catch (ReglaNegocioException e) {
            return new ResultadoRegla("RN-04", false, "Revisión del funcionario", e.getMessage());
        }
    }

    public Set<TipoDocumentoAdjunto> obtenerDocumentosObligatorios(TipoPermiso tipoPermiso) {
        return DOCUMENTOS_OBLIGATORIOS.getOrDefault(tipoPermiso, Collections.emptySet());
    }
}