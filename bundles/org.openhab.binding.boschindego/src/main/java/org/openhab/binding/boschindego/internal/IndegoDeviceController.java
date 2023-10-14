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

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.boschindego.internal.dto.DeviceCommand;
import org.openhab.binding.boschindego.internal.dto.PredictiveAdjustment;
import org.openhab.binding.boschindego.internal.dto.PredictiveStatus;
import org.openhab.binding.boschindego.internal.dto.request.SetStateRequest;
import org.openhab.binding.boschindego.internal.dto.response.DeviceCalendarResponse;
import org.openhab.binding.boschindego.internal.dto.response.DevicePropertiesResponse;
import org.openhab.binding.boschindego.internal.dto.response.DeviceStateResponse;
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

/**
 * Controller for communicating with a Bosch Indego device through Bosch services.
 * This class provides methods for retrieving state information as well as controlling
 * the device.
 * 
 * The implementation is based on zazaz-de's iot-device-bosch-indego-controller, but
 * rewritten from scratch to use Jetty HTTP client for HTTP communication and GSON for
 * JSON parsing. Thanks to Oliver Schünemann for providing the original implementation.
 * 
 * @see <a href=
 *      "https://github.com/zazaz-de/iot-device-bosch-indego-controller">zazaz-de/iot-device-bosch-indego-controller</a>
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class IndegoDeviceController extends IndegoController {

    private String serialNumber;

    /**
     * Initialize the controller instance.
     * 
     * @param httpClient the HttpClient for communicating with the service
     * @param authorizationProvider the AuthorizationProvider for authenticating with the service
     * @param serialNumber the serial number of the device instance
     */
    public IndegoDeviceController(HttpClient httpClient, AuthorizationProvider authorizationProvider,
            String serialNumber) {
        super(httpClient, authorizationProvider);
        if (serialNumber.isBlank()) {
            throw new IllegalArgumentException("Serial number must be provided");
        }
        this.serialNumber = serialNumber;
    }

    /**
     * Queries the serial number and device service properties from the server.
     *
     * @return the device serial number and properties
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DevicePropertiesResponse getDeviceProperties() throws IndegoAuthenticationException, IndegoException {
        return super.getDeviceProperties(serialNumber);
    }

    /**
     * Queries the device state from the server.
     * 
     * @return the device state
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceStateResponse getState() throws IndegoAuthenticationException, IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/state", DeviceStateResponse.class);
    }

    /**
     * Queries the device state from the server. This overload will return when the state
     * has changed, or the timeout has been reached.
     * 
     * @param timeout maximum time to wait for response
     * @return the device state
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceStateResponse getState(Duration timeout) throws IndegoAuthenticationException, IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/state?longpoll=true&timeout=" + timeout.getSeconds(),
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
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/operatingData", OperatingDataResponse.class);
    }

    /**
     * Queries the map generated by the device from the server.
     * 
     * @return the garden map
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public RawType getMap() throws IndegoAuthenticationException, IndegoException {
        return getRawRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/map");
    }

    /**
     * Queries the calendar.
     * 
     * @return the calendar
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public DeviceCalendarResponse getCalendar() throws IndegoAuthenticationException, IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/calendar", DeviceCalendarResponse.class);
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
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + serialNumber + "/state", request);
    }

    /**
     * Queries the predictive weather forecast.
     * 
     * @return the weather forecast DTO
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public LocationWeatherResponse getWeather() throws IndegoAuthenticationException, IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/weather", LocationWeatherResponse.class);
    }

    /**
     * Queries the predictive adjustment.
     * 
     * @return the predictive adjustment
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public int getPredictiveAdjustment() throws IndegoAuthenticationException, IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/useradjustment",
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
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/useradjustment", adjustment);
    }

    /**
     * Queries predictive moving.
     * 
     * @return predictive moving
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public boolean getPredictiveMoving() throws IndegoAuthenticationException, IndegoException {
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive", PredictiveStatus.class).enabled;
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
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive", status);
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
            return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/lastcutting",
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
            return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/nextcutting",
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
        return getRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/calendar", DeviceCalendarResponse.class);
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
        putRequestWithAuthentication(SERIAL_NUMBER_SUBPATH + serialNumber + "/predictive/calendar", calendar);
    }

    /**
     * Request map position updates for the next ({@code count} * {@code interval}) number of seconds.
     * 
     * @param count number of updates
     * @param interval number of seconds between updates
     * @throws IndegoAuthenticationException if request was rejected as unauthorized
     * @throws IndegoException if any communication or parsing error occurred
     */
    public void requestPosition(int count, int interval) throws IndegoAuthenticationException, IndegoException {
        postRequest(SERIAL_NUMBER_SUBPATH + serialNumber + "/requestPosition?count=" + count + "&interval=" + interval);
    }
}
