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
package org.openhab.binding.growatt.internal.cloud;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.growatt.internal.config.GrowattInverterConfiguration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * The {@link GrowattCloud} class allows the binding to access the inverter state and settings via HTTP calls to the
 * remote Growatt cloud API server (instead of receiving the data from the local Grott proxy server).
 * <p>
 * This class is necessary since the Grott proxy server does not (yet) support easy access to some inverter register
 * settings, such as the settings for the battery charging and discharging programs.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GrowattCloud implements AutoCloseable {

    // JSON field names for the battery charging program
    public static final String CHARGE_PROGRAM_POWER = "chargePowerCommand";
    public static final String CHARGE_PROGRAM_TARGET_SOC = "wchargeSOCLowLimit2";
    public static final String CHARGE_PROGRAM_ALLOW_AC_CHARGING = "acChargeEnable";
    public static final String CHARGE_PROGRAM_START_TIME = "forcedChargeTimeStart1";
    public static final String CHARGE_PROGRAM_STOP_TIME = "forcedChargeTimeStop1";
    public static final String CHARGE_PROGRAM_ENABLE = "forcedChargeStopSwitch1";

    // JSON field names for the battery discharging program
    public static final String DISCHARGE_PROGRAM_POWER = "disChargePowerCommand";
    public static final String DISCHARGE_PROGRAM_TARGET_SOC = "wdisChargeSOCLowLimit2";
    public static final String DISCHARGE_PROGRAM_START_TIME = "forcedDischargeTimeStart1";
    public static final String DISCHARGE_PROGRAM_STOP_TIME = "forcedDischargeTimeStop1";
    public static final String DISCHARGE_PROGRAM_ENABLE = "forcedDischargeStopSwitch1";

    // API server URL
    private static final String SERVER_URL = "https://server-api.growatt.com/";

    // API end points
    private static final String LOGIN_API = "newTwoLoginAPI.do";
    private static final String PLANT_LIST_API = "PlantListAPI.do";
    private static final String PLANT_INFO_API = "newTwoPlantAPI.do";
    private static final String NEW_TCP_SET_API = "newTcpsetAPI.do";
    private static final String NEW_MIX_API = "newMixApi.do";

    // HTTP headers (user agent is spoofed to mimic the Growatt Android Shine app)
    private static final String USER_AGENT = "Dalvik/2.1.0 (Linux; U; Android 12; https://www.openhab.org)";
    private static final String FORM_CONTENT = "application/x-www-form-urlencoded";

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(5);

    private final Logger logger = LoggerFactory.getLogger(GrowattCloud.class);
    private final HttpClient httpClient;
    private final GrowattInverterConfiguration configuration;

    /**
     * Constructor.
     *
     * @param configuration the thing configuration parameters.
     * @param httpClientFactory the OH core {@link HttpClientFactory} instance.
     * @throws Exception if anything goes wrong.
     */
    public GrowattCloud(GrowattInverterConfiguration configuration, HttpClientFactory httpClientFactory)
            throws Exception {
        this.configuration = configuration;
        this.httpClient = httpClientFactory.createHttpClient("growatt-cloud-api", new SslContextFactory.Client(true));
        this.httpClient.start();
    }

    @Override
    public void close() throws Exception {
        httpClient.stop();
    }

    /**
     * Create a hash of the given password using normal MD5, except add 'c' if a byte of the digest is less than 10
     *
     * @param password the plain text password
     * @return the hash of the password
     * @throws ApiException if MD5 algorithm is not supported
     */
    private static String createHash(String password) throws ApiException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ApiException("Hash algorithm error", e);
        }
        byte[] bytes = md.digest(password.getBytes());
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        for (int i = 0; i < result.length(); i += 2) {
            if (result.charAt(i) == '0') {
                result.replace(i, i + 1, "c");
            }
        }
        return result.toString();
    }

    /**
     * Execute an HTTP request using the given HTTP method, to the given end point, and with the given request URL
     * parameters and/or request form fields.
     *
     * @param method the HTTP method to use.
     * @param endPoint the API end point.
     * @param params the request URL parameters (may be null).
     * @param fields the request form fields (may be null).
     * @return a Map of JSON elements containing the server response.
     * @throws ApiException if any error occurs.
     */
    public Map<String, JsonElement> doHttpRequest(HttpMethod method, String endPoint,
            @Nullable Map<String, String> params, @Nullable Fields fields) throws ApiException {

        Request request = httpClient.newRequest(SERVER_URL + endPoint).method(method)
                .header(HttpHeader.USER_AGENT, USER_AGENT).timeout(HTTP_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        if (params != null) {
            params.entrySet().forEach(p -> request.param(p.getKey(), p.getValue()));
        }

        if (fields != null) {
            request.content(new FormContentProvider(fields), FORM_CONTENT);
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new ApiException("HTTP I/O Exception", e);
        }

        int status = response.getStatus();
        if (status != HttpStatus.OK_200) {
            throw new ApiException(String.format("HTTP %d %s", status, HttpStatus.getMessage(status)));
        }

        String content = response.getContentAsString();
        if (content == null || content.isBlank()) {
            throw new ApiException("HTTP response content is " + (content == null ? "null" : "blank"));
        }

        logger.trace("doHttpRequest() response:{}", content);
        try {
            JsonElement jsonObject = JsonParser.parseString(content).getAsJsonObject();
            if (jsonObject instanceof JsonObject jsonElement) {
                return jsonElement.asMap();
            }
            throw new ApiException("JSON invalid response");
        } catch (JsonParseException | IllegalStateException e) {
            throw new ApiException("JSON parse error", e);
        }
    }

    /**
     * Get all of the mix inverter settings.
     *
     * @return a Map of JSON elements containing the server response.
     * @throws ApiException if any error occurs.
     */
    public Map<String, JsonElement> getMixAllSettings() throws ApiException {
        Map<String, String> params = new LinkedHashMap<>(); // keep params in order
        params.put("op", "getMixSetParams");
        params.put("serialNum", configuration.deviceId);
        params.put("kind", "0");

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.GET, NEW_MIX_API, params, null);

        JsonElement obj = result.get("obj");
        if (obj instanceof JsonObject object) {
            JsonElement mixBean = object.get("mixBean");
            if (mixBean instanceof JsonObject mixBeanObject) {
                return mixBeanObject.asMap();
            }
        }
        throw new ApiException("Invalid JSON response");
    }

    /**
     * Get the plant information.
     * <p>
     * This method is not currently used, but is included as a Java template for future implementations if needed.
     * See https://github.com/indykoning/PyPi_GrowattServer/blob/master/growattServer/__init__.py
     *
     * @return a Map of JSON elements containing the server response.
     * @throws ApiException if any error occurs.
     */
    public Map<String, JsonElement> getPlantInfo() throws ApiException {
        if (configuration.plantId == null) {
            throw new ApiException("Plant Id missing");
        }

        Map<String, String> params = new LinkedHashMap<>(); // keep params in order
        params.put("op", "getAllDeviceList");
        params.put("plantId", Objects.requireNonNull(configuration.plantId));
        params.put("pageNum", "1");
        params.put("pageSize", "1");

        return doHttpRequest(HttpMethod.GET, PLANT_INFO_API, params, null);
    }

    /**
     * Get the plant list.
     * <p>
     * This method is not currently used, but is included as a Java template for future implementations if needed.
     * See https://github.com/indykoning/PyPi_GrowattServer/blob/master/growattServer/__init__.py
     *
     * @return a Map of JSON elements containing the server response.
     * @throws ApiException if any error occurs.
     */
    public Map<String, JsonElement> getPlantList() throws ApiException {
        if (configuration.userId == null) {
            throw new ApiException("User Id missing");
        }

        Map<String, String> params = new LinkedHashMap<>(); // keep params in order
        params.put("userId", Objects.requireNonNull(configuration.userId));

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.GET, PLANT_LIST_API, params, null);

        JsonElement back = result.get("back");
        if (back instanceof JsonObject backObject) {
            JsonElement success = backObject.get("success");
            if (success instanceof JsonPrimitive successPrimitive) {
                if (successPrimitive.getAsBoolean()) {
                    return backObject.asMap();
                }
            }
        }
        throw new ApiException("Invalid JSON response");
    }

    /**
     * Attempt to login to the remote server by posting the given user credentials.
     *
     * @return a Map of JSON elements containing the server response.
     * @throws ApiException if any error occurs.
     */
    public Map<String, JsonElement> postLoginCredentials() throws ApiException {
        if (configuration.userName == null) {
            throw new ApiException("User name missing");
        }
        if (configuration.password == null) {
            throw new ApiException("Password missing");
        }

        Fields fields = new Fields();
        fields.put("userName", Objects.requireNonNull(configuration.userName));
        fields.put("password", createHash(Objects.requireNonNull(configuration.password)));

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.POST, LOGIN_API, null, fields);

        JsonElement back = result.get("back");
        if (back instanceof JsonObject backObject) {
            JsonElement success = backObject.get("success");
            if (success instanceof JsonPrimitive successPrimitive) {
                if (successPrimitive.getAsBoolean()) {
                    return backObject.asMap();
                }
            }
        }
        throw new ApiException("Login failed");
    }

    /**
     * Post a command to setup the mix inverter battery charging program.
     *
     * @param chargingPower the rate of charging 0%..100%
     * @param targetSOC the SOC at which to stop charging 0%..100%
     * @param allowAcCharging allow the battery to be charged from AC power
     * @param startTime the start time of the charging program
     * @param stopTime the stop time of the charging program
     * @param programEnable charge program shall be enabled
     * @return a Map of JSON elements containing the server response
     * @throws ApiException if any error occurs
     */
    public Map<String, JsonElement> setupChargingProgram(int chargingPower, int targetSOC, boolean allowAcCharging,
            LocalTime startTime, LocalTime stopTime, boolean programEnable) throws ApiException {
        if (chargingPower < 1 || chargingPower > 100) {
            throw new ApiException("Charge power out of range (1%..100%)");
        }
        if (targetSOC < 1 || targetSOC > 100) {
            throw new ApiException("Target SOC out of range (1%..100%)");
        }

        Fields fields = new Fields();
        fields.put("op", "mixSetApiNew");
        fields.put("serialNum", configuration.deviceId);
        fields.put("type", "mix_ac_charge_time_period");
        fields.put("param1", String.format("%d", chargingPower));
        fields.put("param2", String.format("%d", targetSOC));
        fields.put("param3", allowAcCharging ? "1" : "0");
        fields.put("param4", String.format("%02d", startTime.getHour()));
        fields.put("param5", String.format("%02d", startTime.getMinute()));
        fields.put("param6", String.format("%02d", stopTime.getHour()));
        fields.put("param7", String.format("%02d", stopTime.getMinute()));
        fields.put("param8", programEnable ? "1" : "0");
        fields.put("param9", "00");
        fields.put("param10", "00");
        fields.put("param11", "00");
        fields.put("param12", "00");
        fields.put("param13", "0");
        fields.put("param14", "00");
        fields.put("param15", "00");
        fields.put("param16", "00");
        fields.put("param17", "00");
        fields.put("param18", "0");

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.POST, NEW_TCP_SET_API, null, fields);

        JsonElement success = result.get("success");
        if (success instanceof JsonPrimitive sucessPrimitive) {
            if (sucessPrimitive.getAsBoolean()) {
                return result;
            }
        }
        throw new ApiException("Command failed");
    }

    /**
     * Post a command to setup the mix inverter battery discharge program.
     *
     * @param dischargingPower the rate of discharging 1%..100%
     * @param targetSOC the SOC at which to stop charging 1%..100%
     * @param startTime the start time of the discharging program
     * @param stopTime the stop time of the discharging program
     * @param programEnable discharge program shall be enabled
     * @return a Map of JSON elements containing the server response
     * @throws ApiException if any error occurs
     */
    public Map<String, JsonElement> setupDischargingProgram(int dischargingPower, int targetSOC, LocalTime startTime,
            LocalTime stopTime, boolean programEnable) throws ApiException {
        if (dischargingPower < 1 || dischargingPower > 100) {
            throw new ApiException("Discharge power out of range (1%..100%)");
        }
        if (targetSOC < 1 || targetSOC > 100) {
            throw new ApiException("Target SOC out of range (1%..100%)");
        }

        Fields fields = new Fields();
        fields.put("op", "mixSetApiNew");
        fields.put("serialNum", configuration.deviceId);
        fields.put("type", "mix_ac_discharge_time_period");
        fields.put("param1", String.format("%d", dischargingPower));
        fields.put("param2", String.format("%d", targetSOC));
        fields.put("param3", String.format("%02d", startTime.getHour()));
        fields.put("param4", String.format("%02d", startTime.getMinute()));
        fields.put("param5", String.format("%02d", stopTime.getHour()));
        fields.put("param6", String.format("%02d", stopTime.getMinute()));
        fields.put("param7", programEnable ? "1" : "0");
        fields.put("param8", "00");
        fields.put("param9", "00");
        fields.put("param10", "00");
        fields.put("param11", "00");
        fields.put("param12", "0");
        fields.put("param13", "00");
        fields.put("param14", "00");
        fields.put("param15", "00");
        fields.put("param16", "00");
        fields.put("param17", "0");

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.POST, NEW_TCP_SET_API, null, fields);

        JsonElement success = result.get("success");
        if (success instanceof JsonPrimitive sucessPrimitive) {
            if (sucessPrimitive.getAsBoolean()) {
                return result;
            }
        }
        throw new ApiException("Command failed");
    }

    /**
     * Look for an entry in the given Map, and return its value as a boolean.
     *
     * @param map the source map.
     * @param key the key to search for in the map.
     * @return the boolean value.
     * @throws ApiException if any error occurs.
     */
    public static boolean mapGetBoolean(Map<String, JsonElement> map, String key) throws ApiException {
        JsonElement element = map.get(key);
        if (element instanceof JsonPrimitive primitive) {
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            } else if (primitive.isNumber() || primitive.isString()) {
                try {
                    switch (primitive.getAsInt()) {
                        case 0:
                            return false;
                        case 1:
                            return true;
                    }
                } catch (NumberFormatException e) {
                    throw new ApiException("Boolean bad value", e);
                }
            }
        }
        throw new ApiException("Boolean missing or bad value");
    }

    /**
     * Look for an entry in the given Map, and return its value as an integer.
     *
     * @param map the source map.
     * @param key the key to search for in the map.
     * @return the integer value.
     * @throws ApiException if any error occurs.
     */
    public static int mapGetInteger(Map<String, JsonElement> map, String key) throws ApiException {
        JsonElement element = map.get(key);
        if (element instanceof JsonPrimitive primitive) {
            try {
                return primitive.getAsInt();
            } catch (NumberFormatException e) {
                throw new ApiException("Integer bad value", e);
            }
        }
        throw new ApiException("Integer missing or bad value");
    }

    /**
     * Look for an entry in the given Map, and return its value as a LocalTime.
     *
     * @param source the source map.
     * @param key the key to search for in the map.
     * @return the LocalTime.
     * @throws ApiException if any error occurs.
     */
    public static LocalTime mapGetLocalTime(Map<String, JsonElement> source, String key) throws ApiException {
        JsonElement element = source.get(key);
        if ((element instanceof JsonPrimitive primitive) && primitive.isString()) {
            try {
                return localTimeOf(primitive.getAsString());
            } catch (DateTimeException e) {
                throw new ApiException("LocalTime bad value", e);
            }
        }
        throw new ApiException("LocalTime missing or bad value");
    }

    /**
     * Parse a time formatted string into a LocalTime entity.
     * <p>
     * Note: unlike the standard LocalTime.parse() method, this method accepts hour and minute fields from the Growatt
     * server that are without leading zeros e.g. "1:1" and it accepts the conventional "01:01" format too.
     *
     * @param localTime a time formatted string e.g. "12:34"
     * @return a corresponding LocalTime entity.
     * @throws DateTimeException if any error occurs.
     */
    public static LocalTime localTimeOf(String localTime) throws DateTimeException {
        String splitParts[] = localTime.split(":");
        if (splitParts.length < 2) {
            throw new DateTimeException("LocalTime bad value");
        }
        try {
            return LocalTime.of(Integer.valueOf(splitParts[0]), Integer.valueOf(splitParts[1]));
        } catch (NumberFormatException | DateTimeException e) {
            throw new DateTimeException("LocalTime bad value", e);
        }
    }
}
