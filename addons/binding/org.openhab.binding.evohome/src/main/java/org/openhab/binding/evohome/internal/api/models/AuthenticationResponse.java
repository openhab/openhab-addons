package org.openhab.binding.evohome.internal.api.models;

import com.google.gson.annotations.SerializedName;

public class AuthenticationResponse {

    @SerializedName("access_token")
    String AccessToken;

    @SerializedName("token_type")
    String TokenType;

    @SerializedName("expires_in")
    int ExpiresIn;

    @SerializedName("refresh_token")
    String RefreshToken;

    @SerializedName("scope")
    String Scope;

}
