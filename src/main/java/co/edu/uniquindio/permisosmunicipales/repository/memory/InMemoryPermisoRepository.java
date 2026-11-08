package co.edu.uniquindio.permisosmunicipales.repository.memory;

import co.edu.uniquindio.permisosmunicipales.model.Permiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoPermiso;
import co.edu.uniquindio.permisosmunicipales.model.enums.TipoPermiso;
import co.edu.uniquindio.permisosmunicipales.repository.PermisoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InMemoryPermisoRepository implements PermisoRepository {

    private static final Pattern PATRON_RADICADO = Pattern.compile("RAD-(\\d{4})-(\\d+)");

    private final Map<Long, Permiso> almacen = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicLong> consecutivosPorAnio = new ConcurrentHashMap<>();
    private final AtomicLong contadorId = new AtomicLong(1);

    @Override
    public Permiso guardar(Permiso permiso) {
        if (permiso.getId() == null) {
            permiso.setId(contadorId.getAndIncrement());
        } else {
            contadorId.updateAndGet(actual -> Math.max(actual, permiso.getId() + 1));
        }
        almacen.put(permiso.getId(), permiso);
        // Así, si el SeedLoader carga RAD-2026-00002, el siguiente consecutivo
        // real para 2026 ya sale en 3 — nunca choca con un radicado sembrado.
        sincronizarConsecutivo(permiso.getNumeroRadicado());
        return permiso;
    }

    private void sincronizarConsecutivo(String numeroRadicado) {
        Matcher m = PATRON_RADICADO.matcher(numeroRadicado);
        if (m.matches()) {
            int anio = Integer.parseInt(m.group(1));
            long consecutivo = Long.parseLong(m.group(2));
            consecutivosPorAnio.computeIfAbsent(anio, a -> new AtomicLong(0))
                    .updateAndGet(actual -> Math.max(actual, consecutivo));
        }
    }

    @Override
    public Optional<Permiso> buscarPorId(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public Optional<Permiso> buscarPorNumeroRadicado(String numeroRadicado) {
        return almacen.values().stream()
                .filter(p -> p.getNumeroRadicado().equals(numeroRadicado))
                .findFirst();
    }

    @Override
    public boolean existeRadicadoActivo(Long ciudadanoId, TipoPermiso tipoPermiso) {
        return almacen.values().stream()
                .anyMatch(p -> p.getCiudadano().getId().equals(ciudadanoId)
                        && p.getTipoPermiso() == tipoPermiso
                        && p.getEstado() == EstadoPermiso.RADICADO);
    }

    @Override
    public List<Permiso> buscarPorCiudadano(Long ciudadanoId) {
        return almacen.values().stream()
                .filter(p -> p.getCiudadano().getId().equals(ciudadanoId))
                .sorted(Comparator.comparing(Permiso::getFechaRadicacion))
                .collect(Collectors.toList());
    }

    @Override
    public long siguienteConsecutivo(int anio) {
        return consecutivosPorAnio.computeIfAbsent(anio, a -> new AtomicLong(0)).incrementAndGet();
    }

    @Override
    public List<Permiso> listarTodos() {
        return almacen.values().stream()
                .sorted(Comparator.comparing(Permiso::getFechaRadicacion).reversed())
                .collect(Collectors.toList());
    }
}