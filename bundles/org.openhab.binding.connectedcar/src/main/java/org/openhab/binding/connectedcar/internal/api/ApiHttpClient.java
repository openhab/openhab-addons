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
package org.openhab.binding.connectedcar.internal.api;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.net.CookieStore;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
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
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ApiHttpClient} implements http client functions
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class ApiHttpClient {
    private final Logger logger = LoggerFactory.getLogger(ApiHttpClient.class);

    private final HttpClient httpClient;
    private CombinedConfig config = new CombinedConfig();
    private @Nullable ApiEventListener eventListener;

    public ApiHttpClient() {
        this.httpClient = new HttpClient();
    }

    public ApiHttpClient(HttpClient httpClient, @Nullable ApiEventListener eventListener) {
        this.httpClient = httpClient;
        this.eventListener = eventListener;
    }

    public void setConfig(CombinedConfig config) {
        logger.trace("{}: HTTP Update config from {}/{} to {}/{}", this.config.getLogId(), this.config.tokenSetId,
                this.config.api.clientId, config.tokenSetId, config.api.clientId);
        this.config = config;
    }

    public CombinedConfig getConfig() {
        return config;
    }

    /**
     * Sends a HTTP GET request using the synchronous client
     *
     * @param path Path of the requested resource
     * @return response
     */
    public ApiResult get(String uri, Map<String, String> headers, String token) throws ApiException {
        headers.put(HttpHeader.AUTHORIZATION.toString(), "Bearer " + token);
        return request(HttpMethod.GET, uri, "", headers, "", "", token);
    }

    public ApiResult get(String uri, Map<String, String> headers, boolean followRedirect) throws ApiException {
        return request(HttpMethod.GET, uri, "", headers, "", "", "", followRedirect);
    }

    public ApiResult get(String uri, Map<String, String> headers) throws ApiException {
        return request(HttpMethod.GET, uri, "", headers, "", "", "");
    }

    public ApiResult get(String uri, String vin, Map<String, String> headers) throws ApiException {
        return request(HttpMethod.GET, uri, "", headers, "", vin, "");
    }

    /**
     * Sends a HTTP PUT request using the synchronous client
     *
     * @param path Path of the requested resource
     * @return response
     */
    public ApiResult put(String uri, Map<String, String> headers, String data) throws ApiException {
        return request(HttpMethod.PUT, uri, "", headers, data, "", "", false);
    }

    /**
     * Sends a HTTP POST request using the synchronous client
     *
     * @param path Path of the requested resource
     * @return response
     */
    public ApiResult post(String uri, String parms, Map<String, String> headers, String data) throws ApiException {
        return request(HttpMethod.POST, uri, parms, headers, data, "", "", false);
    }

    public ApiResult post(String uri, Map<String, String> headers, String data) throws ApiException {
        return request(HttpMethod.POST, uri, "", headers, data, "", "", false);
    }

    public ApiResult post(String uri, Map<String, String> headers, String data, String token) throws ApiException {
        return request(HttpMethod.POST, uri, "", headers, data, "", token, false);
    }

    public ApiResult post(String uri, Map<String, String> headers, Map<String, String> data, boolean json)
            throws ApiException {
        return request(HttpMethod.POST, uri, "", headers, buildPostData(data, json), "", "", false);
    }

    public ApiResult post(String uri, Map<String, String> headers, Map<String, String> data, boolean json,
            boolean followRedirect) throws ApiException {
        return request(HttpMethod.POST, uri, "", headers, buildPostData(data, json), "", "", followRedirect);
    }

    private ApiResult request(HttpMethod method, String uri, String parms, Map<String, String> headers, String data,
            String pvin, String token) throws ApiException {
        return request(method, uri, parms, headers, data, pvin, token, true);
    }

    /**
     * Make HTTP request (GET/PUT) with given set of headers. Body gets fill depending on method and content type.
     *
     * @param method HTTP method (GET/POST)
     * @param uri URL of URI. A complete URL will be created based on the brand if only a URI is given
     *            url
     * @param parms Paremeters will be added to the URL
     * @param headers HTTP headers, additional headers might be added depending on content type
     * @param data Body field, gets formatted according content type (form encoded vs. JSON format)
     * @param pvin The account handler specifies a specific VIN, if empty config.vehicle.vin will be used
     * @param token Bearer or security token (or empty)
     * @return Returns the HTTP response. In additional lastHttpHeaders get filled with the http response headers
     * @throws ApiException
     */
    public ApiResult request(HttpMethod method, String uri, String parms, Map<String, String> headers, String data,
            String pvin, String token, boolean followRedirect) throws ApiException {
        Request request = null;
        String url = "";
        try {
            String vin = pvin.isEmpty() ? config.vehicle.vin : pvin;
            url = getBrandUrl(uri, parms, vin);
            ApiResult apiResult = new ApiResult(method.toString(), url);
            request = httpClient.newRequest(url).method(method).timeout(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            fillHttpHeaders(request, headers, token);
            fillPostData(request, data);

            // Do request and get response
            logger.debug("{}: HTTP {} {}", config.getLogId(), request.getMethod(), request.getURI());
            logger.trace("  Headers: \n{}", request.getHeaders().toString());
            if (!data.isEmpty()) {
                logger.trace("  Body/Data: {}", data);
            }

            request.followRedirects(false);
            ContentResponse contentResponse = request.send();
            apiResult = new ApiResult(contentResponse);
            if (apiResult.rateLimit > 0) {
                logger.debug("{}: Remaining rate limit = {}", config.vehicle.vin, apiResult.rateLimit);
                if (eventListener != null) {
                    eventListener.onRateLimit(apiResult.rateLimit);
                }
            }

            // validate response, API errors are reported as Json
            String response = apiResult.response.replaceAll("[\r\n\t]", "");
            logger.debug("{}: HTTP {} Response: {}", config.getLogId(), apiResult.httpCode, response);
            logger.trace("  Headers: \n{}", apiResult.responseHeaders);
            String location = apiResult.getLocation();
            switch (apiResult.httpCode) {
                case HttpStatus.UNAUTHORIZED_401:
                case HttpStatus.FORBIDDEN_403:
                case HttpStatus.METHOD_NOT_ALLOWED_405:
                    throw new ApiSecurityException("Forbidden", apiResult);
                case HttpStatus.OK_200:
                case HttpStatus.CREATED_201:
                case HttpStatus.ACCEPTED_202:
                case HttpStatus.NO_CONTENT_204:
                case HttpStatus.SEE_OTHER_303:
                case HttpStatus.MULTI_STATUS_207:
                    return apiResult; // valid
                case HttpStatus.MOVED_PERMANENTLY_301:
                case HttpStatus.TEMPORARY_REDIRECT_307:
                case HttpStatus.FOUND_302:
                    if (!location.isEmpty()) {
                        logger.debug("{}: HTTP {} -> {}", config.getLogId(), apiResult.httpCode, location);
                    }
                    break;
                default:
                    throw new ApiException("API call failed", apiResult);
            }
            if (response.contains("\"error\":")) {
                throw new ApiException("API returned error", apiResult);
            }
            if (response.isEmpty() && location.isEmpty()) {
                throw new ApiException("Invalid result received from API, maybe URL problem", apiResult);
            }
            return apiResult;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new ApiException("API call failed!", new ApiResult(request, e), e);
        }
    }

    public void clearCookies() {
        CookieStore store = httpClient.getCookieStore();
        if (store != null) {
            store.removeAll();
        }
    }

    /**
     * Constructs an URL from the stored information, a specified path and a specified argument string
     *
     */
    private String getBrandUrl(String template, String args, String vin) throws ApiException {
        if (config.api.brand.isEmpty()) {
            throw new ApiException(String.format("Brand for API access not set (%s)", config.api.brand));
        }
        String path = MessageFormat.format(template, config.api.brand, config.api.xcountry, vin, config.user.id);
        if (!template.contains("://")) { // not a full URL
            return getUrl(path.isEmpty() ? path : path + (!args.isEmpty() ? "?" + args : ""));
        } else {
            return path + (!args.isEmpty() ? "?" + args : "");
        }
    }

    public static void fillHttpHeaders(Request request, Map<String, String> headers, String token) {
        for (Map.Entry<String, String> h : headers.entrySet()) {
            String key = h.getKey();
            String value = h.getValue();
            if (key.equals(HttpHeader.USER_AGENT.toString())) {
                request.agent(value);
            } else if (key.equals(HttpHeader.ACCEPT.toString())) {
                request.accept(value);
            } else {
                if (!value.isEmpty()) {
                    request.header(key, value);
                }
            }
        }
    }

    /**
     * Fill standad http headers
     *
     * @param HTTP header fields to insert
     * @param token Access token (bearer), might be empty
     * @return Updated headers
     */
    public Map<String, String> fillAppHeaders(Map<String, String> headers, String token) throws ApiException {
        if (!config.api.xappName.isEmpty()) {
            headers.put(CNAPI_HEADER_APP, config.api.xappName);
            headers.put(CNAPI_HEADER_VERS, config.api.xappVersion);
            headers.put("X-Country-Id", "DE");
            headers.put("X-Language-Id", "de");
        }
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(HttpHeader.ACCEPT.toString(),
                "application/json, application/vnd.volkswagenag.com-error-v1+json, */*");
        headers.put(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString());
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION.toString())) {
            headers.put(HttpHeader.AUTHORIZATION.toString(), "Bearer " + token);
        }
        return headers;
    }

    /**
     * Fill in POST data, set http headers
     *
     * @param request HTTP request structure
     * @param data POST data, might be empty
     */
    private void fillPostData(Request request, String data) {
        if (!data.isEmpty()) {
            StringContentProvider postData;
            if (request.getHeaders().contains(HttpHeader.CONTENT_TYPE)) {
                String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
                postData = new StringContentProvider(contentType, data, StandardCharsets.UTF_8);
            } else {
                boolean json = data.startsWith("{") || data.contains("\": {");
                String type = json ? CONTENT_TYPE_JSON : CONTENT_TYPE_FORM_URLENC;
                request.header(HttpHeader.CONTENT_TYPE, type);
                postData = new StringContentProvider(type, data, StandardCharsets.UTF_8);
            }
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

    /**
     * Constructs an URL from the stored information and a specified path
     *
     * @param path Path to include in URL
     * @return URL
     */
    private String getUrl(String path) throws ApiException {
        String base = getBaseUrl();
        return base.endsWith("/") && path.startsWith("/") ? base + substringBefore(path, "/")
                : base + (path.startsWith("/") ? path : "/" + path);
    }

    /**
     * Build URL base depending on brand
     *
     * @return URL prefix/base url
     * @throws ApiException
     */
    public String getBaseUrl() throws ApiException {
        if (!config.vstatus.apiUrlPrefix.isEmpty()) {
            return config.vstatus.apiUrlPrefix;
        }
        if (!config.api.apiDefaultUrl.isEmpty()) {
            return config.api.apiDefaultUrl;
        }
        throw new ApiException("Unknown  base URL for brand URL");
    }

    /**
     * Get parameter value from URL string
     *
     * @param input URL string
     * @param parameter Parameter name
     * @return Extracted value
     */
    public static String getUrlParm(String input, String parameter) {
        String p = getUrlParm(input, parameter, "?");
        return !p.isEmpty() ? p : getUrlParm(input, parameter, "&");
    }

    public static String getUrlParm(String input, String parameter, String seperator) {
        String pattern = seperator + parameter + "=";
        if (input.contains(pattern)) {
            String res = substringAfter(input, pattern);
            return res.contains("&") ? substringBefore(res, "&") : res;
        }
        return "";
    }
}
