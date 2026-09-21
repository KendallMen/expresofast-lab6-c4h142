package cr.ac.ucr.paraiso.ie.c4h142.expresofast.data;

import org.springframework.data.jpa.repository.JpaRepository;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> { 
    boolean existsByPlaca(String placa);
}

