package cr.ac.ucr.paraiso.ie.c4h142.expresofast.business;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.security.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_CredencialesValidas_RetornaTokenYRoles() {
        Authentication authMock = new UsernamePasswordAuthenticationToken(
            "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        when(authenticationManager.authenticate(any())).thenReturn(authMock);
        when(jwtTokenProvider.generarToken(authMock)).thenReturn("token-falso");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(3600000L);

        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("Password123!");

        AuthResponseDTO response = authService.login(request);

        assertEquals("token-falso", response.getToken());
        assertEquals("admin", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void login_CredencialesInvalidas_LanzaExcepcion() {
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("incorrecta");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}