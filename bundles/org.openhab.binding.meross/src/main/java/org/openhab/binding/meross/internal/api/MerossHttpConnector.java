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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.dto.CloudCredentials;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.exception.MerossApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossHttpConnector} class is responsible for handling the Http functionality for connecting to the Meross
 * Host
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Refactor, limiting connections
 */
@NonNullByDefault
public class MerossHttpConnector {
    private final Logger logger = LoggerFactory.getLogger(MerossHttpConnector.class);
    private static final String INITIAL_STRING = "23x17ahWarFH6w29";
    private static final long CONNECTION_TIMEOUT_SECONDS = 15;
    private final String apiBaseUrl;
    private final String userEmail;
    private final String userPassword;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(CONNECTION_TIMEOUT_SECONDS, ChronoUnit.SECONDS)).build();

    private @Nullable String token;
    private int loginStatusCode;
    private @Nullable Integer apiStatus;
    private @Nullable CloudCredentials credentials;

    private static final Gson gson = new Gson();
    private static final Type deviceListType = new TypeToken<List<Device>>() {
    }.getType();

    public MerossHttpConnector(String apiBaseUrl, String userEmail, String password) {
        this.apiBaseUrl = apiBaseUrl;
        this.userEmail = userEmail;
        this.userPassword = password;
    }

    /**
     * @param paramsData The params
     * @param uri The uri
     * @param path The path (endpoint)
     * @return The http response
     * @throws IOException if it fails to return the http response
     */
    private HttpResponse<String> postResponse(Map<String, String> paramsData, String uri, String path)
            throws IOException {
        String dataToSign;
        String encodedParams;
        String authorizationValue;
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
        String payload = new Gson().toJson(payloadMap);
        if (token != null) {
            authorizationValue = "Basic %s".formatted(token);
        } else {
            authorizationValue = "Basic";
        }
        HttpRequest postRequest = HttpRequest.newBuilder().uri(URI.create(uri + path))
                .header("Authorization", authorizationValue).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload)).build();
        try {
            return client.sendAsync(postRequest, HttpResponse.BodyHandlers.ofString()).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException("Error while posting data", e);
        }
    }

    private static String encodeParams(Map<String, String> paramsData) {
        return Base64.getEncoder().encodeToString(new Gson().toJson(paramsData).getBytes());
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
            HttpResponse<String> response = postResponse(loginMap, apiBaseUrl, MerossEnum.HttpEndpoint.LOGIN.value());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            apiStatus = jsonElement.getAsJsonObject().get("apiStatus").getAsInt();
            CloudCredentials credentials = this.credentials = gson.fromJson(jsonElement.getAsJsonObject().get("data"),
                    CloudCredentials.class);
            loginStatusCode = response.statusCode();
            token = credentials != null ? credentials.token() : null;
        } catch (IOException e) {
            token = null;
            throw new ConnectException("Error while logging in");
        }
    }

    public void logout() {
        try {
            if (token != null) {
                postResponse(Collections.emptyMap(), apiBaseUrl, MerossEnum.HttpEndpoint.LOGOUT.value());
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
            HttpResponse<String> response = postResponse(Collections.emptyMap(), apiBaseUrl,
                    MerossEnum.HttpEndpoint.DEV_LIST.value());
            JsonElement jsonElement = JsonParser.parseString(response.body());
            List<Device> devices = gson.fromJson(jsonElement.getAsJsonObject().get("data"), deviceListType);
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
