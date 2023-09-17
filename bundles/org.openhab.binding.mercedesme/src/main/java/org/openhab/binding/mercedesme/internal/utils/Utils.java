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
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.ChargeProgramConfigure.ChargeProgram;
import org.openhab.binding.mercedesme.internal.proto.VehicleCommands.TemperatureConfigure.TemperaturePoint.Zone;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.ChargeProgramParameters;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.ChargeProgramsValue;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.TemperaturePointsValue;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VEPUpdate;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VehicleAttributeStatus;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.WeeklyProfileValue;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.protobuf.Descriptors.FieldDescriptor;

/**
 * The {@link Utils} class defines an HTTP Server for authentication callbacks
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    private static final List<Integer> PORTS = new ArrayList<>();
    private static int port = 8090;

    public static final Gson GSON = new Gson();
    public static final Map<String, Integer> ZONE_HASHMAP = new HashMap<String, Integer>();
    public static final Map<String, Integer> PROGRAM_HASHMAP = new HashMap<String, Integer>();
    public static TimeZoneProvider timeZoneProvider = new TimeZoneProvider() {

        @Override
        public ZoneId getTimeZone() {
            return ZoneId.systemDefault();
        }
    };

    public static DateTimeType getDateTimeType(long ms) {
        Instant timestamp = Instant.ofEpochMilli(ms);
        ZonedDateTime zdt = timestamp.atZone(timeZoneProvider.getTimeZone());
        return DateTimeType.valueOf(zdt.toLocalDateTime().toString());
    }

    /**
     * Get free port without other Thread interference
     *
     * @return
     */
    public static synchronized int getFreePort() {
        while (PORTS.contains(port)) {
            port++;
        }
        PORTS.add(port);
        return port;
    }

    public static synchronized void addPort(int portNr) {
        if (PORTS.contains(portNr)) {
            LOGGER.warn("Port {} already occupied", portNr);
        }
        PORTS.add(portNr);
    }

    public static synchronized void removePort(int portNr) {
        PORTS.remove(Integer.valueOf(portNr));
    }

    public static String getCallbackIP() throws SocketException {
        // https://stackoverflow.com/questions/901755/how-to-get-the-ip-of-the-computer-on-linux-through-java
        // https://stackoverflow.com/questions/1062041/ip-address-not-obtained-in-java
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
                .hasMoreElements();) {
            NetworkInterface iface = ifaces.nextElement();
            try {
                if (!iface.isLoopback()) {
                    if (iface.isUp()) {
                        for (Enumeration<InetAddress> addresses = iface.getInetAddresses(); addresses
                                .hasMoreElements();) {
                            InetAddress address = addresses.nextElement();
                            if (address instanceof Inet4Address) {
                                return address.getHostAddress();
                            }
                        }
                    }
                }
            } catch (SocketException se) {
                // Calling one network interface failed - continue searching
                LOGGER.trace("Network {} failed {}", iface.getName(), se.getMessage());
            }
        }
        throw new SocketException("IP address not detected");
    }

    public static String getCallbackAddress(String callbackIP, int callbackPort) {
        return "http://" + callbackIP + Constants.COLON + callbackPort + Constants.CALLBACK_ENDPOINT;
    }

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

    public static String getRisSDKVersion(String region) {
        switch (region) {
            case Constants.REGION_CHINA:
                return Constants.RIS_SDK_VERSION_CN;
            default:
                return Constants.RIS_SDK_VERSION;
        }
    }

    public static String getAuthURL(String region) {
        return getRestAPIServer(region) + "/v1/login";
    }

    public static String getTokenUrl(String region) {
        return getLoginServer(region) + "/as/token.oauth2";
    }

    public static String getLoginAppId(String region) {
        switch (region) {
            case Constants.REGION_CHINA:
                return Constants.LOGIN_APP_ID_CN;
            default:
                return Constants.LOGIN_APP_ID;
        }
    }

    /** Read the object from Base64 string. */
    public static Object fromString(String s) {
        try {
            byte[] data = Base64.getDecoder().decode(s);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            Object o = ois.readObject();
            ois.close();
            return o;
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Error converting string to token {}", e.getMessage());
        }
        return Constants.NOT_SET;
    }

    /** Write the object to a Base64 string. */
    public static String toString(Serializable o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            LOGGER.warn("Error converting token to string {}", e.getMessage());
        }
        return Constants.NOT_SET;
    }

    public static String proto2Json(VEPUpdate update) {
        JSONObject protoJson = new JSONObject();
        Map<String, VehicleAttributeStatus> m = update.getAttributesMap();
        m.forEach((key, value) -> {
            Map<FieldDescriptor, Object> attMap = value.getAllFields();
            JSONObject attributesJson = getJsonObject(attMap);
            protoJson.put(key, attributesJson);

            if (value.hasTemperaturePointsValue()) {
                TemperaturePointsValue tpv = value.getTemperaturePointsValue();
                JSONArray tmpPoints = new JSONArray();
                List<VehicleEvents.TemperaturePoint> temperaturePointsList = tpv.getTemperaturePointsList();
                temperaturePointsList.forEach(point -> {
                    JSONObject tmpPoint = getJsonObject(point.getAllFields());
                    tmpPoints.put(tmpPoint);
                });
                JSONObject points = new JSONObject();
                points.put("emperature_points", tmpPoints);
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
                JSONObject weeklyProfiles = getJsonObject(wpv.getAllFields());
                attributesJson.put("weekly_profile_value", weeklyProfiles);
            }
        });
        return protoJson.toString();
    }

    public static JSONObject getJsonObject(Map<FieldDescriptor, Object> attMap) {
        JSONObject joa = new JSONObject();
        attMap.forEach((aKey, aValue) -> {
            String[] bKey = aKey.toString().split("\\.");
            if (bKey.length > 1) {
                joa.put(bKey[bKey.length - 1], aValue);
            } else {
                joa.put(bKey[0], aValue);
            }
        });
        return joa;
    }

    public static int getZoneNumber(String zone) {
        if (ZONE_HASHMAP.isEmpty()) {
            Zone[] zones = Zone.values();
            for (int i = 0; i < zones.length - 1; i++) {
                ZONE_HASHMAP.put(zones[i].name(), zones[i].getNumber());
            }
        }
        if (ZONE_HASHMAP.containsKey(zone)) {
            return ZONE_HASHMAP.get(zone);
        }
        return -1;
    }

    public static int getChargeProgramNumber(String program) {
        if (PROGRAM_HASHMAP.isEmpty()) {
            ChargeProgram[] programs = ChargeProgram.values();
            for (int i = 0; i < programs.length - 1; i++) {
                PROGRAM_HASHMAP.put(programs[i].name(), programs[i].getNumber());
            }
        }
        if (PROGRAM_HASHMAP.containsKey(program)) {
            return PROGRAM_HASHMAP.get(program);
        }
        return -1;
    }

    public static String getDurationString(long durationMinutes) {
        long minutesPerDay = 24 * 60;
        long days = durationMinutes / minutesPerDay;
        long remain = durationMinutes - (days * minutesPerDay);
        long hours = remain / 60;
        remain = remain - (hours * 60);
        if (days == 0 && hours == 0) {
            return remain + "m";
        } else if (days > 0) {
            return days + "d " + hours + "h " + remain + "m";
        } else {
            return hours + "h " + remain + "m";
        }
    }

    public static boolean boolFromState(@Nullable State s) {
        if (s != null) {
            if (OnOffType.ON.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static int getInt(VehicleAttributeStatus value) {
        return Double.valueOf(getDouble(value)).intValue();
    }

    /**
     * Priority:
     * 1) get display value with converted values
     * 2) get double if available
     * 3) get in if available
     *
     * @param value
     * @return
     */
    public static double getDouble(VehicleAttributeStatus value) {
        double ret = -1;
        if (!isNil(value)) {
            if (value.getDisplayValue() != null) {
                if (value.getDisplayValue().strip().length() > 0) {
                    try {
                        ret = Double.valueOf(value.getDisplayValue());
                        return ret;
                    } catch (NumberFormatException nfe) {
                        LOGGER.info("Cannot transform {} / {} Display Value {} into Double", value.getStringValue(),
                                value.toString(), value.getDisplayValue());
                    }
                }
            }
            if (value.hasDoubleValue()) {
                return value.getDoubleValue();
            } else if (value.hasIntValue()) {
                return value.getIntValue();
            }
        }
        return ret;
    }

    public static boolean isNil(@Nullable VehicleAttributeStatus value) {
        if (value != null) {
            if (value.hasNilValue()) {
                return value.getNilValue();
            }
        }
        return false;
    }
}
