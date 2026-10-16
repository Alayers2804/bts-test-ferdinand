
package com.bts.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

    @JsonProperty("authentication_token")
    private final String authenticationToken;

    @JsonProperty("refresh_token")
    private final String refreshToken;

    public LoginResponse(
            String authenticationToken,
            String refreshToken) {
        this.authenticationToken = authenticationToken;
        this.refreshToken = refreshToken;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
