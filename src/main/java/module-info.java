module co.edu.uniquindio.permisosmunicipales {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.permisosmunicipales.app to javafx.fxml;
    exports co.edu.uniquindio.permisosmunicipales.app;

    opens co.edu.uniquindio.permisosmunicipales.controller to javafx.fxml;
}