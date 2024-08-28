/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.daikin.internal;

import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.daikin.internal.api.BasicInfo;
import org.openhab.binding.daikin.internal.api.ControlInfo;
import org.openhab.binding.daikin.internal.api.DemandControl;
import org.openhab.binding.daikin.internal.api.EnergyInfoDayAndWeek;
import org.openhab.binding.daikin.internal.api.EnergyInfoYear;
import org.openhab.binding.daikin.internal.api.Enums.SpecialMode;
import org.openhab.binding.daikin.internal.api.SensorInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseBasicInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseControlInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseModelInfo;
import org.openhab.binding.daikin.internal.api.airbase.AirbaseZoneInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles performing the actual HTTP requests for communicating with Daikin air conditioning units.
 *
 * @author Tim Waterhouse - Initial Contribution
 * @author Paul Smedley - Modifications to support Airbase Controllers
 * @author Jimmy Tanagra - Add support for https and Daikin's uuid authentication
 *         Implement connection retry
 *
 */
@NonNullByDefault
public class DaikinWebTargets {
    private static final int TIMEOUT_MS = 5000;

    private String getBasicInfoUri;
    private String setControlInfoUri;
    private String getControlInfoUri;
    private String getSensorInfoUri;
    private String registerUuidUri;
    private String getEnergyInfoYearUri;
    private String getEnergyInfoWeekUri;
    private String setSpecialModeUri;
    private String setDemandControlUri;
    private String getDemandControlUri;

    private String setAirbaseControlInfoUri;
    private String getAirbaseControlInfoUri;
    private String getAirbaseSensorInfoUri;
    private String getAirbaseBasicInfoUri;
    private String getAirbaseModelInfoUri;
    private String getAirbaseZoneInfoUri;
    private String setAirbaseZoneInfoUri;

    private @Nullable String uuid;
    private final @Nullable HttpClient httpClient;

    private Logger logger = LoggerFactory.getLogger(DaikinWebTargets.class);

    public DaikinWebTargets(@Nullable HttpClient httpClient, @Nullable String host, @Nullable Boolean secure,
            @Nullable String uuid) {
        this.httpClient = httpClient;
        this.uuid = uuid;

        String baseUri = (secure != null && secure.booleanValue() ? "https://" : "http://") + host + "/";
        getBasicInfoUri = baseUri + "common/basic_info";
        setControlInfoUri = baseUri + "aircon/set_control_info";
        getControlInfoUri = baseUri + "aircon/get_control_info";
        getSensorInfoUri = baseUri + "aircon/get_sensor_info";
        registerUuidUri = baseUri + "common/register_terminal";
        getEnergyInfoYearUri = baseUri + "aircon/get_year_power_ex";
        getEnergyInfoWeekUri = baseUri + "aircon/get_week_power_ex";
        setSpecialModeUri = baseUri + "aircon/set_special_mode";
        setDemandControlUri = baseUri + "aircon/set_demand_control";
        getDemandControlUri = baseUri + "aircon/get_demand_control";

        // Daikin Airbase API
        getAirbaseBasicInfoUri = baseUri + "skyfi/common/basic_info";
        setAirbaseControlInfoUri = baseUri + "skyfi/aircon/set_control_info";
        getAirbaseControlInfoUri = baseUri + "skyfi/aircon/get_control_info";
        getAirbaseSensorInfoUri = baseUri + "skyfi/aircon/get_sensor_info";
        getAirbaseModelInfoUri = baseUri + "skyfi/aircon/get_model_info";
        getAirbaseZoneInfoUri = baseUri + "skyfi/aircon/get_zone_setting";
        setAirbaseZoneInfoUri = baseUri + "skyfi/aircon/set_zone_setting";
    }

    // Standard Daikin API
    public BasicInfo getBasicInfo() throws DaikinCommunicationException {
        String response = invoke(getBasicInfoUri);
        return BasicInfo.parse(response);
    }

    public ControlInfo getControlInfo() throws DaikinCommunicationException {
        String response = invoke(getControlInfoUri);
        return ControlInfo.parse(response);
    }

    public boolean setControlInfo(ControlInfo info) throws DaikinCommunicationException {
        Map<String, String> queryParams = info.getParamString();
        return isSuccessful(invoke(setControlInfoUri, queryParams));
    }

    public SensorInfo getSensorInfo() throws DaikinCommunicationException {
        String response = invoke(getSensorInfoUri);
        return SensorInfo.parse(response);
    }

    public void registerUuid(String key) throws DaikinCommunicationException {
        Map<String, String> params = new HashMap<>();
        params.put("key", key);
        String response = invoke(registerUuidUri, params);
        logger.debug("registerUuid result: {}", response);
    }

    public EnergyInfoYear getEnergyInfoYear() throws DaikinCommunicationException {
        String response = invoke(getEnergyInfoYearUri);
        return EnergyInfoYear.parse(response);
    }

    public EnergyInfoDayAndWeek getEnergyInfoDayAndWeek() throws DaikinCommunicationException {
        String response = invoke(getEnergyInfoWeekUri);
        return EnergyInfoDayAndWeek.parse(response);
    }

    public boolean setSpecialMode(SpecialMode newMode) throws DaikinCommunicationException {
        Map<String, String> queryParams = new HashMap<>();
        if (newMode == SpecialMode.NORMAL) {
            queryParams.put("set_spmode", "0");

            ControlInfo controlInfo = getControlInfo();
            if (!controlInfo.advancedMode.isUndefined()) {
                queryParams.put("spmode_kind", controlInfo.getSpecialMode().getValue());
            }
        } else {
            queryParams.put("set_spmode", "1");
            queryParams.put("spmode_kind", newMode.getValue());
        }
        String response = invoke(setSpecialModeUri, queryParams);
        if (isSuccessful(response)) {
            return true;
        } else {
            logger.warn("Error setting special mode. Response: '{}'", response);
            return false;
        }
    }

    public boolean setStreamerMode(boolean state) throws DaikinCommunicationException {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("en_streamer", state ? "1" : "0");
        String response = invoke(setSpecialModeUri, queryParams);
        if (isSuccessful(response)) {
            return true;
        } else {
            logger.warn("Error setting streamer mode. Response: '{}'", response);
            return false;
        }
    }

    public DemandControl getDemandControl() throws DaikinCommunicationException {
        String response = invoke(getDemandControlUri);
        return DemandControl.parse(response);
    }

    public boolean setDemandControl(DemandControl info) throws DaikinCommunicationException {
        Map<String, String> queryParams = info.getParamString();
        return isSuccessful(invoke(setDemandControlUri, queryParams));
    }

    // Daikin Airbase API
    public AirbaseControlInfo getAirbaseControlInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseControlInfoUri);
        return AirbaseControlInfo.parse(response);
    }

    public boolean setAirbaseControlInfo(AirbaseControlInfo info) throws DaikinCommunicationException {
        Map<String, String> queryParams = info.getParamString();
        return isSuccessful(invoke(setAirbaseControlInfoUri, queryParams));
    }

    public SensorInfo getAirbaseSensorInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseSensorInfoUri);
        return SensorInfo.parse(response);
    }

    public AirbaseBasicInfo getAirbaseBasicInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseBasicInfoUri);
        return AirbaseBasicInfo.parse(response);
    }

    public AirbaseModelInfo getAirbaseModelInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseModelInfoUri);
        return AirbaseModelInfo.parse(response);
    }

    public AirbaseZoneInfo getAirbaseZoneInfo() throws DaikinCommunicationException {
        String response = invoke(getAirbaseZoneInfoUri);
        return AirbaseZoneInfo.parse(response);
    }

    public boolean setAirbaseZoneInfo(AirbaseZoneInfo zoneinfo) throws DaikinCommunicationException {
        Map<String, String> queryParams = zoneinfo.getParamString();
        return isSuccessful(invoke(setAirbaseZoneInfoUri, queryParams));
    }

    private boolean isSuccessful(String response) {
        return response.contains("ret=OK");
    }

    private String invoke(String uri) throws DaikinCommunicationException {
        return invoke(uri, null);
    }

    private synchronized String invoke(String url, @Nullable Map<String, String> params)
            throws DaikinCommunicationException {
        int attemptCount = 1;
        try {
            while (true) {
                try {
                    String result = executeUrl(url, params);
                    if (attemptCount > 1) {
                        logger.debug("HTTP request successful on attempt #{}: {}", attemptCount, url);
                    }
                    return result;
                } catch (ExecutionException | TimeoutException e) {
                    if (attemptCount >= 3) {
                        logger.debug("HTTP request failed after {} attempts: {}", attemptCount, url, e);
                        Throwable rootCause = getRootCause(e);
                        String message = rootCause.getMessage();
                        // EOFException message is too verbose/gibberish
                        if (message == null || rootCause instanceof EOFException) {
                            message = "Connection error";
                        }
                        throw new DaikinCommunicationException(message);
                    }
                    logger.debug("HTTP request error on attempt #{}: {} {}", attemptCount, url, e.getMessage());
                    Thread.sleep(500 * attemptCount);
                    attemptCount++;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DaikinCommunicationException("Execution interrupted");
        }
    }

    private String executeUrl(String url, @Nullable Map<String, String> params)
            throws InterruptedException, TimeoutException, ExecutionException, DaikinCommunicationException {
        Request request = httpClient.newRequest(url).method(HttpMethod.GET).timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        if (uuid != null) {
            request.header("X-Daikin-uuid", uuid);
            logger.trace("Header: X-Daikin-uuid: {}", uuid);
        }
        if (params != null) {
            params.forEach((key, value) -> request.param(key, value));
        }
        logger.trace("Calling url: {}", request.getURI());

        ContentResponse response = request.send();

        if (response.getStatus() != HttpStatus.OK_200) {
            logger.debug("Daikin controller HTTP status: {} - {} {}", response.getStatus(), response.getReason(), url);
        }

        if (response.getStatus() == HttpStatus.FORBIDDEN_403) {
            throw new DaikinCommunicationForbiddenException("Daikin controller access denied. Check uuid/key.");
        }

        return response.getContentAsString();
    }

    private Throwable getRootCause(Throwable exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            exception = cause;
            cause = cause.getCause();
        }
        return exception;
    }
}
