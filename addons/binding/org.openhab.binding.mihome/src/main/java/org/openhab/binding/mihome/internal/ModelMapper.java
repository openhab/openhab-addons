/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
 */
public class ModelMapper {

    private static final Map<String, ThingTypeUID> thingMapper = new HashMap<String, ThingTypeUID>();
    private static final Map<String, String> labelMapper = new HashMap<String, String>();
    static {
        // Alphabetical order
        thingMapper.put("curtain", THING_TYPE_ACTOR_CURTAIN);
        thingMapper.put("gateway", THING_TYPE_GATEWAY);
        thingMapper.put("plug", THING_TYPE_ACTOR_PLUG);
        thingMapper.put("ctrl_neutral1", THING_TYPE_ACTOR_AQARA1);
        thingMapper.put("ctrl_neutral2", THING_TYPE_ACTOR_AQARA2);
        thingMapper.put("ctrl_ln1", THING_TYPE_ACTOR_WALL1);
        thingMapper.put("ctrl_ln2", THING_TYPE_ACTOR_WALL2);
        thingMapper.put("86sw1", THING_TYPE_SENSOR_AQARA1);
        thingMapper.put("86sw2", THING_TYPE_SENSOR_AQARA2);
        thingMapper.put("cube", THING_TYPE_SENSOR_CUBE);
        thingMapper.put("sensor_ht", THING_TYPE_SENSOR_HT);
        thingMapper.put("magnet", THING_TYPE_SENSOR_MAGNET);
        thingMapper.put("motion", THING_TYPE_SENSOR_MOTION);
        thingMapper.put("natgas", THING_TYPE_SENSOR_GAS);
        thingMapper.put("smoke", THING_TYPE_SENSOR_SMOKE);
        thingMapper.put("switch", THING_TYPE_SENSOR_SWITCH);

        labelMapper.put("curtain", "Xiaomi Aqara Intelligent Curtain Motor");
        labelMapper.put("gateway", "Xiaomi Mi Smart Home Gateway");
        labelMapper.put("plug", "Xiaomi Mi Smart Socket Plug");
        labelMapper.put("ctrl_neutral1", "Xiaomi Aqara Wall Switch 1 Button");
        labelMapper.put("ctrl_neutral2", "Xiaomi Aqara Wall Switch 2 Button");
        labelMapper.put("ctrl_ln1", "Xiaomi \"zero-fire\" 1 Channel Wall Switch");
        labelMapper.put("ctrl_ln2", "Xiaomi \"zero-fire\" 2 Channel Wall Switch");
        labelMapper.put("86sw1", "Xiaomi Aqara Smart Switch 1 Button");
        labelMapper.put("86sw2", "Xiaomi Aqara Smart Switch 2 Button");
        labelMapper.put("cube", "Xiaomi Mi Smart Cube");
        labelMapper.put("sensor_ht", "Xiaomi Mi Temperature & Humidity Sensor");
        labelMapper.put("magnet", "Xiaomi Door/Window Sensor");
        labelMapper.put("motion", "Xiaomi Mi Motion Sensor");
        labelMapper.put("natgas", "New, yet unconfirmed device");
        labelMapper.put("smoke", "New, yet unconfirmed device");
        labelMapper.put("switch", "Xiaomi Mi Wireless Switch");
    }

    public static ThingTypeUID getThingTypeForModel(String model) {
        return thingMapper.get(model);
    }

    public static String getLabelForModel(String model) {
        return labelMapper.get(model);
    }
}
