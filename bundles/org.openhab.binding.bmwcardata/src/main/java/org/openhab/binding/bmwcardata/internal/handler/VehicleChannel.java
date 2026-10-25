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
package org.openhab.binding.bmwcardata.internal.handler;

import static org.openhab.binding.bmwcardata.internal.BMWCarDataBindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwcardata.internal.api.dto.TelematicValue;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * The {@link VehicleChannel} maps the channels of a vehicle to the CarData technical descriptors they represent.
 *
 * @author Michael Weger - Initial contribution
 * @author Mark Herwege - Value conversion from TelematicStateConverter and CarDataUnits of the mybmw rewrite
 */
@NonNullByDefault
public enum VehicleChannel {
    DOOR_LOCK(GROUP_STATUS, "door-lock", "vehicle.cabin.door.lock.status", Kind.LOCK_STATE, null),
    TRUNK(GROUP_STATUS, "trunk", "vehicle.body.trunk.isOpen", Kind.CONTACT, null),
    HOOD(GROUP_STATUS, "hood", "vehicle.body.hood.isOpen", Kind.CONTACT, null),
    TRUNK_LOCKED(GROUP_STATUS, "trunk-locked", "vehicle.body.trunk.isLocked", Kind.SWITCH, null),
    DOOR_FRONT_DRIVER(GROUP_STATUS, "door-front-driver", "vehicle.cabin.door.row1.driver.isOpen", Kind.CONTACT, null),
    DOOR_FRONT_PASSENGER(GROUP_STATUS, "door-front-passenger", "vehicle.cabin.door.row1.passenger.isOpen", Kind.CONTACT,
            null),
    DOOR_REAR_DRIVER(GROUP_STATUS, "door-rear-driver", "vehicle.cabin.door.row2.driver.isOpen", Kind.CONTACT, null),
    DOOR_REAR_PASSENGER(GROUP_STATUS, "door-rear-passenger", "vehicle.cabin.door.row2.passenger.isOpen", Kind.CONTACT,
            null),
    WINDOW_FRONT_DRIVER(GROUP_STATUS, "window-front-driver", "vehicle.cabin.window.row1.driver.status", Kind.OPENING,
            null),
    WINDOW_FRONT_PASSENGER(GROUP_STATUS, "window-front-passenger", "vehicle.cabin.window.row1.passenger.status",
            Kind.OPENING, null),
    WINDOW_REAR_DRIVER(GROUP_STATUS, "window-rear-driver", "vehicle.cabin.window.row2.driver.status", Kind.OPENING,
            null),
    WINDOW_REAR_PASSENGER(GROUP_STATUS, "window-rear-passenger", "vehicle.cabin.window.row2.passenger.status",
            Kind.OPENING, null),
    REAR_WINDOW(GROUP_STATUS, "rear-window", "vehicle.body.trunk.window.isOpen", Kind.CONTACT, null),
    SUNROOF(GROUP_STATUS, "sunroof", "vehicle.cabin.sunroof.overallStatus", Kind.OPENING, null),
    MOVING(GROUP_STATUS, "moving", "vehicle.isMoving", Kind.SWITCH, null),
    DEEP_SLEEP(GROUP_STATUS, "deep-sleep", "vehicle.vehicle.deepSleepModeActive", Kind.SWITCH, null),
    BATTERY_VOLTAGE(GROUP_STATUS, "battery-voltage", "vehicle.electricalSystem.battery.voltage", Kind.QUANTITY,
            Units.VOLT),
    BATTERY_SOC(GROUP_STATUS, "battery-soc", "vehicle.electricalSystem.battery.stateOfCharge", Kind.QUANTITY,
            Units.PERCENT),
    // the catalogue describes isActive as ignition state and isIgnitionOn as engine state
    IGNITION(GROUP_STATUS, "ignition", "vehicle.drivetrain.engine.isActive", Kind.SWITCH, null),
    ENGINE(GROUP_STATUS, "engine", "vehicle.drivetrain.engine.isIgnitionOn", Kind.SWITCH, null),
    ALARM(GROUP_SECURITY, "alarm", "vehicle.vehicle.antiTheftAlarmSystem.alarm.isOn", Kind.SWITCH, null),
    ALARM_ARM_STATUS(GROUP_SECURITY, "arm-status", "vehicle.vehicle.antiTheftAlarmSystem.alarm.armStatus", Kind.STRING,
            null),
    // provided without time zone and in an undocumented format
    ALARM_ACTIVATION_TIME(GROUP_SECURITY, "activation-time",
            "vehicle.vehicle.antiTheftAlarmSystem.alarm.activationTime", Kind.STRING, null),
    BATTERY_CAPACITY(GROUP_BATTERY, "capacity", "vehicle.drivetrain.batteryManagement.batterySizeMax", Kind.QUANTITY,
            Units.KILOWATT_HOUR),
    BATTERY_ENERGY(GROUP_BATTERY, "energy-content", "vehicle.drivetrain.batteryManagement.maxEnergy", Kind.QUANTITY,
            Units.KILOWATT_HOUR),
    // no unit is documented for the state of certified energy
    BATTERY_HEALTH(GROUP_BATTERY, "health", "vehicle.powertrain.electric.battery.stateOfHealth.displayed", Kind.NUMBER,
            null),
    ENERGY_TO_FULL(GROUP_BATTERY, "energy-to-full",
            "vehicle.drivetrain.electricEngine.charging.smeEnergyDeltaFullyCharged", Kind.QUANTITY,
            Units.KILOWATT_HOUR),
    SERVICE_DISTANCE(GROUP_SERVICE, "distance", "vehicle.status.serviceDistance.next", Kind.QUANTITY,
            MetricPrefix.KILO(SIUnits.METRE)),
    INSPECTION_DATE(GROUP_SERVICE, "inspection-date", "vehicle.status.serviceTime.inspectionDateLegal", Kind.DATETIME,
            null),
    GRID_ENERGY(GROUP_STATISTICS, "grid-energy",
            "vehicle.drivetrain.electricEngine.charging.consumptionOverLifeTime.overall.gridEnergy", Kind.QUANTITY,
            Units.KILOWATT_HOUR),
    FUEL_CONSUMED(GROUP_STATISTICS, "fuel-consumed",
            "vehicle.drivetrain.fuelSystem.consumptionOverLifeTime.overall.fuel", Kind.QUANTITY, Units.LITRE),
    // the unit kWh/100km has no openHAB dimension
    AVG_CONSUMPTION(GROUP_STATISTICS, "avg-consumption", "vehicle.drivetrain.avgElectricRangeConsumption", Kind.NUMBER,
            null),
    AVG_SPEED(GROUP_STATISTICS, "avg-speed", "vehicle.vehicle.avgSpeed", Kind.QUANTITY, SIUnits.KILOMETRE_PER_HOUR),
    WEEKLY_DISTANCE(GROUP_STATISTICS, "weekly-distance", "vehicle.vehicle.averageWeeklyDistanceShortTerm",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.METRE)),
    WEEKLY_DISTANCE_LONG_TERM(GROUP_STATISTICS, "weekly-distance-long-term",
            "vehicle.vehicle.averageWeeklyDistanceLongTerm", Kind.QUANTITY, MetricPrefix.KILO(SIUnits.METRE)),
    MILEAGE(GROUP_RANGE, "mileage", "vehicle.vehicle.travelledDistance", Kind.QUANTITY,
            MetricPrefix.KILO(SIUnits.METRE)),
    TOTAL_RANGE(GROUP_RANGE, "total", "vehicle.drivetrain.lastRemainingRange", Kind.QUANTITY,
            MetricPrefix.KILO(SIUnits.METRE)),
    ELECTRIC_RANGE(GROUP_RANGE, "electric", "vehicle.drivetrain.electricEngine.kombiRemainingElectricRange",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.METRE)),
    ELECTRIC_RANGE_FORECAST(GROUP_RANGE, "electric-charging-forecast",
            "vehicle.drivetrain.electricEngine.remainingElectricRange", Kind.QUANTITY,
            MetricPrefix.KILO(SIUnits.METRE)),
    FUEL_LEVEL(GROUP_RANGE, "fuel-level", "vehicle.drivetrain.fuelSystem.level", Kind.QUANTITY, Units.PERCENT),
    REMAINING_FUEL(GROUP_RANGE, "remaining-fuel", "vehicle.drivetrain.fuelSystem.remainingFuel", Kind.QUANTITY,
            Units.LITRE),
    SOC(GROUP_CHARGE, "soc", "vehicle.drivetrain.batteryManagement.header", Kind.QUANTITY, Units.PERCENT),
    TARGET_SOC(GROUP_CHARGE, "target-soc", "vehicle.powertrain.electric.battery.stateOfCharge.target", Kind.QUANTITY,
            Units.PERCENT),
    CHARGING_STATUS(GROUP_CHARGE, "status", "vehicle.drivetrain.electricEngine.charging.status", Kind.STRING, null),
    PLUGGED(GROUP_CHARGE, "plugged", "vehicle.powertrain.tractionBattery.charging.port.anyPosition.isPlugged",
            Kind.SWITCH, null),
    CHARGING_POWER(GROUP_CHARGE, "power", "vehicle.powertrain.electric.battery.charging.power", Kind.QUANTITY,
            Units.WATT),
    TIME_REMAINING(GROUP_CHARGE, "time-remaining", "vehicle.drivetrain.electricEngine.charging.timeRemaining",
            Kind.QUANTITY, Units.MINUTE),
    AC_CURRENT(GROUP_CHARGE, "ac-current", "vehicle.drivetrain.electricEngine.charging.acAmpere", Kind.QUANTITY,
            Units.AMPERE),
    AC_VOLTAGE(GROUP_CHARGE, "ac-voltage", "vehicle.drivetrain.electricEngine.charging.acVoltage", Kind.QUANTITY,
            Units.VOLT),
    AC_LIMIT(GROUP_CHARGE, "ac-limit", "vehicle.powertrain.electric.battery.charging.acLimit.selected", Kind.QUANTITY,
            Units.AMPERE),
    PHASES(GROUP_CHARGE, "phases", "vehicle.drivetrain.electricEngine.charging.phaseNumber", Kind.STRING, null),
    CHARGING_METHOD(GROUP_CHARGE, "method", "vehicle.drivetrain.electricEngine.charging.method", Kind.STRING, null),
    PORT(GROUP_CHARGE, "port", "vehicle.body.chargingPort.status", Kind.STRING, null),
    PREDICTED_SOC(GROUP_CHARGE, "predicted-soc", "vehicle.drivetrain.electricEngine.charging.level", Kind.QUANTITY,
            Units.PERCENT),
    MIN_TARGET_SOC(GROUP_CHARGE, "min-target-soc", "vehicle.powertrain.electric.battery.stateOfCharge.targetMin",
            Kind.QUANTITY, Units.PERCENT),
    TIME_TO_FULL(GROUP_CHARGE, "time-to-full", "vehicle.drivetrain.electricEngine.charging.timeToFullyCharged",
            Kind.QUANTITY, Units.MINUTE),
    CHARGING_MODE(GROUP_CHARGE, "mode", "vehicle.drivetrain.electricEngine.charging.chargingMode", Kind.STRING, null),
    CHARGING_END_REASON(GROUP_CHARGE, "end-reason", "vehicle.drivetrain.electricEngine.charging.reasonChargingEnd",
            Kind.STRING, null),
    LAST_CHARGING_RESULT(GROUP_CHARGE, "last-result", "vehicle.drivetrain.electricEngine.charging.lastChargingResult",
            Kind.STRING, null),
    CABLE_LOCK(GROUP_CHARGE, "cable-lock", "vehicle.body.chargingPort.lockedStatus", Kind.STRING, null),
    CHARGING_FLAP(GROUP_CHARGE, "flap", "vehicle.powertrain.tractionBattery.charging.port.anyPosition.flap.isOpen",
            Kind.CONTACT, null),
    PRESSURE_FRONT_LEFT(GROUP_TYRES, "pressure-front-left", "vehicle.chassis.axle.row1.wheel.left.tire.pressure",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    PRESSURE_FRONT_RIGHT(GROUP_TYRES, "pressure-front-right", "vehicle.chassis.axle.row1.wheel.right.tire.pressure",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    PRESSURE_REAR_LEFT(GROUP_TYRES, "pressure-rear-left", "vehicle.chassis.axle.row2.wheel.left.tire.pressure",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    PRESSURE_REAR_RIGHT(GROUP_TYRES, "pressure-rear-right", "vehicle.chassis.axle.row2.wheel.right.tire.pressure",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    TARGET_FRONT_LEFT(GROUP_TYRES, "target-front-left", "vehicle.chassis.axle.row1.wheel.left.tire.pressureTarget",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    TARGET_FRONT_RIGHT(GROUP_TYRES, "target-front-right", "vehicle.chassis.axle.row1.wheel.right.tire.pressureTarget",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    TARGET_REAR_LEFT(GROUP_TYRES, "target-rear-left", "vehicle.chassis.axle.row2.wheel.left.tire.pressureTarget",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    TARGET_REAR_RIGHT(GROUP_TYRES, "target-rear-right", "vehicle.chassis.axle.row2.wheel.right.tire.pressureTarget",
            Kind.QUANTITY, MetricPrefix.KILO(SIUnits.PASCAL)),
    TEMPERATURE_FRONT_LEFT(GROUP_TYRES, "temperature-front-left",
            "vehicle.chassis.axle.row1.wheel.left.tire.temperature", Kind.QUANTITY, SIUnits.CELSIUS),
    TEMPERATURE_FRONT_RIGHT(GROUP_TYRES, "temperature-front-right",
            "vehicle.chassis.axle.row1.wheel.right.tire.temperature", Kind.QUANTITY, SIUnits.CELSIUS),
    TEMPERATURE_REAR_LEFT(GROUP_TYRES, "temperature-rear-left", "vehicle.chassis.axle.row2.wheel.left.tire.temperature",
            Kind.QUANTITY, SIUnits.CELSIUS),
    TEMPERATURE_REAR_RIGHT(GROUP_TYRES, "temperature-rear-right",
            "vehicle.chassis.axle.row2.wheel.right.tire.temperature", Kind.QUANTITY, SIUnits.CELSIUS),
    LATITUDE(GROUP_NAVIGATION, null, "vehicle.cabin.infotainment.navigation.currentLocation.latitude", Kind.NUMBER,
            null),
    LONGITUDE(GROUP_NAVIGATION, null, "vehicle.cabin.infotainment.navigation.currentLocation.longitude", Kind.NUMBER,
            null),
    HEADING(GROUP_NAVIGATION, "heading", "vehicle.cabin.infotainment.navigation.currentLocation.heading", Kind.QUANTITY,
            Units.DEGREE_ANGLE),
    ALTITUDE(GROUP_NAVIGATION, "altitude", "vehicle.cabin.infotainment.navigation.currentLocation.altitude",
            Kind.QUANTITY, SIUnits.METRE),
    GPS_FIX(GROUP_NAVIGATION, "gps-fix", "vehicle.cabin.infotainment.navigation.currentLocation.fixStatus", Kind.STRING,
            null),
    DESTINATION_DISTANCE(GROUP_NAVIGATION, "destination-distance",
            "vehicle.cabin.infotainment.navigation.destinationSet.distance", Kind.QUANTITY,
            MetricPrefix.KILO(SIUnits.METRE)),
    ARRIVAL_TIME(GROUP_NAVIGATION, "arrival-time", "vehicle.cabin.infotainment.navigation.destinationSet.arrivalTime",
            Kind.STRING, null),
    COMFORT_STATE(GROUP_CLIMATE, "comfort-state", "vehicle.cabin.hvac.preconditioning.status.comfortState", Kind.STRING,
            null),
    PRECONDITIONING_PROGRESS(GROUP_CLIMATE, "progress", "vehicle.cabin.hvac.preconditioning.status.progress",
            Kind.QUANTITY, Units.PERCENT),
    PRECONDITIONING_REMAINING(GROUP_CLIMATE, "remaining-running-time",
            "vehicle.cabin.hvac.preconditioning.status.remainingRunningTime", Kind.QUANTITY, Units.SECOND),
    TARGET_TEMPERATURE(GROUP_CLIMATE, "target-temperature",
            "vehicle.cabin.hvac.preconditioning.configuration.directStartSettings.targetTemperature", Kind.QUANTITY,
            SIUnits.CELSIUS),
    REAR_DEFROST(GROUP_CLIMATE, "rear-defrost", "vehicle.cabin.hvac.preconditioning.status.rearDefrostActive",
            Kind.SWITCH, null),
    MIRROR_HEATING(GROUP_CLIMATE, "mirror-heating",
            "vehicle.cabin.hvac.preconditioning.status.isExteriorMirrorHeatingActive", Kind.SWITCH, null),
    AIR_PURIFICATION(GROUP_CLIMATE, "air-purification", "vehicle.cabin.hvac.statusAirPurification", Kind.STRING, null),
    AUXILIARY_ACTIVITY(GROUP_CLIMATE, "auxiliary-activity", "vehicle.vehicle.preConditioning.activity", Kind.STRING,
            null),
    AUXILIARY_REMAINING(GROUP_CLIMATE, "auxiliary-remaining-time", "vehicle.vehicle.preConditioning.remainingTime",
            Kind.QUANTITY, Units.MINUTE),
    AUXILIARY_ERROR(GROUP_CLIMATE, "auxiliary-error", "vehicle.vehicle.preConditioning.error", Kind.STRING, null),
    TRIP_END(GROUP_TRIP, "end-time", "vehicle.trip.segment.end.time", Kind.DATETIME, null),
    TRIP_END_MILEAGE(GROUP_TRIP, "end-mileage", "vehicle.trip.segment.end.travelledDistance", Kind.QUANTITY,
            MetricPrefix.KILO(SIUnits.METRE)),
    TRIP_END_SOC(GROUP_TRIP, "end-soc", "vehicle.trip.segment.end.drivetrain.batteryManagement.hvSoc", Kind.QUANTITY,
            Units.PERCENT),
    // the unit kWh/100km has no openHAB dimension
    TRIP_RECUPERATION(GROUP_TRIP, "recuperation",
            "vehicle.trip.segment.accumulated.drivetrain.electricEngine.recuperationTotal", Kind.NUMBER, null),
    TRIP_COMFORT_ENERGY(GROUP_TRIP, "comfort-energy",
            "vehicle.trip.segment.accumulated.drivetrain.electricEngine.energyConsumptionComfort", Kind.QUANTITY,
            Units.KILOWATT_HOUR),
    TRIP_ELECTRIC_SHARE(GROUP_TRIP, "electric-share",
            "vehicle.trip.segment.accumulated.drivetrain.transmission.setting.fractionDriveElectric", Kind.QUANTITY,
            Units.PERCENT),
    TRIP_ECO_PRO_SHARE(GROUP_TRIP, "eco-pro-share",
            "vehicle.trip.segment.accumulated.drivetrain.transmission.setting.fractionDriveEcoPro", Kind.QUANTITY,
            Units.PERCENT),
    TRIP_ECO_PRO_PLUS_SHARE(GROUP_TRIP, "eco-pro-plus-share",
            "vehicle.trip.segment.accumulated.drivetrain.transmission.setting.fractionDriveEcoProPlus", Kind.QUANTITY,
            Units.PERCENT),
    TRIP_ACCELERATION_RATING(GROUP_TRIP, "acceleration-rating",
            "vehicle.trip.segment.accumulated.acceleration.starsAverage", Kind.NUMBER, null),
    TRIP_ANTICIPATION_RATING(GROUP_TRIP, "anticipation-rating",
            "vehicle.trip.segment.accumulated.chassis.brake.starsAverage", Kind.NUMBER, null);

    /**
     * The drive a channel needs to apply to a vehicle.
     */
    public enum Drive {
        ANY,
        // an electric motor with a high-voltage battery that can be charged: electric and plug-in hybrid vehicles
        ELECTRIC,
        // a combustion engine: combustion, mild hybrid and plug-in hybrid vehicles
        COMBUSTION
    }

    private static final Set<VehicleChannel> ELECTRIC_ONLY = EnumSet.of(BATTERY_CAPACITY, BATTERY_ENERGY,
            BATTERY_HEALTH, ENERGY_TO_FULL, GRID_ENERGY, AVG_CONSUMPTION, ELECTRIC_RANGE, ELECTRIC_RANGE_FORECAST, SOC,
            TARGET_SOC, CHARGING_STATUS, PLUGGED, CHARGING_POWER, TIME_REMAINING, AC_CURRENT, AC_VOLTAGE, AC_LIMIT,
            PHASES, CHARGING_METHOD, DC_PORT, PREDICTED_SOC, MIN_TARGET_SOC, TIME_TO_FULL, CHARGING_MODE,
            CHARGING_END_REASON, LAST_CHARGING_RESULT, CABLE_LOCK, CHARGING_FLAP, TRIP_END_SOC, TRIP_RECUPERATION,
            TRIP_COMFORT_ENERGY, TRIP_ELECTRIC_SHARE);

    // lastRemainingRange is meant for vehicles with a combustion engine, according to BMW's catalogue
    private static final Set<VehicleChannel> COMBUSTION_ONLY = EnumSet.of(FUEL_CONSUMED, FUEL_LEVEL, REMAINING_FUEL,
            TOTAL_RANGE);

    enum Kind {
        QUANTITY,
        NUMBER,
        STRING,
        SWITCH,
        CONTACT,
        // an opening that can be partly open: a contact that is open when partly open, plus a detail channel with
        // the reported state
        OPENING,
        // a lock state word, normalized so that e.g. SELECTIVE-LOCKED and SELECTIVELOCKED compare equal
        LOCK_STATE,
        DATETIME
    }

    private static final Map<String, VehicleChannel> BY_DESCRIPTOR = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(VehicleChannel::getDescriptor, Function.identity()));

    // units as sent by CarData, in lower case; other units are parsed by openHAB
    private static final Map<String, Unit<?>> UNITS = Map.ofEntries(Map.entry("km", MetricPrefix.KILO(SIUnits.METRE)),
            Map.entry("kilometers", MetricPrefix.KILO(SIUnits.METRE)),
            Map.entry("kilometres", MetricPrefix.KILO(SIUnits.METRE)), Map.entry("mi", ImperialUnits.MILE),
            Map.entry("miles", ImperialUnits.MILE), Map.entry("m", SIUnits.METRE), Map.entry("ft", ImperialUnits.FOOT),
            Map.entry("%", Units.PERCENT), Map.entry("percent", Units.PERCENT), Map.entry("v", Units.VOLT),
            Map.entry("a", Units.AMPERE), Map.entry("w", Units.WATT), Map.entry("kw", MetricPrefix.KILO(Units.WATT)),
            Map.entry("kwh", Units.KILOWATT_HOUR), Map.entry("s", Units.SECOND), Map.entry("min", Units.MINUTE),
            Map.entry("minutes", Units.MINUTE), Map.entry("h", Units.HOUR), Map.entry("weeks", Units.WEEK),
            Map.entry("months", Units.MONTH), Map.entry("kpa", MetricPrefix.KILO(SIUnits.PASCAL)),
            Map.entry("bar", Units.BAR), Map.entry("°c", SIUnits.CELSIUS), Map.entry("celsius", SIUnits.CELSIUS),
            Map.entry("°", Units.DEGREE_ANGLE), Map.entry("degree", Units.DEGREE_ANGLE),
            Map.entry("degrees", Units.DEGREE_ANGLE), Map.entry("l", Units.LITRE), Map.entry("liters", Units.LITRE),
            Map.entry("litres", Units.LITRE), Map.entry("gal", ImperialUnits.GALLON_LIQUID_US),
            Map.entry("km/h", SIUnits.KILOMETRE_PER_HOUR), Map.entry("mph", ImperialUnits.MILES_PER_HOUR));

    // points in time as documented by the catalogue, besides ISO 8601 and epoch seconds
    private static final List<DateTimeFormatter> DATE_TIME_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm[:ss][ 'UTC']", Locale.ROOT),
            DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm[:ss][ 'UTC']", Locale.ROOT));
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT),
            DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.ROOT), DateTimeFormatter.ISO_LOCAL_DATE);

    // normalized words used by CarData to mark a value as unavailable
    private static final Set<String> UNDEFINED_WORDS = Set.of("", "INVALID", "NA", "UNKNOWN", "ISUNKNOWN");
    private static final Set<String> TRUE_WORDS = Set.of("TRUE", "ISTRUE", "ON", "YES", "ACTIVE");
    private static final Set<String> FALSE_WORDS = Set.of("FALSE", "ISFALSE", "OFF", "NO", "INACTIVE");
    // doors are documented as boolean but reported as OPEN or CLOSED; windows and the sunroof can be partly open
    private static final Set<String> OPEN_WORDS = Set.of("OPEN", "INTERMEDIATE", "OPENTILT", "INTERMEDIATETILT", "TRUE",
            "ISTRUE");
    private static final Set<String> CLOSED_WORDS = Set.of("CLOSED", "FALSE", "ISFALSE");

    private final String groupId;
    private final @Nullable String channelId;
    private final String descriptor;
    private final Kind kind;
    private final @Nullable Unit<?> defaultUnit;

    VehicleChannel(String groupId, @Nullable String channelId, String descriptor, Kind kind,
            @Nullable Unit<?> defaultUnit) {
        this.groupId = groupId;
        this.channelId = channelId;
        this.descriptor = descriptor;
        this.kind = kind;
        this.defaultUnit = defaultUnit;
    }

    public String getDescriptor() {
        return descriptor;
    }

    /**
     * @return the id of the channel within the thing, or {@code null} if the descriptor only contributes to a
     *         derived channel
     */
    public @Nullable String getChannelUid() {
        String channelId = this.channelId;
        return channelId != null ? groupId + "#" + channelId : null;
    }

    /**
     * @return the id of the channel with the reported state of an opening, {@code <channel>-state}, or {@code null}
     *         for all other kinds
     */
    public @Nullable String getDetailChannelUid() {
        String channelUid = getChannelUid();
        return kind == Kind.OPENING && channelUid != null ? channelUid + "-state" : null;
    }

    public String getGroupId() {
        return groupId;
    }

    /**
     * @return the drive the channel needs to apply to a vehicle
     */
    public Drive getDrive() {
        if (ELECTRIC_ONLY.contains(this)) {
            return Drive.ELECTRIC;
        }
        return COMBUSTION_ONLY.contains(this) ? Drive.COMBUSTION : Drive.ANY;
    }

    public static @Nullable VehicleChannel fromDescriptor(String descriptor) {
        return BY_DESCRIPTOR.get(descriptor);
    }

    public static Set<String> descriptors() {
        return BY_DESCRIPTOR.keySet();
    }

    /**
     * Converts a descriptor value into the state of this channel. CarData values are loosely typed: booleans arrive
     * as JSON booleans, as {@code "true"} or as words such as {@code ASN_isTrue}, and the REST API sends numbers as
     * strings. Values that are marked as unavailable or cannot be interpreted result in {@link UnDefType#UNDEF}.
     *
     * @return the state, or {@code null} if the value is missing
     */
    public @Nullable State toState(TelematicValue telematicValue) {
        JsonPrimitive primitive = toPrimitive(telematicValue);
        if (primitive == null) {
            return null;
        }
        if (isUndefined(primitive)) {
            return UnDefType.UNDEF;
        }
        State state = switch (kind) {
            case STRING -> new StringType(primitive.getAsString().strip());
            case LOCK_STATE -> {
                String word = word(primitive);
                yield word == null ? null : new StringType(word);
            }
            case SWITCH -> {
                Boolean bool = toBoolean(primitive);
                yield bool == null ? null : OnOffType.from(bool);
            }
            case CONTACT, OPENING -> toContact(primitive);
            case DATETIME -> {
                Instant instant = toInstant(primitive);
                yield instant == null ? null : new DateTimeType(instant);
            }
            case NUMBER -> {
                BigDecimal number = toNumber(primitive);
                yield number == null ? null : new DecimalType(number);
            }
            case QUANTITY -> toQuantity(primitive, telematicValue.unit);
        };
        return state != null ? state : UnDefType.UNDEF;
    }

    /**
     * Converts a value for the detail channel of an opening, see {@link #getDetailChannelUid()}.
     *
     * @return the reported state, e.g. {@code INTERMEDIATE}, or {@code null} if the value is missing
     */
    public @Nullable State toDetailState(TelematicValue telematicValue) {
        JsonPrimitive primitive = toPrimitive(telematicValue);
        if (primitive == null) {
            return null;
        }
        return isUndefined(primitive) ? UnDefType.UNDEF : new StringType(primitive.getAsString().strip());
    }

    /**
     * Derives the state of {@link #PLUGGED} from the charging port status, for vehicles that report only the latter.
     *
     * @return {@code ON} for {@code CONNECTED}, {@code OFF} for {@code DISCONNECTED}, or {@code null} for other values
     */
    static @Nullable State toPluggedState(TelematicValue telematicValue) {
        JsonPrimitive primitive = toPrimitive(telematicValue);
        String word = primitive != null ? word(primitive) : null;
        if ("CONNECTED".equals(word)) {
            return OnOffType.ON;
        }
        if ("DISCONNECTED".equals(word)) {
            return OnOffType.OFF;
        }
        return null;
    }

    private @Nullable State toQuantity(JsonPrimitive primitive, @Nullable String unitSymbol) {
        BigDecimal number = toNumber(primitive);
        if (number == null) {
            return null;
        }
        Unit<?> defaultUnit = this.defaultUnit;
        Unit<?> unit = parseUnit(unitSymbol);
        if (unit == null) {
            return defaultUnit == null ? null : new QuantityType<>(number, defaultUnit);
        }
        if (defaultUnit != null && !unit.isCompatible(defaultUnit)) {
            return null;
        }
        return new QuantityType<>(number, unit);
    }

    private static @Nullable State toContact(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        }
        String word = word(primitive);
        if (word != null && OPEN_WORDS.contains(word)) {
            return OpenClosedType.OPEN;
        }
        if (word != null && CLOSED_WORDS.contains(word)) {
            return OpenClosedType.CLOSED;
        }
        return null;
    }

    static @Nullable JsonPrimitive toPrimitive(TelematicValue telematicValue) {
        JsonElement value = telematicValue.value;
        return value != null && value.isJsonPrimitive() ? value.getAsJsonPrimitive() : null;
    }

    static boolean isUndefined(JsonPrimitive primitive) {
        if (!primitive.isString()) {
            return false;
        }
        String word = word(primitive);
        return word == null || UNDEFINED_WORDS.contains(word);
    }

    /**
     * Normalizes a CarData word for comparison: upper case, without the {@code ASN_} prefix and without punctuation,
     * so that {@code ASN_selective-Locked}, {@code SELECTIVE_LOCKED} and {@code SELECTIVELOCKED} compare equal.
     *
     * @return the normalized word, or {@code null} if the value is not a string or empty
     */
    static @Nullable String word(JsonPrimitive primitive) {
        if (!primitive.isString()) {
            return null;
        }
        String word = primitive.getAsString().strip().toUpperCase(Locale.ROOT);
        if (word.startsWith("ASN_")) {
            word = word.substring(4);
        }
        word = word.replaceAll("[^A-Z0-9]", "");
        return word.isEmpty() ? null : word;
    }

    static @Nullable Unit<?> parseUnit(@Nullable String symbol) {
        if (symbol == null || symbol.isBlank()) {
            return null;
        }
        String trimmed = symbol.strip();
        Unit<?> unit = UNITS.get(trimmed.toLowerCase(Locale.ROOT));
        return unit != null ? unit : UnitUtils.parseUnit(trimmed);
    }

    /**
     * Converts a point in time. The catalogue documents e.g. {@code dd.mm.yyyy hh:mm:ss UTC} for an int32 value,
     * therefore epoch seconds, the documented formats, dates and ISO 8601 are accepted.
     */
    static @Nullable Instant toInstant(JsonPrimitive primitive) {
        if (primitive.isNumber()) {
            return Instant.ofEpochSecond(primitive.getAsLong());
        }
        String text = primitive.getAsString().strip();
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            // try the documented formats below
        }
        for (DateTimeFormatter format : DATE_TIME_FORMATS) {
            try {
                return LocalDateTime.parse(text, format).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                // try the next format
            }
        }
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(text, format).atStartOfDay().toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                // try the next format
            }
        }
        BigDecimal epochSeconds = toNumber(primitive);
        return epochSeconds == null ? null : Instant.ofEpochSecond(epochSeconds.longValue());
    }

    static @Nullable BigDecimal toNumber(JsonPrimitive primitive) {
        try {
            return primitive.isNumber() ? primitive.getAsBigDecimal() : new BigDecimal(primitive.getAsString().strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * @return the value of a JSON boolean or a boolean word such as {@code true} or {@code ASN_isTrue}, {@code null}
     *         for other values, also for words such as {@code ACTIVE} that may be one value of a longer list
     */
    static @Nullable Boolean toStrictBoolean(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        String word = word(primitive);
        if ("TRUE".equals(word) || "ISTRUE".equals(word)) {
            return Boolean.TRUE;
        }
        if ("FALSE".equals(word) || "ISFALSE".equals(word)) {
            return Boolean.FALSE;
        }
        return null;
    }

    static @Nullable Boolean toBoolean(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        String word = word(primitive);
        if (word != null && TRUE_WORDS.contains(word)) {
            return Boolean.TRUE;
        }
        if (word != null && FALSE_WORDS.contains(word)) {
            return Boolean.FALSE;
        }
        return null;
    }
}
