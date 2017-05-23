package org.openhab.binding.evohome.internal.api.models.v1;

public class LoginResponse {

    private String sessionId;

    private UserInfo userInfo;

    @Override
    public String toString() {
        return "sessionId[" + sessionId + "] userInfo[" + userInfo + "]";
    }

    public String getSessionId() {
        return sessionId;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }
}
