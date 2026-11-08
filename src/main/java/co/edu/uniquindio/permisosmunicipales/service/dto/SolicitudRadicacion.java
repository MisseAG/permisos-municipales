package co.edu.uniquindio.permisosmunicipales.service.dto;

import co.edu.uniquindio.permisosmunicipales.model.Documento;
import co.edu.uniquindio.permisosmunicipales.model.Funcionario;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;

import java.util.List;

public class SolicitudRadicacion {

    private final String numeroIdentificacionCiudadano;
    private final TipoPermiso tipoPermiso;
    private final String otroTipoPermiso;
    private final String asunto;
    private final String direccionPredio;
    private final List<Documento> documentos;
    private final boolean documentosLegiblesYCompletos;
    private final boolean documentosConFirmasYSellos;
    private final Funcionario funcionario;

    public SolicitudRadicacion(String numeroIdentificacionCiudadano, TipoPermiso tipoPermiso,
                               String otroTipoPermiso, String asunto, String direccionPredio,
                               List<Documento> documentos, boolean documentosLegiblesYCompletos,
                               boolean documentosConFirmasYSellos, Funcionario funcionario) {
        this.numeroIdentificacionCiudadano = numeroIdentificacionCiudadano;
        this.tipoPermiso = tipoPermiso;
        this.otroTipoPermiso = otroTipoPermiso;
        this.asunto = asunto;
        this.direccionPredio = direccionPredio;
        this.documentos = documentos;
        this.documentosLegiblesYCompletos = documentosLegiblesYCompletos;
        this.documentosConFirmasYSellos = documentosConFirmasYSellos;
        this.funcionario = funcionario;
    }

    public String getNumeroIdentificacionCiudadano() { return numeroIdentificacionCiudadano; }
    public TipoPermiso getTipoPermiso() { return tipoPermiso; }
    public String getOtroTipoPermiso() { return otroTipoPermiso; }
    public String getAsunto() { return asunto; }
    public String getDireccionPredio() { return direccionPredio; }
    public List<Documento> getDocumentos() { return documentos; }
    public boolean isDocumentosLegiblesYCompletos() { return documentosLegiblesYCompletos; }
    public boolean isDocumentosConFirmasYSellos() { return documentosConFirmasYSellos; }
    public Funcionario getFuncionario() { return funcionario; }
}