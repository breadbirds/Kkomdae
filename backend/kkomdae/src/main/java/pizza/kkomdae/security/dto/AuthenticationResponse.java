package pizza.kkomdae.security.dto;

import lombok.Getter;

@Getter
public class AuthenticationResponse {
    private String jwt;
    private String refreshToken;

    public AuthenticationResponse(String jwt, String refreshToken) {
        this.jwt = jwt;
        this.refreshToken = refreshToken;
    }
}
