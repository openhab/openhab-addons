/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.carnet.internal.api;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.*;
import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
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
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.CarNetSecurityException;
import org.openhab.binding.carnet.internal.config.CarNetCombinedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarNetHttpClient} implements http client functions
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetHttpClient {
    private final Logger logger = LoggerFactory.getLogger(CarNetHttpClient.class);

    private final HttpClient httpClient;
    private CarNetCombinedConfig config = new CarNetCombinedConfig();
    private HttpFields responseHeaders = new HttpFields();

    public CarNetHttpClient() {
        this.httpClient = new HttpClient();
    }

    public CarNetHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setConfig(CarNetCombinedConfig config) {
        this.config = config;
    }

    public static void fillHttpHeaders(Request request, Map<String, String> headers, String token) {
        if (!headers.isEmpty()) {
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
    }

    /**
     * Sends a HTTP GET request using the synchronous client
     *
     * @param path Path of the requested resource
     * @return response
     */
    public String get(String uri, Map<String, String> headers, String token) throws CarNetException {
        headers.put(HttpHeader.AUTHORIZATION.toString(), "Bearer " + token);
        return request(HttpMethod.GET, uri, "", headers, "", "", token);
    }

    public String get(String uri, Map<String, String> headers, boolean followRedirect) throws CarNetException {
        return request(HttpMethod.GET, uri, "", headers, "", "", "", followRedirect);
    }

    public String get(String uri, Map<String, String> headers) throws CarNetException {
        return request(HttpMethod.GET, uri, "", headers, "", "", "");
    }

    public String get(String uri, String vin, Map<String, String> headers) throws CarNetException {
        return request(HttpMethod.GET, uri, "", headers, "", vin, "");
    }

    /**
     * Sends a HTTP POST request using the synchronous client
     *
     * @param path Path of the requested resource
     * @return response
     */
    public String post(String uri, String parms, Map<String, String> headers, String data) throws CarNetException {
        return request(HttpMethod.POST, uri, parms, headers, data, "", "", false);
    }

    public String post(String uri, Map<String, String> headers, String data) throws CarNetException {
        return request(HttpMethod.POST, uri, "", headers, data, "", "", false);
    }

    public String post(String uri, Map<String, String> headers, String data, String token) throws CarNetException {
        return request(HttpMethod.POST, uri, "", headers, data, "", token, false);
    }

    public String post(String uri, Map<String, String> headers, Map<String, String> data, boolean json)
            throws CarNetException {
        return request(HttpMethod.POST, uri, "", headers, buildPostData(data, json), "", "", false);
    }

    public String post(String uri, Map<String, String> headers, Map<String, String> data, boolean json,
            boolean followRedirect) throws CarNetException {
        return request(HttpMethod.POST, uri, "", headers, buildPostData(data, json), "", "", followRedirect);
    }

    /**
     * Make http request (GET/PUT) with given set of headers. Body gets fill depending on method and content type.
     *
     * @param method HTTP method (GET/POST)
     * @param uri URL of URI suffix. If only a suffix is given a complete URL will be created based on the brand base
     *            url
     * @param parms Paremeters will be added to the URL
     * @param headers HTTP headers, additional headers might be added depending on content type
     * @param data Body field, gets formatted according content type (form encoded vs. JSON format)
     * @param pvin The account handler specifies a specific pin, if empty config.vehicle.vin will be used
     * @param token Bearer or security token (or empty)
     * @return Returns the HTTP response. In additional lastHttpHeaders get filled with the http response headers
     * @throws CarNetException
     */
    private String request(HttpMethod method, String uri, String parms, Map<String, String> headers, String data,
            String pvin, String token, boolean followRedirect) throws CarNetException {
        Request request = null;
        String url = "";
        try {
            String vin = pvin.isEmpty() ? config.vehicle.vin : pvin;
            url = getBrandUrl(uri, parms, vin);
            CarNetApiResult apiResult = new CarNetApiResult(method.toString(), url);
            request = httpClient.newRequest(url).method(method).timeout(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            fillHttpHeaders(request, headers, token);
            fillPostData(request, data);

            // Do request and get response
            logger.debug("HTTP {} {}\nBody/Data={}", request.getMethod(), request.getURI(), data);
            logger.trace("  Headers: \n{}", request.getHeaders().toString());
            // request.followRedirects(followRedirect);
            request.followRedirects(false);
            ContentResponse contentResponse = request.send();
            apiResult = new CarNetApiResult(contentResponse);
            int code = contentResponse.getStatus();
            String response = contentResponse.getContentAsString().replaceAll("[\r\n\t]", "");
            responseHeaders = contentResponse.getHeaders();

            // validate response, API errors are reported as Json
            logger.trace("HTTP Response: {}", response);
            logger.trace("  Headers: \n{}", responseHeaders);
            String loc = getRedirect();
            switch (code) {
                case HttpStatus.FORBIDDEN_403:
                case HttpStatus.METHOD_NOT_ALLOWED_405:
                    throw new CarNetSecurityException("Forbidden", apiResult);
                case HttpStatus.OK_200:
                case HttpStatus.ACCEPTED_202:
                case HttpStatus.NO_CONTENT_204:
                case HttpStatus.SEE_OTHER_303:
                    return response; // valid
                case HttpStatus.MOVED_PERMANENTLY_301:
                case HttpStatus.TEMPORARY_REDIRECT_307:
                case HttpStatus.FOUND_302:
                    if (!loc.isEmpty()) {
                        logger.debug("HTTP {} -> {}", code, loc);
                        apiResult.location = loc;
                    }
                    break;
            }
            if (response.contains("\"error\":")) {
                throw new CarNetException("API returned error", apiResult);
            }
            if (response.isEmpty() && loc.isEmpty()) {
                throw new CarNetException("Invalid result received from API, maybe URL problem", apiResult);
            }
            return response;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            throw new CarNetException("API call failed!", new CarNetApiResult(request, e), e);
        }
    }

    private String request(HttpMethod method, String uri, String parms, Map<String, String> headers, String data,
            String pvin, String token) throws CarNetException {
        return request(method, uri, parms, headers, data, pvin, token, true);
    }

    /**
     * Get redirect location from http response headers
     *
     * @return
     */
    public String getRedirect() {
        String value = responseHeaders.get("Location");
        return value != null ? value : "";
    }

    public String getReferrer() {
        String value = responseHeaders.get("Referer");
        return value != null ? value : "";
    }

    /**
     * Get rresponse time from http response headers
     *
     * @return
     */
    public String getResponseDate() {
        String value = responseHeaders.get("Date");
        return value != null ? value : "";
    }

    /**
     * Constructs an URL from the stored information, a specified path and a specified argument string
     *
     */
    private String getBrandUrl(String uriTemplate, String args, String vin) throws CarNetException {
        if (config.api.brand.isEmpty()) {
            throw new CarNetException("Brand for API access not set");
        }
        String path = MessageFormat.format(uriTemplate, config.api.brand, config.api.xcountry, vin, config.user.id);
        if (!uriTemplate.contains("://")) { // not a full URL
            return getUrl(path.isEmpty() ? path : path + (!args.isEmpty() ? "?" + args : ""));
        } else {
            return path + (!args.isEmpty() ? "?" + args : "");
        }
    }

    /**
     * Fill standad http headers
     */
    public Map<String, String> fillAppHeaders(Map<String, String> headers, String token) throws CarNetException {
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
        headers.put(HttpHeader.AUTHORIZATION.toString(), "Bearer " + token);
        return headers;
    }

    /**
     * Fill http headers for token refresh request
     *
     * @return
     */
    public Map<String, String> fillRefreshHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(CNAPI_HEADER_APP, config.api.xappName);
        headers.put(CNAPI_HEADER_VERS, config.api.xappVersion);
        headers.put(HttpHeader.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded");
        headers.put("X-Client-Id", config.api.xClientId);
        return headers;
    }

    /**
     * Fill in POST data, set http headers
     *
     * @param request HTTP request structure
     * @param data POST data
     */
    private void fillPostData(Request request, String data) {
        if (!data.isEmpty()) {
            StringContentProvider postData;
            if (request.getHeaders().contains(HttpHeader.CONTENT_TYPE)) {
                String contentType = request.getHeaders().get(HttpHeader.CONTENT_TYPE);
                postData = new StringContentProvider(contentType, data, StandardCharsets.UTF_8);
            } else {
                boolean json = data.startsWith("{");
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
    private String getUrl(String path) throws CarNetException {
        String base = getBaseUrl();
        return base.endsWith("/") && path.startsWith("/") ? base + path : base + "/" + path;
    }

    /**
     * Build URL base depending on brand
     *
     * @return URL prefix/base url
     * @throws CarNetException
     */
    public String getBaseUrl() throws CarNetException {
        if (!config.vehicle.apiUrlPrefix.isEmpty()) {
            return config.vehicle.apiUrlPrefix;
        }
        if (!config.api.apiDefaultUrl.isEmpty()) {
            return config.api.apiDefaultUrl;
        }
        throw new CarNetException("Unknown brand for base URL");
    }

    /**
     * Get parameter value from URL string
     *
     * @param input URL string
     * @param parameter Parameter name
     * @return Extracted value
     */
    public static String getUrlParm(String input, String parameter) {
        return getUrlParm(input, parameter, "&");
    }

    public static String getUrlParm(String input, String parameter, String seperator) {
        String pattern = seperator + parameter + "=";
        if (input.contains(pattern)) {
            String res = substringAfter(input, pattern);
            return res.contains("&") ? substringBefore(res, "&") : res;
        }
        return "";
    }

    public static long parseDate(String timestamp) {
        ZonedDateTime zdt = ZonedDateTime.parse(timestamp, DateTimeFormatter.RFC_1123_DATE_TIME);
        return zdt.toInstant().toEpochMilli() * 1000; // return ms
    }

    /**
     * Encode fields for URL string
     *
     * @param s Field value
     * @return URL encoded value
     */
    public static String urlEncode(String s) {
        String url = UrlEncoded.encodeString(s, StandardCharsets.UTF_8); // returns forms data format
        url = url.replace("+", "%20");
        url = url.replace("%2D", "-");
        url = url.replace("%5F", "_");
        return url;
    }

    /**
     * Generate a NONCE value
     *
     * @return new NONCE
     */
    public static String generateNonce() {
        String dateTimeString = Long.toString(new Date().getTime());
        byte[] nonceBytes = dateTimeString.getBytes();
        return Base64.getEncoder().encodeToString(nonceBytes);
    }
}
