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
package org.openhab.binding.emeraldhws.internal;

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
import org.openhab.binding.emeraldhws.internal.api.List;
import org.openhab.binding.emeraldhws.internal.api.Login;
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
public class EmeraldHWSWebTargets {
    private static final int TIMEOUT_MS = 30000;

    // AWS Cognito Constants (Obtain exact IDs from the emerald_hws_py script)
    private static final String AWS_REGION = "ap-southeast-2"; // Assuming Australian region
    private static final String IDENTITY_POOL_ID = "ap-southeast-2:f5bbb02c-c00e-4f10-acb3-e7d1b05268e8";
    private static final String COGNITO_PROVIDER = "cognito-idp." + AWS_REGION
            + ".amazonaws.com/ap-southeast-2_xxxxxxxxx";

    private String getTokenUri = "https://api.emerald-ems.com.au/api/v1/customer/sign-in";
    private String getListUri = "https://api.emerald-ems.com.au/api/v1/customer/property/list";
    private final Logger logger = LoggerFactory.getLogger(EmeraldHWSWebTargets.class);
    private HttpClient httpClient;
    String token = "";
    private final Gson gson = new Gson();

    public EmeraldHWSWebTargets(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Nullable
    public Login getToken(String email, String password)
            throws EmeraldHWSCommunicationException, EmeraldHWSAuthenticationException {
        JsonObject payload = new JsonObject();
        payload.addProperty("app_version", "2.5.3");
        payload.addProperty("device_name", "iPhone15,2");
        payload.addProperty("device_os_version", "17.2.1");
        payload.addProperty("device_type", "iOS");
        payload.addProperty("password", password);
        payload.addProperty("email", email);
        payload.addProperty("password", password);
        logger.debug("payload = {}", payload.toString());
        String response = invoke(getTokenUri, HttpMethod.POST, null, null, payload.toString());
        return gson.fromJson(response, Login.class);
    }

    @Nullable
    public List getList(String email, String password)
            throws EmeraldHWSCommunicationException, EmeraldHWSAuthenticationException {
        String response = invoke(getListUri, email, password);
        return gson.fromJson(response, List.class);
    }

    // --- NEW AWS COGNITO METHODS ---

    /**
     * Step 1: Exchange Emerald Login Token for AWS Identity ID
     */
    public String getAwsIdentityId(String emeraldToken) throws Exception {
        String uri = "https://cognito-identity." + AWS_REGION + ".amazonaws.com/";

        JsonObject logins = new JsonObject();
        logins.addProperty(COGNITO_PROVIDER, emeraldToken);

        JsonObject payload = new JsonObject();
        payload.addProperty("IdentityPoolId", IDENTITY_POOL_ID);
        payload.add("Logins", logins);

        String response = invokeAws(uri, "AWSCognitoIdentityService.GetId", payload.toString());
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
        return jsonResponse.get("IdentityId").getAsString();
    }

    /**
     * Step 2: Exchange AWS Identity ID for temporary STS Credentials
     */
    public JsonObject getAwsCredentials(String identityId, String emeraldToken) throws Exception {
        String uri = "https://cognito-identity." + AWS_REGION + ".amazonaws.com/";

        JsonObject logins = new JsonObject();
        logins.addProperty(COGNITO_PROVIDER, emeraldToken);

        JsonObject payload = new JsonObject();
        payload.addProperty("IdentityId", identityId);
        payload.add("Logins", logins);

        String response = invokeAws(uri, "AWSCognitoIdentityService.GetCredentialsForIdentity", payload.toString());
        JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
        return jsonResponse.getAsJsonObject("Credentials");
        // Returns object with: AccessKeyId, SecretKey, SessionToken
    }

    private String invokeAws(String uri, String amzTarget, String payload) throws Exception {
        Request request = httpClient.newRequest(uri).method(HttpMethod.POST)
                .header("content-type", "application/x-amz-json-1.1").header("x-amz-target", amzTarget)
                .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .content(new StringContentProvider(payload), "application/json");

        ContentResponse response = request.send();
        if (!HttpStatus.isSuccess(response.getStatus())) {
            throw new Exception("AWS returned error: " + response.getStatus() + " - " + response.getContentAsString());
        }
        return response.getContentAsString();
    }

    private String invoke(String uri, String email, String password)
            throws EmeraldHWSCommunicationException, EmeraldHWSAuthenticationException {
        if (token.isEmpty()) {
            Login login = getToken(email, password);
            token = login.token;
        }
        return invoke(uri, HttpMethod.GET, "Authorization", "Bearer " + token, "");
    }

    private String invoke(String uri, HttpMethod method, @Nullable String headerKey, @Nullable String headerValue,
            String params) throws EmeraldHWSCommunicationException, EmeraldHWSAuthenticationException {
        logger.debug("Calling url: {}", uri);
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
                    throw new EmeraldHWSAuthenticationException("Unauthorized");
                }
                if (!HttpStatus.isSuccess(status)) {
                    throw new EmeraldHWSCommunicationException(
                            String.format("Tesla Powerwall returned error <%d> while invoking %s", status, uri));
                }
            } catch (TimeoutException | ExecutionException | InterruptedException ex) {
                throw new EmeraldHWSCommunicationException(String.format("{}", ex.getLocalizedMessage(), ex));
            }
        }

        return jsonResponse;
    }
}
