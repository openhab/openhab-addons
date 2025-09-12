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
package org.openhab.binding.meross.internal.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.meross.internal.api.MerossEnum.HttpConnectionType;

import com.google.gson.Gson;

/**
 * The {@link MerossHttpConnector} class is responsible for handling the Http functionality.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public abstract class MerossHttpConnector {

    protected static final long CONNECTION_TIMEOUT_SECONDS = 15;
    protected final String apiBaseUrl;

    protected @Nullable HttpClient httpClient;
    protected static final Gson GSON = new Gson();

    protected MerossHttpConnector(@Nullable HttpClient httpClient, String apiBaseUrl) {
        this.httpClient = httpClient;
        this.apiBaseUrl = apiBaseUrl;
    }

    public static class Builder {
        private @Nullable HttpClient httpClient;
        private String apiBaseUrl = "";
        private String userEmail = "";
        private String userPassword = "";

        private HttpConnectionType httpConnectionType = HttpConnectionType.CLOUD;

        public Builder httpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public Builder setHttpConnectionType(HttpConnectionType httpConnectionType) {
            this.httpConnectionType = httpConnectionType;
            return this;
        }

        public Builder setApiBaseUrl(String apiBaseUrl) {
            this.apiBaseUrl = apiBaseUrl;
            return this;
        }

        public Builder setUserEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder setUserPassword(String userPassword) {
            this.userPassword = userPassword;
            return this;
        }

        public MerossHttpConnector build() {
            if (httpConnectionType == HttpConnectionType.LOCAL) {
                return new MerossLocalHttpConnector(httpClient, apiBaseUrl);
            }
            return new MerossCloudHttpConnector(httpClient, apiBaseUrl, userEmail, userPassword);
        }
    }

    protected static String encodeParams(Map<String, String> paramsData) {
        return Base64.getEncoder().encodeToString(new Gson().toJson(paramsData).getBytes());
    }

    /**
     * @param content
     * @param uri The uri
     * @param path The path (endpoint)
     * @return The http response
     * @throws IOException if it fails to return the http response
     */
    protected abstract ContentResponse postResponse(String content, String uri, String path) throws IOException;

    public String postResponse(byte[] content) throws IOException {
        return postResponse(new String(content, StandardCharsets.UTF_8), apiBaseUrl, "").getContentAsString();
    }
}
