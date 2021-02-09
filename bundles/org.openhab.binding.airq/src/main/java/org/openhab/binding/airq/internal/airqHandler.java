/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
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

/**
 * The {@link airqHandler} is responsible for retrieving all information from the air-Q device
 * and change properties and channels accordingly.
 *
 * @author Aurelio Caliaro - Initial contribution
 */
@NonNullByDefault
public class airqHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(airqHandler.class);
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> getConfigDataJob;
    private String ipaddress;
    private String password;
    private @Nullable ThingStatus thStatus;
    protected static final int POLLING_PERIOD_DATA = 15000; // in milliseconds
    protected static final int POLLING_PERIOD_CONFIG = 1; // in minutes

    final class ResultPair {
        private final float value;
        private final float maxdev;

        public float getvalue() {
            return value;
        }

        public float getmaxdev() {
            return maxdev;
        }

        // ResultPair() expects a string formed as this: [1234,56,789,012] and gives back a ResultPair
        // consisting of the two numbers
        public ResultPair(String input) {
            value = Float.parseFloat(input.substring(1, input.indexOf(',')));
            maxdev = Float.parseFloat(input.substring(input.indexOf(',') + 1, input.length() - 1));
        }
    }

    public airqHandler(Thing thing) {
        super(thing);
        ipaddress = "";
        password = "";
    }

    public Boolean ohCmd2airqCmd(String ohcmd) {
        switch (ohcmd) {
            case "ON":
                return true;
            case "OFF":
                return false;
        }
        return false;
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
        logger.info(
                "air-Q - airqHandler - handleCommand(): request received to handle value {} command {} of channelUID={}",
                command, command.getClass(), channelUID);
        if ((command instanceof OnOffType) || (command instanceof StringType)) {
            JsonObject newobj = new JsonObject();
            JsonObject subjson = new JsonObject();
            switch (channelUID.getId()) {
                case "wifi":
                    // we do not allow to switch off Wifi because otherwise we can't connect to the air-Q device anymore
                    break;
                case "wifiInfo":
                    newobj.addProperty("WifiInfo", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "fireAlarm":
                    newobj.addProperty("FireAlarm", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "cloudUpload":
                    newobj.addProperty("cloudUpload", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "autoDriftCompensation":
                    newobj.addProperty("AutoDriftCompensation", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "autoUpdate":
                    // note that this property is binary but uses 1 and 0 instead of true and false
                    newobj.addProperty("AutoUpdate", command.toString() == "ON" ? 1 : 0);
                    changeSettings(newobj);
                    break;
                case "advancedDataProcessing":
                    newobj.addProperty("AdvancedDataProcessing", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "gasAlarm":
                    newobj.addProperty("GasAlarm", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "soundPressure":
                    newobj.addProperty("SoundInfo", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "alarmForwarding":
                    newobj.addProperty("AlarmForwarding", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "averaging":
                    newobj.addProperty("averaging", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                case "errorBars":
                    newobj.addProperty("ErrorBars", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                    break;
                /*
                 * Not supported yet!
                 *
                 * case "getHistoryFiles":
                 * getDataFiles();
                 * break;
                 */ case "ppm_and_ppb":
                    newobj.addProperty("ppm&ppb", ohCmd2airqCmd(command.toString()));
                    changeSettings(newobj);
                case "nightmode_FanNightOff":
                    subjson.addProperty("FanNightOff", ohCmd2airqCmd(command.toString()));
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "nightmode_WifiNightOff":
                    subjson.addProperty("WifiNightOff", ohCmd2airqCmd(command.toString()));
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "SSID":
                    JsonElement wifidatael = new Gson().fromJson(command.toString(), JsonElement.class);
                    if (wifidatael != null) {
                        JsonObject wifidataobj = wifidatael.getAsJsonObject();
                        newobj.addProperty("WiFissid", wifidataobj.get("WiFissid").getAsString());
                        newobj.addProperty("WiFipass", wifidataobj.get("WiFipass").getAsString());
                        String bssid = wifidataobj.get("WiFibssid").getAsString();
                        if (!bssid.isEmpty()) {
                            newobj.addProperty("WiFibssid", bssid);
                        }
                        newobj.addProperty("reset", wifidataobj.get("reset").getAsString());
                        logger.info("air-Q - airqHandler - handleCommand(): dummy to change settings: {}",
                                newobj.toString());
                        changeSettings(newobj);
                    } else {
                        logger.error("Cannot extract wlan data from this string: {}", wifidatael);
                    }
                    break;
                case "timeServer":
                    newobj.addProperty(channelUID.getId(), command.toString());
                    changeSettings(newobj);
                    break;
                case "nightmode_StartDay":
                    if (isTimeFormat(command.toString())) {
                        subjson.addProperty("StartDay", command.toString());
                        newobj.add("NightMode", subjson);
                        changeSettings(newobj);
                    } else {
                        logger.error(
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct time format (eg. 08:00)",
                                channelUID.getId(), command.toString());
                    }
                    break;
                case "nightmode_StartNight":
                    if (isTimeFormat(command.toString())) {
                        subjson.addProperty("StartNight", command.toString());
                        newobj.add("NightMode", subjson);
                        changeSettings(newobj);
                    } else {
                        logger.error(
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct time format (eg. 08:00)",
                                channelUID.getId(), command.toString());
                    }
                    break;
                // TODO
                case "location":
                    break;
                case "nightmode_BrightnessDay":
                    break;
                case "nightmode_BrightnessNight":
                    break;
                case "roomType":
                    break;
                case "logLevel":
                    break;
                case "averagingRhythm":
                    break;
                case "powerFreqSuppression":
                    break;
                default:
                    logger.error(
                            "air-Q - airqHandler - handleCommand(): unknown command {} received (channelUID={}, value={})",
                            command, channelUID, command);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("air-Q - airqHandler - initialize(): ipaddress={}, password={}",
                getThing().getConfiguration().get("ipAddress"), getThing().getConfiguration().get("password"));
        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);
        if (getThing().getConfiguration().get("ipAddress") != null) {
            ipaddress = getThing().getConfiguration().get("ipAddress").toString();
        }
        if (getThing().getConfiguration().get("password") != null) {
            password = getThing().getConfiguration().get("password").toString();
        }
        if ((ipaddress.isEmpty()) || (password.isEmpty())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP Address and the device password must be provided to access your air-Q.");
            return;
        } else {
            // we try if the device is reachable and the password is correct. Otherwise a corresponding message is
            // thrown in Thing manager.
            String data = getDecryptedContentString("http://".concat(ipaddress.concat("/data")), "GET", null);
            if (data == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "We tried to get data from the air-Q device, but failed. Probable cause: invalid password.");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }

        }
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollData, 0, POLLING_PERIOD_DATA, TimeUnit.MILLISECONDS);
        getConfigDataJob = scheduler.scheduleWithFixedDelay(this::getConfigData, 0, POLLING_PERIOD_CONFIG,
                TimeUnit.MINUTES);
        logger.debug("air-Q - airqHandler - initialize() finished");
    }

    // AES decoding based on this tutorial: https://www.javainterviewpoint.com/aes-256-encryption-and-decryption/
    public @Nullable String decrypt(byte[] base64text, String password) {
        String content = "";
        logger.trace("air-Q - airqHandler - decrypt(): password={}, content to decypt: {}", password, base64text);
        byte[] encodedtextwithIV = Base64.getDecoder().decode(base64text);
        byte[] ciphertext = Arrays.copyOfRange(encodedtextwithIV, 16, encodedtextwithIV.length);
        byte[] passkey = Arrays.copyOf(password.getBytes(), 32);
        if (password.length() < 32) {
            Arrays.fill(passkey, password.length(), 32, (byte) '0');
        }
        byte[] IV = Arrays.copyOf(encodedtextwithIV, 16);
        // We comment this out because by setting the appropriate log level a third party could find the air-Q password.
        // logger.trace("air-Q - airqHandler - decrypt(): passkey={}", passkey);
        // logger.trace("air-Q - airqHandler - decrypt(): IV={}", IV);
        // logger.trace("air-Q - airqHandler - decrypt(): text to decode: {}", ciphertext);
        SecretKey seckey = new SecretKeySpec(passkey, 0, passkey.length, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(seckey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decryptedText = cipher.doFinal(ciphertext);
            content = new String(decryptedText);
            logger.trace("air-Q - airqHandler - decrypt(): Text decoded as String: {}", content);
        } catch (BadPaddingException bpe) {
            if (System.out != null) {
                System.out.println("Error while decrypting. Probably the provided password is wrong.");
            }
            return null;
        } catch (Exception e) {
            if (System.out != null) {
                System.out.println("air-Q - airqHandler - decrypt(): Error while decrypting: " + e.toString());
            }
            return null;
        }
        return content;
    }

    public String encrypt(byte[] toencode, String password) {
        String content = "";
        logger.trace("air-Q - airqHandler - encrypt(): text to encode: {}", new String(toencode));
        byte[] passkey = Arrays.copyOf(password.getBytes(), 32);
        if (password.length() < 32) {
            Arrays.fill(passkey, password.length(), 32, (byte) '0');
        }
        byte[] IV = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(IV);
        SecretKey seckey = new SecretKeySpec(passkey, 0, passkey.length, "AES");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(seckey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encryptedText = cipher.doFinal(toencode);
            byte[] totaltext = new byte[16 + encryptedText.length];
            System.arraycopy(IV, 0, totaltext, 0, 16);
            System.arraycopy(encryptedText, 0, totaltext, 16, encryptedText.length);
            byte[] encodedcontent = Base64.getEncoder().encode(totaltext);
            logger.trace("air-Q - airqHandler - encrypt(): encrypted text: {}", encodedcontent);
            content = new String(encodedcontent);
        } catch (Exception e) {
            if (System.out != null) {
                System.out.println("air-Q - airqHandler - encrypt(): Error while encrypting: " + e.toString());
            }
        }
        return content;
    }

    protected @Nullable String getDecryptedContentString(String url, String requestMethod, @Nullable String body) {
        Result res = null;
        String jsonAnswer = null;
        res = getData(url, "GET", null);
        if (res != null) {
            String jsontext = res.getBody();
            logger.trace("air-Q - airqHandler - getDecryptedContentString(): Result from doNetwork is {} with body={}",
                    res, res.getBody());
            // Gson code based on https://riptutorial.com/de/gson
            Gson gson = new Gson();
            JsonElement ans = gson.fromJson(jsontext, JsonElement.class);
            if (ans != null) {
                JsonObject jsonObj = ans.getAsJsonObject();
                jsonAnswer = decrypt(jsonObj.get("content").getAsString().getBytes(),
                        (String) (getThing().getConfiguration().get("password")));
                if (jsonAnswer == null) {
                    logger.error("The air-Q data could not be decrypted. Probably the password is wrong.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong password");
                }
            } else {
                logger.error("The air-Q data could not be extracted from this string: {}", ans);
            }
        }
        return jsonAnswer;
    }

    // Do the networking job and in addition does additional tests for online/offline management
    protected @Nullable Result getData(String address, String requestMethod, @Nullable String body) {
        Result res = null;
        res = doNetwork(address, "GET", body);
        if (res == null) {
            if (thStatus != ThingStatus.OFFLINE) {
                logger.error("air-Q - airqHandler - run(): cannot reach air-Q device. Status set to OFFLINE.");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "air-Q device not reachable");
                thStatus = ThingStatus.OFFLINE;
            } else {
                logger.warn("air-Q - airqHandler - run(): retried but still cannot reach the air-Q device.");
            }
        } else {
            if (thStatus == ThingStatus.OFFLINE) {
                logger.error("air-Q - airqHandler - run(): can reach air-Q device again, Status set back to ONLINE.");
                thStatus = ThingStatus.ONLINE;
                updateStatus(ThingStatus.ONLINE);
            }
        }
        return res;
    }

    protected @Nullable Result doNetwork(String address, String requestMethod, @Nullable String body) {
        int timeout = 10000;
        HttpURLConnection conn = null;
        logger.debug("air-Q - airqHandler - doNetwork(): connecting to {} with method {} and body {}", address,
                requestMethod, body);
        try {
            conn = (HttpURLConnection) new URL(address).openConnection();
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            if (body != null && !body.isEmpty()) {
                conn.setDoOutput(true);
                try (Writer out = new OutputStreamWriter(conn.getOutputStream())) {
                    out.write(body);
                }
            }
            try (InputStream in = conn.getInputStream(); ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                conn.disconnect();
                return new Result(result.toString(StandardCharsets.UTF_8.name()), conn.getResponseCode());
            }
        } catch (IOException exc) {
            if (System.out != null) {

                {
                    System.out.println(
                            "air-Q - airqHandler - doNetwork(): Error while accessing air-Q: " + exc.toString());
                }
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
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
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        if (getConfigDataJob != null) {
            getConfigDataJob.cancel(true);
        }
    }

    public void pollData() {
        logger.trace("air-Q - airqHandler - run(): starting polled data handler");
        if ((!ipaddress.isEmpty()) && (!password.isEmpty())) {
            try {
                String url = "http://".concat(ipaddress.concat("/data"));
                String jsonAnswer = getDecryptedContentString(url, "GET", null);
                if (jsonAnswer != null) {
                    Gson gson = new Gson();
                    JsonElement decEl = gson.fromJson(jsonAnswer, JsonElement.class);
                    if (decEl != null) {
                        JsonObject decObj = decEl.getAsJsonObject();
                        logger.debug("air-Q - airqHandler - run(): decObj={}", decObj);
                        // processType(decObj, "bat", "battery", "pair");
                        processType(decObj, "cnt0_3", "fineDustCnt00_3", "pair");
                        processType(decObj, "cnt0_5", "fineDustCnt00_5", "pair");
                        processType(decObj, "cnt1", "fineDustCnt01", "pair");
                        processType(decObj, "cnt2_5", "fineDustCnt02_5", "pair");
                        processType(decObj, "cnt5", "fineDustCnt05", "pair");
                        processType(decObj, "cnt10", "fineDustCnt10", "pair");
                        processType(decObj, "co", "co", "pair");
                        processType(decObj, "co2", "co2", "pair");
                        processType(decObj, "dewpt", "dewpt", "pair");
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
                        processType(decObj, "sound", "sound", "pair");
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
                        processType(decObj, "health", "health", "number");
                        processType(decObj, "measuretime", "measureTime", "number");
                        processType(decObj, "performance", "performance", "number");
                        processType(decObj, "timestamp", "timestamp", "datetime");
                        processType(decObj, "uptime", "uptime", "number");
                        processType(decObj, "tvoc", "tvoc", "pair");
                    } else {
                        logger.error("The air-Q data could not be extracted from this string: {}", decEl);
                    }
                }
            } catch (Exception e) {
                if (System.out != null) {
                    System.out.println(
                            "air-Q - airqHandler - polldata.run(): Error while retrieving air-Q data: " + e.toString());
                }
            }
        }
    }

    public void getConfigData() {
        Result res = null;
        logger.trace("air-Q - airqHandler - processConfigData(): starting processing data");
        if ((!ipaddress.isEmpty()) && (!password.isEmpty())) {
            try {
                String url = "http://".concat(ipaddress.concat("/config"));
                res = getData(url, "GET", null);
                if (res != null) {
                    String jsontext = res.getBody();
                    logger.trace("air-Q - airqHandler - processConfigData(): Result from doNetwork is {} with body={}",
                            res, res.getBody());
                    Gson gson = new Gson();
                    JsonElement ans = gson.fromJson(jsontext, JsonElement.class);
                    if (ans != null) {
                        JsonObject jsonObj = ans.getAsJsonObject();
                        String jsonAnswer = decrypt(jsonObj.get("content").getAsString().getBytes(),
                                (String) (getThing().getConfiguration().get("password")));
                        if (jsonAnswer != null) {
                            JsonElement decEl = gson.fromJson(jsonAnswer, JsonElement.class);
                            if (decEl != null) {
                                JsonObject decObj = decEl.getAsJsonObject();
                                logger.debug("air-Q - airqHandler - processConfigData(): decObj={}", decObj);
                                processType(decObj, "Wifi", "wifi", "boolean");
                                processType(decObj, "WLANssid", "SSID", "arr");
                                processType(decObj, "pass", "password", "string");
                                processType(decObj, "WifiInfo", "wifiInfo", "boolean");
                                processType(decObj, "TimeServer", "timeServer", "string");
                                processType(decObj, "geopos", "location", "coord");
                                processType(decObj, "NightMode", "", "nightmode");
                                processType(decObj, "devicename", "devicename", "string");
                                processType(decObj, "RoomType", "roomType", "string");
                                processType(decObj, "Logging", "logLevel", "string");
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
                            } else {
                                logger.error("The air-Q data could not be extracted from this string: {}", decEl);
                            }
                        }
                    } else {
                        logger.error("The air-Q data could not be extracted from this string: {}", ans);
                    }
                }
            } catch (Exception e) {
                if (System.out != null) {

                    System.out.println("Error in processConfigData(): " + e.toString());
                }
            }
        }
    }

    private void processType(JsonObject dec, String airqName, String channelName, String type) {
        logger.trace("air-Q - airqHandler - processType(): airqName={}, channelName={}, type={}, dec={}", airqName,
                channelName, type, dec);
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
                    logger.trace("air-Q - airqHandler - processType(): channel {} set to {}", channelName, itemval);
                    break;
                case "string":
                case "time":
                    String strstr = dec.get(airqName).toString();
                    updateState(channelName, new StringType(strstr.substring(1, strstr.length() - 1)));
                    logger.trace("air-Q - airqHandler - processType(): channel {} set to {}", channelName, strstr);
                    break;
                case "number":
                    updateState(channelName, new DecimalType(dec.get(airqName).toString()));
                    logger.trace("air-Q - airqHandler - processType(): channel {} set to {}", channelName,
                            dec.get(airqName).toString());
                    break;
                case "pair":
                    ResultPair pair = new ResultPair(dec.get(airqName).toString());
                    updateState(channelName, new DecimalType(pair.getvalue()));
                    updateState(channelName + "_maxerr", new DecimalType(pair.getmaxdev()));
                    logger.trace("air-Q - airqHandler - processType(): channel {} set to {}, channel {} set to {}",
                            channelName, pair.getvalue(), channelName + "_maxerr", pair.getmaxdev());
                    break;
                case "datetime":
                    Long timest = Long.valueOf(dec.get(airqName).toString());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    String timestampString = sdf.format(new Date(timest));
                    updateState(channelName, DateTimeType.valueOf(timestampString));
                    logger.trace("air-Q - airqHandler - processType(): channel {} set to {} (original: {})",
                            channelName, timestampString, timest);
                    break;
                case "coord":
                    JsonElement ans_coord = new Gson().fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (ans_coord != null) {
                        JsonObject json_coord = ans_coord.getAsJsonObject();
                        Float latitude = json_coord.get("lat").getAsFloat();
                        Float longitude = json_coord.get("long").getAsFloat();
                        updateState(channelName, new PointType(new DecimalType(latitude), new DecimalType(longitude)));
                    } else {
                        logger.error("Cannot extract coordinates from this data: {}", dec.get(airqName).toString());
                    }
                    break;
                case "nightmode":
                    JsonElement daynightdata = new Gson().fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (daynightdata != null) {
                        JsonObject json_daynightdata = daynightdata.getAsJsonObject();
                        processType(json_daynightdata, "StartDay", "nightMode_startDay", "string");
                        processType(json_daynightdata, "StartNight", "nightMode_startNight", "string");
                        processType(json_daynightdata, "BrightnessDay", "nightMode_brightnessDay", "number");
                        processType(json_daynightdata, "BrightnessNight", "nightMode_brightnessNight", "number");
                        processType(json_daynightdata, "FanNightOff", "nightMode_fanNightOff", "boolean");
                        processType(json_daynightdata, "WifiNightOff", "nightMode_wifiNightOff", "boolean");
                    } else {
                        logger.error("Cannot extract day/night data: {}", dec.get(airqName).toString());
                    }
                    break;
                case "wlan":
                    JsonElement wlandata = new Gson().fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if (wlandata != null) {
                        JsonObject json_wlandata = wlandata.getAsJsonObject();
                        processType(json_wlandata, "Gateway", "WLAN_config_gateway", "string");
                        processType(json_wlandata, "MAC", "WLAN_config_MAC", "string");
                        processType(json_wlandata, "SSID", "WLAN_config_SSID", "string");
                        processType(json_wlandata, "IP address", "WLAN_config_IPAddress", "string");
                        processType(json_wlandata, "Net Mask", "WLAN_config_netMask", "string");
                        processType(json_wlandata, "BSSID", "WLAN_config_BSSID", "string");
                    } else {
                        logger.error("Cannot extract WLAN data from this string: {}", dec.get(airqName).toString());
                    }
                    break;
                case "arr":
                    JsonElement jsonarr = new Gson().fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if ((jsonarr != null) && (jsonarr.isJsonArray())) {
                        JsonArray arr = jsonarr.getAsJsonArray();
                        String str = new String();
                        for (JsonElement el : arr) {
                            str = str.concat(el.getAsString()).concat(", ");
                        }
                        logger.trace("air-Q - airqHandler - processType(): channel {} set to {}", channelName,
                                str.substring(0, str.length() - 2));
                        updateState(channelName, new StringType(str.substring(0, str.length() - 2)));
                    } else {
                        logger.error("air-Q - airqHandler - processType(): cannot handle this as an array: {}",
                                jsonarr);
                    }
                    break;
                case "calib":
                    JsonElement lastcalib = new Gson().fromJson(dec.get(airqName).toString(), JsonElement.class);
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
                            str = str.concat(attributeName).concat(": offset=")
                                    .concat(attributeValue.get("offset").getAsString()).concat(" [")
                                    .concat(timecalibString).concat("]");
                        }
                        logger.trace("air-Q - airqHandler - processType(): channel {} set to {}", channelName,
                                str.substring(0, str.length() - 1));
                        updateState(channelName, new StringType(str.substring(0, str.length() - 1)));
                    } else {
                        logger.error("Cannot extract calibration data from this string: {}",
                                dec.get(airqName).toString());
                    }
                    break;
                case "property":
                    String propstr = dec.get(airqName).toString();
                    getThing().setProperty(channelName, propstr);
                    logger.trace("air-Q - airqHandler - processType(): property {} set to {}", channelName, propstr);
                    break;
                case "proparr":
                    JsonElement proparr = new Gson().fromJson(dec.get(airqName).toString(), JsonElement.class);
                    if ((proparr != null) && proparr.isJsonArray()) {
                        JsonArray arr = proparr.getAsJsonArray();
                        String arrstr = new String();
                        for (JsonElement el : arr) {
                            arrstr = arrstr.concat(el.getAsString()).concat(", ");
                        }
                        logger.trace("air-Q - airqHandler - processType(): property array {} set to {}", channelName,
                                arrstr.substring(0, arrstr.length() - 2));
                        getThing().setProperty(channelName, arrstr.substring(0, arrstr.length() - 2));
                    } else {
                        logger.error("air-Q - airqHandler - processType(): cannot handle this as an array: {}",
                                proparr);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void changeSettings(JsonObject jsonchange) {
        String jsoncmd = jsonchange.toString();
        logger.trace("air-Q - airqHandler - changeSettings(): called with jsoncmd={}", jsoncmd);
        if ((!ipaddress.isEmpty()) && (!password.isEmpty())) {
            Result res = null;
            try {
                String url = "http://".concat(ipaddress.concat("/config"));
                String jsonbody = encrypt(jsoncmd.getBytes(), (String) (getThing().getConfiguration().get("password")));
                String fullbody = "request=".concat(jsonbody);
                logger.trace("air-Q - airqHandler - changeSettings(): doing call to url={}, method=POST, body={}", url,
                        fullbody);
                res = getData(url, "POST", fullbody);
                if (res != null) {
                    Gson gson = new Gson();
                    JsonElement ans = gson.fromJson(res.getBody(), JsonElement.class);
                    if (ans != null) {
                        JsonObject jsonObj = ans.getAsJsonObject();
                        String jsonAnswer = decrypt(jsonObj.get("content").getAsString().getBytes(),
                                (String) (getThing().getConfiguration().get("password")));
                        logger.trace("air-Q - airqHandler - changeSettings(): call returned {}", jsonAnswer);
                    } else {
                        logger.error("The air-Q data could not be extracted from this string: {}", ans);
                    }
                }
            } catch (Exception e) {
                if (System.out != null) {

                    System.out.println(
                            "air-Q - airqHandler - prepareChangeSettings(): Error while changing settings in air-Q data: "
                                    + e.toString());
                }
            }
        }
    }
    // Getting the data files will be implemented in future, but it is not ready yet
    /*
     * private void getDataFiles() {
     * Result res = null;
     * String url = "http://".concat(ipaddress.concat("/dirbuff"));
     * try {
     * File f_base = createDataDir("/air-q_data");
     * if (f_base.isDirectory()) {
     * res = getData(url, "GET", null);
     * if (res != null) {
     * logger.trace("air-Q - airqHandler - getDataFiles(): Result from doNetwork is {} with body={}", res,
     * res.getBody());
     * String answer = decrypt(res.getBody().getBytes(),
     * (String) (getThing().getConfiguration().get("password")));
     * logger.trace("air-Q - airqHandler - getDataFiles(): Result after decrypt: {}", answer);
     * // We got the directory and file structure. Now iterate through all files and copy them to the file
     * // system
     * Gson gson = new Gson();
     * JsonElement gsonEl = gson.fromJson(answer, JsonElement.class);
     * if (gsonEl != null) {
     * JsonObject jsonObj = gsonEl.getAsJsonObject();
     * Iterator<String> ityr = jsonObj.keySet().iterator();
     * while (ityr.hasNext()) {
     * String year = ityr.next();
     * JsonObject jsonmonths = jsonObj.getAsJsonObject(year);
     * Iterator<String> itmon = jsonmonths.keySet().iterator();
     * while (itmon.hasNext()) {
     * String month = itmon.next();
     * JsonObject jsondays = jsonmonths.getAsJsonObject(month);
     * Iterator<String> itday = jsondays.keySet().iterator();
     * while (itday.hasNext()) {
     * String day = itday.next();
     * File f_day = createDataDir("/air-q_data/" + year + "/" + month + "/" + day);
     * if (f_day.isDirectory()) {
     * JsonArray jsonfilearr = jsondays.getAsJsonArray(day);
     * for (JsonElement el : jsonfilearr) {
     * String filename = el.getAsString();
     * String fullfilename = "air-q_data/" + year + "/" + month + "/" + day + "/"
     * + filename;
     * // We test if the file exists already. If it does, we do not download it
     * // again.
     * File f = new File(fullfilename);
     * if (f.isFile()) {
     * logger.trace("Element in year {}, month {}, day {}, file {}", year,
     * month, day, filename);
     * String encodedFileRequest = encrypt(
     * (year + "/" + month + "/" + day + "/" + filename).getBytes(),
     * (String) (getThing().getConfiguration().get("password")));
     * String fileurl = "http://".concat(
     * ipaddress.concat("/file?request=").concat(encodedFileRequest));
     * res = getData(fileurl, "GET", null);
     * if (res != null) {
     * FileWriter datafile = new FileWriter(fullfilename);
     * logger.debug("Writing data to {}", fullfilename);
     * for (String line : res.getBody().split("\\n")) {
     * String decodedText = decrypt(line.getBytes(),
     * (String) (getThing().getConfiguration()
     * .get("password")));
     * datafile.append(decodedText);
     * }
     * datafile.close();
     * }
     * } else {
     * logger.debug("Skipping file {} as it exists already", fullfilename);
     * }
     * }
     * }
     * }
     * }
     * }
     * } else {
     * logger.error("No data received; answer cannot be interpreted. Answer={}", answer);
     * }
     *
     * }
     * }
     * } catch (
     *
     * Exception e) {
     * System.out.println("Error in getDataFiles(): " + e.toString());
     * }
     * }
     *
     * private File createDataDir(String dir) {
     * File f = new File(System.getProperty("user.dir") + dir);
     * if (f.exists()) {
     * if (!f.isDirectory()) {
     * logger.error(
     * "Cannot create or use directory {} as there is already a file (and not a directory) with that name",
     * dir);
     * }
     * } else {
     * if (!f.mkdir()) {
     * logger.error("Cannot create or use directory {} as the directory could not be created.", dir);
     * }
     * }
     * return f;
     * }
     *
     * private File createDataFile(String dir) {
     * File f = new File(System.getProperty("user.dir") + dir);
     * if (f.exists()) {
     * if (!f.isFile()) {
     * logger.error(
     * "Cannot create or use file {} as there is already such an entry, but not a file (maybe a directory) with that name"
     * ,
     * dir);
     * } else if (!f.canWrite()) {
     * logger.error("Cannot write file {}", dir);
     * }
     * } else {
     * try {
     * if (!f.createNewFile()) {
     * logger.error("Cannot create new file {}.", dir);
     * }
     * } catch (IOException exc) {
     * logger.error("Error while creating data file {}: ", dir, exc);
     * }
     * }
     * return f;
     * }
     *
     */
};
