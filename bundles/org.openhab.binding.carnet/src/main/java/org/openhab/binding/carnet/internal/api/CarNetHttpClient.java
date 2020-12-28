/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.carnet.internal.CarNetUtils.*;
import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.*;

import java.net.MalformedURLException;
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
    private boolean nextRedirect = false;

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
        return request(HttpMethod.POST, uri, parms, headers, data, "", "");
    }

    public String post(String uri, Map<String, String> headers, String data) throws CarNetException {
        return request(HttpMethod.POST, uri, "", headers, data, "", "");
    }

    public String post(String uri, Map<String, String> headers, String data, String token) throws CarNetException {
        return request(HttpMethod.POST, uri, "", headers, data, "", token);
    }

    public String post(String uri, Map<String, String> headers, Map<String, String> data, boolean json)
            throws CarNetException {
        return request(HttpMethod.POST, uri, "", headers, buildPostData(data, json), "", "");
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
            String pvin, String token) throws CarNetException {
        Request request = null;
        String url = "";
        try {
            String vin = pvin.isEmpty() ? config.vehicle.vin : pvin;
            url = getBrandUrl(uri, parms, vin);
            CarNetApiResult apiResult = new CarNetApiResult(method.toString(), url);
            request = httpClient.newRequest(url).method(method).timeout(CNAPI_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            fillHttpHeaders(request, headers, token);
            fillPostData(request, data);

            // Do request and get response
            logger.debug("HTTP {} {}, data={}", request.getMethod(), request.getURI(), data);
            logger.trace("  Headers: {}", request.getHeaders());
            request.followRedirects(nextRedirect);
            nextRedirect = false;
            ContentResponse contentResponse = request.send();
            apiResult = new CarNetApiResult(contentResponse);
            int code = contentResponse.getStatus();
            String response = contentResponse.getContentAsString().replaceAll("\t", "").replaceAll("\r\n", "").trim();
            responseHeaders = contentResponse.getHeaders();

            // validate response, API errors are reported as Json
            logger.trace("HTTP Response: {}", response);
            logger.trace("  Headers: {}", responseHeaders);
            String loc = getRedirect();
            if (!loc.isEmpty()) {
                logger.debug("HTTP {} -> {}", code, loc);
            }
            if (code == HttpStatus.FORBIDDEN_403) {
                throw new CarNetSecurityException("Forbidden", apiResult);
            }
            if (response.contains("\"error\":")) {
                throw new CarNetException("API returned error", apiResult);
            }
            if ((code != HttpStatus.OK_200) && (code != HttpStatus.ACCEPTED_202) && (code != HttpStatus.NO_CONTENT_204)
                    && (code != HttpStatus.FOUND_302) && (code != HttpStatus.SEE_OTHER_303)) {
                throw new CarNetException("API Call failed (HTTP" + code + ")", apiResult);
            }
            if (response.isEmpty() && (code != HttpStatus.FOUND_302) && (code != HttpStatus.SEE_OTHER_303)) {
                throw new CarNetException("Invalid result received from API, maybe URL problem", apiResult);
            }
            return response;
        } catch (ExecutionException | InterruptedException | TimeoutException | MalformedURLException e) {
            throw new CarNetException("API call failed!", new CarNetApiResult(request, e), e);
        }
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
    private String getBrandUrl(String uriTemplate, String args, String vin) throws MalformedURLException {
        String path = MessageFormat.format(uriTemplate, config.account.brand, config.account.country, vin,
                config.user.id);
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
        headers.put(HttpHeader.USER_AGENT.toString(), CNAPI_HEADER_USER_AGENT);
        headers.put(CNAPI_HEADER_APP, config.xappName);
        headers.put(CNAPI_HEADER_VERS, config.xappVersion);
        headers.put(HttpHeader.ACCEPT.toString(), CNAPI_ACCEPTT_JSON);
        headers.put(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString());
        headers.put(HttpHeader.AUTHORIZATION.toString(), "Bearer " + token);
        headers.put("X-Country-Id", "DE");
        headers.put("X-Language-Id", "de");
        return headers;
    }

    /**
     * Fill http headers for SOAP requests
     */
    public static Map<String, String> fillActionHeaders(Map<String, String> headers, String contentType, String token,
            String securityToken) throws CarNetException {
        // "User-Agent": "okhttp/3.7.0",
        // "Host": "msg.volkswagen.de",
        // "X-App-Version": "3.14.0",
        // "X-App-Name": "myAudi",
        // "Authorization": "Bearer " + self.vwToken.get("access_token"),
        // "Accept-charset": "UTF-8",
        // "Content-Type": content_type,
        // "Accept": "application/json,
        // application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml,application/vnd.volkswagenag.com-error-v1+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,
        // application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml,
        // application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteLockUnlock_v1_0_0+xml,*/*","
        headers.put(HttpHeader.USER_AGENT.toString(), "okhttp/3.7.0)");
        headers.put(CNAPI_HEADER_APP, CNAPI_HEADER_APP_MYAUDI);
        headers.put(CNAPI_HEADER_VERS, "3.14.0");
        if (!contentType.isEmpty()) {
            headers.put(HttpHeader.CONTENT_TYPE.toString(), contentType);
        }
        headers.put(HttpHeader.ACCEPT.toString(),
                "application/json, application/vnd.vwg.mbb.ChargerAction_v1_0_0+xml,application/vnd.volkswagenag.com-error-v1+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteStandheizung_v2_0_0+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,application/vnd.vwg.mbb.RemoteLockUnlock_v1_0_0+xml,application/vnd.vwg.mbb.operationList_v3_0_2+xml,application/vnd.vwg.mbb.genericError_v1_0_2+xml,*/*");
        headers.put(HttpHeader.ACCEPT_CHARSET.toString(), StandardCharsets.UTF_8.toString());

        headers.put(HttpHeader.AUTHORIZATION.toString(), "Bearer " + token);
        headers.put(HttpHeader.HOST.toString(), "msg.volkswagen.de");
        if (!securityToken.isEmpty()) {
            headers.put("x-mbbSecToken", securityToken);
        }
        return headers;
    }

    /**
     * Fill http headers for token refresh request
     *
     * @return
     */
    public Map<String, String> fillRefreshHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeader.USER_AGENT.toString(), "okhttp/3.7.0");
        headers.put(CNAPI_HEADER_APP, config.xappName);
        headers.put(CNAPI_HEADER_VERS, config.xappVersion);
        headers.put(HttpHeader.CONTENT_TYPE.toString(), "application/x-www-form-urlencoded");
        headers.put("X-Client-Id", config.xClientId);
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
                postData = new StringContentProvider(json ? CNAPI_ACCEPTT_JSON : CNAPI_CONTENTT_FORM_URLENC, data,
                        StandardCharsets.UTF_8);
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
    private String getUrl(String path) throws MalformedURLException {
        String base = getBaseUrl();
        return base.endsWith("/") && path.startsWith("/") ? base + path : base + "/" + path;
    }

    /**
     * Build URL base depending on brand
     *
     * @return URL prefix/base url
     * @throws MalformedURLException
     */
    private String getBaseUrl() throws MalformedURLException {
        if (config.account.brand.equalsIgnoreCase(CNAPI_BRAND_AUDI)) {
            return CNAPI_BASE_URL_AUDI;
        }
        if (config.account.brand.equalsIgnoreCase(CNAPI_BRAND_VW)) {
            return CNAPI_BASE_URL_VW;
        }
        throw new MalformedURLException("Unknown brand for base URL");
    }

    /**
     * Get parameter value from URL string
     *
     * @param input URL string
     * @param parameter Parameter name
     * @return Extracted value
     */
    public static String getUrlParm(String input, String parameter) {
        String pattern = "&" + parameter + "=";
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
