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
package org.openhab.binding.evcc.internal.api;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.EVCC_REST_API;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.LONG_CONNECTION_TIMEOUT_MILLISEC;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.api.dto.Result;
import org.openhab.binding.evcc.internal.api.dto.Status;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EvccAPI} is responsible for API calls to evcc.
 * API requests were written for evcc version 0.123.1
 *
 * @author Florian Hotze - Initial contribution
 * @author Luca Arnecke - Update to evcc version 0.123.1
 */
@NonNullByDefault
public class EvccAPI {
    private final Logger logger = LoggerFactory.getLogger(EvccAPI.class);
    private final Gson gson = new Gson();
    private final TimeZoneProvider timeZoneProvider;
    private String host;

    public EvccAPI(String host, TimeZoneProvider timeZoneProvider) {
        this.host = (host.endsWith("/") ? host.substring(0, host.length() - 1) : host);
        this.timeZoneProvider = timeZoneProvider;
    }

    /**
     * Make a HTTP request.
     *
     * @param url full request URL
     * @param method request method, e.g. GET, POST
     * @return the response body
     * @throws EvccApiException if HTTP request failed
     */
    private String httpRequest(String url, String method) throws EvccApiException {
        try {
            String response = HttpUtil.executeUrl(method, url, LONG_CONNECTION_TIMEOUT_MILLISEC);
            logger.trace("{} {} - {}", method, url, response);
            return response;
        } catch (IOException e) {
            throw new EvccApiException("HTTP request failed for URL " + url, e);
        }
    }

    // End utility functions

    // API calls to evcc
    /**
     * Get the status from evcc.
     *
     * @return {@link Result} result object from API
     * @throws EvccApiException if status request failed
     */
    public Result getResult() throws EvccApiException {
        final String response = httpRequest(this.host + EVCC_REST_API + "state", "GET");
        logger.trace("API Response >> {}", response);
        try {
            Status status = gson.fromJson(response, Status.class);
            if (status == null) {
                throw new EvccApiException("Status is null");
            }
            return status.getResult();
        } catch (JsonSyntaxException e) {
            throw new EvccApiException("Error parsing response: " + response, e);
        }
    }

    // Site API calls.
    public String setPrioritySoC(int prioritySoc) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "prioritysoc/" + prioritySoc, "POST");
    }

    public String setBufferSoC(int bufferSoC) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "buffersoc/" + bufferSoC, "POST");
    }

    public String setBufferStartSoC(int bufferStartSoC) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "bufferstartsoc/" + bufferStartSoC, "POST");
    }

    public String setResidualPower(int residualPower) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "residualpower/" + residualPower, "POST");
    }

    public String setBatteryDischargeControl(boolean batteryDischargeControl) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "batterydischargecontrol/" + batteryDischargeControl, "POST");
    }

    // Loadpoint specific API calls.
    public String setMode(int loadpoint, String mode) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mode/" + mode, "POST");
    }

    public String setLimitEnergy(int loadpoint, float limitEnergy) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/limitenergy/" + limitEnergy,
                "POST");
    }

    public String setLimitSoC(int loadpoint, int limitSoC) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/limitsoc/" + limitSoC, "POST");
    }

    public String setPhases(int loadpoint, int phases) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/phases/" + phases, "POST");
    }

    public String setMinCurrent(int loadpoint, int minCurrent) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mincurrent/" + minCurrent, "POST");
    }

    public String setMaxCurrent(int loadpoint, int maxCurrent) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/maxcurrent/" + maxCurrent, "POST");
    }

    // Vehicle specific API calls.
    public String setVehicleMinSoC(String vehicleName, int minSoC) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "vehicles/" + vehicleName + "/minsoc/" + minSoC, "POST");
    }

    public String setVehicleLimitSoC(String vehicleName, int limitSoC) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "vehicles/" + vehicleName + "/limitsoc/" + limitSoC, "POST");
    }

    public String setVehiclePlan(String vehicleName, int planSoC, ZonedDateTime planTime) throws EvccApiException {
        ZoneId zoneId = timeZoneProvider.getTimeZone();
        ZonedDateTime adjustedTime = planTime.withZoneSameInstant(zoneId);
        String formattedTime = adjustedTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return httpRequest(
                this.host + EVCC_REST_API + "vehicles/" + vehicleName + "/plan/soc/" + planSoC + "/" + formattedTime,
                "POST");
    }

    public String removeVehiclePlan(String vehicleName) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "vehicles/" + vehicleName + "/plan/soc", "DELETE");
    }
}
