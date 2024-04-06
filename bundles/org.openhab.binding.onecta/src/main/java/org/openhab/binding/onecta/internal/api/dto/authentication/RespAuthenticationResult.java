package org.openhab.binding.onecta.internal.api.dto.authentication;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class RespAuthenticationResult {
    @SerializedName("AccessToken")
    private String accessToken = "";
    @SerializedName("ExpiresIn")
    private int expiresIn;
    @SerializedName("IdToken")
    private String idToken = "";
    @SerializedName("TokenType")
    private String tokenType = "";

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return !accessToken.isEmpty() ? accessToken : "";
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
