package co.edu.uniquindio.permisosmunicipales.service;

public class GeneradorRadicado {

    public String generar(int anio, long consecutivo) {
        return String.format("RAD-%d-%05d", anio, consecutivo);
    }
}