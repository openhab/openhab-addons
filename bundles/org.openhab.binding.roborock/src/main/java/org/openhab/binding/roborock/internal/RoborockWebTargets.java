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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles performing the actual HTTP requests for communicating with the RoborockAPI.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class RoborockWebTargets {
    private static final int TIMEOUT_MS = 30000;
    private static final String BASE_URI = "https://usiot.roborock.com";
    private static final String getTokenUri = BASE_URI + "/api/v1/login";
    private static final String getHomeDetailUri = BASE_URI + "/api/v1/getHomeDetail";
    private static final String getHomeDatapath = "/v2/user/homes/";
    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(RoborockWebTargets.class);
    private HttpClient httpClient;
    String safeToken = "";

    public RoborockWebTargets(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private String generateSafeToken(String email) throws NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String token = encoder.encodeToString(bytes);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(email.getBytes());
        md.update(token.substring(0, 16).getBytes());
        byte[] rawData = md.digest();
        byte[] encoded = Base64.getEncoder().encode(rawData);
        return new String(encoded);
    }

    private static String md5Hex(String data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // should never occur
            throw new RuntimeException(e);
        }
        byte[] array = md.digest(data.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String getHawkAuthentication(String id, String secret, String key, String path)
            throws NoSuchAlgorithmException, InvalidKeyException {

        int timestamp = (int) Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString().substring(0, 8);
        String prestr = id + ":" + secret + ":" + nonce + ":" + timestamp + ":" + md5Hex(path) + "::";

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKey);
        byte[] macBytes = mac.doFinal(prestr.getBytes(StandardCharsets.UTF_8));
        byte[] encoded = Base64.getEncoder().encode(macBytes);
        String macString = new String(encoded);
        return "Hawk id=\"" + id + "\",s=\"" + secret + "\",ts=\"" + timestamp + "\",nonce=\"" + nonce + "\",mac=\""
                + macString + "\"";
    }

    @Nullable
    public Login doLogin(String email, String password)
            throws RoborockCommunicationException, RoborockAuthenticationException, NoSuchAlgorithmException {
        String payload = "?username=" + email + "&password=" + password + "&needtwostepauth=false";
        safeToken = generateSafeToken(email);
        String response = invoke(getTokenUri + payload, HttpMethod.POST, null, null, null);
        return gson.fromJson(response, Login.class);
    }

    @Nullable
    public Home getHomeDetail(String token) throws RoborockCommunicationException, RoborockAuthenticationException {
        String response = invoke(getHomeDetailUri, HttpMethod.GET, "Authorization", token, null);
        return gson.fromJson(response, Home.class);
    }

    @Nullable
    public HomeData getHomeData(String rrHomeID, @Nullable Rriot rriot) throws RoborockCommunicationException,
            RoborockAuthenticationException, NoSuchAlgorithmException, InvalidKeyException {
        String path = "/user/homes/" + rrHomeID;
        String token = getHawkAuthentication(rriot.u, rriot.s, rriot.h, path);
        String response = invoke(rriot.r.a + path, HttpMethod.GET, "Authorization", token, null);
        return gson.fromJson(response, HomeData.class);
    }

    public String getVacuumList(String token) throws RoborockCommunicationException, RoborockAuthenticationException {
        return invoke(getHomeDetailUri, HttpMethod.GET, "Authorization", token, null);
    }

    /*
     * public String getDetailedInformation(String publicID, String apiKey)
     * throws RoborockCommunicationException, RoborockAuthenticationException {
     * return invoke(BASE_VEHICLE_URI + publicID + "/detailed?api_key=" + apiKey);
     * }
     * 
     * public void sendCommand(String publicID, String apiKey, String command)
     * throws RoborockCommunicationException, RoborockAuthenticationException {
     * invoke(BASE_VEHICLE_URI + publicID + "/command/" + command + "?api_key=" + apiKey);
     * return;
     * }
     */

    private String invoke(String uri, HttpMethod method, @Nullable String headerKey, @Nullable String headerValue,
            @Nullable String params) throws RoborockCommunicationException, RoborockAuthenticationException {
        logger.debug("Calling url: {}", uri);
        int status = 0;
        String jsonResponse = "";
        synchronized (this) {
            try {
                Request request = httpClient.newRequest(uri).method(method).header("content-type", "application/json")
                        .header("header_clientid", safeToken).header(headerKey, headerValue)
                        .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (logger.isTraceEnabled()) {
                    logger.trace("{} request for {}", method, uri);
                }
                ContentResponse response = request.send();
                status = response.getStatus();
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
                logger.info("cause = {}", ex.getCause(), ex);
                throw new RoborockCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            } catch (InterruptedException ex) {
                throw new RoborockCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            }
        }

        return jsonResponse;
    }
}
