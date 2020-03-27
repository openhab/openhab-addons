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

import java.awt.Color;

import com.google.gson.Gson;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.webthings.internal.dto.ItemStateEventPayload;
import org.openhab.binding.webthings.internal.dto.WebThingsPropertyCommand;

/**
 * The {@link OpenhabToWebThingConverter} is responsible to handle all interactions with the webthing framework
 *
 * @author schneider_sven - Initial contribution
 */
@NonNullByDefault
public class OpenhabToWebThingConverter {

    /**
     * Get websocket command for webthing based on openhab itemStateEvent
     * @param id ID of webthing
     * @param property Name of channel (should equal parameter name) which shall be modified
     * @param command Payload with command type and new value
     * @return Finished JSON string
     */
    public static String getPropertyFromCommand(String id , String property ,ItemStateEventPayload command){
        String jsonString = null;

        Gson g = new Gson();
        WebThingsPropertyCommand propertyCommand;
        if(id != ""){
            propertyCommand = new WebThingsPropertyCommand(id, "setProperty");
        }else{
            propertyCommand = new WebThingsPropertyCommand("setProperty");
        }

        // Switch possible command types from: https://www.eclipse.org/smarthome/documentation/javadoc/org/eclipse/smarthome/core/types/Command.html
        // TODO: Add more command transformations
        switch (command.getType()) {
            case "DateTime":
                String dateTime = command.getValue();
                dateTime = dateTime.substring(dateTime.indexOf("T")+1, dateTime.lastIndexOf(":")) + " - " + dateTime.substring(0, dateTime.indexOf("T"));
                propertyCommand.addData(property, dateTime); 
                break;
            case "Decimal":
                Float decimal = Float.parseFloat(command.getValue());             
                propertyCommand.addData(property, decimal);  
                break;
            case "HSB":
                String hsb = command.getValue();

                // Get HSB values
                Float hue = Float.parseFloat(hsb.substring(0, hsb.indexOf(",")));
                Float saturation = Float.parseFloat(hsb.substring(hsb.indexOf(",") +1, hsb.lastIndexOf(",")));
                Float brightness = Float.parseFloat(hsb.substring(hsb.lastIndexOf(",") +1, hsb.length()));

                // Convert HSB to RGB and then to HTML hex
                Color rgb = Color.getHSBColor(hue/360, saturation/100, brightness/100);
                String hex = String.format("#%02x%02x%02x", rgb.getRed(), rgb.getGreen(), rgb.getBlue());

                propertyCommand.addData(property, hex);
                break;
            case "IncreaseDecrease":
                break;
            case "NextPrevious":
                break;
            case "OnOff":
                boolean onOff;
                if(command.getValue().equals("ON")){
                    onOff = true;
                }else{
                    onOff = false;
                }
                propertyCommand.addData(property, onOff);
                break;
            case "OpenClosed":
                boolean openClosed;
                if(command.getValue().equals("OPEN")){
                    openClosed = true;
                }else{
                    openClosed = false;
                }
                propertyCommand.addData(property, openClosed);
            case "Percent":
                Integer percent = Math.round(Float.parseFloat(command.getValue()));             
                propertyCommand.addData(property, percent);          
                break;
            case "PlayPause":
                break;
            case "Point":
                String point = command.getValue();
                propertyCommand.addData(property, point); 
                break;
            case "Refresh":
                break;
            case "RewindFastfordward":
                break;
            case "StopMove":
                break;
            case "StringList":
                String stringList = command.getValue();
                propertyCommand.addData(property, stringList); 
                break;
            case "String":
                String string = command.getValue();
                propertyCommand.addData(property, string); 
                break;
            case "UpDown":
                break;
            default:
                jsonString = null;
                break;
        }
        jsonString = g.toJson(propertyCommand);

        return jsonString;
    }
}
