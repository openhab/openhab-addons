package org.openhab.binding.evohome.internal.api.models.v1;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("Username")
    private final String username;

    @SerializedName("Password")
    private final String password;

    @SerializedName("ApplicationId")
    private final String applicationId;

    //
    public LoginRequest(String username, String password, String applicationId) {
        this.username = username;
        this.password = password;
        this.applicationId = applicationId;
    }
}
