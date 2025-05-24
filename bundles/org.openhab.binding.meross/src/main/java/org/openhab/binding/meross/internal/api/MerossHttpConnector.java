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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.dto.CloudCredentials;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.exception.MerossApiException;
import org.openhab.binding.meross.internal.handler.MerossBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link MerossHttpConnector} class is responsible for handling the Http functionality for connecting to the Meross
 * Cloud
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossHttpConnector {
    private final Logger logger = LoggerFactory.getLogger(MerossHttpConnector.class);
    private static final String INITIAL_STRING = "23x17ahWarFH6w29";
    private static final String DEFAULT_APP_TYPE = "MerossIOT";
    private static final String MODULE_VERSION = "0.0.0";
    private static final long CONNECTION_TIMEOUT_SECONDS = 15;
    private @Nullable String token;
    private final String apiBaseUrl;
    private final String userEmail;
    private final String userPassword;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.of(CONNECTION_TIMEOUT_SECONDS, ChronoUnit.SECONDS)).build();
    private final File credentialFile;
    private final File deviceFile;

    public MerossHttpConnector(String apiBaseUrl, String userEmail, String password, File credentialFile,
            File deviceFile) {
        this.apiBaseUrl = apiBaseUrl;
        this.userEmail = userEmail;
        this.userPassword = password;
        this.credentialFile = credentialFile;
        this.deviceFile = deviceFile;
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
                .header("Authorization", authorizationValue).header("AppVersion", "0.0.0").header("vender", "meross")
                .header("AppType", DEFAULT_APP_TYPE).header("AppLanguage", "EN")
                .header("User-Agent", DEFAULT_APP_TYPE + "/" + MODULE_VERSION)
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build();
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
    public HttpResponse<String> login() throws ConnectException {
        Map<String, String> loginMap = Map.of("email", userEmail, "password", userPassword);
        try {
            return postResponse(loginMap, apiBaseUrl, MerossEnum.HttpEndpoint.LOGIN.value());
        } catch (IOException e) {
            throw new ConnectException("Error while logging in");
        }
    }

    /**
     * @return The apiStatus value. The default return value has been set to -1 as the OK(0) value is a
     *         significant value for the API
     */
    public int apiStatus() {
        try {
            return JsonParser.parseString(login().body()).getAsJsonObject().get("apiStatus").getAsInt();
        } catch (IOException e) {
            logger.debug("Couldn't get apiStatus");
        }
        return -1;
    }

    /**
     * @param devName The device name
     * @return The device UUID
     * @throws IOException if the device UUID cannot be retrieved
     */
    public String getDevUUIDByDevName(String devName) throws IOException {
        @Nullable
        ArrayList<Device> devices = readDevices();
        if (devices != null) {
            Optional<String> uuid = devices.stream().filter(device -> device.devName().equals(devName))
                    .map(Device::uuid).findFirst();
            if (uuid.isPresent()) {
                return uuid.get();
            }
        }
        return "";
    }

    private void setToken(String token) {
        this.token = token;
    }

    public void logout() {
        try {
            postResponse(Collections.emptyMap(), apiBaseUrl, MerossEnum.HttpEndpoint.LOGOUT.value());
        } catch (IOException e) {
            logger.debug("Cannot log out", e);
        }
    }

    /**
     * @return The credentials
     * @throws IOException if credentials could not be retrieved
     */
    public String fetchCredentials() throws IOException {
        JsonElement jsonElement = JsonParser.parseString(login().body());
        return jsonElement.getAsJsonObject().get("data").toString();
    }

    /**
     * @return The devices
     * @throws IOException if devices could not be retrieved
     */
    public String fetchDevices() throws IOException {
        CloudCredentials credentials = new Gson().fromJson(fetchCredentials(), CloudCredentials.class);
        String token = credentials.token();
        setToken(token);
        Map<String, String> emptyMap = Collections.emptyMap();
        HttpResponse<String> response = postResponse(emptyMap, apiBaseUrl, MerossEnum.HttpEndpoint.DEV_LIST.value());
        JsonElement jsonElement = JsonParser.parseString(response.body());
        return jsonElement.getAsJsonObject().get("data").toString();
    }

    /**
     * @param credentialFile The credentials' file to be written once credentials are fetched from cloud
     */
    public void fetchCredentialsAndWrite(File credentialFile) {
        String json = null;
        try {
            json = fetchCredentials();
        } catch (IOException e) {
            logger.debug("IOException while fetching credentials", e);
        }
        if (json != null) {
            writeFile(json, credentialFile);
        }
    }

    /**
     * @param deviceFile The device file to be written once devices are fetched from cloud
     */
    public void fetchDevicesAndWrite(File deviceFile) {
        String json = null;
        try {
            json = fetchDevices();
        } catch (IOException e) {
            logger.debug("IOException while fetching devices {}", e.getMessage());
        }
        if (json != null) {
            writeFile(json, deviceFile);
        }
    }

    /**
     * @return The user's credentials
     * @throws IOException if it fails to read the file
     */

    public @Nullable CloudCredentials readCredentials() throws IOException {
        File file = new File(String.valueOf(credentialFile));
        try {
            return new Gson().fromJson(readFile(file), CloudCredentials.class);
        } catch (JsonSyntaxException e) {
            throw new IOException();
        }
    }

    /**
     * @return The user's devices
     * @throws IOException if it fails to read the file
     */
    public @Nullable ArrayList<Device> readDevices() throws IOException {
        TypeToken<ArrayList<Device>> type = new TypeToken<>() {
        };
        File file = new File(String.valueOf(deviceFile));
        try {
            return new Gson().fromJson(readFile(file), type);
        } catch (JsonSyntaxException e) {
            throw new IOException();

        }
    }

    private String readFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    private void writeFile(String content, File file) {
        file.getParentFile().mkdirs();
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            logger.error("Couldn't create file '{}'.", file.getPath(), e);
        } catch (IOException e) {
            logger.error("Couldn't write to file '{}'.", file.getPath(), e);
        }
    }

    public void fetchDataAsync() throws ConnectException, MerossApiException {
        int httpStatusCode = login().statusCode();
        int apiStatusCode = apiStatus();
        String apiMessage = MerossEnum.ApiStatusCode.getMessageByApiStatusCode(apiStatusCode);
        if (httpStatusCode != 200) {
            throw new ConnectException();
        } else if (apiStatusCode != MerossEnum.ApiStatusCode.OK.value()) {
            if (apiMessage != null) {
                throw new MerossApiException(apiMessage);
            }
        } else {
            CompletableFuture.runAsync(() -> fetchCredentialsAndWrite(MerossBridgeHandler.credentialfile))
                    .thenRunAsync(() -> fetchDevicesAndWrite(MerossBridgeHandler.deviceFile)).exceptionally(e -> {
                        logger.debug("Cannot fetch data {}", e.getMessage());
                        return null;
                    }).join();
        }
    }
}
