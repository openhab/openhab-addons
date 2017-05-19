package org.openhab.binding.evohome.internal.api.models;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {

    @SerializedName("access_token")
    public String AccessToken;

    @SerializedName("token_type")
    public String TokenType;

    @SerializedName("expires_in")
    public int ExpiresIn;

    @SerializedName("refresh_token")
    public String RefreshToken;

    @SerializedName("scope")
    public String Scope;

}
