/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link InnogyConfig} contains all configurations for the innogy SmartHome library.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
public class InnogyConfig {
    private String authCode = null;
    private String accessToken = null;
    private String refreshToken = null;
    private String clientId = null;
    private String clientSecret = null;
    private String redirectUrl = null;

    /**
     * Creates a new {@link InnogyConfig} and set the given clientId, clientSecret, redirectUrl and refreshToken.
     *
     * @param clientId
     * @param clientSecret
     * @param redirectUrl
     * @param refreshToken
     */
    public InnogyConfig(String clientId, String clientSecret, String redirectUrl, String refreshToken) {
        this(clientId, clientSecret, redirectUrl, null, null, refreshToken);
    }

    /**
     * Creates a new {@link InnogyConfig} and set the given clientId, clientSecret, redirectUrl, authCode, accessToken
     * and refreshToken.
     *
     * @param clientId
     * @param clientSecret
     * @param redirectUrl
     * @param authCode
     * @param accessToken
     * @param refreshToken
     */
    public InnogyConfig(String clientId, String clientSecret, String redirectUrl, String authCode, String accessToken,
            String refreshToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.authCode = authCode;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /**
     * Creates an empty {@link InnogyConfig}.
     */
    public InnogyConfig() {
    }

    /**
     * Checks, if the {@link InnogyConfig} has the necessary {@link #clientId} and {@link #clientSecret} set.
     *
     * @return true if everything seems fine or false, if one of those are missing.
     */
    public boolean checkClientData() {
        return !(StringUtils.isEmpty(clientId) || StringUtils.isEmpty(clientSecret)
                || StringUtils.isEmpty(redirectUrl));
    }

    /**
     * Checks, if the {@link InnogyConfig} includes the refresh token.
     *
     * @return
     */
    public boolean checkRefreshToken() {
        return !StringUtils.isEmpty(refreshToken);
    }

    /**
     * Checks, if the {@link InnogyConfig} includes the access token.
     *
     * @return
     */
    public boolean checkAccessToken() {
        return !StringUtils.isEmpty(accessToken);
    }

    /**
     * Checks, if the authCode is set.
     *
     * @return
     */
    public boolean checkAuthCode() {
        return !StringUtils.isBlank(authCode);
    }

    /**
     * @return the authCode
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * @param authCode the authCode to set
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @param accessToken the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return the refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * @param refreshToken the refreshToken to set
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the clientSecret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * @param clientSecret the clientSecret to set
     */
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    /**
     * @return the redirectUrl
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * @param redirectUrl the redirectUrl
     */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public String toString() {
        String simplifiedAccessToken = "";
        String simplifiedRefreshToken = "";

        if (!StringUtils.isEmpty(accessToken)) {
            simplifiedAccessToken = accessToken.substring(0, 10) + "..."
                    + accessToken.substring(accessToken.length() - 10);
        }
        if (!StringUtils.isEmpty(refreshToken)) {
            simplifiedRefreshToken = refreshToken.substring(0, 5) + "..."
                    + refreshToken.substring(refreshToken.length() - 5);
        }

        return "Config [clientId=" + clientId + ", clientSecret=" + clientSecret + ", redirectUrl=" + redirectUrl
                + ", authCode=" + authCode + ", accessToken=" + simplifiedAccessToken + ", refreshToken="
                + simplifiedRefreshToken + "]";
    }
}
