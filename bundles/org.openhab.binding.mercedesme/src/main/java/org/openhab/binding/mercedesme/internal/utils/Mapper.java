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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
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

    public static final ChannelStateMap INVALID_MAP = new ChannelStateMap(EMPTY, EMPTY, UnDefType.UNDEF, -1);
    public static final Map<String, String[]> CHANNELS = new HashMap<>();
    public static final String TIMESTAMP = "timestamp";
    public static final String VALUE = "value";

    public static ChannelStateMap getChannelStateMap(JSONObject jo) {
        if (CHANNELS.isEmpty()) {
            init();
        }
        Set<String> s = jo.keySet();
        if (s.size() == 1) {
            String id = s.toArray()[0].toString();
            String[] ch = CHANNELS.get(id);
            if (ch != null) {
                State state;
                switch (id) {
                    // Kilometer values
                    case "odo":
                    case "rangeelectric":
                    case "rangeliquid":
                        state = getKilometers((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state, getTimestamp((JSONObject) jo.get(id)));

                    // Percentages
                    case "soc":
                    case "tanklevelpercent":
                        state = getPercentage((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state, getTimestamp((JSONObject) jo.get(id)));

                    // Contacts
                    case "decklidstatus":
                    case "doorstatusfrontleft":
                    case "doorstatusfrontright":
                    case "doorstatusrearleft":
                    case "doorstatusrearright":
                        state = getContact((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state, getTimestamp((JSONObject) jo.get(id)));

                    // Number Status
                    case "lightswitchposition":
                    case "rooftopstatus":
                    case "sunroofstatus":
                    case "windowstatusfrontleft":
                    case "windowstatusfrontright":
                    case "windowstatusrearleft":
                    case "windowstatusrearright":
                    case "doorlockstatusvehicle":
                        state = getDecimal((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state, getTimestamp((JSONObject) jo.get(id)));

                    // Switches
                    case "interiorLightsFront":
                    case "interiorLightsRear":
                    case "readingLampFrontLeft":
                    case "readingLampFrontRight":
                        state = getOnOffType((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state, getTimestamp((JSONObject) jo.get(id)));

                    case "doorlockstatusdecklid":
                    case "doorlockstatusgas":
                        state = getOnOffTypeLock((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state, getTimestamp((JSONObject) jo.get(id)));

                    // Angle
                    case "positionHeading":
                        state = getAngle((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state, getTimestamp((JSONObject) jo.get(id)));
                    default:
                        LOGGER.trace("No mapping available for {}", id);
                }
            } else {
                LOGGER.trace("No mapping available for {}", id);
            }
        } else {
            LOGGER.debug("More than one key found {}", s);
        }
        return INVALID_MAP;
    }

    private static long getTimestamp(JSONObject jo) {
        if (jo.has(TIMESTAMP)) {
            return jo.getLong(TIMESTAMP);
        }
        return -1;
    }

    private static State getOnOffType(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            boolean b = Boolean.valueOf(value);
            return OnOffType.from(b);
        } else {
            LOGGER.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getOnOffTypeLock(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            boolean b = Boolean.valueOf(value);
            // Yes, false is locked and true unlocked
            // https://developer.mercedes-benz.com/products/vehicle_lock_status/specifications/vehicle_lock_status_api
            return OnOffType.from(!b);
        } else {
            LOGGER.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getAngle(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return QuantityType.valueOf(Double.valueOf(value), Units.DEGREE_ANGLE);
        } else {
            LOGGER.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getDecimal(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return DecimalType.valueOf(value);
        } else {
            LOGGER.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getContact(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            boolean b = Boolean.valueOf(value);
            if (!b) {
                return OpenClosedType.CLOSED;
            } else {
                return OpenClosedType.OPEN;
            }
        } else {
            LOGGER.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getKilometers(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return QuantityType.valueOf(Integer.valueOf(value), KILOMETRE_UNIT);
        } else {
            LOGGER.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getPercentage(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return QuantityType.valueOf(Integer.valueOf(value), Units.PERCENT);
        } else {
            LOGGER.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
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
        CHANNELS.put("interiorLightsFront", new String[] { "interior-front", GROUP_LIGHTS });
        CHANNELS.put("interiorLightsRear", new String[] { "interior-rear", GROUP_LIGHTS });
        CHANNELS.put("lightswitchposition", new String[] { "light-switch", GROUP_LIGHTS });
        CHANNELS.put("readingLampFrontLeft", new String[] { "reading-left", GROUP_LIGHTS });
        CHANNELS.put("readingLampFrontRight", new String[] { "reading-right", GROUP_LIGHTS });
        CHANNELS.put("rooftopstatus", new String[] { "rooftop", GROUP_DOORS });
        CHANNELS.put("sunroofstatus", new String[] { "sunroof", GROUP_DOORS });
        CHANNELS.put("windowstatusfrontleft", new String[] { "driver-front", GROUP_WINDOWS });
        CHANNELS.put("windowstatusfrontright", new String[] { "passenger-front", GROUP_WINDOWS });
        CHANNELS.put("windowstatusrearleft", new String[] { "driver-rear", GROUP_WINDOWS });
        CHANNELS.put("windowstatusrearright", new String[] { "passenger-rear", GROUP_WINDOWS });
        CHANNELS.put("doorlockstatusvehicle", new String[] { "doors", GROUP_LOCK });
        CHANNELS.put("doorlockstatusdecklid", new String[] { "deck-lid", GROUP_LOCK });
        CHANNELS.put("doorlockstatusgas", new String[] { "flap", GROUP_LOCK });
        CHANNELS.put("positionHeading", new String[] { "heading", GROUP_LOCATION });
    }
}
