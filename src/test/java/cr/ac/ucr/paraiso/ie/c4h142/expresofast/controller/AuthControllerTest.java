package cr.ac.ucr.paraiso.ie.c4h142.expresofast.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.business.AuthService;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.security.JwtTokenProvider;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private AuthService authService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider; 

    @Test
    void login_CredencialesCorrectas_Retorna200ConToken() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("Password123!");

        AuthResponseDTO response = new AuthResponseDTO(
                "eyJhbGciOiJIUzI1NiJ9.fake.token", "admin", List.of("ROLE_ADMIN"), 3600000L);

        when(authService.login(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void login_CredencialesIncorrectas_Retorna401() throws Exception {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("admin");
        request.setPassword("incorrecta");

        when(authService.login(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}