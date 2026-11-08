package co.edu.uniquindio.permisosmunicipales.service;

import co.edu.uniquindio.permisosmunicipales.model.Funcionario;
import co.edu.uniquindio.permisosmunicipales.model.Permiso;

import java.time.Clock;
import java.time.LocalDateTime;

public class AuditoriaService {

    private final Clock reloj;

    public AuditoriaService(Clock reloj) {
        this.reloj = reloj;
    }

    public void registrar(Permiso permiso, Funcionario funcionario) {
        LocalDateTime selloDeTiempo = LocalDateTime.now(reloj);
        // RN-07: trazabilidad de quién radicó y cuándo.
        System.out.printf("[AUDITORIA] %s radicó %s el %s%n",
                funcionario.getNombreCompleto(), permiso.getNumeroRadicado(), selloDeTiempo);
    }
}