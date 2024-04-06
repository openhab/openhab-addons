package org.openhab.binding.onecta.internal.api.dto.authentication;

import com.google.gson.annotations.SerializedName;

public class RespTokenResult {
    @SerializedName("access_token")
    private String accessToken = "";
    @SerializedName("refresh_token")
    private String refreshToken = "";
    @SerializedName("expires_in")
    private Integer expiresIn;
    @SerializedName("id_token")
    private String idToken = "";
    @SerializedName("token_type")
    private String tokenType = "";

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
