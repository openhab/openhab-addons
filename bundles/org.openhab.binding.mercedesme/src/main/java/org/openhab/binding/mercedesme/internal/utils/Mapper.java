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
    public static final Map<String, String[]> CHANNELS = new HashMap<String, String[]>();
    public static final String TIMESTAMP = "timestamp";
    public static final String VALUE = "value";

    public static Unit<Length> defaultLengthUnit = KILOMETRE_UNIT;
    public static Unit<Temperature> defaultTemperatureUnit = SIUnits.CELSIUS;
    public static Unit<Pressure> defaultPressureUnit = Units.BAR;
    public static Unit<Volume> defaultVolumeUnit = Units.LITRE;
    public static Unit<Speed> defaultSpeedUnit = SIUnits.KILOMETRE_PER_HOUR;

    public static void initialze(UnitProvider up) {
        // Configure Mapper default values
        Unit<Length> lengthUnit = up.getUnit(Length.class);
        if (lengthUnit != null) {
            if (ImperialUnits.FOOT.equals(lengthUnit)) {
                defaultLengthUnit = ImperialUnits.MILE;
                defaultSpeedUnit = ImperialUnits.MILES_PER_HOUR;
                defaultPressureUnit = ImperialUnits.POUND_FORCE_SQUARE_INCH;
                defaultVolumeUnit = ImperialUnits.GALLON_LIQUID_US;
            }
        }
        Unit<Temperature> temperatureUnit = up.getUnit(Temperature.class);
        if (temperatureUnit != null) {
            defaultTemperatureUnit = temperatureUnit;
        }
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
                case "odo":
                case "rangeelectric":
                case "overallRange":
                case "rangeliquid":
                case "distanceStart":
                case "distanceReset":
                    Unit lengthUnit = defaultLengthUnit;
                    if (value.hasDistanceUnit()) {
                        observer = new UOMObserver(value.getDistanceUnit().toString());
                        if (observer.getUnit().isEmpty()) {
                            LOGGER.trace("No Unit found for {} - take default ", key);
                        } else {
                            lengthUnit = observer.getUnit().get();
                        }
                    }
                    state = QuantityType.valueOf(Utils.getDouble(value), lengthUnit);
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // special String Value
                case "drivenTimeStart":
                case "drivenTimeReset":
                    int duration = Utils.getInt(value);
                    if (duration < 0) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = StringType.valueOf(Utils.getDurationString(duration));
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // KiloWatt values
                case "chargingPower":
                    double power = Utils.getDouble(value);
                    state = QuantityType.valueOf(Math.max(0, power), KILOWATT_UNIT);
                    return new ChannelStateMap(ch[0], ch[1], state);

                case "averageSpeedStart":
                case "averageSpeedReset":
                    Unit speedUnit = defaultSpeedUnit;
                    if (value.hasSpeedUnit()) {
                        observer = new UOMObserver(value.getSpeedUnit().toString());
                        if (observer.getUnit().isEmpty()) {
                            LOGGER.trace("No Unit found for {} - take default ", key);
                        } else {
                            lengthUnit = observer.getUnit().get();
                        }
                    }
                    double speed = Utils.getDouble(value);
                    state = QuantityType.valueOf(Math.max(0, speed), speedUnit);
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // KiloWatt/Hour values
                case "electricconsumptionstart":
                case "electricconsumptionreset":
                    double consumptionEv = Utils.getDouble(value);
                    state = new DecimalType(consumptionEv);
                    if (value.hasElectricityConsumptionUnit()) {
                        observer = new UOMObserver(value.getElectricityConsumptionUnit().toString());
                    } else {
                        LOGGER.debug("Don't have electric consumption unit for {}", key);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // Litre values
                case "liquidconsumptionstart":
                case "liquidconsumptionreset":
                    double consumptionComb = Utils.getDouble(value);
                    state = new DecimalType(consumptionComb);
                    if (value.hasCombustionConsumptionUnit()) {
                        observer = new UOMObserver(value.getCombustionConsumptionUnit().toString());
                    }
                    return new ChannelStateMap(ch[0], ch[1], state, observer);

                // Time - end of charging
                case "endofchargetime":
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
                case "tirePressMeasTimestamp":
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
                case "soc":
                case "tanklevelpercent":
                    double level = Utils.getDouble(value);
                    state = QuantityType.valueOf(level, Units.PERCENT);
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Contacts
                case "doorstatusfrontright":
                case "doorstatusfrontleft":
                case "doorstatusrearright":
                case "doorstatusrearleft":
                case "decklidstatus":
                case "engineHoodStatus":
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = getContact(value.getBoolValue());
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Number Status
                case "doorLockStatusOverall":
                case "windowStatusOverall":
                case "doorStatusOverall":
                case "ignitionstate":
                case "sunroofstatus":
                case "sunroofStatusFrontBlind":
                case "sunroofStatusRearBlind":
                case "rooftopstatus":
                case "windowstatusfrontleft":
                case "windowstatusfrontright":
                case "windowstatusrearleft":
                case "windowstatusrearright":
                case "windowStatusRearRightBlind":
                case "windowStatusRearLeftBlind":
                case "windowStatusRearBlind":
                case "flipWindowStatus":
                case "starterBatteryState":
                case "tirewarningsrdk":
                case "serviceintervaldays":
                case "chargeFlapDCStatus":
                case "chargeCouplerACStatus":
                case "chargeCouplerDCStatus":
                case "chargeCouplerDCLockStatus":
                case "tireSensorAvailable":
                    int stateNumberInteger = Utils.getInt(value);
                    if (stateNumberInteger < 0) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = new DecimalType(stateNumberInteger);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                case "tireMarkerFrontRight":
                case "tireMarkerFrontLeft":
                case "tireMarkerRearRight":
                case "tireMarkerRearLeft":
                    double stateNumberDouble = Utils.getDouble(value);
                    if (stateNumberDouble < 0) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = new DecimalType(stateNumberDouble);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Switches
                case "parkbrakestatus":
                case "precondNow":
                case "precondSeatFrontRight":
                case "precondSeatFrontLeft":
                case "precondSeatRearRight":
                case "precondSeatRearLeft":
                case "warningbrakefluid":
                case "warningbrakeliningwear":
                case "warningwashwater":
                case "warningcoolantlevellow":
                case "warningenginelight":
                case "chargingactive":
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
                case "doorlockstatusfrontright":
                case "doorlockstatusfrontleft":
                case "doorlockstatusrearright":
                case "doorlockstatusrearleft":
                case "doorlockstatusdecklid":
                case "doorlockstatusgas":
                    if (Utils.isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        // sad but true - false means locked
                        state = OnOffType.from(!value.getBoolValue());
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Angle
                case "positionHeading":
                    double heading = Utils.getDouble(value);
                    if (heading < 0) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = QuantityType.valueOf(heading, Units.DEGREE_ANGLE);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // tires
                case "tirepressureFrontLeft":
                case "tirepressureFrontRight":
                case "tirepressureRearLeft":
                case "tirepressureRearRight":
                    Unit pressureUnit = defaultPressureUnit;
                    if (value.hasPressureUnit()) {
                        observer = new UOMObserver(value.getPressureUnit().toString());
                        if (observer.getUnit().isEmpty()) {
                            LOGGER.debug("No Unit found for {} - take default ", key);
                        } else {
                            pressureUnit = observer.getUnit().get();
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
        CHANNELS.put("doorLockStatusOverall", new String[] { "lock", GROUP_VEHICLE });
        CHANNELS.put("windowStatusOverall", new String[] { "windows", GROUP_VEHICLE });
        CHANNELS.put("doorStatusOverall", new String[] { "door-status", GROUP_VEHICLE });
        CHANNELS.put("ignitionstate", new String[] { "ignition", GROUP_VEHICLE });
        CHANNELS.put("parkbrakestatus", new String[] { "park-brake", GROUP_VEHICLE });

        CHANNELS.put("doorstatusfrontright", new String[] { "front-right", GROUP_DOORS });
        CHANNELS.put("doorstatusfrontleft", new String[] { "front-left", GROUP_DOORS });
        CHANNELS.put("doorstatusrearright", new String[] { "rear-right", GROUP_DOORS });
        CHANNELS.put("doorstatusrearleft", new String[] { "rear-left", GROUP_DOORS });
        CHANNELS.put("decklidstatus", new String[] { "deck-lid", GROUP_DOORS });
        CHANNELS.put("engineHoodStatus", new String[] { "engine-hood", GROUP_DOORS });
        CHANNELS.put("sunroofstatus", new String[] { "sunroof", GROUP_DOORS });
        CHANNELS.put("sunroofStatusFrontBlind", new String[] { "sunroof-front-blind", GROUP_DOORS });
        CHANNELS.put("sunroofStatusRearBlind", new String[] { "sunroof-rear-blind", GROUP_DOORS });
        CHANNELS.put("rooftopstatus", new String[] { "rooftop", GROUP_DOORS });

        CHANNELS.put("doorlockstatusfrontright", new String[] { "front-right", GROUP_LOCK });
        CHANNELS.put("doorlockstatusfrontleft", new String[] { "front-left", GROUP_LOCK });
        CHANNELS.put("doorlockstatusrearright", new String[] { "rear-right", GROUP_LOCK });
        CHANNELS.put("doorlockstatusrearleft", new String[] { "rear-left", GROUP_LOCK });
        CHANNELS.put("doorlockstatusdecklid", new String[] { "deck-lid", GROUP_LOCK });
        CHANNELS.put("doorlockstatusgas", new String[] { "gas-flap", GROUP_LOCK });

        CHANNELS.put("windowstatusfrontleft", new String[] { "front-left", GROUP_WINDOWS });
        CHANNELS.put("windowstatusfrontright", new String[] { "front-right", GROUP_WINDOWS });
        CHANNELS.put("windowstatusrearleft", new String[] { "rear-left", GROUP_WINDOWS });
        CHANNELS.put("windowstatusrearright", new String[] { "rear-right", GROUP_WINDOWS });
        CHANNELS.put("windowStatusRearRightBlind", new String[] { "rear-right-blind", GROUP_WINDOWS });
        CHANNELS.put("windowStatusRearLeftBlind", new String[] { "rear-left-blind", GROUP_WINDOWS });
        CHANNELS.put("windowStatusRearBlind", new String[] { "rear-blind", GROUP_WINDOWS });
        CHANNELS.put("flipWindowStatus", new String[] { "flip-window", GROUP_WINDOWS });

        CHANNELS.put("precondNow", new String[] { "active", GROUP_HVAC });
        CHANNELS.put("precondSeatFrontRight", new String[] { "front-right", GROUP_HVAC });
        CHANNELS.put("precondSeatFrontLeft", new String[] { "front-left", GROUP_HVAC });
        CHANNELS.put("precondSeatRearRight", new String[] { "rear-right", GROUP_HVAC });
        CHANNELS.put("precondSeatRearLeft", new String[] { "rear-left", GROUP_HVAC });
        // temperaturePoints - special handling: sets zone & temperature

        CHANNELS.put("starterBatteryState", new String[] { "starter-battery", GROUP_SERVICE });
        CHANNELS.put("warningbrakefluid", new String[] { "brake-fluid", GROUP_SERVICE });
        CHANNELS.put("warningwashwater", new String[] { "wash-water", GROUP_SERVICE });
        CHANNELS.put("warningbrakeliningwear", new String[] { "brake-lining-wear", GROUP_SERVICE });
        CHANNELS.put("warningcoolantlevellow", new String[] { "coolant-fluid", GROUP_SERVICE });
        CHANNELS.put("warningenginelight", new String[] { "engine", GROUP_SERVICE });
        CHANNELS.put("tirewarningsrdk", new String[] { "tires-rdk", GROUP_SERVICE });
        CHANNELS.put("serviceintervaldays", new String[] { "service-days", GROUP_SERVICE });

        CHANNELS.put("odo", new String[] { CHANNEL_MILEAGE, GROUP_RANGE });
        CHANNELS.put("rangeelectric", new String[] { "range-electric", GROUP_RANGE });
        CHANNELS.put("soc", new String[] { "soc", GROUP_RANGE });
        CHANNELS.put("rangeliquid", new String[] { "range-fuel", GROUP_RANGE });
        CHANNELS.put("overallRange", new String[] { "range-hybrid", GROUP_RANGE });
        CHANNELS.put("tanklevelpercent", new String[] { "fuel-level", GROUP_RANGE });

        CHANNELS.put("chargeFlapDCStatus", new String[] { "charge-flap", GROUP_CHARGE });
        CHANNELS.put("chargeCouplerACStatus", new String[] { "coupler-ac", GROUP_CHARGE });
        CHANNELS.put("chargeCouplerDCStatus", new String[] { "coupler-dc", GROUP_CHARGE });
        CHANNELS.put("chargeCouplerDCLockStatus", new String[] { "coupler-lock", GROUP_CHARGE });
        CHANNELS.put("chargingactive", new String[] { "active", GROUP_CHARGE });
        CHANNELS.put("chargingPower", new String[] { "power", GROUP_CHARGE });
        CHANNELS.put("endofchargetime", new String[] { "end-time", GROUP_CHARGE });

        CHANNELS.put("positionHeading", new String[] { "heading", GROUP_POSITION });

        CHANNELS.put("distanceStart", new String[] { "distance", GROUP_TRIP });
        CHANNELS.put("drivenTimeStart", new String[] { "time", GROUP_TRIP });
        CHANNELS.put("averageSpeedStart", new String[] { "avg-speed", GROUP_TRIP });
        CHANNELS.put("electricconsumptionstart", new String[] { "cons-ev", GROUP_TRIP });
        CHANNELS.put("liquidconsumptionstart", new String[] { "cons-conv", GROUP_TRIP });
        CHANNELS.put("distanceReset", new String[] { "distance-reset", GROUP_TRIP });
        CHANNELS.put("drivenTimeReset", new String[] { "time-reset", GROUP_TRIP });
        CHANNELS.put("averageSpeedReset", new String[] { "avg-speed-reset", GROUP_TRIP });
        CHANNELS.put("electricconsumptionreset", new String[] { "cons-ev-reset", GROUP_TRIP });
        CHANNELS.put("liquidconsumptionreset", new String[] { "cons-conv-reset", GROUP_TRIP });

        CHANNELS.put("tirepressureRearRight", new String[] { "pressure-rear-right", GROUP_TIRES });
        CHANNELS.put("tirepressureFrontRight", new String[] { "pressure-front-right", GROUP_TIRES });
        CHANNELS.put("tirepressureRearLeft", new String[] { "pressure-rear-left", GROUP_TIRES });
        CHANNELS.put("tirepressureFrontLeft", new String[] { "pressure-front-left", GROUP_TIRES });
        CHANNELS.put("tireMarkerFrontRight", new String[] { "marker-rear-right", GROUP_TIRES });
        CHANNELS.put("tireMarkerFrontLeft", new String[] { "marker-front-right", GROUP_TIRES });
        CHANNELS.put("tireMarkerRearRight", new String[] { "marker-rear-left", GROUP_TIRES });
        CHANNELS.put("tireMarkerRearLeft", new String[] { "marker-front-left", GROUP_TIRES });
        CHANNELS.put("tireSensorAvailable", new String[] { "sensor-available", GROUP_TIRES });
        CHANNELS.put("tirePressMeasTimestamp", new String[] { "last-update", GROUP_TIRES });
    }
}
