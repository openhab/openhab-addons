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
package org.openhab.binding.mercedesme.internal.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.MercedesMeHandlerFactory;
import org.openhab.binding.mercedesme.internal.server.AuthService;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.CommandOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.VehicleCommands.ChargeProgramConfigure.ChargeProgram;
import com.daimler.mbcarkit.proto.VehicleCommands.TemperatureConfigure.TemperaturePoint.Zone;
import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramParameters;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.VEPUpdate;
import com.daimler.mbcarkit.proto.VehicleEvents.VVRTimeProfile;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.WeeklyProfileValue;
import com.daimler.mbcarkit.proto.VehicleEvents.WeeklySetting;
import com.daimler.mbcarkit.proto.VehicleEvents.WeeklySettingsHeadUnitValue;
import com.google.gson.Gson;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * {@link Utils} provides several helper functions used from different classes
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final List<Integer> PORTS = new ArrayList<>();
    private static final List<CommandOption> FAHRENHEIT_COMMAND_OPTIONS = new ArrayList<>();
    private static final List<CommandOption> CELSIUS_COMMAND_OPTIONS = new ArrayList<>();

    private static final int R = 6371; // Radius of the earth
    private static int port = 8090;
    private static TimeZoneProvider timeZoneProvider = new TimeZoneProvider() {
        @Override
        public ZoneId getTimeZone() {
            return ZoneId.systemDefault();
        }
    };
    private static LocaleProvider localeProvider = new LocaleProvider() {

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    };
    public static final Gson GSON = new Gson();
    public static final Map<String, Integer> ZONE_HASHMAP = new HashMap<>();
    public static final Map<String, Integer> PROGRAM_HASHMAP = new HashMap<>();

    public static void initialize(TimeZoneProvider tzp, LocaleProvider lp) {
        timeZoneProvider = tzp;
        localeProvider = lp;
    }

    /**
     * Getting openHAB DateTimeType from given milliseconds according to configured TimeZone
     *
     * @param ms - milliseconds in epoch milli
     * @return openHAB DateTimeType according to configured TimeZone
     */
    public static DateTimeType getDateTimeType(long ms) {
        Instant timestamp = Instant.ofEpochMilli(ms);
        return new DateTimeType(timestamp.atZone(timeZoneProvider.getTimeZone()));
    }

    /**
     * Calculates the DateTime of charge end according to given Mercedes parameters
     *
     * @param ms - current timestamp in milliseconds in epoch milli
     * @param minutesAfterMidnight - minutes after midnight
     * @return calculates the start of day from given in ms plus minutes given in minutesAfterMidnight
     */
    public static DateTimeType getEndOfChargeTime(long ms, long minutesAfterMidnight) {
        // get today midnight
        Instant timestamp = Instant.ofEpochMilli(ms);
        ZonedDateTime zdt = timestamp.atZone(timeZoneProvider.getTimeZone());
        ZonedDateTime endTime = zdt.withHour(0).withMinute(0).withSecond(0).plusMinutes(minutesAfterMidnight);
        return new DateTimeType(endTime);
    }

    /**
     * Get free port without other Thread interference from other AccountHandlers
     *
     * @return number of free port
     */
    public static synchronized int getFreePort() {
        while (PORTS.contains(port)) {
            port++;
        }
        PORTS.add(port);
        return port;
    }

    /**
     * Register port for an AccountHandler
     */
    public static synchronized void addPort(int portNr) {
        if (PORTS.contains(portNr) && portNr != 99999) {
            LOGGER.warn("Port {} already occupied", portNr);
        }
        PORTS.add(portNr);
    }

    /**
     * Unregister port for an AccountHandler
     */
    public static synchronized void removePort(int portNr) {
        PORTS.remove(Integer.valueOf(portNr));
    }

    public static String getCallbackAddress(String callbackIP, int callbackPort) {
        return "http://" + callbackIP + Constants.COLON + callbackPort + Constants.CALLBACK_ENDPOINT;
    }

    /**
     * Calculate REST API server address according to region
     *
     * @param region - configured region
     * @return base REST server address
     */
    public static String getRestAPIServer(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.REST_API_BASE_PA;
            case Constants.REGION_CHINA:
                return Constants.REST_API_BASE_CN;
            case Constants.REGION_NORAM:
                return Constants.REST_API_BASE_NA;
            default:
                return Constants.REST_API_BASE;
        }
    }

    /**
     * Calculate Login API server address according to region
     *
     * @param region - configured region
     * @return base login server address
     */
    public static String getLoginServer(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.LOGIN_BASE_URI_PA;
            case Constants.REGION_CHINA:
                return Constants.LOGIN_BASE_URI_CN;
            case Constants.REGION_NORAM:
                return Constants.LOGIN_BASE_URI_NA;
            default:
                return Constants.LOGIN_BASE_URI;
        }
    }

    /**
     * Calculate websocket server address according to region
     *
     * @param region - configured region
     * @return websocket base server address
     */
    public static String getWebsocketServer(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.WEBSOCKET_API_BASE_PA;
            case Constants.REGION_CHINA:
                return Constants.WEBSOCKET_API_BASE_CN;
            case Constants.REGION_NORAM:
                return Constants.WEBSOCKET_API_BASE_PA;
            default:
                return Constants.WEBSOCKET_API_BASE;
        }
    }

    /**
     * Calculate application name according to region
     *
     * @param region - configured region
     * @return application name as String
     */
    public static String getApplication(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.X_APPLICATIONNAME_AP;
            case Constants.REGION_CHINA:
                return Constants.X_APPLICATIONNAME_CN;
            case Constants.REGION_NORAM:
                return Constants.X_APPLICATIONNAME_US;
            default:
                return Constants.X_APPLICATIONNAME;
        }
    }

    /**
     * Calculate application version according to region
     *
     * @param region - configured region
     * @return application version as String
     */
    public static String getRisApplicationVersion(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.RIS_APPLICATION_VERSION_PA;
            case Constants.REGION_CHINA:
                return Constants.RIS_APPLICATION_VERSION_CN;
            case Constants.REGION_NORAM:
                return Constants.RIS_APPLICATION_VERSION_NA;
            default:
                return Constants.RIS_APPLICATION_VERSION;
        }
    }

    /**
     * Calculate user agent according to region
     *
     * @param region - configured region
     * @return user agent as String
     */
    public static String getUserAgent(String region) {
        switch (region) {
            case Constants.REGION_APAC:
                return Constants.WEBSOCKET_USER_AGENT_PA;
            case Constants.REGION_CHINA:
                return Constants.WEBSOCKET_USER_AGENT_CN;
            default:
                return Constants.WEBSOCKET_USER_AGENT;
        }
    }

    /**
     * Calculate SDK version according to region
     *
     * @param region - configured region
     * @return SDK version as String
     */
    public static String getRisSDKVersion(String region) {
        switch (region) {
            case Constants.REGION_CHINA:
                return Constants.RIS_SDK_VERSION_CN;
            default:
                return Constants.RIS_SDK_VERSION;
        }
    }

    /**
     * Calculate authorization config URL as pre-configuration prior to authorization call
     *
     * @param region - configured region
     * @return authorization config URL as String
     */
    public static String getAuthConfigURL(String region) {
        return getRestAPIServer(region) + "/v1/config";
    }

    /**
     * Calculate login app id according to region
     *
     * @param region - configured region
     * @return login app id as String
     */
    public static String getLoginAppId(String region) {
        switch (region) {
            case Constants.REGION_CHINA:
                return Constants.LOGIN_APP_ID_CN;
            default:
                return Constants.LOGIN_APP_ID;
        }
    }

    /**
     * Calculate authorization URL for authorization call
     *
     * @param region - configured region
     * @return authorization URL as String
     */
    public static String getAuthURL(String region) {
        return getRestAPIServer(region) + "/v1/login";
    }

    /**
     * Calculate token URL for getting token
     *
     * @param region - configured region
     * @return token URL as String
     */
    public static String getTokenUrl(String region) {
        return getLoginServer(region) + "/as/token.oauth2";
    }

    /**
     * Decode String as Base64 from stored AccessTokenResponse
     *
     * @param token - Base64 String from storage
     * @return AccessTokenResponse decoded from String, invalid token otherwise
     */
    public static AccessTokenResponse fromString(String token) {
        try {
            byte[] data = Base64.getDecoder().decode(token);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return (AccessTokenResponse) o;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Error converting string to token {}", e.getMessage());
        }
        return AuthService.INVALID_TOKEN;
    }

    /**
     * Encode AccessTokenResponse as Base64 String for storage
     *
     * @param token - AccessTokenResponse to convert
     */
    public static String toString(AccessTokenResponse token) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(token);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            LOGGER.warn("Error converting token to string {}", e.getMessage());
        }
        return Constants.NOT_SET;
    }

    /**
     * Combine vehicle data maps which is needed for partial updates.
     * First take fullMap, then updates are taken from updateMap.
     *
     * @param fullMap - last present update of vehicle data
     * @param updateMap - updates to override
     * @return combined Map with updates taken into account
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Map combineMaps(Map fullMap, Map updateMap) {
        final Map combined = new TreeMap();
        fullMap.forEach((key, value) -> {
            combined.put(key, value);
        });
        updateMap.forEach((key, value) -> {
            combined.put(key, value);
        });
        return combined;
    }

    /**
     * Converts a protobuf update into JSON String
     *
     * @param protoUpdate - proto update
     * @param uid - thing type uid for identification
     * @return JSON as String
     */
    @SuppressWarnings({ "unused", "null" })
    public static String proto2Json(VEPUpdate protoUpdate, ThingTypeUID uid) {
        JSONObject protoJson = new JSONObject();
        Map<String, VehicleAttributeStatus> m = protoUpdate.getAttributesMap();
        m.forEach((key, value) -> {
            Map<FieldDescriptor, Object> attMap = value.getAllFields();
            JSONObject attributesJson = getJsonObject(attMap);

            if (value.hasTemperaturePointsValue()) {
                TemperaturePointsValue tpv = value.getTemperaturePointsValue();
                JSONArray tmpPoints = new JSONArray();
                List<VehicleEvents.TemperaturePoint> temperaturePointsList = tpv.getTemperaturePointsList();
                temperaturePointsList.forEach(point -> {
                    JSONObject tmpPoint = getJsonObject(point.getAllFields());
                    tmpPoints.put(tmpPoint);
                });
                JSONObject points = new JSONObject();
                points.put("temperature_points", tmpPoints);
                attributesJson.put("temperature_points_value", points);
            } else if (value.hasChargeProgramsValue()) {
                ChargeProgramsValue cpv = value.getChargeProgramsValue();
                JSONArray chargeProgramArray = new JSONArray();
                List<ChargeProgramParameters> l = cpv.getChargeProgramParametersList();
                l.forEach(cpp -> {
                    chargeProgramArray.put(getJsonObject(cpp.getAllFields()));
                });
                attributesJson.put("charge_programs_value", chargeProgramArray);
            } else if (value.hasWeeklyProfileValue()) {
                WeeklyProfileValue wpv = value.getWeeklyProfileValue();
                JSONObject weeklyProfiles = new JSONObject();
                List<VVRTimeProfile> timeProfilesList = wpv.getTimeProfilesList();
                timeProfilesList.forEach(timeProfileProto -> {
                    JSONObject timeProfileJson = new JSONObject();
                    JSONArray days = new JSONArray(timeProfileProto.getDaysList());
                    timeProfileJson.put("days", days);
                    timeProfileJson.put("hour", timeProfileProto.getHour());
                    timeProfileJson.put("minute", timeProfileProto.getMinute());
                    timeProfileJson.put("active", timeProfileProto.getActive());
                    timeProfileJson.put("applicationIdentifier", timeProfileProto.getApplicationIdentifier());
                    weeklyProfiles.put(Integer.toString(timeProfileProto.getIdentifier()), timeProfileJson);
                });
                attributesJson.put("weekly_profile_value", weeklyProfiles);
            } else if (value.hasWeeklySettingsHeadUnitValue()) {
                WeeklySettingsHeadUnitValue wshuv = value.getWeeklySettingsHeadUnitValue();
                List<WeeklySetting> valList = wshuv.getWeeklySettingsList();
                JSONArray settingsJsonArray = new JSONArray();
                valList.forEach(weeklySettingProto -> {
                    JSONObject settings = new JSONObject();
                    settings.put("day", weeklySettingProto.getDay());
                    settings.put("minutes_since_midnight", weeklySettingProto.getMinutesSinceMidnight());
                    settingsJsonArray.put(settings);
                });
                attributesJson.put("weekly_settings_head_unit_value", settingsJsonArray);
            }

            // check for errors - in fact JSONObject returns null in case of errors
            if (attributesJson.toString() == null) {
                LOGGER.trace("JSON conversion failed for Proto {}", key);
                attributesJson = new JSONObject();
                attributesJson.put(key, attMap.toString());
            }
            if (attributesJson.toString() == null) {
                LOGGER.trace("JSON conversion failed for Map {}", key);
                attributesJson = new JSONObject();
                attributesJson.put(key, "Not supported by binding");
            }

            // Anonymize position
            if ("positionLat".equals(key)) {
                attributesJson.put("double_value", 1.23);
            } else if ("positionLong".equals(key)) {
                attributesJson.put("double_value", 4.56);
            }
            protoJson.put(key, attributesJson);
        });
        // finally put binding version in
        JSONObject bindingInfo = new JSONObject();
        bindingInfo.put("version", Constants.BINDING_VERSION);
        bindingInfo.put("vehicle", uid.getAsString());
        bindingInfo.put("oh-bundle", MercedesMeHandlerFactory.getVersion());
        protoJson.put("bindingInfo", bindingInfo);
        return protoJson.toString();
    }

    /**
     * Converts a proto Map with FieldDescriptor into a JSON Object
     *
     * @param attMap - proto attributes Map
     * @return JSONObject with key value pairs
     */
    public static JSONObject getJsonObject(Map<FieldDescriptor, Object> attMap) {
        JSONObject joa = new JSONObject();
        attMap.forEach((aKey, aValue) -> {
            String[] bKey = aKey.toString().split("\\.");
            if (bKey.length > 1) {
                joa.put(bKey[bKey.length - 1], aValue);
            } else {
                joa.put(bKey[0], aValue.toString());
            }
        });
        return joa;
    }

    /**
     * Calculate zone number from 3rdparty generated proto files
     *
     * @param zone - zone definition as String
     * @return zone number for selection
     */
    public static int getZoneNumber(String zone) {
        if (ZONE_HASHMAP.isEmpty()) {
            Zone[] zones = Zone.values();
            for (int i = 0; i < zones.length - 1; i++) {
                ZONE_HASHMAP.put(zones[i].name(), zones[i].getNumber());
            }
        }
        Integer zoneNumber = ZONE_HASHMAP.get(zone);
        if (zoneNumber != null) {
            return zoneNumber;
        }
        return -1;
    }

    /**
     * Calculate charge program number from 3rdparty generated proto files
     *
     * @param program - charge program definition as String
     * @return charge program number for selection
     */
    public static int getChargeProgramNumber(String program) {
        if (PROGRAM_HASHMAP.isEmpty()) {
            ChargeProgram[] programs = ChargeProgram.values();
            for (int i = 0; i < programs.length - 1; i++) {
                PROGRAM_HASHMAP.put(programs[i].name(), programs[i].getNumber());
            }
        }
        Integer programNumber = PROGRAM_HASHMAP.get(program);
        if (programNumber != null) {
            return programNumber;
        }
        return -1;
    }

    /**
     * Calculate duration String from given minutes
     *
     * @param durationMinutes - duration in minutes
     * @return Sting in format days, hours and minutes
     */
    public static String getDurationString(long durationMinutes) {
        if (durationMinutes < 0) {
            return "-1";
        }
        Duration duration = Duration.ofMinutes(durationMinutes);
        if (duration.toDaysPart() > 0) {
            return String.format("%sd %sh %sm", duration.toDaysPart(), duration.toHoursPart(),
                    duration.toMinutesPart());
        } else if (duration.toHoursPart() > 0) {
            return String.format("%sh %sm", duration.toHoursPart(), duration.toMinutesPart());
        } else {
            return String.format("%sm", duration.toMinutesPart());
        }
    }

    /**
     * Get int from proto VehicleAttributeStatus
     *
     * @param value - proto value
     * @return value as int, -1 otherwise
     */
    public static int getInt(VehicleAttributeStatus value) {
        return Double.valueOf(getDouble(value)).intValue();
    }

    /**
     * Get double from proto VehicleAttributeStatus
     *
     * @param value - proto value
     * @return value as double, -1 otherwise
     */
    public static double getDouble(@Nullable VehicleAttributeStatus value) {
        double ret = -1;
        if (value != null) {
            if (!isNil(value)) {
                if (value.getDisplayValue() != null) {
                    if (value.getDisplayValue().strip().length() > 0) {
                        try {
                            return Double.valueOf(value.getDisplayValue());
                        } catch (NumberFormatException nfe) {
                            LOGGER.trace("Cannot transform Display Value {} into Double", value.getDisplayValue());
                        }
                    }
                }
                if (value.hasDoubleValue()) {
                    return value.getDoubleValue();
                } else if (value.hasIntValue()) {
                    return value.getIntValue();
                }
            }
        }
        return ret;
    }

    /**
     * Checks proto VehicleAttributeStatus is nil
     *
     * @param value - proto value
     * @return true if nil value is present, false otherwise
     */
    public static boolean isNil(@Nullable VehicleAttributeStatus value) {
        if (value != null) {
            if (value.hasNilValue()) {
                return value.getNilValue();
            }
        }
        return false;
    }

    /**
     * Get country code from configured LocaleProvider
     *
     * @return country code
     */
    public static String getCountry() {
        return localeProvider.getLocale().getCountry();
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Meters
     */
    public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        double height = el1 - el2;
        distance = Math.pow(distance, 2) + Math.pow(height, 2);
        return Math.sqrt(distance);
    }

    /**
     * Calculates a list of CommandOptions for temperature settings which are also available in the Mercedes Me app
     *
     * @param unit - unit of temperature
     * @return List of CommandOptions, empty if unit isn't supported
     */
    public static List<CommandOption> getTemperatureOptions(Unit<?> unit) {
        if (ImperialUnits.FAHRENHEIT.equals(unit)) {
            if (FAHRENHEIT_COMMAND_OPTIONS.isEmpty()) {
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("60 °F", "60 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("61 °F", "61 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("62 °F", "62 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("63 °F", "63 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("64 °F", "64 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("65 °F", "65 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("66 °F", "66 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("67 °F", "67 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("68 °F", "68 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("69 °F", "69 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("70 °F", "70 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("71 °F", "71 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("72 °F", "72 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("73 °F", "73 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("74 °F", "74 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("75 °F", "75 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("76 °F", "76 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("77 °F", "77 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("78 °F", "78 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("79 °F", "79 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("80 °F", "80 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("81 °F", "81 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("82 °F", "82 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("83 °F", "83 °F"));
                FAHRENHEIT_COMMAND_OPTIONS.add(new CommandOption("84 °F", "84 °F"));
            }
            return FAHRENHEIT_COMMAND_OPTIONS;
        } else if (SIUnits.CELSIUS.equals(unit)) {
            if (CELSIUS_COMMAND_OPTIONS.isEmpty()) {
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("16 °C", "16 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("16.5 °C", "16.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("17 °C", "17 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("17.5 °C", "17.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("18 °C", "18 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("18.5 °C", "18.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("19 °C", "19 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("19.5 °C", "19.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("20 °C", "20 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("20.5 °C", "20.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("21 °C", "21 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("21.5 °C", "21.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("22 °C", "22 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("22.5 °C", "22.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("23 °C", "23 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("23.5 °C", "23.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("24 °C", "24 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("24.5 °C", "24.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("25 °C", "25 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("25.5 °C", "25.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("26 °C", "26 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("26.5 °C", "26.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("27 °C", "27 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("27.5 °C", "27.5 °C"));
                CELSIUS_COMMAND_OPTIONS.add(new CommandOption("28 °C", "28 °C"));
            }
            return CELSIUS_COMMAND_OPTIONS;
        } else {
            return new ArrayList<CommandOption>();
        }
    }
}
