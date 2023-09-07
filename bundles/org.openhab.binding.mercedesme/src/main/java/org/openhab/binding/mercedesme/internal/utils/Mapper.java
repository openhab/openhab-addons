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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.VehicleAttributeStatus;
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
                    state = QuantityType.valueOf(value.getIntValue(), KILOMETRE_UNIT);
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Percentages
                case "soc":
                case "tanklevelpercent":
                    state = QuantityType.valueOf(value.getIntValue(), Units.PERCENT);
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Contacts
                case "decklidstatus":
                case "doorstatusfrontleft":
                case "doorstatusfrontright":
                case "doorstatusrearleft":
                case "doorstatusrearright":
                    state = getContact(value.getBoolValue());
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Number Status
                case "lightswitchposition":
                case "rooftopstatus":
                case "sunroofstatus":
                case "windowstatusfrontleft":
                case "windowstatusfrontright":
                case "windowstatusrearleft":
                case "windowstatusrearright":
                case "doorlockstatusvehicle":
                    return new ChannelStateMap(ch[0], ch[1], new DecimalType(value.getIntValue()));

                // Switches
                case "interiorLightsFront":
                case "interiorLightsRear":
                case "readingLampFrontLeft":
                case "readingLampFrontRight":
                case "warningbrakefluid":
                case "warningbrakeliningwear":
                case "warningwashwater":
                case "warningcoolantlevellow":
                case "warningenginelight":
                    if (value.hasBoolValue()) {
                        state = OnOffType.from(value.getBoolValue());
                    } else {
                        state = UnDefType.UNDEF;
                    }
                    return new ChannelStateMap(ch[0], ch[1], state);

                case "doorlockstatusdecklid":
                case "doorlockstatusgas":
                    state = OnOffType.from(value.getBoolValue());
                    return new ChannelStateMap(ch[0], ch[1], state);

                // Angle
                case "positionHeading":
                    state = QuantityType.valueOf(Double.valueOf(value.getDoubleValue()), Units.DEGREE_ANGLE);
                    return new ChannelStateMap(ch[0], ch[1], state);

                // tires
                case "tirepressureFrontLeft":
                case "tirepressureFrontRight":
                case "tirepressureRearLeft":
                case "tirepressureRearRight":
                    state = QuantityType.valueOf(Double.valueOf(value.getDisplayValue()), Units.BAR);
                    return new ChannelStateMap(ch[0], ch[1], state);
                default:
                    // LOGGER.trace("No mapping available for {}", key);
            }
        } else {
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

    /**
     * Mapping of json id towards channel group and id
     */
    private static void init() {
        CHANNELS.put("odo", new String[] { "mileage", GROUP_RANGE });
        CHANNELS.put("rangeelectric", new String[] { "range-electric", GROUP_RANGE });
        CHANNELS.put("soc", new String[] { "soc", GROUP_RANGE });
        CHANNELS.put("rangeliquid", new String[] { "range-fuel", GROUP_RANGE });
        CHANNELS.put("tanklevelpercent", new String[] { "fuel-level", GROUP_RANGE });
        CHANNELS.put("decklidstatus", new String[] { "deck-lid", GROUP_DOORS });
        CHANNELS.put("doorstatusfrontleft", new String[] { "driver-front", GROUP_DOORS });
        CHANNELS.put("doorstatusfrontright", new String[] { "passenger-front", GROUP_DOORS });
        CHANNELS.put("doorstatusrearleft", new String[] { "driver-rear", GROUP_DOORS });
        CHANNELS.put("doorstatusrearright", new String[] { "passenger-rear", GROUP_DOORS });
        CHANNELS.put("rooftopstatus", new String[] { "rooftop", GROUP_DOORS });
        CHANNELS.put("sunroofstatus", new String[] { "sunroof", GROUP_DOORS });
        CHANNELS.put("windowstatusfrontleft", new String[] { "driver-front", GROUP_WINDOWS });
        CHANNELS.put("windowstatusfrontright", new String[] { "passenger-front", GROUP_WINDOWS });
        CHANNELS.put("windowstatusrearleft", new String[] { "driver-rear", GROUP_WINDOWS });
        CHANNELS.put("windowstatusrearright", new String[] { "passenger-rear", GROUP_WINDOWS });
        CHANNELS.put("doorlockstatusvehicle", new String[] { "doors", GROUP_LOCK });
        CHANNELS.put("doorlockstatusdecklid", new String[] { "deck-lid", GROUP_LOCK });
        CHANNELS.put("doorlockstatusgas", new String[] { "flap", GROUP_LOCK });
        CHANNELS.put("positionHeading", new String[] { "heading", GROUP_POSITION });
        CHANNELS.put("tirepressureRearRight", new String[] { "pressure-rear-right", GROUP_TIRES });
        CHANNELS.put("tirepressureFrontRight", new String[] { "pressure-front-right", GROUP_TIRES });
        CHANNELS.put("tirepressureRearLeft", new String[] { "pressure-rear-left", GROUP_TIRES });
        CHANNELS.put("tirepressureFrontLeft", new String[] { "pressure-front-left", GROUP_TIRES });
        CHANNELS.put("warningbrakefluid", new String[] { "brake-fluid", GROUP_SERVICE });
        CHANNELS.put("warningbrakeliningwear", new String[] { "brake-lining-wear", GROUP_SERVICE });
        CHANNELS.put("warningwashwater", new String[] { "wash-water", GROUP_SERVICE });
        CHANNELS.put("warningcoolantlevellow", new String[] { "coolant-fluid", GROUP_SERVICE });
        CHANNELS.put("warningenginelight", new String[] { "engine", GROUP_SERVICE });
    }
}
