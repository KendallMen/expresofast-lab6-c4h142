package cr.ac.ucr.paraiso.ie.c4h142.expresofast.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombreRol(String nombreRol);
}