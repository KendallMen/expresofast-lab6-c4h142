package cr.ac.ucr.paraiso.ie.c4h142.expresofast.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cr.ac.ucr.paraiso.ie.c4h142.expresofast.business.EnvioService;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto.EnvioResponseDTO;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/envios")
@CrossOrigin(origins = "*")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> obtenerOptimizados() {
        return ResponseEntity.ok(envioService.obtenerEnviosOptimizados());
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> registrar(@Valid @RequestBody EnvioRequestDTO dto) {
        Envio guardado = envioService.registrarEnvio(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(envioService.obtenerRespuesta(guardado.getId()));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstado(@PathVariable Integer id,
                                                              @Valid @RequestBody CambioEstadoDTO cambioDTO) {
        envioService.actualizarEstado(id, cambioDTO);
        return ResponseEntity.ok(envioService.obtenerRespuesta(id));
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacora(id));
    }
}