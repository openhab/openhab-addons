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
package org.openhab.binding.sungrow.internal.client.dto;

import com.google.gson.annotations.SerializedName;

/**
 * @author Christian Kemper - Initial contribution
 */
public class Login {

    @SerializedName("user_account")
    private String username;
    @SerializedName("user_password")
    private String password;
    @SerializedName("appkey")
    private String appKey;
    @SerializedName("lang")
    private Language language;
    @SerializedName("api_key_param")
    private ApiKeyParameter apiKey;

    public Login(String username, String password, String appKey) {
        this.username = username;
        this.password = password;
        this.appKey = appKey;
        this.language = Language.ENGLISH;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public ApiKeyParameter getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKeyParameter apiKey) {
        this.apiKey = apiKey;
    }
}
