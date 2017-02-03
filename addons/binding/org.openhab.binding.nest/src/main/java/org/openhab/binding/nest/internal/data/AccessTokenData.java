package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

public class AccessTokenData {
    public String getAccessToken() {
        return accessToken;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("expires_in")
    private Long expiresIn;
}
