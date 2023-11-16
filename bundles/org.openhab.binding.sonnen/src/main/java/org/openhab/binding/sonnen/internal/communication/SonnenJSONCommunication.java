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
package org.openhab.binding.sonnen.internal.communication;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
    private SonnenJsonPowerMeterDataDTO @Nullable [] powerMeterData;

    public SonnenJSONCommunication() {
        gson = new Gson();
        config = new SonnenConfiguration();
    }

    /**
     * Refreshes the battery connection with the new API Call V2.
     *
     * @return an empty string if no error occurred, the error message otherwise.
     */
    public String refreshBatteryConnectionAPICALLV2(boolean powerMeter) {
        String result = "";
        String urlStr = "http://" + config.hostIP + "/api/v2/status";
        Properties httpHeader = new Properties();
        httpHeader = createHeader(config.authToken);
        try {
            String response = HttpUtil.executeUrl("GET", urlStr, httpHeader, null, "application/json", 10000);
            logger.debug("BatteryData = {}", response);
            if (response == null) {
                throw new IOException("HttpUtil.executeUrl returned null");
            }
            batteryData = gson.fromJson(response, SonnenJsonDataDTO.class);

            if (powerMeter) {
                response = HttpUtil.executeUrl("GET", "http://" + config.hostIP + "/api/v2/powermeter", httpHeader,
                        null, "application/json", 10000);
                logger.debug("PowerMeterData = {}", response);
                if (response == null) {
                    throw new IOException("HttpUtil.executeUrl returned null");
                }

                powerMeterData = gson.fromJson(response, SonnenJsonPowerMeterDataDTO[].class);
            }
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error processiong Get request {}:  {}", urlStr, e.getMessage());
            String message = e.getMessage();
            if (message != null && message.contains("WWW-Authenticate header")) {
                result = "Given token: " + config.authToken + " is not valid.";
            } else {
                result = "Cannot find service on given IP " + config.hostIP + ". Please verify the IP address!";
                logger.debug("Error in establishing connection: {}", e.getMessage());
            }
            batteryData = null;
            powerMeterData = new SonnenJsonPowerMeterDataDTO[] {};
        }
        return result;
    }

    /**
     * Refreshes the battery connection with the Old API Call.
     *
     * @return an empty string if no error occurred, the error message otherwise.
     */
    public String refreshBatteryConnectionAPICALLV1() {
        String result = "";
        String urlStr = "http://" + config.hostIP + "/api/v1/status";
        try {
            String response = HttpUtil.executeUrl("GET", urlStr, 10000);
            logger.debug("BatteryData = {}", response);
            if (response == null) {
                throw new IOException("HttpUtil.executeUrl returned null");
            }
            batteryData = gson.fromJson(response, SonnenJsonDataDTO.class);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error processiong Get request {}:  {}", urlStr, e.getMessage());
            result = "Cannot find service on given IP " + config.hostIP + ". Please verify the IP address!";
            batteryData = null;
        }
        return result;
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

    /**
     * Returns the actual stored Power Meter Data Array
     *
     * @return JSON Data from the Power Meter or null if request failed
     */
    public SonnenJsonPowerMeterDataDTO @Nullable [] getPowerMeterData() {
        return this.powerMeterData;
    }

    /**
     * Creates the header for the Get Request
     *
     * @return The created Header Properties
     */
    private Properties createHeader(String authToken) {
        Properties httpHeader = new Properties();
        httpHeader.setProperty("Host", config.hostIP);
        httpHeader.setProperty("Accept", "*/*");
        httpHeader.setProperty("Proxy-Connection", "keep-alive");
        httpHeader.setProperty("Auth-Token", authToken);
        httpHeader.setProperty("Accept-Encoding", "gzip;q=1.0, compress;q=0.5");
        return httpHeader;
    }
}
