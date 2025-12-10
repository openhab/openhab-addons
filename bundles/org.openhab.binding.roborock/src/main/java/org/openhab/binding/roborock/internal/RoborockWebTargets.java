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

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

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
import org.openhab.binding.roborock.internal.api.GetUrlByEmail;
import org.openhab.binding.roborock.internal.api.Home;
import org.openhab.binding.roborock.internal.api.HomeData;
import org.openhab.binding.roborock.internal.api.Login.Rriot;
import org.openhab.binding.roborock.internal.api.SignCodeV3;
import org.openhab.binding.roborock.internal.util.ProtocolUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles performing the actual HTTP requests for communicating with the Roborock API.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class RoborockWebTargets {
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
     * @throws RoborockException If MD5 algorithm is not available.
     */
    private String generateSafeToken(String email) throws RoborockException {
        try {
            byte[] randomBytes = new byte[20];
            secureRandom.nextBytes(randomBytes);
            Encoder urlEncoder = Base64.getUrlEncoder().withoutPadding();
            String token = urlEncoder.encodeToString(randomBytes);

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(email.getBytes(StandardCharsets.UTF_8));
            md.update(token.substring(0, 16).getBytes(StandardCharsets.UTF_8));

            byte[] rawData = md.digest();
            return Base64.getEncoder().encodeToString(rawData);
        } catch (NoSuchAlgorithmException e) {
            throw new RoborockException("Decryption of received data failed", e);
        }
    }

    /**
     * Generates a Hawk Authentication header string.
     *
     * @param id The ID (from Rriot.u).
     * @param secret The secret (from Rriot.s).
     * @param key The key (from Rriot.h).
     * @param path The request path.
     * @return The Hawk Authorization header string.
     * @throws RoborockException If Hawk Authentication generation fails.
     */
    private String getHawkAuthentication(String id, String secret, String key, String path) throws RoborockException {
        try {
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
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RoborockException("Failed generating HawkAuthentication string", e);
        }
    }

    /**
     * Retrieves the base URL for API communication based on the user's email.
     *
     * @param email The user's email.
     * @return A {@link GetUrlByEmail} object containing login data, or null if retrieval fails.
     * @throws RoborockException If authentication fails.
     */
    @Nullable
    public GetUrlByEmail getUrlByEmail(String email) throws RoborockException {
        safeToken = generateSafeToken(email);

        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String payload = "?email=" + encodedEmail + "&needtwostepauth=false";
        String response = invoke(GET_URL_BY_EMAIL_URI + payload, HttpMethod.POST);
        return gson.fromJson(response, GetUrlByEmail.class);
    }

    /**
     * Requests a 2FA code based on the user's email.
     *
     * @param baseUri The base URI for API calls.
     * @param email The user's email.
     * @return A String containing the response.
     * @throws RoborockException If authentication fails.
     */
    @Nullable
    public String requestCodeV4(String baseUri, String email) throws RoborockException {
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
        String payload = "?email=" + encodedEmail + "&type=login&platform=";
        return invoke(baseUri + REQUEST_CODE_V4 + payload, HttpMethod.POST, "application/x-www-form-urlencoded",
                "header_clientlang", "en");
    }

    @Nullable
    public SignCodeV3 signKeyV3(String baseUri, String nonce) throws RoborockException {
        String payload = "?s=" + nonce;

        String response = invoke(baseUri + SIGN_KEY_V3 + payload, HttpMethod.POST);
        return gson.fromJson(response, SignCodeV3.class);
    }

    /**
     * Performs a login operation to obtain authentication tokens.
     *
     * @param baseUri The base URI for API calls.
     * @param email The user's email.
     * @param password The user's password.
     * @return A {@link Login} object containing authentication details, or null if login fails.
     * @throws RoborockException If authentication fails.
     */
    @Nullable
    public String doLogin(String baseUri, String email, String twofa) throws RoborockException {
        if (safeToken.isEmpty()) {
            safeToken = generateSafeToken(email); // Generate if somehow missed
        }

        String payload = "?username=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&verifycode=" + twofa
                + "&verifycodetype=AUTH_EMAIL_CODE";

        return invoke(baseUri + GET_TOKEN_PATH + payload, HttpMethod.POST);
    }

    /**
     * Performs a loginV4 operation to obtain authentication tokens.
     *
     * @param baseUri The base URI for API calls.
     * @param email The user's email.
     * @param country The user's country.
     * @param countryCode The user's country code.
     * @param twofa The 2FA code sent to the user's email
     * @return A {@link Login} object containing authentication details, or null if login fails.
     * @throws RoborockException If authentication fails.
     */
    @Nullable
    public String doLoginV4(String baseUri, String country, String countryCode, String email, String twofa)
            throws RoborockException {
        if (safeToken.isEmpty()) {
            safeToken = generateSafeToken(email); // Generate if somehow missed
        }

        String x_mercy_ks = UUID.randomUUID().toString().substring(0, 16);
        SignCodeV3 signCodeV3 = signKeyV3(baseUri, x_mercy_ks);
        if (signCodeV3 == null || signCodeV3.data == null || signCodeV3.data.k == null) {
            throw new RoborockException("Failed to obtain signCodeV3 or its required data for loginV4.");
        }
        String x_mercy_k = signCodeV3.data.k;
        String payload = "?country=" + country + "&countryCode=" + countryCode + "&email="
                + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&code=" + twofa
                + "&majorVersion=14&minorVersion=0";

        return invoke(baseUri + CODE_LOGIN_V4 + payload, HttpMethod.POST, "application/json", "x-mercy-ks", x_mercy_ks,
                "x-mercy-k", x_mercy_k);
    }

    /**
     * Retrieves home detail information.
     *
     * @param baseUri The base URI for API calls.
     * @param token The authentication token.
     * @return A {@link Home} object containing home details, or null if retrieval fails.
     * @throws RoborockException If authentication fails.
     */
    @Nullable
    public Home getHomeDetail(String baseUri, String token) throws RoborockException {
        String response = invoke(baseUri + GET_HOME_DETAIL_PATH, HttpMethod.GET, "application/json", "Authorization",
                token);
        return gson.fromJson(response, Home.class);
    }

    /**
     * Retrieves home data for a specific home ID.
     *
     * @param rrHomeID The Roborock Home ID.
     * @param rriot The Rriot object containing Hawk authentication details.
     * @return A {@link HomeData} object containing home data, or null if retrieval fails.
     * @throws RoborockException If authentication fails.
     * @throws InvalidKeyException If the provided key for Hawk authentication is invalid.
     */
    @Nullable
    public HomeData getHomeData(String rrHomeID, Rriot rriot) throws RoborockException {
        String path = GET_HOME_DATA_V3_PATH + rrHomeID;
        String token = getHawkAuthentication(rriot.u, rriot.s, rriot.h, path);
        String response = invoke(rriot.r.a + path, HttpMethod.GET, "application/json", "Authorization", token);
        return gson.fromJson(response, HomeData.class);
    }

    /**
     * Retrieves routines for a specific device.
     *
     * @param deviceID The device ID.
     * @param rriot The Rriot object containing Hawk authentication details.
     * @return The raw JSON response string containing routines, or null if retrieval fails.
     * @throws RoborockException If authentication fails.
     * @throws InvalidKeyException If the provided key for Hawk authentication is invalid.
     */
    @Nullable
    public String getRoutines(String deviceID, Rriot rriot) throws RoborockException {
        String path = GET_ROUTINES_PATH + deviceID;
        String hawkToken = getHawkAuthentication(rriot.u, rriot.s, rriot.h, path);
        return invoke(rriot.r.a + path, HttpMethod.GET, "application/json", "Authorization", hawkToken);
    }

    /**
     * Executes a specific routine (scene).
     *
     * @param sceneID The ID of the scene (routine) to execute.
     * @param rriot The Rriot object containing Hawk authentication details.
     * @return The raw JSON response string, or null if execution fails.
     * @throws RoborockException If authentication or comms fails.
     */
    @Nullable
    public String setRoutine(String sceneID, Rriot rriot) throws RoborockException {
        String path = SET_ROUTINE_PATH + sceneID + SET_ROUTINE_PATH_SUFFIX;
        String hawkToken = getHawkAuthentication(rriot.u, rriot.s, rriot.h, path);
        return invoke(rriot.r.a + path, HttpMethod.POST, "application/json", "Authorization", hawkToken);
    }

    private String invoke(String uri, HttpMethod method) throws RoborockException {
        return invoke(uri, method, "application/json", null, null);
    }

    private String invoke(String uri, HttpMethod method, String contentType, @Nullable String headerKey,
            @Nullable String headerValue) throws RoborockException {
        return invoke(uri, method, contentType, headerKey, headerValue, null, null);
    }

    /**
     * Performs an HTTP request using the configured HttpClient.
     *
     * @param uri The full URI to call.
     * @param method The HTTP method (GET, POST, etc.).
     * @param contentType The content-type of the request
     * @param headerKey Optional: The name of an additional header.
     * @param headerValue Optional: The value of an additional header.
     * @param headerKey2 Optional: The name of an additional header.
     * @param headerValue2 Optional: The value of an additional header.
     * @return The response body as a String.
     * @throws RoborockException If there is a comms or authentication error.
     */
    private String invoke(String uri, HttpMethod method, String contentType, @Nullable String headerKey,
            @Nullable String headerValue, @Nullable String headerKey2, @Nullable String headerValue2)
            throws RoborockException {
        logger.debug("Calling url: {}", uri);
        String jsonResponse = "";

        synchronized (this) {
            try {
                Request request = httpClient.newRequest(uri).method(method).header("content-type", contentType)
                        .header("header_clientid", safeToken).timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (headerKey != null && headerValue != null) {
                    request.header(headerKey, headerValue);
                }

                if (headerKey2 != null && headerValue2 != null) {
                    request.header(headerKey2, headerValue2);
                    request.header("header_clientlang", "en");
                    request.header("header_appversion", "4.54.02");
                    request.header("header_phonesystem", "iOS");
                    request.header("header_phonemodel", "iPhone16,1");
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("{} request for {}", method, uri);
                }

                ContentResponse response = request.send();
                int status = response.getStatus();
                jsonResponse = response.getContentAsString();

                if (!jsonResponse.isEmpty()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("JSON response: '{}'", jsonResponse);
                    }
                }

                if (status == HttpStatus.UNAUTHORIZED_401) {
                    throw new RoborockException("Unauthorized");
                }
                if (!HttpStatus.isSuccess(status)) {
                    throw new RoborockException(
                            String.format("Roborock returned error <%d> while invoking %s", status, uri));
                }
            } catch (TimeoutException | ExecutionException | InterruptedException ex) {
                throw new RoborockException(String.format("%s", ex.getLocalizedMessage(), ex));
            }
        }

        return jsonResponse;
    }
}
