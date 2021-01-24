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
import javax.measure.quantity.Angle;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;

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
    // Netatmo API urls
    public static final String NETATMO_BASE_URL = "https://api.netatmo.com/";
    public static final String NETATMO_APP_URL = "https://app.netatmo.net/";

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Pressure> PRESSURE_UNIT = HECTO(SIUnits.PASCAL);
    public static final Unit<Speed> WIND_SPEED_UNIT = SIUnits.KILOMETRE_PER_HOUR;
    public static final Unit<Angle> WIND_DIRECTION_UNIT = Units.DEGREE_ANGLE;
    public static final Unit<Length> RAIN_UNIT = MILLI(SIUnits.METRE);
    public static final Unit<Dimensionless> CO2_UNIT = Units.PARTS_PER_MILLION;
    public static final Unit<Dimensionless> NOISE_UNIT = Units.DECIBEL;

    public enum MeasureType {
        SUM_RAIN,
        @SerializedName("Temperature")
        TEMP,
        @SerializedName("Humidity")
        HUM,
        @SerializedName("CO2")
        CO2,
        @SerializedName("Noise")
        NOISE,
        @SerializedName("Pressure")
        PRESSURE,
        @SerializedName("Wind")
        WIND;
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

    // Default unit associated with each kind of measurement
    public static final Map<MeasureType, Unit<?>> MEASUREUNITS = Map.of(MeasureType.SUM_RAIN, RAIN_UNIT,
            MeasureType.TEMP, TEMPERATURE_UNIT, MeasureType.HUM, HUMIDITY_UNIT, MeasureType.CO2, CO2_UNIT,
            MeasureType.NOISE, NOISE_UNIT, MeasureType.PRESSURE, PRESSURE_UNIT, MeasureType.WIND, WIND_SPEED_UNIT);

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
        ACCESS_DOORBELL;
    }

    public static final Set<Scope> WEATHER_SCOPES = Set.of(Scope.READ_STATION);
    public static final Set<Scope> ENERGY_SCOPES = Set.of(Scope.READ_THERMOSTAT, Scope.WRITE_THERMOSTAT);
    public static final Set<Scope> WELCOME_SCOPES = Set.of(Scope.READ_CAMERA, Scope.WRITE_CAMERA, Scope.ACCESS_CAMERA);
    public static final Set<Scope> DOORBELL_SCOPES = Set.of(Scope.READ_DOORBELL, Scope.WRITE_DOORBELL,
            Scope.ACCESS_DOORBELL);
    public static final Set<Scope> PRESENCE_SCOPES = Set.of(Scope.READ_PRESENCE, Scope.ACCESS_PRESENCE);
    public static final Set<Scope> SMOKE_SCOPES = Set.of(Scope.READ_SMOKEDETECTOR);
    public static final Set<Scope> AIR_QUALITY_SCOPES = Set.of(Scope.READ_HOMECOACH);
    public static final Set<Scope> SECURITY_SCOPES = Stream.of(WELCOME_SCOPES, PRESENCE_SCOPES, SMOKE_SCOPES)
            .flatMap(Set::stream).collect(Collectors.toSet());
    public static final Set<Scope> ALL_SCOPES = Stream
            .of(WEATHER_SCOPES, ENERGY_SCOPES, SECURITY_SCOPES, AIR_QUALITY_SCOPES).flatMap(Set::stream)
            .collect(Collectors.toSet());

    // Radio signal quality thresholds
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final int[] WIFI_SIGNAL_LEVELS = new int[] { 86, 71, 56 }; // Resp : bad, average, good
    public static final int[] RADIO_SIGNAL_LEVELS = new int[] { 90, 80, 70, 60 }; // Resp : low, medium, high, full
    public static final int[] NO_RADIO = EMPTY_INT_ARRAY;

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
        UNKNOWN("");

        String apiDescriptor;

        SetpointMode(String descriptor) {
            this.apiDescriptor = descriptor;
        }

        public String getDescriptor() {
            return apiDescriptor;
        }
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
        VEHICLE;
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
