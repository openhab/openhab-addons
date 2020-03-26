/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal.converters;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.types.Command;

import java.awt.Color;

import org.eclipse.smarthome.core.library.types.*;

/**
 * The {@link WebThingsHandler} is responsible to handle all interactions to
 * convert/import WebThings to openHAB
 *
 * @author schneider_sven - Initial contribution
 */
public class WebThingToOpenhabConverter {

    /**
     * Extracts the relevant information from the properties of a WebThing to create a channel
     * @param propertyName Property of WebThing to convert into channel
     * @param properties Properties of WebThing
     * @return Map with itemType, label, description and defaultTag
     */
    public static Map<String, String> getChannelInfoFromProperty(String propertyName, JsonObject properties) {
        JsonObject propertyData = properties.getAsJsonObject(propertyName);

        Map<String, String> channelMetaInfo = new HashMap<String, String>();
        String itemType = "";
        String defaultTag = "";

        switch (propertyData.get("@type").getAsString()) {
            case "AlarmProperty":
                itemType ="Switch";
                defaultTag = "Switchable";
                break;
            case "BooleanProperty":
                itemType ="Switch";
                defaultTag = "Switchable";
                break;
            case "BrightnessProperty":
                itemType = "Dimmer";
                break;
            case "ColorModeProperty":
                itemType = "String";
                defaultTag = "Lighting";
                break;
            case "ColorProperty":
                itemType = "Color";
                defaultTag = "Lighting";
                break;
            case "ColorTemperatureProperty":
                itemType = "Dimmer";
                defaultTag = "Lighting";
                break;
            case "CurrentProperty":
                itemType = "Number";
                break;
            case "FrequencyProperty":
                itemType ="Number";
                break;
            case "HeatingCoolingProperty":
                itemType ="String";
                break;
            case "ImageProperty":
                itemType ="String";
                break;
            case "InstantaneousPowerProperty":
                itemType ="Number";
                break;
            case "LeakProperty":
                itemType ="Switch";
                defaultTag = "Switchable";
                break;
            case "LevelProperty":
                if(propertyData.has("unit") && propertyData.get("unit").getAsString().equals("percent")){
                    itemType ="Dimmer";
                } else{
                    itemType ="Number";
                }
                break;
            case "LockedProperty":
                itemType ="Switch";
                defaultTag = "Switchable";
                break;
            case "MotionProperty":
                itemType ="Switch";
                defaultTag = "Switchable";
                break;
            case "OnOffProperty":
                itemType = "Switch";
                defaultTag = "Switchable";
                break;
            case "OpenProperty":
                itemType ="Contact";
                defaultTag = "ContactSensor";
                break;
            case "PushedProperty":
                itemType ="Switch";
                defaultTag = "Switchable";
                break;
            case "TargetTemperatureProperty":
                itemType ="Number";
                defaultTag = "TargetTemperature";
                break;
            case "TemperatureProperty":
                itemType ="Number";
                defaultTag = "CurrentTemperature";
                break;
            case "ThermostatModeProperty":
                itemType ="String";
                break;
            case "VideoProperty":
                itemType ="String";
                break;
            case "VoltageProperty":
                itemType ="Number";
                break;
            default:
                switch (propertyData.get("type").getAsString()) {
                    case "boolean":
                        itemType = "Switch";
                        defaultTag = "Switchable";
                        break;
                    case "string":
                        itemType = "String";
                    case "integer":
                    case "number":
                        itemType = "Number";
                    default:
                        itemType = "";
                        break;
                }
                break;
        }
        channelMetaInfo.put("itemType", itemType);
        if(propertyData.has("title")){
            channelMetaInfo.put("label", propertyData.get("title").getAsString());
        }
        if(propertyData.has("description")){
            channelMetaInfo.put("description", propertyData.get("description").getAsString()); 
        }
        channelMetaInfo.put("defaultTag", defaultTag);  

        return channelMetaInfo;
    }

    /**
     * Get Command for openHAB channel based on webThing command
     * @param itemType ItemType of openHAB item
     * @param value Command with value to be transformed
     * @return Finished JSON string
     */
    @NonNull
    public static Command getCommandFromProperty(@NonNull String itemType, @NonNull String value){
        Command ohCommand = StringType.valueOf("EmptyCommand");

        // TODO: Add more command transformations
        switch (itemType) {
            case "Color":
                if(!value.contains("#")){
                    value = "#" + value;
                }
                Color rgb = Color.decode(value);
                float[] hsb = new float[3];
                hsb = Color.RGBtoHSB(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), hsb);

                String hsbString = String.valueOf(hsb[0]*360) + "," + String.valueOf(hsb[1]*100) + "," + String.valueOf(hsb[2]*100);
                ohCommand = HSBType.valueOf(hsbString);
                break;
            case "Contact":
                if(value.equals("true")){
                    ohCommand = OpenClosedType.OPEN;
                }else{
                    ohCommand = OpenClosedType.CLOSED;
                }
                break;
            case "DateTime":
                ohCommand = DateTimeType.valueOf(value);
                break;
            case "Dimmer":
                ohCommand = PercentType.valueOf(value);
                break;
            case "Group":
                break;
            case "Image":
                break;
            case "Location":
                ohCommand = PointType.valueOf(value);
                break;
            case "Number":
                ohCommand = DecimalType.valueOf(value);
                break;
            case "Player":
                break;
            case "Rollershutter":
                ohCommand = PercentType.valueOf(value);
                break;
            case "String":
                ohCommand = StringType.valueOf(value);
                break;
            case "Switch":
                if(value.equals("true")){
                    ohCommand = OnOffType.ON;
                }else{
                    ohCommand = OnOffType.OFF;
                }
                break;     
            default:
                ohCommand = StringType.valueOf("EmptyCommand");
                break;
        }
        return ohCommand;
    }
}
