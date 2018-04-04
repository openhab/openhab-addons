/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal;

import static org.openhab.binding.mihome.XiaomiGatewayBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Maps the model (provided from Xiaomi) to thing.
 *
 * @author Patrick Boos - Initial contribution
 * @author Kuba Wolanin - Renamed labels
 * @author Dieter Schmidt - Refactor
 * @author Daniel Walters - Add Xiaomi Aqara Temperature, Humidity & Pressure sensor and Xiaomi Aqara Door/Window sensor
 */
public class ModelMapper {

    private static final Map<String, ThingTypeUID> THING_MAP = new HashMap<>();
    private static final Map<String, String> LABEL_MAP = new HashMap<>();
    static {
        // Alphabetical order
        THING_MAP.put("curtain", THING_TYPE_ACTOR_CURTAIN);
        THING_MAP.put("gateway", THING_TYPE_GATEWAY);
        THING_MAP.put("plug", THING_TYPE_ACTOR_PLUG);
        THING_MAP.put("ctrl_neutral1", THING_TYPE_ACTOR_AQARA1);
        THING_MAP.put("ctrl_neutral2", THING_TYPE_ACTOR_AQARA2);
        THING_MAP.put("ctrl_ln1", THING_TYPE_ACTOR_AQARA_ZERO1);
        THING_MAP.put("ctrl_ln1.aq1", THING_TYPE_ACTOR_AQARA_ZERO1);
        THING_MAP.put("ctrl_ln2", THING_TYPE_ACTOR_AQARA_ZERO2);
        THING_MAP.put("ctrl_ln2.aq1", THING_TYPE_ACTOR_AQARA_ZERO2);
        THING_MAP.put("86sw1", THING_TYPE_SENSOR_AQARA1);
        THING_MAP.put("86sw2", THING_TYPE_SENSOR_AQARA2);
        THING_MAP.put("cube", THING_TYPE_SENSOR_CUBE);
        THING_MAP.put("sensor_ht", THING_TYPE_SENSOR_HT);
        THING_MAP.put("magnet", THING_TYPE_SENSOR_MAGNET);
        THING_MAP.put("motion", THING_TYPE_SENSOR_MOTION);
        THING_MAP.put("natgas", THING_TYPE_SENSOR_GAS);
        THING_MAP.put("sensor_magnet.aq2", THING_TYPE_SENSOR_AQARA_MAGNET);
        THING_MAP.put("sensor_motion.aq2", THING_TYPE_SENSOR_AQARA_MOTION);
        THING_MAP.put("sensor_wleak.aq1", THING_TYPE_SENSOR_WATER);
        THING_MAP.put("sensor_switch.aq2", THING_TYPE_SENSOR_AQARA_SWITCH);
        THING_MAP.put("smoke", THING_TYPE_SENSOR_SMOKE);
        THING_MAP.put("switch", THING_TYPE_SENSOR_SWITCH);
        THING_MAP.put("weather.v1", THING_TYPE_SENSOR_AQARA_WEATHER_V1);

        LABEL_MAP.put("curtain", "Xiaomi Aqara Intelligent Curtain Motor");
        LABEL_MAP.put("gateway", "Xiaomi Mi Smart Home Gateway");
        LABEL_MAP.put("plug", "Xiaomi Mi Smart Socket Plug");
        LABEL_MAP.put("ctrl_neutral1", "Xiaomi Aqara Wall Switch 1 Button");
        LABEL_MAP.put("ctrl_neutral2", "Xiaomi Aqara Wall Switch 2 Button");
        LABEL_MAP.put("ctrl_ln1", "Xiaomi \"zero-fire\" 1 Channel Wall Switch");
        LABEL_MAP.put("ctrl_ln1.aq1", "Xiaomi Aqara \"zero-fire\" 1 Channel Wall Switch");
        LABEL_MAP.put("ctrl_ln2", "Xiaomi \"zero-fire\" 2 Channel Wall Switch");
        LABEL_MAP.put("ctrl_ln2.aq1", "Xiaomi \"zero-fire\" 2 Channel Wall Switch");
        LABEL_MAP.put("86sw1", "Xiaomi Aqara Smart Switch 1 Button");
        LABEL_MAP.put("86sw2", "Xiaomi Aqara Smart Switch 2 Button");
        LABEL_MAP.put("cube", "Xiaomi Mi Smart Cube");
        LABEL_MAP.put("sensor_ht", "Xiaomi Mi Temperature & Humidity Sensor");
        LABEL_MAP.put("magnet", "Xiaomi Door/Window Sensor");
        LABEL_MAP.put("motion", "Xiaomi Mi Motion Sensor");
        LABEL_MAP.put("natgas", "Xiaomi Mijia Honeywell Gas Alarm Detector");
        LABEL_MAP.put("sensor_magnet.aq2", "Xiaomi Aqara Door/Window Sensor");
        LABEL_MAP.put("sensor_motion.aq2", "Xiaomi Aqara Motion Sensor");
        LABEL_MAP.put("sensor_wleak.aq1", "Xiaomi Aqara Water Leak Sensor");
        LABEL_MAP.put("sensor_switch.aq2", "Xiaomi Aqara Wireless Switch");
        LABEL_MAP.put("smoke", "Xiaomi Mijia Honeywell Fire Alarm Detector");
        LABEL_MAP.put("switch", "Xiaomi Mi Wireless Switch");
        LABEL_MAP.put("weather.v1", "Xiaomi Aqara Temperature, Humidity & Pressure Sensor");
    }

    public static ThingTypeUID getThingTypeForModel(String model) {
        return THING_MAP.get(model);
    }

    public static String getLabelForModel(String model) {
        return LABEL_MAP.get(model);
    }
}
