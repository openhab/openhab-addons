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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.openhab.binding.sunsynk.internal.classes.APIdata;
import org.openhab.binding.sunsynk.internal.classes.Client;
import org.openhab.binding.sunsynk.internal.classes.Details;
import org.openhab.binding.sunsynk.internal.classes.Inverter;
import org.openhab.binding.sunsynk.internal.config.SunSynkAccountConfig;
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
        logger.debug("SunSynk Handler Intialised");
    }

    public /* @NonNull */ List<Inverter> getInvertersFromSunSynk() {
        logger.debug("Attempting to find inverters tied to account");
        SunSynkAccountConfig accountConfig = getConfigAs(SunSynkAccountConfig.class);
        Client sunAccount = authenticate(accountConfig.getEmail(), accountConfig.getPassword());
        // accessToken =
        // //{"code":0,"msg":"Success","data":{"access_token":"xxxxxxx","token_type":"bearer","refresh_token":"xxxxxxxx","expires_in":258669,"scope":"all"},"success":true}

        APIdata apiData = sunAccount.getData();
        String newToken = apiData.getAccessToken();
        APIdata.static_access_token = newToken;

        logger.debug("Account token: {}", APIdata.static_access_token);

        logger.debug("Account expires: {}", sunAccount.getExpiresIn());

        // Discover Connected plants and inverters.
        Details sunAccountDetails = getDetails(APIdata.static_access_token);
        ArrayList<Inverter> inverters = sunAccountDetails.getInverters(APIdata.static_access_token);

        // List<Inverterdata.InverterInfo> inverters = sunAccountDetails.getInverters();
        // got here could convert to array list or just return HashMap

        if (!inverters.isEmpty() | inverters != null) {
            return inverters;
        }
        return new ArrayList<>();
    }

    private Details getDetails(String access_token) {

        String response = "";
        String httpsURL = makeLoginURL(
                "api/v1/inverters?page=1&limit=10&total=0&status=-1&sn=&plantId=&type=-2&softVer=&hmiVer=&agentCompanyId=-1&gsn=");
        try {
            URL myUrl = new URL(httpsURL);
            HttpsURLConnection connection = (HttpsURLConnection) myUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + access_token);

            connection.setDoOutput(true);

            logger.debug("Details response code: {}", connection.getResponseCode());
            logger.debug("Details response message: {}", connection.getResponseMessage());

            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            response = br.readLine();
            br.close();
            Gson gson = new Gson();
            Details details = gson.fromJson(response, Details.class);
            return details;
        } catch (IOException e) {
            logger.debug("Error attempting to autheticate account", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to authenticate account");

            Details details = new Details();
            return details;
        }
    }

    private Client authenticate(String username, String password) {
        String response = "";
        String httpsURL = makeLoginURL("oauth/token");
        try {
            URL myUrl = new URL(httpsURL);
            String payload = makeLoginBody(username, password);
            HttpsURLConnection connection = (HttpsURLConnection) myUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream outStream = connection.getOutputStream();
            OutputStreamWriter outStreamWriter = new OutputStreamWriter(outStream, "UTF-8");
            outStreamWriter.write(payload);
            outStreamWriter.flush();
            outStreamWriter.close();
            outStream.close();

            logger.debug("Authentication response code: {}", connection.getResponseCode());
            logger.debug("Authentication response message: {}", connection.getResponseMessage());

            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            response = br.readLine();
            br.close();
            Gson gson = new Gson();
            Client API_Token = gson.fromJson(response, Client.class);
            return API_Token;

        } catch (IOException e) {
            logger.debug("Error attempting to find inverters registered to account", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error attempting to find inverters registered to account");

            Client API_Token = new Client();
            return API_Token;
        }
    }

    private static String makeLoginURL(String path) {
        return "https://api.sunsynk.net" + "/" + path;
    }

    private static String makeLoginBody(String username, String password) {
        // String body = "{\"username\": \"jo.blogs@gmail.com\", \"password\": \"pssword\", \"grant_type\":
        // \"password\", \"client_id\": \"csp-web\"}";
        String body = "{\"username\": \"" + username + "\", \"password\": \"" + password
                + "\", \"grant_type\": \"password\", \"client_id\": \"csp-web\"}";
        return body;
    }
}
