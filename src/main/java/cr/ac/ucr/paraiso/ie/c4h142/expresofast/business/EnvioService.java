package cr.ac.ucr.paraiso.ie.c4h142.expresofast.business;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.CapacidadExcedidaException;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.ResourceNotFoundException;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BitacoraEnvioRepository bitacoraEnvioRepository;
    private static final double TARIFA_BASE = 1000.0;
    private static final double COSTO_POR_KG = 200.0;
    private static final double COSTO_POR_KM = 50.0;

    public EnvioService(EnvioRepository envioRepository,
            VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository,
            UsuarioRepository usuarioRepository,
            BitacoraEnvioRepository bitacoraEnvioRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
        this.bitacoraEnvioRepository = bitacoraEnvioRepository;
    }

    @Transactional(readOnly = true)
    public List<EnvioResponseDTO> obtenerEnviosOptimizados() {
        return envioRepository.findAllOptimizados();
    }

    @Transactional
    public Envio registrarEnvio(EnvioRequestDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findById(dto.getVehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        Conductor conductor = conductorRepository.findById(dto.getConductorId())
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        if (dto.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new CapacidadExcedidaException(
                    "El peso del envío (" + dto.getPesoKg() + " kg) supera la capacidad del vehículo ("
                    + vehiculo.getCapacidadKg() + " kg)");
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(dto.getCodigoRastreo());
        envio.setDireccionDestino(dto.getDireccionDestino());
        envio.setPesoKg(dto.getPesoKg());
        envio.setCosto(dto.getCosto());
        envio.setEstadoEnvio("PENDIENTE");
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);

        Envio guardado = envioRepository.save(envio);
        return guardado;
    }

    @Transactional
    public Envio actualizarEstado(Integer id, CambioEstadoDTO cambioDTO) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));

        String estadoAnterior = envio.getEstadoEnvio();
        String estadoNuevo = cambioDTO.getNuevoEstado();

        validarTransicion(estadoAnterior, estadoNuevo, envio.getCodigoRastreo());

        envio.setEstadoEnvio(estadoNuevo);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        BitacoraEnvio bitacora = new BitacoraEnvio();
        bitacora.setEnvio(envio);
        bitacora.setEstadoAnterior(estadoAnterior);
        bitacora.setEstadoNuevo(estadoNuevo);
        bitacora.setFechaCambio(LocalDateTime.now());
        bitacora.setUsuario(usuario);
        bitacora.setObservaciones(cambioDTO.getObservaciones());
        bitacoraEnvioRepository.save(bitacora);

        return envio;
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerRespuesta(Integer id) {
        return envioRepository.findResponseById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));
    }

    private void validarTransicion(String estadoActual, String estadoNuevo, String codigo) {
        boolean esEstadoFinal = "ENTREGADO".equals(estadoActual) || "CANCELADO".equals(estadoActual);
        boolean intentaVolverAtras = "PENDIENTE".equals(estadoNuevo) || "EN_TRANSITO".equals(estadoNuevo);

        if (esEstadoFinal && intentaVolverAtras) {
            throw new InvalidStateTransitionException(
                    "Transición de estado no permitida para el envío " + codigo);
        }
    }

    @Transactional(readOnly = true)
    public List<BitacoraResponseDTO> obtenerBitacora(Integer envioId) {
        if (!envioRepository.existsById(envioId)) {
            throw new ResourceNotFoundException("Envío no encontrado");
        }
        return bitacoraEnvioRepository.findByEnvioId(envioId);
    }

    @Transactional
    public int actualizarEstadoPorVehiculo(Integer vehiculoId, String estado) {
        return envioRepository.actualizarEstadoPorVehiculo(vehiculoId, estado);
    }

    @Transactional
    public void cancelarEnvio(Integer id) {
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Envío no encontrado"));

        if ("EN_TRANSITO".equals(envio.getEstadoEnvio())) {
            throw new InvalidStateTransitionException(
                    "No se puede cancelar el envío " + envio.getCodigoRastreo() + " porque ya está en tránsito");
        }

        envio.setEstadoEnvio("CANCELADO");
    }

    public double calcularTarifa(double pesoKg, double distanciaKm) {
        return TARIFA_BASE + (pesoKg * COSTO_POR_KG) + (distanciaKm * COSTO_POR_KM);
    }
}
