module co.edu.uniquindio.permisosmunicipales {
    requires javafx.controls;
    requires javafx.fxml;


    opens co.edu.uniquindio.permisosmunicipales to javafx.fxml;
    exports co.edu.uniquindio.permisosmunicipales;
  //  opens co.edu.uniquindio.permisosmunicipales.controller to javafx.fxml;

}