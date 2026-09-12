package cr.ac.ucr.paraiso.ie.c4h142.expresofast.security;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.data.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Usuario;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        var authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombreRol()))
                .collect(Collectors.toList());

        return User.withUsername(usuario.getUsername())
            .password(usuario.getPasswordHash())
            .authorities(authorities)
            .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
            .build();
    }
}