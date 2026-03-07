/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
        Properties httpHeader = createHeader(config.authToken);
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
            logger.debug("Error processing Get request {}:  {}", urlStr, e.getMessage());
            result = handleException(e, 0, "status");
            batteryData = null;
            powerMeterData = new SonnenJsonPowerMeterDataDTO[] {};
        }
        return result;
    }

    /**
     * Start and Stops the charging of the battery from the grid
     */
    public String startStopBatteryCharging(@Nullable String putData, int chargeRate) {
        return executeBatteryOperation(putData, chargeRate, "charge");
    }

    /**
     * Start and Stops the discharging of the battery to the grid
     */
    public String startStopBatteryDischarging(@Nullable String putData, int dischargeRate) {
        return executeBatteryOperation(putData, dischargeRate, "discharge");
    }

    /**
     * Internal helper to execute charging/discharging logic to avoid code duplication.
     */
    private String executeBatteryOperation(@Nullable String putData, int rate, String operation) {
        String result = "";
        String configUrl = "http://" + config.hostIP + "/api/v2/configurations";
        String setpointUrl = "http://" + config.hostIP + "/api/v2/setpoint/" + operation + "/" + rate;
        Properties header = createHeader(config.authToken);

        try {
            // Validate range before network calls
            if (rate < 0 || rate > 10000) {
                throw new IllegalArgumentException(
                        "Max battery " + operation + " power " + rate + " needs to be in the range of 0 - 10000.");
            }

            if (putData != null) {
                InputStream targetStream = new ByteArrayInputStream(putData.getBytes(StandardCharsets.UTF_8));
                String response = HttpUtil.executeUrl("PUT", configUrl, header, targetStream,
                        "application/x-www-form-urlencoded", 10000);

                if (response == null) {
                    throw new IOException("HttpUtil.executeUrl (PUT) returned null");
                }

                batteryData = gson.fromJson(response, SonnenJsonDataDTO.class);
            }
            SonnenJsonDataDTO currentData = getBatteryData();

            // Execute setpoint if manual mode (1) is active, or isInAutomaticMode = false
            if (currentData != null
                    && ("1".equals(currentData.emgetOperationMode()) || !currentData.isInAutomaticMode())) {
                String response2 = HttpUtil.executeUrl("POST", setpointUrl, header, null, "application/json", 10000);
                logger.debug("{}OperationMode = {}", operation, response2);
                if (response2 == null) {
                    throw new IOException("HttpUtil.executeUrl (POST) returned null");
                }
            }
        } catch (IOException | JsonSyntaxException | IllegalArgumentException e) {
            logger.debug("Error processing {} request: {}", operation, e.getMessage());
            result = handleException(e, rate, operation);
            batteryData = null;
        }
        return result;
    }

    /**
     * Centralized exception handling for error messages.
     */
    private String handleException(Exception e, int rate, String operation) {
        String message = e.getMessage();
        if (message != null && message.contains("WWW-Authenticate header")) {
            return "Given token: " + config.authToken + " is not valid.";
        } else if (e instanceof IllegalArgumentException) {
            return "Max battery " + operation + " power needs to be in the range of 0 - 10000. It cannot be: " + rate;
        } else {
            return "Cannot find service on given IP " + config.hostIP + ". Please verify the IP address!";
        }
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
            logger.debug("Error processing Get request {}:  {}", urlStr, e.getMessage());
            result = "Cannot find service on given IP " + config.hostIP + ". Please verify the IP address!";
            batteryData = null;
        }
        return result;
    }

    public void setConfig(SonnenConfiguration config2) {
        this.config = config2;
    }

    public @Nullable SonnenJsonDataDTO getBatteryData() {
        return this.batteryData;
    }

    public SonnenJsonPowerMeterDataDTO @Nullable [] getPowerMeterData() {
        return this.powerMeterData;
    }

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
