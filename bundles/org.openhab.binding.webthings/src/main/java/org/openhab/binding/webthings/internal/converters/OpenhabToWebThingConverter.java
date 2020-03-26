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

import static org.openhab.binding.webthings.internal.WebThingsBindingGlobals.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.items.dto.ItemDTO;
import org.eclipse.smarthome.core.thing.dto.ChannelDTO;
import org.eclipse.smarthome.core.thing.dto.ThingDTO;
import org.eclipse.smarthome.io.rest.core.thing.EnrichedChannelDTO;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mozilla.iot.webthing.Property;
import org.mozilla.iot.webthing.Thing;
import org.mozilla.iot.webthing.Value;
import org.mozilla.iot.webthing.example.SingleThing.FadeAction;
import org.openhab.binding.webthings.internal.dto.ItemStateEventPayload;
import org.openhab.binding.webthings.internal.dto.CompleteThingDTO;
import org.openhab.binding.webthings.internal.dto.WebThingsPropertyCommand;

/**
 * The {@link OpenhabToWebThingConverter} is responsible to handle all interactions with the webthing framework
 *
 * @author schneider_sven - Initial contribution
 */
@NonNullByDefault
public class OpenhabToWebThingConverter {
    private List<Thing> webThingList = new ArrayList<Thing>();

    /**
     * Add single WebThing to ThingList
     * @param thing WebThing
     */
    public void addThing(Thing thing){
        webThingList.add(thing);
    }

    /**
     * Add multiple WebThings to ThingList
     * @param thingList List of WebThings
     */
    public void addThingList(List<Thing> thingList){
        webThingList.addAll(thingList);
    }

    /**
     * @return the webThingList
     */
    public List<Thing> getWebThingList() {
        return webThingList;
    }

    /**
     * Create a WebThing without stating explicit @types
     * @param ohThing openHAB thing to be converted into WebThing
     * @return WebThing
     */
    public Thing createCustomThing(ThingDTO ohThing){
        JSONArray types = new JSONArray();

        // If Mozilla capabilities should be matched import channel tags
        if(mozilla){
            for(ChannelDTO channel: ohThing.channels){
                Set<String> capabilities = getTypes(channel.defaultTags);
                for(String c : capabilities){
                    types.put(c);
                }
            }
        }

        // Create WebThing
        Thing wotThing = new Thing(ohThing.UID, ohThing.label, new JSONArray(), ohThing.properties.get("productName"));

        // https://www.openhab.org/docs/configuration/items.html
        // Create Properties based on channels, creation based on linked Items not possible via normal ThingDTO
        for(ChannelDTO channel: ohThing.channels){
            JSONObject description = new JSONObject();
            switch (channel.itemType) {
                    case "Color":
                    description.put("@type", "ColorProperty");
                    description.put("title", channel.label);
                    description.put("type", "string");
                    description.put("description",channel.description);
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing,channel.id, new Value("FFFFFF"),description));
                    break;
                case "Contact":
                    description.put("@type", "OpenProperty");
                    description.put("title", channel.label);
                    description.put("type", "boolean");
                    description.put("description", channel.description);
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing,channel.id,new Value(false), description));
                    break;
                case "DateTime":
                    break;
                case "Dimmer":
                    description.put("@type", "LevelProperty");
                    description.put("title", channel.label);
                    description.put("type", "integer");
                    description.put("description", channel.description);
                    description.put("minimum", 0);
                    description.put("maximum", 100);
                    description.put("unit", "percent");
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing,channel.id,new Value(50), description));
                    break;
                case "Group":
                    break;
                case "Image":
                    description.put("@type", "ImageProperty");
                    description.put("title", channel.label);
                    description.put("type", "null");
                    description.put("description", channel.description);
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing,channel.id,new Value(null), description));

                    //TODO: The primitive type of an ImageProperty is null or undefined (omitted), but it must provide a mediaType in one or more link relations which link to binary representations of the image property resource.
                    break;
                case "Location":
                    description.put("title", channel.label);
                    description.put("type", "string");
                    description.put("description", channel.description);
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing, channel.id, new Value(""), description));
                    break;
                case "Number":
                    description.put("@type", "LevelProperty");
                    description.put("title", channel.label);
                    description.put("type", "integer");
                    description.put("description", channel.description);
                    description.put("minimum", 0);
                    description.put("maximum", Integer.MAX_VALUE);
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing, channel.id, new Value(50), description));
                    break;
                case "Player":
                    // Add suitable action
                    break;
                case "Rollershutter":
                    // Add suitable action
                    break;
                case "String":
                    description.put("title", channel.label);
                    description.put("type", "string");
                    description.put("description", channel.description);
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing, channel.id, new Value(""), description));
                    break;
                case "Switch":
                    description.put("@type", "OnOffProperty");
                    description.put("title", channel.label);
                    description.put("type", "boolean");
                    description.put("description", channel.description);
                    description.put("channelTypeUID", channel.channelTypeUID);
                    wotThing.addProperty(new Property(wotThing, channel.id, new Value(true), description));
                    break;
                default:
                    break;
            }
        }
        return wotThing;
    }

    /**
     * Create a WebThing without stating explicit @types
     * @param ohThing openHAB thing to be converted into WebThing
     * @return WebThing
     */
    public Thing createCustomThing(CompleteThingDTO ohThing, Map<String, ItemDTO> ohItems){
        JSONArray types = new JSONArray();

        // If Mozilla capabilities should be matched import channel tags
        if(mozilla){
            for(EnrichedChannelDTO channel: ohThing.channels){
                Set<String> capabilities = getTypes(channel.defaultTags);
                for(String c : capabilities){
                    types.put(c);
                }
            }
        }

        // Create WebThing
        Thing wotThing = new Thing(ohThing.UID, ohThing.label, types, ohThing.properties.get("productName"));

        // https://www.openhab.org/docs/configuration/items.html
        // Create Properties based on linked items
        for(EnrichedChannelDTO channel: ohThing.channels){
            for(String linkedChannel: channel.linkedItems){
                String propertyName = linkedChannel.replace(ohThing.UID.replace(":", "_") + "_", "");

                if(ohItems.containsKey(linkedChannel)){
                    ItemDTO item = ohItems.get(linkedChannel);
                    String propertyType = ohItems.get(linkedChannel).type;
                 
                    JSONObject description = new JSONObject();
                    switch (propertyType) {
                            case "Color":
                            description.put("@type", "ColorProperty");
                            description.put("title", item.label);
                            description.put("type", "string");
                            description.put("description",channel.description);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing,propertyName , new Value("FFFFFF"),description));
                            break;
                        case "Contact":
                            description.put("@type", "OpenProperty");
                            description.put("title", item.label);
                            description.put("type", "boolean");
                            description.put("description", channel.description);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName, new Value(false), description));
                            break;
                        case "DateTime":
                            description.put("title", item.label);
                            description.put("type", "string");
                            description.put("description", channel.description);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName, new Value(""), description));
                            break;
                        case "Dimmer":
                            description.put("@type", "LevelProperty");
                            description.put("title", item.label);
                            description.put("type", "integer");
                            description.put("description", channel.description);
                            description.put("minimum", 0);
                            description.put("maximum", 100);
                            description.put("unit", "percent");
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName,new Value(50), description));
                            break;
                        case "Group":
                            break;
                        case "Image":
                            description.put("@type", "ImageProperty");
                            description.put("title", item.label);
                            description.put("type", "null");
                            description.put("description", channel.description);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName,new Value(null), description));

                            //TODO: The primitive type of an ImageProperty is null or undefined (omitted), but it must provide a mediaType in one or more link relations which link to binary representations of the image property resource.
                            break;
                        case "Location":
                            description.put("title", item.label);
                            description.put("type", "string");
                            description.put("description", channel.description);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName , new Value(""), description));
                            break;
                        case "Number":
                            description.put("@type", "LevelProperty");
                            description.put("title", item.label);
                            description.put("type", "integer");
                            description.put("description", channel.description);
                            description.put("minimum", 0);
                            description.put("maximum", Integer.MAX_VALUE);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName , new Value(50), description));
                            break;
                        case "Player":
                            // Add suitable action
                            break;
                        case "Rollershutter":
                            // Add suitable action
                            break;
                        case "String":
                            description.put("title", item.label);
                            description.put("type", "string");
                            description.put("description", channel.description);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName, new Value(""), description));
                            break;
                        case "Switch":
                            description.put("@type", "OnOffProperty");
                            description.put("title", item.label);
                            description.put("type", "boolean");
                            description.put("description", channel.description);
                            description.put("channelTypeUID", channel.channelTypeUID);
                            wotThing.addProperty(new Property(wotThing, propertyName, new Value(true), description));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return wotThing;
    }

    /**
     * Get Mozilla WebThing capabilities/types
     * Tags: https://www.openhab.org/docs/developer/bindings/thing-xml.html#default-tags
     * Capabilities: https://iot.mozilla.org/schemas/#capabilities
     * @param tag Default tag of channel which shall be matched to a WebThings capability
     * @return type (empty if "no match" option is selected)
     */
    private Set<String> getTypes(Set<String> tags){
        Set<String> capabilities = new HashSet<String>();
        for(String tag: tags){
            switch (tag) {
                case "Lighting":
                    capabilities.add("Light");
                    capabilities.add("ColorControl");
                    break;
                case "Switchable":
                    capabilities.add("OnOffSwitch");
                    break;
                case "CurrentTemperature":
                    capabilities.add("TemperatureSensor");
                    capabilities.add("Thermostat");
                    capabilities.add("MultiLevelSensor");
                    break;
                case "TargetTemperature":
                    capabilities.add("Thermostat");
                    capabilities.add("MultiLevelSensor");
                    break;
                case "CurrentHumidity":
                    capabilities.add("MultiLevelSensor");
                    break;    
                default:
                    break;
            }
        }
        return capabilities;
    }

    /**
     * Remove WebThing form WebThing Server
     * @param ohthing openHAB ThingDTO
     */
    public void removeThing(ThingDTO ohthing){
        Iterator<Thing> iter = webThingList.iterator();
        while(iter.hasNext()){
            Thing wotThing = (Thing) iter.next();
            if(wotThing.getId().contains(ohthing.UID)){
                iter.remove();
            }
        }
    }

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

    /**
     * Create sample WebThing
     * @return the samlple WebThing
     */
    public static Thing makeTestThing() {
        Thing thing = new Thing("urn:dev:ops:my-lamp-1234",
                                "My Lamp",
                                new JSONArray(Arrays.asList("OnOffSwitch",
                                                            "Light")),
                                "A web connected lamp");

        JSONObject onDescription = new JSONObject();
        onDescription.put("@type", "OnOffProperty");
        onDescription.put("title", "On/Off");
        onDescription.put("type", "boolean");
        onDescription.put("description", "Whether the lamp is turned on");
        thing.addProperty(new Property(thing,
                                       "on",
                                       new Value(true),
                                       onDescription));

        JSONObject brightnessDescription = new JSONObject();
        brightnessDescription.put("@type", "BrightnessProperty");
        brightnessDescription.put("title", "Brightness");
        brightnessDescription.put("type", "integer");
        brightnessDescription.put("description",
                                  "The level of light from 0-100");
        brightnessDescription.put("minimum", 0);
        brightnessDescription.put("maximum", 100);
        brightnessDescription.put("unit", "percent");
        thing.addProperty(new Property(thing,
                                       "brightness",
                                       new Value(50),
                                       brightnessDescription));

        JSONObject fadeMetadata = new JSONObject();
        JSONObject fadeInput = new JSONObject();
        JSONObject fadeProperties = new JSONObject();
        JSONObject fadeBrightness = new JSONObject();
        JSONObject fadeDuration = new JSONObject();
        fadeMetadata.put("title", "Fade");
        fadeMetadata.put("description", "Fade the lamp to a given level");
        fadeInput.put("type", "object");
        fadeInput.put("required",
                      new JSONArray(Arrays.asList("brightness", "duration")));
        fadeBrightness.put("type", "integer");
        fadeBrightness.put("minimum", 0);
        fadeBrightness.put("maximum", 100);
        fadeBrightness.put("unit", "percent");
        fadeDuration.put("type", "integer");
        fadeDuration.put("minimum", 1);
        fadeDuration.put("unit", "milliseconds");
        fadeProperties.put("brightness", fadeBrightness);
        fadeProperties.put("duration", fadeDuration);
        fadeInput.put("properties", fadeProperties);
        fadeMetadata.put("input", fadeInput);
        thing.addAvailableAction("fade", fadeMetadata, FadeAction.class);

        JSONObject overheatedMetadata = new JSONObject();
        overheatedMetadata.put("description",
                               "The lamp has exceeded its safe operating temperature");
        overheatedMetadata.put("type", "number");
        overheatedMetadata.put("unit", "degree celsius");
        thing.addAvailableEvent("overheated", overheatedMetadata);

        return thing;
    }
}
