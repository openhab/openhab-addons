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
package org.openhab.binding.sunsynk.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.sunsynk.internal.api.dto.APIdata;
import org.openhab.binding.sunsynk.internal.api.dto.Battery;
import org.openhab.binding.sunsynk.internal.api.dto.Daytemps;
import org.openhab.binding.sunsynk.internal.api.dto.Grid;
import org.openhab.binding.sunsynk.internal.api.dto.PlantSummary;
import org.openhab.binding.sunsynk.internal.api.dto.RealTimeInData;
import org.openhab.binding.sunsynk.internal.api.dto.Settings;
import org.openhab.binding.sunsynk.internal.api.exception.SunSynkDeviceControllerException;
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
 * It collects data from Sun Synk Connect for the various data objects defined in the dto package.
 * It also sends commands to the inverter via Sun Synk Connect to change settings.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class DeviceController {

    private static final int TIMEOUT_IN_MS = 4000;
    private final Logger logger = LoggerFactory.getLogger(DeviceController.class);
    private static final String BEARER_TYPE = "Bearer ";
    private int successFlags = 0;
    private String sn = "";
    private String alias = "";
    private String plantId = "";
    private String plantName = "";
    private Settings batterySettings = new Settings();
    private Battery realTimeBattery = new Battery();
    private Grid grid = new Grid();
    private Daytemps inverterDayTemperatures = new Daytemps();
    private RealTimeInData realTimeDataIn = new RealTimeInData();
    public static final int COMMONSETTINGS = 1 << 0;
    public static final int GRIDREALTIME = 1 << 1;
    public static final int BATTERYREALTIME = 1 << 2;
    public static final int INVERTERDAYTEMPS = 1 << 3;
    public static final int REALTIMEIN = 1 << 4;
    public static final int PLANTSUMMARY = 1 << 5;
    public static final Map<String, Integer> statusMap = new HashMap<>();
    static {
        statusMap.put("CommonSettings", COMMONSETTINGS);
        statusMap.put("GridRealTime", GRIDREALTIME);
        statusMap.put("BatteryRealTime", BATTERYREALTIME);
        statusMap.put("InverterDayTemps", INVERTERDAYTEMPS);
        statusMap.put("RealTimeIn", REALTIMEIN);
        statusMap.put("PlantSummary", PLANTSUMMARY);
    }
    public Settings tempInverterChargeSettings = new Settings(); // Holds modified battery settings.
    public PlantSummary plantSummary = new PlantSummary();

    public DeviceController() {
    }

    /**
     * Sets the identity of the device (inverter) according to the configuration parameters;
     * serial number and alias.
     * 
     * @param config
     */
    public DeviceController(SunSynkInverterConfig config) {
        this.sn = config.getSerialnumber();
        this.alias = config.getAlias();
        this.plantId = config.getPlantId();
        this.plantName = config.getPlantName();
    }

    /**
     * Entry method to get status of the concrete device (inverter) and update the internal state.
     * 
     * @param batterySettingsUpdate used to skip the battery charge settings status
     * @throws SunSynkGetStatusException
     * @throws JsonSyntaxException
     * @throws SunSynkDeviceControllerException
     * @see Settings
     * @see Grid
     * @see Battery
     * @see Daytemps
     * @see RealTimeInData
     * @see PlantSummary
     * @return successFlags bitmask of successful data retrievals
     */
    public int sendGetState(boolean batterySettingsUpdate)
            throws SunSynkGetStatusException, JsonSyntaxException, SunSynkDeviceControllerException {
        logger.debug("Will get STATE for Inverter {} serial {}", this.alias, this.sn);
        successFlags = 0; // tracks the number of good responses
        try {
            if (!batterySettingsUpdate) { // normally get settings to track changes made by other UIs
                getCommonSettings(); // battery charge settings
            }
            getGridRealTime(); // grid status
            getBatteryRealTime(); // battery status
            getInverterACDCTemperatures(); // get Inverter temperatures
            getRealTimeIn(); // used for solar power now
            getSummary(); // used for plant totals
        } catch (IOException e) {
            String message = Objects.requireNonNullElse(e.getMessage(), "unknown error message.");
            logger.debug("Failed to send to Inverter API: {}.", message);
            int found = message.indexOf("Authentication challenge without WWW-Authenticate header.");
            if (found > -1) {
                throw new SunSynkGetStatusException("Authentication token failed.", e);
            }
            throw new SunSynkGetStatusException("Unknown authentication fail.", e);
        }
        return successFlags;
    }

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
        return this.inverterDayTemperatures;
    }

    public RealTimeInData getRealtimeSolarStatus() {
        return this.realTimeDataIn;
    }

    public PlantSummary getPlantSummary() {
        return this.plantSummary;
    }

    @SuppressWarnings("unused")
    private void getCommonSettings() throws IOException, JsonSyntaxException, SunSynkDeviceControllerException {
        logger.debug("Trying Common Settings");
        String response = apiGetMethod(makeURL("common/setting/" + this.sn + "/read", ""), APIdata.staticAccessToken);
        logger.trace("Common Settings: {}", response);
        Gson gson = new Gson();
        @Nullable
        Settings settings = gson.fromJson(response, Settings.class);
        if (settings == null) {
            throw new SunSynkDeviceControllerException("Could not retrieve battery charge settings.");
        }
        if (settings.getCode() != 0) {
            logger.debug("Failed to get common settings: {}.", settings.getMsg());
        } else {
            this.batterySettings = settings;
            this.batterySettings.buildLists();
            successFlags = successFlags | COMMONSETTINGS;
        }
    }

    @SuppressWarnings("unused")
    private void getGridRealTime() throws IOException, JsonSyntaxException, SunSynkDeviceControllerException {
        logger.debug("Trying Grid Real Time Settings");
        String response = apiGetMethod(makeURL("inverter/grid/" + this.sn + "/realtime", "sn=" + this.sn),
                APIdata.staticAccessToken);
        logger.trace("Grid Real Time Settings: {}", response);
        Gson gson = new Gson();
        @Nullable
        Grid grid = gson.fromJson(response, Grid.class);
        if (grid == null) {
            throw new SunSynkDeviceControllerException("Could not retrieve grid state.");
        }
        if (grid.getCode() != 0) {
            logger.debug("Failed to get grid real time values: {}.", grid.getMsg());
        } else {
            this.grid = grid;
            this.grid.sumVIP();
            successFlags = successFlags | GRIDREALTIME;
        }
    }

    @SuppressWarnings("unused")
    private void getBatteryRealTime() throws IOException, JsonSyntaxException, SunSynkDeviceControllerException {
        logger.debug("Trying Battery Real Time Settings");
        String response = apiGetMethod(makeURL("inverter/battery/" + this.sn + "/realtime", "sn=" + this.sn + "&lan"),
                APIdata.staticAccessToken);
        logger.trace("Battery Real Time Settings: {}", response);
        Gson gson = new Gson();
        @Nullable
        Battery battery = gson.fromJson(response, Battery.class);
        if (battery == null) {
            throw new SunSynkDeviceControllerException("Could not retrieve battery state.");
        }
        if (battery.getCode() != 0) {
            logger.debug("Failed to get battery real time values: {}.", battery.getMsg());
        } else {
            this.realTimeBattery = battery;
            successFlags = successFlags | BATTERYREALTIME;
        }
    }

    @SuppressWarnings("unused")
    private void getInverterACDCTemperatures()
            throws IOException, JsonSyntaxException, SunSynkDeviceControllerException {
        logger.debug("Trying Temperature History");
        String date = getAPIFormatDate();
        String response = apiGetMethod(
                makeURL("inverter/" + this.sn + "/output/day", "lan=en&date=" + date + "&column=dc_temp,igbt_temp"),
                APIdata.staticAccessToken);
        logger.trace("Temperature History: {}", response);
        Gson gson = new Gson();
        @Nullable
        Daytemps daytemps = gson.fromJson(response, Daytemps.class);
        if (daytemps == null) {
            throw new SunSynkDeviceControllerException("Could not retrieve inverter temperatures.");
        }
        if (daytemps.getCode() != 0) {
            logger.debug("Failed to get inverter temperatures: {}.", daytemps.getMsg());
        } else {
            this.inverterDayTemperatures = daytemps;
            this.inverterDayTemperatures.getLastValue();
            successFlags = successFlags | INVERTERDAYTEMPS;
        }
    }

    @SuppressWarnings("unused")
    private void getRealTimeIn() throws IOException, JsonSyntaxException, SunSynkDeviceControllerException { // Get URL
                                                                                                             // response
        logger.debug("Trying Real Time Solar");
        String response = apiGetMethod(makeURL("inverter/" + this.sn + "/realtime/input", ""),
                APIdata.staticAccessToken);
        logger.trace("Real Time Solar Response: {}", response);
        Gson gson = new Gson();
        @Nullable
        RealTimeInData realTimeInData = gson.fromJson(response, RealTimeInData.class);
        if (realTimeInData == null) {
            throw new SunSynkDeviceControllerException("Could not retrieve solar state.");
        }
        if (realTimeInData.getCode() != 0) {
            logger.debug("Failed to get solar realtime values: {}.", realTimeInData.getMsg());
        } else {
            this.realTimeDataIn = realTimeInData;
            this.realTimeDataIn.stringEval();
            successFlags = successFlags | REALTIMEIN;
        }
    }

    @SuppressWarnings("unused")
    private void getSummary() throws IOException, JsonSyntaxException, SunSynkDeviceControllerException {
        logger.debug("Trying Plant Summary");
        String response = apiGetMethod(makeURL("plant/" + this.plantId + "/realtime", "id=" + this.plantId),
                APIdata.staticAccessToken);
        logger.trace("Plant Summary Response: {}", response);
        Gson gson = new Gson();
        @Nullable
        PlantSummary summary = gson.fromJson(response, PlantSummary.class);
        if (summary == null) {
            throw new SunSynkDeviceControllerException("Could not retrieve plant summary.");
        }
        if (summary.getCode() != 0) {
            logger.debug("Failed to get plant summary values for plant {} : {} .", this.plantName, summary.getMsg());
        } else {
            this.plantSummary = summary;
            successFlags = successFlags | PLANTSUMMARY;
        }
    }

    /**
     * Sends the internal battery charge and discharge settings to the concrete device (inverter)
     * of a Sun Synk Connect Account
     * 
     * @param settings
     * @throws SunSynkSendCommandException
     */
    public void sendSettings(Settings settings) throws SunSynkSendCommandException {
        String body = settings.buildBody();
        sendCommandToSunSynk(body);
    }

    private void sendCommandToSunSynk(String body) throws SunSynkSendCommandException {

        try {
            apiPostMethod(makeURL("common/setting/" + this.sn + "/set", ""), body, APIdata.staticAccessToken);
        } catch (IOException e) {
            String message = Objects.requireNonNullElse(e.getMessage(), "unknown error message");
            logger.debug("Failed to send to Inverter API: {} ", message);
            int found = message.indexOf("Authentication challenge without WWW-Authenticate header");
            if (found > -1) {
                throw new SunSynkSendCommandException("Authentication token failed", e);
            }
            throw new SunSynkSendCommandException("Unknown authentication fail", e);
        }
        logger.debug("Sent command: to inverter {}.", this.sn);
    }

    private String apiPostMethod(String httpsURL, String body, String accessToken) throws IOException {
        Properties headers = new Properties();
        headers.setProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.setProperty(HttpHeaders.AUTHORIZATION, BEARER_TYPE + accessToken);
        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return HttpUtil.executeUrl(HttpMethod.POST.asString(), httpsURL, headers, stream, MediaType.APPLICATION_JSON,
                TIMEOUT_IN_MS);
    }

    private String apiGetMethod(String httpsURL, String accessToken) throws IOException {
        Properties headers = new Properties();
        headers.setProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        headers.setProperty(HttpHeaders.AUTHORIZATION, BEARER_TYPE + accessToken);
        return HttpUtil.executeUrl(HttpMethod.GET.asString(), httpsURL, headers, null, MediaType.APPLICATION_JSON,
                TIMEOUT_IN_MS);
    }

    private String makeURL(String endPoint, String queryParameters) {
        String query = "?";
        if ("".equals(queryParameters)) {
            query = "";
        }
        return "https://api.sunsynk.net/api/v1/" + endPoint + query + queryParameters;
    }

    private String getAPIFormatDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
