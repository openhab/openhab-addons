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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.roborock.internal.api.Login;
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
    private static final String getVacuumUri = BASE_URI + "/api/v1/getHomeDetail";
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

    @Nullable
    public Login getToken(String email, String password)
            throws RoborockCommunicationException, RoborockAuthenticationException, NoSuchAlgorithmException {
        String payload = "?username=" + email + "&password=" + password + "&needtwostepauth=false";
        safeToken = generateSafeToken(email);
        String response = invoke(getTokenUri + payload, HttpMethod.POST, null, null, null);
        return gson.fromJson(response, Login.class);
    }

    public String getVacuumList(String token) throws RoborockCommunicationException, RoborockAuthenticationException {
        return invoke(getVacuumUri, HttpMethod.GET, "Authorization", token, null);
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
            } catch (TimeoutException | ExecutionException | InterruptedException ex) {
                throw new RoborockCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            }
        }

        return jsonResponse;
    }
}
