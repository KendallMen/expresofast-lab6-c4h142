// BitacoraResponseDTO.java
package cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto;

import java.time.LocalDateTime;

public class BitacoraResponseDTO {
    private Integer id;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime fechaCambio;
    private String usuario;
    private String observaciones;

    public BitacoraResponseDTO(Integer id, String estadoAnterior, String estadoNuevo,
                                LocalDateTime fechaCambio, String usuario, String observaciones) {
        this.id = id;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.fechaCambio = fechaCambio;
        this.usuario = usuario;
        this.observaciones = observaciones;
    }

    public Integer getId() { return id; }
    public String getEstadoAnterior() { return estadoAnterior; }
    public String getEstadoNuevo() { return estadoNuevo; }
    public LocalDateTime getFechaCambio() { return fechaCambio; }
    public String getUsuario() { return usuario; }
    public String getObservaciones() { return observaciones; }
}