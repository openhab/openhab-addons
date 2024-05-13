package org.openhab.binding.onecta.internal.api.dto.authentication;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

@NonNullByDefault
public class RespAuthenticationRoot {
    @SerializedName("AuthenticationResult")
    private RespAuthenticationResult respAuthenticationResult = new RespAuthenticationResult();
    @SerializedName("ChallengeParameters")
    private RespChallengeParameters respChallengeParameters = new RespChallengeParameters();
    @SerializedName("__type")
    private String __type = "";
    @SerializedName("message")
    private String message = "";

    public RespAuthenticationResult getAuthenticationResult() {
        return respAuthenticationResult;
    }

    public RespChallengeParameters getChallengeParameters() {
        return respChallengeParameters;
    }

    public String get__type() {
        return __type;
    }

    public String getMessage() {
        return message;
    }
}
