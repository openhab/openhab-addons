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
package org.openhab.binding.haassohnpelletstove.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    private String xhspin = "";
    private @Nullable HaasSohnpelletstoveJsonDataDTO ovenData;

    public HaasSohnpelletstoveJSONCommunication() {
        gson = new Gson();
        ovenData = new HaasSohnpelletstoveJsonDataDTO();
        config = new HaasSohnpelletstoveConfiguration();
    }

    /**
     * Refreshes the oven Connection with the internal oven token.
     *
     * @return an empty string if no error occurred, the error message otherwise.
     */
    public String refreshOvenConnection() {
        String result = "";
        HaasSohnpelletstoveJsonDataDTO responseObject = null;
        String urlStr = "http://" + config.hostIP + "/status.cgi";

        String response = null;
        try {
            response = HttpUtil.executeUrl("GET", urlStr, 10000);
            logger.debug("OvenData = {}", response);
            responseObject = gson.fromJson(response, HaasSohnpelletstoveJsonDataDTO.class);
            ovenData = responseObject;
            xhspin = getValidXHSPIN(ovenData);
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("Error processiong Get request {}", urlStr);
            result = "Timeout error with " + config.hostIP
                    + ". Cannot find service on given IP. Please verify the IP-Address!";
            logger.debug("Error in establishing connection: {}", e.getMessage());
        } catch (Exception e) {
            logger.debug("Unknwon Error: {}", e.getMessage());
            ovenData = new HaasSohnpelletstoveJsonDataDTO();
        }
        return result;
    }

    /**
     * Gets the status of the oven
     *
     * @return an empty string if no error occurred, the error message otherwise.
     */
    public String updateOvenData(@Nullable String postData) {
        String error = "";
        String urlStr = "http://" + config.hostIP + "/status.cgi";
        // Run the HTTP POST request and get the JSON response from Oven
        String response = null;
        Properties httpHeader = new Properties();

        try {
            InputStream targetStream = null;
            if (postData != null) {
                targetStream = new ByteArrayInputStream(postData.getBytes(StandardCharsets.UTF_8));
            }
            refreshOvenConnection();
            httpHeader = createHeader(postData != null ? postData : null);
            response = HttpUtil.executeUrl("POST", urlStr, httpHeader, targetStream != null ? targetStream : null,
                    "application/json", 10000);
            logger.debug("Execute POST request with content to {} with header: {}", urlStr, httpHeader.toString());
            ovenData = gson.fromJson(response, HaasSohnpelletstoveJsonDataDTO.class);
            logger.debug("OvenData = {}", response);
        } catch (IOException e) {
            logger.debug("Error processiong POST request {}", urlStr);
            error = "Cannot execute command on Stove. Please verify connection or PIN";

        } catch (JsonSyntaxException e) {
            logger.debug("Error in establishing connection: {}", e.getMessage());
            error = "Cannot find service on given IP " + config.hostIP + ". Please verify the IP address!";
        }
        return error;
    }

    /**
     * Creates the header for the Post Request
     *
     * @return The created Header Properties
     */
    private Properties createHeader(@Nullable String postData) {
        Properties httpHeader = new Properties();
        httpHeader.setProperty("Host", Objects.requireNonNull(config.hostIP));
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
     * @return XHSPIN or empty when it cannot be determined
     */
    private String getValidXHSPIN(@Nullable HaasSohnpelletstoveJsonDataDTO ovenData) {
        if (ovenData != null && config.hostPIN != null) {
            String nonce = ovenData.getNonce();
            String hostPIN = config.hostPIN;
            String ePin = MD5Utils.getMD5String(hostPIN);
            return MD5Utils.getMD5String(nonce + ePin);
        } else {
            return "";
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
