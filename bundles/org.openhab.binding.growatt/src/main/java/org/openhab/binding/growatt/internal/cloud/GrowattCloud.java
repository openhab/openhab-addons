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
package org.openhab.binding.growatt.internal.cloud;

import java.lang.reflect.Type;
import java.net.HttpCookie;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.growatt.internal.GrowattBindingConstants;
import org.openhab.binding.growatt.internal.config.GrowattBridgeConfiguration;
import org.openhab.binding.growatt.internal.dto.GrowattDevice;
import org.openhab.binding.growatt.internal.dto.GrowattPlant;
import org.openhab.binding.growatt.internal.dto.GrowattPlantList;
import org.openhab.binding.growatt.internal.dto.GrowattUser;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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
    private static final String LOGIN_API_ENDPOINT = "newTwoLoginAPI.do";
    private static final String PLANT_LIST_API_ENDPOINT = "PlantListAPI.do";
    private static final String PLANT_INFO_API_ENDPOINT = "newTwoPlantAPI.do";
    private static final String NEW_TCP_SET_API_ENDPOINT = "newTcpsetAPI.do";

    private static final String FMT_NEW_DEVICE_TYPE_API_DO = "new%sApi.do";

    // command operations
    private static final String OP_GET_ALL_DEVICE_LIST = "getAllDeviceList";

    // enum of device types
    private static enum DeviceType {
        MIX,
        MAX,
        MIN,
        SPA,
        SPH,
        TLX
    }

    /*
     * Map of device types vs. field parameters for GET requests to FMT_NEW_DEVICE_TYPE_API_DO end-points.
     * Note: some values are guesses which have not yet been confirmed by users
     */
    private static final Map<DeviceType, String> SUPPORTED_TYPES_GET_PARAM = Map.of(
    // @formatter:off
            DeviceType.MIX, "getMixSetParams",
            DeviceType.MAX, "getMaxSetData",
            DeviceType.MIN, "getMinSetData",
            DeviceType.SPA, "getSpaSetData",
            DeviceType.SPH, "getSphSetData",
            DeviceType.TLX, "getTlxSetData"
    // @formatter:on
    );

    /*
     * Map of device types vs. field parameters for POST commands to NEW_TCP_SET_API_ENDPOINT.
     * Note: some values are guesses which have not yet been confirmed by users
     */
    private static final Map<DeviceType, String> SUPPORTED_TYPE_POST_PARAM = Map.of(
    // @formatter:off
            DeviceType.MIX, "mixSetApiNew", // was "mixSetApi"
            DeviceType.MAX, "maxSetApi",
            DeviceType.MIN, "minSetApi",
            DeviceType.SPA, "spaSetApi",
            DeviceType.SPH, "sphSet",
            DeviceType.TLX, "tlxSet"
    // @formatter:on
    );

    // enum to select charge resp. discharge program
    private static enum ProgramType {
        CHARGE,
        DISCHARGE
    }

    // enum of program modes
    public static enum ProgramMode {
        LOAD_FIRST,
        BATTERY_FIRST,
        GRID_FIRST
    }

    // @formatter:off
    private static final Type DEVICE_LIST_TYPE = new TypeToken<List<GrowattDevice>>() {}.getType();
    // @formatter:on

    // HTTP headers (user agent is spoofed to mimic the Growatt Android Shine app)
    private static final String USER_AGENT = "Dalvik/2.1.0 (Linux; U; Android 12; https://www.openhab.org)";
    private static final String FORM_CONTENT = "application/x-www-form-urlencoded";

    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(GrowattCloud.class);
    private final HttpClient httpClient;
    private final GrowattBridgeConfiguration configuration;
    private final Gson gson = new Gson();
    private final List<String> plantIds = new ArrayList<>();
    private final Map<String, DeviceType> deviceIdTypeMap = new ConcurrentHashMap<>();

    private String userId = "";

    /**
     * Constructor.
     *
     * @param configuration the bridge configuration parameters.
     * @param httpClientFactory the OH core {@link HttpClientFactory} instance.
     * @throws Exception if anything goes wrong.
     */
    public GrowattCloud(GrowattBridgeConfiguration configuration, HttpClientFactory httpClientFactory)
            throws Exception {
        this.configuration = configuration;
        this.httpClient = httpClientFactory.createHttpClient(GrowattBindingConstants.BINDING_ID);
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
     * @throws GrowattApiException if MD5 algorithm is not supported
     */
    private static String createHash(String password) throws GrowattApiException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new GrowattApiException("Hash algorithm error", e);
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
     * Refresh the login cookies.
     *
     * @throws GrowattApiException if any error occurs.
     */
    private void refreshCookies() throws GrowattApiException {
        List<HttpCookie> cookies = httpClient.getCookieStore().getCookies();
        if (cookies.isEmpty() || cookies.stream().anyMatch(HttpCookie::hasExpired)) {
            postLoginCredentials();
        }
    }

    /**
     * Login to the server (if necessary) and then execute an HTTP request using the given HTTP method, to the given end
     * point, and with the given request URL parameters and/or request form fields. If the cookies are not valid first
     * login to the server before making the actual HTTP request.
     *
     * @param method the HTTP method to use.
     * @param endPoint the API end point.
     * @param params the request URL parameters (may be null).
     * @param fields the request form fields (may be null).
     * @return a Map of JSON elements containing the server response.
     * @throws GrowattApiException if any error occurs.
     */
    private Map<String, JsonElement> doHttpRequest(HttpMethod method, String endPoint,
            @Nullable Map<String, String> params, @Nullable Fields fields) throws GrowattApiException {
        refreshCookies();
        return doHttpRequestInner(method, endPoint, params, fields);
    }

    /**
     * Inner method to execute an HTTP request using the given HTTP method, to the given end point, and with the given
     * request URL parameters and/or request form fields.
     *
     * @param method the HTTP method to use.
     * @param endPoint the API end point.
     * @param params the request URL parameters (may be null).
     * @param fields the request form fields (may be null).
     * @return a Map of JSON elements containing the server response.
     * @throws GrowattApiException if any error occurs.
     */
    private Map<String, JsonElement> doHttpRequestInner(HttpMethod method, String endPoint,
            @Nullable Map<String, String> params, @Nullable Fields fields) throws GrowattApiException {
        //
        Request request = httpClient.newRequest(SERVER_URL + endPoint).method(method).agent(USER_AGENT)
                .timeout(HTTP_TIMEOUT.getSeconds(), TimeUnit.SECONDS);

        if (params != null) {
            params.entrySet().forEach(p -> request.param(p.getKey(), p.getValue()));
        }

        if (fields != null) {
            request.content(new FormContentProvider(fields), FORM_CONTENT);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("{} {}{} {} {}", method, request.getPath(), params == null ? "" : "?" + request.getQuery(),
                    request.getVersion(), fields == null ? "" : "? " + FormContentProvider.convert(fields));
        }

        ContentResponse response;
        try {
            response = request.send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new GrowattApiException("HTTP I/O Exception", e);
        }

        int status = response.getStatus();
        String content = response.getContentAsString();

        logger.trace("HTTP {} {} {}", status, HttpStatus.getMessage(status), content);

        if (status != HttpStatus.OK_200) {
            throw new GrowattApiException(String.format("HTTP %d %s", status, HttpStatus.getMessage(status)));
        }

        if (content == null || content.isBlank()) {
            throw new GrowattApiException("Response is " + (content == null ? "null" : "blank"));
        }

        if (content.contains("<html>")) {
            logger.warn("HTTP {} {} {}", status, HttpStatus.getMessage(status), content);
            throw new GrowattApiException("Response is HTML");
        }

        try {
            JsonElement jsonObject = JsonParser.parseString(content).getAsJsonObject();
            if (jsonObject instanceof JsonObject jsonElement) {
                return jsonElement.asMap();
            }
            throw new GrowattApiException("Response JSON invalid");
        } catch (JsonSyntaxException | IllegalStateException e) {
            throw new GrowattApiException("Response JSON syntax exception", e);
        }
    }

    /**
     * Get the deviceType for the given deviceId. If the deviceIdTypeMap is empty then download it freshly.
     *
     * @param the deviceId to get.
     * @return the deviceType.
     * @throws GrowattApiException if any error occurs.
     */
    private DeviceType getDeviceTypeChecked(String deviceId) throws GrowattApiException {
        if (deviceIdTypeMap.isEmpty()) {
            if (plantIds.isEmpty()) {
                refreshCookies();
            }
            for (String plantId : plantIds) {
                for (GrowattDevice device : getPlantInfo(plantId)) {
                    try {
                        deviceIdTypeMap.put(device.getId(), DeviceType.valueOf(device.getType().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // just ignore unsupported device types
                    }
                }
            }
            logger.debug("Downloaded deviceTypes:{}", deviceIdTypeMap);
        }
        if (deviceId.isBlank()) {
            throw new GrowattApiException("Device id is blank");
        }
        DeviceType deviceType = deviceIdTypeMap.get(deviceId);
        if (deviceType != null) {
            return deviceType;
        }
        throw new GrowattApiException("Unsupported device:" + deviceId);
    }

    /**
     * Get the inverter device settings.
     *
     * @param the deviceId to get.
     * @return a Map of JSON elements containing the server response.
     * @throws GrowattApiException if any error occurs.
     */
    public Map<String, JsonElement> getDeviceSettings(String deviceId) throws GrowattApiException {
        DeviceType deviceType = getDeviceTypeChecked(deviceId);
        String dt = deviceType.name().toLowerCase();

        String endPoint = String.format(FMT_NEW_DEVICE_TYPE_API_DO, dt.substring(0, 1).toUpperCase() + dt.substring(1));

        Map<String, String> params = new LinkedHashMap<>(); // keep params in order
        params.put("op", Objects.requireNonNull(SUPPORTED_TYPES_GET_PARAM.get(deviceType)));
        params.put("serialNum", deviceId);
        params.put("kind", "0");

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.GET, endPoint, params, null);

        JsonElement obj = result.get("obj");
        if (obj instanceof JsonObject object) {
            Map<String, JsonElement> map = object.asMap();
            Optional<String> key = map.keySet().stream().filter(k -> k.toLowerCase().endsWith("bean")).findFirst();
            if (key.isPresent()) {
                JsonElement beanJson = map.get(key.get());
                if (beanJson instanceof JsonObject bean) {
                    return bean.asMap();
                }
            }
        }
        throw new GrowattApiException("Invalid JSON response");
    }

    /**
     * Get the plant information.
     *
     * @param the plantId to get.
     * @return a list of {@link GrowattDevice} containing the server response.
     * @throws GrowattApiException if any error occurs.
     */
    public List<GrowattDevice> getPlantInfo(String plantId) throws GrowattApiException {
        Map<String, String> params = new LinkedHashMap<>(); // keep params in order
        params.put("op", OP_GET_ALL_DEVICE_LIST);
        params.put("plantId", plantId);
        params.put("pageNum", "1");
        params.put("pageSize", "1");

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.GET, PLANT_INFO_API_ENDPOINT, params, null);

        JsonElement deviceList = result.get("deviceList");
        if (deviceList instanceof JsonArray deviceArray) {
            try {
                List<GrowattDevice> devices = gson.fromJson(deviceArray, DEVICE_LIST_TYPE);
                if (devices != null) {
                    return devices;
                }
            } catch (JsonSyntaxException e) {
                // fall through
            }
        }
        throw new GrowattApiException("Invalid JSON response");
    }

    /**
     * Get the plant list.
     *
     * @param the userId to get from.
     * @return a {@link GrowattPlantList} containing the server response.
     * @throws GrowattApiException if any error occurs.
     */
    public GrowattPlantList getPlantList(String userId) throws GrowattApiException {
        Map<String, String> params = new LinkedHashMap<>(); // keep params in order
        params.put("userId", userId);

        Map<String, JsonElement> result = doHttpRequest(HttpMethod.GET, PLANT_LIST_API_ENDPOINT, params, null);

        JsonElement back = result.get("back");
        if (back instanceof JsonObject backObject) {
            try {
                GrowattPlantList plantList = gson.fromJson(backObject, GrowattPlantList.class);
                if (plantList != null && plantList.getSuccess()) {
                    return plantList;
                }
            } catch (JsonSyntaxException e) {
                // fall through
            }
        }
        throw new GrowattApiException("Invalid JSON response");
    }

    /**
     * Attempt to login to the remote server by posting the given user credentials.
     *
     * @throws GrowattApiException if any error occurs.
     */
    private void postLoginCredentials() throws GrowattApiException {
        String userName = configuration.userName;
        if (userName == null || userName.isBlank()) {
            throw new GrowattApiException("User name missing");
        }
        String password = configuration.password;
        if (password == null || password.isBlank()) {
            throw new GrowattApiException("Password missing");
        }

        Fields fields = new Fields();
        fields.put("userName", userName);
        fields.put("password", createHash(password));

        Map<String, JsonElement> result = doHttpRequestInner(HttpMethod.POST, LOGIN_API_ENDPOINT, null, fields);

        JsonElement back = result.get("back");
        if (back instanceof JsonObject backObject) {
            try {
                GrowattPlantList plantList = gson.fromJson(backObject, GrowattPlantList.class);
                if (plantList != null && plantList.getSuccess()) {
                    GrowattUser user = plantList.getUserId();
                    userId = user != null ? user.getId() : userId;
                    plantIds.clear();
                    plantIds.addAll(plantList.getPlants().stream().map(GrowattPlant::getId).toList());
                    logger.debug("Logged in userId:{}, plantIds:{}", userId, plantIds);
                    return;
                }
            } catch (JsonSyntaxException e) {
                // fall through
            }
        }
        throw new GrowattApiException("Login failed");
    }

    /**
     * Post a command to setup the inverter battery charging program.
     *
     * @param the deviceId to set up
     * @param programModeInt index of the type of program Load First (0) / Battery First (1) / Grid First (2)
     * @param powerLevel the rate of charging / discharging
     * @param stopSOC the SOC at which to stop charging / discharging
     * @param enableAcCharging allow charging from AC power
     * @param startTime the start time of the charging / discharging program
     * @param stopTime the stop time of the charging / discharging program
     * @param enableProgram charging / discharging program shall be enabled
     *
     * @throws GrowattApiException if any error occurs
     */
    public void setupBatteryProgram(String deviceId, int programModeInt, @Nullable Integer powerLevel,
            @Nullable Integer stopSOC, @Nullable Boolean enableAcCharging, @Nullable String startTime,
            @Nullable String stopTime, @Nullable Boolean enableProgram) throws GrowattApiException {
        //
        if (deviceId.isBlank()) {
            throw new GrowattApiException("Device id is blank");
        }

        ProgramMode programMode;
        try {
            programMode = ProgramMode.values()[programModeInt];
        } catch (IndexOutOfBoundsException e) {
            throw new GrowattApiException("Program mode is out of range (0..2)");
        }

        DeviceType deviceType = getDeviceTypeChecked(deviceId);
        switch (deviceType) {

            case MIX:
            case SPA:
                setTimeProgram(deviceId, deviceType,
                        programMode == ProgramMode.BATTERY_FIRST ? ProgramType.CHARGE : ProgramType.DISCHARGE,
                        powerLevel, stopSOC, enableAcCharging, startTime, stopTime, enableProgram);
                return;

            case TLX:
                if (enableAcCharging != null) {
                    setEnableAcCharging(deviceId, deviceType, enableAcCharging);
                }
                if (powerLevel != null) {
                    setPowerLevel(deviceId, deviceType, programMode, powerLevel);
                }
                if (stopSOC != null) {
                    setStopSOC(deviceId, deviceType, programMode, stopSOC);
                }
                if (startTime != null || stopTime != null || enableProgram != null) {
                    setTimeSegment(deviceId, deviceType, programMode, startTime, stopTime, enableProgram);
                }
                return;

            default:
        }
        throw new GrowattApiException("Unsupported device type:" + deviceType.name());
    }

    /**
     * Look for an entry in the given Map, and return its value as a boolean.
     *
     * @param map the source map.
     * @param key the key to search for in the map.
     * @return the boolean value.
     * @throws GrowattApiException if any error occurs.
     */
    public static boolean mapGetBoolean(Map<String, JsonElement> map, String key) throws GrowattApiException {
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
                    throw new GrowattApiException("Boolean bad value", e);
                }
            }
        }
        throw new GrowattApiException("Boolean missing or bad value");
    }

    /**
     * Look for an entry in the given Map, and return its value as an integer.
     *
     * @param map the source map.
     * @param key the key to search for in the map.
     * @return the integer value.
     * @throws GrowattApiException if any error occurs.
     */
    public static int mapGetInteger(Map<String, JsonElement> map, String key) throws GrowattApiException {
        JsonElement element = map.get(key);
        if (element instanceof JsonPrimitive primitive) {
            try {
                return primitive.getAsInt();
            } catch (NumberFormatException e) {
                throw new GrowattApiException("Integer bad value", e);
            }
        }
        throw new GrowattApiException("Integer missing or bad value");
    }

    /**
     * Look for an entry in the given Map, and return its value as a LocalTime.
     *
     * @param source the source map.
     * @param key the key to search for in the map.
     * @return the LocalTime.
     * @throws GrowattApiException if any error occurs.
     */
    public static LocalTime mapGetLocalTime(Map<String, JsonElement> source, String key) throws GrowattApiException {
        JsonElement element = source.get(key);
        if ((element instanceof JsonPrimitive primitive) && primitive.isString()) {
            try {
                return localTimeOf(primitive.getAsString());
            } catch (DateTimeException e) {
                throw new GrowattApiException("LocalTime bad value", e);
            }
        }
        throw new GrowattApiException("LocalTime missing or bad value");
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

    /**
     * Post a command to set up the inverter battery charging / discharging program.
     *
     * @param the deviceId to set up
     * @param the deviceType to set up
     * @param programType selects whether the program is for charge or discharge
     * @param powerLevel the rate of charging / discharging 1%..100%
     * @param stopSOC the SOC at which to stop the program 5%..100%
     * @param enableAcCharging allow charging from AC power (only applies to hybrid/mix inverters)
     * @param startTime the start time of the program
     * @param stopTime the stop time of the program
     * @param enableProgram the program shall be enabled
     *
     * @throws GrowattApiException if any error occurs
     */
    private void setTimeProgram(String deviceId, DeviceType deviceType, ProgramType programType,
            @Nullable Integer powerLevel, @Nullable Integer stopSOC, @Nullable Boolean enableAcCharging,
            @Nullable String startTime, @Nullable String stopTime, @Nullable Boolean enableProgram)
            throws GrowattApiException {
        //
        if (powerLevel == null || powerLevel < 1 || powerLevel > 100) {
            throw new GrowattApiException("Power level parameter is null or out of range (1%..100%)");
        }
        if (stopSOC == null || stopSOC < 5 || stopSOC > 100) {
            throw new GrowattApiException("Target SOC parameter is null out of range (5%..100%)");
        }
        if (startTime == null) {
            throw new GrowattApiException("Start time parameter is null");
        }
        if (stopTime == null) {
            throw new GrowattApiException("Stop time parameter is null");
        }
        if (enableProgram == null) {
            throw new GrowattApiException("Program enable parameter is null");
        }
        boolean isMixChargeCommand = deviceType == DeviceType.MIX && programType == ProgramType.CHARGE;
        if (isMixChargeCommand && enableAcCharging == null) {
            throw new GrowattApiException("Allow ac charging parameter is null");
        }
        LocalTime localStartTime;
        try {
            localStartTime = GrowattCloud.localTimeOf(startTime);
        } catch (DateTimeException e) {
            throw new GrowattApiException("Start time is invalid");
        }
        LocalTime localStopTime;
        try {
            localStopTime = GrowattCloud.localTimeOf(stopTime);
        } catch (DateTimeException e) {
            throw new GrowattApiException("Stop time is invalid");
        }

        Fields fields = new Fields();

        fields.put("op", Objects.requireNonNull(SUPPORTED_TYPE_POST_PARAM.get(deviceType)));
        fields.put("serialNum", deviceId);
        fields.put("type", String.format("%s_ac_%s_time_period", deviceType.name().toLowerCase(),
                programType.name().toLowerCase()));

        int paramId = 1;

        paramId = addParam(fields, paramId, String.format("%d", powerLevel));
        paramId = addParam(fields, paramId, String.format("%d", stopSOC));
        if (isMixChargeCommand) {
            paramId = addParam(fields, paramId, enableAcCharging ? "1" : "0");
        }
        paramId = addParam(fields, paramId, String.format("%02d", localStartTime.getHour()));
        paramId = addParam(fields, paramId, String.format("%02d", localStartTime.getMinute()));
        paramId = addParam(fields, paramId, String.format("%02d", localStopTime.getHour()));
        paramId = addParam(fields, paramId, String.format("%02d", localStopTime.getMinute()));
        paramId = addParam(fields, paramId, enableProgram ? "1" : "0");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "0");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "00");
        paramId = addParam(fields, paramId, "0");

        postSetCommandForm(fields);
    }

    /**
     * Add a new entry in the given {@link Fields} map in the form "paramN" = paramValue where N is the parameter index.
     *
     * @param fields the map to be added to.
     * @param parameterIndex the parameter index.
     * @param parameterValue the parameter value.
     *
     * @return the next parameter index.
     */
    private int addParam(Fields fields, int parameterIndex, String parameterValue) {
        fields.put(String.format("param%d", parameterIndex), parameterValue);
        return parameterIndex + 1;
    }

    /**
     * Inner method to execute a POST setup command using the given form fields.
     *
     * @param fields the form fields to be posted.
     *
     * @throws GrowattApiException if any error occurs
     */
    private void postSetCommandForm(Fields fields) throws GrowattApiException {
        Map<String, JsonElement> result = doHttpRequest(HttpMethod.POST, NEW_TCP_SET_API_ENDPOINT, null, fields);
        JsonElement success = result.get("success");
        if (success instanceof JsonPrimitive sucessPrimitive) {
            if (sucessPrimitive.getAsBoolean()) {
                return;
            }
        }
        throw new GrowattApiException("Command failed");
    }

    /**
     * Post a command to enable / disable ac charging.
     *
     * @param the deviceId to set up
     * @param the deviceType to set up
     * @param enableAcCharging enable or disable the function
     *
     * @throws GrowattApiException if any error occurs
     */
    private void setEnableAcCharging(String deviceId, DeviceType deviceType, boolean enableAcCharging)
            throws GrowattApiException {
        //
        Fields fields = new Fields();

        fields.put("op", Objects.requireNonNull(SUPPORTED_TYPE_POST_PARAM.get(deviceType)));
        fields.put("serialNum", deviceId);
        fields.put("type", "ac_charge");
        fields.put("param1", enableAcCharging ? "1" : "0");

        postSetCommandForm(fields);
    }

    /**
     * Post a command to set up a program charge / discharge power level.
     *
     * @param the deviceId to set up
     * @param the deviceType to set up
     * @param programMode the program mode that the setting shall apply to
     * @param powerLevel the rate of charging / discharging 1%..100%
     *
     * @throws GrowattApiException if any error occurs
     */
    private void setPowerLevel(String deviceId, DeviceType deviceType, ProgramMode programMode, int powerLevel)
            throws GrowattApiException {
        //
        if (powerLevel < 1 || powerLevel > 100) {
            throw new GrowattApiException("Power level out of range (1%..100%)");
        }

        String typeParam;
        switch (programMode) {
            case BATTERY_FIRST:
                typeParam = "charge_power";
                break;
            case GRID_FIRST:
            case LOAD_FIRST:
                typeParam = "discharge_power";
                break;
            default:
                throw new GrowattApiException("Unexpected exception");
        }

        Fields fields = new Fields();

        fields.put("op", Objects.requireNonNull(SUPPORTED_TYPE_POST_PARAM.get(deviceType)));
        fields.put("serialNum", deviceId);
        fields.put("type", typeParam);
        fields.put("param1", String.format("%d", powerLevel));

        postSetCommandForm(fields);
    }

    /**
     * Post a command to set up a program target (stop) SOC level.
     *
     * @param the deviceId to set up
     * @param the deviceType to set up
     * @param programMode the program mode that the setting shall apply to
     * @param stopSOC the SOC at which to stop the program 11%..100%
     *
     * @throws GrowattApiException if any error occurs
     */
    private void setStopSOC(String deviceId, DeviceType deviceType, ProgramMode programMode, int stopSOC)
            throws GrowattApiException {
        //
        if (stopSOC < 11 || stopSOC > 100) {
            throw new GrowattApiException("Target SOC out of range (11%..100%)");
        }

        String typeParam;
        switch (programMode) {
            case BATTERY_FIRST:
                typeParam = "charge_stop_soc";
                break;
            case GRID_FIRST:
                typeParam = "on_grid_discharge_stop_soc";
                break;
            case LOAD_FIRST:
                typeParam = "discharge_stop_soc";
                break;
            default:
                throw new GrowattApiException("Unexpected exception");
        }

        Fields fields = new Fields();

        fields.put("op", Objects.requireNonNull(SUPPORTED_TYPE_POST_PARAM.get(deviceType)));
        fields.put("serialNum", deviceId);
        fields.put("type", typeParam);
        fields.put("param1", String.format("%d", stopSOC));

        postSetCommandForm(fields);
    }

    /**
     * Post a command to set up a time segment program.
     * Note: uses separate dedicated time segments for Load First, Battery First, Grid First modes.
     *
     * @param the deviceId to set up
     * @param the deviceType to set up
     * @param programMode the program mode for the time segment
     * @param startTime the start time of the program
     * @param stopTime the stop time of the program
     * @param enableProgram the program shall be enabled
     *
     * @throws GrowattApiException if any error occurs
     */
    private void setTimeSegment(String deviceId, DeviceType deviceType, ProgramMode programMode,
            @Nullable String startTime, @Nullable String stopTime, @Nullable Boolean enableProgram)
            throws GrowattApiException {
        //
        if (startTime == null) {
            throw new GrowattApiException("Start time parameter is null");
        }
        if (stopTime == null) {
            throw new GrowattApiException("Stop time parameter is null");
        }
        if (enableProgram == null) {
            throw new GrowattApiException("Program enable parameter is null");
        }
        LocalTime localStartTime;
        try {
            localStartTime = GrowattCloud.localTimeOf(startTime);
        } catch (DateTimeException e) {
            throw new GrowattApiException("Start time is invalid");
        }
        LocalTime localStopTime;
        try {
            localStopTime = GrowattCloud.localTimeOf(stopTime);
        } catch (DateTimeException e) {
            throw new GrowattApiException("Stop time is invalid");
        }

        Fields fields = new Fields();

        fields.put("op", Objects.requireNonNull(SUPPORTED_TYPE_POST_PARAM.get(deviceType)));
        fields.put("serialNum", deviceId);
        fields.put("type", String.format("time_segment%d", programMode.ordinal() + 1));
        fields.put("param1", String.format("%d", programMode.ordinal()));
        fields.put("param2", String.format("%02d", localStartTime.getHour()));
        fields.put("param3", String.format("%02d", localStartTime.getMinute()));
        fields.put("param4", String.format("%02d", localStopTime.getHour()));
        fields.put("param5", String.format("%02d", localStopTime.getMinute()));
        fields.put("param6", enableProgram ? "1" : "0");

        postSetCommandForm(fields);
    }
}
