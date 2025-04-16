/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lgthinq.lgservices.api;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TokenResult} Hold information about token and related entities
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class TokenResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 202409261447L;
    private String accessToken = "";
    private String refreshToken = "";
    private int expiresIn;
    private Date generatedTime = new Date();
    private String oauthBackendUrl = "";
    private UserInfo userInfo = new UserInfo();
    private LGThinqGateway gatewayInfo = new LGThinqGateway();

    public TokenResult(String accessToken, String refreshToken, int expiresIn, Date generatedTime,
            String ouathBackendUrl) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.generatedTime = generatedTime;
        this.oauthBackendUrl = ouathBackendUrl;
    }

    // This constructor will never be called by this. It only exists because of ObjectMapper instantiation needs
    public TokenResult() {
    }

    public LGThinqGateway getGatewayInfo() {
        return gatewayInfo;
    }

    public void setGatewayInfo(LGThinqGateway gatewayInfo) {
        this.gatewayInfo = gatewayInfo;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Date getGeneratedTime() {
        return generatedTime;
    }

    public void setGeneratedTime(Date generatedTime) {
        this.generatedTime = generatedTime;
    }

    public String getOauthBackendUrl() {
        return oauthBackendUrl;
    }

    public void setOauthBackendUrl(String ouathBackendUrl) {
        this.oauthBackendUrl = ouathBackendUrl;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
