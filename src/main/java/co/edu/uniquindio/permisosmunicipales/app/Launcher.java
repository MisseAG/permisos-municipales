package co.edu.uniquindio.permisosmunicipales.app;

import co.edu.uniquindio.permisosmunicipales.controller.MainController;
import co.edu.uniquindio.permisosmunicipales.repository.CiudadanoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.memory.InMemoryCiudadanoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.memory.InMemoryPermisoRepository;
import co.edu.uniquindio.permisosmunicipales.service.*;
import co.edu.uniquindio.permisosmunicipales.util.SeedLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Launcher extends Application{

    private CiudadanoRepository ciudadanoRepo;
    private PermisoRepository permisoRepo;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        ciudadanoRepo = new InMemoryCiudadanoRepository();
        permisoRepo = new InMemoryPermisoRepository();
        SeedLoader.cargarDatosIniciales(ciudadanoRepo, permisoRepo);

        System.out.println("Inicializando aplicación...");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);

        MainController controller = loader.getController();
        controller.setDependencias(ciudadanoRepo, permisoRepo);

        stage.setScene(scene);
        stage.setTitle("Permisos Municipales");
        stage.show();
    }
}
