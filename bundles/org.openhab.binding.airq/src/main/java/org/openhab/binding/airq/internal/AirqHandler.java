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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.openhab.binding.airq.internal.action.AirqActions;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
    private static final DateTimeFormatter CLOCK_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    // device defaults, written when a retained flat Switch channel is switched ON
    private static final int AUTO_UPDATE_DEFAULT_DAYS = 90;
    private static final int AVERAGING_DEFAULT_VALUES = 30;

    private record ChannelMapping(String dataKey, String channelId, String type, @Nullable Unit<?> unit,
            String groupId) {
        ChannelMapping(String dataKey, String channelId, String type) {
            this(dataKey, channelId, type, null, "measurements");
        }

        ChannelMapping(String dataKey, String channelId, String type, Unit<?> unit) {
            this(dataKey, channelId, type, unit, "measurements");
        }

        ChannelMapping(String dataKey, String channelId, Unit<?> unit) {
            this(dataKey, channelId, "measurement", unit, "advanced-measurements");
        }
    }

    private static final Map<String, String> CHANNEL_IDS = Map.ofEntries(
            Map.entry("fineDustCnt00_3_maxerr", "fine-dust-cnt00-3-maxerr"),
            Map.entry("fineDustCnt00_5_maxerr", "fine-dust-cnt00-5-maxerr"),
            Map.entry("fineDustCnt01_maxerr", "fine-dust-cnt01-maxerr"),
            Map.entry("fineDustCnt02_5_maxerr", "fine-dust-cnt02-5-maxerr"),
            Map.entry("fineDustCnt05_maxerr", "fine-dust-cnt05-maxerr"),
            Map.entry("fineDustCnt10_maxerr", "fine-dust-cnt10-maxerr"), Map.entry("co_maxerr", "co-maxerr"),
            Map.entry("co2_maxerr", "co2-maxerr"), Map.entry("dewpt_maxerr", "dewpt-maxerr"),
            Map.entry("h2s_maxerr", "h2s-maxerr"), Map.entry("humidityRelative_maxerr", "humidity-relative-maxerr"),
            Map.entry("humidityAbsolute_maxerr", "humidity-absolute-maxerr"), Map.entry("no2_maxerr", "no2-maxerr"),
            Map.entry("o3_maxerr", "o3-maxerr"), Map.entry("o2_maxerr", "o2-maxerr"),
            Map.entry("fineDustConc01_maxerr", "fine-dust-conc01-maxerr"),
            Map.entry("fineDustConc02_5_maxerr", "fine-dust-conc02-5-maxerr"),
            Map.entry("fineDustConc10_maxerr", "fine-dust-conc10-maxerr"),
            Map.entry("pressure_maxerr", "pressure-maxerr"), Map.entry("so2_maxerr", "so2-maxerr"),
            Map.entry("sound_maxerr", "sound-maxerr"), Map.entry("temperature_maxerr", "temperature-maxerr"),
            Map.entry("tvoc_maxerr", "tvoc-maxerr"), Map.entry("virus_free_maxerr", "virus-free-maxerr"),
            Map.entry("mold_free_maxerr", "mold-free-maxerr"), Map.entry("radon_maxerr", "radon-maxerr"),
            Map.entry("avgFineDustSize", "avg-fine-dust-size"), Map.entry("fineDustCnt00_3", "fine-dust-cnt00-3"),
            Map.entry("fineDustCnt00_5", "fine-dust-cnt00-5"), Map.entry("fineDustCnt01", "fine-dust-cnt01"),
            Map.entry("fineDustCnt02_5", "fine-dust-cnt02-5"), Map.entry("fineDustCnt05", "fine-dust-cnt05"),
            Map.entry("fineDustCnt10", "fine-dust-cnt10"), Map.entry("dCO2dt", "d-co2dt"), Map.entry("dHdt", "d-hdt"),
            Map.entry("doorEvent", "door-event"), Map.entry("healthIndex", "health-index"),
            Map.entry("humidityRelative", "humidity-relative"), Map.entry("humidityAbsolute", "humidity-absolute"),
            Map.entry("measureTime", "measure-time"), Map.entry("performanceIndex", "performance-index"),
            Map.entry("fineDustConc01", "fine-dust-conc01"), Map.entry("fineDustConc02_5", "fine-dust-conc02-5"),
            Map.entry("fineDustConc10", "fine-dust-conc10"), Map.entry("virus_free", "virus-free"),
            Map.entry("mold_free", "mold-free"), Map.entry("wifiInfo", "wifi-info"),
            Map.entry("timeServer", "time-server"), Map.entry("nightModeStartDay", "night-mode-start-day"),
            Map.entry("nightModeStartNight", "night-mode-start-night"),
            Map.entry("nightModeBrightnessDay", "night-mode-brightness-day"),
            Map.entry("nightModeBrightnessNight", "night-mode-brightness-night"),
            Map.entry("nightModeFanNightOff", "night-mode-fan-night-off"),
            Map.entry("nightModeWifiNightOff", "night-mode-wifi-night-off"),
            Map.entry("nightModeActivated", "night-mode-activated"),
            Map.entry("nightModeAlarmNightOff", "night-mode-alarm-night-off"), Map.entry("deviceName", "device-name"),
            Map.entry("roomType", "room-type"), Map.entry("logLevel", "log-level"),
            Map.entry("fireAlarm", "fire-alarm"), Map.entry("wlanConfigGateway", "wlan-config-gateway"),
            Map.entry("wlanConfigMac", "wlan-config-mac"), Map.entry("wlanConfigSsid", "wlan-config-ssid"),
            Map.entry("wlanConfigIPAddress", "wlan-config-ip-address"),
            Map.entry("wlanConfigNetMask", "wlan-config-net-mask"), Map.entry("wlanConfigBssid", "wlan-config-bssid"),
            Map.entry("cloudUpload", "cloud-upload"), Map.entry("averagingRhythm", "averaging-rhythm"),
            Map.entry("powerFreqSuppression", "power-freq-suppression"),
            Map.entry("autoDriftCompensation", "auto-drift-compensation"), Map.entry("autoUpdate", "auto-update"),
            Map.entry("advancedDataProcessing", "advanced-data-processing"), Map.entry("ppm_and_ppb", "ppm-and-ppb"),
            Map.entry("gasAlarm", "gas-alarm"), Map.entry("soundPressure", "sound-info"),
            Map.entry("alarmForwarding", "alarm-forwarding"), Map.entry("userCalib", "user-calib"),
            Map.entry("initialCalFinished", "initial-cal-finished"), Map.entry("warmupPhase", "warmup-phase"));

    private static final Map<String, String> LEGACY_CHANNEL_IDS = CHANNEL_IDS.entrySet().stream()
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, Map.Entry::getKey));

    private static final List<ChannelMapping> MEASUREMENTS = List.of(
            new ChannelMapping("cnt0_3", "fineDustCnt00_3", "pair"),
            new ChannelMapping("cnt0_5", "fineDustCnt00_5", "pair"),
            new ChannelMapping("cnt1", "fineDustCnt01", "pair"),
            new ChannelMapping("cnt2_5", "fineDustCnt02_5", "pair"),
            new ChannelMapping("cnt5", "fineDustCnt05", "pair"), new ChannelMapping("cnt10", "fineDustCnt10", "pair"),
            // datenlesen.md and measurement.md: the default units, used unless the device is configured otherwise
            new ChannelMapping("co", "co", "pair", AirqUnits.MILLIGRAM_PER_CUBICMETRE),
            new ChannelMapping("co2", "co2", "pair", Units.PARTS_PER_MILLION),
            new ChannelMapping("dewpt", "dewpt", "pair", SIUnits.CELSIUS),
            new ChannelMapping("h2s", "h2s", "pair", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("humidity", "humidityRelative", "pair", Units.PERCENT),
            new ChannelMapping("humidity_abs", "humidityAbsolute", "pair", AirqUnits.GRAM_PER_CUBICMETRE),
            new ChannelMapping("no2", "no2", "pair", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("o3", "o3", "pair", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("oxygen", "o2", "pair", Units.PERCENT),
            new ChannelMapping("pm1", "fineDustConc01", "pair", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("pm2_5", "fineDustConc02_5", "pair", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("pm10", "fineDustConc10", "pair", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("pressure", "pressure", "pair", AirqUnits.HECTOPASCAL),
            new ChannelMapping("radon", "radon", "pair", Units.BECQUEREL_PER_CUBIC_METRE),
            new ChannelMapping("so2", "so2", "pair", Units.MICROGRAM_PER_CUBICMETRE),
            new ChannelMapping("sound", "sound", "pair", Units.DECIBEL),
            new ChannelMapping("temperature", "temperature", "pair", SIUnits.CELSIUS),
            new ChannelMapping("Status", "status", "string", null, "general"),
            new ChannelMapping("TypPS", "avgFineDustSize", "quantity", AirqUnits.MICROMETRE),
            new ChannelMapping("dCO2dt", "dCO2dt", "number"), new ChannelMapping("dHdt", "dHdt", "number"),
            new ChannelMapping("door_event", "doorEvent", "number"),
            new ChannelMapping("health", "healthIndex", "index"), new ChannelMapping("health", "health", "number"),
            new ChannelMapping("measuretime", "measureTime", "numberMilliseconds"),
            new ChannelMapping("performance", "performanceIndex", "index"),
            new ChannelMapping("performance", "performance", "number"),
            new ChannelMapping("timestamp", "timestamp", "datetime"),
            new ChannelMapping("uptime", "uptime", "numberTimePeriod", null, "general"),
            new ChannelMapping("tvoc", "tvoc", "pair", Units.PARTS_PER_BILLION),
            new ChannelMapping("virus", "virus_free", "pair", Units.PERCENT),
            new ChannelMapping("mold", "mold_free", "pair", Units.PERCENT),
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
            new ChannelMapping("WLANssid", "ssid", "arr"), new ChannelMapping("WifiInfo", "wifiInfo", "boolean"),
            new ChannelMapping("TimeServer", "timeServer", "string"), new ChannelMapping("geopos", "location", "coord"),
            new ChannelMapping("NightMode", "", "nightmode"), new ChannelMapping("daytime", "daytime", "boolean"),
            new ChannelMapping("devicename", "deviceName", "string"),
            new ChannelMapping("RoomType", "roomType", "string"), new ChannelMapping("logging", "logLevel", "string"),
            new ChannelMapping("FireAlarm", "fireAlarm", "boolean"),
            new ChannelMapping("air-Q-Hardware-Version", "hardwareVersion", "property"),
            new ChannelMapping("WLAN config", "", "wlan"), new ChannelMapping("cloudUpload", "cloudUpload", "boolean"),
            new ChannelMapping("SecondsMeasurementDelay", "averagingRhythm", "numberTimePeriod"),
            new ChannelMapping("Rejection", "powerFreqSuppression", "string"),
            new ChannelMapping("air-Q-Software-Version", "softwareVersion", "property"),
            new ChannelMapping("sensors", "sensorList", "proparr"),
            new ChannelMapping("AutoDriftCompensation", "autoDriftCompensation", "boolean"),
            new ChannelMapping("AutoUpdate", "autoUpdate", "numberDays"),
            new ChannelMapping("AdvancedDataProcessing", "advancedDataProcessing", "boolean"),
            new ChannelMapping("Industry", "Industry", "property"),
            new ChannelMapping("ppm&ppb", "ppm_and_ppb", "boolean"),
            new ChannelMapping("GasAlarm", "gasAlarm", "boolean"), new ChannelMapping("id", "id", "property"),
            new ChannelMapping("SoundInfo", "soundPressure", "soundInfo"),
            new ChannelMapping("AlarmForwarding", "alarmForwarding", "boolean"),
            new ChannelMapping("usercalib", "userCalib", "calib"),
            new ChannelMapping("InitialCalFinished", "initialCalFinished", "boolean"),
            new ChannelMapping("Averaging", "averaging", "number"),
            new ChannelMapping("SensorInfo", "sensorInfo", "property"),
            new ChannelMapping("warmup-phase", "warmupPhase", "boolean"));
    // the units of the deprecated "ppm&ppb" setting, which the device uses for keys without a "units" entry
    private static final Map<String, Unit<?>> PPM_AND_PPB_UNITS = Map.of("co", Units.PARTS_PER_MILLION, "no2",
            Units.PARTS_PER_BILLION, "o3", Units.PARTS_PER_BILLION, "so2", Units.PARTS_PER_BILLION);
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

    // read from GET /config: the configured unit and the molar mass in g/mol per measurement key
    private volatile Map<String, Unit<?>> configuredUnits = Map.of();
    private volatile Map<String, Double> molarMasses = Map.of();
    // keys configured with a unit the binding doesn't know, their readings can't be interpreted
    private volatile Set<String> unknownUnits = Set.of();
    private volatile boolean ppmAndPpb;
    private volatile boolean configurationRequested;

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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = legacyChannelId(channelUID.getIdWithoutGroup());
        if ("averagingRhythm".equals(channelId) || "nightModeStartDay".equals(channelId)
                || "nightModeStartNight".equals(channelId)) {
            handleTimeCommand(channelId, command);
            return;
        }
        if ("autoUpdate".equals(channelId) || "averaging".equals(channelId)) {
            handleIntegerSettingCommand(channelId, command);
            return;
        }
        if ("nightModeBrightnessDay".equals(channelId) || "nightModeBrightnessNight".equals(channelId)) {
            BigDecimal brightness;
            if (command instanceof QuantityType<?> quantity) {
                QuantityType<?> converted = quantity.toUnit(Units.ONE);
                if (converted == null) {
                    logger.debug("Ignoring incompatible brightness unit for channel {}", channelId);
                    return;
                }
                brightness = converted.toBigDecimal();
            } else if (command instanceof DecimalType decimal) {
                brightness = decimal.toBigDecimal();
            } else if (command instanceof StringType string) {
                try {
                    brightness = new BigDecimal(string.toString());
                } catch (NumberFormatException e) {
                    logger.debug("Ignoring malformed brightness command for channel {}", channelId);
                    return;
                }
            } else {
                logger.debug("Ignoring non-numeric brightness command for channel {}", channelId);
                return;
            }
            // the device accepts whole numbers from 0 (LEDs off) to 10
            int level;
            try {
                level = brightness.intValueExact();
            } catch (ArithmeticException e) {
                logger.debug("Ignoring fractional brightness command for channel {}", channelId);
                return;
            }
            if (level < 0 || level > 10) {
                logger.debug("Ignoring out-of-range brightness command for channel {}", channelId);
                return;
            }
            JsonObject nightMode = new JsonObject();
            nightMode.addProperty("nightModeBrightnessDay".equals(channelId) ? "BrightnessDay" : "BrightnessNight",
                    level);
            JsonObject settings = new JsonObject();
            settings.add("NightMode", nightMode);
            changeSettings(settings);
            return;
        }
        boolean acceptedCommand = switch (channelId) {
            case "wifi", "wifiInfo", "fireAlarm", "cloudUpload", "autoDriftCompensation", "advancedDataProcessing",
                    "gasAlarm", "alarmForwarding", "ppm_and_ppb", "nightModeFanNightOff", "nightModeWifiNightOff",
                    "nightModeActivated", "nightModeAlarmNightOff", "warmupPhase" ->
                command instanceof OnOffType;
            case "timeServer", "roomType", "logLevel", "powerFreqSuppression", "deviceName" ->
                command instanceof StringType;
            case "location" -> command instanceof PointType;
            default -> false;
        };
        if (acceptedCommand) {
            JsonObject newobj = new JsonObject();
            JsonObject subjson = new JsonObject();
            switch (channelId) {
                case "wifi":
                    // we do not allow to switch off Wifi because otherwise we can't connect to the air-Q device anymore
                    break;
                case "warmupPhase":
                    // a one-shot command that aborts a running warm-up phase, a warm-up can't be started
                    if (command == OnOffType.OFF) {
                        newobj.addProperty("warmup-phase", false);
                        changeSettings(newobj);
                    }
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
                case "advancedDataProcessing":
                    newobj.addProperty("AdvancedDataProcessing", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "gasAlarm":
                    newobj.addProperty("GasAlarm", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "alarmForwarding":
                    newobj.addProperty("AlarmForwarding", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "ppm_and_ppb":
                    newobj.addProperty("ppm&ppb", command == OnOffType.ON);
                    changeSettings(newobj);
                    break;
                case "nightModeFanNightOff":
                    subjson.addProperty("FanNightOff", command == OnOffType.ON);
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "nightModeWifiNightOff":
                    subjson.addProperty("WifiNightOff", command == OnOffType.ON);
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "nightModeActivated":
                    subjson.addProperty("Activated", command == OnOffType.ON);
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "nightModeAlarmNightOff":
                    subjson.addProperty("AlarmNightOff", command == OnOffType.ON);
                    newobj.add("NightMode", subjson);
                    changeSettings(newobj);
                    break;
                case "timeServer":
                    newobj.addProperty("TimeServer", command.toString());
                    changeSettings(newobj);
                    break;
                case "location":
                    PointType pt = (PointType) command;
                    subjson.addProperty("lat", pt.getLatitude());
                    subjson.addProperty("long", pt.getLongitude());
                    newobj.add("geopos", subjson);
                    changeSettings(newobj);
                    break;
                case "roomType":
                    newobj.addProperty("RoomType", command.toString());
                    changeSettings(newobj);
                    break;
                case "deviceName":
                    newobj.addProperty("devicename", command.toString());
                    changeSettings(newobj);
                    break;
                case "logLevel":
                    String ll = command.toString();
                    if ("Error".equals(ll) || "Warning".equals(ll) || "Info".equals(ll)) {
                        newobj.addProperty("logging", ll);
                        changeSettings(newobj);
                    } else {
                        logger.warn(
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct setting for the log level (only Error, Warning or Info)",
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
                                "air-Q - airqHandler - handleCommand(): {} should be set to {} but it isn't a correct setting for the power frequency suppression (only 50Hz, 60Hz or 50Hz+60Hz)",
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

        if (getThing().getChannels().stream().anyMatch(channel -> channel.getUID().getGroupId() == null)) {
            logger.info(
                    "Thing {} has deprecated flat air-Q channels. They remain supported for compatibility; migrate Item links to grouped channels as described in the binding README.",
                    getThing().getUID());
        }

        // the first data poll reads the configuration first, it may belong to a different device after a change
        configuredUnits = Map.of();
        unknownUnits = Set.of();
        molarMasses = Map.of();
        ppmAndPpb = false;
        configurationRequested = false;
        pollingJob = scheduler.scheduleWithFixedDelay(this::pollData, 0, POLLING_PERIOD_DATA_MSEC,
                TimeUnit.MILLISECONDS);
        getConfigDataJob = scheduler.scheduleWithFixedDelay(this::getConfigData, POLLING_PERIOD_CONFIG,
                POLLING_PERIOD_CONFIG, TimeUnit.MINUTES);
    }

    private void handleTimeCommand(String channelId, Command command) {
        boolean measurementDelay = "averagingRhythm".equals(channelId);
        Unit<?> unit = measurementDelay ? Units.SECOND : Units.MINUTE;
        BigDecimal value;
        if (!measurementDelay && command instanceof StringType clockCommand) {
            try {
                value = BigDecimal
                        .valueOf(LocalTime.parse(clockCommand.toString(), CLOCK_TIME_FORMAT).toSecondOfDay() / 60);
            } catch (DateTimeParseException e) {
                logger.debug("Ignoring malformed clock time command for channel {}", channelId);
                return;
            }
        } else if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> converted = quantity.toUnit(unit);
            if (converted == null) {
                logger.debug("Ignoring incompatible time unit for channel {}", channelId);
                return;
            }
            value = converted.toBigDecimal();
        } else if (command instanceof DecimalType decimal) {
            value = decimal.toBigDecimal();
        } else {
            logger.debug("Ignoring non-numeric time command for channel {}", channelId);
            return;
        }
        int timeValue;
        try {
            timeValue = value.intValueExact();
        } catch (ArithmeticException e) {
            logger.debug("Ignoring non-integer or out-of-range time command for channel {}", channelId);
            return;
        }
        if (timeValue < 0 || (!measurementDelay && timeValue >= 24 * 60)) {
            logger.debug("Ignoring out-of-range time command for channel {}", channelId);
            return;
        }
        JsonObject settings = new JsonObject();
        if (measurementDelay) {
            settings.addProperty("SecondsMeasurementDelay", timeValue);
        } else {
            JsonObject nightMode = new JsonObject();
            String key = "nightModeStartDay".equals(channelId) ? "StartDay" : "StartNight";
            nightMode.addProperty(key, LocalTime.ofSecondOfDay(timeValue * 60).format(CLOCK_TIME_FORMAT));
            settings.add("NightMode", nightMode);
        }
        changeSettings(settings);
    }

    /**
     * Handles the integer settings AutoUpdate (interval in days, 0 = off) and Averaging (number of averaged values,
     * 1 = off). ON and OFF from retained flat Switch channels map to the device default and to "off".
     */
    private void handleIntegerSettingCommand(String channelId, Command command) {
        boolean autoUpdate = "autoUpdate".equals(channelId);
        int minimum = autoUpdate ? 0 : 1;
        BigDecimal value;
        if (command instanceof OnOffType) {
            int defaultValue = autoUpdate ? AUTO_UPDATE_DEFAULT_DAYS : AVERAGING_DEFAULT_VALUES;
            value = BigDecimal.valueOf(command == OnOffType.ON ? defaultValue : minimum);
        } else if (command instanceof QuantityType<?> quantity) {
            QuantityType<?> converted = quantity.toUnit(autoUpdate ? Units.DAY : Units.ONE);
            if (converted == null) {
                logger.debug("Ignoring incompatible unit for channel {}", channelId);
                return;
            }
            value = converted.toBigDecimal();
        } else if (command instanceof DecimalType decimal) {
            value = decimal.toBigDecimal();
        } else {
            logger.debug("Ignoring non-numeric command for channel {}", channelId);
            return;
        }
        int intValue;
        try {
            intValue = value.intValueExact();
        } catch (ArithmeticException e) {
            logger.debug("Ignoring non-integer or out-of-range command for channel {}", channelId);
            return;
        }
        if (intValue < minimum) {
            logger.debug("Ignoring out-of-range command for channel {}", channelId);
            return;
        }
        JsonObject settings = new JsonObject();
        settings.addProperty(autoUpdate ? "AutoUpdate" : "Averaging", intValue);
        changeSettings(settings);
    }

    // AES decoding based on this tutorial: https://www.javainterviewpoint.com/aes-256-encryption-and-decryption/
    public String decrypt(byte[] base64text, String password) throws AirqException {
        String content = "";
        byte[] encodedtextwithIV;
        try {
            encodedtextwithIV = Base64.getDecoder().decode(base64text);
        } catch (IllegalArgumentException e) {
            throw new AirqException("Device response is not valid base64", e);
        }
        if (encodedtextwithIV.length <= 16) {
            throw new AirqException("Device response is too short to be decrypted");
        }
        byte[] ciphertext = Arrays.copyOfRange(encodedtextwithIV, 16, encodedtextwithIV.length);
        SecretKey seckey = new SecretKeySpec(aesKey(password), "AES");
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

    // AES-256 key: the UTF-8 encoded password, padded with '0' bytes or truncated to 32 bytes
    static byte[] aesKey(String password) {
        byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] key = Arrays.copyOf(passwordBytes, 32);
        Arrays.fill(key, Math.min(passwordBytes.length, 32), 32, (byte) '0');
        return key;
    }

    public String encrypt(byte[] toencode, String password) throws AirqException {
        byte[] iv = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        SecretKey seckey = new SecretKeySpec(aesKey(password), "AES");
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
            return new String(encodedcontent, StandardCharsets.US_ASCII);
        } catch (BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException exc) {
            throw new AirqException("Failed to encrypt data", exc);
        }
    }

    // reads url with GET and returns the decrypted content of the response
    protected String getDecryptedContentString(String url) throws AirqException, InterruptedException {
        Result res = getData(url, "GET", null);
        return decryptResponse(res.getBody());
    }

    // the device answers with {"content": "<encrypted>"} or, in some cases, with the bare encrypted string
    private String decryptResponse(String body) throws AirqException {
        String text = body.trim();
        if (text.isEmpty()) {
            throw new AirqEmptyResonseException();
        }

        String encrypted;
        if (text.startsWith("{")) {
            JsonElement content = parseObject(text).get("content");
            if (content == null || !content.isJsonPrimitive()) {
                throw new AirqException("Device response has no encrypted content");
            }
            encrypted = content.getAsString();
        } else if (text.startsWith("\"")) {
            JsonElement content = parseElement(text);
            if (!content.isJsonPrimitive()) {
                throw new AirqException("Device response has no encrypted content");
            }
            encrypted = content.getAsString();
        } else {
            encrypted = text;
        }
        return decrypt(encrypted.getBytes(StandardCharsets.UTF_8), config.password);
    }

    private JsonObject parseObject(String json) throws AirqException {
        JsonElement element = parseElement(json);
        if (!element.isJsonObject()) {
            throw new AirqException("Device response is not a JSON object");
        }
        return element.getAsJsonObject();
    }

    private JsonElement parseElement(String json) throws AirqException {
        JsonElement element;
        try {
            // Gson code based on https://riptutorial.com/de/gson
            element = gson.fromJson(json, JsonElement.class);
        } catch (JsonSyntaxException e) {
            logger.debug("Failed to parse response from device: {}", e.getMessage());
            throw new AirqException("Syntax error while parsing response from device");
        }
        if (element == null) {
            throw new AirqEmptyResonseException();
        }
        return element;
    }

    // calls the networking job and in addition does additional tests for online/offline management
    protected Result getData(String address, String requestMethod, @Nullable String body)
            throws AirqException, InterruptedException {
        int timeout = 10;
        Request request = httpClient.newRequest(address).timeout(timeout, TimeUnit.SECONDS).method(requestMethod);
        if (body != null) {
            request = request.content(new StringContentProvider(body)).header(HttpHeader.CONTENT_TYPE,
                    "application/x-www-form-urlencoded");
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
        if (!configurationRequested) {
            // the units of the measurements are part of the configuration, which is then read every minute
            configurationRequested = true;
            getConfigData();
        }
        try {
            String url = "http://" + config.ipAddress + "/data";
            String jsonAnswer = getDecryptedContentString(url);

            JsonObject decObj = parseObject(jsonAnswer);
            try {
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
        } catch (RuntimeException e) {
            // an uncaught exception would cancel the scheduled polling job for good
            logger.debug("Failed to process measurement data", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to process measurement data: " + e.getMessage());
        }
    }

    public void getConfigData() {
        try {
            String url = "http://" + config.ipAddress + "/config";
            JsonObject decObj = parseObject(getDecryptedContentString(url));
            readUnits(decObj);
            processMappings(decObj, CONFIGURATION, "general");
        } catch (AirqException | JsonSyntaxException e) {
            logger.warn("Failed to retrieve configuration: {}", e.getMessage());
        } catch (InterruptedException e) {
            // nothing
        } catch (RuntimeException e) {
            // an uncaught exception would cancel the scheduled configuration job for good
            logger.warn("Failed to process configuration: {}", e.getMessage(), e);
        }
    }

    private void processMappings(JsonObject data, List<ChannelMapping> mappings, @Nullable String groupOverride) {
        for (ChannelMapping mapping : mappings) {
            String groupId = groupOverride == null ? mapping.groupId() : groupOverride;
            String channelId = groupId + "#" + mapping.channelId();
            Unit<?> unit = mapping.unit();
            if (unit == null) {
                processType(data, mapping.dataKey(), channelId, mapping.type());
            } else if ("pair".equals(mapping.type())) {
                processPair(data, mapping.dataKey(), channelId, unit);
            } else if ("quantity".equals(mapping.type())) {
                JsonElement reading = data.get(mapping.dataKey());
                updateMappedState(channelId,
                        reading != null && isNumeric(reading) ? new QuantityType<>(reading.getAsBigDecimal(), unit)
                                : UnDefType.UNDEF);
            } else {
                processMeasurement(data, mapping.dataKey(), channelId, unit);
            }
        }
    }

    // a [value, error] pair of a standard sensor, a malformed pair is a syntax error of the response
    private void processPair(JsonObject data, String key, String channel, Unit<?> unit) {
        JsonElement reading = data.get(key);
        String errorChannel = errorChannel(channel, "maxerr", "_maxerr");
        if (reading == null || reading.isJsonNull()) {
            updateMappedState(channel, UnDefType.UNDEF);
            updateMappedState(errorChannel, UnDefType.UNDEF);
        } else if (reading.isJsonArray() && reading.getAsJsonArray().size() == 2
                && isNumeric(reading.getAsJsonArray().get(0)) && isNumeric(reading.getAsJsonArray().get(1))) {
            JsonArray pair = reading.getAsJsonArray();
            updateMeasurement(data, key, channel, errorChannel, unit, pair.get(0).getAsBigDecimal(),
                    pair.get(1).getAsBigDecimal());
        } else {
            throw new JsonSyntaxException("Failed to parse pair: " + reading);
        }
    }

    private void processMeasurement(JsonObject data, String key, String channel, Unit<?> unit) {
        JsonElement reading = data.get(key);
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
            updateMeasurement(data, key, channel, errorChannel, unit, pair.get(0).getAsBigDecimal(),
                    pair.get(1).getAsBigDecimal());
        } else if (isNumeric(reading)) {
            updateMeasurement(data, key, channel, errorChannel, unit, reading.getAsBigDecimal(), null);
        } else {
            updateMappedState(channel, UnDefType.UNDEF);
            updateMappedState(errorChannel, UnDefType.UNDEF);
            logger.debug("Ignoring malformed measurement for channel {}", channel);
        }
    }

    /**
     * Updates a measurement and its error bar, which the device reports in the same unit (datenlesen.md). Both are
     * converted from the unit the device is configured to to the unit of the channel. Error bars are differences, so
     * temperature errors are converted without the zero-point offset and reported in kelvin.
     */
    private void updateMeasurement(JsonObject data, String key, String channel, String errorChannel,
            Unit<?> channelUnit, BigDecimal value, @Nullable BigDecimal error) {
        Unit<?> deviceUnit = deviceUnit(key, channelUnit);
        if (deviceUnit == null) {
            updateMappedState(channel, UnDefType.UNDEF);
            updateMappedState(errorChannel, UnDefType.UNDEF);
            return;
        }
        updateMappedState(channel, toChannelUnit(data, key, new QuantityType<>(value, deviceUnit), channelUnit, false));
        if (error == null) {
            updateMappedState(errorChannel, UnDefType.UNDEF);
        } else {
            Unit<?> errorUnit = channelUnit.isCompatible(Units.KELVIN) ? Units.KELVIN : channelUnit;
            updateMappedState(errorChannel,
                    toChannelUnit(data, key, new QuantityType<>(error, deviceUnit), errorUnit, true));
        }
    }

    // measurement.md: the "units" setting, else the deprecated "ppm&ppb" setting, else the sensor's default unit
    private @Nullable Unit<?> deviceUnit(String key, Unit<?> defaultUnit) {
        if (unknownUnits.contains(key)) {
            return null;
        }
        Unit<?> configured = configuredUnits.get(key);
        if (configured != null) {
            return configured;
        }
        Unit<?> legacy = PPM_AND_PPB_UNITS.get(key);
        return ppmAndPpb && legacy != null ? legacy : defaultUnit;
    }

    private State toChannelUnit(JsonObject data, String key, QuantityType<?> quantity, Unit<?> unit,
            boolean difference) {
        QuantityType<?> converted = difference ? toUnitRelative(quantity, unit) : quantity.toUnit(unit);
        if (converted != null) {
            return converted;
        }
        // volume fraction and mass density, converted with the temperature and pressure of the same measurement
        Double molarMass = molarMasses.get(key);
        BigDecimal temperature = reading(data, "temperature", SIUnits.CELSIUS);
        BigDecimal pressure = reading(data, "pressure", AirqUnits.HECTOPASCAL);
        if (molarMass != null && temperature != null && pressure != null) {
            converted = AirqUnits.convertConcentration(quantity, unit, molarMass, temperature.doubleValue(),
                    pressure.doubleValue());
        }
        if (converted == null) {
            logger.debug("Cannot convert {} from {} to {}", key, quantity.getUnit(), unit);
            return UnDefType.UNDEF;
        }
        return converted;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static @Nullable QuantityType<?> toUnitRelative(QuantityType quantity, Unit unit) {
        return quantity.toUnitRelative(unit);
    }

    private @Nullable BigDecimal reading(JsonObject data, String key, Unit<?> unit) {
        JsonElement reading = data.get(key);
        if (reading != null && reading.isJsonArray() && !reading.getAsJsonArray().isEmpty()) {
            reading = reading.getAsJsonArray().get(0);
        }
        Unit<?> deviceUnit = deviceUnit(key, unit);
        if (reading == null || !isNumeric(reading) || deviceUnit == null) {
            return null;
        }
        QuantityType<?> converted = new QuantityType<>(reading.getAsBigDecimal(), deviceUnit).toUnit(unit);
        return converted == null ? null : converted.toBigDecimal();
    }

    // reads the configured units, the deprecated ppm&ppb setting, and the molar masses of the gas sensors
    private void readUnits(JsonObject configuration) {
        Map<String, Unit<?>> units = new HashMap<>();
        Set<String> unknown = new HashSet<>();
        JsonElement unitsElement = configuration.get("units");
        if (unitsElement != null && unitsElement.isJsonObject()) {
            for (Entry<String, JsonElement> entry : unitsElement.getAsJsonObject().entrySet()) {
                Unit<?> unit = entry.getValue().isJsonPrimitive() ? AirqUnits.deviceUnit(entry.getValue().getAsString())
                        : null;
                if (unit != null) {
                    units.put(entry.getKey(), unit);
                } else {
                    unknown.add(entry.getKey());
                    // the configuration is read every minute, warn once
                    if (!unknownUnits.contains(entry.getKey())) {
                        logger.warn("Unknown unit {} configured for {}, its measurements are undefined",
                                entry.getValue(), entry.getKey());
                    }
                }
            }
        }
        Map<String, Double> masses = new HashMap<>();
        JsonElement sensorInfo = configuration.get("SensorInfo");
        if (sensorInfo != null && sensorInfo.isJsonObject()) {
            for (Entry<String, JsonElement> sensor : sensorInfo.getAsJsonObject().entrySet()) {
                JsonElement molarMass = sensor.getValue().isJsonObject()
                        ? sensor.getValue().getAsJsonObject().get("Molar Mass")
                        : null;
                if (molarMass != null && isNumeric(molarMass)) {
                    masses.put(sensor.getKey(), molarMass.getAsDouble());
                }
            }
        }
        JsonElement legacy = configuration.get("ppm&ppb");
        ppmAndPpb = legacy != null && legacy.isJsonPrimitive()
                && ("true".equals(legacy.getAsString()) || "1".equals(legacy.getAsString()));
        configuredUnits = Map.copyOf(units);
        unknownUnits = Set.copyOf(unknown);
        molarMasses = Map.copyOf(masses);
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
                case "soundInfo":
                    JsonElement soundInfo = dec.get(airqName);
                    if (soundInfo.isJsonObject()) {
                        updateMappedState(channelName, new StringType(soundInfoSummary(soundInfo.getAsJsonObject())));
                    } else {
                        updateMappedState(channelName, UnDefType.UNDEF);
                        logger.debug("Ignoring non-object value for channel {}", channelName);
                    }
                    break;
                case "numberTimePeriod":
                    updateMappedState(channelName,
                            new QuantityType<>(dec.get(airqName).getAsBigDecimal(), Units.SECOND));
                    break;
                case "numberDays":
                    updateMappedState(channelName, new QuantityType<>(dec.get(airqName).getAsBigDecimal(), Units.DAY));
                    break;
                case "numberMilliseconds":
                    updateMappedState(channelName,
                            new QuantityType<>(dec.get(airqName).getAsBigDecimal(), MetricPrefix.MILLI(Units.SECOND)));
                    break;
                case "clockTime":
                    try {
                        LocalTime time = LocalTime.parse(dec.get(airqName).getAsString(), CLOCK_TIME_FORMAT);
                        updateMappedState(channelName, new QuantityType<>(time.toSecondOfDay() / 60, Units.MINUTE));
                    } catch (DateTimeParseException e) {
                        updateMappedState(channelName, UnDefType.UNDEF);
                        logger.debug("Ignoring malformed clock time for channel {}", channelName);
                    }
                    break;
                case "pair":
                    ResultPair pair = new ResultPair(dec.get(airqName));
                    updateMappedState(channelName, new DecimalType(pair.getValue()));
                    updateMappedState(errorChannel(channelName, "maxerr", "_maxerr"),
                            new DecimalType(pair.getMaxdev()));
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
                                "clockTime");
                        processType(jsonDaynightdata, "StartNight", nestedChannel(channelName, "nightModeStartNight"),
                                "clockTime");
                        processType(jsonDaynightdata, "BrightnessDay",
                                nestedChannel(channelName, "nightModeBrightnessDay"), "number");
                        processType(jsonDaynightdata, "BrightnessNight",
                                nestedChannel(channelName, "nightModeBrightnessNight"), "number");
                        processType(jsonDaynightdata, "FanNightOff", nestedChannel(channelName, "nightModeFanNightOff"),
                                "boolean");
                        processType(jsonDaynightdata, "WifiNightOff",
                                nestedChannel(channelName, "nightModeWifiNightOff"), "boolean");
                        processType(jsonDaynightdata, "AlarmNightOff",
                                nestedChannel(channelName, "nightModeAlarmNightOff"), "boolean");
                        // deviceconfig.md: firmware before 1.80 has no Activated key and evaluates the night mode
                        if (jsonDaynightdata.has("Activated")) {
                            processType(jsonDaynightdata, "Activated", nestedChannel(channelName, "nightModeActivated"),
                                    "boolean");
                        } else {
                            updateMappedState(nestedChannel(channelName, "nightModeActivated"), OnOffType.ON);
                        }
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
                    JsonElement jsonarr = dec.get(airqName);
                    if (jsonarr.isJsonArray()) {
                        // an empty list is published too, e.g. after all Wi-Fi networks were deleted
                        List<String> names = new ArrayList<>();
                        for (JsonElement el : jsonarr.getAsJsonArray()) {
                            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                                names.add(el.getAsString());
                            } else {
                                logger.debug("Ignoring non-string entry of {}", airqName);
                            }
                        }
                        updateMappedState(channelName, new StringType(String.join(", ", names)));
                    } else {
                        updateMappedState(channelName, UnDefType.UNDEF);
                        logger.debug("Ignoring non-array value of {}", airqName);
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

    private void updateMappedState(String channelId, State state) {
        if (!channelId.endsWith("#")) {
            String currentChannelId = groupedChannelId(channelId);
            updateState(currentChannelId, state);
            if (!currentChannelId.equals(channelId) && getThing().getChannel(channelId) != null) {
                updateState(channelId, state);
            }
            String legacyId = rawChannelId(channelId);
            Channel legacyChannel = getThing().getChannel(legacyId);
            if (!legacyId.equals(channelId) && legacyChannel != null) {
                State legacyState = state;
                if (state instanceof QuantityType<?> quantity) {
                    if ("String".equals(legacyChannel.getAcceptedItemType())
                            && ("nightModeStartDay".equals(legacyId) || "nightModeStartNight".equals(legacyId))) {
                        QuantityType<?> minutes = quantity.toUnit(Units.MINUTE);
                        if (minutes != null) {
                            legacyState = new StringType(
                                    LocalTime.ofSecondOfDay(minutes.intValue() * 60).format(CLOCK_TIME_FORMAT));
                        }
                    } else if ("autoUpdate".equals(legacyId) && "Switch".equals(legacyChannel.getAcceptedItemType())) {
                        legacyState = OnOffType.from(quantity.intValue() > 0);
                    } else if ("Number".equals(legacyChannel.getAcceptedItemType()) || legacyId.endsWith("_maxerr")) {
                        // plain Number and Number:Dimensionless error channels get the number in the channel's unit
                        legacyState = new DecimalType(quantity.toBigDecimal());
                    }
                } else if (state instanceof DecimalType decimal && "averaging".equals(legacyId)
                        && "Switch".equals(legacyChannel.getAcceptedItemType())) {
                    legacyState = OnOffType.from(decimal.intValue() > 1);
                } else if (state instanceof StringType string && "soundPressure".equals(legacyId)
                        && "Switch".equals(legacyChannel.getAcceptedItemType())) {
                    legacyState = OnOffType.from(!string.toString().isEmpty());
                }
                updateState(legacyId, legacyState);
            }
        }
    }

    /**
     * Summarizes the acoustic signals {sensor: {threshold: {pass, freq, vol, length, repeat}}}, e.g.
     * "co2 1200 up: 900 Hz, 100 %, 2000 ms; co2 2000 down: 1200 Hz, 100 %, 2000 ms, repeat".
     */
    private static String soundInfoSummary(JsonObject soundInfo) {
        List<String> signals = new ArrayList<>();
        for (Entry<String, JsonElement> sensor : soundInfo.entrySet()) {
            if (!sensor.getValue().isJsonObject()) {
                continue;
            }
            for (Entry<String, JsonElement> threshold : sensor.getValue().getAsJsonObject().entrySet()) {
                if (!threshold.getValue().isJsonObject()) {
                    continue;
                }
                JsonObject signal = threshold.getValue().getAsJsonObject();
                StringBuilder text = new StringBuilder(sensor.getKey()).append(' ').append(threshold.getKey());
                if (signal.has("pass")) {
                    text.append(' ').append(signal.get("pass").getAsString());
                }
                List<String> details = new ArrayList<>();
                for (String[] field : new String[][] { { "freq", " Hz" }, { "vol", " %" }, { "length", " ms" } }) {
                    if (signal.has(field[0])) {
                        details.add(signal.get(field[0]).getAsString() + field[1]);
                    }
                }
                if (signal.has("repeat") && signal.get("repeat").getAsBoolean()) {
                    details.add("repeat");
                }
                if (!details.isEmpty()) {
                    text.append(": ").append(String.join(", ", details));
                }
                signals.add(text.toString());
            }
        }
        return String.join("; ", signals);
    }

    private static String groupedChannelId(String channelId) {
        int separator = channelId.indexOf('#');
        if (separator < 0) {
            return channelId;
        }
        String legacyId = rawChannelId(channelId);
        return channelId.substring(0, separator + 1) + CHANNEL_IDS.getOrDefault(legacyId, legacyId);
    }

    private static String legacyChannelId(String channelId) {
        String legacyId = LEGACY_CHANNEL_IDS.get(channelId);
        return legacyId != null ? legacyId : channelId;
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

    // returns whether the device accepted the change
    private boolean changeSettings(JsonObject jsonchange) {
        try {
            String jsoncmd = jsonchange.toString();
            Result res;
            String url = "http://" + config.ipAddress + "/config";
            String jsonbody = encrypt(jsoncmd.getBytes(StandardCharsets.UTF_8), config.password);
            String fullbody = "request=" + jsonbody;
            res = getData(url, "POST", fullbody);
            // only the keys are logged, the values may contain passwords
            Set<String> keys = jsonchange.keySet();
            if (res.getResponseCode() < 200 || res.getResponseCode() >= 300) {
                logger.warn("Failed to change settings {}: device answered with HTTP status {}", keys,
                        res.getResponseCode());
                return false;
            }

            // konfigschreiben.md: the response contains a success and error log for the requested changes
            String changeLog = decryptResponse(res.getBody());
            // the change log may repeat the Wi-Fi key
            String loggedChangeLog = jsonchange.has("WiFipass") ? "<omitted>" : changeLog;
            if (reportsError(changeLog)) {
                logger.warn("Device reported an error while changing settings {}: {}", keys, loggedChangeLog);
                return false;
            }
            logger.debug("Changed settings {}: {}", keys, loggedChangeLog);
            return true;
        } catch (AirqException e) {
            logger.warn("Failed to change settings {}: {}", jsonchange.keySet(), e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /**
     * Adds a Wi-Fi network to the device (deviceconfig.md). Wrong credentials together with a restart take the device
     * off the network, then it can only be set up again with the air-Q app or its hotspot.
     *
     * @param ssid the SSID, 1 to 32 bytes
     * @param password the Wi-Fi key, empty for an unsecured network
     * @param bssid the BSSID as 12 hex digits, separators are removed, or null or empty to omit it
     * @param hidden whether the network is hidden
     * @param restart whether the device restarts to connect, otherwise it remembers the network for later
     * @return whether the device accepted the network
     */
    public boolean setWifi(String ssid, String password, @Nullable String bssid, boolean hidden, boolean restart) {
        int ssidLength = ssid.getBytes(StandardCharsets.UTF_8).length;
        if (ssidLength < 1 || ssidLength > 32) {
            logger.warn("Not setting Wi-Fi: the SSID must have 1 to 32 bytes");
            return false;
        }
        String normalizedBssid = bssid == null ? "" : bssid.replaceAll("[:-]", "").toLowerCase(Locale.ROOT);
        if (!normalizedBssid.isEmpty() && !normalizedBssid.matches("[0-9a-f]{12}")) {
            logger.warn("Not setting Wi-Fi: the BSSID must have 12 hex digits");
            return false;
        }
        JsonObject settings = new JsonObject();
        settings.addProperty("WiFissid", ssid);
        settings.addProperty("WiFipass", password);
        if (!normalizedBssid.isEmpty()) {
            settings.addProperty("WiFibssid", normalizedBssid);
        }
        if (hidden) {
            settings.addProperty("WiFihidden", true);
        }
        if (restart) {
            settings.addProperty("reset", true);
        }
        return changeSettings(settings);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(AirqActions.class);
    }

    static boolean reportsError(String changeLog) {
        return changeLog.toLowerCase(Locale.ROOT).contains("error");
    }
}
