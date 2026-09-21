package cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EnvioResponseDTO {
    private Integer id;
    private String codigoRastreo;
    private String direccionDestino;
    private BigDecimal pesoKg;
    private BigDecimal costo;
    private String estadoEnvio;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String placaVehiculo;
    private String nombreEmpresa;
    private String nombreConductor;

    public EnvioResponseDTO(Integer id, String codigoRastreo, String direccionDestino,
                             BigDecimal pesoKg, BigDecimal costo, String estadoEnvio,
                             LocalDateTime fechaCreacion, LocalDateTime fechaModificacion,
                             String placaVehiculo, String nombreEmpresa, String nombreConductor) {
        this.id = id;
        this.codigoRastreo = codigoRastreo;
        this.direccionDestino = direccionDestino;
        this.pesoKg = pesoKg;
        this.costo = costo;
        this.estadoEnvio = estadoEnvio;
        this.fechaCreacion = fechaCreacion;
        this.fechaModificacion = fechaModificacion;
        this.placaVehiculo = placaVehiculo;
        this.nombreEmpresa = nombreEmpresa;
        this.nombreConductor = nombreConductor;
    }

    public Integer getId() { return id; }
    public String getCodigoRastreo() { return codigoRastreo; }
    public String getDireccionDestino() { return direccionDestino; }
    public BigDecimal getPesoKg() { return pesoKg; }
    public BigDecimal getCosto() { return costo; }
    public String getEstadoEnvio() { return estadoEnvio; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public String getPlacaVehiculo() { return placaVehiculo; }
    public String getNombreEmpresa() { return nombreEmpresa; }
    public String getNombreConductor() { return nombreConductor; }
}