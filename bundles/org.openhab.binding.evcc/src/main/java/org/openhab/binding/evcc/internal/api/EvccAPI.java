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
package org.openhab.binding.evcc.internal.api;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.EVCC_REST_API;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.LONG_CONNECTION_TIMEOUT_MILLISEC;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.api.dto.Result;
import org.openhab.binding.evcc.internal.api.dto.Status;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EvccAPI} is responsible for API calls to evcc.
 * 
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class EvccAPI {
    private final Logger logger = LoggerFactory.getLogger(EvccAPI.class);
    private final Gson gson = new Gson();
    private String host;

    public EvccAPI(String host) {
        this.host = (host.endsWith("/") ? host.substring(0, host.length() - 1) : host);
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

    // Loadpoint specific API calls.
    public String setMode(int loadpoint, String mode) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mode/" + mode, "POST");
    }

    public String setMinSoC(int loadpoint, int minSoC) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/minsoc/" + minSoC, "POST");
    }

    public String setTargetSoC(int loadpoint, int targetSoC) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetsoc/" + targetSoC, "POST");
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

    public String setTargetCharge(int loadpoint, float targetSoC, ZonedDateTime targetTime) throws EvccApiException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetcharge/" + targetSoC + "/"
                + targetTime.toLocalDateTime().format(formatter), "POST");
    }

    public String unsetTargetCharge(int loadpoint) throws EvccApiException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetcharge", "DELETE");
    }
}
