package cr.ac.ucr.paraiso.ie.c4h142.expresofast.business;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock private VehiculoRepository vehiculoRepository;
    @Mock private ConductorRepository conductorRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    @Test
    void registrarVehiculo_PlacaDuplicada_LanzaExcepcion() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca("CL-123456");

        when(vehiculoRepository.existsByPlaca("CL-123456")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> vehiculoService.registrarVehiculo(vehiculo));

        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    void asignarConductor_ConductorInactivo_LanzaExcepcion() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);

        Conductor conductor = new Conductor();
        conductor.setId(1);
        conductor.setActivo(false);

        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo));
        when(conductorRepository.findById(1)).thenReturn(Optional.of(conductor));

        assertThrows(IllegalStateException.class,
                () -> vehiculoService.asignarConductor(1, 1));
    }

    @Test
    void asignarConductor_VehiculoNoExiste_LanzaResourceNotFound() {
        when(vehiculoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculoService.asignarConductor(99, 1));
    }
}