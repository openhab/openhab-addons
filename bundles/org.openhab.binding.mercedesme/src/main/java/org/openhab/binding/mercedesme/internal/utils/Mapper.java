/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.Constants;
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
public class Mapper {
    private final static Logger logger = LoggerFactory.getLogger(Mapper.class);
    public static final Map<String, String[]> CHANNELS = new HashMap<String, String[]>();
    public static final String VALUE = "value";

    public static ChannelStateMap getChannelStateMap(JSONObject jo) {
        if (CHANNELS.isEmpty()) {
            init();
        }
        Set<String> s = jo.keySet();
        if (s.size() == 1) {
            String id = s.toArray()[0].toString();
            if (CHANNELS.containsKey(id)) {
                String[] ch = CHANNELS.get(id);
                State state;
                switch (id) {
                    // Kilometer values
                    case "odo":
                    case "rangeelectric":
                    case "rangeliquid":
                        state = getKilometers((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state);

                    // Percentages
                    case "soc":
                    case "tanklevelpercent":
                        state = getPercentage((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state);

                    // Contacts
                    case "decklidstatus":
                    case "doorstatusfrontleft":
                    case "doorstatusfrontright":
                    case "doorstatusrearleft":
                    case "doorstatusrearright":
                        state = getContact((JSONObject) jo.get(id));
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
                        state = getDecimal((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state);

                    // Switches
                    case "interiorLightsFront":
                    case "interiorLightsRear":
                    case "readingLampFrontLeft":
                    case "readingLampFrontRight":
                        state = getOnOffType((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state);

                    case "doorlockstatusdecklid":
                    case "doorlockstatusgas":
                        state = getOnOffTypeLock((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state);

                    // Angle
                    case "positionHeading":
                        state = getAngle((JSONObject) jo.get(id));
                        return new ChannelStateMap(ch[0], ch[1], state);
                    default:
                        logger.info("No mapping available for {}", id);
                }
            } else {
                logger.info("No mapping available for {}", id);
            }

        } else {
            logger.info("More than one key found {}", s);
        }
        return null;
    }

    private static State getOnOffType(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            boolean b = Boolean.valueOf(value);
            return OnOffType.from(b);
        } else {
            logger.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getOnOffTypeLock(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            boolean b = Boolean.valueOf(value);
            return OnOffType.from(!b);
        } else {
            logger.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getAngle(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return QuantityType.valueOf(Double.valueOf(value), Units.DEGREE_ANGLE);
        } else {
            logger.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getDecimal(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return DecimalType.valueOf(value);
        } else {
            logger.warn("JSONObject contains no value {}", jo);
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
            logger.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getKilometers(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return QuantityType.valueOf(Integer.valueOf(value), Constants.KILOMETRE_UNIT);
        } else {
            logger.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    private static State getPercentage(JSONObject jo) {
        if (jo.has(VALUE)) {
            String value = jo.get(VALUE).toString();
            return QuantityType.valueOf(Integer.valueOf(value), Units.PERCENT);
        } else {
            logger.warn("JSONObject contains no value {}", jo);
            return UnDefType.UNDEF;
        }
    }

    /**
     * Mapping of json id towards channel group and id
     */
    private static void init() {
        CHANNELS.put("odo", new String[] { "mileage", "range" });
        CHANNELS.put("rangeelectric", new String[] { "range-electric", "range" });
        CHANNELS.put("soc", new String[] { "soc", "range" });
        CHANNELS.put("rangeliquid", new String[] { "range-fuel", "range" });
        CHANNELS.put("tanklevelpercent", new String[] { "fuel-level", "range" });
        CHANNELS.put("decklidstatus", new String[] { "deck-lid", "doors" });
        CHANNELS.put("doorstatusfrontleft", new String[] { "driver-front", "doors" });
        CHANNELS.put("doorstatusfrontright", new String[] { "passenger-front", "doors" });
        CHANNELS.put("doorstatusrearleft", new String[] { "driver-rear", "doors" });
        CHANNELS.put("doorstatusrearright", new String[] { "passenger-rear", "doors" });
        CHANNELS.put("interiorLightsFront", new String[] { "interior-front", "lights" });
        CHANNELS.put("interiorLightsRear", new String[] { "interior-rear", "lights" });
        CHANNELS.put("lightswitchposition", new String[] { "light-switch", "lights" });
        CHANNELS.put("readingLampFrontLeft", new String[] { "reading-left", "lights" });
        CHANNELS.put("readingLampFrontRight", new String[] { "reading-right", "lights" });
        CHANNELS.put("rooftopstatus", new String[] { "rooftop", "doors" });
        CHANNELS.put("sunroofstatus", new String[] { "sunroof", "doors" });
        CHANNELS.put("windowstatusfrontleft", new String[] { "driver-front", "windows" });
        CHANNELS.put("windowstatusfrontright", new String[] { "passenger-front", "windows" });
        CHANNELS.put("windowstatusrearleft", new String[] { "driver-rear", "windows" });
        CHANNELS.put("windowstatusrearright", new String[] { "passenger-rear", "windows" });
        CHANNELS.put("doorlockstatusvehicle", new String[] { "doors", "lock" });
        CHANNELS.put("doorlockstatusdecklid", new String[] { "decklid", "lock" });
        CHANNELS.put("doorlockstatusgas", new String[] { "flap", "lock" });
        CHANNELS.put("positionHeading", new String[] { "heading", "location" });
    }
}
