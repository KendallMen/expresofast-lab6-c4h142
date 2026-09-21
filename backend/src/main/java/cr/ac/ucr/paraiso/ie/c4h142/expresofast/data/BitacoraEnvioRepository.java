package cr.ac.ucr.paraiso.ie.c4h142.expresofast.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.BitacoraResponseDTO;

public interface BitacoraEnvioRepository extends JpaRepository<BitacoraEnvio, Integer> {

    @Query("""
        SELECT new cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.BitacoraResponseDTO(
            b.id, b.estadoAnterior, b.estadoNuevo, b.fechaCambio, u.username, b.observaciones)
        FROM BitacoraEnvio b
        JOIN b.usuario u
        WHERE b.envio.id = :envioId
        ORDER BY b.fechaCambio DESC
        """)
    List<BitacoraResponseDTO> findByEnvioId(@Param("envioId") Integer envioId);
}