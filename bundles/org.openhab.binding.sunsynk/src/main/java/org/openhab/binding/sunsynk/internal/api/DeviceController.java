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
package org.openhab.binding.sunsynk.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsynk.internal.api.dto.APIdata;
import org.openhab.binding.sunsynk.internal.api.dto.Battery;
import org.openhab.binding.sunsynk.internal.api.dto.Daytemps;
import org.openhab.binding.sunsynk.internal.api.dto.Grid;
import org.openhab.binding.sunsynk.internal.api.dto.RealTimeInData;
import org.openhab.binding.sunsynk.internal.api.dto.Settings;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkGetStatusException;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkSendCommandException;
import org.openhab.binding.sunsynk.internal.config.SunSynkInverterConfig;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link DeviceController} class defines methods that control
 * communication with the inverter.
 *
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class DeviceController {

    private final Logger logger = LoggerFactory.getLogger(DeviceController.class);
    private String sn = "";
    private String alias = "";
    private @Nullable Settings batterySettings = new Settings();
    private @Nullable Battery realTimeBattery = new Battery();
    private @Nullable Grid grid = new Grid();
    private @Nullable Daytemps inverter_day_temperatures = new Daytemps();
    private @Nullable RealTimeInData realTimeDataIn = new RealTimeInData();
    public @Nullable Settings tempInverterChargeSettings = new Settings(); // Holds modified battery settings.

    public DeviceController() {
    }

    public DeviceController(SunSynkInverterConfig config) {
        this.sn = config.getsn();
        this.alias = config.getAlias();
    }

    public void sendGetState(boolean batterySettingsUpdate) throws SunSynkGetStatusException { // Class entry method to
                                                                                               // update internal
        // inverter state
        logger.debug("Will get STATE for Inverter {} serial {}", this.alias, this.sn);
        try {
            if (!batterySettingsUpdate) { // normally get settings to track changes made by other UIs
                logger.debug("Trying Common Settings");
                getCommonSettings(); // battery charge settings
            }
            logger.debug("Trying Grid Real Time Settings");
            getGridRealTime(); // grid status
            logger.debug("Trying Battery Real Time Settings");
            getBatteryRealTime(); // battery status
            logger.debug("Trying Temperature History");
            getInverterACDCTemperatures(); // get Inverter temperatures
            logger.debug("Trying Real Time Solar");
            getRealTimeIn(); // Used for solar power now
        } catch (IOException e) {
            logger.debug("Failed to send to Inverter API: {} ", e.getMessage());
            int found = e.getMessage().indexOf("Authentication challenge without WWW-Authenticate header");
            if (found > -1) {
                throw new SunSynkGetStatusException("Authentication token failed", e);
            }
            throw new SunSynkGetStatusException("Unknown athentication fail", e);
        }
        logger.debug("Successfully got and parsed new data for Inverter {} serial {}", this.alias, this.sn);
    }

    public @Nullable Settings getBatteryChargeSettings() {
        return this.batterySettings;
    }

    public @Nullable Battery getRealTimeBatteryState() {
        return this.realTimeBattery;
    }

    public @Nullable Grid getRealTimeGridStatus() {
        return this.grid;
    }

    public @Nullable Daytemps getInverterTemperatureHistory() {
        return this.inverter_day_temperatures;
    }

    public @Nullable RealTimeInData getRealtimeSolarStatus() {
        return this.realTimeDataIn;
    }

    private void getCommonSettings() throws IOException, JsonSyntaxException {
        String response = apiGetMethod(makeURL("api/v1/common/setting/" + this.sn + "/read"),
                APIdata.static_access_token);
        Gson gson = new Gson();
        this.batterySettings = gson.fromJson(response, Settings.class);
        this.batterySettings.buildLists();
    }

    private void getGridRealTime() throws IOException, JsonSyntaxException {
        String response = apiGetMethod(makeURL("api/v1/inverter/grid/" + this.sn + "/realtime?sn=") + this.sn,
                APIdata.static_access_token);
        Gson gson = new Gson();
        this.grid = gson.fromJson(response, Grid.class);
        this.grid.sumVIP();
    }

    private void getBatteryRealTime() throws IOException, JsonSyntaxException {
        String response = apiGetMethod(
                makeURL("api/v1/inverter/battery/" + this.sn + "/realtime?sn=" + this.sn + "&lan"),
                APIdata.static_access_token);
        Gson gson = new Gson();
        this.realTimeBattery = gson.fromJson(response, Battery.class);
    }

    private void getInverterACDCTemperatures() throws IOException, JsonSyntaxException {
        String date = getAPIFormatDate();
        String response = apiGetMethod(
                makeURL("api/v1/inverter/" + this.sn + "/output/day?lan=en&date=" + date + "&column=dc_temp,igbt_temp"),
                APIdata.static_access_token);
        Gson gson = new Gson();
        this.inverter_day_temperatures = gson.fromJson(response, Daytemps.class);
        this.inverter_day_temperatures.getLastValue();
    }

    private void getRealTimeIn() throws IOException, JsonSyntaxException { // Get URL Respnse
        String response = apiGetMethod(makeURL("api/v1/inverter/" + this.sn + "/realtime/input"),
                APIdata.static_access_token);
        Gson gson = new Gson();
        this.realTimeDataIn = gson.fromJson(response, RealTimeInData.class);
        this.realTimeDataIn.sumPVIV();
    }

    public void sendSettings(@Nullable Settings settings) throws SunSynkSendCommandException {
        String body = settings.buildBody();
        sendCommandToSunSynk(body);
    }

    public void sendCommandToSunSynk(String body) throws SunSynkSendCommandException {
        String path = "api/v1/common/setting/" + this.sn + "/set";

        try {
            apiPostMethod(makeURL(path), body, APIdata.static_access_token);
        } catch (IOException e) {
            logger.debug("Failed to send to Inverter API: {} ", e.getMessage());
            int found = e.getMessage().indexOf("Authentication challenge without WWW-Authenticate header");
            if (found > -1) {
                throw new SunSynkSendCommandException("Authentication token failed", e);
            }
            throw new SunSynkSendCommandException("Unknown athentication fail", e);
        }
    }

    private String apiPostMethod(String httpsURL, String body, String access_token) throws IOException {
        Properties headers = new Properties();
        headers.setProperty("Accept", "application/json");
        headers.setProperty("Authorization", "Bearer " + access_token);
        headers.setProperty("Requester", "www.openhab.org"); // optional
        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return HttpUtil.executeUrl("POST", httpsURL, headers, stream, "application/json", 2000);
    }

    private String apiGetMethod(String httpsURL, String access_token) throws IOException {
        Properties headers = new Properties();
        headers.setProperty("Accept", "application/json");
        headers.setProperty("Requester", "www.openhab.org"); // optional
        headers.setProperty("Authorization", "Bearer " + access_token);
        return HttpUtil.executeUrl("GET", httpsURL, headers, null, "application/json", 2000);
    }

    private String makeURL(String path) {
        return "https://api.sunsynk.net" + "/" + path;
    }

    private String getAPIFormatDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter APIformat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(APIformat);
    }
}
