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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VehicleAttributeStatus;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Mapper} maps a given Json Object towards a channel, group and state
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

    public static ChannelStateMap getChannelStateMap(String key, VehicleAttributeStatus value) {
        if (CHANNELS.isEmpty()) {
            init();
        }
        String[] ch = CHANNELS.get(key);
        if (ch != null) {
            State state;
            switch (key) {
                // Kilometer values
                case "odo":
                case "rangeelectric":
                case "rangeliquid":
                case "distanceStart":
                    if (isNil(value)) {
                        state = QuantityType.valueOf(-1, KILOMETRE_UNIT);
                    } else {
                        if (value.hasDoubleValue()) {
                            state = QuantityType.valueOf(value.getDoubleValue(), KILOMETRE_UNIT);
                        } else if (value.hasIntValue()) {
                            state = QuantityType.valueOf(value.getIntValue(), KILOMETRE_UNIT);
                        } else {
                            state = UnDefType.UNDEF;
                            LOGGER.info("Neither Double nor Integer value available for Kilometer {}", key);
                        }
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // KiloWatt values
                case "chargingPower":
                    if (isNil(value)) {
                        state = QuantityType.valueOf(0, KILOWATT_UNIT);
                    } else {
                        if (value.hasDoubleValue()) {
                            state = QuantityType.valueOf(value.getDoubleValue(), KILOWATT_UNIT);
                        } else if (value.hasIntValue()) {
                            state = QuantityType.valueOf(value.getIntValue(), KILOWATT_UNIT);
                        } else {
                            state = QuantityType.valueOf(0, KILOWATT_UNIT);
                            LOGGER.info("Neither Double nor Integer value available for Kilometer {}", key);
                        }
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // KiloWatt/Hour values
                case "electricconsumptionstart":
                    if (isNil(value)) {
                        state = QuantityType.valueOf(-1, KILOWATT_HOUR_UNIT);
                    } else {
                        if (value.hasDoubleValue()) {
                            state = QuantityType.valueOf(value.getDoubleValue(), KILOWATT_HOUR_UNIT);
                        } else if (value.hasIntValue()) {
                            state = QuantityType.valueOf(value.getIntValue(), KILOWATT_HOUR_UNIT);
                        } else {
                            state = QuantityType.valueOf(-1, KILOWATT_HOUR_UNIT);
                            LOGGER.info("Neither Double nor Integer value available for Kilometer {}", key);
                        }
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Litre values
                case "gasconsumptionstart":
                    if (isNil(value)) {
                        state = QuantityType.valueOf(-1, Units.LITRE);
                    } else {
                        if (value.hasDoubleValue()) {
                            state = QuantityType.valueOf(value.getDoubleValue(), Units.LITRE);
                        } else if (value.hasIntValue()) {
                            state = QuantityType.valueOf(value.getIntValue(), Units.LITRE);
                        } else {
                            state = QuantityType.valueOf(-1, Units.LITRE);
                            LOGGER.info("Neither Double nor Integer value available for Kilometer {}", key);
                        }
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Time - end of charging
                case "endofchargetime":
                    if (isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        // [todo] end time calculation missing
                        Instant time = Instant.ofEpochMilli(value.getTimestampInMs());
                        LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
                        state = DateTimeType.valueOf(ldt.toString());
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Time - Driven Time Trip
                case "drivenTimeStart":
                    if (isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        // [todo] end time calculation missing
                        int hoursDriven = (int) value.getIntValue() / 60;
                        int minutesDriven = (int) value.getIntValue() - (hoursDriven * 60);
                        LOGGER.info("Driven Time {} h {} m", hoursDriven, minutesDriven);
                        Instant time = Instant.ofEpochMilli(value.getTimestampInMs());
                        LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneOffset.UTC).withHour(hoursDriven)
                                .withMinute(minutesDriven);
                        state = DateTimeType.valueOf(ldt.toString());
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // DateTime - last Update
                case "tirePressMeasTimestamp":
                    if (isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        Instant time = Instant.ofEpochMilli(value.getTimestampInMs());
                        LocalDateTime ldt = LocalDateTime.ofInstant(time, ZoneOffset.UTC);
                        state = DateTimeType.valueOf(ldt.toString());
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Percentages
                case "soc":
                case "tanklevelpercent":
                    if (isNil(value)) {
                        state = QuantityType.valueOf(-1, Units.PERCENT);
                    } else {
                        state = QuantityType.valueOf(value.getIntValue(), Units.PERCENT);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Contacts
                case "doorstatusfrontright":
                case "doorstatusfrontleft":
                case "doorstatusrearright":
                case "doorstatusrearleft":
                case "decklidstatus":
                case "engineHoodStatus":
                    if (isNil(value)) {
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
                case "doorlockstatusvehicle":
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
                case "averageSpeedStart":
                case "tireMarkerFrontRight":
                case "tireMarkerFrontLeft":
                case "tireMarkerRearRight":
                case "tireMarkerRearLeft":
                case "tireSensorAvailable":
                    if (isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        if (value.hasDoubleValue()) {
                            state = new DecimalType(value.getDoubleValue());
                        } else if (value.hasIntValue()) {
                            state = new DecimalType(value.getIntValue());
                        } else {
                            state = UnDefType.UNDEF;
                        }
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Switches
                case "parkbrakestatus":
                case "precondActive":
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
                    if (isNil(value)) {
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
                    if (isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        // sad but true - false means locked
                        state = OnOffType.from(!value.getBoolValue());
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Angle
                case "positionHeading":
                    if (isNil(value)) {
                        state = UnDefType.UNDEF;
                    } else {
                        state = QuantityType.valueOf(Double.valueOf(value.getDoubleValue()), Units.DEGREE_ANGLE);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                // tires
                case "tirepressureFrontLeft":
                case "tirepressureFrontRight":
                case "tirepressureRearLeft":
                case "tirepressureRearRight":
                    if (isNil(value)) {
                        state = QuantityType.valueOf(-1, Units.BAR);
                    } else {
                        state = QuantityType.valueOf(Double.valueOf(value.getDisplayValue()), Units.BAR);
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);
                default:
                    // LOGGER.trace("No mapping available for {}", key);
            }
        } else

        {
            // LOGGER.trace("No mapping available for {}", key);
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

    public static boolean isNil(@Nullable VehicleAttributeStatus value) {
        if (value != null) {
            if (value.hasNilValue()) {
                return value.getNilValue();
            }
        }
        return false;
    }

    /**
     * Mapping of json id towards channel group and id
     */
    private static void init() {
        CHANNELS.put("doorLockStatusOverall", new String[] { "lock-status", GROUP_VEHICLE });
        CHANNELS.put("windowStatusOverall", new String[] { "window-status", GROUP_VEHICLE });
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

        CHANNELS.put("precondActive", new String[] { "active", GROUP_HVAC });
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

        CHANNELS.put("odo", new String[] { "mileage", GROUP_RANGE });
        CHANNELS.put("rangeelectric", new String[] { "range-electric", GROUP_RANGE });
        CHANNELS.put("soc", new String[] { "soc", GROUP_RANGE });
        CHANNELS.put("rangeliquid", new String[] { "range-fuel", GROUP_RANGE });
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
        CHANNELS.put("drivenTimeStart", new String[] { "driven-time", GROUP_TRIP });
        CHANNELS.put("averageSpeedStart", new String[] { "avg-speed", GROUP_TRIP });
        CHANNELS.put("electricconsumptionstart", new String[] { "consumption-ev", GROUP_TRIP });
        CHANNELS.put("gasconsumptionstart", new String[] { "consumption-conv", GROUP_TRIP });

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
