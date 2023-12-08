package org.openhab.binding.salus.internal.rest;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public record AuthToken(
        @SerializedName("access_token") String accessToken,
        @SerializedName("refresh_token") String refreshToken,
        @SerializedName("expires_in") Long expiresIn,
        @SerializedName("role") String role) {
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
