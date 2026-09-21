package cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto;

import jakarta.validation.constraints.NotBlank;

public class CambioEstadoDTO {
    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado;

    private String observaciones;

    public String getNuevoEstado() { return nuevoEstado; }
    public void setNuevoEstado(String nuevoEstado) { this.nuevoEstado = nuevoEstado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}