/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.speedporthybrid.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.codec.DecoderException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.speedporthybrid.internal.SpeedportHybridConfiguration;
import org.openhab.binding.speedporthybrid.internal.model.AuthParameters;
import org.openhab.binding.speedporthybrid.internal.model.JsonModel;
import org.openhab.binding.speedporthybrid.internal.model.JsonModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handle all communication with the SpeedportHybrid router.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public class SpeedportHybridClient {

    private final Logger logger = LoggerFactory.getLogger(SpeedportHybridClient.class);

    private static final int CHALLANGEV_LENGTH = 64;
    private static final String CHALLANGEV = "challenge = \"";
    private static final String CSRF_TOKEN = "csrf_token";
    private static final String NULLTOKEN = "nulltoken";

    private final HandlerCallback callback;

    private final HttpClient http;

    @Nullable
    private String host;

    @Nullable
    private String password;

    private Gson gson = new Gson();

    SpeedportHybridClient(HandlerCallback callback, HttpClient http) {
        this.callback = callback;
        this.http = http;
    }

    void setConfig(SpeedportHybridConfiguration config) {
        this.host = config.host;
        this.password = config.password;
    }

    @Nullable
    JsonModelList getLoginModel(AuthParameters authParameters) {
        login(authParameters);
        String login = request("/data/Login.json", authParameters);

        if (login == null || login.isEmpty()) {
            return null;
        }

        return gson.fromJson(fixContent(login), JsonModelList.class);
    }

    @Nullable
    JsonModelList setModule(String data, AuthParameters authParameters) {
        JsonModelList models = requestEncrypted("/data/Modules.json", data, authParameters);

        authParameters.updateCSRFToken(NULLTOKEN);

        if (models != null) {
            JsonModel csrfTokenModel = models.getModel(CSRF_TOKEN);
            if (csrfTokenModel != null) {
                String varvalue = csrfTokenModel.varvalue;
                authParameters.updateCSRFToken(varvalue != null ? varvalue : NULLTOKEN);
            }
        }

        return models;
    }

    void login(AuthParameters authParameters) {
        authParameters.reset();
        String challengev = refreshChallengev();
        if (challengev != null) {
            authParameters.updateChallengev(challengev, password);
            if (doLogin(authParameters.getAuthData())) {
                refreshCSRFToken(authParameters);
            } else {
                logger.debug("No login at '{}', challangev: '{}'", host, authParameters.getChallengev());
            }
        }
    }

    private boolean doLogin(String authData) {
        String url = "http://" + host + "/data/Login.json";
        Request request = http.POST(url);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.content(new StringContentProvider(authData));

        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.debug("Unable to connect to router at '{}': {}", host, e.getLocalizedMessage());
            callback.updateStatus(OFFLINE, COMMUNICATION_ERROR, "Unbale to connect to router at '" + host + "'.");
            return false;
        }

        JsonModelList models = gson.fromJson(fixContent(response.getContentAsString()), JsonModelList.class);
        JsonModel login = models.getModel("login");

        if (login != null && login.hasValue("success")) {
            logger.trace("Successful login at '{}'.", host);
            callback.updateStatus(ONLINE, NONE, null);
            return true;
        } else {
            logger.debug("Login failed at '{}'.", host);
            callback.updateStatus(OFFLINE, CONFIGURATION_ERROR, "Invalid credentials for router at '" + host + "'.");
            return false;
        }
    }

    private void refreshCSRFToken(AuthParameters authParameters) {
        String overview = request("/html/content/overview/index.html", authParameters);

        if (overview == null || overview.isEmpty()) {
            return;
        }

        String beginPattern = CSRF_TOKEN + " = \"";
        String endPattern = "\";";
        int beginIndex = overview.indexOf(beginPattern);
        int endIndex = overview.indexOf(endPattern);
        authParameters.updateCSRFToken(overview.substring(beginIndex + beginPattern.length(), endIndex));
    }

    private @Nullable String refreshChallengev() {
        String content = request("", null);

        if (content == null || content.isEmpty()) {
            return null;
        }

        if (content.indexOf(CHALLANGEV) > 0) {
            int beginIndex = content.indexOf(CHALLANGEV) + CHALLANGEV.length();
            String challengev = content.substring(beginIndex, beginIndex + CHALLANGEV_LENGTH);
            logger.trace("Extracted challengev '{}' from router at {}. ", challengev, host);

            return challengev;
        } else {
            logger.trace("Challengev not extracted from router at {}. ", host);
            return null;
        }
    }

    @Nullable
    private String request(String path, @Nullable AuthParameters authParameters) {
        String finalURL = "http://" + host + path;

        if (authParameters != null) {
            finalURL += authParameters.getAuthData();
        }
        ContentResponse response;
        try {
            response = http.GET(finalURL);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            callback.updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
            return null;
        }

        if (response.getStatus() != HttpStatus.OK_200) {
            logger.debug("Failed to connect to router at '{}', got status '{}' with message '{}'.", host,
                    response.getStatus(), response.getContentAsString());
            callback.updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "Unable to retrieve URL '" + path + "' from router at '" + host + "'.");
            return null;
        }

        return response.getContentAsString();
    }

    private @Nullable JsonModelList requestEncrypted(String path, String data, AuthParameters authParameters) {
        byte[] encrypted;
        try {
            String fullData = CSRF_TOKEN + "=" + authParameters.getCSRFToken() + "&" + data;
            encrypted = CryptoUtils.INSTANCE.encrypt(authParameters.getChallengev(), authParameters.getDerivedKey(),
                    fullData);
        } catch (IllegalStateException | InvalidCipherTextException | DecoderException e) {
            logger.debug("Failed to encrypt request for router at '{}'.", host);
            return null;
        }

        Request request = http.POST("http://" + host + path);
        request.header(HttpHeader.CONTENT_TYPE, "application/x-www-form-urlencoded");
        request.content(new BytesContentProvider(encrypted));

        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            callback.updateStatus(OFFLINE, COMMUNICATION_ERROR, "Unbale to connect to router at '" + host + "'.");
            return null;
        }

        return gson.fromJson(fixContent(response.getContentAsString()), JsonModelList.class);
    }

    private String fixContent(String content) {
        return "{jsonModels:" + content + "}";
    }
}
