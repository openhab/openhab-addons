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
package org.openhab.binding.evcc.internal;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.EVCC_REST_API;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.LONG_CONNECTION_TIMEOUT_MILLISEC;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.dto.Result;
import org.openhab.binding.evcc.internal.dto.Status;
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
public class EvccAPI {
    private static final Logger logger = LoggerFactory.getLogger(EvccAPI.class);
    private static final Gson gson = new Gson();
    private final @Nullable String host;

    public EvccAPI(@Nullable String host) {
        this.host = host;
    }

    /**
     * Make a HTTP request.
     * 
     * @param description request description for logger
     * @param url full request URL
     * @param method reguest method, e.g. GET, POST
     * @return the response body or null if request failed
     */
    private @Nullable String httpRequest(@Nullable String description, String url, String method) {
        String response = "";
        try {
            response = HttpUtil.executeUrl(method, url, LONG_CONNECTION_TIMEOUT_MILLISEC);
            logger.trace("{} - {}, {} - {}", description, url, method, response);
            return response;
        } catch (IOException e) {
            logger.debug("IO Exception - {} - {}, {} - {}", description, url, method, e.toString());
            return null;
        }
    }

    // End utility functions

    // API calls to evcc
    /**
     * Get the status from evcc.
     * 
     * @param host hostname of IP address of the evcc instance
     * @return Status object or null if request failed
     */
    public @Nullable Result getResult() {
        final String response = httpRequest("Status", this.host + EVCC_REST_API + "state", "GET");
        try {
            Status status = gson.fromJson(response, Status.class);
            if (status == null)
                return null;
            return status.getResult();
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to get status:", e);
            return null;
        }
    }

    // Loadpoint specific API calls.
    public @Nullable String setMode(int loadpoint, String mode) {
        return httpRequest("Set mode of loadpoint " + loadpoint,
                this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mode/" + mode, "POST");
    }

    public @Nullable String setMinSoC(int loadpoint, int minSoC) {
        return httpRequest("Set minSoC of loadpoint " + loadpoint,
                this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/minsoc/" + minSoC, "POST");
    }

    public @Nullable String setTargetSoC(int loadpoint, int targetSoC) {
        return httpRequest("Set targetSoC of loadpoint " + loadpoint,
                this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetsoc/" + targetSoC, "POST");
    }

    public @Nullable String setPhases(int loadpoint, int phases) {
        return httpRequest("Set phases of loadpoint " + loadpoint,
                this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/phases/" + phases, "POST");
    }

    public @Nullable String setMinCurrent(int loadpoint, int minCurrent) {
        return httpRequest("Set minCurrent of loadpoint " + loadpoint,
                this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mincurrent/" + minCurrent, "POST");
    }

    public @Nullable String setMaxCurrent(int loadpoint, int maxCurrent) {
        return httpRequest("Set maxCurrent of loadpoint " + loadpoint,
                this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/maxcurrent/" + maxCurrent, "POST");
    }

    public @Nullable String setTargetCharge(int loadpoint, int targetSoC, ZonedDateTime targetTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return httpRequest("Set targetTime of loadpoint " + loadpoint, this.host + EVCC_REST_API + "loadpoints/"
                + loadpoint + "/targetcharge/" + targetSoC + "/" + targetTime.toLocalDateTime().format(formatter),
                "POST");
    }

    public @Nullable String unsetTargetCharge(int loadpoint) {
        return httpRequest("Unset targetTime of loadpoint " + loadpoint,
                this.host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetcharge", "DELETE");
    }
}
