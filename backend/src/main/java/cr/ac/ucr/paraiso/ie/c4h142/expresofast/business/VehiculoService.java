package cr.ac.ucr.paraiso.ie.c4h142.expresofast.business;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.VehiculoRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.ResourceNotFoundException;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, ConductorRepository conductorRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
    }

    @Transactional
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new DuplicateResourceException("Ya existe un vehículo con la placa " + vehiculo.getPlaca());
        }
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional
    public void asignarConductor(Integer vehiculoId, Integer conductorId) {
        Vehiculo vehiculo = vehiculoRepository.findById(vehiculoId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException("Conductor no encontrado"));

        if (Boolean.FALSE.equals(conductor.getActivo())) {
            throw new IllegalStateException("No se puede asignar un conductor inactivo");
        }
    }
}