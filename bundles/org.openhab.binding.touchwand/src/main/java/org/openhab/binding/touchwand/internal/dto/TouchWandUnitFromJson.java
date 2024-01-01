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
package org.openhab.binding.touchwand.internal.dto;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link TouchWandUnitFromJson} parse Json unit data
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandUnitFromJson {

    private static final Logger logger = LoggerFactory.getLogger(TouchWandUnitFromJson.class);

    public TouchWandUnitFromJson() {
    }

    public static TouchWandUnitData parseResponse(JsonObject jsonUnit) {
        final Gson gson = new Gson();
        TouchWandUnitData touchWandUnit;
        String type = jsonUnit.get("type").getAsString();
        if (!Arrays.asList(SUPPORTED_TOUCHWAND_TYPES).contains(type)) {
            type = TYPE_UNKNOWN;
        }

        switch (type) {
            case TYPE_WALLCONTROLLER:
                touchWandUnit = gson.fromJson(jsonUnit, TouchWandUnitDataWallController.class);
                break;
            case TYPE_SWITCH:
                touchWandUnit = gson.fromJson(jsonUnit, TouchWandShutterSwitchUnitData.class);
                break;
            case TYPE_DIMMER:
                touchWandUnit = gson.fromJson(jsonUnit, TouchWandShutterSwitchUnitData.class);
                break;
            case TYPE_SHUTTER:
                touchWandUnit = gson.fromJson(jsonUnit, TouchWandShutterSwitchUnitData.class);
                break;
            case TYPE_ALARMSENSOR:
                Gson builder = new GsonBuilder()
                        .registerTypeAdapter(TouchWandUnitDataAlarmSensor.class, new AlarmSensorUnitDataDeserializer())
                        .create();
                touchWandUnit = builder.fromJson(jsonUnit, TouchWandUnitDataAlarmSensor.class);
                break;
            case TYPE_BSENSOR:
                touchWandUnit = gson.fromJson(jsonUnit, TouchWandBSensorUnitData.class);
                break;
            case TYPE_THERMOSTAT:
                touchWandUnit = gson.fromJson(jsonUnit, TouchWandThermostatUnitData.class);
                break;
            case TYPE_UNKNOWN:
                touchWandUnit = new TouchWandUnknownTypeUnitData();
                break;
            default:
                touchWandUnit = new TouchWandUnknownTypeUnitData();
        }

        if (touchWandUnit == null) {
            touchWandUnit = new TouchWandUnknownTypeUnitData();
        }

        return touchWandUnit;
    }

    public static TouchWandUnitData parseResponse(String JsonUnit) {
        TouchWandUnitData myTouchWandUnitData;
        JsonObject unitObj;
        try {
            unitObj = JsonParser.parseString(JsonUnit).getAsJsonObject();
            myTouchWandUnitData = parseResponse(unitObj);
        } catch (JsonParseException | IllegalStateException e) {
            logger.warn("Could not parse response {}", JsonUnit);
            myTouchWandUnitData = new TouchWandUnknownTypeUnitData(); // Return unknown type
        }
        return myTouchWandUnitData;
    }
}
