package org.openhab.binding.onecta.internal.api.dto.authentication;

import com.google.gson.annotations.SerializedName;

public class RespStep7SessionInfo {
    @SerializedName("login_token")
    public String login_token;
    @SerializedName("expires_in")
    public String expires_in;

    public String getLogin_token() {
        return login_token;
    }

    public String getExpires_in() {
        return expires_in;
    }
}
