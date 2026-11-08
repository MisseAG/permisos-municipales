package co.edu.uniquindio.permisosmunicipales.repository.memory;

import co.edu.uniquindio.permisosmunicipales.model.Ciudadano;
import co.edu.uniquindio.permisosmunicipales.model.enums.EstadoCiudadano;
import co.edu.uniquindio.permisosmunicipales.repository.CiudadanoRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InMemoryCiudadanoRepository implements CiudadanoRepository {

    private final Map<Long, Ciudadano> almacen = new ConcurrentHashMap<>();
    private final AtomicLong contadorId = new AtomicLong(1);

    @Override
    public Ciudadano guardar(Ciudadano ciudadano) {
        if (ciudadano.getId() == null) {
            ciudadano.setId(contadorId.getAndIncrement());
        } else {
            // si viene con id explícito (p. ej. desde el SeedLoader), el contador
            // no debe volver a repartir ese mismo número para un ciudadano nuevo
            contadorId.updateAndGet(actual -> Math.max(actual, ciudadano.getId() + 1));
        }
        almacen.put(ciudadano.getId(), ciudadano);
        return ciudadano;
    }

    @Override
    public Optional<Ciudadano> buscarPorId(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public Optional<Ciudadano> buscarPorNumeroIdentificacion(String numeroIdentificacion) {
        return almacen.values().stream()
                .filter(c -> c.getNumeroIdentificacion().equals(numeroIdentificacion))
                .findFirst();
    }

    @Override
    public boolean existePorNumeroIdentificacion(String numeroIdentificacion) {
        return buscarPorNumeroIdentificacion(numeroIdentificacion).isPresent();
    }

    @Override
    public List<Ciudadano> buscar(String textoLibre) {
        String filtro = textoLibre == null ? "" : textoLibre.toLowerCase().trim();
        if (filtro.isEmpty()) {
            return listar(0, Integer.MAX_VALUE, null);
        }
        return almacen.values().stream()
                .filter(c -> c.getNombres().toLowerCase().contains(filtro)
                        || c.getApellidos().toLowerCase().contains(filtro)
                        || c.getNumeroIdentificacion().toLowerCase().contains(filtro))
                .sorted(Comparator.comparing(Ciudadano::getId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Ciudadano> listar(int pagina, int tamanoPagina, EstadoCiudadano filtroEstado) {
        return almacen.values().stream()
                .filter(c -> filtroEstado == null || c.getEstado() == filtroEstado)
                .sorted(Comparator.comparing(Ciudadano::getId))
                .skip((long) pagina * tamanoPagina)
                .limit(tamanoPagina)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminar(Long id) {
        // RN-08: un ciudadano con permisos no se elimina, se inactiva.
        // Por eso "eliminar" nunca hace almacen.remove(id).
        buscarPorId(id).ifPresent(c -> c.setEstado(EstadoCiudadano.INACTIVO));
    }

    @Override
    public void eliminarFisicamente(Long id) {
        almacen.remove(id);
    }

}