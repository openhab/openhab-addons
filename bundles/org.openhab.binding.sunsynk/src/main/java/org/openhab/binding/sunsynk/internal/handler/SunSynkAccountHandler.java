/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.handler;

//import static org.openhab.binding.sunsynk.internal.SunSynkBindingConstants.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.openhab.binding.sunsynk.internal.classes.APIdata;
import org.openhab.binding.sunsynk.internal.classes.Client;
import org.openhab.binding.sunsynk.internal.classes.Details;
import org.openhab.binding.sunsynk.internal.classes.Inverter;
import org.openhab.binding.sunsynk.internal.config.SunSynkAccountConfig;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SunSynkAccountHandler} is responsible for handling the SunSynk Account Bridge
 *
 *
 * @author Lee Charlton - Initial contribution
 */
// @NonNullByDefault

public class SunSynkAccountHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SunSynkAccountHandler.class);

    public SunSynkAccountHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        logger.debug("SunSynk Handler Intialised attempting to retrieve configuration");
        // Map<String, String> props = getThing().getProperties();
        // Map<String, String> editProps = editProperties();
        // logger.debug("Account properties: " + props);
        configAccount();
    }

    public void setBridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public /* @NonNull */ List<Inverter> getInvertersFromSunSynk() {
        logger.debug("Attempting to find inverters tied to account");
        // Discover Connected plants and inverters.
        Details sunAccountDetails = getDetails(APIdata.static_access_token);
        ArrayList<Inverter> inverters = sunAccountDetails.getInverters(APIdata.static_access_token);
        // List<Inverterdata.InverterInfo> inverters = sunAccountDetails.getInverters();
        if (!inverters.isEmpty() | inverters != null) {
            return inverters;
        }
        return new ArrayList<>();
    }

    private Details getDetails(String access_token) {

        try {
            Gson gson = new Gson();
            Properties headers = new Properties();
            String response = "";
            String httpsURL = makeLoginURL(
                    "api/v1/inverters?page=1&limit=10&total=0&status=-1&sn=&plantId=&type=-2&softVer=&hmiVer=&agentCompanyId=-1&gsn=");
            headers.setProperty("Accept", "application/json");
            headers.setProperty("Authorization", "Bearer " + access_token);
            response = HttpUtil.executeUrl("GET", httpsURL, headers, null, "application/json", 2000);
            Details details = gson.fromJson(response, Details.class);
            return details;
        } catch (IOException e) {
            logger.debug("Error attempting to find inverters registered to account", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to find inverters registered to account");
            Details details = new Details();
            return details;
        }
    }

    public void configAccount() {
        SunSynkAccountConfig accountConfig = getConfigAs(SunSynkAccountConfig.class);
        Client sunAccount = authenticate(accountConfig.getEmail(), accountConfig.getPassword());
        Optional<APIdata> checkAPI = sunAccount.safeAPIData();
        if (!checkAPI.isPresent()) { // API Data failed
            logger.debug("Account fialed to Authenticate, likely a certificate path or maybe a password problem.");
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        APIdata apiData = checkAPI.get();
        String newToken = apiData.getAccessToken();
        APIdata.static_access_token = newToken;
        Configuration configuration = editConfiguration();
        configuration.put("access_token", newToken);
        configuration.put("refresh_token", sunAccount.getRefreshTokenString());
        long baseTime = new Date().getTime(); // Time Now
        baseTime = baseTime + (sunAccount.getExpiresIn() * 1000L);
        java.util.Date time = new java.util.Date((long) baseTime);
        configuration.put("expires_in", time.toString());
        updateConfiguration(configuration);
        logger.debug("Account configuration updated : {}", configuration);
    }

    private Client authenticate(String username, String password) {
        try {
            Gson gson = new Gson();
            String response = "";
            String httpsURL = makeLoginURL("oauth/token");
            String payload = makeLoginBody(username, password);
            Properties headers = new Properties();
            headers.setProperty("Accept", "application/json");
            headers.setProperty("Content-Type", "application/json"); // may not need this.
            InputStream stream = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
            response = HttpUtil.executeUrl("POST", httpsURL, headers, stream, "application/json", 2000);
            Client API_Token = gson.fromJson(response, Client.class);
            return API_Token;
        } catch (IOException e) {
            logger.debug("Error attempting to autheticate account", e.getCause());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to authenticate account");
            Client API_Token = new Client();
            return API_Token;
        }
    }

    private static String makeLoginURL(String path) {
        return "https://api.sunsynk.net" + "/" + path;
    }

    private static String makeLoginBody(String username, String password) {
        String body = "{\"username\": \"" + username + "\", \"password\": \"" + password
                + "\", \"grant_type\": \"password\", \"client_id\": \"csp-web\"}";
        return body;
    }
}
