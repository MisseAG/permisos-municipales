package co.edu.uniquindio.permisosmunicipales.controller;

import co.edu.uniquindio.permisosmunicipales.model.Permiso;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PermisosListadoController {

    @FXML private TextField campoBusqueda;
    @FXML private TableView<Permiso> tablaPermisos;
    @FXML private TableColumn<Permiso, String> colRadicado;
    @FXML private TableColumn<Permiso, String> colCiudadano;
    @FXML private TableColumn<Permiso, String> colIdentificacion;
    @FXML private TableColumn<Permiso, String> colTipoPermiso;
    @FXML private TableColumn<Permiso, String> colFechaRadicacion;
    @FXML private TableColumn<Permiso, String> colFechaLimite;
    @FXML private TableColumn<Permiso, String> colEstado;
    @FXML private Label lblTotal;

    private PermisoRepository permisoRepository;

    public void setPermisoRepository(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
        cargarDatos();
    }

    @FXML
    public void initialize() {
        colRadicado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroRadicado()));
        colCiudadano.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCiudadano().getNombreCompleto()));
        colIdentificacion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCiudadano().getNumeroIdentificacion()));
        colTipoPermiso.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getOtroTipoPermiso() != null
                        ? d.getValue().getOtroTipoPermiso()
                        : d.getValue().getTipoPermiso().toString()));
        colFechaRadicacion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaRadicacion().toString()));
        colFechaLimite.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFechaLimiteRespuesta().toString()));

        // mismo estilo de "badge" que ya usas en la columna Estado de Ciudadano
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().toString()));
        colEstado.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean vacio) {
                super.updateItem(estado, vacio);
                if (vacio || estado == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(estado);
                    badge.getStyleClass().add(estado.equals("RADICADO") ? "badge-activo" : "badge-inactivo");
                    setGraphic(badge);
                }
            }
        });

        campoBusqueda.textProperty().addListener((obs, viejo, nuevo) -> cargarDatos());
    }

    private void cargarDatos() {
        if (permisoRepository == null) return;

        List<Permiso> todos = permisoRepository.listarTodos();
        String filtro = campoBusqueda.getText() == null ? "" : campoBusqueda.getText().toLowerCase(Locale.ROOT).trim();

        List<Permiso> filtrados = filtro.isEmpty() ? todos : todos.stream()
                .filter(p -> p.getNumeroRadicado().toLowerCase(Locale.ROOT).contains(filtro)
                        || p.getCiudadano().getNumeroIdentificacion().toLowerCase(Locale.ROOT).contains(filtro))
                .collect(Collectors.toList());

        tablaPermisos.setItems(FXCollections.observableArrayList(filtrados));
        lblTotal.setText(filtrados.size() + " de " + todos.size() + " permisos");
    }
}