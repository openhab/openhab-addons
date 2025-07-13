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
package org.openhab.binding.shelly.internal.api;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.SHELLY_API_TIMEOUT_MS;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthChallenge;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2AuthRsp;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcBaseMessage;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link ShellyHttpClient} implements basic HTTP access
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyHttpClient {
    private final Logger logger = LoggerFactory.getLogger(ShellyHttpClient.class);

    public static final String HTTP_HEADER_AUTH = HttpHeaders.AUTHORIZATION;
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String HTTP_AUTH_TYPE_DIGEST = "Digest";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    public static final String CONTENT_TYPE_FORM_URLENC = "application/x-www-form-urlencoded";

    protected final HttpClient httpClient;
    protected ShellyThingConfiguration config = new ShellyThingConfiguration();
    protected String thingName;
    protected final Gson gson = new Gson();
    protected int timeoutErrors = 0;
    protected int timeoutsRecovered = 0;
    private ShellyDeviceProfile profile;
    protected boolean basicAuth = false;

    public ShellyHttpClient(String thingName, ShellyThingInterface thing) {
        this(thingName, thing.getThingConfig(), thing.getHttpClient());
        this.profile = thing.getProfile();
    }

    public ShellyHttpClient(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        profile = new ShellyDeviceProfile();
        this.thingName = thingName;
        setConfig(thingName, config);
        this.httpClient = httpClient;
    }

    public void setConfig(String thingName, ShellyThingConfiguration config) {
        this.thingName = thingName;
        this.config = config;
    }

    /**
     * Submit GET request and return response, check for invalid responses
     *
     * @param uri: URI (e.g. "/settings")
     */
    public <T> T callApi(String uri, Class<T> classOfT) throws ShellyApiException {
        String json = httpRequest(uri);
        return fromJson(gson, json, classOfT);
    }

    public <T> T postApi(String uri, String data, Class<T> classOfT) throws ShellyApiException {
        String json = httpPost(uri, data);
        return fromJson(gson, json, classOfT);
    }

    protected String httpRequest(String uri) throws ShellyApiException {
        ShellyApiResult apiResult = new ShellyApiResult();
        int retries = 3;
        boolean timeout = false;
        while (retries > 0) {
            try {
                apiResult = innerRequest(HttpMethod.GET, uri, null, "");

                // If call doesn't throw an exception the device is reachable == no timeout
                if (timeout) {
                    logger.debug("{}: API timeout #{}/{} recovered ({})", thingName, timeoutErrors, timeoutsRecovered,
                            apiResult.getUrl());
                    timeoutsRecovered++;
                }
                return apiResult.response; // successful
            } catch (ShellyApiException e) {
                if (e.isHttpAccessUnauthorized() && !profile.isGen2 && !basicAuth && !config.password.isEmpty()) {
                    logger.debug("{}: Access is unauthorized, auto-activate basic auth", thingName);
                    basicAuth = true;
                    apiResult = innerRequest(HttpMethod.GET, uri, null, "");
                }

                if (e.isConnectionError()
                        || (!e.isTimeout() && !apiResult.isHttpServerError()) && !apiResult.isNotFound()
                        || profile.hasBattery || (retries == 0)) {
                    // Sensor in sleep mode or API exception for non-battery device or retry counter expired
                    throw e; // non-timeout exception
                }

                timeout = true;
                timeoutErrors++; // count the retries
                retries--;
                if (profile.alwaysOn) {
                    logger.debug("{}: API Timeout, retry #{} ({})", thingName, timeoutErrors, e.toString());
                }
            }
        }
        throw new ShellyApiException("API Timeout or inconsistent result"); // successful
    }

    public String httpPost(String uri, String data) throws ShellyApiException {
        return innerRequest(HttpMethod.POST, uri, null, data).response;
    }

    public String httpPost(@Nullable Shelly2AuthChallenge auth, String data) throws ShellyApiException {
        return innerRequest(HttpMethod.POST, SHELLYRPC_ENDPOINT, auth, data).response;
    }

    private ShellyApiResult innerRequest(HttpMethod method, String uri, @Nullable Shelly2AuthChallenge auth,
            String data) throws ShellyApiException {
        Request request = null;
        String url = "http://" + config.deviceIp + uri;
        ShellyApiResult apiResult = new ShellyApiResult(method.toString(), url);

        try {
            request = httpClient.newRequest(url).method(method.toString()).timeout(SHELLY_API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);

            if (!uri.equals(SHELLY_URL_DEVINFO) && !config.password.isEmpty()) { // not for /shelly or no password
                                                                                 // configured
                // Add Auth info
                // Gen 1: Basic Auth
                // Gen 2: Digest Auth
                String authHeader = "";
                if (auth != null) { // only if we received an Auth challenge
                    authHeader = formatAuthResponse(uri,
                            buildAuthResponse(uri, auth, SHELLY2_AUTHDEF_USER, config.password));
                } else {
                    if (basicAuth) {
                        String bearer = config.userId + ":" + config.password;
                        authHeader = HTTP_AUTH_TYPE_BASIC + " " + Base64.getEncoder().encodeToString(bearer.getBytes());
                    }
                }
                if (!authHeader.isEmpty()) {
                    request.header(HTTP_HEADER_AUTH, authHeader);
                }
            }
            fillPostData(request, data);
            logger.trace("{}: HTTP {} {}\n{}\n{}", thingName, method, url, request.getHeaders(), data);

            // Do request and get response
            ContentResponse contentResponse = request.send();
            apiResult = new ShellyApiResult(contentResponse);
            apiResult.httpCode = contentResponse.getStatus();
            String response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("{}: HTTP Response {}: {}\n{}", thingName, contentResponse.getStatus(), response,
                    contentResponse.getHeaders());

            if (response.contains("\"error\":{")) { // Gen2
                Shelly2RpcBaseMessage message = gson.fromJson(response, Shelly2RpcBaseMessage.class);
                if (message != null && message.error != null) {
                    apiResult.httpCode = message.error.code;
                    apiResult.response = message.error.message;
                    if (getInteger(message.error.code) == HttpStatus.UNAUTHORIZED_401) {
                        apiResult.authChallenge = getString(message.error.message).replaceAll("\\\"", "\"");
                    }
                }
            }
            HttpFields headers = contentResponse.getHeaders();
            String authChallenge = headers.get(HttpHeader.WWW_AUTHENTICATE);
            if (!getString(authChallenge).isEmpty()) {
                apiResult.authChallenge = authChallenge;
            }

            // validate response, API errors are reported as Json
            if (apiResult.httpCode != HttpStatus.OK_200) {
                throw new ShellyApiException(apiResult);
            }

            if (response.isEmpty() || !response.startsWith("{") && !response.startsWith("[") && !url.contains("/debug/")
                    && !url.contains("/sta_cache_reset")) {
                throw new ShellyApiException("Unexpected response: " + response);
            }
        } catch (ExecutionException | InterruptedException | TimeoutException | IllegalArgumentException e) {
            ShellyApiException ex = new ShellyApiException(apiResult, e);
            if (!ex.isConnectionError() && !ex.isTimeout()) { // will be handled by the caller
                logger.trace("{}: API call returned exception", thingName, ex);
            }
            throw ex;
        }
        return apiResult;
    }

    protected @Nullable Shelly2AuthRsp buildAuthResponse(String uri, @Nullable Shelly2AuthChallenge challenge,
            String user, String password) throws ShellyApiException {
        if (challenge == null) {
            return null; // not required
        }
        if (!SHELLY2_AUTHTTYPE_DIGEST.equalsIgnoreCase(challenge.authType)
                || !SHELLY2_AUTHALG_SHA256.equalsIgnoreCase(challenge.algorithm)) {
            throw new IllegalArgumentException("Unsupported Auth type/algorithm requested by device");
        }
        Shelly2AuthRsp response = new Shelly2AuthRsp();
        response.username = user;
        response.realm = challenge.realm;
        response.nonce = challenge.nonce;
        response.cnonce = Long.toHexString((long) Math.floor(Math.random() * 10e8));
        response.nc = "00000001";
        response.authType = challenge.authType;
        response.algorithm = challenge.algorithm;
        String ha1 = sha256(response.username + ":" + response.realm + ":" + password);
        String ha2 = sha256(HttpMethod.POST + ":" + uri);// SHELLY2_AUTH_NOISE;
        response.response = sha256(
                ha1 + ":" + response.nonce + ":" + response.nc + ":" + response.cnonce + ":" + "auth" + ":" + ha2);
        return response;
    }

    protected String formatAuthResponse(String uri, @Nullable Shelly2AuthRsp rsp) {
        return rsp != null ? MessageFormat.format(HTTP_AUTH_TYPE_DIGEST
                + " username=\"{0}\", realm=\"{1}\", uri=\"{2}\", nonce=\"{3}\", cnonce=\"{4}\", nc=\"{5}\", qop=\"auth\",response=\"{6}\", algorithm=\"{7}\", ",
                rsp.username, rsp.realm, uri, rsp.nonce, rsp.cnonce, rsp.nc, rsp.response, rsp.algorithm) : "";
    }

    /**
     * Fill in POST data, set http headers
     *
     * @param request HTTP request structure
     * @param data POST data, might be empty
     */
    private void fillPostData(Request request, String data) {
        boolean json = data.startsWith("{") || data.contains("\": {");
        String type = json ? CONTENT_TYPE_JSON : CONTENT_TYPE_FORM_URLENC;
        request.header(HttpHeader.CONTENT_TYPE, type);
        if (!data.isEmpty()) {
            StringContentProvider postData;
            postData = new StringContentProvider(type, data, StandardCharsets.UTF_8);
            request.content(postData);
            // request.header(HttpHeader.CONTENT_LENGTH, Long.toString(postData.getLength()));
        }
    }

    /**
     * Format POST body depending on content type (JSON or form encoded)
     *
     * @param dataMap Field list
     * @param json true=JSON format, false=form encoded
     * @return formatted body
     */
    public static String buildPostData(Map<String, String> dataMap, boolean json) {
        String data = "";
        for (Map.Entry<String, String> e : dataMap.entrySet()) {
            data = data + (data.isEmpty() ? "" : json ? ", " : "&");
            if (!json) {
                data = data + e.getKey() + "=" + e.getValue();
            } else {
                data = data + "\"" + e.getKey() + "\" : \"" + e.getValue() + "\"";
            }
        }
        return json ? "{ " + data + " }" : data;
    }

    public String getControlUriPrefix(Integer id) {
        String uri = "";
        if (profile.isLight || profile.isDimmer) {
            if (profile.isDuo || profile.isDimmer) {
                // Duo + Dimmer
                uri = SHELLY_URL_CONTROL_LIGHT;
            } else {
                // Bulb + RGBW2
                uri = "/" + (profile.inColor ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE);
            }
        } else {
            // Roller, Relay
            uri = SHELLY_URL_CONTROL_RELEAY;
        }
        uri = uri + "/" + id;
        return uri;
    }

    public int getTimeoutErrors() {
        return timeoutErrors;
    }

    public int getTimeoutsRecovered() {
        return timeoutsRecovered;
    }

    public void postEvent(String device, String index, String event, Map<String, String> parms)
            throws ShellyApiException {
    }
}
