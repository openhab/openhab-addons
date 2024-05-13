package org.openhab.binding.onecta.internal.api.dto.authentication;

import com.google.gson.annotations.SerializedName;

public class ReqAuthenticationRoot {
    @SerializedName("ClientId")
    private String clientId;
    @SerializedName("AuthFlow")
    private String authFlow;
    @SerializedName("AuthParameters")
    private ReqAuthParameters authParameters;

    public ReqAuthenticationRoot(String clientId, String refreshToken) {
        this.clientId = clientId;
        this.authFlow = "REFRESH_TOKEN_AUTH";
        this.authParameters = new ReqAuthParameters(refreshToken);
    }

    public String getClientId() {
        return clientId;
    }

    public String getAuthFlow() {
        return authFlow;
    }

    public ReqAuthParameters getAuthParameters() {
        return authParameters;
    }
}
