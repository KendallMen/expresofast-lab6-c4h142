package cr.ac.ucr.paraiso.ie.c4h142.expresofast.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.security.JwtTokenProvider;

@WebMvcTest(controllers = EnvioController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva los filtros de Spring Security en este slice test
class EnvioControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private EnvioService envioService;
    @MockitoBean private JwtTokenProvider jwtTokenProvider;

    @Test
    void obtenerOptimizados_Exitoso_Retorna200ConListaJson() throws Exception {
        EnvioResponseDTO dto = new EnvioResponseDTO(
                1, "EXP-1234", "Paraiso, Cartago", new BigDecimal("10.0"), new BigDecimal("3500.00"),
                "PENDIENTE", null, null, "CL-123456", "Transportes X", "Juan Pérez");

        when(envioService.obtenerEnviosOptimizados()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/envios/optimizados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoRastreo").value("EXP-1234"));
    }

    @Test
    void registrar_PayloadInvalido_Retorna400ConErroresDeValidacion() throws Exception {
        EnvioRequestDTO dtoInvalido = new EnvioRequestDTO(); // todos los campos nulos/vacíos

        mockMvc.perform(post("/api/envios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").exists());
    }

    @Test
    void obtenerBitacora_EnvioNoEncontrado_Retorna404() throws Exception {
        when(envioService.obtenerBitacora(99))
                .thenThrow(new ResourceNotFoundException("Envío no encontrado"));

        mockMvc.perform(get("/api/envios/99/bitacora"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Envío no encontrado"));
    }
}