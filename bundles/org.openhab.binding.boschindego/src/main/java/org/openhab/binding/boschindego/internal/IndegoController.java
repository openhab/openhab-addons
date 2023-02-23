/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
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
import org.openhab.binding.boschindego.internal.dto.DeviceCommand;
import org.openhab.binding.boschindego.internal.dto.PredictiveAdjustment;
import org.openhab.binding.boschindego.internal.dto.PredictiveStatus;
import org.openhab.binding.boschindego.internal.dto.request.AuthenticationRequest;
import org.openhab.binding.boschindego.internal.dto.request.SetStateRequest;
import org.openhab.binding.boschindego.internal.dto.response.AuthenticationResponse;
import org.openhab.binding.boschindego.internal.dto.response.DeviceCalendarResponse;
import org.openhab.binding.boschindego.internal.dto.response.DeviceStateResponse;
import org.openhab.binding.boschindego.internal.dto.response.ErrorResponse;
import org.openhab.binding.boschindego.internal.dto.response.LocationWeatherResponse;
import org.openhab.binding.boschindego.internal.dto.response.OperatingDataResponse;
import org.openhab.binding.boschindego.internal.dto.response.PredictiveLastCuttingResponse;
import org.openhab.binding.boschindego.internal.dto.response.PredictiveNextCuttingResponse;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoInvalidCommandException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoInvalidResponseException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoTimeoutException;
import org.openhab.core.library.types.RawType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Controller for communicating with a Bosch Indego device through Bosch services.
 * This class provides methods for retrieving state information as well as controlling
 * the device.
 * 
 * The implementation is based on zazaz-de/iot-device-bosch-indego-controller, but
 * rewritten from scratch to use Jetty HTTP client for HTTP communication and GSON for
 * JSON parsing. Thanks to Oliver Schünemann for providing the original implementation.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoController {

    private static final String BASE_URL = "https://api.indego.iot.bosch-si.com/api/v1/";
    private static final URI BASE_URI = URI.create(BASE_URL);
    private static final String SERIAL_NUMBER_SUBPATH = "alms/";
    private static final String SSO_COOKIE_NAME = "BOSCH_INDEGO_SSO";
    private static final String CONTEXT_HEADER_NAME = "x-im-context-id";
    private static final String CONTENT_TYPE_HEADER = "application/json";

    private final Logger logger = LoggerFactory.getLogger(IndegoController.class);
    private final String basicAuthenticationHeader;
    private final Gson gson = new Gson();
    private final HttpClient httpClient;

    private IndegoSession session = new IndegoSession();

    /**
     * Initialize the controller instance.
     * 
     * @param username the username for authenticating
     * @param password the password
     */
    public IndegoController(HttpClient httpClient, String username, String password) {
        this.httpClient = httpClient;
        basicAuthenticationHeader = "Basic "
                + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    /**
     * Authenticate with server and store session context and serial number.
     * 
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    private void authenticate() throws IndegoAuthenticationException, IndegoException {
        int status = 0;
        try {
            Request request = httpClient.newRequest(BASE_URL + "authenticate").method(HttpMethod.POST)
                    .header(HttpHeader.AUTHORIZATION, basicAuthenticationHeader);

            AuthenticationRequest authRequest = new AuthenticationRequest();
            authRequest.device = "";
            authRequest.osType = "Android";
            authRequest.osVersion = "4.0";
            authRequest.deviceManufacturer = "unknown";
            authRequest.deviceType = "unknown";
            String json = gson.toJson(authRequest);
            request.content(new StringContentProvider(json));
            request.header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_HEADER);

            if (logger.isTraceEnabled()) {
                logger.trace("POST request for {}", BASE_URL + "authenticate");
            }

            ContentResponse response = sendRequest(request);
            status = response.getStatus();
            if (status == HttpStatus.UNAUTHORIZED_401) {
                throw new IndegoAuthenticationException("Authentication was rejected");
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoAuthenticationException("The request failed with HTTP error: " + status);
            }

            String jsonResponse = response.getContentAsString();
            if (jsonResponse.isEmpty()) {
                throw new IndegoInvalidResponseException("No content returned", status);
            }
            logger.trace("JSON response: '{}'", jsonResponse);

            AuthenticationResponse authenticationResponse = gson.fromJson(jsonResponse, AuthenticationResponse.class);
            if (authenticationResponse == null) {
                throw new IndegoInvalidResponseException("Response could not be parsed as AuthenticationResponse",
                        status);
            }
            session = new IndegoSession(authenticationResponse.contextId, authenticationResponse.serialNumber,
                    getContextExpirationTimeFromCookie());
            logger.debug("Initialized session {}", session);
        } catch (JsonParseException e) {
            throw new IndegoInvalidResponseException("Error parsing AuthenticationResponse", e, status);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException | ExecutionException e) {
            throw new IndegoException(e);
        }
    }

    /**
     * Get context expiration time as a calculated {@link Instant} relative to now.
     * The information is obtained from max age in the Bosch Indego SSO cookie.
     * Please note that this cookie is only sent initially when authenticating, so
     * the value will not be subject to any updates.
     * 
     * @return expiration time as {@link Instant} or {@link Instant#MIN} if not present
     */
    private Instant getContextExpirationTimeFromCookie() {
        return httpClient.getCookieStore().get(BASE_URI).stream().filter(c -> SSO_COOKIE_NAME.equals(c.getName()))
                .findFirst().map(c -> {
                    return Instant.now().plusSeconds(c.getMaxAge());
                }).orElseGet(() -> {
                    return Instant.MIN;
                });
    }

    /**
     * Deauthenticate session. This method should be called as part of cleanup to reduce
     * lingering sessions. This can potentially avoid killed sessions in situation with
     * multiple clients (e.g. openHAB and mobile app) if restrictions on concurrent
     * number of sessions would be put on the service.
     *
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void deauthenticate() throws IndegoException {
        if (session.isValid()) {
            deleteRequest("authenticate");
            session.invalidate();
        }
    }

    /**
     * Wraps {@link #getRequest(String, Class)} into an authenticated session.
     *
     * @param path the relative path to which the request should be sent
     * @param dtoClass the DTO class to which the JSON result should be deserialized
     * @return the deserialized DTO from the JSON response
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoTimeoutException if device cannot be reached (gateway timeout error)
     * @throws IndegoException if any communication or parsing error occurred
     */
    private <T> T getRequestWithAuthentication(String path, Class<? extends T> dtoClass)
            throws IndegoAuthenticationException, IndegoTimeoutException, IndegoException {
        if (!session.isValid()) {
            authenticate();
        }
        try {
            logger.debug("Session {} valid, skipping authentication", session);
            return getRequest(path, dtoClass);
        } catch (IndegoAuthenticationException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Context rejected", e);
            } else {
                logger.debug("Context rejected: {}", e.getMessage());
            }
            session.invalidate();
            authenticate();
            return getRequest(path, dtoClass);
        }
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
    private <T> T getRequest(String path, Class<? extends T> dtoClass)
            throws IndegoAuthenticationException, IndegoTimeoutException, IndegoException {
        int status = 0;
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.GET).header(CONTEXT_HEADER_NAME,
                    session.getContextId());
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
                throw new IndegoAuthenticationException("Context rejected");
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
     * Wraps {@link #getRawRequest(String)} into an authenticated session.
     *
     * @param path the relative path to which the request should be sent
     * @return the raw data from the response
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    private RawType getRawRequestWithAuthentication(String path) throws IndegoAuthenticationException, IndegoException {
        if (!session.isValid()) {
            authenticate();
        }
        try {
            logger.debug("Session {} valid, skipping authentication", session);
            return getRawRequest(path);
        } catch (IndegoAuthenticationException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Context rejected", e);
            } else {
                logger.debug("Context rejected: {}", e.getMessage());
            }
            session.invalidate();
            authenticate();
            return getRawRequest(path);
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
    private RawType getRawRequest(String path) throws IndegoAuthenticationException, IndegoException {
        int status = 0;
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.GET).header(CONTEXT_HEADER_NAME,
                    session.getContextId());
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
    private void putRequestWithAuthentication(String path, Object requestDto)
            throws IndegoAuthenticationException, IndegoException {
        if (!session.isValid()) {
            authenticate();
        }
        try {
            logger.debug("Session {} valid, skipping authentication", session);
            putPostRequest(HttpMethod.PUT, path, requestDto);
        } catch (IndegoAuthenticationException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Context rejected", e);
            } else {
                logger.debug("Context rejected: {}", e.getMessage());
            }
            session.invalidate();
            authenticate();
            putPostRequest(HttpMethod.PUT, path, requestDto);
        }
    }

    /**
     * Wraps {@link #putPostRequest(HttpMethod, String, Object)} into an authenticated session.
     * 
     * @param path the relative path to which the request should be sent
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    private void postRequestWithAuthentication(String path) throws IndegoAuthenticationException, IndegoException {
        if (!session.isValid()) {
            authenticate();
        }
        try {
            logger.debug("Session {} valid, skipping authentication", session);
            putPostRequest(HttpMethod.POST, path, null);
        } catch (IndegoAuthenticationException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Context rejected", e);
            } else {
                logger.debug("Context rejected: {}", e.getMessage());
            }
            session.invalidate();
            authenticate();
            putPostRequest(HttpMethod.POST, path, null);
        }
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
    private void putPostRequest(HttpMethod method, String path, @Nullable Object requestDto)
            throws IndegoAuthenticationException, IndegoException {
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(method)
                    .header(CONTEXT_HEADER_NAME, session.getContextId())
                    .header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_HEADER);
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
     * Sends a DELETE request to the server.
     * 
     * @param path the relative path to which the request should be sent
     * @throws IndegoException if any communication or parsing error occurred
     */
    private void deleteRequest(String path) throws IndegoException {
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.DELETE)
                    .header(CONTEXT_HEADER_NAME, session.getContextId());
            if (logger.isTraceEnabled()) {
                logger.trace("DELETE request for {}", BASE_URL + path);
            }
            ContentResponse response = sendRequest(request);
            int status = response.getStatus();
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoException("The request failed with error: " + status);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException | ExecutionException e) {
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
    private synchronized ContentResponse sendRequest(Request request)
            throws InterruptedException, TimeoutException, ExecutionException {
        return request.send();
    }

    /**
     * Gets serial number of the associated Indego device
     *
     * @return the serial number of the device
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public synchronized String getSerialNumber() throws IndegoAuthenticationException, IndegoException {
        if (!session.isInitialized()) {
            logger.debug("Session not yet initialized when serial number was requested; authenticating...");
            authenticate();
        }
        return session.getSerialNumber();
    }

    /**
     * Queries the device state from the server.
     * 
     * @return the device state
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceStateResponse getState() throws IndegoAuthenticationException, IndegoException {
        return getRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/state",
                DeviceStateResponse.class);
    }

    /**
     * Queries the device state from the server. This overload will return when the state
     * has changed, or the timeout has been reached.
     * 
     * @param timeout Maximum time to wait for response
     * @return the device state
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceStateResponse getState(Duration timeout) throws IndegoAuthenticationException, IndegoException {
        return getRequestWithAuthentication(
                SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/state?longpoll=true&timeout=" + timeout.getSeconds(),
                DeviceStateResponse.class);
    }

    /**
     * Queries the device operating data from the server.
     * Server will request this directly from the device, so operation might be slow.
     * 
     * @return the device state
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoTimeoutException if device cannot be reached (gateway timeout error)
     * @throws IndegoException if any communication or parsing error occurred
     */
    public OperatingDataResponse getOperatingData()
            throws IndegoAuthenticationException, IndegoTimeoutException, IndegoException {
        return getRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/operatingData",
                OperatingDataResponse.class);
    }

    /**
     * Queries the map generated by the device from the server.
     * 
     * @return the garden map
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public RawType getMap() throws IndegoAuthenticationException, IndegoException {
        return getRawRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/map");
    }

    /**
     * Queries the calendar.
     * 
     * @return the calendar
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceCalendarResponse getCalendar() throws IndegoAuthenticationException, IndegoException {
        DeviceCalendarResponse calendar = getRequestWithAuthentication(
                SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/calendar", DeviceCalendarResponse.class);
        return calendar;
    }

    /**
     * Sends a command to the Indego device.
     * 
     * @param command the control command to send to the device
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoInvalidCommandException if the command was not processed correctly
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void sendCommand(DeviceCommand command)
            throws IndegoAuthenticationException, IndegoInvalidCommandException, IndegoException {
        SetStateRequest request = new SetStateRequest();
        request.state = command.getActionCode();
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/state", request);
    }

    /**
     * Queries the predictive weather forecast.
     * 
     * @return the weather forecast DTO
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public LocationWeatherResponse getWeather() throws IndegoAuthenticationException, IndegoException {
        return getRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive/weather",
                LocationWeatherResponse.class);
    }

    /**
     * Queries the predictive adjustment.
     * 
     * @return the predictive adjustment
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public int getPredictiveAdjustment() throws IndegoAuthenticationException, IndegoException {
        return getRequestWithAuthentication(
                SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive/useradjustment",
                PredictiveAdjustment.class).adjustment;
    }

    /**
     * Sets the predictive adjustment.
     * 
     * @param adjust the predictive adjustment
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void setPredictiveAdjustment(final int adjust) throws IndegoAuthenticationException, IndegoException {
        final PredictiveAdjustment adjustment = new PredictiveAdjustment();
        adjustment.adjustment = adjust;
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive/useradjustment",
                adjustment);
    }

    /**
     * Queries predictive moving.
     * 
     * @return predictive moving
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public boolean getPredictiveMoving() throws IndegoAuthenticationException, IndegoException {
        return getRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive",
                PredictiveStatus.class).enabled;
    }

    /**
     * Sets predictive moving.
     * 
     * @param enable
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void setPredictiveMoving(final boolean enable) throws IndegoAuthenticationException, IndegoException {
        final PredictiveStatus status = new PredictiveStatus();
        status.enabled = enable;
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive", status);
    }

    /**
     * Queries predictive last cutting as {@link Instant}.
     * 
     * @return predictive last cutting
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public @Nullable Instant getPredictiveLastCutting() throws IndegoAuthenticationException, IndegoException {
        try {
            return getRequestWithAuthentication(
                    SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive/lastcutting",
                    PredictiveLastCuttingResponse.class).getLastCutting();
        } catch (IndegoInvalidResponseException e) {
            if (e.getHttpStatusCode() == HttpStatus.NO_CONTENT_204) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Queries predictive next cutting as {@link Instant}.
     * 
     * @return predictive next cutting
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public @Nullable Instant getPredictiveNextCutting() throws IndegoAuthenticationException, IndegoException {
        try {
            return getRequestWithAuthentication(
                    SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive/nextcutting",
                    PredictiveNextCuttingResponse.class).getNextCutting();
        } catch (IndegoInvalidResponseException e) {
            if (e.getHttpStatusCode() == HttpStatus.NO_CONTENT_204) {
                return null;
            }
            throw e;
        }
    }

    /**
     * Queries predictive exclusion time.
     * 
     * @return predictive exclusion time DTO
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceCalendarResponse getPredictiveExclusionTime() throws IndegoAuthenticationException, IndegoException {
        return getRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive/calendar",
                DeviceCalendarResponse.class);
    }

    /**
     * Sets predictive exclusion time.
     * 
     * @param calendar calendar DTO
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void setPredictiveExclusionTime(final DeviceCalendarResponse calendar)
            throws IndegoAuthenticationException, IndegoException {
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/predictive/calendar", calendar);
    }

    /**
     * Request map position updates for the next ({@link count} * {@link interval}) number of seconds.
     * 
     * @param count Number of updates
     * @param interval Number of seconds between updates
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void requestPosition(int count, int interval) throws IndegoAuthenticationException, IndegoException {
        postRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + this.getSerialNumber() + "/requestPosition?count=" + count
                + "&interval=" + interval);
    }
}
