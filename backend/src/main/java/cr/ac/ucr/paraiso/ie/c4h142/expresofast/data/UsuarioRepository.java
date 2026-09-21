package cr.ac.ucr.paraiso.ie.c4h142.expresofast.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
}