package co.edu.uniquindio.permisosmunicipales.app;

import co.edu.uniquindio.permisosmunicipales.controller.CiudadanoCrudController;
import co.edu.uniquindio.permisosmunicipales.controller.MainController;
import co.edu.uniquindio.permisosmunicipales.model.Funcionario;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;
import co.edu.uniquindio.permisosmunicipales.repository.CiudadanoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.memory.InMemoryCiudadanoRepository;
import co.edu.uniquindio.permisosmunicipales.repository.memory.InMemoryPermisoRepository;
import co.edu.uniquindio.permisosmunicipales.service.*;
import co.edu.uniquindio.permisosmunicipales.service.dto.ResultadoRadicacion;
import co.edu.uniquindio.permisosmunicipales.service.dto.SolicitudRadicacion;
import co.edu.uniquindio.permisosmunicipales.service.exception.ReglaNegocioException;
import co.edu.uniquindio.permisosmunicipales.util.SeedLoader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

public class Test extends Application {

    private CiudadanoRepository ciudadanoRepo;
    private PermisoRepository permisoRepo;

    public static void main(String[] args) {
        launch(args); // arranca el hilo de JavaFX y luego llama a start()
    }

    @Override
    public void start(Stage stage) throws IOException {
        ciudadanoRepo = new InMemoryCiudadanoRepository();
        permisoRepo = new InMemoryPermisoRepository();
        SeedLoader.cargarDatosIniciales(ciudadanoRepo, permisoRepo);

        // Prueba 1
        System.out.println("Test01");
        System.out.println(ciudadanoRepo.listar(0, 20, null).size()); // debería imprimir 10
        System.out.println(permisoRepo.existeRadicadoActivo(1L, TipoPermiso.CONSTRUCCION_OBRA_NUEVA)); // true

        // Prueba 2
        System.out.println("Test02");
        Clock reloj = Clock.systemDefaultZone();
        PermisoService permisoService = new PermisoService(
                permisoRepo, ciudadanoRepo, new ReglasRadicacion(),
                new GeneradorRadicado(), new AuditoriaService(reloj), reloj);

        Funcionario funcionarioDePrueba = new Funcionario(1L, "jperez", "Juliana", "Pérez");

        ResultadoRadicacion resultado = permisoService.radicar(new SolicitudRadicacion("1098765432",
                TipoPermiso.CONSTRUCCION_OBRA_NUEVA, null, "Otra obra", "Cra 5 #10-20",
                List.of(), true, true, funcionarioDePrueba));

        if (resultado.isExitosa()) {
            System.out.println("ERROR: no debió radicar, RN-02 debía fallar");
        } else {
            resultado.getResultados().forEach(r ->
                    System.out.println(r.getCodigo() + " -> " + (r.isCumple() ? "CUMPLE" : "NO CUMPLE: " + r.getDetalle())));
        }

        // Prueba 3
        System.out.println("Test03");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);

        MainController controller = loader.getController();
        controller.setDependencias(ciudadanoRepo, permisoRepo);

        stage.setScene(scene);
        stage.setTitle("Permisos Municipales");
        stage.show();
    }
}