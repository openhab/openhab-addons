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

import com.daimler.mbcarkit.proto.VehicleEvents.VehicleAttributeStatus;

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
            switch (key) {
                // Kilometer values
                case MB_KEY_ODO:
                case MB_KEY_RANGEELECTRIC:
                case MB_KEY_OVERALL_RANGE:
                case MB_KEY_RANGELIQUID:
                case MB_KEY_DISTANCE_START:
                case MB_KEY_DISTANCE_RESET:
                case MB_KEY_ECOSCORE_BONUS:
                    Unit<?> lengthUnit = defaultLengthUnit;
                    if (value.hasDistanceUnit()) {
                        observer = new UOMObserver(value.getDistanceUnit().toString());
                        Unit<?> queryUnit = observer.getUnit();
                        if (queryUnit != null) {
                            lengthUnit = queryUnit;
                        } else {
                            LOGGER.trace("No Unit found for {} - take default ", key);
                        }
                    }
                    state = QuantityType.valueOf(Utils.getDouble(value), lengthUnit);
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
                    double power = Utils.getDouble(value);
                    state = QuantityType.valueOf(Math.max(0, power), KILOWATT_UNIT);
                    return new ChannelStateMap(ch[0], ch[1], state);

                case MB_KEY_AVERAGE_SPEED_START:
                case MB_KEY_AVERAGE_SPEED_RESET:
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
                    double speed = Utils.getDouble(value);
                    state = QuantityType.valueOf(Math.max(0, speed), speedUnit);
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // KiloWatt/Hour values
                case MB_KEY_ELECTRICCONSUMPTIONSTART:
                case MB_KEY_ELECTRICCONSUMPTIONRESET:
                    double consumptionEv = Utils.getDouble(value);
                    state = new DecimalType(consumptionEv);
                    if (value.hasElectricityConsumptionUnit()) {
                        observer = new UOMObserver(value.getElectricityConsumptionUnit().toString());
                    } else {
                        LOGGER.trace("Don't have electric consumption unit for {}", key);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // Litre values
                case MB_KEY_LIQUIDCONSUMPTIONSTART:
                case MB_KEY_LIQUIDCONSUMPTIONRESET:
                    double consumptionComb = Utils.getDouble(value);
                    state = new DecimalType(consumptionComb);
                    if (value.hasCombustionConsumptionUnit()) {
                        observer = new UOMObserver(value.getCombustionConsumptionUnit().toString());
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
                    double level = Utils.getDouble(value);
                    state = QuantityType.valueOf(level, Units.PERCENT);
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
                        state = getContact(value.getBoolValue());
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
                    } else {
                        if (value.hasBoolValue()) {
                            state = OnOffType.from(value.getBoolValue());
                        } else {
                            state = UnDefType.UNDEF;
                        }
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
                        // sad but true - false means locked
                        state = OnOffType.from(!value.getBoolValue());
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
                    double pressure = Utils.getDouble(value);
                    state = QuantityType.valueOf(pressure, pressureUnit);
                    return new ChannelStateMap(ch[0], ch[1], state, observer);
                default:
                    break;
            }
        }
        return INVALID_MAP;
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
