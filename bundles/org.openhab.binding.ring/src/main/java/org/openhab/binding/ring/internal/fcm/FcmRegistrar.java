/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.fcm;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.ring.internal.errors.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Handles the registration of a virtual Android device with Google's servers
 * to obtain the cryptographic tokens required for Firebase Cloud Messaging (FCM).
 *
 * @author Paul Smedley - Initial contribution
 *
 */
@NonNullByDefault
public class FcmRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(FcmRegistrar.class);

    private static final String URL_CHECKIN = "https://android.clients.google.com/checkin";
    private static final String URL_REGISTER = "https://android.clients.google.com/c2dm/register3";
    private static final int CONNECTION_TIMEOUT = 12000;

    // Official Ring App Firebase Constants
    private static final String RING_SENDER_ID = "876313859327";
    private static final String RING_APP_ID = "1:876313859327:android:e10ec6ddb3c81f39";
    private static final String RING_PROJECT_ID = "ring-17770";
    private static final String RING_PACKAGE_NAME = "com.ringapp";

    private final HttpClient httpClient;

    public FcmRegistrar(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Performs the full 2-step registration process to obtain an FCM token.
     *
     * @return FcmCredentials containing the androidId, securityToken, and fcmToken.
     * @throws AuthenticationException if network communication or parsing fails.
     */
    public FcmCredentials register() throws AuthenticationException {
        logger.debug("Starting FCM device registration flow");

        // Step 1: Check-in as a generic Android device
        CheckinResult checkin = performAndroidCheckin();
        logger.debug("Android check-in successful. Android ID: {}", checkin.androidId());

        // Step 2: Register the device specifically for the Ring application's FCM scope
        String fcmToken = performC2dmRegistration(checkin.androidId(), checkin.securityToken());
        logger.debug("FCM token generation successful.");

        return new FcmCredentials(checkin.androidId(), checkin.securityToken(), fcmToken);
    }

    private CheckinResult performAndroidCheckin() throws AuthenticationException {
        // Generic initialization payload expected by Google's check-in server
        String checkinPayload = "{\"checkin\":{\"type\":3,\"mac_addr\":[\"\"],\"imei\":\"\"},\"version\":3,\"id\":0,\"security_token\":0}";

        try {
            Request request = httpClient.newRequest(URL_CHECKIN).method(HttpMethod.POST)
                    .timeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).header("Content-Type", "application/json")
                    .content(new StringContentProvider(checkinPayload));

            ContentResponse response = request.send();

            if (response.getStatus() != HttpStatus.OK_200) {
                throw new AuthenticationException("Google check-in failed with status: " + response.getStatus());
            }

            String responseString = response.getContentAsString();
            JsonObject json = JsonParser.parseString(responseString).getAsJsonObject();

            if (!json.has("android_id") || !json.has("security_token")) {
                throw new AuthenticationException("Google check-in response missing required tokens");
            }

            // The API returns these as large numbers, parse them safely as strings
            String androidId = json.get("android_id").getAsString();
            String securityToken = json.get("security_token").getAsString();

            return new CheckinResult(androidId, securityToken);

        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new AuthenticationException("Communication error during Android check-in: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new AuthenticationException("Failed to parse Google check-in response: " + e.getMessage());
        }
    }

    private String performC2dmRegistration(String androidId, String securityToken) throws AuthenticationException {
        // Build the URL-encoded payload matching Ring's exact Firebase project setup
        StringBuilder payloadBuilder = new StringBuilder();
        payloadBuilder.append("sender=").append(RING_SENDER_ID).append("&X-subtype=").append(RING_SENDER_ID)
                .append("&device=").append(androidId).append("&app=").append(RING_PACKAGE_NAME).append("&X-app_id=")
                .append(RING_APP_ID).append("&X-project_id=").append(RING_PROJECT_ID);

        try {
            Request request = httpClient.newRequest(URL_REGISTER).method(HttpMethod.POST)
                    .timeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .header("Authorization", "AidLogin " + androidId + ":" + securityToken)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .content(new StringContentProvider(payloadBuilder.toString()));

            ContentResponse response = request.send();

            if (response.getStatus() != HttpStatus.OK_200) {
                throw new AuthenticationException("FCM C2DM registration failed with status: " + response.getStatus());
            }

            String responseString = response.getContentAsString();

            // The response format is typically plain text: "token=APA91bHPR...[token_string]"
            if (responseString.startsWith("token=")) {
                return responseString.substring(6).trim();
            } else if (responseString.contains("Error=")) {
                throw new AuthenticationException("FCM Registration returned error: " + responseString);
            } else {
                throw new AuthenticationException("Unexpected FCM registration response format");
            }

        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new AuthenticationException("Communication error during FCM registration: " + e.getMessage());
        }
    }

    // --- Data Records ---

    private record CheckinResult(String androidId, String securityToken) {
    }

    /**
     * Holds the complete set of credentials required to open the MCS socket
     * and subscribe the Ring account to push notifications.
     */
    public record FcmCredentials(String androidId, String securityToken, String fcmToken) {
    }
}
