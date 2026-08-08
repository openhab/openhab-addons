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
package org.openhab.binding.rachio.internal.api;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.getString;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RachioHttp} implements the http-based REST API to access the Rachio Cloud
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioHttp {
    private final Logger logger = LoggerFactory.getLogger(RachioHttp.class);
    private static final String HTTP_METHOD_DELETE = "DELETE";
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_PUT = "PUT";
    private static final Pattern URL_USERINFO_PATTERN = Pattern
            .compile("(?i)\\b([a-z][a-z0-9+.-]*://)([^\\s\"'<>/]*@)");
    private static final Pattern ESCAPED_URL_USERINFO_PATTERN = Pattern
            .compile("(?i)\\b([a-z][a-z0-9+.-]*:\\\\/\\\\/)([^\\s\"'<>/\\\\]*@)");
    private static final Pattern AUTHORIZATION_PATTERN = Pattern
            .compile("(?i)(authorization\\s*[=:]\\s*(?:bearer\\s+)?)([^\\s,;\"'}]+)");
    private static final Pattern AUTHORIZATION_JSON_PATTERN = Pattern
            .compile("(?i)(\"authorization\"\\s*:\\s*\"(?:bearer\\s+)?)([^\"]+)(\")");
    private static final Pattern BEARER_PATTERN = Pattern.compile("(?i)(bearer\\s+)([A-Za-z0-9._~+/=-]+)");
    private static final Pattern API_KEY_JSON_PATTERN = Pattern
            .compile("(?i)(\"(?:api[-_]?key|apikey)\"\\s*:\\s*\")([^\"]+)(\")");

    private final AtomicInteger apiCalls = new AtomicInteger();
    private final HttpClient httpClient;
    private final String apikey;

    /**
     * Constructor for the Rachio API class to create a connection to the Rachio cloud service.
     *
     * @param httpClient shared HTTP client managed by openHAB core
     * @param key Rachio API Access token (see Web UI)
     */
    public RachioHttp(HttpClient httpClient, String key) {
        this.httpClient = httpClient;
        this.apikey = key;
    }

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
     * @return RachioApiResult including GET response, http code etc.
     * @throws RachioApiException if something went wrong (e.g. unable to connect)
     */
    public RachioApiResult httpGet(String url, @Nullable String urlParameters) throws RachioApiException {
        return httpRequest(HTTP_METHOD_GET, url, urlParameters, null);
    }

    /**
     * Given a URL, send a HTTP PUT request to the URL.
     *
     * @param url The URL to send a PUT request to.
     * @param putData Request body to send with the PUT request.
     * @return RachioApiResult including PUT response, http code etc.
     * @throws RachioApiException if something went wrong (e.g. unable to connect)
     */
    public RachioApiResult httpPut(String url, String putData) throws RachioApiException {
        return httpRequest(HTTP_METHOD_PUT, url, null, putData);
    }

    /**
     * Given a URL and a set parameters, send a HTTP POST request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a POST request to.
     * @param postData List of parameters to use in the URL for the POST request. Null if no parameters.
     * @return RachioApiResult including GET response, http code etc.
     * @throws RachioApiException if something went wrong (e.g. unable to connect)
     */
    public RachioApiResult httpPost(String url, String postData) throws RachioApiException {
        return httpRequest(HTTP_METHOD_POST, url, null, postData);
    }

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
     * @return RachioApiResult including GET response, http code etc.
     * @throws RachioApiException if something went wrong (e.g. unable to connect)
     */
    public RachioApiResult httpDelete(String url, @Nullable String urlParameters) throws RachioApiException {
        return httpRequest(HTTP_METHOD_DELETE, url, urlParameters, null);
    }

    /**
     * Given a URL and a set parameters, send a HTTP GET request to the URL location created by the URL and parameters.
     *
     * @param url The URL to send a GET request to.
     * @param urlParameters List of parameters to use in the URL for the GET request. Null if no parameters.
     * @return RachioApiResult including GET response, http code etc.
     * @throws RachioApiException if something went wrong (e.g. unable to connect)
     */
    protected RachioApiResult httpRequest(String method, String url, @Nullable String urlParameters,
            @Nullable String reqDatas) throws RachioApiException {
        RachioApiResult result = new RachioApiResult();
        try {
            int apiCall = apiCalls.incrementAndGet();

            URI location = URI.create(urlParameters != null ? url + "?" + urlParameters : url);
            result.requestMethod = method;
            result.url = location.toString();
            result.apiCalls = apiCall;

            Request request = httpClient.newRequest(location).method(method)
                    .timeout(HTTP_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .header(HttpHeader.USER_AGENT, SERVLET_WEBHOOK_USER_AGENT);
            if (!apikey.isEmpty()) {
                request.header(HttpHeader.AUTHORIZATION, "Bearer " + apikey);
            }
            if (method.equals(HTTP_METHOD_PUT) || method.equals(HTTP_METHOD_POST)) {
                request.content(new StringContentProvider(reqDatas != null ? reqDatas : "", StandardCharsets.UTF_8),
                        SERVLET_WEBHOOK_APPLICATION_JSON);
            } else {
                request.header(HttpHeader.CONTENT_TYPE, SERVLET_WEBHOOK_APPLICATION_JSON);
            }
            logger.trace("RachioHttp[Call #{}]: Call Rachio cloud service: {} '{}'", apiCall, request.getMethod(),
                    sanitizeForLogging(result.url));

            ContentResponse response = request.send();
            result.responseCode = response.getStatus();
            String rateLimit = response.getHeaders().get(RACHIO_JSON_RATE_LIMIT);
            if (rateLimit != null) {
                result.setRateLimit(rateLimit, response.getHeaders().get(RACHIO_JSON_RATE_REMAINING),
                        response.getHeaders().get(RACHIO_JSON_RATE_RESET));
                if (result.isRateLimitBlocked()) {
                    String message = MessageFormat.format(
                            "RachioHttp: Critical API rate limit: {0} / {1}, reset at {2}", result.rateRemaining,
                            result.rateLimit, result.rateReset);
                    throw new RachioApiException(message, result);
                }
            }

            String responseBody = new String(response.getContent(), StandardCharsets.UTF_8);
            if (result.responseCode < HttpStatus.OK_200 || result.responseCode >= HttpStatus.MULTIPLE_CHOICES_300) {
                String errorResponse = responseBody;
                result.resultString = "responseLength=" + errorResponse.length();
                String message = MessageFormat.format(
                        "RachioHttp: Error sending HTTP {0} request to {1} - http response code={2}, responseLength={3}",
                        request.getMethod(), sanitizeForLogging(result.url), result.responseCode,
                        errorResponse.length());
                throw new RachioApiException(message, result);
            }

            result.resultString = responseBody;
            logger.trace("RachioHttp: {} {} - responseLength={}", request.getMethod(), sanitizeForLogging(url),
                    result.resultString.length());

            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.resultString = sanitizeForLogging(getString(e.toString()));
            throw new RachioApiException(result.resultString, result, e);
        } catch (ExecutionException e) {
            Throwable cause = e;
            @Nullable
            Throwable executionCause = e.getCause();
            if (executionCause != null) {
                cause = executionCause;
            }
            result.resultString = sanitizeForLogging(getString(cause.toString()));
            throw new RachioApiException(result.resultString, result, cause);
        } catch (TimeoutException | RuntimeException e) {
            result.resultString = sanitizeForLogging(getString(e.toString()));
            throw new RachioApiException(result.resultString, result, e);
        }
    }

    static String sanitizeForLogging(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        String sanitized = URL_USERINFO_PATTERN.matcher(value).replaceAll("$1***:***@");
        sanitized = ESCAPED_URL_USERINFO_PATTERN.matcher(sanitized).replaceAll("$1***:***@");
        sanitized = AUTHORIZATION_PATTERN.matcher(sanitized).replaceAll("$1[redacted]");
        sanitized = AUTHORIZATION_JSON_PATTERN.matcher(sanitized).replaceAll("$1[redacted]$3");
        sanitized = BEARER_PATTERN.matcher(sanitized).replaceAll("$1[redacted]");
        sanitized = API_KEY_JSON_PATTERN.matcher(sanitized).replaceAll("$1[redacted]$3");
        return sanitizeKnownApiKey(sanitized);
    }

    private static String sanitizeKnownApiKey(String value) {
        return value.replaceAll("(?i)((?:api[-_]?key|apikey|access_token)=)([^&\\s\"'}]+)", "$1[redacted]");
    }
}
