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
package org.openhab.binding.sonnen.internal.communication;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonnen.internal.Helper;
import org.openhab.binding.sonnen.internal.SonnenConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This class handles the JSON communication with the sonnen battery
 *
 * @author Christian Feininger - Initial contribution
 *
 */
@NonNullByDefault
public class SonnenJSONCommunication {

    private final Logger logger = LoggerFactory.getLogger(SonnenJSONCommunication.class);
    private SonnenConfiguration config;

    private Gson gson;
    private @Nullable SonnenJsonDataDTO batteryData;

    public SonnenJSONCommunication() {
        gson = new Gson();
        config = new SonnenConfiguration();
    }

    /**
     * Refreshes the battery connection.
     *
     * @param message Message object to pass errors to the calling method.
     * @param thingUID Thing UID for logging purposes
     * @return true if no error occurred, false otherwise.
     */
    public boolean refreshBatteryConnection(Helper message, String thingUID) {
        boolean resultOk = false;
        String statusDescr = "";
        String urlStr = "http://" + config.hostIP + "/api/v1/status";

        try {
            String response = HttpUtil.executeUrl("GET", urlStr, 10000);
            logger.debug("BatteryData = {}", response);
            if (response == null) {
                throw new IOException("HttpUtil.executeUrl returned null");
            }
            batteryData = gson.fromJson(response, SonnenJsonDataDTO.class);
            resultOk = true;
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error processiong Get request {}", urlStr);
            batteryData = null;
            statusDescr = "Cannot find service on given IP " + config.hostIP + ". Please verify the IP address!";

            resultOk = false;
            logger.debug("Setting thing '{}' to OFFLINE: Error '{}'", thingUID, e.getMessage());
        }
        message.setStatusDescription(statusDescr);
        return resultOk;
    }

    /**
     * Set the config for service to communicate
     *
     * @param config2
     */
    public void setConfig(SonnenConfiguration config2) {
        this.config = config2;
    }

    /**
     * Returns the actual stored Battery Data
     *
     * @return JSON Data from the Battery or null if request failed
     */
    public @Nullable SonnenJsonDataDTO getBatteryData() {
        return this.batteryData;
    }
}
