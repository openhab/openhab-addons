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
package org.openhab.binding.sunsynk.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.HttpsURLConnection;

import org.openhab.binding.sunsynk.internal.classes.APIdata;
import org.openhab.binding.sunsynk.internal.classes.Battery;
import org.openhab.binding.sunsynk.internal.classes.Daytemps;
import org.openhab.binding.sunsynk.internal.classes.Grid;
import org.openhab.binding.sunsynk.internal.classes.RealTimeInData;
import org.openhab.binding.sunsynk.internal.classes.Settings;
import org.openhab.binding.sunsynk.internal.config.SunSynkInverterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SunSynkInverter} class defines methods, which contol
 * communication with the inverter.
 *
 * @author Lee Charlton - Initial contribution
 */

public class SunSynkInverter {

    private final Logger logger = LoggerFactory.getLogger(SunSynkInverter.class);
    private String uid;
    private String access_token;
    private String gsn;
    private String sn;
    private String alias;
    private int refresh;
    private Settings batterySettings; // cotains common settings including the inverter charge discharge and battert use
    private Battery realTimeBattery; // contains the realtime stus of the battery
    private Grid grid; // realtime grid
    private Daytemps inverter_day_temperatures; // inverter temperatures history
    private RealTimeInData realTimeDataIn; // realtime data

    public SunSynkInverter(SunSynkInverterConfig config) {
        // this.gsn = config.getgsn();
        // this.access_token = config.getToken();
        this.sn = config.getsn();
        this.refresh = config.getRefresh();
        this.alias = config.getAlias();
    }

    public boolean sendGetState() { // Entry method to class for updating internal state from the inverter
        logger.debug("Will get STATE for Inverter {} serial {}", this.alias, this.sn);
        // Get inverter infos
        try {
            boolean autheticated = getCommonSettings(); // battery charge settings
            if (!autheticated)
                return false;
            autheticated = getGridRealTime(); // grid status
            if (!autheticated)
                return false;
            autheticated = getBatteryRealTime(); // battery status
            if (!autheticated)
                return false;
            autheticated = getInverterACDCTemperatures(); // get Inverter temperatures
            if (!autheticated)
                return false;
            autheticated = getRealTimeIn(); // may not need this one used for solar values
            if (!autheticated)
                return false;

        } catch (Exception e) {
            logger.debug("Failed to get Inverter API information: ", e);
            // updateStatus(ThingStatus.OFFLINE);
            // Should Thing be put off line?
            // Need to Throw an exception <----- got here to stop following debug log
            return false;
        }
        logger.debug("Successfully got and parsed new data for Inverter {} serial {}", this.alias, this.sn);
        return true;
    }
    /*
     * /
     * public void sendGetGeneralInfo(){
     * try{
     * getCommonSettings(); // battery charge settings
     * getGridRealTime(); // grid status
     * getBatteryRealTime(); // battery status
     * getInverterACDCTemperatures(); // get Inverter temperatures
     * getRealTimeIn(); // may not need this one used for solar values
     * 
     * }catch (Exception e) {
     * logger.debug("Failed to get Inverter API information: ", e);
     * // Should Thing be put off line?
     * return; // should return something?
     * }
     * }
     */

    // ------ GETTERS ------ //
    public Settings getBatteryChargeSettings() {
        return this.batterySettings;
    }

    public Battery getRealTimeBatteryState() {
        return this.realTimeBattery;
    }

    public Grid getRealTimeGridStatus() {
        return this.grid;
    }

    public Daytemps getInverterTemperatureHistory() {
        return this.inverter_day_temperatures;
    }

    public RealTimeInData getRealtimeSolarStatus() {
        return this.realTimeDataIn;
    }

    // ------ SETTERS ------ //

    // ------ RETRIEVERS ------//
    // ------ Battery charge settings ------ //
    // https://api.sunsynk.net/api/v1/common/setting/2211229948/read
    @SuppressWarnings("null")
    boolean getCommonSettings() {// need to get this to run in the class, Ben suggested using a Builder
        // Get URL Respnse
        String response = apiGetMethod(makeURL("api/v1/common/setting/" + this.sn + "/read"),
                APIdata.static_access_token);
        if (response == "Authentication Fail") {
            return false;
        }
        // JSON response -> realTime data Structure
        Gson gson = new Gson();
        this.batterySettings = gson.fromJson(response, Settings.class);
        this.batterySettings.buildLists();
        return true;
    }

    // ------ Realtime Grid ------ //
    // https://api.sunsynk.net/api/v1/inverter/grid/{inverter_sn}/realtime?sn={inverter_sn}
    @SuppressWarnings("null")
    boolean getGridRealTime() {// need to get this to run in the class, Ben suggested using a Builder
        // Get URL Respnse
        String response = apiGetMethod(makeURL("api/v1/inverter/grid/" + this.sn + "/realtime?sn=") + this.sn,
                APIdata.static_access_token);
        if (response == "Authentication Fail") {
            return false;
        }
        // JSON response -> realTime data Structure
        Gson gson = new Gson();
        this.grid = gson.fromJson(response, Grid.class);
        this.grid.sumVIP();
        return true;
    }

    // ------ Realtime battery ------ //
    // https://api.sunsynk.net/api/v1/inverter/battery/{inverter_sn}/realtime?sn={inverter_sn}&lan
    @SuppressWarnings("null")
    boolean getBatteryRealTime() {
        // Get URL Respnse
        String response = apiGetMethod(
                makeURL("api/v1/inverter/battery/" + this.sn + "/realtime?sn=" + this.sn + "&lan"),
                APIdata.static_access_token);
        if (response == "Authentication Fail") {
            return false;
        }
        // JSON response -> realTime data Structure
        Gson gson = new Gson();
        this.realTimeBattery = gson.fromJson(response, Battery.class);
        return true;
    }

    // ------ Realtime acdc temperatures ------ //
    // https://api.sunsynk.net/api/v1/inverter/{inverter_sn}/output/day?lan=en&date={date}&column=dc_temp,igbt_temp
    @SuppressWarnings("null")
    boolean getInverterACDCTemperatures() {
        String date = getAPIFormatDate();
        // Get URL Respnse
        String response = apiGetMethod(
                makeURL("api/v1/inverter/" + this.sn + "/output/day?lan=en&date=" + date + "&column=dc_temp,igbt_temp"),
                APIdata.static_access_token);
        if (response == "Authentication Fail") {
            return false;
        }
        // JSON response -> realTime data Structure
        Gson gson = new Gson();
        this.inverter_day_temperatures = gson.fromJson(response, Daytemps.class);
        this.inverter_day_temperatures.getLastValue();
        return true;
    }

    // ------ Realtime Input ------ //
    // https://api.sunsynk.net//api/v1/inverter/grid/{inverter_sn}/realtime?sn={inverter_sn}
    @SuppressWarnings("null")
    boolean getRealTimeIn() {// need to get this to run in the class, Ben suggested using a Builder
        // Get URL Respnse
        String response = apiGetMethod(makeURL("api/v1/inverter/" + this.sn + "/realtime/input"),
                APIdata.static_access_token);
        if (response == "Authentication Fail") {
            return false;
        }
        // JSON response -> realTime data Structure
        Gson gson = new Gson();
        this.realTimeDataIn = gson.fromJson(response, RealTimeInData.class);
        this.realTimeDataIn.sumPVIV();
        return true;
    }
    // ------ COMMANDS ------ //

    public String sendCommandToSunSynk(String body, String access_token) {
        // GET URL
        String path = "api/v1/common/setting/" + this.sn + "/set";
        // POST COMMAND
        String postResponse = apiPostMethod(makeURL(path), body, access_token);
        return postResponse;
    }

    private static class CommandRequest {
        private String cmd;

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }
    }

    // ------ Helpers ------ //

    private String apiPostMethod(String httpsURL, String body, String access_token) {
        String response = "";
        try {
            URL myUrl = new URL(httpsURL);
            HttpsURLConnection connection = (HttpsURLConnection) myUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + access_token);
            connection.setDoOutput(true);
            OutputStream wr = connection.getOutputStream();
            wr.write(body.getBytes("UTF-8"));
            wr.flush();
            wr.close();
            logger.debug("POST Response Code: {}", connection.getResponseCode());
            // logger.debug("POST Response Message: {}", connection.getResponseMessage());
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            response = br.readLine();
            br.close();
        } catch (IOException e) {
            response = "Empty";
            logger.debug("apiPostMethod faied to get a response from {}} with token {} Eception.", httpsURL,
                    access_token, e);
        }
        return response;
    }

    private String apiGetMethod(String httpsURL, String access_token) {
        String response = "";
        try {
            URL myUrl = new URL(httpsURL);
            HttpsURLConnection connection = (HttpsURLConnection) myUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + access_token);
            connection.setDoOutput(true);
            logger.debug("GET Response Code: {}.", connection.getResponseCode());
            if (connection.getResponseCode() == 401) {
                logger.debug("Authentication failure");
                return "Authentication Fail";
            }
            // logger.debug("GET Response Message: {}.", connection.getResponseMessage());
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            response = br.readLine();
            br.close();
        } catch (IOException e) {
            response = "Empty";
            logger.debug("apiGetMethod faied to get a response from {}} with token {} Eception ", httpsURL,
                    access_token, e);
        }
        return response;
    }

    private String makeURL(String path) {
        return "https://api.sunsynk.net" + "/" + path;
    }

    private String getAPIFormatDate() {
        LocalDate date = LocalDate.now(); // Create a date object
        DateTimeFormatter APIformat = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 2023-12-14
        String formattedDate = date.format(APIformat);
        return formattedDate;
    }
}
