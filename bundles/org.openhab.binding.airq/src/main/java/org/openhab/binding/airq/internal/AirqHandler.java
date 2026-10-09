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
package org.openhab.binding.airq.internal;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.measure.MetricPrefix;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AirqHandler} is responsible for retrieving all information from the air-Q device
 * and change properties and channels accordingly.
 *
 * @author Aurelio Caliaro - Initial contribution
 * @author Fabian Wolter - Improve error handling
 * @author Michael Weger - Additional measurement channels and safe diagnostics
 */
@NonNullByDefault
public class AirqHandler extends BaseThingHandler {
    private record ChannelMapping(String dataKey, String channelId, String type, @Nullable Unit<?> unit,
            String groupId) {
        ChannelMapping(String dataKey, String channelId, String type) {
            this(dataKey, channelId, type, null, "measurements");
        }

        ChannelMapping(String dataKey, String channelId, Unit<?> unit) {
            this(dataKey, channelId, "measurement", unit, "advanced-measurements");
        }
    }

    private static final List<ChannelMapping> MEASUREMENTS = List.of(
            new ChannelMapping("cnt0_3", "fineDustCnt00_3", "pair"),
            new ChannelMapping("cnt0_5", "fineDustCnt00_5", "pair"),
            new ChannelMapping("cnt1", "fineDustCnt01", "pair"),
            new ChannelMapping("cnt2_5", "fineDustCnt02_5", "pair"),
            new ChannelMapping("cnt5", "fineDustCnt05", "pair"), new ChannelMapping("cnt10", "fineDustCnt10", "pair"),
            new ChannelMapping("co", "co", "pair"), new ChannelMapping("co2", "co2", "pairPPM"),
            new ChannelMapping("dewpt", "dewpt", "pair"), new ChannelMapping("h2s", "h2s", "pair"),
            new ChannelMapping("humidity", "humidityRelative", "pair"),
            new ChannelMapping("humidity_abs", "humidityAbsolute", "pair"), new ChannelMapping("no2", "no2", "pair"),
            new ChannelMapping("o3", "o3", "pair"), new ChannelMapping("oxygen", "o2", "pair"),
            new ChannelMapping("pm1", "fineDustConc01", "pair"),
            new ChannelMapping("pm2_5", "fineDustConc02_5", "pair"),
            new ChannelMapping("pm10", "fineDustConc10", "pair"), new ChannelMapping("pressure", "pressure", "pair"),
            new ChannelMapping("radon", "radon", "pair"), new ChannelMapping("so2", "so2", "pair"),
            new ChannelMapping("sound", "sound", "pairDB"), new ChannelMapping("temperature", "temperature", "pair"),
            new ChannelMapping("Status", "status", "string", null, "general"),
            new ChannelMapping("TypPS", "avgFineDustSize", "number"), new ChannelMapping("dCO2dt", "dCO2dt", "number"),
            new ChannelMapping("dHdt", "dHdt", "number"), new ChannelMapping("door_event", "doorEvent", "number"),
            new ChannelMapping("health", "healthIndex", "index"), new ChannelMapping("health", "health", "number"),
            new ChannelMapping("measuretime", "measureTime", "number"),
            new ChannelMapping("performance", "performanceIndex", "index"),
            new ChannelMapping("performance", "performance", "number"),
            new ChannelMapping("timestamp", "timestamp", "datetime"),
            new ChannelMapping("uptime", "uptime", "numberTimePeriod"), new ChannelMapping("tvoc", "tvoc", "pairPPB"),
            new ChannelMapping("virus", "virus_free", "pair"), new ChannelMapping("mold", "mold_free", "pair"),
            new ChannelMapping("c2h4o", "c2h4o", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("nh3_MR100", "nh3-mr100", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("ash3", "ash3", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("br2", "br2", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("ch4s", "ch4s", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("cl2_M20", "cl2-m20", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("clo2", "clo2", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("cs2", "cs2", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("ethanol", "ethanol", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("c2h4", "c2h4", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("ch2o_M10", "ch2o-m10", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("f2", "f2", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("hcl", "hcl", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("hcn", "hcn", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("hf", "hf", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("h2_M1000", "h2-m1000", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("h2o2", "h2o2", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("n2o", "n2o", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("no_M250", "no-m250", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("acid_M100", "acid-m100", Units.PARTS_PER_BILLION),
            new ChannelMapping("ph3", "ph3", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("sih4", "sih4", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("tvoc_ionsc", "tvoc-ionsc", Units.PARTS_PER_BILLION),
            new ChannelMapping("pressure_rel", "pressure-rel", MetricPrefix.HECTO(SIUnits.PASCAL)),
            new ChannelMapping("sound_max", "sound-max", Units.DECIBEL),
            new ChannelMapping("ch4_MIPEX", "ch4-mipex", Units.PERCENT),
            new ChannelMapping("c3h8_MIPEX", "c3h8-mipex", Units.PERCENT),
            new ChannelMapping("r32", "r32", Units.PERCENT), new ChannelMapping("r454b", "r454b", Units.PERCENT),
            new ChannelMapping("r454c", "r454c", Units.PERCENT));
    private static final List<ChannelMapping> CONFIGURATION = List.of(new ChannelMapping("Wifi", "wifi", "boolean"),
            new ChannelMapping("WLANssid", "ssid", "arr"), new ChannelMapping("pass", "password", "string"),
            new ChannelMapping("WifiInfo", "wifiInfo", "boolean"),
            new ChannelMapping("TimeServer", "timeServer", "string"), new ChannelMapping("geopos", "location", "coord"),
            new ChannelMapping("NightMode", "", "nightmode"), new ChannelMapping("devicename", "deviceName", "string"),
            new ChannelMapping("RoomType", "roomType", "string"), new ChannelMapping("logging", "logLevel", "string"),
            new ChannelMapping("DeleteKey", "deleteKey", "string"),
            new ChannelMapping("FireAlarm", "fireAlarm", "boolean"),
            new ChannelMapping("air-Q-Hardware-Version", "hardwareVersion", "property"),
            new ChannelMapping("WLAN config", "", "wlan"), new ChannelMapping("cloudUpload", "cloudUpload", "boolean"),
            new ChannelMapping("SecondsMeasurementDelay", "averagingRhythm", "number"),
            new ChannelMapping("Rejection", "powerFreqSuppression", "string"),
            new ChannelMapping("air-Q-Software-Version", "softwareVersion", "property"),
            new ChannelMapping("sensors", "sensorList", "proparr"),
            new ChannelMapping("AutoDriftCompensation", "autoDriftCompensation", "boolean"),
            new ChannelMapping("AutoUpdate", "autoUpdate", "boolean"),
            new ChannelMapping("AdvancedDataProcessing", "advancedDataProcessing", "boolean"),
            new ChannelMapping("Industry", "Industry", "property"),
            new ChannelMapping("ppm&ppb", "ppm_and_ppb", "boolean"),
            new ChannelMapping("GasAlarm", "gasAlarm", "boolean"), new ChannelMapping("id", "id", "property"),
            new ChannelMapping("SoundInfo", "soundPressure", "boolean"),
            new ChannelMapping("AlarmForwarding", "alarmForwarding", "boolean"),
            new ChannelMapping("usercalib", "userCalib", "calib"),
            new ChannelMapping("InitialCalFinished", "initialCalFinished", "boolean"),
            new ChannelMapping("Averaging", "averaging", "boolean"),
            new ChannelMapping("SensorInfo", "sensorInfo", "property"),
            new ChannelMapping("ErrorBars", "errorBars", "boolean"),
            new ChannelMapping("warmup-phase", "warmupPhase", "boolean"));
    private static final Set<String> MEASUREMENT_KEYS = MEASUREMENTS.stream()
            .filter(mapping -> !"string".equals(mapping.type())).map(ChannelMapping::dataKey)
            .collect(Collectors.toUnmodifiableSet());
    private final Logger logger = LoggerFactory.getLogger(AirqHandler.class);
    private final Gson gson = new Gson();
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> getConfigDataJob;
    protected static final int POLLING_PERIOD_DATA_MSEC = 15000; // in milliseconds
    protected static final int POLLING_PERIOD_CONFIG = 1; // in minutes
    protected final HttpClient httpClient;
    AirqConfiguration config = new AirqConfiguration();

    final class ResultPair {
        private final float value;
        private final float maxdev;

        public float getValue() {
            return value;
        }

        public float getMaxdev() {
            return maxdev;
        }

        /**
         * Expects a string consisting of two values as sent by the air-Q device
         * and returns a corresponding object
         *
         * @param input element as JSON array
         * @return ResultPair object with the two values
         * @throws AirqException when parsing fails
         */
        public ResultPair(JsonElement input) throws JsonSyntaxException {
            if (input instanceof JsonArray pair && pair.size() == 2) {
                value = pair.get(0).getAsFloat();
                maxdev = pair.get(1).getAsFloat();
            } else {
                throw new JsonSyntaxException("Failed to parse pair: " + input);
            }
        }
    }

    public AirqHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    private boolean isTimeFormat(String str) {
        try {
            LocalTime.parse(str);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof OnOffType) || (command instanceof StringType)) {
            JsonObject newobj = new JsonObject();
            JsonObject subjson = new JsonObject();
            switch (channelUID.getIdWithoutGroup()) {
                case "wifi":
                    // we do not allow to switch off Wifi because otherwise we can't connect to the air-Q device anymore
                    break;
                case "wifiInfo":
                    newobj.addProperty("WifiInfo", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "fireAlarm":
                    newobj.addProperty("FireAlarm", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "cloudUpload":
                    newobj.addProperty("cloudUpload", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "autoDriftCompensation":
                    newobj.addProperty("AutoDriftCompensation", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "autoUpdate":
                    // note that this property is binary but uses 1 and 0 instead of true and false
                    newobj.addProperty("AutoUpdate", command == OnOffType.ON ? 1 : 0);
                    changeSettings(newobj);
                    break;
                case "advancedDataProcessing":
                    newobj.addProperty("AdvancedDataProcessing", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "gasAlarm":
                    newobj.addProperty("GasAlarm", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "soundPressure":
                    newobj.addProperty("SoundInfo", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "alarmForwarding":
                    newobj.addProperty("AlarmForwarding", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "averaging":
                    newobj.addProperty("averaging", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "errorBars":
                    newobj.addProperty("ErrorBars", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "ppm_and_ppb":
                    newobj.addProperty("ppm&ppb", command == OnOffType.ON);
                    changeSettings(newobj);
                case "nightmodeFanNightOff":
                    subjson.addProperty("FanNightOff", command == OnOffType.ON);
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "nightmodeWifiNightOff":
                    subjson.addProperty("WifiNightOff", command == OnOffType.ON);
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "SSID":
                    JsonElement wifidatael = gson.fromJson(command.toString(), JsonElement.class);
                    if (wifidatael != null) {
                        JsonObject wifidataobj = wifidatael.getAsJsonObject();
                        newobj.addProperty("WiFissid", wifidataobj.get("WiFissid").getAsString());
                        newobj.addProperty("WiFipass", wifidataobj.get("WiFipass").getAsString());
                        String bssid = wifidataobj.get("WiFibssid").getAsString();
                        if (!bssid.isEmpty()) {
                            newobj.addProperty("WiFibssid", bssid);
                        }
                        newobj.addProperty("reset", wifidataobj.get("reset").getAsString());
                        changeSettings(newobj);
                    } else {
                        logger.warn("Cannot extract wlan data from this string: {}", wifidatael);
                    }
                    break;
                case "timeServer":
                    newobj.addProperty(channelUID.getIdWithoutGroup(), command.toString());
                    changeSettings(newobj);
                    break;
                case "nightmodeStartDay":
                    if (isTimeFormat(command.toString())) {
                        subjson.addProperty("StartDay", command.toString());
                        newobj.add("NightMode", subjson);
                        changeSettings(newobj);
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct time format (eg. 08:00)",
                                channelUID.getIdWithoutGroup(), command.toString());
                    }
                    break;
                case "nightmodeStartNight":
                    if (isTimeFormat(command.toString())) {
                        subjson.addProperty("StartNight", command.toString());
                        newobj.add("NightMode", subjson);
                        changeSettings(newobj);
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct time format (eg. 08:00)",
                                channelUID.getIdWithoutGroup(), command.toString());
                    }
                    break;
                case "location":
                    PointType pt = (PointType) command;
                    subjson.addProperty("lat", pt.getLatitude());
                    subjson.addProperty("long", pt.getLongitude());
                    newobj.add("geopos", subjson);
                    changeSettings(newobj);
                    break;
                case "nightmodeBrightnessDay":
                    try {
                        subjson.addProperty("BrightnessDay", Float.parseFloat(command.toString()));
                        newobj.add("NightMode", subjson);
                        changeSettings(newobj);
                    } catch (NumberFormatException exc) {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} only accepts a float value, and {} is not.",
                                channelUID.getIdWithoutGroup(), command.toString());
                    }
                    break;
                case "nightmodeBrightnessNight":
                    try {
                        subjson.addProperty("BrightnessNight", Float.parseFloat(command.toString()));
                        newobj.add("NightMode", subjson);
                        changeSettings(newobj);
                    } catch (NumberFormatException exc) {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} only accepts a float value, and {} is not.",
                                channelUID.getIdWithoutGroup(), command.toString());
                    }
                    break;
                case "roomType":
                    newobj.addProperty("RoomType", command.toString());
                    changeSettings(newobj);
                    break;
                case "logLevel":
                    String ll = command.toString();
                    if ("Error".equals(ll) || "Warning".equals(ll) || "Info".equals(ll)) {
                        newobj.addProperty("logging", ll);
                        changeSettings(newobj);
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct setting for the power frequency suppression (only 50Hz or 60Hz)",
                                channelUID.getIdWithoutGroup(), command.toString());
                    }
                    break;
                case "averagingRhythm":
                    try {
                        newobj.addProperty("SecondsMeasurementDelay", Integer.parseUnsignedInt(command.toString()));
                    } catch (NumberFormatException exc) {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} only accepts an integer value, and {} is not.",
                                channelUID.getIdWithoutGroup(), command.toString());
                    }
                    break;
                case "powerFreqSuppression":
                    String newFreq = command.toString();
                    if ("50Hz".equals(newFreq) || "60Hz".equals(newFreq) || "50Hz+60Hz".equals(newFreq)) {
                        newobj.addProperty("Rejection", newFreq);
                        changeSettings(newobj);
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct setting for the power frequency suppression (only 50Hz or 60Hz)",
                                channelUID.getIdWithoutGroup(), command.toString());
                    }
                    break;
                default:
                    logger.warn(
                            "air-Q - airqHandler - handleCommand(): unknown command {} received (channelUID={}, value={})",
                            command, channelUID, command);
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(AirqConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollData, 0, POLLING_PERIOD_DATA_MSEC,
                TimeUnit.MILLISECONDS);
        getConfigDataJob = scheduler.scheduleWithFixedDelay(this::getConfigData, 0, POLLING_PERIOD_CONFIG,
                TimeUnit.MINUTES);
    }

    // AES decoding based on this tutorial: https://www.javainterviewpoint.com/aes-256-encryption-and-decryption/
    public String decrypt(byte[] base64text, String password) throws AirqException {
        String content = "";
        byte[] encodedtextwithIV = Base64.getDecoder().decode(base64text);
        byte[] ciphertext = Arrays.copyOfRange(encodedtextwithIV, 16, encodedtextwithIV.length);
        byte[] passkey = Arrays.copyOf(password.getBytes(), 32);
        if (password.length() < 32) {
            Arrays.fill(passkey, password.length(), 32, (byte) '0');
        }
        SecretKey seckey = new SecretKeySpec(passkey, 0, passkey.length, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(seckey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Arrays.copyOf(encodedtextwithIV, 16));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedText = cipher.doFinal(ciphertext);
            content = new String(decryptedText, StandardCharsets.UTF_8);
            return content;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException exc) {
            throw new AirqException(exc);
        } catch (BadPaddingException e) {
            throw new AirqPasswordIncorrectException();
        }
    }

    public String encrypt(byte[] toencode, String password) throws AirqException {
        byte[] passkey = Arrays.copyOf(password.getBytes(StandardCharsets.UTF_8), 32);
        if (password.length() < 32) {
            Arrays.fill(passkey, password.length(), 32, (byte) '0');
        }
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        SecretKey seckey = new SecretKeySpec(passkey, 0, passkey.length, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(seckey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedText = cipher.doFinal(toencode);
            byte[] totaltext = new byte[16 + encryptedText.length];
            System.arraycopy(iv, 0, totaltext, 0, 16);
            System.arraycopy(encryptedText, 0, totaltext, 16, encryptedText.length);
            byte[] encodedcontent = Base64.getEncoder().encode(totaltext);
            return new String(encodedcontent);
        } catch (BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException exc) {
            throw new AirqException("Failed to encrypt data", exc);
        }
    }

    // gets the data after online/offline management and does the JSON work, or at least the first step.
    protected String getDecryptedContentString(String url, String requestMethod, @Nullable String body)
            throws AirqException, InterruptedException {
        Result res = getData(url, "GET", null);
        String jsontext = res.getBody();
        // Gson code based on https://riptutorial.com/de/gson
        JsonElement ans = gson.fromJson(jsontext, JsonElement.class);
        if (ans == null) {
            throw new AirqEmptyResonseException();
        }

        JsonObject jsonObj = ans.getAsJsonObject();
        return decrypt(jsonObj.get("content").getAsString().getBytes(), config.password);
    }

    // calls the networking job and in addition does additional tests for online/offline management
    protected Result getData(String address, String requestMethod, @Nullable String body)
            throws AirqException, InterruptedException {
        int timeout = 10;
        Request request = httpClient.newRequest(address).timeout(timeout, TimeUnit.SECONDS).method(requestMethod);
        if (body != null) {
            request = request.content(new StringContentProvider(body)).header(HttpHeader.CONTENT_TYPE,
                    "application/json");
        }
        try {
            ContentResponse response = request.send();
            return new Result(response.getContentAsString(), response.getStatus());
        } catch (ExecutionException e) {
            throw new AirqException("Connection failed: " + e.getMessage());
        } catch (TimeoutException e) {
            throw new AirqException("Timeout while connecting");
        }
    }

    public static class Result {
        private final String body;
        private final int responseCode;

        public Result(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }

        public String getBody() {
            return body;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
        }

        ScheduledFuture<?> localGetConfigDataJob = getConfigDataJob;
        if (localGetConfigDataJob != null) {
            localGetConfigDataJob.cancel(true);
        }
    }

    public void pollData() {
        try {
            String url = "http://" + config.ipAddress + "/data";
            String jsonAnswer = getDecryptedContentString(url, "GET", null);

            try {
                JsonElement decEl = gson.fromJson(jsonAnswer, JsonElement.class);
                if (decEl == null) {
                    throw new AirqEmptyResonseException();
                }

                JsonObject decObj = decEl.getAsJsonObject();
                if (logger.isTraceEnabled()) {
                    logger.trace("Received measurement data (non-measurement values omitted): {}",
                            sanitizeData(decObj));
                }
                processMappings(decObj, MEASUREMENTS, null);

                updateStatus(ThingStatus.ONLINE);
            } catch (JsonSyntaxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Syntax error while parsing response from device");
            }
        } catch (AirqPasswordIncorrectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device password incorrect");
        } catch (AirqException e) {
            String causeMessage = "";
            Throwable cause = e.getCause();
            if (cause != null) {
                causeMessage = cause.getClass().getSimpleName() + ": " + cause.getMessage() + ": ";
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, causeMessage + e.getMessage());
        } catch (InterruptedException e) {
            // nothing
        }
    }

    public void getConfigData() {
        Result res = null;
        try {
            String url = "http://" + config.ipAddress + "/config";
            res = getData(url, "GET", null);
            String jsontext = res.getBody();
            JsonElement ans = gson.fromJson(jsontext, JsonElement.class);
            if (ans == null) {
                throw new AirqEmptyResonseException();
            }

            JsonObject jsonObj = ans.getAsJsonObject();
            String jsonAnswer = decrypt(jsonObj.get("content").getAsString().getBytes(), config.password);
            JsonElement decEl = gson.fromJson(jsonAnswer, JsonElement.class);
            if (decEl == null) {
                throw new AirqEmptyResonseException();
            }

            JsonObject decObj = decEl.getAsJsonObject();
            processMappings(decObj, CONFIGURATION, "general");
        } catch (AirqException | JsonSyntaxException e) {
            logger.warn("Failed to retrieve configuration: {}", e.getMessage());
        } catch (InterruptedException e) {
            // nothing
        }
    }

    private void processMappings(JsonObject data, List<ChannelMapping> mappings, @Nullable String groupOverride) {
        for (ChannelMapping mapping : mappings) {
            String groupId = groupOverride == null ? mapping.groupId() : groupOverride;
            String channelId = groupId + "#" + mapping.channelId();
            Unit<?> unit = mapping.unit();
            if (unit != null) {
                processMeasurement(data.get(mapping.dataKey()), channelId, unit);
            } else {
                processType(data, mapping.dataKey(), channelId, mapping.type());
            }
        }
    }

    private void processMeasurement(@Nullable JsonElement reading, String channel, Unit<?> unit) {
        if (reading == null) {
            return;
        }
        String errorChannel = errorChannel(channel, "advanced-maxerr", "-maxerr");
        if (reading.isJsonNull()) {
            updateMappedState(channel, UnDefType.UNDEF);
            updateMappedState(errorChannel, UnDefType.UNDEF);
        } else if (reading.isJsonArray() && reading.getAsJsonArray().size() == 2
                && isNumeric(reading.getAsJsonArray().get(0)) && isNumeric(reading.getAsJsonArray().get(1))) {
            JsonArray pair = reading.getAsJsonArray();
            updateMappedState(channel, new QuantityType<>(pair.get(0).getAsBigDecimal(), unit));
            updateMappedState(errorChannel, new QuantityType<>(pair.get(1).getAsBigDecimal(), unit));
        } else if (isNumeric(reading)) {
            updateMappedState(channel, new QuantityType<>(reading.getAsBigDecimal(), unit));
            updateMappedState(errorChannel, UnDefType.UNDEF);
        } else {
            updateMappedState(channel, UnDefType.UNDEF);
            updateMappedState(errorChannel, UnDefType.UNDEF);
            logger.debug("Ignoring malformed measurement for channel {}", channel);
        }
    }

    static JsonObject sanitizeData(JsonObject data) {
        JsonObject sanitized = new JsonObject();
        for (Entry<String, JsonElement> entry : data.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            boolean measurement = MEASUREMENT_KEYS.contains(key);
            if (measurement && (value.isJsonNull() || isNumeric(value)
                    || (value.isJsonArray() && value.getAsJsonArray().size() == 2
                            && isNumeric(value.getAsJsonArray().get(0)) && isNumeric(value.getAsJsonArray().get(1))))) {
                sanitized.add(key, value);
            } else {
                sanitized.addProperty(key, "<omitted>");
            }
        }
        return sanitized;
    }

    private static boolean isNumeric(JsonElement value) {
        return value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber();
    }

    private void processType(JsonObject dec, String airqName, String channelName, String type) {
        // If a device variant does not have a specific sensor type, the value is not present in the JSON data.
        // Under rare conditions an existing sensor has a null value in the JSON data on a single event.
        if (dec.get(airqName) == null || dec.get(airqName).isJsonNull()) {
            updateMappedState(channelName, UnDefType.UNDEF);
            if (type.contentEquals("pair")) {
                updateMappedState(errorChannel(channelName, "maxerr", "_maxerr"), UnDefType.UNDEF);
            }
        } else {
            switch (type) {
                case "boolean":
                    String itemval = dec.get(airqName).toString();
                    if (itemval.contentEquals("true") || itemval.contentEquals("1")) {
                        updateMappedState(channelName, OnOffType.ON);
                    } else if (itemval.contentEquals("false") || itemval.contentEquals("0")) {
                        updateMappedState(channelName, OnOffType.OFF);
                    }
                    break;
                case "string":
                case "time":
                    String strstr = dec.get(airqName).toString();
                    updateMappedState(channelName, new StringType(strstr.substring(1, strstr.length() - 1)));
                    break;
                case "number":
                    updateMappedState(channelName, new DecimalType(dec.get(airqName).toString()));
                    break;
                case "numberTimePeriod":
                    updateMappedState(channelName,
                            new QuantityType<>(dec.get(airqName).getAsBigInteger(), Units.SECOND));
                    break;
                case "pair":
                    ResultPair pair = new ResultPair(dec.get(airqName));
                    updateMappedState(channelName, new DecimalType(pair.getValue()));
                    updateMappedState(errorChannel(channelName, "maxerr", "_maxerr"),
                            new DecimalType(pair.getMaxdev()));
                    break;
                case "pairPPM":
                    ResultPair pairPPM = new ResultPair(dec.get(airqName));
                    updateMappedState(channelName, new QuantityType<>(pairPPM.getValue(), Units.PARTS_PER_MILLION));
                    updateMappedState(errorChannel(channelName, "maxerr", "_maxerr"),
                            new DecimalType(pairPPM.getMaxdev()));
                    break;
                case "pairPPB":
                    ResultPair pairPPB = new ResultPair(dec.get(airqName));
                    updateMappedState(channelName, new QuantityType<>(pairPPB.getValue(), Units.PARTS_PER_BILLION));
                    updateMappedState(errorChannel(channelName, "maxerr", "_maxerr"),
                            new DecimalType(pairPPB.getMaxdev()));
                    break;
                case "pairDB":
                    ResultPair pairDB = new ResultPair(dec.get(airqName));
                    updateMappedState(channelName, new QuantityType<>(pairDB.getValue(), Units.DECIBEL));
                    updateMappedState(errorChannel(channelName, "maxerr", "_maxerr"),
                            new DecimalType(pairDB.getMaxdev()));
                    break;
                case "index":
                    double rawValue = Double.parseDouble(dec.get(airqName).toString());
                    updateMappedState(channelName, new QuantityType<>(rawValue / 10, Units.PERCENT));
                    break;
                case "datetime":
                    Long timest = Long.valueOf(dec.get(airqName).toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String timestampString = sdf.format(new Date(timest));
                    updateMappedState(channelName, DateTimeType.valueOf(timestampString));
                    break;
                case "coord":
                    JsonElement ansCoord = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (ansCoord != null) {
                        JsonObject jsonCoord = ansCoord.getAsJsonObject();
                        Float latitude = jsonCoord.get("lat").getAsFloat();
                        Float longitude = jsonCoord.get("long").getAsFloat();
                        updateMappedState(channelName,
                                new PointType(new DecimalType(latitude), new DecimalType(longitude)));
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - processType(): Cannot extract coordinates from this data: {}",
                                dec.get(airqName).toString());
                    }
                    break;
                case "nightmode":
                    JsonElement daynightdata = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (daynightdata != null) {
                        JsonObject jsonDaynightdata = daynightdata.getAsJsonObject();
                        processType(jsonDaynightdata, "StartDay", nestedChannel(channelName, "nightModeStartDay"),
                                "string");
                        processType(jsonDaynightdata, "StartNight", nestedChannel(channelName, "nightModeStartNight"),
                                "string");
                        processType(jsonDaynightdata, "BrightnessDay",
                                nestedChannel(channelName, "nightModeBrightnessDay"), "number");
                        processType(jsonDaynightdata, "BrightnessNight",
                                nestedChannel(channelName, "nightModeBrightnessNight"), "number");
                        processType(jsonDaynightdata, "FanNightOff", nestedChannel(channelName, "nightModeFanNightOff"),
                                "boolean");
                        processType(jsonDaynightdata, "WifiNightOff",
                                nestedChannel(channelName, "nightModeWifiNightOff"), "boolean");
                    } else {
                        logger.warn("air-Q - airqHandler - processType(): Cannot extract day/night data: {}",
                                dec.get(airqName).toString());
                    }
                    break;
                case "wlan":
                    JsonElement wlandata = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (wlandata != null) {
                        JsonObject jsonWlandata = wlandata.getAsJsonObject();
                        processType(jsonWlandata, "Gateway", nestedChannel(channelName, "wlanConfigGateway"), "string");
                        processType(jsonWlandata, "MAC", nestedChannel(channelName, "wlanConfigMac"), "string");
                        processType(jsonWlandata, "SSID", nestedChannel(channelName, "wlanConfigSsid"), "string");
                        processType(jsonWlandata, "IP address", nestedChannel(channelName, "wlanConfigIPAddress"),
                                "string");
                        processType(jsonWlandata, "Net Mask", nestedChannel(channelName, "wlanConfigNetMask"),
                                "string");
                        processType(jsonWlandata, "BSSID", nestedChannel(channelName, "wlanConfigBssid"), "string");
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - processType(): Cannot extract WLAN data from this string: {}",
                                dec.get(airqName).toString());
                    }
                    break;
                case "arr":
                    JsonElement jsonarr = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if ((jsonarr != null) && (jsonarr.isJsonArray())) {
                        JsonArray arr = jsonarr.getAsJsonArray();
                        StringBuilder str = new StringBuilder();
                        for (JsonElement el : arr) {
                            str.append(el.getAsString() + ", ");
                        }
                        if (str.length() >= 2) {
                            updateMappedState(channelName, new StringType(str.substring(0, str.length() - 2)));
                        } else {
                            logger.trace("air-Q - airqHandler - processType(): cannot handle this as an array: {}",
                                    jsonarr);
                        }
                    } else {
                        logger.warn("air-Q - airqHandler - processType(): cannot handle this as an array: {}", jsonarr);
                    }
                    break;
                case "calib":
                    JsonElement lastcalib = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (lastcalib != null) {
                        JsonObject calibobj = lastcalib.getAsJsonObject();
                        String str = new String();
                        Long timecalib;
                        SimpleDateFormat sdfcalib = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm:ss");
                        for (Entry<String, JsonElement> entry : calibobj.entrySet()) {
                            String attributeName = entry.getKey();
                            JsonObject attributeValue = (JsonObject) entry.getValue();
                            timecalib = Long.valueOf(attributeValue.get("timestamp").toString());
                            String timecalibString = sdfcalib.format(new Date(timecalib * 1000));
                            str = str + attributeName + ": offset=" + attributeValue.get("offset").getAsString() + " ["
                                    + timecalibString + "]";
                        }
                        if (!str.isEmpty()) {
                            updateMappedState(channelName, new StringType(str.substring(0, str.length() - 1)));
                        } else {
                            logger.trace(
                                    "air-Q - airqHandler - processType(): Cannot extract calibration data from this string: {}",
                                    dec.get(airqName).toString());
                        }
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - processType(): Cannot extract calibration data from this string: {}",
                                dec.get(airqName).toString());
                    }
                    break;
                case "property":
                    String propstr = dec.get(airqName).toString();
                    getThing().setProperty(rawChannelId(channelName), propstr);
                    break;
                case "proparr":
                    JsonElement proparr = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if ((proparr != null) && proparr.isJsonArray()) {
                        JsonArray arr = proparr.getAsJsonArray();
                        String arrstr = new String();
                        for (JsonElement el : arr) {
                            arrstr = arrstr + el.getAsString() + ", ";
                        }
                        if (arrstr.length() >= 2) {
                            getThing().setProperty(rawChannelId(channelName), arrstr.substring(0, arrstr.length() - 2));
                        } else {
                            logger.trace("air-Q - airqHandler - processType(): cannot handle this as an array: {}",
                                    proparr);
                        }
                    } else {
                        logger.warn("air-Q - airqHandler - processType(): cannot handle this as an array: {}", proparr);
                    }
                    break;
                default:
                    logger.warn(
                            "air-Q - airqHandler - processType(): a setting of type {} should be changed but I don't know this type.",
                            type);
                    break;
            }
        }
    }

    private void updateMappedState(String channelId, org.openhab.core.types.State state) {
        if (!channelId.endsWith("#")) {
            updateState(channelId, state);
        }
    }

    private static String nestedChannel(String parentChannelId, String channelId) {
        int groupSeparator = parentChannelId.indexOf('#');
        return groupSeparator < 0 ? channelId : parentChannelId.substring(0, groupSeparator + 1) + channelId;
    }

    private static String errorChannel(String channelId, String groupId, String suffix) {
        int groupSeparator = channelId.indexOf('#');
        String rawChannelId = groupSeparator < 0 ? channelId : channelId.substring(groupSeparator + 1);
        return groupId + "#" + rawChannelId + suffix;
    }

    private static String rawChannelId(String channelId) {
        int groupSeparator = channelId.indexOf('#');
        return groupSeparator < 0 ? channelId : channelId.substring(groupSeparator + 1);
    }

    private void changeSettings(JsonObject jsonchange) {
        try {
            String jsoncmd = jsonchange.toString();
            Result res;
            String url = "http://" + config.ipAddress + "/config";
            String jsonbody = encrypt(jsoncmd.getBytes(StandardCharsets.UTF_8), config.password);
            String fullbody = "request=" + jsonbody;
            res = getData(url, "POST", fullbody);
            JsonElement ans = gson.fromJson(res.getBody(), JsonElement.class);

            if (ans == null) {
                throw new AirqEmptyResonseException();
            }

            JsonObject jsonObj = ans.getAsJsonObject();
            decrypt(jsonObj.get("content").getAsString().getBytes(), config.password);
        } catch (AirqException e) {
            logger.warn("Failed to change settings", e);
        } catch (InterruptedException e) {
            // nothing
        }
    }
}
