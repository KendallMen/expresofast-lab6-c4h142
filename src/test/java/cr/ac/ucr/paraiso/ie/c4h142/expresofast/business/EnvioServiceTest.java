package cr.ac.ucr.paraiso.ie.c4h142.expresofast.business;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.CapacidadExcedidaException;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.InvalidStateTransitionException;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private ConductorRepository conductorRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private BitacoraEnvioRepository bitacoraEnvioRepository;

    @InjectMocks
    private EnvioService envioService;

    private Vehiculo vehiculo;
    private Conductor conductor;

    @BeforeEach
    void setUp() {
        vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setCapacidadKg(new BigDecimal("500.0"));

        conductor = new Conductor();
        conductor.setId(1);
    }

    @Test
    void crearEnvio_DatosValidos_RetornaEnvioDTO() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setCodigoRastreo("EXP-1234");
        dto.setDireccionDestino("Paraiso, Cartago");
        dto.setPesoKg(new BigDecimal("10.0"));
        dto.setCosto(new BigDecimal("3500.00"));
        dto.setVehiculoId(1);
        dto.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));
        when(envioRepository.save(any(Envio.class))).thenAnswer(inv -> inv.getArgument(0));

        Envio resultado = envioService.registrarEnvio(dto);

        assertEquals("PENDIENTE", resultado.getEstadoEnvio());
        assertEquals("EXP-1234", resultado.getCodigoRastreo());
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    void crearEnvio_VehiculoSinCapacidad_LanzaExcepcion() {
        EnvioRequestDTO dto = new EnvioRequestDTO();
        dto.setPesoKg(new BigDecimal("600.0")); // supera capacidad de 500
        dto.setVehiculoId(1);
        dto.setConductorId(1);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        assertThrows(CapacidadExcedidaException.class, () -> envioService.registrarEnvio(dto));
        verify(envioRepository, never()).save(any());
    }

    @Test
    void actualizarEstado_TransicionInvalida_LanzaExcepcion() {
        Envio envio = new Envio();
        envio.setId(1);
        envio.setCodigoRastreo("EXP-9999");
        envio.setEstadoEnvio("ENTREGADO");

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado("EN_TRANSITO");

        assertThrows(InvalidStateTransitionException.class,
                () -> envioService.actualizarEstado(1, cambio));

        verify(bitacoraEnvioRepository, never()).save(any());
    }

    @Test
    void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {
        Envio envio = new Envio();
        envio.setId(1);
        envio.setCodigoRastreo("EXP-5555");
        envio.setEstadoEnvio("EN_TRANSITO");

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));

        assertThrows(InvalidStateTransitionException.class, () -> envioService.cancelarEnvio(1));
    }

    @ParameterizedTest
    @CsvSource({
        "10.0, 20.0, 4000.0",
        "5.0, 10.0, 2500.0",
        "25.0, 100.0, 11000.0"
    })
    void calcularTarifa_CasosVariados_CalculaCorrectamente(
            double pesoKg, double distanciaKm, double tarifaEsperada) {
        double tarifaCalculada = envioService.calcularTarifa(pesoKg, distanciaKm);
        assertEquals(tarifaEsperada, tarifaCalculada, 0.01);
    }

    @Test
    void actualizarEstado_TransicionValida_ActualizaYRegistraBitacora() {
        Envio envio = new Envio();
        envio.setId(1);
        envio.setCodigoRastreo("EXP-1111");
        envio.setEstadoEnvio("PENDIENTE");

        Usuario usuario = new Usuario();
        usuario.setUsername("admin");

        when(envioRepository.findById(1)).thenReturn(Optional.of(envio));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, java.util.List.of()));

        CambioEstadoDTO cambio = new CambioEstadoDTO();
        cambio.setNuevoEstado("EN_TRANSITO");
        cambio.setObservaciones("Sale de bodega");

        Envio resultado = envioService.actualizarEstado(1, cambio);

        assertEquals("EN_TRANSITO", resultado.getEstadoEnvio());
        verify(bitacoraEnvioRepository, times(1)).save(any(BitacoraEnvio.class));
    }

    @Test
    void cancelarEnvio_EnvioPendiente_CancelaSinExcepcion() {
        Envio envio = new Envio();
        envio.setId(2);
        envio.setCodigoRastreo("EXP-2222");
        envio.setEstadoEnvio("PENDIENTE");

        when(envioRepository.findById(2)).thenReturn(Optional.of(envio));

        envioService.cancelarEnvio(2);

        assertEquals("CANCELADO", envio.getEstadoEnvio());
    }

    @Test
    void obtenerBitacora_EnvioExiste_RetornaHistorial() {
        when(envioRepository.existsById(1)).thenReturn(true);
        when(bitacoraEnvioRepository.findByEnvioId(1)).thenReturn(java.util.List.of());

        var resultado = envioService.obtenerBitacora(1);

        assertNotNull(resultado);
        verify(bitacoraEnvioRepository, times(1)).findByEnvioId(1);
    }

    @Test
    void obtenerEnviosOptimizados_RetornaListaDelRepositorio() {
        when(envioRepository.findAllOptimizados()).thenReturn(java.util.List.of());

        var resultado = envioService.obtenerEnviosOptimizados();

        assertNotNull(resultado);
        verify(envioRepository, times(1)).findAllOptimizados();
    }
}
