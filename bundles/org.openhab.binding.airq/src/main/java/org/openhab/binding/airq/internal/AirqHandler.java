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
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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
 */
@NonNullByDefault
public class AirqHandler extends BaseThingHandler {
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
         * @param input string formed as this: [1234,56,789,012] (including the brackets)
         * @return ResultPair object with the two values
         */
        public ResultPair(String input) {
            value = Float.parseFloat(input.substring(1, input.indexOf(',')));
            maxdev = Float.parseFloat(input.substring(input.indexOf(',') + 1, input.length() - 1));
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
            switch (channelUID.getId()) {
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
                    newobj.addProperty(channelUID.getId(), command.toString());
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
                                channelUID.getId(), command.toString());
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
                                channelUID.getId(), command.toString());
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
                                channelUID.getId(), command.toString());
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
                                channelUID.getId(), command.toString());
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
                                channelUID.getId(), command.toString());
                    }
                    break;
                case "averagingRhythm":
                    try {
                        newobj.addProperty("SecondsMeasurementDelay", Integer.parseUnsignedInt(command.toString()));
                    } catch (NumberFormatException exc) {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} only accepts an integer value, and {} is not.",
                                channelUID.getId(), command.toString());
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
                                channelUID.getId(), command.toString());
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
        config = getThing().getConfiguration().as(AirqConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollData, 0, POLLING_PERIOD_DATA_MSEC,
                TimeUnit.MILLISECONDS);
        getConfigDataJob = scheduler.scheduleWithFixedDelay(this::getConfigData, 0, POLLING_PERIOD_CONFIG,
                TimeUnit.MINUTES);
    }

    // AES decoding based on this tutorial: https://www.javainterviewpoint.com/aes-256-encryption-and-decryption/
    public String decrypt(byte[] base64text, String password) throws AirqException {
        String content = "";
        logger.trace("air-Q - airqHandler - decrypt(): content to decrypt: {}", base64text);
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
            logger.trace("air-Q - airqHandler - decrypt(): Text decoded as String: {}", content);
            return content;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException exc) {
            throw new AirqException(exc);
        } catch (BadPaddingException e) {
            throw new AirqPasswordIncorrectException();
        }
    }

    public String encrypt(byte[] toencode, String password) throws AirqException {
        logger.trace("air-Q - airqHandler - encrypt(): text to encode: {}", new String(toencode));
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
            logger.trace("air-Q - airqHandler - encrypt(): encrypted text: {}", encodedcontent);
            return new String(encodedcontent);
        } catch (BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException exc) {
            throw new AirqException("Failed to encrypt data", exc);
        }
    }

    // gets the data after online/offline management and does the JSON work, or at least the first step.
    protected String getDecryptedContentString(String url, String requestMethod, @Nullable String body)
            throws AirqException {
        Result res = getData(url, "GET", null);
        String jsontext = res.getBody();
        logger.trace("air-Q - airqHandler - getDecryptedContentString(): Result from getData() is {} with body={}", res,
                res.getBody());
        // Gson code based on https://riptutorial.com/de/gson
        JsonElement ans = gson.fromJson(jsontext, JsonElement.class);
        if (ans == null) {
            throw new AirqEmptyResonseException();
        }

        JsonObject jsonObj = ans.getAsJsonObject();
        return decrypt(jsonObj.get("content").getAsString().getBytes(), config.password);
    }

    // calls the networking job and in addition does additional tests for online/offline management
    protected Result getData(String address, String requestMethod, @Nullable String body) throws AirqException {
        int timeout = 10;
        logger.trace("air-Q - airqHandler - getData(): connecting to {} with method {} and body {}", address,
                requestMethod, body);
        Request request = httpClient.newRequest(address).timeout(timeout, TimeUnit.SECONDS).method(requestMethod);
        if (body != null) {
            request = request.content(new StringContentProvider(body)).header(HttpHeader.CONTENT_TYPE,
                    "application/json");
        }
        try {
            ContentResponse response = request.send();
            return new Result(response.getContentAsString(), response.getStatus());
        } catch (InterruptedException | ExecutionException | TimeoutException exc) {
            throw new AirqException("Error while accessing air-Q", exc);
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
        logger.trace("air-Q - airqHandler - run(): starting polled data handler");
        try {
            String url = "http://" + config.ipAddress + "/data";
            String jsonAnswer = getDecryptedContentString(url, "GET", null);
            JsonElement decEl = gson.fromJson(jsonAnswer, JsonElement.class);
            if (decEl == null) {
                throw new AirqEmptyResonseException();
            }

            JsonObject decObj = decEl.getAsJsonObject();
            logger.trace("air-Q - airqHandler - run(): decObj={}, jsonAnswer={}", decObj, jsonAnswer);
            // 'bat' is a field that is already delivered by air-Q but as
            // there are no air-Q devices which are powered with batteries
            // it is obsolete at this moment. We implemented the code anyway
            // to make it easier to add afterwords, but for the moment it is not applicable.
            // processType(decObj, "bat", "battery", "pair");
            processType(decObj, "cnt0_3", "fineDustCnt00_3", "pair");
            processType(decObj, "cnt0_5", "fineDustCnt00_5", "pair");
            processType(decObj, "cnt1", "fineDustCnt01", "pair");
            processType(decObj, "cnt2_5", "fineDustCnt02_5", "pair");
            processType(decObj, "cnt5", "fineDustCnt05", "pair");
            processType(decObj, "cnt10", "fineDustCnt10", "pair");
            processType(decObj, "co", "co", "pair");
            processType(decObj, "co2", "co2", "pairPPM");
            processType(decObj, "dewpt", "dewpt", "pair");
            processType(decObj, "h2s", "h2s", "pair");
            processType(decObj, "humidity", "humidityRelative", "pair");
            processType(decObj, "humidity_abs", "humidityAbsolute", "pair");
            processType(decObj, "no2", "no2", "pair");
            processType(decObj, "o3", "o3", "pair");
            processType(decObj, "oxygen", "o2", "pair");
            processType(decObj, "pm1", "fineDustConc01", "pair");
            processType(decObj, "pm2_5", "fineDustConc02_5", "pair");
            processType(decObj, "pm10", "fineDustConc10", "pair");
            processType(decObj, "pressure", "pressure", "pair");
            processType(decObj, "so2", "so2", "pair");
            processType(decObj, "sound", "sound", "pairDB");
            processType(decObj, "temperature", "temperature", "pair");
            // We have two places where the Device ID is delivered: with the measurement data and
            // with the configuration.
            // We take the info from the configuration and show it as a property, so we don't need
            // something like processType(decObj, "DeviceID", "DeviceID", "string") at this moment. We leave
            // this as a reminder in case for some reason it will be needed in future, e.g. when an air-Q
            // device also sends data from other devices (then with another Device ID)
            processType(decObj, "Status", "status", "string");
            processType(decObj, "TypPS", "avgFineDustSize", "number");
            processType(decObj, "dCO2dt", "dCO2dt", "number");
            processType(decObj, "dHdt", "dHdt", "number");
            processType(decObj, "door_event", "doorEvent", "number");
            processType(decObj, "health", "healthIndex", "index");
            processType(decObj, "health", "health", "number");
            processType(decObj, "measuretime", "measureTime", "number");
            processType(decObj, "performance", "performanceIndex", "index");
            processType(decObj, "performance", "performance", "number");
            processType(decObj, "timestamp", "timestamp", "datetime");
            processType(decObj, "uptime", "uptime", "numberTimePeriod");
            processType(decObj, "tvoc", "tvoc", "pairPPB");

            updateStatus(ThingStatus.ONLINE);
        } catch (AirqPasswordIncorrectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Device password incorrect");
        } catch (AirqException e) {
            String causeMessage = "";
            Throwable cause = e.getCause();
            if (cause != null) {
                causeMessage = cause.getClass().getSimpleName() + ": " + cause.getMessage() + ": ";
            }

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, causeMessage + e.getMessage());
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Syntax error while parsing response from device");
        }
    }

    public void getConfigData() {
        Result res = null;
        logger.trace("air-Q - airqHandler - getConfigData(): starting processing data");
        try {
            String url = "http://" + config.ipAddress + "/config";
            res = getData(url, "GET", null);
            String jsontext = res.getBody();
            logger.trace("air-Q - airqHandler - getConfigData(): Result from getBody() is {} with body={}", res,
                    res.getBody());
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
            logger.trace("air-Q - airqHandler - getConfigData(): decObj={}", decObj);
            processType(decObj, "Wifi", "wifi", "boolean");
            processType(decObj, "WLANssid", "ssid", "arr");
            processType(decObj, "pass", "password", "string");
            processType(decObj, "WifiInfo", "wifiInfo", "boolean");
            processType(decObj, "TimeServer", "timeServer", "string");
            processType(decObj, "geopos", "location", "coord");
            processType(decObj, "NightMode", "", "nightmode");
            processType(decObj, "devicename", "deviceName", "string");
            processType(decObj, "RoomType", "roomType", "string");
            processType(decObj, "logging", "logLevel", "string");
            processType(decObj, "DeleteKey", "deleteKey", "string");
            processType(decObj, "FireAlarm", "fireAlarm", "boolean");
            processType(decObj, "air-Q-Hardware-Version", "hardwareVersion", "property");
            processType(decObj, "WLAN config", "", "wlan");
            processType(decObj, "cloudUpload", "cloudUpload", "boolean");
            processType(decObj, "SecondsMeasurementDelay", "averagingRhythm", "number");
            processType(decObj, "Rejection", "powerFreqSuppression", "string");
            processType(decObj, "air-Q-Software-Version", "softwareVersion", "property");
            processType(decObj, "sensors", "sensorList", "proparr");
            processType(decObj, "AutoDriftCompensation", "autoDriftCompensation", "boolean");
            processType(decObj, "AutoUpdate", "autoUpdate", "boolean");
            processType(decObj, "AdvancedDataProcessing", "advancedDataProcessing", "boolean");
            processType(decObj, "Industry", "Industry", "property");
            processType(decObj, "ppm&ppb", "ppm_and_ppb", "boolean");
            processType(decObj, "GasAlarm", "gasAlarm", "boolean");
            processType(decObj, "id", "id", "property");
            processType(decObj, "SoundInfo", "soundPressure", "boolean");
            processType(decObj, "AlarmForwarding", "alarmForwarding", "boolean");
            processType(decObj, "usercalib", "userCalib", "calib");
            processType(decObj, "InitialCalFinished", "initialCalFinished", "boolean");
            processType(decObj, "Averaging", "averaging", "boolean");
            processType(decObj, "SensorInfo", "sensorInfo", "property");
            processType(decObj, "ErrorBars", "errorBars", "boolean");
            processType(decObj, "warmup-phase", "warmupPhase", "boolean");
        } catch (AirqException | JsonSyntaxException e) {
            logger.warn("Failed to retrieve configuration: {}", e.getMessage());
        }
    }

    private void processType(JsonObject dec, String airqName, String channelName, String type) {
        logger.trace("air-Q - airqHandler - processType(): airqName={}, channelName={}, type={}", airqName, channelName,
                type);
        if (dec.get(airqName) == null) {
            logger.trace("air-Q - airqHandler - processType(): get({}) is null", airqName);
            updateState(channelName, UnDefType.UNDEF);
            if (type.contentEquals("pair")) {
                updateState(channelName + "_maxerr", UnDefType.UNDEF);
            }
        } else {
            switch (type) {
                case "boolean":
                    String itemval = dec.get(airqName).toString();
                    if (itemval.contentEquals("true") || itemval.contentEquals("1")) {
                        updateState(channelName, OnOffType.ON);
                    } else if (itemval.contentEquals("false") || itemval.contentEquals("0")) {
                        updateState(channelName, OnOffType.OFF);
                    }
                    break;
                case "string":
                case "time":
                    String strstr = dec.get(airqName).toString();
                    updateState(channelName, new StringType(strstr.substring(1, strstr.length() - 1)));
                    break;
                case "number":
                    updateState(channelName, new DecimalType(dec.get(airqName).toString()));
                    break;
                case "numberTimePeriod":
                    updateState(channelName, new QuantityType<>(dec.get(airqName).getAsBigInteger(), Units.SECOND));
                    break;
                case "pair":
                    ResultPair pair = new ResultPair(dec.get(airqName).toString());
                    updateState(channelName, new DecimalType(pair.getValue()));
                    updateState(channelName + "_maxerr", new DecimalType(pair.getMaxdev()));
                    break;
                case "pairPPM":
                    ResultPair pairPPM = new ResultPair(dec.get(airqName).toString());
                    updateState(channelName, new QuantityType<>(pairPPM.getValue(), Units.PARTS_PER_MILLION));
                    updateState(channelName + "_maxerr", new DecimalType(pairPPM.getMaxdev()));
                    break;
                case "pairPPB":
                    ResultPair pairPPB = new ResultPair(dec.get(airqName).toString());
                    updateState(channelName, new QuantityType<>(pairPPB.getValue(), Units.PARTS_PER_BILLION));
                    updateState(channelName + "_maxerr", new DecimalType(pairPPB.getMaxdev()));
                    break;
                case "pairDB":
                    ResultPair pairDB = new ResultPair(dec.get(airqName).toString());
                    logger.trace("air-Q - airqHandler - processType(): db transmitted as {} with unit {}",
                            pairDB.getValue(), Units.DECIBEL);
                    updateState(channelName, new QuantityType<>(pairDB.getValue(), Units.DECIBEL));
                    updateState(channelName + "_maxerr", new DecimalType(pairDB.getMaxdev()));
                    break;
                case "index":
                    double rawValue = Double.parseDouble(dec.get(airqName).toString());
                    updateState(channelName, new QuantityType<>(rawValue / 10, Units.PERCENT));
                    break;
                case "datetime":
                    Long timest = Long.valueOf(dec.get(airqName).toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String timestampString = sdf.format(new Date(timest));
                    updateState(channelName, DateTimeType.valueOf(timestampString));
                    break;
                case "coord":
                    JsonElement ansCoord = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (ansCoord != null) {
                        JsonObject jsonCoord = ansCoord.getAsJsonObject();
                        Float latitude = jsonCoord.get("lat").getAsFloat();
                        Float longitude = jsonCoord.get("long").getAsFloat();
                        updateState(channelName, new PointType(new DecimalType(latitude), new DecimalType(longitude)));
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
                        processType(jsonDaynightdata, "StartDay", "nightModeStartDay", "string");
                        processType(jsonDaynightdata, "StartNight", "nightModeStartNight", "string");
                        processType(jsonDaynightdata, "BrightnessDay", "nightModeBrightnessDay", "number");
                        processType(jsonDaynightdata, "BrightnessNight", "nightModeBrightnessNight", "number");
                        processType(jsonDaynightdata, "FanNightOff", "nightModeFanNightOff", "boolean");
                        processType(jsonDaynightdata, "WifiNightOff", "nightModeWifiNightOff", "boolean");
                    } else {
                        logger.warn("air-Q - airqHandler - processType(): Cannot extract day/night data: {}",
                                dec.get(airqName).toString());
                    }
                    break;
                case "wlan":
                    JsonElement wlandata = gson.fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (wlandata != null) {
                        JsonObject jsonWlandata = wlandata.getAsJsonObject();
                        processType(jsonWlandata, "Gateway", "wlanConfigGateway", "string");
                        processType(jsonWlandata, "MAC", "wlanConfigMac", "string");
                        processType(jsonWlandata, "SSID", "wlanConfigSsid", "string");
                        processType(jsonWlandata, "IP address", "wlanConfigIPAddress", "string");
                        processType(jsonWlandata, "Net Mask", "wlanConfigNetMask", "string");
                        processType(jsonWlandata, "BSSID", "wlanConfigBssid", "string");
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
                            updateState(channelName, new StringType(str.substring(0, str.length() - 2)));
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
                            updateState(channelName, new StringType(str.substring(0, str.length() - 1)));
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
                    getThing().setProperty(channelName, propstr);
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
                            logger.trace("air-Q - airqHandler - processType(): property array {} set to {}",
                                    channelName, arrstr.substring(0, arrstr.length() - 2));
                            getThing().setProperty(channelName, arrstr.substring(0, arrstr.length() - 2));
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

    private void changeSettings(JsonObject jsonchange) {
        try {
            String jsoncmd = jsonchange.toString();
            logger.trace("air-Q - airqHandler - changeSettings(): called with jsoncmd={}", jsoncmd);
            Result res;
            String url = "http://" + config.ipAddress + "/config";
            String jsonbody = encrypt(jsoncmd.getBytes(StandardCharsets.UTF_8), config.password);
            String fullbody = "request=" + jsonbody;
            logger.trace("air-Q - airqHandler - changeSettings(): doing call to url={}, method=POST, body={}", url,
                    fullbody);
            res = getData(url, "POST", fullbody);
            JsonElement ans = gson.fromJson(res.getBody(), JsonElement.class);

            if (ans == null) {
                throw new AirqEmptyResonseException();
            }

            JsonObject jsonObj = ans.getAsJsonObject();
            String jsonAnswer;
            jsonAnswer = decrypt(jsonObj.get("content").getAsString().getBytes(), config.password);
            logger.trace("air-Q - airqHandler - changeSettings(): call returned {}", jsonAnswer);
        } catch (AirqException e) {
            logger.warn("Failed to change settings", e);
        }
    }
}
