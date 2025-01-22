/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
     * Start and Stops the charging of the battery from the grid
     *
     * @return an empty string if no error occurred, the error message otherwise.
     */
    public String startStopBatteryCharging(String putData) {
        String result = "";
        String urlStr = "http://" + config.hostIP + "/api/v2/configurations";
        String urlStr2 = "http://" + config.hostIP + "/api/v2/setpoint/charge/"
                + Integer.toString(config.chargingPower);
        Properties header = createHeader(config.authToken);
        try {
            // in putData there is 1 or 2 inside to turn on or off the manual mode of the battery
            // it will be executed by a change of the switch an either turn on or off the manual mode of the battery
            InputStream targetStream = new ByteArrayInputStream(putData.getBytes(StandardCharsets.UTF_8));
            String response = HttpUtil.executeUrl("PUT", urlStr, header, targetStream,
                    "application/x-www-form-urlencoded", 10000);
            logger.debug("ChargingOperationMode = {}", response);
            if (response == null) {
                throw new IOException("HttpUtil.executeUrl returned null");
            }
            batteryData = gson.fromJson(response, SonnenJsonDataDTO.class);
            // if battery is put to manual mode
            if (config.chargingPower > 10000) {
                throw new IllegalArgumentException(
                        "Max battery charging power in watt needs to be in the range of greater 0 and smaller 10000.");
            }
            SonnenJsonDataDTO batteryData2 = getBatteryData();
            if (batteryData2.emgetOperationMode() != null && Integer.parseInt(batteryData2.emgetOperationMode()) == 1
                    && config.chargingPower > 0 && config.chargingPower <= 10000) {
                // start charging
                String response2 = HttpUtil.executeUrl("POST", urlStr2, header, null, "application/json", 10000);
                logger.debug("ChargingOperationMode = {}", response2);
                if (response2 == null) {
                    throw new IOException("HttpUtil.executeUrl returned null");
                }
            }
        } catch (IOException | JsonSyntaxException | IllegalArgumentException e) {
            logger.debug("Error processiong Put request {}:  {}", urlStr, e.getMessage());
            String message = e.getMessage();
            if (message != null && message.contains("WWW-Authenticate header")) {
                result = "Given token: " + config.authToken + " is not valid.";
            } else if (e.getCause() instanceof IllegalArgumentException) {
                result = "Max battery charging power needs to be in the range of greater 0 and smaller 10000. It cannot be: "
                        + config.chargingPower;
                logger.debug("Error in value for battery capacity: {}", e.getMessage());
            } else {
                result = "Cannot find service on given IP " + config.hostIP + ". Please verify the IP address!";
                logger.debug("Error in establishing connection: {}", e.getMessage());
            }
            batteryData = null;
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
        httpHeader.setProperty("Accept-Encoding", "gzip;q=1.0, compress;q=0.5");
        httpHeader.setProperty("Host", config.hostIP);
        httpHeader.setProperty("Accept", "*/*");
        httpHeader.setProperty("Proxy-Connection", "keep-alive");
        httpHeader.setProperty("Auth-Token", authToken);
        return httpHeader;
    }
}
