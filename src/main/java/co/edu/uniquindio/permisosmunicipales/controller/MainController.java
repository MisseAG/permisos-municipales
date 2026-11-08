    package co.edu.uniquindio.permisosmunicipales.controller;

    import co.edu.uniquindio.permisosmunicipales.repository.CiudadanoRepository;
    import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;
    import co.edu.uniquindio.permisosmunicipales.service.*;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.control.Button;
    import javafx.scene.control.Label;
    import javafx.scene.layout.StackPane;

    import java.io.IOException;
    import java.time.Clock;

    public class MainController {

        @FXML private StackPane areaContenido;
        @FXML private Button btnNavCiudadanos;
        @FXML private Button btnNavPermisosRadicar; // antes btnNavPermisos
        @FXML private Button btnNavPermisosListado; // nuevo

        private CiudadanoService ciudadanoService;
        private CiudadanoRepository ciudadanoRepository;
        private PermisoRepository permisoRepository;


        public void setDependencias(CiudadanoRepository ciudadanoRepository,
                                    PermisoRepository permisoRepository) {
            this.ciudadanoRepository = ciudadanoRepository;
            this.permisoRepository = permisoRepository;
            this.ciudadanoService = new CiudadanoService(ciudadanoRepository, permisoRepository);
            onSeleccionarCiudadanos(); // vista inicial al abrir la app
        }

        @FXML
        private void onSeleccionarCiudadanos() {
            marcarActivo(btnNavCiudadanos);
            cargarVista("/fxml/ciudadano-crud.fxml", loader -> {
                CiudadanoCrudController controller = loader.getController();
                controller.setCiudadanoService(ciudadanoService);
            });
        }

        @FXML
        private void onSeleccionarPermisos() {
            marcarActivo(btnNavPermisosRadicar);
            cargarVista("/fxml/radicar-permiso.fxml", loader -> {
                RadicarPermisoController controller = loader.getController();
                controller.setServicios(ciudadanoService,
                        new PermisoService(permisoRepository, ciudadanoRepository, new ReglasRadicacion(),
                                new GeneradorRadicado(), new AuditoriaService(Clock.systemDefaultZone()),
                                Clock.systemDefaultZone()));
            });
        }


        private void cargarVista(String rutaFxml, java.util.function.Consumer<FXMLLoader> inyectarDependencias) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
                Parent vista = loader.load();
                inyectarDependencias.accept(loader);
                areaContenido.getChildren().setAll(vista);
            } catch (IOException e) {
                throw new RuntimeException("No se pudo cargar " + rutaFxml, e);
            }
        }

        @FXML
        private void onSeleccionarListadoPermisos() {
            marcarActivo(btnNavPermisosListado);
            cargarVista("/fxml/permisos-listado.fxml", loader -> {
                PermisosListadoController controller = loader.getController();
                controller.setPermisoRepository(permisoRepository);
            });
        }

        private void marcarActivo(Button botonActivo) {
            btnNavCiudadanos.getStyleClass().remove("nav-button-activo");
            btnNavPermisosRadicar.getStyleClass().remove("nav-button-activo");
            btnNavPermisosListado.getStyleClass().remove("nav-button-activo");
            botonActivo.getStyleClass().add("nav-button-activo");
        }
    }