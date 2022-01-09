package org.openhab.binding.lgthinq.handler;

import org.openhab.binding.lgthinq.api.Gateway;

public class AuthenticatedSession {
    private Gateway gateway;
    private String refreshToken;
    private String oauthUrl;
    private String accessToken;
    private String userNumber;

    public AuthenticatedSession(Gateway gateway, String refreshToken, String oauthUrl, String accessToken, String userNumber) {
        this.gateway = gateway;
        this.refreshToken = refreshToken;
        this.oauthUrl = oauthUrl;
        this.accessToken = accessToken;
        this.userNumber = userNumber;
    }

    public Gateway getGateway() {
        return gateway;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getOauthUrl() {
        return oauthUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getUserNumber() {
        return userNumber;
    }
}


