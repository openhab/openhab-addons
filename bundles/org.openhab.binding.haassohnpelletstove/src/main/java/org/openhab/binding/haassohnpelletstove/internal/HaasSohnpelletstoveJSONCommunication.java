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
package org.openhab.binding.haassohnpelletstove.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This class handles the JSON communication with the Wifi Modul of the Stove
 *
 * @author Christian Feininger - Initial contribution
 *
 */
@NonNullByDefault
public class HaasSohnpelletstoveJSONCommunication {

    private final Logger logger = LoggerFactory.getLogger(HaasSohnpelletstoveJSONCommunication.class);
    private HaasSohnpelletstoveConfiguration config;

    private Gson gson;
    private @Nullable String xhspin;
    private @Nullable HaasSohnpelletstoveJsonDataDTO ovenData;

    public HaasSohnpelletstoveJSONCommunication() {
        gson = new Gson();
        ovenData = new HaasSohnpelletstoveJsonDataDTO();
        xhspin = "";
        config = new HaasSohnpelletstoveConfiguration();
    }

    /**
     * Refreshes the oven Connection with the internal oven token.
     *
     * @param message Message object to pass errors to the calling method.
     * @param thingUID Thing UID for logging purposes
     * @return true if no error occurred, false otherwise.
     */
    public boolean refreshOvenConnection(Helper message, String thingUID) {
        if (config.hostIP == null || config.hostPIN == null) {
            message.setStatusDescription("Error in configuration. Please recreate Thing.");
            return false;
        }
        HaasSohnpelletstoveJsonDataDTO result = null;
        boolean resultOk = false;
        String error = "", errorDetail = "", statusDescr = "";
        String urlStr = "http://" + config.hostIP + "/status.cgi";

        String response = null;
        try {
            response = HttpUtil.executeUrl("GET", urlStr, 10000);
            logger.debug("OvenData = {}", response);
            result = gson.fromJson(response, HaasSohnpelletstoveJsonDataDTO.class);
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
            ovenData = result;
            xhspin = getValidXHSPIN(ovenData);
        } else {
            logger.debug("Setting thing '{}' to OFFLINE: Error '{}': {}", thingUID, error, errorDetail);
            ovenData = new HaasSohnpelletstoveJsonDataDTO();
        }
        message.setStatusDescription(statusDescr);
        return resultOk;
    }

    /**
     * Gets the status of the oven
     *
     * @return true if success or false in case of error
     */
    public boolean updateOvenData(@Nullable String postData, Helper helper, String thingUID) {
        String statusDescr = "";
        boolean resultOk = false;
        String error = "", errorDetail = "";
        if (config.hostIP == null || config.hostPIN == null) {
            return false;
        }
        String urlStr = "http://" + config.hostIP + "/status.cgi";

        // Run the HTTP POST request and get the JSON response from Oven
        String response = null;

        Properties httpHeader = new Properties();

        if (postData != null) {
            try {
                InputStream targetStream = new ByteArrayInputStream(postData.getBytes(StandardCharsets.UTF_8));
                refreshOvenConnection(helper, thingUID);
                httpHeader = createHeader(postData);
                response = HttpUtil.executeUrl("POST", urlStr, httpHeader, targetStream, "application/json", 10000);
                resultOk = true;
                logger.debug("Execute POST request with content to {} with header: {}", urlStr, httpHeader.toString());
            } catch (IOException e) {
                logger.debug("Error processiong POST request {}", urlStr);
                statusDescr = "Cannot execute command on Stove. Please verify connection and Thing Status";
                resultOk = false;
            }
        } else {
            try {
                refreshOvenConnection(helper, thingUID);
                httpHeader = createHeader(null);
                response = HttpUtil.executeUrl("POST", urlStr, httpHeader, null, "", 10000);
                resultOk = true;
                logger.debug("Execute POST request to {} with header: {}", urlStr, httpHeader.toString());
            } catch (IOException e) {
                logger.debug("Error processiong POST request {}", e.getMessage());
                String message = e.getMessage();
                if (message != null && message.contains("Authentication challenge without WWW-Authenticate ")) {
                    statusDescr = "Cannot connect to stove. Given PIN: " + config.hostPIN + " is incorrect!";
                }
                resultOk = false;
            }
        }
        if (resultOk) {
            logger.debug("OvenData = {}", response);
            ovenData = gson.fromJson(response, HaasSohnpelletstoveJsonDataDTO.class);
        } else {
            logger.debug("Setting thing '{}' to OFFLINE: Error '{}': {}", thingUID, error, errorDetail);
            ovenData = new HaasSohnpelletstoveJsonDataDTO();
        }
        helper.setStatusDescription(statusDescr);
        return resultOk;
    }

    /**
     * Creates the header for the Post Request
     *
     * @return The created Header Properties
     */
    private Properties createHeader(@Nullable String postData) {
        Properties httpHeader = new Properties();
        httpHeader.setProperty("Host", config.hostIP);
        httpHeader.setProperty("Accept", "*/*");
        httpHeader.setProperty("Proxy-Connection", "keep-alive");
        httpHeader.setProperty("X-BACKEND-IP", "https://app.haassohn.com");
        httpHeader.setProperty("Accept-Language", "de-DE;q=1.0, en-DE;q=0.9");
        httpHeader.setProperty("Accept-Encoding", "gzip;q=1.0, compress;q=0.5");
        httpHeader.setProperty("token", "32 bytes");
        httpHeader.setProperty("Content-Type", "application/json");
        if (postData != null) {
            int a = postData.getBytes(StandardCharsets.UTF_8).length;
            httpHeader.setProperty(xhspin, Integer.toString(a));
        }
        httpHeader.setProperty("User-Agent", "ios");
        httpHeader.setProperty("Connection", "keep-alive");
        httpHeader.setProperty("X-HS-PIN", xhspin);
        return httpHeader;
    }

    /**
     * Generate the valid encrypted string to communicate with the oven.
     *
     * @param ovenData
     * @return
     */
    private @Nullable String getValidXHSPIN(@Nullable HaasSohnpelletstoveJsonDataDTO ovenData) {
        if (ovenData != null && config.hostPIN != null) {
            String nonce = ovenData.getNonce();
            String hostPIN = config.hostPIN;
            String ePin = MD5Utils.getMD5String(hostPIN);
            return MD5Utils.getMD5String(nonce + ePin);
        } else {
            return null;
        }
    }

    /**
     * Set the config for service to communicate
     *
     * @param config2
     */
    public void setConfig(@Nullable HaasSohnpelletstoveConfiguration config2) {
        if (config2 != null) {
            this.config = config2;
        }
    }

    /**
     * Returns the actual stored Oven Data
     *
     * @return
     */
    @Nullable
    public HaasSohnpelletstoveJsonDataDTO getOvenData() {
        return this.ovenData;
    }
}
