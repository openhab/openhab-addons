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
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.meross.internal.dto.CloudCredentials;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.exception.MerossApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossCloudHttpConnector} class is responsible for handling the Http functionality for connecting to the
 * Meross Cloud. This is required for discovery and setting up a cloud MQTT connection to get updates.
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Refactored, limiting connections, using common http client
 */
@NonNullByDefault
public class MerossCloudHttpConnector extends MerossHttpConnector {
    private final Logger logger = LoggerFactory.getLogger(MerossCloudHttpConnector.class);
    private static final String INITIAL_STRING = "23x17ahWarFH6w29";
    private final String userEmail;
    private final String userPassword;

    private @Nullable String token;
    private int loginStatusCode;
    private @Nullable Integer apiStatus;
    private @Nullable CloudCredentials credentials;

    private static final Type DEVICE_LIST_TYPE = new TypeToken<List<Device>>() {
    }.getType();

    public MerossCloudHttpConnector(@Nullable HttpClient httpClient, String apiBaseUrl, String userEmail,
            String password) {
        super(httpClient, apiBaseUrl);
        this.userEmail = userEmail;
        this.userPassword = password;
    }

    /**
     * @param content
     * @param uri The uri
     * @param path The path (endpoint)
     * @return The http response
     * @throws IOException if it fails to return the http response
     */
    @Override
    protected ContentResponse postResponse(String content, String uri, String path) throws IOException {
        HttpClient httpClient = this.httpClient;
        if (httpClient == null) {
            throw new IOException("Internal error: http client not set");
        }
        String authorizationValue;
        if (token != null) {
            authorizationValue = "Basic %s".formatted(token);
        } else {
            authorizationValue = "Basic";
        }
        Request request = httpClient.newRequest(URI.create(uri + path)).method(HttpMethod.POST)
                .header("Authorization", authorizationValue)
                .content(new StringContentProvider(content), "application/json")
                .timeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        try {
            return request.send();
        } catch (TimeoutException e) {
            throw new IOException("Timeout while posting data", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Error while posting data", e);
        }
    }

    private String createContent(Map<String, String> paramsData) {
        String dataToSign;
        String encodedParams;
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        long timestamp = Instant.now().toEpochMilli();
        encodedParams = encodeParams(paramsData);
        dataToSign = "%s%d%s%s".formatted(INITIAL_STRING, timestamp, nonce, encodedParams);
        String md5hash = MD5Util.getMD5String(dataToSign);
        Map<String, String> payloadMap = new HashMap<>();
        payloadMap.put("params", encodedParams);
        payloadMap.put("sign", md5hash);
        payloadMap.put("timestamp", String.valueOf(timestamp));
        payloadMap.put("nonce", nonce);
        return GSON.toJson(payloadMap);
    }

    /**
     * @return The http response to login request
     * @throws ConnectException throws exception if a login fail
     */
    public void login() throws ConnectException {
        if (token != null) {
            logout();
        }
        Map<String, String> loginMap = Map.of("email", userEmail, "password", userPassword);
        try {
            ContentResponse response = postResponse(createContent(loginMap), apiBaseUrl,
                    MerossEnum.HttpEndpoint.LOGIN.value());
            JsonElement jsonElement = JsonParser.parseString(response.getContentAsString());
            apiStatus = jsonElement.getAsJsonObject().get("apiStatus").getAsInt();
            CloudCredentials credentials = this.credentials = GSON.fromJson(jsonElement.getAsJsonObject().get("data"),
                    CloudCredentials.class);
            loginStatusCode = response.getStatus();
            token = credentials != null ? credentials.token() : null;
        } catch (IOException e) {
            token = null;
            throw new ConnectException("Error while logging in");
        }
    }

    public void logout() {
        try {
            if (token != null) {
                postResponse(createContent(Map.of()), apiBaseUrl, MerossEnum.HttpEndpoint.LOGOUT.value());
            }
        } catch (IOException e) {
            logger.debug("Cannot log out", e);
        } finally {
            token = null;
        }
    }

    /**
     * Get the credentials retrieved from the last connection. If not connected before, login to retrieve and logout.
     *
     * @return The credentials
     * @throws ConnectException if credentials could not be retrieved
     */
    public @Nullable CloudCredentials getCredentials() throws ConnectException {
        if (credentials == null) {
            try {
                login();
            } catch (ConnectException e) {
                throw e;
            } finally {
                logout();
            }
        }
        return credentials;
    }

    /**
     * Fetch the devices from the Meross cloud account. If already logged in, will not login again. If not logged in,
     * will login and logout at end of fetch.
     *
     * @return The devices
     * @throws IOException if devices could not be retrieved
     */
    public List<Device> getDevices() throws ConnectException {
        boolean loggedIn = token != null;
        try {
            if (!loggedIn) {
                login();
            }
            ContentResponse response = postResponse(createContent(Map.of()), apiBaseUrl,
                    MerossEnum.HttpEndpoint.DEV_LIST.value());
            String devicesResponse = response.getContentAsString();
            logger.trace("Get devices response: {}", devicesResponse);
            JsonElement jsonElement = JsonParser.parseString(devicesResponse);
            JsonElement data = jsonElement.getAsJsonObject().get("data");
            List<Device> devices = null;
            if (data.isJsonArray()) {
                devices = GSON.fromJson(data, DEVICE_LIST_TYPE);
            }
            return devices != null ? devices : List.of();
        } catch (IOException e) {
            throw new ConnectException();
        } finally {
            if (!loggedIn) {
                logout();
            }
        }
    }

    /**
     * Check the api status. If already logged in, will not login again. If not logged in, will login and logout after
     * retrieving status.
     *
     * @return The apiStatus value. The default return value has been set to -1 as the OK(0) value is a
     *         significant value for the API
     */
    private int getApiStatus() {
        boolean loggedIn = token != null;
        if (!loggedIn) {
            try {
                login();
            } catch (ConnectException e) {
                logger.debug("Couldn't get apiStatus");
                return -1;
            } finally {
                logout();
            }
        }
        Integer apiStatus = this.apiStatus;
        return apiStatus != null ? apiStatus : -1;
    }

    public void checkApiStatus() throws ConnectException, MerossApiException {
        int apiStatusCode = getApiStatus();
        String apiMessage = MerossEnum.ApiStatusCode.getMessageByApiStatusCode(apiStatusCode);
        if (loginStatusCode != 200) {
            throw new ConnectException();
        } else if (apiStatusCode != MerossEnum.ApiStatusCode.OK.value()) {
            if (apiMessage != null) {
                throw new MerossApiException(apiMessage);
            }
        }
    }
}
