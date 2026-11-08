package co.edu.uniquindio.permisosmunicipales.controller;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoIdentificacion;
import co.edu.uniquindio.permisosmunicipales.service.CiudadanoService;
import co.edu.uniquindio.permisosmunicipales.model.enums.ResultadoEliminacion;
import co.edu.uniquindio.permisosmunicipales.service.exception.ReglaNegocioException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

public class CiudadanoCrudController {

    // --- form ---
    @FXML private Label lblTituloFormulario;
    @FXML private Label lblSubtituloFormulario;
    @FXML private ComboBox<TipoIdentificacion> comboTipoIdentificacion;
    @FXML private javafx.scene.layout.VBox contenedorOtroTipo;
    @FXML private TextField campoOtroTipoIdentificacion;
    @FXML private TextField campoNumeroIdentificacion;
    @FXML private TextField campoNombres;
    @FXML private TextField campoApellidos;
    @FXML private TextField campoEmail;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoDireccion;
    @FXML private ComboBox<EstadoCiudadano> comboEstado;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    // --- búsqueda y tabla ---
    @FXML private TextField campoBusqueda;
    @FXML private TableView<Ciudadano> tablaCiudadanos;
    @FXML private TableColumn<Ciudadano, String> colId;
    @FXML private TableColumn<Ciudadano, String> colIdentificacion;
    @FXML private TableColumn<Ciudadano, String> colNombreCompleto;
    @FXML private TableColumn<Ciudadano, String> colEmail;
    @FXML private TableColumn<Ciudadano, String> colTelefono;
    @FXML private TableColumn<Ciudadano, String> colEstado;
    @FXML private TableColumn<Ciudadano, Void> colAcciones;

    // --- paginación ---
    @FXML private Label lblPaginacion;
    @FXML private Label lblPaginaActual;
    @FXML private ComboBox<Integer> comboFilasPorPagina;

    private CiudadanoService ciudadanoService;
    private Long idEnEdicion = null; // null => modo "crear"
    private int paginaActual = 0;

    // El controller no se construye con argumentos (JavaFX exige constructor vacío),
    // así que la dependencia se inyecta después de cargar el FXML.
    public void setCiudadanoService(CiudadanoService ciudadanoService) {
        this.ciudadanoService = ciudadanoService;
        cargarDatos();
    }

    @FXML
    public void initialize() {
        comboTipoIdentificacion.setItems(FXCollections.observableArrayList(TipoIdentificacion.values()));
        comboEstado.setItems(FXCollections.observableArrayList(EstadoCiudadano.values()));
        comboFilasPorPagina.setItems(FXCollections.observableArrayList(10, 20, 50));
        comboFilasPorPagina.setValue(10);
        comboFilasPorPagina.setOnAction(e -> { paginaActual = 0; cargarDatos(); });

        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colIdentificacion.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTipoIdentificacion() + " - " + d.getValue().getNumeroIdentificacion()));
        colNombreCompleto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCompleto()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getTelefono() == null ? "" : d.getValue().getTelefono()));
        colEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(estado);
                    badge.getStyleClass().add(estado.equals("ACTIVO") ? "badge-activo" : "badge-inactivo");
                    setGraphic(badge);
                }
            }
        });
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().toString()));

        configurarColumnaAcciones();

        campoBusqueda.textProperty().addListener((obs, viejo, nuevo) -> { paginaActual = 0; cargarDatos(); });

        prepararFormularioParaCrear();
    }

    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(columna -> new TableCell<>() {
            private final Button btnEditar = new Button("✏");
            private final Button btnEliminar = new Button("🗑");
            private final HBox contenedor = new HBox(5, btnEditar, btnEliminar);

            {
                btnEditar.getStyleClass().add("boton-accion");
                btnEliminar.getStyleClass().add("boton-accion");
                btnEditar.setOnAction(e -> onEditar(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> onEliminar(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vacio) {
                super.updateItem(item, vacio);
                setGraphic(vacio ? null : contenedor);
            }
        });
    }

    // --- carga de datos ---

    private void cargarDatos() {
        if (ciudadanoService == null) return; // aún no se inyectó el servicio

        String textoBusqueda = campoBusqueda.getText();
        List<Ciudadano> resultado = (textoBusqueda == null || textoBusqueda.isBlank())
                ? ciudadanoService.listar(paginaActual, comboFilasPorPagina.getValue(), null)
                : ciudadanoService.buscar(textoBusqueda);

        ObservableList<Ciudadano> items = FXCollections.observableArrayList(resultado);
        tablaCiudadanos.setItems(items);

        int desde = resultado.isEmpty() ? 0 : paginaActual * comboFilasPorPagina.getValue() + 1;
        int hasta = paginaActual * comboFilasPorPagina.getValue() + resultado.size();
        lblPaginacion.setText(String.format("Mostrando %d-%d de %d ciudadanos", desde, hasta, resultado.size()));
        lblPaginaActual.setText(String.valueOf(paginaActual + 1));
    }

    @FXML
    private void onPaginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarDatos();
        }
    }

    @FXML
    private void onPaginaSiguiente() {
        paginaActual++;
        cargarDatos();
        // Si la página quedó vacía (no había más datos), se regresa a la anterior.
        if (tablaCiudadanos.getItems().isEmpty() && paginaActual > 0) {
            paginaActual--;
            cargarDatos();
        }
    }

    // --- formulario: crear / editar ---

    @FXML
    private void onCambioTipoIdentificacion() {
        boolean esOtro = comboTipoIdentificacion.getValue() == TipoIdentificacion.OTRO;
        contenedorOtroTipo.setVisible(esOtro);
        contenedorOtroTipo.setManaged(esOtro);
    }

    @FXML
    private void onRegistrarNuevo() {
        prepararFormularioParaCrear();
    }

    private void prepararFormularioParaCrear() {
        idEnEdicion = null;
        lblTituloFormulario.setText("Registrar Ciudadano");
        lblSubtituloFormulario.setText("Completa los datos del nuevo ciudadano.");
        limpiarFormulario();
        comboTipoIdentificacion.setDisable(false);
        campoNumeroIdentificacion.setDisable(false);
        comboEstado.setValue(EstadoCiudadano.ACTIVO);
    }

    private void onEditar(Ciudadano ciudadano) {
        idEnEdicion = ciudadano.getId();
        lblTituloFormulario.setText("Editar Ciudadano #" + ciudadano.getId());
        lblSubtituloFormulario.setText("Actualiza los datos del registro seleccionado.");

        comboTipoIdentificacion.setValue(ciudadano.getTipoIdentificacion());
        campoOtroTipoIdentificacion.setText(ciudadano.getOtroTipoIdentificacion());
        campoNumeroIdentificacion.setText(ciudadano.getNumeroIdentificacion());
        campoNombres.setText(ciudadano.getNombres());
        campoApellidos.setText(ciudadano.getApellidos());
        campoEmail.setText(ciudadano.getEmail());
        campoTelefono.setText(ciudadano.getTelefono());
        campoDireccion.setText(ciudadano.getDireccion());
        comboEstado.setValue(ciudadano.getEstado());
        onCambioTipoIdentificacion();
        ocultarError();

        // Tipo y número de identificación no se editan — ver el comentario en
        // CiudadanoService.actualizar(): cambiarlos rompería la trazabilidad con Permiso.
        comboTipoIdentificacion.setDisable(true);
        campoNumeroIdentificacion.setDisable(true);
    }

    @FXML
    private void onGuardar() {
        ocultarError();
        try {
            if (idEnEdicion == null) {
                ciudadanoService.crear(
                        comboTipoIdentificacion.getValue(),
                        campoOtroTipoIdentificacion.getText(),
                        campoNumeroIdentificacion.getText(),
                        campoNombres.getText(),
                        campoApellidos.getText(),
                        campoEmail.getText(),
                        campoTelefono.getText(),
                        campoDireccion.getText());
            } else {
                ciudadanoService.actualizar(
                        idEnEdicion,
                        campoNombres.getText(),
                        campoApellidos.getText(),
                        campoEmail.getText(),
                        campoTelefono.getText(),
                        campoDireccion.getText());
            }
            prepararFormularioParaCrear();
            cargarDatos();
        } catch (ReglaNegocioException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        prepararFormularioParaCrear();
    }

    private void onEliminar(Ciudadano ciudadano) {
        // El texto del modal depende de si RN-08 va a inactivar en vez de borrar.
        boolean quedaraInactivo = ciudadanoService.tienePermisosAsociados(ciudadano.getId());

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(quedaraInactivo
                ? "Este ciudadano tiene permisos registrados y será marcado como Inactivo"
                : "¿Confirmar eliminación? Esta acción no se puede deshacer");
        confirmacion.setContentText(ciudadano.getNombreCompleto() + " — " + ciudadano.getNumeroIdentificacion());

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            ResultadoEliminacion resultado = ciudadanoService.eliminar(ciudadano.getId());
            if (idEnEdicion != null && idEnEdicion.equals(ciudadano.getId())) {
                prepararFormularioParaCrear(); // si se borró el que se estaba editando
            }
            cargarDatos();
        }
    }

    private void limpiarFormulario() {
        comboTipoIdentificacion.setValue(null);
        campoOtroTipoIdentificacion.clear();
        campoNumeroIdentificacion.clear();
        campoNombres.clear();
        campoApellidos.clear();
        campoEmail.clear();
        campoTelefono.clear();
        campoDireccion.clear();
        comboEstado.setValue(null);
        onCambioTipoIdentificacion();
        ocultarError();
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}