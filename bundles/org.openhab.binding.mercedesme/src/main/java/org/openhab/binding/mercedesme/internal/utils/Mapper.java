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
package org.openhab.binding.mercedesme.internal.utils;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Volume;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.VehicleEvents.Auxheatwarning;
import com.daimler.mbcarkit.proto.VehicleEvents.AuxheatwarningsArrayAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.BoolAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsArrayAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.ChargeProgramsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleCombustionConsumptionAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleDistanceAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleElectricityConsumptionAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoublePressureAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.DoubleSpeedAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64Attribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64ClockHourAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64DistanceAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.Int64RatioAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsArrayAttribute;
import com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePointsValue;
import com.daimler.mbcarkit.proto.VehicleEvents.VSUMetadata;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;
import com.daimler.mbcarkit.proto.VehicleEvents.VehicleStatusUpdate;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Timestamp;

/**
 * {@link Mapper} converts Mercedes keys to channel name and group and converts delivered vehicle data
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class Mapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mapper.class);

    public static final ChannelStateMap INVALID_MAP = new ChannelStateMap(EMPTY, EMPTY, UnDefType.UNDEF);
    public static final Map<String, String[]> CHANNELS = new HashMap<>();
    public static final String TIMESTAMP = "timestamp";
    public static final String VALUE = "value";

    public static Unit<Length> defaultLengthUnit = KILOMETRE_UNIT;
    public static Unit<Temperature> defaultTemperatureUnit = SIUnits.CELSIUS;
    public static Unit<Pressure> defaultPressureUnit = Units.BAR;
    public static Unit<Volume> defaultVolumeUnit = Units.LITRE;
    public static Unit<Speed> defaultSpeedUnit = SIUnits.KILOMETRE_PER_HOUR;

    public static void initialize(UnitProvider up) {
        // Configure Mapper default values
        Unit<Length> lengthUnit = up.getUnit(Length.class);
        if (ImperialUnits.FOOT.equals(lengthUnit)) {
            defaultLengthUnit = ImperialUnits.MILE;
            defaultSpeedUnit = ImperialUnits.MILES_PER_HOUR;
            defaultPressureUnit = ImperialUnits.POUND_FORCE_SQUARE_INCH;
            defaultVolumeUnit = ImperialUnits.GALLON_LIQUID_US;
        }
        Unit<Temperature> temperatureUnit = up.getUnit(Temperature.class);
        defaultTemperatureUnit = temperatureUnit;
    }

    public static ChannelStateMap getChannelStateMap(String key, VehicleAttributeStatus value) {
        if (CHANNELS.isEmpty()) {
            init();
        }
        String[] ch = CHANNELS.get(key);
        if (ch != null) {
            State state;
            UOMObserver observer = null;
            // declared here (not inside the Kilometer-values case below) so it stays in scope for the
            // Average-speed case further down, which reuses this variable - see the comment there
            Unit<?> lengthUnit = defaultLengthUnit;
            switch (key) {
                // Kilometer values
                case MB_KEY_ODO:
                case MB_KEY_RANGEELECTRIC:
                case MB_KEY_OVERALL_RANGE:
                case MB_KEY_RANGELIQUID:
                case MB_KEY_DISTANCE_START:
                case MB_KEY_DISTANCE_RESET:
                case MB_KEY_ECOSCORE_BONUS:
                    // unit lookup runs unconditionally - the vehicle can report a distance unit for an
                    // attribute even while the reading itself is currently unavailable (e.g. a BEV's
                    // liquid-consumption-style attributes still carry their unit while nil) - only the
                    // numeric value below is gated on Utils.isNil()
                    lengthUnit = defaultLengthUnit;
                    if (value.hasDistanceUnit()) {
                        observer = new UOMObserver(value.getDistanceUnit().toString());
                        Unit<?> queryUnit = observer.getUnit();
                        if (queryUnit != null) {
                            lengthUnit = queryUnit;
                        } else {
                            LOGGER.trace("No Unit found for {} - take default ", key);
                        }
                    }
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = QuantityType.valueOf(Utils.getDouble(value), lengthUnit);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // special String Value
                case MB_KEY_DRIVEN_TIME_START:
                case MB_KEY_DRIVEN_TIME_RESET:
                    int duration = Utils.getInt(value);
                    if (duration < 0) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = StringType.valueOf(Utils.getDurationString(duration));
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // KiloWatt values
                case MB_KEY_CHARGING_POWER:
                    if (Utils.isNil(value)) {
                        // don't let Math.max(0, -1) below mask an unavailable reading as "0 kW"
                        // (looks identical to "not charging")
                        state = UnDefType.UNDEF;
                    } else {
                        double power = Utils.getDouble(value);
                        state = QuantityType.valueOf(Math.max(0, power), KILOWATT_UNIT);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                case MB_KEY_AVERAGE_SPEED_START:
                case MB_KEY_AVERAGE_SPEED_RESET:
                    // unit lookup runs unconditionally, see comment on the Kilometer-values case above
                    Unit<?> speedUnit = defaultSpeedUnit;
                    if (value.hasSpeedUnit()) {
                        observer = new UOMObserver(value.getSpeedUnit().toString());
                        Unit<?> queryUnit = observer.getUnit();
                        if (queryUnit != null) {
                            lengthUnit = observer.getUnit();
                        } else {
                            LOGGER.trace("No Unit found for {} - take default ", key);
                        }
                    }
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        double speed = Utils.getDouble(value);
                        state = QuantityType.valueOf(Math.max(0, speed), speedUnit);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // KiloWatt/Hour values
                case MB_KEY_ELECTRICCONSUMPTIONSTART:
                case MB_KEY_ELECTRICCONSUMPTIONRESET:
                    // unit lookup runs unconditionally, see comment on the Kilometer-values case above
                    if (value.hasElectricityConsumptionUnit()) {
                        observer = new UOMObserver(value.getElectricityConsumptionUnit().toString());
                    } else {
                        LOGGER.trace("Don't have electric consumption unit for {}", key);
                    }
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        double consumptionEv = Utils.getDouble(value);
                        state = new DecimalType(consumptionEv);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // Litre values
                case MB_KEY_LIQUIDCONSUMPTIONSTART:
                case MB_KEY_LIQUIDCONSUMPTIONRESET:
                    // unit lookup runs unconditionally, see comment on the Kilometer-values case above -
                    // this is also the case that originally surfaced the bug: a BEV reports
                    // liquidconsumptionstart/reset as nil_value=true (no combustion engine) while still
                    // carrying combustion_consumption_unit, so the observer must not depend on isNil()
                    if (value.hasCombustionConsumptionUnit()) {
                        observer = new UOMObserver(value.getCombustionConsumptionUnit().toString());
                    }
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        double consumptionComb = Utils.getDouble(value);
                        state = new DecimalType(consumptionComb);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // Time - end of charging
                case MB_KEY_ENDOFCHARGETIME:
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        // int value is representing "minutes after Midnight!
                        Instant time = Instant.ofEpochMilli(value.getTimestampInMs());
                        long minutesAddon = Utils.getInt(value);
                        time.plus(minutesAddon, ChronoUnit.MINUTES);
                        state = Utils.getEndOfChargeTime(time.toEpochMilli(), minutesAddon);
                        if (Locale.US.getCountry().equals(Utils.getCountry())) {
                            observer = new UOMObserver(UOMObserver.TIME_US);
                        } else {
                            observer = new UOMObserver(UOMObserver.TIME_ROW);
                        }
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // DateTime - last Update
                case MB_KEY_TIRE_PRESS_MEAS_TIMESTAMP:
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = Utils.getDateTimeType(value.getTimestampInMs());
                    }
                    if (Locale.US.getCountry().equals(Utils.getCountry())) {
                        observer = new UOMObserver(UOMObserver.TIME_US);
                    } else {
                        observer = new UOMObserver(UOMObserver.TIME_ROW);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // Percentages
                case MB_KEY_SOC:
                case MB_KEY_TANKLEVELPERCENT:
                case MB_KEY_ADBLUELEVELPERCENT:
                case MB_KEY_ECOSCORE_ACCEL:
                case MB_KEY_ECOSCORE_CONSTANT:
                case MB_KEY_ECOSCORE_COASTING:
                    if (Utils.isNil(value)) {
                        // status may report NOT_RECEIVED/INVALID/NOT_AVAILABLE with a default
                        // int_value of 0 - don't forward that as a real percentage (e.g. State of
                        // Charge briefly showing 0% during charging)
                        state = UnDefType.UNDEF;
                    } else {
                        double level = Utils.getDouble(value);
                        state = QuantityType.valueOf(level, Units.PERCENT);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Contacts
                case MB_KEY_DOORSTATUSFRONTRIGHT:
                case MB_KEY_DOORSTATUSFRONTLEFT:
                case MB_KEY_DOORSTATUSREARRIGHT:
                case MB_KEY_DOORSTATUSREARLEFT:
                case MB_KEY_DECKLIDSTATUS:
                case MB_KEY_ENGINE_HOOD_STATUS:
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        // Doorstatus / Decklidstatus / EngineHoodStatus are int_value enums
                        // (CLOSED=0, OPEN=1) delivered via Mapper.putEnum() - not bool_value, so
                        // getBoolValue() would always read the default false (= CLOSED)
                        state = getContact(Utils.getInt(value) != 0);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Number Status
                case MB_KEY_DOOR_LOCK_STATUS_OVERALL:
                case MB_KEY_WINDOW_STATUS_OVERALL:
                case MB_KEY_DOOR_STATUS_OVERALL:
                case MB_KEY_IGNITIONSTATE:
                case MB_KEY_SUNROOFSTATUS:
                case MB_KEY_SUNROOF_STATUS_FRONT_BLIND:
                case MB_KEY_SUNROOF_STATUS_REAR_BLIND:
                case MB_KEY_ROOFTOPSTATUS:
                case MB_KEY_WINDOWSTATUSFRONTLEFT:
                case MB_KEY_WINDOWSTATUSFRONTRIGHT:
                case MB_KEY_WINDOWSTATUSREARLEFT:
                case MB_KEY_WINDOWSTATUSREARRIGHT:
                case MB_KEY_WINDOW_STATUS_REAR_RIGHT_BLIND:
                case MB_KEY_WINDOW_STATUS_REAR_LEFT_BLIND:
                case MB_KEY_WINDOW_STATUS_REAR_BLIND:
                case MB_KEY_FLIP_WINDOW_STATUS:
                case MB_KEY_STARTER_BATTERY_STATE:
                case MB_KEY_TIREWARNINGSRDK:
                case MB_KEY_SERVICEINTERVALDAYS:
                case MB_KEY_CHARGE_FLAP_DC_STATUS:
                case MB_KEY_CHARGE_COUPLER_AC_STATUS:
                case MB_KEY_CHARGE_COUPLER_DC_STATUS:
                case MB_KEY_CHARGE_COUPLER_DC_LOCK_STATUS:
                case MB_KEY_TIRE_SENSOR_AVAILABLE:
                case MB_KEY_CHARGE_STATUS:
                case MB_KEY_CHARGE_ERROR:
                case MB_KEY_TIRE_MARKER_FRONT_RIGHT:
                case MB_KEY_TIRE_MARKER_FRONT_LEFT:
                case MB_KEY_TIRE_MARKER_REAR_RIGHT:
                case MB_KEY_TIRE_MARKER_REAR_LEFT:
                case MB_KEY_POSITION_ERROR:
                case MB_KEY_AUXILIARY_WARNINGS:
                case MB_KEY_PRECOND_NOW_ERROR:
                    int stateNumberInteger = Utils.getInt(value);
                    if (stateNumberInteger < 0) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = new DecimalType(stateNumberInteger);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Switches
                case MB_KEY_PARKBRAKESTATUS:
                case MB_KEY_PRECOND_NOW:
                case MB_KEY_PRECOND_SEAT_FRONT_RIGHT:
                case MB_KEY_PRECOND_SEAT_FRONT_LEFT:
                case MB_KEY_PRECOND_SEAT_REAR_RIGHT:
                case MB_KEY_PRECOND_SEAT_REAR_LEFT:
                case MB_KEY_WARNINGBRAKEFLUID:
                case MB_KEY_WARNINGBRAKELININGWEAR:
                case MB_KEY_WARNINGWASHWATER:
                case MB_KEY_WARNINGCOOLANTLEVELLOW:
                case MB_KEY_WARNINGENGINELIGHT:
                case MB_KEY_CHARGINGACTIVE:
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else if (value.hasBoolValue()) {
                        state = OnOffType.from(value.getBoolValue());
                    } else if (value.hasIntValue()) {
                        // Parkbrakestatus, PrecondNow, PrecondSeat and Warningwashwater are binary 0/1
                        // enums delivered via Mapper.putEnum() (int_value oneof, not bool_value) - proto
                        // declares 0 as the "off"/inactive/not-engaged member in all four, see
                        // vehicle-events.proto
                        state = OnOffType.from(value.getIntValue() != 0);
                    } else {
                        state = UnDefType.UNDEF;
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Switches - lock values with reversed boolean interpretation
                case MB_KEY_DOORLOCKSTATUSFRONTRIGHT:
                case MB_KEY_DOORLOCKSTATUSFRONTLEFT:
                case MB_KEY_DOORLOCKSTATUSREARRIGHT:
                case MB_KEY_DOORLOCKSTATUSREARLEFT:
                case MB_KEY_DOORLOCKSTATUSDECKLID:
                case MB_KEY_DOORLOCKSTATUSGAS:
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        // Doorlockstatus is an int_value enum (LOCKED=0, UNLOCKED=1) delivered via
                        // Mapper.putEnum() - not bool_value, so getBoolValue() would always read the
                        // default false.
                        state = OnOffType.from(Utils.getInt(value) == 0);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Angle
                case MB_KEY_POSITION_HEADING:
                    double heading = Utils.getDouble(value);
                    if (heading < 0) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = QuantityType.valueOf(heading, Units.DEGREE_ANGLE);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // tires
                case MB_KEY_TIREPRESSURE_FRONT_LEFT:
                case MB_KEY_TIREPRESSURE_FRONT_RIGHT:
                case MB_KEY_TIREPRESSURE_REAR_LEFT:
                case MB_KEY_TIREPRESSURE_REAR_RIGHT:
                    // unit lookup runs unconditionally, see comment on the Kilometer-values case above
                    Unit<?> pressureUnit = defaultPressureUnit;
                    if (value.hasPressureUnit()) {
                        observer = new UOMObserver(value.getPressureUnit().toString());
                        Unit<?> queryUnit = observer.getUnit();
                        if (queryUnit != null) {
                            pressureUnit = queryUnit;
                        } else {
                            LOGGER.trace("No Unit found for {} - take default ", key);
                        }
                    }
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        double pressure = Utils.getDouble(value);
                        state = QuantityType.valueOf(pressure, pressureUnit);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);
                default:
                    break;
            }
        }
        return INVALID_MAP;
    }

    /**
     * Converts a {@link VehicleStatusUpdate} (the typed push format, added in app version 165-1) into a
     * {@code Map<String, VehicleAttributeStatus>} keyed by the same {@code MB_KEY_*} constants the old
     * {@code VEPUpdate} format used, so it feeds directly into
     * {@link #getChannelStateMap(String, VehicleAttributeStatus)}
     * without any behavior change there.
     * <p>
     * Only the fields with a direct equivalent in the old attribute set are converted; the complex array-typed
     * fields (temperature points, charge programs, auxiliary warnings) and the fields with no old channel are
     * left out.
     * <p>
     * Enum-typed fields are converted via {@code getValueValue()}, the raw number declared for that value in the
     * {@code .proto} source (e.g. {@code IGNITIONSTATE_ON = 4;}) - not a positional/ordinal guess. Since these
     * are the same numeric codes the server always sent for {@code int_value} in the old format (verified against
     * {@code vehicle-events.proto} directly), no extra name-to-code lookup table is needed here.
     *
     * @param vsu the typed status update for a single VIN
     * @return a key-to-attribute map ready for {@link #getChannelStateMap(String, VehicleAttributeStatus)}
     */
    public static Map<String, VehicleAttributeStatus> fromVehicleStatusUpdate(VehicleStatusUpdate vsu) {
        Map<String, VehicleAttributeStatus> attributes = new HashMap<>();

        // Every put*() call below is gated on vsu.hasXxx() - a partial (delta) update only ever sets the
        // handful of fields that actually changed, and every field here is a singular message type, so
        // proto3 gives each one a real hasXxx() presence check. Without this gate, an absent field's
        // getter returns its default instance (value 0, unset metadata -> status defaults to
        // AttributeStatus.VALUE_VALID), which Utils.isNil() cannot distinguish from genuine data.

        // bool
        if (vsu.hasWarningbrakefluid()) {
            putBool(attributes, MB_KEY_WARNINGBRAKEFLUID, vsu.getWarningbrakefluid());
        }
        if (vsu.hasWarningbrakeliningwear()) {
            putBool(attributes, MB_KEY_WARNINGBRAKELININGWEAR, vsu.getWarningbrakeliningwear());
        }
        if (vsu.hasWarningcoolantlevellow()) {
            putBool(attributes, MB_KEY_WARNINGCOOLANTLEVELLOW, vsu.getWarningcoolantlevellow());
        }
        if (vsu.hasWarningenginelight()) {
            putBool(attributes, MB_KEY_WARNINGENGINELIGHT, vsu.getWarningenginelight());
        }
        if (vsu.hasChargingactive()) {
            putBool(attributes, MB_KEY_CHARGINGACTIVE, vsu.getChargingactive());
        }

        // plain int64 / double (no unit)
        if (vsu.hasServiceintervaldays()) {
            putInt64(attributes, MB_KEY_SERVICEINTERVALDAYS, vsu.getServiceintervaldays());
        }
        if (vsu.hasTirePressMeasTimestamp()) {
            putInt64(attributes, MB_KEY_TIRE_PRESS_MEAS_TIMESTAMP, vsu.getTirePressMeasTimestamp());
        }
        if (vsu.hasDrivenTimeReset()) {
            putInt64(attributes, MB_KEY_DRIVEN_TIME_RESET, vsu.getDrivenTimeReset());
        }
        if (vsu.hasDrivenTimeStart()) {
            putInt64(attributes, MB_KEY_DRIVEN_TIME_START, vsu.getDrivenTimeStart());
        }
        if (vsu.hasPositionHeading()) {
            putDouble(attributes, MB_KEY_POSITION_HEADING, vsu.getPositionHeading());
        }
        if (vsu.hasChargingPower()) {
            putDouble(attributes, MB_KEY_CHARGING_POWER, vsu.getChargingPower());
        }
        if (vsu.hasPositionLong()) {
            putDouble(attributes, MB_KEY_POSITION_LONG, vsu.getPositionLong());
        }
        if (vsu.hasPositionLat()) {
            putDouble(attributes, MB_KEY_POSITION_LAT, vsu.getPositionLat());
        }

        // enum-typed status attributes -> int_value = proto-declared enum number
        if (vsu.hasTireSensorAvailable()) {
            putEnum(attributes, MB_KEY_TIRE_SENSOR_AVAILABLE, vsu.getTireSensorAvailable().getValueValue(),
                    vsu.getTireSensorAvailable().getMetadata());
        }
        if (vsu.hasChargeCouplerDCLockStatus()) {
            putEnum(attributes, MB_KEY_CHARGE_COUPLER_DC_LOCK_STATUS,
                    vsu.getChargeCouplerDCLockStatus().getValueValue(),
                    vsu.getChargeCouplerDCLockStatus().getMetadata());
        }
        if (vsu.hasChargeCouplerDCStatus()) {
            putEnum(attributes, MB_KEY_CHARGE_COUPLER_DC_STATUS, vsu.getChargeCouplerDCStatus().getValueValue(),
                    vsu.getChargeCouplerDCStatus().getMetadata());
        }
        if (vsu.hasChargeCouplerACStatus()) {
            putEnum(attributes, MB_KEY_CHARGE_COUPLER_AC_STATUS, vsu.getChargeCouplerACStatus().getValueValue(),
                    vsu.getChargeCouplerACStatus().getMetadata());
        }
        if (vsu.hasChargeFlapDCStatus()) {
            putEnum(attributes, MB_KEY_CHARGE_FLAP_DC_STATUS, vsu.getChargeFlapDCStatus().getValueValue(),
                    vsu.getChargeFlapDCStatus().getMetadata());
        }
        if (vsu.hasChargingstatus()) {
            putEnum(attributes, MB_KEY_CHARGE_STATUS, vsu.getChargingstatus().getValueValue(),
                    vsu.getChargingstatus().getMetadata());
        }
        if (vsu.hasChargingErrorDetails()) {
            putEnum(attributes, MB_KEY_CHARGE_ERROR, vsu.getChargingErrorDetails().getValueValue(),
                    vsu.getChargingErrorDetails().getMetadata());
        }
        if (vsu.hasTirewarningsrdk()) {
            putEnum(attributes, MB_KEY_TIREWARNINGSRDK, vsu.getTirewarningsrdk().getValueValue(),
                    vsu.getTirewarningsrdk().getMetadata());
        }
        if (vsu.hasStarterBatteryState()) {
            putEnum(attributes, MB_KEY_STARTER_BATTERY_STATE, vsu.getStarterBatteryState().getValueValue(),
                    vsu.getStarterBatteryState().getMetadata());
        }
        if (vsu.hasFlipWindowStatus()) {
            putEnum(attributes, MB_KEY_FLIP_WINDOW_STATUS, vsu.getFlipWindowStatus().getValueValue(),
                    vsu.getFlipWindowStatus().getMetadata());
        }
        if (vsu.hasWindowStatusRearBlind()) {
            putEnum(attributes, MB_KEY_WINDOW_STATUS_REAR_BLIND, vsu.getWindowStatusRearBlind().getValueValue(),
                    vsu.getWindowStatusRearBlind().getMetadata());
        }
        if (vsu.hasWindowStatusRearLeftBlind()) {
            putEnum(attributes, MB_KEY_WINDOW_STATUS_REAR_LEFT_BLIND,
                    vsu.getWindowStatusRearLeftBlind().getValueValue(),
                    vsu.getWindowStatusRearLeftBlind().getMetadata());
        }
        if (vsu.hasWindowStatusRearRightBlind()) {
            putEnum(attributes, MB_KEY_WINDOW_STATUS_REAR_RIGHT_BLIND,
                    vsu.getWindowStatusRearRightBlind().getValueValue(),
                    vsu.getWindowStatusRearRightBlind().getMetadata());
        }
        if (vsu.hasWindowstatusrearright()) {
            putEnum(attributes, MB_KEY_WINDOWSTATUSREARRIGHT, vsu.getWindowstatusrearright().getValueValue(),
                    vsu.getWindowstatusrearright().getMetadata());
        }
        if (vsu.hasWindowstatusrearleft()) {
            putEnum(attributes, MB_KEY_WINDOWSTATUSREARLEFT, vsu.getWindowstatusrearleft().getValueValue(),
                    vsu.getWindowstatusrearleft().getMetadata());
        }
        if (vsu.hasWindowstatusfrontright()) {
            putEnum(attributes, MB_KEY_WINDOWSTATUSFRONTRIGHT, vsu.getWindowstatusfrontright().getValueValue(),
                    vsu.getWindowstatusfrontright().getMetadata());
        }
        if (vsu.hasWindowstatusfrontleft()) {
            putEnum(attributes, MB_KEY_WINDOWSTATUSFRONTLEFT, vsu.getWindowstatusfrontleft().getValueValue(),
                    vsu.getWindowstatusfrontleft().getMetadata());
        }
        if (vsu.hasRooftopstatus()) {
            putEnum(attributes, MB_KEY_ROOFTOPSTATUS, vsu.getRooftopstatus().getValueValue(),
                    vsu.getRooftopstatus().getMetadata());
        }
        if (vsu.hasSunroofStatusRearBlind()) {
            putEnum(attributes, MB_KEY_SUNROOF_STATUS_REAR_BLIND, vsu.getSunroofStatusRearBlind().getValueValue(),
                    vsu.getSunroofStatusRearBlind().getMetadata());
        }
        if (vsu.hasSunroofStatusFrontBlind()) {
            putEnum(attributes, MB_KEY_SUNROOF_STATUS_FRONT_BLIND, vsu.getSunroofStatusFrontBlind().getValueValue(),
                    vsu.getSunroofStatusFrontBlind().getMetadata());
        }
        if (vsu.hasSunroofstatus()) {
            putEnum(attributes, MB_KEY_SUNROOFSTATUS, vsu.getSunroofstatus().getValueValue(),
                    vsu.getSunroofstatus().getMetadata());
        }
        if (vsu.hasIgnitionstate()) {
            putEnum(attributes, MB_KEY_IGNITIONSTATE, vsu.getIgnitionstate().getValueValue(),
                    vsu.getIgnitionstate().getMetadata());
        }
        if (vsu.hasDoorStatusOverall()) {
            putEnum(attributes, MB_KEY_DOOR_STATUS_OVERALL, vsu.getDoorStatusOverall().getValueValue(),
                    vsu.getDoorStatusOverall().getMetadata());
        }
        if (vsu.hasWindowStatusOverall()) {
            putEnum(attributes, MB_KEY_WINDOW_STATUS_OVERALL, vsu.getWindowStatusOverall().getValueValue(),
                    vsu.getWindowStatusOverall().getMetadata());
        }
        if (vsu.hasDoorlockstatusvehicle()) {
            putEnum(attributes, MB_KEY_DOOR_LOCK_STATUS_OVERALL, vsu.getDoorlockstatusvehicle().getValueValue(),
                    vsu.getDoorlockstatusvehicle().getMetadata());
        }
        if (vsu.hasTireMarkerFrontRight()) {
            putEnum(attributes, MB_KEY_TIRE_MARKER_FRONT_RIGHT, vsu.getTireMarkerFrontRight().getValueValue(),
                    vsu.getTireMarkerFrontRight().getMetadata());
        }
        if (vsu.hasTireMarkerFrontLeft()) {
            putEnum(attributes, MB_KEY_TIRE_MARKER_FRONT_LEFT, vsu.getTireMarkerFrontLeft().getValueValue(),
                    vsu.getTireMarkerFrontLeft().getMetadata());
        }
        if (vsu.hasTireMarkerRearRight()) {
            putEnum(attributes, MB_KEY_TIRE_MARKER_REAR_RIGHT, vsu.getTireMarkerRearRight().getValueValue(),
                    vsu.getTireMarkerRearRight().getMetadata());
        }
        if (vsu.hasTireMarkerRearLeft()) {
            putEnum(attributes, MB_KEY_TIRE_MARKER_REAR_LEFT, vsu.getTireMarkerRearLeft().getValueValue(),
                    vsu.getTireMarkerRearLeft().getMetadata());
        }
        if (vsu.hasParkbrakestatus()) {
            putEnum(attributes, MB_KEY_PARKBRAKESTATUS, vsu.getParkbrakestatus().getValueValue(),
                    vsu.getParkbrakestatus().getMetadata());
        }
        if (vsu.hasPrecondNow()) {
            putEnum(attributes, MB_KEY_PRECOND_NOW, vsu.getPrecondNow().getValueValue(),
                    vsu.getPrecondNow().getMetadata());
        }
        if (vsu.hasPrecondSeatFrontRight()) {
            putEnum(attributes, MB_KEY_PRECOND_SEAT_FRONT_RIGHT, vsu.getPrecondSeatFrontRight().getValueValue(),
                    vsu.getPrecondSeatFrontRight().getMetadata());
        }
        if (vsu.hasPrecondSeatFrontLeft()) {
            putEnum(attributes, MB_KEY_PRECOND_SEAT_FRONT_LEFT, vsu.getPrecondSeatFrontLeft().getValueValue(),
                    vsu.getPrecondSeatFrontLeft().getMetadata());
        }
        if (vsu.hasPrecondSeatRearRight()) {
            putEnum(attributes, MB_KEY_PRECOND_SEAT_REAR_RIGHT, vsu.getPrecondSeatRearRight().getValueValue(),
                    vsu.getPrecondSeatRearRight().getMetadata());
        }
        if (vsu.hasPrecondSeatRearLeft()) {
            putEnum(attributes, MB_KEY_PRECOND_SEAT_REAR_LEFT, vsu.getPrecondSeatRearLeft().getValueValue(),
                    vsu.getPrecondSeatRearLeft().getMetadata());
        }
        if (vsu.hasWarningwashwater()) {
            putEnum(attributes, MB_KEY_WARNINGWASHWATER, vsu.getWarningwashwater().getValueValue(),
                    vsu.getWarningwashwater().getMetadata());
        }
        if (vsu.hasDoorlockstatusfrontright()) {
            putEnum(attributes, MB_KEY_DOORLOCKSTATUSFRONTRIGHT, vsu.getDoorlockstatusfrontright().getValueValue(),
                    vsu.getDoorlockstatusfrontright().getMetadata());
        }
        if (vsu.hasDoorlockstatusfrontleft()) {
            putEnum(attributes, MB_KEY_DOORLOCKSTATUSFRONTLEFT, vsu.getDoorlockstatusfrontleft().getValueValue(),
                    vsu.getDoorlockstatusfrontleft().getMetadata());
        }
        if (vsu.hasDoorlockstatusrearright()) {
            putEnum(attributes, MB_KEY_DOORLOCKSTATUSREARRIGHT, vsu.getDoorlockstatusrearright().getValueValue(),
                    vsu.getDoorlockstatusrearright().getMetadata());
        }
        if (vsu.hasDoorlockstatusrearleft()) {
            putEnum(attributes, MB_KEY_DOORLOCKSTATUSREARLEFT, vsu.getDoorlockstatusrearleft().getValueValue(),
                    vsu.getDoorlockstatusrearleft().getMetadata());
        }
        if (vsu.hasDoorlockstatusdecklid()) {
            putEnum(attributes, MB_KEY_DOORLOCKSTATUSDECKLID, vsu.getDoorlockstatusdecklid().getValueValue(),
                    vsu.getDoorlockstatusdecklid().getMetadata());
        }
        if (vsu.hasDoorlockstatusgas()) {
            putEnum(attributes, MB_KEY_DOORLOCKSTATUSGAS, vsu.getDoorlockstatusgas().getValueValue(),
                    vsu.getDoorlockstatusgas().getMetadata());
        }
        if (vsu.hasEngineHoodStatus()) {
            putEnum(attributes, MB_KEY_ENGINE_HOOD_STATUS, vsu.getEngineHoodStatus().getValueValue(),
                    vsu.getEngineHoodStatus().getMetadata());
        }
        if (vsu.hasDecklidstatus()) {
            putEnum(attributes, MB_KEY_DECKLIDSTATUS, vsu.getDecklidstatus().getValueValue(),
                    vsu.getDecklidstatus().getMetadata());
        }
        if (vsu.hasDoorstatusrearleft()) {
            putEnum(attributes, MB_KEY_DOORSTATUSREARLEFT, vsu.getDoorstatusrearleft().getValueValue(),
                    vsu.getDoorstatusrearleft().getMetadata());
        }
        if (vsu.hasDoorstatusrearright()) {
            putEnum(attributes, MB_KEY_DOORSTATUSREARRIGHT, vsu.getDoorstatusrearright().getValueValue(),
                    vsu.getDoorstatusrearright().getMetadata());
        }
        if (vsu.hasDoorstatusfrontleft()) {
            putEnum(attributes, MB_KEY_DOORSTATUSFRONTLEFT, vsu.getDoorstatusfrontleft().getValueValue(),
                    vsu.getDoorstatusfrontleft().getMetadata());
        }
        if (vsu.hasDoorstatusfrontright()) {
            putEnum(attributes, MB_KEY_DOORSTATUSFRONTRIGHT, vsu.getDoorstatusfrontright().getValueValue(),
                    vsu.getDoorstatusfrontright().getMetadata());
        }
        if (vsu.hasEndofChargeTimeWeekday()) {
            putEnum(attributes, MB_KEY_ENDOFCHARGEDAY, vsu.getEndofChargeTimeWeekday().getValueValue(),
                    vsu.getEndofChargeTimeWeekday().getMetadata());
        }
        if (vsu.hasSelectedChargeProgram()) {
            putEnum(attributes, MB_KEY_SELECTED_CHARGE_PROGRAM, vsu.getSelectedChargeProgram().getValueValue(),
                    vsu.getSelectedChargeProgram().getMetadata());
        }
        if (vsu.hasVehiclePositionErrorCode()) {
            putEnum(attributes, MB_KEY_POSITION_ERROR, vsu.getVehiclePositionErrorCode().getValueValue(),
                    vsu.getVehiclePositionErrorCode().getMetadata());
        }
        if (vsu.hasPrecondNowError()) {
            putEnum(attributes, MB_KEY_PRECOND_NOW_ERROR, vsu.getPrecondNowError().getValueValue(),
                    vsu.getPrecondNowError().getMetadata());
        }

        // distance (int64 / double, with unit + display value)
        if (vsu.hasRangeliquid()) {
            putInt64Distance(attributes, MB_KEY_RANGELIQUID, vsu.getRangeliquid());
        }
        if (vsu.hasRangeelectric()) {
            putInt64Distance(attributes, MB_KEY_RANGEELECTRIC, vsu.getRangeelectric());
        }
        if (vsu.hasOdo()) {
            putInt64Distance(attributes, MB_KEY_ODO, vsu.getOdo());
        }
        if (vsu.hasDistanceReset()) {
            putDoubleDistance(attributes, MB_KEY_DISTANCE_RESET, vsu.getDistanceReset());
        }
        if (vsu.hasDistanceStart()) {
            putDoubleDistance(attributes, MB_KEY_DISTANCE_START, vsu.getDistanceStart());
        }
        if (vsu.hasOverallRange()) {
            putDoubleDistance(attributes, MB_KEY_OVERALL_RANGE, vsu.getOverallRange());
        }
        if (vsu.hasEcoscorebonusrange()) {
            putDoubleDistance(attributes, MB_KEY_ECOSCORE_BONUS, vsu.getEcoscorebonusrange());
        }

        // pressure
        if (vsu.hasTirepressureFrontLeft()) {
            putPressure(attributes, MB_KEY_TIREPRESSURE_FRONT_LEFT, vsu.getTirepressureFrontLeft());
        }
        if (vsu.hasTirepressureFrontRight()) {
            putPressure(attributes, MB_KEY_TIREPRESSURE_FRONT_RIGHT, vsu.getTirepressureFrontRight());
        }
        if (vsu.hasTirepressureRearLeft()) {
            putPressure(attributes, MB_KEY_TIREPRESSURE_REAR_LEFT, vsu.getTirepressureRearLeft());
        }
        if (vsu.hasTirepressureRearRight()) {
            putPressure(attributes, MB_KEY_TIREPRESSURE_REAR_RIGHT, vsu.getTirepressureRearRight());
        }

        // speed
        if (vsu.hasAverageSpeedReset()) {
            putSpeed(attributes, MB_KEY_AVERAGE_SPEED_RESET, vsu.getAverageSpeedReset());
        }
        if (vsu.hasAverageSpeedStart()) {
            putSpeed(attributes, MB_KEY_AVERAGE_SPEED_START, vsu.getAverageSpeedStart());
        }

        // ratio (percent)
        if (vsu.hasTanklevelpercent()) {
            putRatio(attributes, MB_KEY_TANKLEVELPERCENT, vsu.getTanklevelpercent());
        }
        if (vsu.hasTankLevelAdBlue()) {
            putRatio(attributes, MB_KEY_ADBLUELEVELPERCENT, vsu.getTankLevelAdBlue());
        }
        if (vsu.hasSoc()) {
            putRatio(attributes, MB_KEY_SOC, vsu.getSoc());
        }
        if (vsu.hasMaxSoc()) {
            putRatio(attributes, MB_KEY_MAX_SOC, vsu.getMaxSoc());
        }
        if (vsu.hasMaxSocLowerLimit()) {
            putRatio(attributes, MB_KEY_MAX_SOC_LOWER_LIMIT, vsu.getMaxSocLowerLimit());
        }
        if (vsu.hasMaxSocUpperLimit()) {
            putRatio(attributes, MB_KEY_MAX_SOC_UPPER_LIMIT, vsu.getMaxSocUpperLimit());
        }
        if (vsu.hasEcoscoreaccel()) {
            putRatio(attributes, MB_KEY_ECOSCORE_ACCEL, vsu.getEcoscoreaccel());
        }
        if (vsu.hasEcoscoreconst()) {
            putRatio(attributes, MB_KEY_ECOSCORE_CONSTANT, vsu.getEcoscoreconst());
        }
        if (vsu.hasEcoscorefreewhl()) {
            putRatio(attributes, MB_KEY_ECOSCORE_COASTING, vsu.getEcoscorefreewhl());
        }

        // clock hour
        if (vsu.hasEndofchargetime()) {
            putClockHour(attributes, MB_KEY_ENDOFCHARGETIME, vsu.getEndofchargetime());
        }

        // consumption
        if (vsu.hasLiquidconsumptionreset()) {
            putCombustionConsumption(attributes, MB_KEY_LIQUIDCONSUMPTIONRESET, vsu.getLiquidconsumptionreset());
        }
        if (vsu.hasLiquidconsumptionstart()) {
            putCombustionConsumption(attributes, MB_KEY_LIQUIDCONSUMPTIONSTART, vsu.getLiquidconsumptionstart());
        }
        if (vsu.hasElectricconsumptionreset()) {
            putElectricityConsumption(attributes, MB_KEY_ELECTRICCONSUMPTIONRESET, vsu.getElectricconsumptionreset());
        }
        if (vsu.hasElectricconsumptionstart()) {
            putElectricityConsumption(attributes, MB_KEY_ELECTRICCONSUMPTIONSTART, vsu.getElectricconsumptionstart());
        }

        // complex array-typed fields - analyzed from live debug logs
        if (vsu.hasTemperaturePoints()) {
            putTemperaturePoints(attributes, MB_KEY_TEMPERATURE_POINTS, vsu.getTemperaturePoints());
        }
        if (vsu.hasChargePrograms()) {
            putChargePrograms(attributes, MB_KEY_CHARGE_PROGRAMS, vsu.getChargePrograms());
        }
        if (vsu.hasAuxheatwarnings()) {
            putAuxheatwarnings(attributes, MB_KEY_AUXILIARY_WARNINGS, vsu.getAuxheatwarnings());
        }

        return attributes;
    }

    private static long toMillis(VSUMetadata metadata) {
        if (!metadata.hasTimestamp()) {
            return 0;
        }
        Timestamp ts = metadata.getTimestamp();
        return ts.getSeconds() * 1000 + ts.getNanos() / 1_000_000;
    }

    private static VehicleAttributeStatus.Builder baseBuilder(VSUMetadata metadata) {
        return VehicleAttributeStatus.newBuilder().setStatus(metadata.getStatusValue())
                .setTimestampInMs(toMillis(metadata));
    }

    private static void putBool(Map<String, VehicleAttributeStatus> map, String key, BoolAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setBoolValue(a.getValue()).build());
    }

    private static void putInt64(Map<String, VehicleAttributeStatus> map, String key, Int64Attribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).build());
    }

    private static void putDouble(Map<String, VehicleAttributeStatus> map, String key, DoubleAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).build());
    }

    private static void putEnum(Map<String, VehicleAttributeStatus> map, String key, int enumValue,
            VSUMetadata metadata) {
        map.put(key, baseBuilder(metadata).setIntValue(enumValue).build());
    }

    private static void putInt64Distance(Map<String, VehicleAttributeStatus> map, String key,
            Int64DistanceAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).setDistanceUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putDoubleDistance(Map<String, VehicleAttributeStatus> map, String key,
            DoubleDistanceAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setDistanceUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putPressure(Map<String, VehicleAttributeStatus> map, String key, DoublePressureAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setPressureUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putSpeed(Map<String, VehicleAttributeStatus> map, String key, DoubleSpeedAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setSpeedUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putRatio(Map<String, VehicleAttributeStatus> map, String key, Int64RatioAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).setRatioUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putClockHour(Map<String, VehicleAttributeStatus> map, String key, Int64ClockHourAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(a.getValue()).setClockHourUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putCombustionConsumption(Map<String, VehicleAttributeStatus> map, String key,
            DoubleCombustionConsumptionAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue()).setCombustionConsumptionUnit(a.getUnit())
                .setDisplayValue(a.getDisplayValue()).build());
    }

    private static void putElectricityConsumption(Map<String, VehicleAttributeStatus> map, String key,
            DoubleElectricityConsumptionAttribute a) {
        map.put(key, baseBuilder(a.getMetadata()).setDoubleValue(a.getValue())
                .setElectricityConsumptionUnit(a.getUnit()).setDisplayValue(a.getDisplayValue()).build());
    }

    /**
     * temperature_points: VehicleHandler reads {@code MB_KEY_TEMPERATURE_POINTS} directly via
     * {@code value.getTemperaturePointsValue()} (not through
     * {@link #getChannelStateMap(String, VehicleAttributeStatus)}),
     * so this builds the old {@link TemperaturePointsValue} oneof case. The new
     * {@code TemperaturePointsArrayAttribute.TemperaturePoint} entries carry the same information as the old
     * {@link com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePoint} (zone, temperature, active) but with a
     * 0-based {@code Zone} enum instead of a string and the temperature wrapped in a
     * {@code DoubleTemperatureAttribute} instead of a raw double - both confirmed against a live debug dump.
     */
    private static void putTemperaturePoints(Map<String, VehicleAttributeStatus> map, String key,
            TemperaturePointsArrayAttribute a) {
        TemperaturePointsValue.Builder valueBuilder = TemperaturePointsValue.newBuilder();
        a.getValueList().forEach(point -> {
            valueBuilder.addTemperaturePoints(com.daimler.mbcarkit.proto.VehicleEvents.TemperaturePoint.newBuilder()
                    .setZone(zoneToLegacyString(point.getZone())).setTemperature(point.getTemperature().getValue())
                    .setTemperatureDisplayValue(point.getTemperature().getDisplayValue())
                    .setActive(BoolValue.newBuilder().setValue(point.getActive()).build()).build());
        });
        VehicleAttributeStatus.Builder statusBuilder = baseBuilder(a.getMetadata())
                .setTemperaturePointsValue(valueBuilder.build());
        // VehicleHandler reads the unit from the outer VehicleAttributeStatus.temperature_unit (one shared unit
        // for all zones), while the new format carries a unit per point (DoubleTemperatureAttribute.unit) - take
        // the first point's unit as the shared one, which is what the old format assumed anyway.
        if (!a.getValueList().isEmpty()) {
            statusBuilder.setTemperatureUnit(a.getValueList().get(0).getTemperature().getUnit());
        }
        map.put(key, statusBuilder.build());
    }

    private static String zoneToLegacyString(TemperaturePointsArrayAttribute.TemperaturePoint.Zone zone) {
        switch (zone) {
            case FRONT_LEFT:
                return "frontLeft";
            case FRONT_CENTER:
                return "frontCenter";
            case FRONT_RIGHT:
                return "frontRight";
            case REAR_LEFT:
                return "rearLeft";
            case REAR_CENTER:
                return "rearCenter";
            case REAR_RIGHT:
                return "rearRight";
            case REAR_2_LEFT:
                return "rear2Left";
            case REAR_2_CENTER:
                return "rear2Center";
            case REAR_2_RIGHT:
                return "rear2Right";
            default:
                LOGGER.trace("Unmapped temperature point zone {}", zone);
                return zone.toString();
        }
    }

    /**
     * charge_programs: VehicleHandler reads {@code MB_KEY_CHARGE_PROGRAMS} directly via
     * {@code value.getChargeProgramsValue()}. The new {@code ChargeProgramsArrayAttribute} reuses the very same
     * {@code ChargeProgramParameters} message the old {@link ChargeProgramsValue} wraps (confirmed in
     * vehicle-events.proto: both declare {@code repeated ChargeProgramParameters}), so this is a direct
     * passthrough, no field-by-field conversion needed.
     */
    private static void putChargePrograms(Map<String, VehicleAttributeStatus> map, String key,
            ChargeProgramsArrayAttribute a) {
        ChargeProgramsValue value = ChargeProgramsValue.newBuilder().addAllChargeProgramParameters(a.getValueList())
                .build();
        map.put(key, baseBuilder(a.getMetadata()).setChargeProgramsValue(value).build());
    }

    /**
     * auxheatwarnings: the old format delivered a single warning code as {@code int_value} (Mapper's
     * "Number Status" case group). The new {@code Auxheatwarnings} enum (NONE=0, CONFIRMATION=1,
     * CONFIRMATION_2=2, higher = more severe) is now a repeated field, so several warnings can be active at
     * once - this takes the most severe (highest) one, or 0 (= NONE) if the list is empty, to keep the single
     * Number channel meaningful. Not yet confirmed against a live vehicle with an actual warning active (the
     * live debug dump only showed an empty list with status VALUE_NOT_AVAILABLE) - worth rechecking once a
     * real auxiliary heater warning occurs.
     */
    private static void putAuxheatwarnings(Map<String, VehicleAttributeStatus> map, String key,
            AuxheatwarningsArrayAttribute a) {
        int worst = a.getValueList().stream().mapToInt(Auxheatwarning::getNumber).max().orElse(0);
        map.put(key, baseBuilder(a.getMetadata()).setIntValue(worst).build());
    }

    private static State getContact(boolean b) {
        if (!b) {
            return OpenClosedType.CLOSED;
        } else {
            return OpenClosedType.OPEN;
        }
    }

    /**
     * Mapping of json id towards channel group and id
     */
    private static void init() {
        CHANNELS.put(MB_KEY_DOOR_LOCK_STATUS_OVERALL, new String[] { OH_CHANNEL_LOCK, GROUP_VEHICLE });
        CHANNELS.put(MB_KEY_WINDOW_STATUS_OVERALL, new String[] { OH_CHANNEL_WINDOWS, GROUP_VEHICLE });
        CHANNELS.put(MB_KEY_DOOR_STATUS_OVERALL, new String[] { OH_CHANNEL_DOOR_STATUS, GROUP_VEHICLE });
        CHANNELS.put(MB_KEY_IGNITIONSTATE, new String[] { OH_CHANNEL_IGNITION, GROUP_VEHICLE });
        CHANNELS.put(MB_KEY_PARKBRAKESTATUS, new String[] { OH_CHANNEL_PARK_BRAKE, GROUP_VEHICLE });

        CHANNELS.put(MB_KEY_DOORSTATUSFRONTRIGHT, new String[] { OH_CHANNEL_FRONT_RIGHT, GROUP_DOORS });
        CHANNELS.put(MB_KEY_DOORSTATUSFRONTLEFT, new String[] { OH_CHANNEL_FRONT_LEFT, GROUP_DOORS });
        CHANNELS.put(MB_KEY_DOORSTATUSREARRIGHT, new String[] { OH_CHANNEL_REAR_RIGHT, GROUP_DOORS });
        CHANNELS.put(MB_KEY_DOORSTATUSREARLEFT, new String[] { OH_CHANNEL_REAR_LEFT, GROUP_DOORS });
        CHANNELS.put(MB_KEY_DECKLIDSTATUS, new String[] { OH_CHANNEL_DECK_LID, GROUP_DOORS });
        CHANNELS.put(MB_KEY_ENGINE_HOOD_STATUS, new String[] { OH_CHANNEL_ENGINE_HOOD, GROUP_DOORS });
        CHANNELS.put(MB_KEY_SUNROOFSTATUS, new String[] { OH_CHANNEL_SUNROOF, GROUP_DOORS });
        CHANNELS.put(MB_KEY_SUNROOF_STATUS_FRONT_BLIND, new String[] { OH_CHANNEL_SUNROOF_FRONT_BLIND, GROUP_DOORS });
        CHANNELS.put(MB_KEY_SUNROOF_STATUS_REAR_BLIND, new String[] { OH_CHANNEL_SUNROOF_REAR_BLIND, GROUP_DOORS });
        CHANNELS.put(MB_KEY_ROOFTOPSTATUS, new String[] { OH_CHANNEL_ROOFTOP, GROUP_DOORS });

        CHANNELS.put(MB_KEY_DOORLOCKSTATUSFRONTRIGHT, new String[] { OH_CHANNEL_FRONT_RIGHT, GROUP_LOCK });
        CHANNELS.put(MB_KEY_DOORLOCKSTATUSFRONTLEFT, new String[] { OH_CHANNEL_FRONT_LEFT, GROUP_LOCK });
        CHANNELS.put(MB_KEY_DOORLOCKSTATUSREARRIGHT, new String[] { OH_CHANNEL_REAR_RIGHT, GROUP_LOCK });
        CHANNELS.put(MB_KEY_DOORLOCKSTATUSREARLEFT, new String[] { OH_CHANNEL_REAR_LEFT, GROUP_LOCK });
        CHANNELS.put(MB_KEY_DOORLOCKSTATUSDECKLID, new String[] { OH_CHANNEL_DECK_LID, GROUP_LOCK });
        CHANNELS.put(MB_KEY_DOORLOCKSTATUSGAS, new String[] { OH_CHANNEL_GAS_FLAP, GROUP_LOCK });

        CHANNELS.put(MB_KEY_WINDOWSTATUSFRONTLEFT, new String[] { OH_CHANNEL_FRONT_LEFT, GROUP_WINDOWS });
        CHANNELS.put(MB_KEY_WINDOWSTATUSFRONTRIGHT, new String[] { OH_CHANNEL_FRONT_RIGHT, GROUP_WINDOWS });
        CHANNELS.put(MB_KEY_WINDOWSTATUSREARLEFT, new String[] { OH_CHANNEL_REAR_LEFT, GROUP_WINDOWS });
        CHANNELS.put(MB_KEY_WINDOWSTATUSREARRIGHT, new String[] { OH_CHANNEL_REAR_RIGHT, GROUP_WINDOWS });
        CHANNELS.put(MB_KEY_WINDOW_STATUS_REAR_RIGHT_BLIND,
                new String[] { OH_CHANNEL_REAR_RIGHT_BLIND, GROUP_WINDOWS });
        CHANNELS.put(MB_KEY_WINDOW_STATUS_REAR_LEFT_BLIND, new String[] { OH_CHANNEL_REAR_LEFT_BLIND, GROUP_WINDOWS });
        CHANNELS.put(MB_KEY_WINDOW_STATUS_REAR_BLIND, new String[] { OH_CHANNEL_REAR_BLIND, GROUP_WINDOWS });
        CHANNELS.put(MB_KEY_FLIP_WINDOW_STATUS, new String[] { OH_CHANNEL_FLIP_WINDOW, GROUP_WINDOWS });

        CHANNELS.put(MB_KEY_PRECOND_NOW, new String[] { OH_CHANNEL_ACTIVE, GROUP_HVAC });
        CHANNELS.put(MB_KEY_PRECOND_SEAT_FRONT_RIGHT, new String[] { OH_CHANNEL_FRONT_RIGHT, GROUP_HVAC });
        CHANNELS.put(MB_KEY_PRECOND_SEAT_FRONT_LEFT, new String[] { OH_CHANNEL_FRONT_LEFT, GROUP_HVAC });
        CHANNELS.put(MB_KEY_PRECOND_SEAT_REAR_RIGHT, new String[] { OH_CHANNEL_REAR_RIGHT, GROUP_HVAC });
        CHANNELS.put(MB_KEY_PRECOND_SEAT_REAR_LEFT, new String[] { OH_CHANNEL_REAR_LEFT, GROUP_HVAC });
        CHANNELS.put(MB_KEY_AUXILIARY_WARNINGS, new String[] { OH_CHANNEL_AUX_STATUS, GROUP_HVAC });
        CHANNELS.put(MB_KEY_PRECOND_NOW_ERROR, new String[] { OH_CHANNEL_AC_STATUS, GROUP_HVAC });
        // temperaturePoints - special handling: sets zone & temperature

        CHANNELS.put(MB_KEY_STARTER_BATTERY_STATE, new String[] { OH_CHANNEL_STARTER_BATTERY, GROUP_SERVICE });
        CHANNELS.put(MB_KEY_WARNINGBRAKEFLUID, new String[] { OH_CHANNEL_BRAKE_FLUID, GROUP_SERVICE });
        CHANNELS.put(MB_KEY_WARNINGWASHWATER, new String[] { OH_CHANNEL_WASH_WATER, GROUP_SERVICE });
        CHANNELS.put(MB_KEY_WARNINGBRAKELININGWEAR, new String[] { OH_CHANNEL_BRAKE_LINING_WEAR, GROUP_SERVICE });
        CHANNELS.put(MB_KEY_WARNINGCOOLANTLEVELLOW, new String[] { OH_CHANNEL_COOLANT_FLUID, GROUP_SERVICE });
        CHANNELS.put(MB_KEY_WARNINGENGINELIGHT, new String[] { OH_CHANNEL_ENGINE, GROUP_SERVICE });
        CHANNELS.put(MB_KEY_TIREWARNINGSRDK, new String[] { OH_CHANNEL_TIRES_RDK, GROUP_SERVICE });
        CHANNELS.put(MB_KEY_SERVICEINTERVALDAYS, new String[] { OH_CHANNEL_SERVICE_DAYS, GROUP_SERVICE });

        CHANNELS.put(MB_KEY_ODO, new String[] { OH_CHANNEL_MILEAGE, GROUP_RANGE });
        CHANNELS.put(MB_KEY_RANGEELECTRIC, new String[] { OH_CHANNEL_RANGE_ELECTRIC, GROUP_RANGE });
        CHANNELS.put(MB_KEY_SOC, new String[] { MB_KEY_SOC, GROUP_RANGE });
        CHANNELS.put(MB_KEY_RANGELIQUID, new String[] { OH_CHANNEL_RANGE_FUEL, GROUP_RANGE });
        CHANNELS.put(MB_KEY_OVERALL_RANGE, new String[] { OH_CHANNEL_RANGE_HYBRID, GROUP_RANGE });
        CHANNELS.put(MB_KEY_TANKLEVELPERCENT, new String[] { OH_CHANNEL_FUEL_LEVEL, GROUP_RANGE });
        CHANNELS.put(MB_KEY_ADBLUELEVELPERCENT, new String[] { OH_CHANNEL_ADBLUE_LEVEL, GROUP_RANGE });

        CHANNELS.put(MB_KEY_CHARGE_FLAP_DC_STATUS, new String[] { OH_CHANNEL_CHARGE_FLAP, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_CHARGE_COUPLER_AC_STATUS, new String[] { OH_CHANNEL_COUPLER_AC, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_CHARGE_COUPLER_DC_STATUS, new String[] { OH_CHANNEL_COUPLER_DC, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_CHARGE_COUPLER_DC_LOCK_STATUS, new String[] { OH_CHANNEL_COUPLER_LOCK, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_CHARGINGACTIVE, new String[] { OH_CHANNEL_ACTIVE, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_CHARGE_STATUS, new String[] { OH_CHANNEL_STATUS, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_CHARGE_ERROR, new String[] { OH_CHANNEL_ERROR, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_CHARGING_POWER, new String[] { OH_CHANNEL_POWER, GROUP_CHARGE });
        CHANNELS.put(MB_KEY_ENDOFCHARGETIME, new String[] { OH_CHANNEL_END_TIME, GROUP_CHARGE });

        CHANNELS.put(MB_KEY_POSITION_HEADING, new String[] { OH_CHANNEL_HEADING, GROUP_POSITION });
        CHANNELS.put(MB_KEY_POSITION_ERROR, new String[] { OH_CHANNEL_STATUS, GROUP_POSITION });

        CHANNELS.put(MB_KEY_DISTANCE_START, new String[] { OH_CHANNEL_DISTANCE, GROUP_TRIP });
        CHANNELS.put(MB_KEY_DRIVEN_TIME_START, new String[] { OH_CHANNEL_TIME, GROUP_TRIP });
        CHANNELS.put(MB_KEY_AVERAGE_SPEED_START, new String[] { OH_CHANNEL_AVG_SPEED, GROUP_TRIP });
        CHANNELS.put(MB_KEY_ELECTRICCONSUMPTIONSTART, new String[] { OH_CHANNEL_CONS_EV, GROUP_TRIP });
        CHANNELS.put(MB_KEY_LIQUIDCONSUMPTIONSTART, new String[] { OH_CHANNEL_CONS_CONV, GROUP_TRIP });
        CHANNELS.put(MB_KEY_DISTANCE_RESET, new String[] { OH_CHANNEL_DISTANCE_RESET, GROUP_TRIP });
        CHANNELS.put(MB_KEY_DRIVEN_TIME_RESET, new String[] { OH_CHANNEL_TIME_RESET, GROUP_TRIP });
        CHANNELS.put(MB_KEY_AVERAGE_SPEED_RESET, new String[] { OH_CHANNEL_AVG_SPEED_RESET, GROUP_TRIP });
        CHANNELS.put(MB_KEY_ELECTRICCONSUMPTIONRESET, new String[] { OH_CHANNEL_CONS_EV_RESET, GROUP_TRIP });
        CHANNELS.put(MB_KEY_LIQUIDCONSUMPTIONRESET, new String[] { OH_CHANNEL_CONS_CONV_RESET, GROUP_TRIP });

        CHANNELS.put(MB_KEY_ECOSCORE_ACCEL, new String[] { OH_CHANNEL_ACCEL, GROUP_ECO });
        CHANNELS.put(MB_KEY_ECOSCORE_CONSTANT, new String[] { OH_CHANNEL_CONSTANT, GROUP_ECO });
        CHANNELS.put(MB_KEY_ECOSCORE_COASTING, new String[] { OH_CHANNEL_COASTING, GROUP_ECO });
        CHANNELS.put(MB_KEY_ECOSCORE_BONUS, new String[] { OH_CHANNEL_BONUS_RANGE, GROUP_ECO });

        CHANNELS.put(MB_KEY_TIREPRESSURE_REAR_RIGHT, new String[] { OH_CHANNEL_PRESSURE_REAR_RIGHT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIREPRESSURE_FRONT_RIGHT, new String[] { OH_CHANNEL_PRESSURE_FRONT_RIGHT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIREPRESSURE_REAR_LEFT, new String[] { OH_CHANNEL_PRESSURE_REAR_LEFT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIREPRESSURE_FRONT_LEFT, new String[] { OH_CHANNEL_PRESSURE_FRONT_LEFT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIRE_MARKER_FRONT_RIGHT, new String[] { OH_CHANNEL_MARKER_REAR_RIGHT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIRE_MARKER_FRONT_LEFT, new String[] { OH_CHANNEL_MARKER_FRONT_RIGHT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIRE_MARKER_REAR_RIGHT, new String[] { OH_CHANNEL_MARKER_REAR_LEFT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIRE_MARKER_REAR_LEFT, new String[] { OH_CHANNEL_MARKER_FRONT_LEFT, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIRE_SENSOR_AVAILABLE, new String[] { OH_CHANNEL_SENSOR_AVAILABLE, GROUP_TIRES });
        CHANNELS.put(MB_KEY_TIRE_PRESS_MEAS_TIMESTAMP, new String[] { OH_CHANNEL_LAST_UPDATE, GROUP_TIRES });
    }
}
