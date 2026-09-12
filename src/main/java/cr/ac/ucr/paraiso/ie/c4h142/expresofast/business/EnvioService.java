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
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.ResourceNotFoundException;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final UsuarioRepository usuarioRepository;
    private final BitacoraEnvioRepository bitacoraEnvioRepository;

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

        // Regla de negocio: el peso del envío no puede superar la capacidad del vehículo
        if (dto.getPesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new IllegalArgumentException(
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

        envio.setEstadoEnvio(estadoNuevo); // Dirty Checking: Hibernate hace el UPDATE al hacer commit

        // Usuario autenticado que realiza el cambio (Pista 2 del enunciado)
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

    // Reto autónomo: bloquear transiciones de estado inválidas
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
        // Verifica que el envío exista antes de consultar su historial
        if (!envioRepository.existsById(envioId)) {
            throw new ResourceNotFoundException("Envío no encontrado");
        }
        return bitacoraEnvioRepository.findByEnvioId(envioId);
    }

    // Se mantiene del Laboratorio 5, por si aún la usas para la actualización masiva por vehículo
    @Transactional
    public int actualizarEstadoPorVehiculo(Integer vehiculoId, String estado) {
        return envioRepository.actualizarEstadoPorVehiculo(vehiculoId, estado);
    }
}