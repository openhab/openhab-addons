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
package org.openhab.binding.emerald.internal;

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
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.emerald.internal.api.EmeraldList;
import org.openhab.binding.emerald.internal.api.Login;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Handles performing the actual HTTP requests for communicating with the Emerald Servers.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class EmeraldWebTargets {
    private static final int TIMEOUT_MS = 30000;

    // AWS Cognito Constants (Obtain exact IDs from the emerald_hws_py script)
    private static final String AWS_REGION = "ap-southeast-2";
    private static final String IDENTITY_POOL_ID = "ap-southeast-2:f5bbb02c-c00e-4f10-acb3-e7d1b05268e8";

    private String getTokenUri = "https://api.emerald-ems.com.au/api/v1/customer/sign-in";
    private String getListUri = "https://api.emerald-ems.com.au/api/v1/customer/property/list";
    private final Logger logger = LoggerFactory.getLogger(EmeraldWebTargets.class);
    private HttpClient httpClient;
    String token = "";
    private final Gson gson = new Gson();

    public EmeraldWebTargets(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nullable
    public Login getToken(String email, String password)
            throws EmeraldCommunicationException, EmeraldAuthenticationException {
        JsonObject payload = new JsonObject();
        payload.addProperty("app_version", "2.5.3");
        payload.addProperty("device_name", "iPhone15,2");
        payload.addProperty("device_os_version", "17.2.1");
        payload.addProperty("device_type", "iOS");
        payload.addProperty("password", password);
        payload.addProperty("email", email);

        String response = invoke(getTokenUri, HttpMethod.POST, null, null, payload.toString());
        return gson.fromJson(response, Login.class);
    }

    @Nullable
    public EmeraldList getList(String email, String password)
            throws EmeraldCommunicationException, EmeraldAuthenticationException {
        String response = invoke(getListUri, email, password);
        return gson.fromJson(response, EmeraldList.class);
    }

    // --- NEW AWS COGNITO METHODS ---

    /**
     * Step 1: Exchange for Unauthenticated AWS Identity ID
     */
    public String getAwsIdentityId() throws Exception {
        String uri = "https://cognito-identity." + AWS_REGION + ".amazonaws.com/";

        JsonObject payload = new JsonObject();
        payload.addProperty("IdentityPoolId", IDENTITY_POOL_ID);
        // No Logins object passed for Unauthenticated access
        String response = invokeAws(uri, "AWSCognitoIdentityService.GetId", payload.toString());
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        if (jsonResponse == null || !jsonResponse.has("IdentityId")) {
            throw new IllegalStateException("AWS returned an invalid or empty response for IdentityId");
        }

        return jsonResponse.get("IdentityId").getAsString();
    }

    /**
     * Step 2: Exchange Identity ID for temporary STS Credentials
     */
    public JsonObject getAwsCredentials(String identityId) throws Exception {
        String uri = "https://cognito-identity." + AWS_REGION + ".amazonaws.com/";

        JsonObject payload = new JsonObject();
        payload.addProperty("IdentityId", identityId);
        // No Logins object passed

        String response = invokeAws(uri, "AWSCognitoIdentityService.GetCredentialsForIdentity", payload.toString());
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

        if (jsonResponse == null || !jsonResponse.has("Credentials")) {
            throw new IllegalStateException("AWS returned an invalid or empty response for Credentials");
        }

        return jsonResponse.getAsJsonObject("Credentials");
    }

    private String invokeAws(String uri, String amzTarget, String payload)
            throws InterruptedException, TimeoutException, ExecutionException, EmeraldCommunicationException {
        Request request = httpClient.newRequest(uri).method(HttpMethod.POST)
                .header("content-type", "application/x-amz-json-1.1").header("x-amz-target", amzTarget)
                .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .content(new StringContentProvider(payload), "application/json");

        ContentResponse response = request.send();
        if (!HttpStatus.isSuccess(response.getStatus())) {
            throw new EmeraldCommunicationException(
                    "AWS returned error: " + response.getStatus() + " - " + response.getContentAsString());
        }
        return response.getContentAsString();
    }

    private String invoke(String uri, String email, String password)
            throws EmeraldCommunicationException, EmeraldAuthenticationException {
        if (token.isEmpty()) {
            Login login = getToken(email, password);
            if (login == null) {
                throw new EmeraldAuthenticationException(
                        "Failed to retrieve a valid authentication token from Emerald API.");
            }
            token = login.token;
        }
        return invoke(uri, HttpMethod.GET, "Authorization", "Bearer " + token, "");
    }

    private String invoke(String uri, HttpMethod method, @Nullable String headerKey, @Nullable String headerValue,
            String params) throws EmeraldCommunicationException, EmeraldAuthenticationException {

        int status = 0;
        String jsonResponse = "";
        synchronized (this) {
            try {
                Request request = httpClient.newRequest(uri).method(method).header("accept", "*/*")
                        .header("content-type", "application/json")
                        .header("user-agent",
                                "EmeraldPlanet/2.5.3 (com.emerald-ems.customer; build:5; iOS 17.2.1) Alamofire/5.4.1")
                        .header("accept-language", "en-GB;q=1.0, en-AU;q=0.9").header(headerKey, headerValue)
                        .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .content(new StringContentProvider(params), "application/json");
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
                    throw new EmeraldAuthenticationException("Unauthorized");
                }
                if (!HttpStatus.isSuccess(status)) {
                    throw new EmeraldCommunicationException(
                            String.format("Emerald Servers returned error <%d> while invoking %s", status, uri));
                }
            } catch (TimeoutException | ExecutionException | InterruptedException ex) {
                throw new EmeraldCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            }
        }

        return jsonResponse;
    }
}
