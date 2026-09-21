package cr.ac.ucr.paraiso.ie.c4h142.expresofast.data;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioResponseDTO;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    // Projects related data directly into a DTO in one SELECT.
    @Query("""
        SELECT new cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioResponseDTO(
            e.id, e.codigoRastreo, e.direccionDestino, e.pesoKg, e.costo, e.estadoEnvio,
            e.fechaCreacion, e.fechaModificacion,
            v.placa, emp.nombre, CONCAT(c.nombre, ' ', c.apellidos)
        )
        FROM Envio e
        JOIN e.vehiculo v
        JOIN v.empresa emp
        JOIN e.conductor c
        ORDER BY e.id DESC
        """)
    List<EnvioResponseDTO> findAllOptimizados();

    @Query("""
        SELECT new cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioResponseDTO(
            e.id, e.codigoRastreo, e.direccionDestino, e.pesoKg, e.costo, e.estadoEnvio,
            e.fechaCreacion, e.fechaModificacion,
            v.placa, emp.nombre, CONCAT(c.nombre, ' ', c.apellidos)
        )
        FROM Envio e
        JOIN e.vehiculo v
        JOIN v.empresa emp
        JOIN e.conductor c
        WHERE e.id = :id
        """)
    Optional<EnvioResponseDTO> findResponseById(@Param("id") Integer id);

    // Updates the status of all shipments assigned to a vehicle.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Envio e SET e.estadoEnvio = :estado WHERE e.vehiculo.id = :vehiculoId")
    int actualizarEstadoPorVehiculo(@Param("vehiculoId") Integer vehiculoId,
                                     @Param("estado") String estado);
}