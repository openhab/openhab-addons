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

import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
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
import org.openhab.binding.boschindego.internal.dto.response.LocationWeatherResponse;
import org.openhab.binding.boschindego.internal.dto.response.PredictiveCuttingTimeResponse;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoInvalidCommandException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoInvalidResponseException;
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
    private static final String SERIAL_NUMBER_SUBPATH = "alms/";
    private static final String CONTEXT_HEADER_NAME = "x-im-context-id";
    private static final String CONTENT_TYPE_HEADER = "application/json";

    private final Logger logger = LoggerFactory.getLogger(IndegoController.class);
    private final String basicAuthenticationHeader;
    private final Gson gson = new Gson();
    private final HttpClient httpClient;

    private String contextId = "";
    private String serialNumber = "";

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
    public void authenticate() throws IndegoAuthenticationException, IndegoException {
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

            ContentResponse response = sendRequest(request);
            int status = response.getStatus();
            if (status == HttpStatus.UNAUTHORIZED_401) {
                throw new IndegoAuthenticationException("Authentication was rejected");
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoAuthenticationException("The request failed with HTTP error: " + status);
            }

            String jsonResponse = response.getContentAsString();
            if (jsonResponse.isEmpty()) {
                throw new IndegoInvalidResponseException("No content returned");
            }
            logger.trace("JSON response: '{}'", jsonResponse);

            AuthenticationResponse authenticationResponse = gson.fromJson(jsonResponse, AuthenticationResponse.class);
            if (authenticationResponse == null) {
                throw new IndegoInvalidResponseException("Response could not be parsed as AuthenticationResponse");
            }

            contextId = authenticationResponse.contextId;
            serialNumber = authenticationResponse.serialNumber;
        } catch (JsonParseException e) {
            throw new IndegoInvalidResponseException("Error parsing AuthenticationResponse", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException | ExecutionException e) {
            throw new IndegoException(e);
        }
    }

    /**
     * Sends a GET request to the server and returns the deserialized JSON response.
     * 
     * @param path the relative path to which the request should be sent
     * @param dtoClass the DTO class to which the JSON result should be deserialized
     * @return the deserialized DTO from the JSON response
     * @throws IndegoException if any communication or parsing error occurred
     */
    private <T> T getRequest(String path, Class<? extends T> dtoClass) throws IndegoException {
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.GET).header(CONTEXT_HEADER_NAME,
                    contextId);
            if (logger.isTraceEnabled()) {
                logger.trace("GET request for {}", BASE_URL + path);
            }
            ContentResponse response = sendRequest(request);
            int status = response.getStatus();
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoAuthenticationException("The request failed with HTTP error: " + status);
            }
            String jsonResponse = response.getContentAsString();
            if (jsonResponse.isEmpty()) {
                throw new IndegoInvalidResponseException("No content returned");
            }
            logger.trace("JSON response: '{}'", jsonResponse);

            @Nullable
            T result = gson.fromJson(jsonResponse, dtoClass);
            if (result == null) {
                throw new IndegoInvalidResponseException("Parsed response is null");
            }
            return result;
        } catch (JsonParseException e) {
            throw new IndegoInvalidResponseException("Error parsing response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException | ExecutionException e) {
            throw new IndegoException(e);
        }
    }

    /**
     * Sends a PUT request to the server.
     * 
     * @param path the relative path to which the request should be sent
     * @param requestDto the DTO which should be sent to the server as JSON
     * @throws IndegoException if any communication or parsing error occurred
     */
    private void putRequest(String path, Object requestDto) throws IndegoException {
        try {
            Request request = httpClient.newRequest(BASE_URL + path).method(HttpMethod.PUT)
                    .header(CONTEXT_HEADER_NAME, contextId).header(HttpHeader.CONTENT_TYPE, CONTENT_TYPE_HEADER);
            String payload = gson.toJson(requestDto);
            request.content(new StringContentProvider(payload));
            if (logger.isTraceEnabled()) {
                logger.trace("PUT request for {} with payload '{}'", BASE_URL + path, payload);
            }
            ContentResponse response = sendRequest(request);
            int status = response.getStatus();
            if (status == HttpStatus.INTERNAL_SERVER_ERROR_500) {
                throw new IndegoInvalidCommandException("The request failed with HTTP error: " + status);
            }
            if (!HttpStatus.isSuccess(status)) {
                throw new IndegoException("The request failed with error: " + status);
            }

            if (logger.isTraceEnabled()) {
                logger.trace("JSON response: '{}'", response.getContentAsString());
            }
        } catch (JsonParseException e) {
            throw new IndegoInvalidResponseException("Error parsing response", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IndegoException(e);
        } catch (TimeoutException | ExecutionException e) {
            throw new IndegoException(e);
        }
    }

    private synchronized ContentResponse sendRequest(Request request)
            throws InterruptedException, TimeoutException, ExecutionException {
        return request.send();
    }

    /**
     * Gets serial number of the associated Indego device
     *
     * @return the serial number of the device
     */
    public String getDeviceSerialNumber() {
        return serialNumber;
    }

    /**
     * Queries the device state from the server.
     * 
     * @return the device state
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceStateResponse getState() throws IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/state", DeviceStateResponse.class);
    }

    public DeviceCalendarResponse getCalendar() throws IndegoException {
        DeviceCalendarResponse calendar = getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/calendar",
                DeviceCalendarResponse.class);
        return calendar;
    }

    /**
     * Sends a command to the Indego device.
     * 
     * @param command the control command to send to the device.
     * @throws IndegoInvalidCommandException if the command was not processed correctly
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void sendCommand(DeviceCommand command) throws IndegoInvalidCommandException, IndegoException {
        SetStateRequest request = new SetStateRequest();
        request.state = command.getActionCode();
        putRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/state", request);
    }

    /**
     * Queries the predictive weather forecast.
     * 
     * @return the weather forecast DTO
     * @throws IndegoException if any communication or parsing error occurred
     */
    public LocationWeatherResponse getWeather() throws IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/weather", LocationWeatherResponse.class);
    }

    public int getPredictiveAdjustment() throws IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/useradjustment",
                PredictiveAdjustment.class).adjustment;
    }

    public void setPredictiveAdjustment(final int adjust) throws IndegoException {
        final PredictiveAdjustment adjustment = new PredictiveAdjustment();
        adjustment.adjustment = adjust;
        putRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/useradjustment", adjustment);
    }

    public boolean getPredictiveMoving() throws IndegoException {
        final PredictiveStatus status = getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive",
                PredictiveStatus.class);
        return status.enabled;
    }

    public void setPredictiveMoving(final boolean enable) throws IndegoException {
        final PredictiveStatus status = new PredictiveStatus();
        status.enabled = enable;
        putRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive", status);
    }

    public Instant getPredictiveNextCutting() throws IndegoException {
        final PredictiveCuttingTimeResponse nextCutting = getRequest(
                SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/nextcutting", PredictiveCuttingTimeResponse.class);
        return nextCutting.getNextCutting();
    }

    public DeviceCalendarResponse getPredictiveExclusionTime() throws IndegoException {
        final DeviceCalendarResponse calendar = getRequest(
                SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/calendar", DeviceCalendarResponse.class);
        return calendar;
    }

    public void setPredictiveExclusionTime(final DeviceCalendarResponse calendar) throws IndegoException {
        putRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/calendar", calendar);
    }
}
