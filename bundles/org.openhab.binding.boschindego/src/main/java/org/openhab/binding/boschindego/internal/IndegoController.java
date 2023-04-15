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
package org.openhab.binding.boschindego.internal;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.boschindego.internal.dto.response.ErrorResponse;
import org.openhab.binding.boschindego.internal.dto.response.Mower;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoInvalidCommandException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoInvalidResponseException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoTimeoutException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.library.types.RawType;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Controller for communicating with a Bosch Indego services.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoController {

    protected static final String SERIAL_NUMBER_SUBPATH = "alms/";

    private static final String BASE_URL = "https://api.indego-cloud.iot.bosch-si.com/api/v1/";
    private static final String CONTENT_TYPE_HEADER = "application/json";

    private static final String BEARER = "Bearer ";

    private final Logger logger = LoggerFactory.getLogger(IndegoController.class);
    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final OAuthClientService oAuthClientService;
    private final String userAgent;

    /**
     * Initialize the controller instance.
     * 
     * @param httpClient the HttpClient for communicating with the service
     * @param oAuthClientService the OAuthClientService for authorization
     */
    public IndegoController(HttpClient httpClient, OAuthClientService oAuthClientService) {
        this.httpClient = httpClient;
        this.oAuthClientService = oAuthClientService;
        userAgent = "openHAB " + FrameworkUtil.getBundle(this.getClass()).getVersion().toString();
    }

    /**
     * Gets serial numbers of all the associated Indego devices.
     *
     * @return the serial numbers of the devices
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public Collection<String> getSerialNumbers() throws IndegoAuthenticationException, IndegoException {
        Mower[] mowers = getRequest(SERIAL_NUMBER_SUBPATH, Mower[].class);

        return Arrays.stream(mowers).map(m -> m.serialNumber).toList();
    }

    private String getAuthorizationUrl() {
        try {
            return oAuthClientService.getAuthorizationUrl(BSK_REDIRECT_URI, BSK_SCOPE, null);
        } catch (OAuthException e) {
            return "";
        }
    }

    private String getAuthorizationHeader() throws IndegoException {
        final AccessTokenResponse accessTokenResponse;
        try {
            accessTokenResponse = oAuthClientService.getAccessTokenResponse();
        } catch (OAuthException | OAuthResponseException e) {
            logger.debug("Error fetching access token: {}", e.getMessage(), e);
            throw new IndegoAuthenticationException(
                    "Error fetching access token. Invalid authcode? Please generate a new one -> "
                            + getAuthorizationUrl(),
                    e);
        } catch (IOException e) {
            throw new IndegoException("An unexpected IOException occurred: " + e.getMessage(), e);
        }
        if (accessTokenResponse == null || accessTokenResponse.getAccessToken() == null
                || accessTokenResponse.getAccessToken().isEmpty()) {
            throw new IndegoAuthenticationException(
                    "No access token. Is this thing authorized? -> " + getAuthorizationUrl());
        }
        if (accessTokenResponse.getRefreshToken() == null || accessTokenResponse.getRefreshToken().isEmpty()) {
            throw new IndegoAuthenticationException("No refresh token. Please reauthorize -> " + getAuthorizationUrl());
        }

        return BEARER + accessTokenResponse.getAccessToken();
    }

    /**
     * Sends a GET request to the server and returns the deserialized JSON response.
     * 
     * @param path the relative path to which the request should be sent
     * @param dtoClass the DTO class to which the JSON result should be deserialized
     * @return the deserialized DTO from the JSON response
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoTimeoutException if device cannot be reached (gateway timeout error)
     * @throws IndegoException if any communication or parsing error occurred
     */
    protected <T> T getRequest(String path, Class<? extends T> dtoClass)
            throws IndegoAuthenticationException, IndegoTimeoutException, IndegoException {
        int status = 0;
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.GET)
                    .header(HttpHeader.AUTHORIZATION, getAuthorizationHeader()).agent(userAgent);
            if (logger.isTraceEnabled()) {
                logger.trace("GET request for {}", BASE_URL + path);
            }
            ContentResponse response = sendRequest(request);
            status = response.getStatus();
            String jsonResponse = response.getContentAsString();
            if (!jsonResponse.isEmpty()) {
                logger.trace("JSON response: '{}'", jsonResponse);
            }
            if (status == HttpStatus.UNAUTHORIZED_401) {
                // This will currently not happen because "WWW-Authenticate" header is missing; see below.
                throw new IndegoAuthenticationException("Unauthorized");
            }
            if (status == HttpStatus.GATEWAY_TIMEOUT_504) {
                throw new IndegoTimeoutException("Gateway timeout");
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoException("The request failed with error: " + status);
            }
            if (jsonResponse.isEmpty()) {
                throw new IndegoInvalidResponseException("No content returned", status);
            }

            @Nullable
            T result = gson.fromJson(jsonResponse, dtoClass);
            if (result == null) {
                throw new IndegoInvalidResponseException("Parsed response is null", status);
            }
            return result;
        } catch (JsonParseException e) {
            throw new IndegoInvalidResponseException("Error parsing response", e, status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException e) {
            throw new IndegoException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof HttpResponseException) {
                Response response = ((HttpResponseException) cause).getResponse();
                if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    /*
                     * The service may respond with HTTP code 401 without any "WWW-Authenticate"
                     * header, violating RFC 7235. Jetty will then throw HttpResponseException.
                     * We need to handle this in order to attempt reauthentication.
                     */
                    throw new IndegoAuthenticationException("Unauthorized", e);
                }
            }
            throw new IndegoException(e);
        }
    }

    /**
     * Sends a GET request to the server and returns the raw response.
     * 
     * @param path the relative path to which the request should be sent
     * @return the raw data from the response
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    protected RawType getRawRequest(String path) throws IndegoAuthenticationException, IndegoException {
        int status = 0;
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.GET)
                    .header(HttpHeader.AUTHORIZATION, getAuthorizationHeader()).agent(userAgent);
            if (logger.isTraceEnabled()) {
                logger.trace("GET request for {}", BASE_URL + path);
            }
            ContentResponse response = sendRequest(request);
            status = response.getStatus();
            if (status == HttpStatus.UNAUTHORIZED_401) {
                // This will currently not happen because "WWW-Authenticate" header is missing; see below.
                throw new IndegoAuthenticationException("Context rejected");
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoException("The request failed with error: " + status);
            }
            byte[] data = response.getContent();
            if (data == null) {
                throw new IndegoInvalidResponseException("No data returned", status);
            }
            String contentType = response.getMediaType();
            if (contentType == null || contentType.isEmpty()) {
                throw new IndegoInvalidResponseException("No content-type returned", status);
            }
            logger.debug("Media download response: type {}, length {}", contentType, data.length);

            return new RawType(data, contentType);
        } catch (JsonParseException e) {
            throw new IndegoInvalidResponseException("Error parsing response", e, status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException e) {
            throw new IndegoException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof HttpResponseException) {
                Response response = ((HttpResponseException) cause).getResponse();
                if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    /*
                     * When contextId is not valid, the service will respond with HTTP code 401 without
                     * any "WWW-Authenticate" header, violating RFC 7235. Jetty will then throw
                     * HttpResponseException. We need to handle this in order to attempt
                     * reauthentication.
                     */
                    throw new IndegoAuthenticationException("Context rejected", e);
                }
            }
            throw new IndegoException(e);
        }
    }

    /**
     * Wraps {@link #putPostRequest(HttpMethod, String, Object)} into an authenticated session.
     * 
     * @param path the relative path to which the request should be sent
     * @param requestDto the DTO which should be sent to the server as JSON
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    protected void putRequestWithAuthentication(String path, Object requestDto)
            throws IndegoAuthenticationException, IndegoException {
        putPostRequest(HttpMethod.PUT, path, requestDto);
    }

    /**
     * Wraps {@link #putPostRequest(HttpMethod, String, Object)} into an authenticated session.
     * 
     * @param path the relative path to which the request should be sent
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    protected void postRequest(String path) throws IndegoAuthenticationException, IndegoException {
        putPostRequest(HttpMethod.POST, path, null);
    }

    /**
     * Sends a PUT/POST request to the server.
     * 
     * @param method the type of request ({@link HttpMethod.PUT} or {@link HttpMethod.POST})
     * @param path the relative path to which the request should be sent
     * @param requestDto the DTO which should be sent to the server as JSON
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    protected void putPostRequest(HttpMethod method, String path, @Nullable Object requestDto)
            throws IndegoAuthenticationException, IndegoException {
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(method)
                    .header(HttpHeader.AUTHORIZATION, getAuthorizationHeader())
                    .header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_HEADER).agent(userAgent);
            if (requestDto != null) {
                String payload = gson.toJson(requestDto);
                request.content(new StringContentProvider(payload));
                if (logger.isTraceEnabled()) {
                    logger.trace("{} request for {} with payload '{}'", method, BASE_URL + path, payload);
                }
            } else {
                logger.trace("{} request for {} with no payload", method, BASE_URL + path);
            }
            ContentResponse response = sendRequest(request);
            String jsonResponse = response.getContentAsString();
            if (!jsonResponse.isEmpty()) {
                logger.trace("JSON response: '{}'", jsonResponse);
            }
            int status = response.getStatus();
            if (status == HttpStatus.UNAUTHORIZED_401) {
                // This will currently not happen because "WWW-Authenticate" header is missing; see below.
                throw new IndegoAuthenticationException("Context rejected");
            }
            if (status == HttpStatus.INTERNAL_SERVER_ERROR_500) {
                try {
                    ErrorResponse result = gson.fromJson(jsonResponse, ErrorResponse.class);
                    if (result != null) {
                        throw new IndegoInvalidCommandException("The request failed with HTTP error: " + status,
                                result.error);
                    }
                } catch (JsonParseException e) {
                    // Ignore missing error code, next line will throw.
                }
                throw new IndegoInvalidCommandException("The request failed with HTTP error: " + status);
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoException("The request failed with error: " + status);
            }
        } catch (JsonParseException e) {
            throw new IndegoException("Error serializing request", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException e) {
            throw new IndegoException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof HttpResponseException) {
                Response response = ((HttpResponseException) cause).getResponse();
                if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                    /*
                     * When contextId is not valid, the service will respond with HTTP code 401 without
                     * any "WWW-Authenticate" header, violating RFC 7235. Jetty will then throw
                     * HttpResponseException. We need to handle this in order to attempt
                     * reauthentication.
                     */
                    throw new IndegoAuthenticationException("Context rejected", e);
                }
            }
            throw new IndegoException(e);
        }
    }

    /**
     * Send request. This method exists for the purpose of avoiding multiple calls to
     * the server at the same time.
     * 
     * @param request the {@link Request} to send
     * @return a {@link ContentResponse} for this request
     * @throws InterruptedException if send thread is interrupted
     * @throws TimeoutException if send times out
     * @throws ExecutionException if execution fails
     */
    protected synchronized ContentResponse sendRequest(Request request)
            throws InterruptedException, TimeoutException, ExecutionException {
        return request.send();
    }
}
