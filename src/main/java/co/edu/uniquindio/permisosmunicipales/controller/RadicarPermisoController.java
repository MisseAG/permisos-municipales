package co.edu.uniquindio.permisosmunicipales.controller;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.Documento;
import co.edu.uniquindio.permisosmunicipales.model.Funcionario;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoDocumentoAdjunto;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoIdentificacion;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;
import co.edu.uniquindio.permisosmunicipales.service.CiudadanoService;
import co.edu.uniquindio.permisosmunicipales.service.PermisoService;
import co.edu.uniquindio.permisosmunicipales.service.ReglasRadicacion;
import co.edu.uniquindio.permisosmunicipales.service.dto.Comprobante;
import co.edu.uniquindio.permisosmunicipales.service.dto.ResultadoRadicacion;
import co.edu.uniquindio.permisosmunicipales.service.dto.ResultadoRegla;
import co.edu.uniquindio.permisosmunicipales.service.dto.SolicitudRadicacion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.time.LocalDateTime;
import java.util.*;

public class RadicarPermisoController {

    // Sección 1
    @FXML private ComboBox<TipoIdentificacion> comboTipoDocumentoBusqueda;
    @FXML private TextField campoNumeroDocumentoBusqueda;
    @FXML private Label lblErrorBusqueda;
    @FXML private VBox panelDatosCiudadano;
    @FXML private TextField campoNombresCiudadano;
    @FXML private TextField campoApellidosCiudadano;
    @FXML private TextField campoDireccionCiudadano;
    @FXML private Label badgeEstadoCiudadano;

    // Sección 2
    @FXML private VBox panelDatosPermiso;
    @FXML private ComboBox<TipoPermiso> comboTipoPermiso;
    @FXML private VBox contenedorOtroTipoPermiso;
    @FXML private TextField campoOtroTipoPermiso;
    @FXML private TextField campoDireccionPredio;
    @FXML private TextArea campoAsunto;

    // Sección 3
    @FXML private VBox panelChecklist;
    @FXML private Label lblProgresoChecklist;
    @FXML private VBox contenedorFilasDocumentos;
    @FXML private CheckBox checkLegiblesYCompletos;
    @FXML private CheckBox checkFirmasYSellos;

    // Sección 4
    @FXML private VBox panelVerificacion;
    @FXML private Label bannerEstado;
    @FXML private VBox contenedorResultadosReglas;
    @FXML private TextArea campoObservaciones;
    @FXML private Button btnConfirmar;
    @FXML private Button btnVolverAVerificar;

    private CiudadanoService ciudadanoService;
    private PermisoService permisoService;
    private final ReglasRadicacion reglas = new ReglasRadicacion();

    private Ciudadano ciudadanoActual;
    private final Map<TipoDocumentoAdjunto, Documento> documentosObligatorios = new LinkedHashMap<>();
    private final List<Documento> documentosAdicionales = new ArrayList<>();

    public void setServicios(CiudadanoService ciudadanoService, PermisoService permisoService) {
        this.ciudadanoService = ciudadanoService;
        this.permisoService = permisoService;
    }

    @FXML
    public void initialize() {
        comboTipoDocumentoBusqueda.setItems(FXCollections.observableArrayList(TipoIdentificacion.values()));
        comboTipoPermiso.setItems(FXCollections.observableArrayList(TipoPermiso.values()));
    }

    // ---------- Sección 1: buscar ciudadano ----------

    @FXML
    private void onBuscarCiudadano() {
        ocultarErrorBusqueda();
        String numero = campoNumeroDocumentoBusqueda.getText();
        if (numero == null || numero.isBlank()) {
            mostrarErrorBusqueda("Ingrese un número de documento");
            return;
        }

        Optional<Ciudadano> encontrado = ciudadanoService.buscarPorNumeroIdentificacion(numero);
        if (encontrado.isEmpty()) {
            panelDatosCiudadano.setVisible(false);
            panelDatosCiudadano.setManaged(false);
            ocultarSeccionesPosteriores();
            mostrarErrorBusqueda("No se encontró un ciudadano con ese número de documento");
            return;
        }

        ciudadanoActual = encontrado.get();
        campoNombresCiudadano.setText(ciudadanoActual.getNombres());
        campoApellidosCiudadano.setText(ciudadanoActual.getApellidos());
        campoDireccionCiudadano.setText(ciudadanoActual.getDireccion());

        boolean activo = ciudadanoActual.getEstado() == EstadoCiudadano.ACTIVO;
        badgeEstadoCiudadano.setText(activo ? "ACTIVO · Apto" : "INACTIVO · No apto");
        badgeEstadoCiudadano.getStyleClass().removeAll("badge-activo", "badge-inactivo");
        badgeEstadoCiudadano.getStyleClass().add(activo ? "badge-activo" : "badge-inactivo");

        panelDatosCiudadano.setVisible(true);
        panelDatosCiudadano.setManaged(true);
        panelDatosPermiso.setVisible(true);
        panelDatosPermiso.setManaged(true);
        panelVerificacion.setVisible(true);
        panelVerificacion.setManaged(true);

        // RN-01 se evalúa de inmediato, igual que en el Figma: "Ciudadano no apto"
        // bloquea el flujo antes de que exista tipo de permiso o documentos.
        if (!activo) {
            panelChecklist.setVisible(false);
            panelChecklist.setManaged(false);
            mostrarSoloRN01Fallida();
        } else {
            panelChecklist.setVisible(true);
            panelChecklist.setManaged(true);
            actualizarPanelVerificacion();
        }
    }

    private void mostrarSoloRN01Fallida() {
        ResultadoRegla rn01 = reglas.evaluarRN01(ciudadanoActual);
        renderizarResultados(List.of(rn01));
        aplicarBanner(false, "El ciudadano está inactivo. No es posible radicar; revise su estado en Gestión de Ciudadanos.");
        btnConfirmar.setDisable(true);
        btnVolverAVerificar.setVisible(false);
        btnVolverAVerificar.setManaged(false);
    }

    private void mostrarErrorBusqueda(String mensaje) {
        lblErrorBusqueda.setText(mensaje);
        lblErrorBusqueda.setVisible(true);
        lblErrorBusqueda.setManaged(true);
    }

    private void ocultarErrorBusqueda() {
        lblErrorBusqueda.setVisible(false);
        lblErrorBusqueda.setManaged(false);
    }

    private void ocultarSeccionesPosteriores() {
        panelDatosPermiso.setVisible(false);
        panelDatosPermiso.setManaged(false);
        panelChecklist.setVisible(false);
        panelChecklist.setManaged(false);
        panelVerificacion.setVisible(false);
        panelVerificacion.setManaged(false);
    }

    // ---------- Sección 2: tipo de permiso ----------

    @FXML
    private void onCambioTipoPermiso() {
        TipoPermiso tipo = comboTipoPermiso.getValue();
        boolean esOtro = tipo == TipoPermiso.OTRO;
        contenedorOtroTipoPermiso.setVisible(esOtro);
        contenedorOtroTipoPermiso.setManaged(esOtro);

        documentosObligatorios.clear();
        documentosAdicionales.clear();
        reconstruirFilasDocumentos(tipo);
        actualizarPanelVerificacion();
    }

    // ---------- Sección 3: checklist dinámico ----------

    private void reconstruirFilasDocumentos(TipoPermiso tipo) {
        contenedorFilasDocumentos.getChildren().clear();
        if (tipo == null) return;

        for (TipoDocumentoAdjunto requerido : reglas.obtenerDocumentosObligatorios(tipo)) {
            contenedorFilasDocumentos.getChildren().add(crearFilaDocumento(requerido, true));
        }
        contenedorFilasDocumentos.getChildren().add(crearFilaDocumento(null, false)); // "Soportes adicionales"
        actualizarProgresoChecklist(tipo);
    }

    private HBox crearFilaDocumento(TipoDocumentoAdjunto tipoDocumento, boolean obligatorio) {
        String titulo = tipoDocumento != null ? formatearNombreDocumento(tipoDocumento) : "Soportes adicionales";

        VBox infoRequisito = new VBox(2,
                new Label(titulo),
                new Label(obligatorio ? "Obligatorio" : "Opcional") {{ setStyle("-fx-text-fill:#9ca3af; -fx-font-size:11px;"); }});

        Label lblArchivo = new Label("Sin archivo adjunto");
        lblArchivo.setStyle("-fx-text-fill:#9ca3af; -fx-font-size:11px;");

        Button btnAdjuntar = new Button("+ Adjuntar");
        btnAdjuntar.getStyleClass().add("button-secundario");
        btnAdjuntar.setOnAction(e -> onAdjuntarDocumento(tipoDocumento, lblArchivo));

        HBox fila = new HBox(10, infoRequisito, new Pane(), lblArchivo, btnAdjuntar);
        HBox.setHgrow(fila.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        fila.setStyle("-fx-alignment: CENTER_LEFT;");
        return fila;
    }

    private String formatearNombreDocumento(TipoDocumentoAdjunto tipo) {
        // FORMULARIO_SOLICITUD -> "Formulario Solicitud"
        String[] partes = tipo.name().split("_");
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            sb.append(parte.charAt(0)).append(parte.substring(1).toLowerCase()).append(" ");
        }
        return sb.toString().trim();
    }

    private void onAdjuntarDocumento(TipoDocumentoAdjunto tipoDocumento, Label lblArchivo) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Adjuntar documento");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF, JPG o PNG", "*.pdf", "*.jpg", "*.jpeg", "*.png"));

        Window ventana = contenedorFilasDocumentos.getScene().getWindow();
        java.io.File archivo = chooser.showOpenDialog(ventana);
        if (archivo == null) return;

        String formato = archivo.getName().substring(archivo.getName().lastIndexOf('.') + 1).toUpperCase();
        Documento documento = new Documento(null,
                tipoDocumento != null ? tipoDocumento : TipoDocumentoAdjunto.OTRO,
                tipoDocumento == null ? archivo.getName() : null, // otroTipoDocumento solo si es "adicional"
                archivo.getName(), archivo.getAbsolutePath(), formato,
                archivo.length(), LocalDateTime.now());

        if (tipoDocumento != null) {
            documentosObligatorios.put(tipoDocumento, documento);
        } else {
            documentosAdicionales.add(documento);
        }

        lblArchivo.setText(archivo.getName() + " · " + formatearTamano(archivo.length()) + " · Adjunto");
        lblArchivo.setStyle("-fx-text-fill:#16a34a; -fx-font-size:11px;");

        actualizarProgresoChecklist(comboTipoPermiso.getValue());
        actualizarPanelVerificacion();
    }

    private String formatearTamano(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        return kb < 1024 ? String.format("%.0f KB", kb) : String.format("%.1f MB", kb / 1024.0);
    }

    private void actualizarProgresoChecklist(TipoPermiso tipo) {
        if (tipo == null) { lblProgresoChecklist.setText(""); return; }
        int total = reglas.obtenerDocumentosObligatorios(tipo).size();
        int adjuntos = documentosObligatorios.size();
        lblProgresoChecklist.setText(adjuntos + " de " + total + " obligatorios");
    }

    @FXML
    private void onCambioChecklist() {
        actualizarPanelVerificacion();
    }

    // ---------- Sección 4: verificación ----------

    private List<Documento> todosLosDocumentos() {
        List<Documento> todos = new ArrayList<>(documentosObligatorios.values());
        todos.addAll(documentosAdicionales);
        return todos;
    }

    private void actualizarPanelVerificacion() {
        if (ciudadanoActual == null || comboTipoPermiso.getValue() == null) return;

        List<ResultadoRegla> resultados = reglas.evaluarTodas(
                ciudadanoActual, comboTipoPermiso.getValue(), todosLosDocumentos(),
                checkLegiblesYCompletos.isSelected(), checkFirmasYSellos.isSelected(),
                permisoService.getPermisoRepository());

        renderizarResultados(resultados);
        boolean todoCumple = resultados.stream().allMatch(ResultadoRegla::isCumple);
        aplicarBanner(todoCumple, todoCumple ? null
                : "La validación " + primeraQueFalla(resultados) + " no fue aprobada. Revise la solicitud y vuelva a verificar.");
        btnConfirmar.setDisable(!todoCumple);
        btnVolverAVerificar.setVisible(!todoCumple);
        btnVolverAVerificar.setManaged(!todoCumple);
    }

    private String primeraQueFalla(List<ResultadoRegla> resultados) {
        return resultados.stream().filter(r -> !r.isCumple()).findFirst()
                .map(ResultadoRegla::getCodigo).orElse("");
    }

    private void renderizarResultados(List<ResultadoRegla> resultados) {
        contenedorResultadosReglas.getChildren().clear();
        for (ResultadoRegla r : resultados) {
            Label chip = new Label(r.isCumple() ? "CUMPLE" : "NO CUMPLE");
            chip.getStyleClass().add(r.isCumple() ? "badge-activo" : "badge-inactivo");

            VBox texto = new VBox(2, new Label(r.getCodigo() + " — " + r.getTitulo()),
                    new Label(r.isCumple() ? r.getDetalle() : r.getDetalle()) {{ setStyle("-fx-text-fill:#6b7280; -fx-font-size:11px;"); }});

            HBox fila = new HBox(10, texto, new Pane(), chip);
            HBox.setHgrow(fila.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
            fila.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 8; -fx-background-radius: 6; -fx-background-color: "
                    + (r.isCumple() ? "transparent" : "#fef2f2") + ";");
            contenedorResultadosReglas.getChildren().add(fila);
        }
    }

    private void aplicarBanner(boolean exito, String mensajeError) {
        bannerEstado.setText(exito ? "Todo listo para radicar" : mensajeError);
        bannerEstado.setStyle("-fx-padding: 10; -fx-background-radius: 6; -fx-background-color: "
                + (exito ? "#dcfce7" : "#fef2f2") + "; -fx-text-fill: " + (exito ? "#16a34a" : "#dc2626") + ";");
    }

    @FXML
    private void onVolverAVerificar() {
        actualizarPanelVerificacion();
    }

    @FXML
    private void onConfirmarRadicacion() {
        Funcionario funcionarioSesion = new Funcionario(1L, "sesion", "Administración", "municipal"); // ver nota de sesión más abajo

        SolicitudRadicacion solicitud = new SolicitudRadicacion(
                ciudadanoActual.getNumeroIdentificacion(),
                comboTipoPermiso.getValue(),
                comboTipoPermiso.getValue() == TipoPermiso.OTRO ? campoOtroTipoPermiso.getText() : null,
                campoAsunto.getText(),
                campoDireccionPredio.getText(),
                todosLosDocumentos(),
                checkLegiblesYCompletos.isSelected(),
                checkFirmasYSellos.isSelected(),
                funcionarioSesion);

        ResultadoRadicacion resultado = permisoService.radicar(solicitud);
        renderizarResultados(resultado.getResultados());

        if (resultado.isExitosa()) {
            mostrarModalExito(resultado.getComprobante());
            limpiarFormularioCompleto();
        } else {
            aplicarBanner(false, "No se pudo radicar. La validación " + primeraQueFalla(resultado.getResultados())
                    + " no fue aprobada. Revise la solicitud y vuelva a verificar.");
            btnConfirmar.setDisable(true);
            btnVolverAVerificar.setVisible(true);
            btnVolverAVerificar.setManaged(true);
        }
    }

    private void mostrarModalExito(Comprobante c) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Ventanilla Única");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm());

        VBox contenido = new VBox(8,
                new Label("Radicación exitosa") {{ setStyle("-fx-font-size:16px; -fx-font-weight:bold;"); }},
                new Label("La solicitud se registró correctamente."),
                new Label("RADICADO N° " + c.getNumeroRadicado()) {{ setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#2563eb;"); }},
                new Label("Fecha de Radicación: " + c.getFechaRadicacion()),
                new Label("Fecha Límite: " + c.getFechaLimiteRespuesta()),
                new Label("Funcionario: " + c.getNombreFuncionario()),
                new Label("Solicitante: " + c.getNombreCiudadano() + " — " + c.getDocumentoSolicitante()),
                new Label("Tipo de permiso: " + c.getTipoPermisoDescripcion()),
                new Label("Documentos recibidos (" + c.getCantidadDocumentos() + "):"));

        for (Comprobante.DocumentoRecibido d : c.getDocumentosRecibidos()) {
            contenido.getChildren().add(new Label("  • " + d.getNombreArchivo() + " — " + d.getTamanoFormateado()));
        }

        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    private void onCancelar() {
        limpiarFormularioCompleto();
    }

    private void limpiarFormularioCompleto() {
        ciudadanoActual = null;
        documentosObligatorios.clear();
        documentosAdicionales.clear();
        campoNumeroDocumentoBusqueda.clear();
        comboTipoDocumentoBusqueda.setValue(null);
        comboTipoPermiso.setValue(null);
        campoOtroTipoPermiso.clear();
        campoDireccionPredio.clear();
        campoAsunto.clear();
        campoObservaciones.clear();
        checkLegiblesYCompletos.setSelected(false);
        checkFirmasYSellos.setSelected(false);
        contenedorFilasDocumentos.getChildren().clear();
        contenedorResultadosReglas.getChildren().clear();
        ocultarErrorBusqueda();
        ocultarSeccionesPosteriores();
        panelDatosCiudadano.setVisible(false);
        panelDatosCiudadano.setManaged(false);
    }
}