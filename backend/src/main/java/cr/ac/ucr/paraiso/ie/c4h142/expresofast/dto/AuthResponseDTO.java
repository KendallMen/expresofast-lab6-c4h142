package cr.ac.ucr.paraiso.ie.c4h142.expresofast.dto;

import java.util.List;

public class AuthResponseDTO {
    private String token;
    private String username;
    private List<String> roles;
    private long expirationTime;

    public AuthResponseDTO(String token, String username, List<String> roles, long expirationTime) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expirationTime = expirationTime;
    }

    public String getToken() { return token; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public long getExpirationTime() { return expirationTime; }
}