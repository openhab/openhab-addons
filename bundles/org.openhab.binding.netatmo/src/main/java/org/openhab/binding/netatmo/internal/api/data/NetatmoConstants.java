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
package org.openhab.binding.netatmo.internal.api.data;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;
import static org.openhab.core.library.CoreItemFactory.*;
import static org.openhab.core.library.unit.MetricPrefix.*;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.annotations.SerializedName;

/**
 * This class holds various definitions and settings provided by the Netatmo
 * API documentation
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoConstants {
    public static class Measure {
        public final double minValue;
        public final double maxValue;
        public final int scale;
        public final Unit<?> unit;

        private Measure(double minValue, double maxValue, double precision, Unit<?> unit) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.unit = unit;
            String[] splitter = Double.toString(precision).split("\\.");
            if (splitter.length > 1) {
                int dec = Integer.parseInt(splitter[1]);
                this.scale = dec > 0 ? Integer.toString(dec).length() : 0;
            } else {
                this.scale = 0;
            }
        }
    }

    public static class MeasureChannelDetails {
        private static final StateDescriptionFragmentBuilder BUILDER = StateDescriptionFragmentBuilder.create();
        public final URI configURI;
        public final String itemType;
        public final StateDescriptionFragment stateDescriptionFragment;

        private MeasureChannelDetails(String measureType, String itemType, String pattern) {
            this.configURI = URI.create(String.join(":", BINDING_ID, measureType, "config"));
            this.itemType = itemType;
            this.stateDescriptionFragment = BUILDER.withReadOnly(true).withPattern(pattern).build();
        }
    }

    public enum MeasureClass {
        INSIDE_TEMPERATURE(0, 50, 0.3, SIUnits.CELSIUS, "temp", "measure", true),
        OUTSIDE_TEMPERATURE(-40, 65, 0.3, SIUnits.CELSIUS, "temp", "measure", true),
        HEAT_INDEX(-40, 65, 1, SIUnits.CELSIUS, "", "", false),
        PRESSURE(260, 1260, 0.1, HECTO(SIUnits.PASCAL), "pressure", "measure", true),
        CO2(0, 5000, 50, Units.PARTS_PER_MILLION, "co2", "measure", true),
        NOISE(35, 120, 1, Units.DECIBEL, "noise", "measure", true),
        RAIN_QUANTITY(0, Double.MAX_VALUE, 0.1, MILLI(SIUnits.METRE), "sum_rain", "sum_rain", false),
        RAIN_INTENSITY(0, 150, 0.1, Units.MILLIMETRE_PER_HOUR, "", "", false),
        WIND_SPEED(0, 160, 1.8, SIUnits.KILOMETRE_PER_HOUR, "", "", false),
        WIND_ANGLE(0, 360, 5, Units.DEGREE_ANGLE, "", "", false),
        HUMIDITY(0, 100, 3, Units.PERCENT, "hum", "measure", true);

        public static final EnumSet<MeasureClass> AS_SET = EnumSet.allOf(MeasureClass.class);

        public final Measure measureDefinition;
        public final String apiDescriptor;
        public final Map<String, MeasureChannelDetails> channels = new HashMap<>(2);

        MeasureClass(double min, double max, double precision, Unit<?> unit, String apiDescriptor, String confFragment,
                boolean canScale) {
            this.measureDefinition = new Measure(min, max, precision, unit);
            this.apiDescriptor = apiDescriptor;
            if (!apiDescriptor.isBlank()) {
                String dimension = UnitUtils.getDimensionName(unit);

                channels.put(String.join("-", apiDescriptor, "measurement"),
                        new MeasureChannelDetails(confFragment, String.join(":", NUMBER, dimension),
                                "%%.%df %s".formatted(measureDefinition.scale, UnitUtils.UNIT_PLACEHOLDER)));
                if (canScale) {
                    channels.put(String.join("-", apiDescriptor, GROUP_TIMESTAMP), new MeasureChannelDetails(
                            GROUP_TIMESTAMP, DATETIME, "@text/extensible-channel-type.timestamp.pattern"));
                }
            }
        }
    }

    // Content types
    public static final String CONTENT_APP_JSON = "application/json;charset=utf-8";
    public static final String CONTENT_APP_FORM = "application/x-www-form-urlencoded;charset=UTF-8";

    // Netatmo API urls
    public static final String URL_API = "https://api.netatmo.com/";
    public static final String PATH_OAUTH = "oauth2";
    public static final String SUB_PATH_TOKEN = "token";
    public static final String SUB_PATH_AUTHORIZE = "authorize";
    public static final String PATH_API = "api";
    public static final String PATH_COMMAND = "command";
    public static final String PATH_STATE = "setstate";
    public static final String SUB_PATH_PERSON_AWAY = "setpersonsaway";
    public static final String SUB_PATH_PERSON_HOME = "setpersonshome";
    public static final String SUB_PATH_HOMES_DATA = "homesdata";
    public static final String SUB_PATH_ADD_WEBHOOK = "addwebhook";
    public static final String SUB_PATH_DROP_WEBHOOK = "dropwebhook";
    public static final String SUB_PATH_SET_ROOM_THERMPOINT = "setroomthermpoint";
    public static final String SUB_PATH_SET_THERM_MODE = "setthermmode";
    public static final String SUB_PATH_SWITCH_SCHEDULE = "switchschedule";
    public static final String SUB_PATH_GET_STATION = "getstationsdata";
    public static final String SUB_PATH_GET_MEASURE = "getmeasure";
    public static final String SUB_PATH_HOMESTATUS = "homestatus";
    public static final String SUB_PATH_HOMECOACH = "gethomecoachsdata";
    public static final String SUB_PATH_GET_EVENTS = "getevents";
    public static final String SUB_PATH_PING = "ping";
    public static final String SUB_PATH_CHANGESTATUS = "changestatus";
    public static final String PARAM_DEVICE_ID = "device_id";
    public static final String PARAM_MODULE_ID = "module_id";
    public static final String PARAM_HOME_ID = "home_id";
    public static final String PARAM_ROOM_ID = "room_id";
    public static final String PARAM_PERSON_ID = "person_id";
    public static final String PARAM_EVENT_ID = "event_id";
    public static final String PARAM_SCHEDULE_ID = "schedule_id";
    public static final String PARAM_OFFSET = "offset";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_GATEWAY_TYPE = "gateway_types";
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_URL = "url";
    public static final String PARAM_FAVORITES = "get_favorites";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_DEVICES_TYPE = "device_types";

    // Payloads
    public static final String PAYLOAD_FLOODLIGHT = "{\"home\": {\"id\":\"%s\",\"modules\": [ {\"id\":\"%s\",\"floodlight\":\"%s\"} ]}}";
    public static final String PAYLOAD_SIREN_PRESENCE = "{\"home\": {\"id\":\"%s\",\"modules\": [ {\"id\":\"%s\",\"siren_status\":\"%s\"} ]}}";
    public static final String PAYLOAD_PERSON_AWAY = "{\"home_id\":\"%s\",\"person_id\":\"%s\"}";
    public static final String PAYLOAD_PERSON_HOME = "{\"home_id\":\"%s\",\"person_ids\":[\"%s\"]}";

    // Autentication process params
    public static final String PARAM_ERROR = "error";

    // Global variables
    public static final int THERM_MAX_SETPOINT = 30;

    // Token scopes
    public enum Scope {
        @SerializedName("read_station")
        READ_STATION,
        @SerializedName("read_thermostat")
        READ_THERMOSTAT,
        @SerializedName("write_thermostat")
        WRITE_THERMOSTAT,
        @SerializedName("read_camera")
        READ_CAMERA,
        @SerializedName("write_camera")
        WRITE_CAMERA,
        @SerializedName("access_camera")
        ACCESS_CAMERA,
        @SerializedName("read_presence")
        READ_PRESENCE,
        @SerializedName("write_presence")
        WRITE_PRESENCE,
        @SerializedName("access_presence")
        ACCESS_PRESENCE,
        @SerializedName("read_smokedetector")
        READ_SMOKEDETECTOR,
        @SerializedName("read_homecoach")
        READ_HOMECOACH,
        @SerializedName("read_doorbell")
        READ_DOORBELL,
        @SerializedName("write_doorbell")
        WRITE_DOORBELL,
        @SerializedName("access_doorbell")
        ACCESS_DOORBELL,
        @SerializedName("read_carbonmonoxidedetector")
        READ_CARBONMONOXIDEDETECTOR,
        UNKNOWN
    }

    // Topology Changes
    public enum TopologyChange {
        @SerializedName("home_owner_added")
        HOME_OWNER_ADDED,
        @SerializedName("device_associated_to_user")
        DEVICE_ASSOCIATED_TO_USER,
        @SerializedName("device_associated_to_home")
        DEVICE_ASSOCIATED_TO_HOME,
        @SerializedName("device_updated")
        DEVICE_UPDATED,
        @SerializedName("device_associated_to_room")
        DEVICE_ASSOCIATED_TO_ROOM,
        @SerializedName("room_created")
        ROOM_CREATED,
        UNKNOWN
    }

    private static final Scope[] SMOKE_SCOPES = { Scope.READ_SMOKEDETECTOR };
    private static final Scope[] CARBON_MONOXIDE_SCOPES = { Scope.READ_CARBONMONOXIDEDETECTOR };
    private static final Scope[] AIR_CARE_SCOPES = { Scope.READ_HOMECOACH };
    private static final Scope[] WEATHER_SCOPES = { Scope.READ_STATION };
    private static final Scope[] THERMOSTAT_SCOPES = { Scope.READ_THERMOSTAT, Scope.WRITE_THERMOSTAT };
    private static final Scope[] WELCOME_SCOPES = { Scope.READ_CAMERA, Scope.WRITE_CAMERA, Scope.ACCESS_CAMERA };
    private static final Scope[] DOORBELL_SCOPES = { Scope.READ_DOORBELL, Scope.WRITE_DOORBELL, Scope.ACCESS_DOORBELL };
    private static final Scope[] PRESENCE_SCOPES = { Scope.READ_PRESENCE, Scope.WRITE_PRESENCE, Scope.ACCESS_PRESENCE };

    public enum FeatureArea {
        AIR_CARE(AIR_CARE_SCOPES),
        WEATHER(WEATHER_SCOPES),
        ENERGY(THERMOSTAT_SCOPES),
        SECURITY(WELCOME_SCOPES, PRESENCE_SCOPES, SMOKE_SCOPES, DOORBELL_SCOPES, CARBON_MONOXIDE_SCOPES),
        NONE();

        public static String ALL_SCOPES = EnumSet.allOf(FeatureArea.class).stream().map(fa -> fa.scopes)
                .flatMap(Set::stream).map(s -> s.name().toLowerCase()).collect(Collectors.joining(" "));

        public final Set<Scope> scopes;

        FeatureArea(Scope[]... scopeArrays) {
            this.scopes = Stream.of(scopeArrays).flatMap(Arrays::stream).collect(Collectors.toSet());
        }
    }

    // Radio signal quality thresholds
    static final int[] WIFI_SIGNAL_LEVELS = new int[] { 99, 84, 69, 54 }; // Resp : bad, average, good, full
    static final int[] RADIO_SIGNAL_LEVELS = new int[] { 90, 80, 70, 60 }; // Resp : low, medium, high, full

    // Thermostat definitions
    public enum SetpointMode {
        @SerializedName("program")
        PROGRAM("program"),
        @SerializedName("away")
        AWAY("away"),
        @SerializedName("hg")
        FROST_GUARD("hg"),
        @SerializedName("manual")
        MANUAL("manual"),
        @SerializedName("off")
        OFF("off"),
        @SerializedName("max")
        MAX("max"),
        @SerializedName("schedule")
        SCHEDULE("schedule"),
        HOME("home"),
        UNKNOWN("");

        public final String apiDescriptor;

        SetpointMode(String descriptor) {
            this.apiDescriptor = descriptor;
        }
    }

    public enum ThermostatZoneType {
        @SerializedName("0")
        DAY("0"),
        @SerializedName("1")
        NIGHT("1"),
        @SerializedName("2")
        AWAY("2"),
        @SerializedName("3")
        FROST_GUARD("3"),
        @SerializedName("4")
        CUSTOM("4"),
        @SerializedName("5")
        ECO("5"),
        @SerializedName("8")
        COMFORT("8"),
        UNKNOWN("");

        public final String zoneId;

        private ThermostatZoneType(String id) {
            zoneId = id;
        }
    }

    public enum FloodLightMode {
        @SerializedName("on")
        ON,
        @SerializedName("off")
        OFF,
        @SerializedName("auto")
        AUTO,
        UNKNOWN
    }

    public enum EventCategory {
        @SerializedName("human")
        HUMAN,
        @SerializedName("animal")
        ANIMAL,
        @SerializedName("vehicle")
        VEHICLE,
        UNKNOWN
    }

    public enum TrendDescription {
        @SerializedName("up")
        UP,
        @SerializedName("stable")
        STABLE,
        @SerializedName("down")
        DOWN,
        UNKNOWN
    }

    public enum VideoStatus {
        @SerializedName("recording")
        RECORDING,
        @SerializedName("available")
        AVAILABLE,
        @SerializedName("deleted")
        DELETED,
        UNKNOWN
    }

    public enum SdCardStatus {
        @SerializedName("1")
        SD_CARD_MISSING,
        @SerializedName("2")
        SD_CARD_INSERTED,
        @SerializedName("3")
        SD_CARD_FORMATTED,
        @SerializedName("4")
        SD_CARD_WORKING,
        @SerializedName("5")
        SD_CARD_DEFECTIVE,
        @SerializedName("6")
        SD_CARD_INCOMPATIBLE_SPEED,
        @SerializedName("7")
        SD_CARD_INSUFFICIENT_SPACE,
        UNKNOWN
    }

    public enum AlimentationStatus {
        @SerializedName("1")
        ALIM_INCORRECT_POWER,
        @SerializedName("2")
        ALIM_CORRECT_POWER,
        UNKNOWN
    }

    public enum SirenStatus {
        SOUND,
        NO_SOUND,
        UNKNOWN;

        public static SirenStatus get(String value) {
            try {
                return valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    public enum BatteryState {
        @SerializedName("full")
        FULL(100),
        @SerializedName("high")
        HIGH(80),
        @SerializedName("medium")
        MEDIUM(50),
        @SerializedName("low")
        LOW(15),
        UNKNOWN(-1);

        public final int level;

        BatteryState(int i) {
            this.level = i;
        }
    }

    public enum ServiceError {
        @SerializedName("99")
        UNKNOWN,
        @SerializedName("-2")
        UNKNOWN_ERROR_IN_OAUTH,
        @SerializedName("-1")
        GRANT_IS_INVALID,
        @SerializedName("1")
        ACCESS_TOKEN_MISSING,
        @SerializedName("2")
        INVALID_TOKEN_MISSING,
        @SerializedName("3")
        ACCESS_TOKEN_EXPIRED,
        @SerializedName("5")
        APPLICATION_DEACTIVATED,
        @SerializedName("7")
        NOTHING_TO_MODIFY,
        @SerializedName("9")
        DEVICE_NOT_FOUND,
        @SerializedName("10")
        MISSING_ARGUMENTS,
        @SerializedName("13")
        OPERATION_FORBIDDEN,
        @SerializedName("19")
        IP_NOT_FOUND,
        @SerializedName("21")
        INVALID_ARGUMENT,
        @SerializedName("22")
        APPLICATION_NOT_FOUND,
        @SerializedName("23")
        USER_NOT_FOUND,
        @SerializedName("25")
        INVALID_DATE,
        @SerializedName("26")
        MAXIMUM_USAGE_REACHED,
        @SerializedName("30")
        INVALID_REFRESH_TOKEN,
        @SerializedName("31")
        METHOD_NOT_FOUND,
        @SerializedName("35")
        UNABLE_TO_EXECUTE,
        @SerializedName("36")
        PROHIBITED_STRING,
        @SerializedName("37")
        NO_MORE_SPACE_AVAILABLE_ON_THE_CAMERA,
        @SerializedName("40")
        JSON_GIVEN_HAS_AN_INVALID_ENCODING,
        @SerializedName("41")
        DEVICE_IS_UNREACHABLE
    }

    public enum HomeStatusError {
        @SerializedName("1")
        UNKNOWN_ERROR("homestatus-unknown-error"),
        @SerializedName("2")
        INTERNAL_ERROR("homestatus-internal-error"),
        @SerializedName("3")
        PARSER_ERROR("homestatus-parser-error"),
        @SerializedName("4")
        COMMAND_UNKNOWN_NODE_MODULE_ERROR("homestatus-command-unknown"),
        @SerializedName("5")
        COMMAND_INVALID_PARAMS("homestatus-invalid-params"),
        @SerializedName("6")
        UNREACHABLE("device-not-connected"),
        UNKNOWN("deserialization-unknown");

        // Associated error message that can be found in properties files
        public final String message;

        HomeStatusError(String message) {
            this.message = message;
        }
    }
}
