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
public class BaseRequest {

    @SerializedName("appkey")
    private String appKey;

    @SerializedName("token")
    private String token;

    @SerializedName("lang")
    private Language language;

    @SerializedName("api_key_param")
    private ApiKeyParameter apiKey;

    public BaseRequest() {
        setLanguage(Language.ENGLISH);
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
