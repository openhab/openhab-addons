package org.openhab.binding.salusbinding.internal.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record AuthToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("role") String role) {
    public AuthToken {
        Objects.requireNonNull(accessToken, "accessToken cannot be null!");
        Objects.requireNonNull(refreshToken, "refreshToken cannot be null!");
    }

    @Override
    public String toString() {
        return "AuthToken{" +
                "accessToken='<SECRET>'" +
                ", refreshToken='<SECRET>'" +
                ", expiresIn=" + expiresIn +
                ", role='" + role + '\'' +
                '}';
    }
}
