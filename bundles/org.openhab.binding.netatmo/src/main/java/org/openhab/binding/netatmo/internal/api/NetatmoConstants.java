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
package org.openhab.binding.netatmo.internal.api;

import static org.openhab.core.library.unit.MetricPrefix.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

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
        public final int maxValue;
        public final double precision;
        public final int scale;
        public final Unit<?> unit;

        public Measure(double minValue, int maxValue, double precision, Unit<?> unit) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.precision = precision;
            this.unit = unit;
            String[] splitter = Double.valueOf(precision).toString().split("\\.");
            this.scale = splitter.length > 1 ? splitter[1].length() : 0;
        }
    }

    public enum MeasureClass {
        INTERIOR_TEMPERATURE,
        EXTERIOR_TEMPERATURE,
        PRESSURE,
        CO2,
        NOISE,
        RAIN_QTTY,
        RAIN_INTENSITY,
        WIND_SPEED,
        WIND_ANGLE,
        HUMIDITY;
    }

    public static final Map<MeasureClass, Measure> NA_MEASURES = Map.of(MeasureClass.INTERIOR_TEMPERATURE,
            new Measure(0, 50, 0.3, SIUnits.CELSIUS), MeasureClass.EXTERIOR_TEMPERATURE,
            new Measure(-40, 65, 0.3, SIUnits.CELSIUS), MeasureClass.PRESSURE,
            new Measure(260, 1260, 1, HECTO(SIUnits.PASCAL)), MeasureClass.CO2,
            new Measure(0, 5000, 50, Units.PARTS_PER_MILLION), MeasureClass.NOISE,
            new Measure(35, 120, 1, Units.DECIBEL), MeasureClass.RAIN_QTTY,
            new Measure(0.2, 150, 0.1, MILLI(SIUnits.METRE)), MeasureClass.RAIN_INTENSITY,
            new Measure(0.2, 150, 0.1, Units.MILLIMETRE_PER_HOUR), MeasureClass.WIND_SPEED,
            new Measure(0, 160, 1.8, SIUnits.KILOMETRE_PER_HOUR), MeasureClass.WIND_ANGLE,
            new Measure(0, 360, 5, Units.DEGREE_ANGLE), MeasureClass.HUMIDITY, new Measure(0, 100, 3, Units.PERCENT));

    // Netatmo API urls
    public static final String NA_API_URL = "https://api.netatmo.com/";
    public static final String NA_APP_URL = "https://app.netatmo.net/";
    public static final String NA_OAUTH_PATH = "oauth2/token";
    public static final String NA_API_PATH = "api";
    public static final String NA_COMMAND_PATH = "command";
    public static final String NA_PERSON_AWAY_SPATH = "setpersonsaway";
    public static final String NA_PERSON_HOME_SPATH = "setpersonshome";
    public static final String NA_HOMES_SPATH = "homesdata";
    public static final String NA_GETHOME_SPATH = "gethomedata";
    public static final String NA_GETCAMERAPICTURE_SPATH = "getcamerapicture";
    public static final String NA_ADDWEBHOOK_SPATH = "addwebhook";
    public static final String NA_DROPWEBHOOK_SPATH = "dropwebhook";
    public static final String NA_SETROOMTHERMPOINT_SPATH = "setroomthermpoint";
    public static final String NA_SETTHERMMODE_SPATH = "setthermmode";
    public static final String NA_SWITCHSCHEDULE_SPATH = "switchschedule";
    public static final String NA_GETTHERMOSTAT_SPATH = "getthermostatsdata";
    public static final String NA_GETSTATION_SPATH = "getstationsdata";
    public static final String NA_GETMEASURE_SPATH = "getmeasure";
    public static final String NA_HOMESTATUS_SPATH = "homestatus";
    public static final String NA_HOMECOACH_SPATH = "gethomecoachsdata";
    public static final String NA_GETLASTEVENT_SPATH = "getlasteventof";

    public static final String NA_DEVICEID_PARAM = "device_id";
    public static final String NA_MODULEID_PARAM = "module_id";
    public static final String NA_HOMEID_PARAM = "home_id";
    public static final String NA_ROOMID_PARAM = "room_id";
    public static final String NA_SCHEDULEID_PARAM = "schedule_id";
    public static final String NA_MODE_PARAM = "mode";

    public enum MeasureType {
        SUM_RAIN(MeasureClass.RAIN_QTTY),
        TEMP(MeasureClass.EXTERIOR_TEMPERATURE),
        HUM(MeasureClass.EXTERIOR_TEMPERATURE),
        CO2(MeasureClass.CO2),
        NOISE(MeasureClass.NOISE),
        PRESSURE(MeasureClass.PRESSURE),
        WIND(MeasureClass.WIND_SPEED);

        private MeasureClass unit;

        MeasureType(MeasureClass unit) {
            this.unit = unit;
        }

        public MeasureClass getUnit() {
            return unit;
        }
    }

    public enum MeasureLimit {
        MIN,
        MAX,
        DATE_MIN,
        DATE_MAX,
        NONE;
    }

    public enum MeasureScale {
        THIRTY_MINUTES("30min"),
        ONE_HOUR("1hour"),
        THREE_HOURS("3hours"),
        ONE_DAY("1day"),
        ONE_WEEK("1week"),
        ONE_MONTH("1month");

        private static Map<String, MeasureScale> stringMap = Arrays.stream(values())
                .collect(Collectors.toMap(MeasureScale::getDescriptor, Function.identity()));

        private final String apiDescriptor;

        MeasureScale(String value) {
            this.apiDescriptor = value;
        }

        public String getDescriptor() {
            return apiDescriptor;
        }

        public static @Nullable MeasureScale from(String descriptor) {
            return stringMap.get(descriptor);
        }
    }

    // Token scopes
    public static enum Scope {
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
        UNKNOWN;
    }

    static final Set<Scope> WEATHER_SCOPES = Set.of(Scope.READ_STATION);
    private static final Set<Scope> SMOKE_SCOPES = Set.of(Scope.READ_SMOKEDETECTOR);
    static final Set<Scope> AIR_QUALITY_SCOPES = Set.of(Scope.READ_HOMECOACH);
    static final Set<Scope> ENERGY_SCOPES = Set.of(Scope.READ_THERMOSTAT, Scope.WRITE_THERMOSTAT);
    private static final Set<Scope> WELCOME_SCOPES = Set.of(Scope.READ_CAMERA, Scope.WRITE_CAMERA, Scope.ACCESS_CAMERA);
    private static final Set<Scope> DOORBELL_SCOPES = Set.of(Scope.READ_DOORBELL, Scope.WRITE_DOORBELL,
            Scope.ACCESS_DOORBELL);
    private static final Set<Scope> PRESENCE_SCOPES = Set.of(Scope.READ_PRESENCE, Scope.ACCESS_PRESENCE);
    static final Set<Scope> SECURITY_SCOPES = Stream.of(WELCOME_SCOPES, PRESENCE_SCOPES, SMOKE_SCOPES, DOORBELL_SCOPES)
            .flatMap(Set::stream).collect(Collectors.toSet());
    static final Set<Scope> ALL_SCOPES = Stream.of(WEATHER_SCOPES, ENERGY_SCOPES, SECURITY_SCOPES, AIR_QUALITY_SCOPES)
            .flatMap(Set::stream).collect(Collectors.toSet());

    // Radio signal quality thresholds
    static final int[] WIFI_SIGNAL_LEVELS = new int[] { 99, 84, 69, 54 }; // Resp : bad, average, good, full
    static final int[] RADIO_SIGNAL_LEVELS = new int[] { 90, 80, 70, 60 }; // Resp : low, medium, high, full
    public static final int[] NO_RADIO = new int[0]; // No radio => no levels

    // Thermostat definitions
    public static enum SetpointMode {
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
        UNKNOWN(""),
        @SerializedName("schedule")
        SCHEDULE("schedule"),
        HOME("home");

        String apiDescriptor;

        SetpointMode(String descriptor) {
            this.apiDescriptor = descriptor;
        }

        public String getDescriptor() {
            return apiDescriptor;
        }

        // TODO Remove unused code found by UCDetector
        // public static SetpointMode fromName(String name) {
        // return Arrays.stream(values()).filter(value -> value.apiDescriptor.equals(name)).findFirst()
        // .orElse(UNKNOWN);
        // }
    }

    public static enum ThermostatZoneType {
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

        String zoneId;

        private ThermostatZoneType(String id) {
            zoneId = id;
        }

        public static ThermostatZoneType fromId(String id) {
            return Arrays.stream(values()).filter(value -> value.zoneId.equals(id)).findFirst().orElse(UNKNOWN);
        }
    }

    // Presence
    public enum PresenceLightMode {
        @SerializedName("on")
        ON,
        @SerializedName("off")
        OFF,
        @SerializedName("auto")
        AUTO,
        UNKNOWN;
    }

    public enum EventCategory {
        @SerializedName("human")
        HUMAN,
        @SerializedName("animal")
        ANIMAL,
        @SerializedName("vehicle")
        VEHICLE,
        UNKNOWN;
    }

    public enum TrendDescription {
        @SerializedName("up")
        UP,
        @SerializedName("stable")
        STABLE,
        @SerializedName("down")
        DOWN,
        UNKNOWN;
    }

    public enum VideoStatus {
        @SerializedName("recording")
        RECORDING,
        @SerializedName("available")
        AVAILABLE,
        @SerializedName("deleted")
        DELETED,
        UNKNOWN;
    }
}
