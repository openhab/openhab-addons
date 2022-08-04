/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
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

    public static final String HTTP_HEADER_AUTH = "Authorization";
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";
    public static final String CONTENT_TYPE_FORM_URLENC = "application/x-www-form-urlencoded";

    protected final HttpClient httpClient;
    protected ShellyThingConfiguration config = new ShellyThingConfiguration();
    protected String thingName;
    protected final Gson gson = new Gson();
    protected int timeoutErrors = 0;
    protected int timeoutsRecovered = 0;
    private ShellyDeviceProfile profile;

    public ShellyHttpClient(String thingName, ShellyThingInterface thing) {
        this(thingName, thing.getThingConfig(), thing.getHttpClient());
        this.profile = thing.getProfile();
        profile.initFromThingType(thingName);
    }

    public ShellyHttpClient(String thingName, ShellyThingConfiguration config, HttpClient httpClient) {
        profile = new ShellyDeviceProfile();
        this.thingName = thingName;
        setConfig(thingName, config);
        this.httpClient = httpClient;
    }

    public void initialize() throws ShellyApiException {
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
                apiResult = innerRequest(HttpMethod.GET, uri, "");
                if (timeout) {
                    logger.debug("{}: API timeout #{}/{} recovered ({})", thingName, timeoutErrors, timeoutsRecovered,
                            apiResult.getUrl());
                    timeoutsRecovered++;
                }
                return apiResult.response; // successful
            } catch (ShellyApiException e) {
                if ((!e.isTimeout() && !apiResult.isHttpServerError()) && !apiResult.isNotFound() || profile.hasBattery
                        || (retries == 0)) {
                    // Sensor in sleep mode or API exception for non-battery device or retry counter expired
                    throw e; // non-timeout exception
                }

                timeout = true;
                retries--;
                timeoutErrors++; // count the retries
                logger.debug("{}: API Timeout, retry #{} ({})", thingName, timeoutErrors, e.toString());
            }
        }
        throw new ShellyApiException("API Timeout or inconsistent result"); // successful
    }

    public String httpPost(String uri, String data) throws ShellyApiException {
        return innerRequest(HttpMethod.POST, uri, data).response;
    }

    private ShellyApiResult innerRequest(HttpMethod method, String uri, String data) throws ShellyApiException {
        Request request = null;
        String url = "http://" + config.deviceIp + uri;
        ShellyApiResult apiResult = new ShellyApiResult(method.toString(), url);

        try {
            request = httpClient.newRequest(url).method(method.toString()).timeout(SHELLY_API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);

            if (!config.password.isEmpty() && !getString(data).contains("\"auth\":{")) {
                String value = config.userId + ":" + config.password;
                request.header(HTTP_HEADER_AUTH,
                        HTTP_AUTH_TYPE_BASIC + " " + Base64.getEncoder().encodeToString(value.getBytes()));
            }
            fillPostData(request, data);
            logger.trace("{}: HTTP {} for {} {}", thingName, method, url, data);

            // Do request and get response
            ContentResponse contentResponse = request.send();
            apiResult = new ShellyApiResult(contentResponse);
            apiResult.httpCode = contentResponse.getStatus();
            String response = contentResponse.getContentAsString().replace("\t", "").replace("\r\n", "").trim();
            logger.trace("{}: HTTP Response {}: {}", thingName, contentResponse.getStatus(), response);

            HttpFields headers = contentResponse.getHeaders();
            String auth = headers.get(HttpHeader.WWW_AUTHENTICATE);
            if (!getString(auth).isEmpty()) {
                apiResult.authResponse = auth;
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
            if (!ex.isTimeout()) { // will be handled by the caller
                logger.trace("{}: API call returned exception", thingName, ex);
            }
            throw ex;
        }
        return apiResult;
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
            request.header(HttpHeader.CONTENT_LENGTH, Long.toString(postData.getLength()));
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
}
