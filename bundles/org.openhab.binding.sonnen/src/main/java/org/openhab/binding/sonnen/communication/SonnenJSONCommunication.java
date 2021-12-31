/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.sonnen.communication;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonnen.internal.SonnenConfiguration;
import org.openhab.binding.sonnen.utilities.Helper;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

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
        batteryData = new SonnenJsonDataDTO();
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
        if (config.hostIP == null) {
            message.setStatusDescription("Error in configuration. Please recreate Thing.");
            return false;
        }
        SonnenJsonDataDTO result = null;
        boolean resultOk = false;
        String error = "", errorDetail = "", statusDescr = "";
        String urlStr = "http://" + config.hostIP + "/api/v1/status";

        String response = null;
        try {
            response = HttpUtil.executeUrl("GET", urlStr, 10000);
            logger.debug("BatteryData = {}", response);
            result = gson.fromJson(response, SonnenJsonDataDTO.class);
            resultOk = true;
        } catch (IOException e) {
            logger.debug("Error processiong Get request {}", urlStr);
            statusDescr = "Timeout error with" + config.hostIP
                    + ". Cannot find service on give IP. Please verify the IP-Address!";
            errorDetail = e.getMessage();
            resultOk = false;
        } catch (Exception e) {
            logger.debug("Unknwon Error: {}", e.getMessage());
            errorDetail = e.getMessage();
            resultOk = false;
        }
        if (resultOk) {
            batteryData = result;
        } else {
            logger.debug("Setting thing '{}' to OFFLINE: Error '{}': {}", thingUID, error, errorDetail);
            batteryData = new SonnenJsonDataDTO();
        }
        message.setStatusDescription(statusDescr);
        return resultOk;
    }

    /**
     * Set the config for service to communicate
     *
     * @param config2
     */
    public void setConfig(@Nullable SonnenConfiguration config2) {
        if (config2 != null) {
            this.config = config2;
        }
    }

    /**
     * Returns the actual stored Battery Data
     *
     * @return
     */
    @Nullable
    public SonnenJsonDataDTO getBatteryData() {
        return this.batteryData;
    }
}
