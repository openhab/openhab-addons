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
import org.eclipse.jdt.annotation.Nullable;
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
    private String host = "";

    public EvccAPI(String host) {
        this.host = host;
    }

    /**
     * Make a HTTP request.
     * 
     * @param url full request URL
     * @param method reguest method, e.g. GET, POST
     * @return the response body
     * @throws IOException if HTTP request failed
     */
    private @Nullable String httpRequest(String url, String method) throws IOException {
        String response = HttpUtil.executeUrl(method, url, LONG_CONNECTION_TIMEOUT_MILLISEC);
        logger.trace("{} {} - {}", method, url, response);
        return response;
    }

    // End utility functions

    // API calls to evcc
    /**
     * Get the status from evcc.
     * 
     * @param host hostname of IP address of the evcc instance
     * @return {@link org.openhab.binding.evcc.internal.api.dto.Result} object or null
     * @throws IOException if HTTP request failed
     * @throws JsonSyntaxException if response JSON read failed
     */
    public @Nullable Result getResult() throws IOException, JsonSyntaxException {
        final String response = httpRequest(this.host + EVCC_REST_API + "state", "GET");
        Status status = gson.fromJson(response, Status.class);
        if (status == null) {
            return null;
        }
        return status.getResult();
    }

    // Loadpoint specific API calls.
    public @Nullable String setMode(int loadpoint, String mode) throws IOException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mode/" + mode, "POST");
    }

    public @Nullable String setMinSoC(int loadpoint, int minSoC) throws IOException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/minsoc/" + minSoC, "POST");
    }

    public @Nullable String setTargetSoC(int loadpoint, int targetSoC) throws IOException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetsoc/" + targetSoC, "POST");
    }

    public @Nullable String setPhases(int loadpoint, int phases) throws IOException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/phases/" + phases, "POST");
    }

    public @Nullable String setMinCurrent(int loadpoint, int minCurrent) throws IOException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mincurrent/" + minCurrent, "POST");
    }

    public @Nullable String setMaxCurrent(int loadpoint, int maxCurrent) throws IOException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/maxcurrent/" + maxCurrent, "POST");
    }

    public @Nullable String setTargetCharge(int loadpoint, int targetSoC, ZonedDateTime targetTime) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetcharge/" + targetSoC + "/"
                + targetTime.toLocalDateTime().format(formatter), "POST");
    }

    public @Nullable String unsetTargetCharge(int loadpoint) throws IOException {
        return httpRequest(this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetcharge", "DELETE");
    }
}
