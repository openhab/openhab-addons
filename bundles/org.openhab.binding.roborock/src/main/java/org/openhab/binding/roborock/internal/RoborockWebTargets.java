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
package org.openhab.binding.roborock.internal;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Handles performing the actual HTTP requests for communicating with the RoborockAPI.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class RoborockWebTargets {
    private static final int TIMEOUT_MS = 30000;
    private static final String EU_IOT_BASE_URL = "https://euiot.roborock.com";
    private static final String getUrlByEmailUri = EU_IOT_BASE_URL + "/api/v1/getUrlByEmail";
    private static final String getTokenPath = "/api/v1/login";
    private static final String getHomeDetailPath = "/api/v1/getHomeDetail";
    private static final String getHomeDatapath = "/user/homes/";
    private static final String getRoutinePath = "/user/scene/device/";
    private static final String setRoutinePath = "/user/scene/";
    private static final String setRoutinePathSuffix = "/execute";

    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(RoborockWebTargets.class);
    private HttpClient httpClient;
    private final SecureRandom secureRandom = new SecureRandom();
    private String safeToken = "";

    public RoborockWebTargets(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Generates a safe token for the client, used in the 'header_clientid'.
     * This token is based on a random UUID and MD5 hash of email and a portion of the UUID.
     *
     * @param email The user's email address.
     * @return The generated safe token.
     * @throws NoSuchAlgorithmException If MD5 algorithm is not available.
     */
    private String generateSafeToken(String email) throws NoSuchAlgorithmException {
        byte[] randomBytes = new byte[20];
        secureRandom.nextBytes(randomBytes);
        Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
        String token = urlEncoder.encodeToString(randomBytes);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(email.getBytes(StandardCharsets.UTF_8));
        md.update(token.substring(0, 16).getBytes(StandardCharsets.UTF_8));

        byte[] rawData = md.digest();
        return Base64.getEncoder().encodeToString(rawData);
    }

    /**
     * Generates a Hawk Authentication header string.
     *
     * @param id The ID (from Rriot.u).
     * @param secret The secret (from Rriot.s).
     * @param key The key (from Rriot.h).
     * @param path The request path.
     * @return The Hawk Authorization header string.
     * @throws NoSuchAlgorithmException If HmacSHA256 algorithm is not available.
     * @throws InvalidKeyException If the provided key is invalid.
     */
    private String getHawkAuthentication(String id, String secret, String key, String path)
            throws NoSuchAlgorithmException, InvalidKeyException {
        int timestamp = (int) Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString().substring(0, 8);
        String prestr = id + ":" + secret + ":" + nonce + ":" + timestamp + ":" + ProtocolUtils.md5Hex(path) + "::";

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);

        byte[] macBytes = mac.doFinal(prestr.getBytes(StandardCharsets.UTF_8));
        String macString = Base64.getEncoder().encodeToString(macBytes);

        return "Hawk id=\"" + id + "\",s=\"" + secret + "\",ts=\"" + timestamp + "\",nonce=\"" + nonce + "\",mac=\""
                + macString + "\"";
    }

    /**
     * Retrieves the base URL for API communication based on the user's email.
     *
     * @param email The user's email.
     * @return The base URL for the Roborock API.
     * @throws RoborockCommunicationException If communication fails.
     * @throws RoborockAuthenticationException If authentication fails.
     * @throws NoSuchAlgorithmException If MD5 algorithm is not available for safe token generation.
     */
    public String getUrlByEmail(String email)
            throws RoborockCommunicationException, RoborockAuthenticationException, NoSuchAlgorithmException {
        safeToken = generateSafeToken(email);

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String payload = "?email=" + encodedEmail + "&needtwostepauth=false";
        String response = invoke(getUrlByEmailUri + payload, HttpMethod.POST, null, null);

        try {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonElement dataElement = jsonResponse.get("data");
            if (dataElement != null && dataElement.isJsonObject()) {
                JsonElement urlElement = dataElement.getAsJsonObject().get("url");
                if (urlElement != null && urlElement.isJsonPrimitive()) {
                    return urlElement.getAsString();
                }
            }
            logger.warn("URL not found in getUrlByEmail response: {}", response);
            return EU_IOT_BASE_URL; // Default to EU_IOT_BASE_URL if URL not explicitly found
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.error("Failed to parse JSON response for getUrlByEmail: {}", response, e);
            throw new RoborockCommunicationException("Failed to parse getUrlByEmail response", e);
        }
    }

    /**
     * Performs a login operation to obtain authentication tokens.
     *
     * @param baseUri The base URI for API calls.
     * @param email The user's email.
     * @param password The user's password.
     * @return A {@link Login} object containing authentication details, or null if login fails.
     * @throws RoborockCommunicationException If communication fails.
     * @throws RoborockAuthenticationException If authentication fails.
     * @throws NoSuchAlgorithmException If MD5 algorithm is not available for safe token generation (though safeToken is
     *             done prior).
     */
    @Nullable
    public Login doLogin(String baseUri, String email, String password)
            throws RoborockCommunicationException, RoborockAuthenticationException, NoSuchAlgorithmException {
        if (safeToken.isEmpty()) {
            logger.warn(
                    "Safe token is empty during doLogin. This might indicate getUrlByEmail was not called or failed.");
            safeToken = generateSafeToken(email); // Generate if somehow missed
        }

        String encodedUsername = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
        String payload = "?username=" + encodedUsername + "&password=" + encodedPassword + "&needtwostepauth=false";

        String response = invoke(baseUri + getTokenPath + payload, HttpMethod.POST, null, null);
        return gson.fromJson(response, Login.class);
    }

    /**
     * Retrieves home detail information.
     *
     * @param baseUri The base URI for API calls.
     * @param token The authentication token.
     * @param rriot The Rriot object containing Hawk authentication details.
     * @return A {@link Home} object containing home details, or null if retrieval fails.
     * @throws RoborockCommunicationException If communication fails.
     * @throws RoborockAuthenticationException If authentication fails.
     */
    @Nullable
    public Home getHomeDetail(String baseUri, String token)
            throws RoborockCommunicationException, RoborockAuthenticationException {
        String response = invoke(baseUri + getHomeDetailPath, HttpMethod.GET, "Authorization", token);
        return gson.fromJson(response, Home.class);
    }

    /**
     * Retrieves home data for a specific home ID.
     *
     * @param rrHomeID The Roborock Home ID.
     * @param rriot The Rriot object containing Hawk authentication details.
     * @return A {@link HomeData} object containing home data, or null if retrieval fails.
     * @throws RoborockCommunicationException If communication fails.
     * @throws RoborockAuthenticationException If authentication fails.
     * @throws NoSuchAlgorithmException If cryptographic algorithm is not available.
     * @throws InvalidKeyException If the provided key for Hawk authentication is invalid.
     */
    @Nullable
    public HomeData getHomeData(String rrHomeID, Rriot rriot) throws RoborockCommunicationException,
            RoborockAuthenticationException, NoSuchAlgorithmException, InvalidKeyException {
        String path = getHomeDatapath + rrHomeID;
        String token = getHawkAuthentication(rriot.u, rriot.s, rriot.h, path);
        String response = invoke(rriot.r.a + path, HttpMethod.GET, "Authorization", token);
        return gson.fromJson(response, HomeData.class);
    }

    /**
     * Retrieves routines for a specific device.
     *
     * @param deviceID The device ID.
     * @param rriot The Rriot object containing Hawk authentication details.
     * @return The raw JSON response string containing routines, or null if retrieval fails.
     * @throws RoborockCommunicationException If communication fails.
     * @throws RoborockAuthenticationException If authentication fails.
     * @throws NoSuchAlgorithmException If cryptographic algorithm is not available.
     * @throws InvalidKeyException If the provided key for Hawk authentication is invalid.
     */
    @Nullable
    public String getRoutines(String deviceID, Rriot rriot) throws RoborockCommunicationException,
            RoborockAuthenticationException, NoSuchAlgorithmException, InvalidKeyException {
        String path = getRoutinePath + deviceID;
        String hawkToken = getHawkAuthentication(rriot.u, rriot.s, rriot.h, path);
        return invoke(rriot.r.a + path, HttpMethod.GET, "Authorization", hawkToken);
    }

    /**
     * Executes a specific routine (scene).
     *
     * @param sceneID The ID of the scene (routine) to execute.
     * @param rriot The Rriot object containing Hawk authentication details.
     * @return The raw JSON response string, or null if execution fails.
     * @throws RoborockCommunicationException If communication fails.
     * @throws RoborockAuthenticationException If authentication fails.
     * @throws NoSuchAlgorithmException If cryptographic algorithm is not available.
     * @throws InvalidKeyException If the provided key for Hawk authentication is invalid.
     */
    @Nullable
    public String setRoutine(String sceneID, Rriot rriot) throws RoborockCommunicationException,
            RoborockAuthenticationException, NoSuchAlgorithmException, InvalidKeyException {
        String path = setRoutinePath + sceneID + setRoutinePathSuffix;
        String hawkToken = getHawkAuthentication(rriot.u, rriot.s, rriot.h, path);
        return invoke(rriot.r.a + path, HttpMethod.POST, "Authorization", hawkToken);
    }

    /**
     * Performs an HTTP request using the configured HttpClient.
     *
     * @param uri The full URI to call.
     * @param method The HTTP method (GET, POST, etc.).
     * @param headerKey Optional: The name of an additional header.
     * @param headerValue Optional: The value of an additional header.
     * @return The response body as a String.
     * @throws RoborockCommunicationException If there's a communication error (timeout, execution error, non-success
     *             HTTP status).
     * @throws RoborockAuthenticationException If the HTTP status is 401 Unauthorized.
     */
    private String invoke(String uri, HttpMethod method, @Nullable String headerKey, @Nullable String headerValue)
            throws RoborockCommunicationException, RoborockAuthenticationException {
        logger.debug("Calling url: {}", uri);
        String jsonResponse = "";

        synchronized (this) {
            try {
                Request request = httpClient.newRequest(uri).method(method).header("content-type", "application/json")
                        .header("header_clientid", safeToken).timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (headerKey != null && headerValue != null) {
                    request.header(headerKey, headerValue);
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("{} request for {}", method, uri);
                }

                ContentResponse response = request.send();
                int status = response.getStatus();
                jsonResponse = response.getContentAsString();

                if (!jsonResponse.isEmpty()) {
                    logger.trace("JSON response: '{}'", jsonResponse);
                }

                if (status == HttpStatus.UNAUTHORIZED_401) {
                    throw new RoborockAuthenticationException("Unauthorized");
                }
                if (!HttpStatus.isSuccess(status)) {
                    throw new RoborockCommunicationException(
                            String.format("Roborock returned error <%d> while invoking %s", status, uri));
                }
            } catch (TimeoutException ex) {
                throw new RoborockCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            } catch (ExecutionException ex) {
                throw new RoborockCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            } catch (InterruptedException ex) {
                throw new RoborockCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            }
        }

        return jsonResponse;
    }
}
