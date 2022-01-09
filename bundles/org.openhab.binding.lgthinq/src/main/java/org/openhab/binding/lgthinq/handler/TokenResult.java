package org.openhab.binding.lgthinq.handler;

import java.util.Date;

public class TokenResult {
    /**
     * "access_token" -> "8f3a2c5ac198e97be32341498c2ca1ec81d6559ce6e92a50fd19d8189c9ad4f4ac18ab6aef9fe0b7926b630351ba1573"
     * "refresh_token" -> "cab8233536317dc82ef35f1772d83ce6d48c4a660a319e0687aaf528ba947e5d6a31e2bd544c556e659ce331a7256560"
     * "expires_in" -> "3600"
     * "status" -> {Integer@5127} 1
     * "oauth2_backend_url" -> "https://us.lgeapi.com/"
     */
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private Date generatedTime;
    private String ouathBackendUrl;

    public TokenResult(String accessToken, String refreshToken, int expiresIn, Date generatedTime, String ouathBackendUrl) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.generatedTime = generatedTime;
        this.ouathBackendUrl = ouathBackendUrl;
    }

    public TokenResult(){};

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public Date getGeneratedTime() {
        return generatedTime;
    }

    public String getOuathBackendUrl() {
        return ouathBackendUrl;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public void setGeneratedTime(Date generatedTime) {
        this.generatedTime = generatedTime;
    }

    public void setOuathBackendUrl(String ouathBackendUrl) {
        this.ouathBackendUrl = ouathBackendUrl;
    }
}
