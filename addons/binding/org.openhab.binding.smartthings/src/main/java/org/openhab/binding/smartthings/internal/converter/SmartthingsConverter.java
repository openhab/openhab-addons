/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.converter;

import java.util.Map;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.PointType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringListType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.smartthings.config.SmartthingsThingConfig;
import org.openhab.binding.smartthings.internal.SmartthingsStateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base converter class.
 * The converter classes are responsible for converting "state" messages from the smartthings hub into openHAB States.
 * And, converting handler.handleCommand() into messages to be sent to smartthings
 *
 * @author Bob Raker - Initial contribution
 *
 */
public abstract class SmartthingsConverter {

    private Logger logger = LoggerFactory.getLogger(SmartthingsConverter.class);

    protected String smartthingsName;
    protected String thingTypeId;

    SmartthingsConverter(String name) {
        smartthingsName = name;
    }

    SmartthingsConverter(Thing thing) {
        smartthingsName = thing.getConfiguration().as(SmartthingsThingConfig.class).smartthingsName;
        thingTypeId = thing.getThingTypeUID().getId();
    }

    public abstract String convertToSmartthings(ChannelUID channelUid, Command command);

    public abstract State convertToOpenHab(String acceptedChannelType, SmartthingsStateData dataFromSmartthings);

    /**
     * Provide a default converter in the base call so it can be used in sub-classes if needed
     *
     * @param command
     * @return The json string to send to Smartthings
     */
    protected String defaultConvertToSmartthings(ChannelUID channelUid, Command command) {
        String value;

        if (command instanceof DateTimeType) {
            DateTimeType dt = (DateTimeType) command;
            value = dt.format("%m/%d/%Y %H.%M.%S");
        } else if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            value = String.format("[%d, %d, %d ]", hsb.getHue().intValue(), hsb.getSaturation().intValue(),
                    hsb.getBrightness().intValue());
        } else if (command instanceof DecimalType) {
            value = command.toString();
        } else if (command instanceof IncreaseDecreaseType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof NextPreviousType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof OnOffType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof OpenClosedType) { // Need to surround with double quotes
            // OpenClosedType needs some tweeking. OpenClosedType.OPEN is fine but if the type is OpenClosedType.CLOSED
            // need to send close, not closed.
            // String commandStr = (command.toString().equalsIgnoreCase("open")) ? "open" : "close";
            // value = surroundWithQuotes(commandStr.toLowerCase());
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof PercentType) {
            value = command.toString();
        } else if (command instanceof PointType) { // Not really sure how to deal with this one and don't see a use for
                                                   // it in Smartthings right now
            value = command.toFullString();
        } else if (command instanceof RefreshType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof RewindFastforwardType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof StopMoveType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof PlayPauseType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof StringListType) {
            value = surroundWithQuotes(command.toString());
        } else if (command instanceof StringType) {
            value = surroundWithQuotes(command.toString());
        } else if (command instanceof UpDownType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else {
            value = command.toString().toLowerCase();
        }

        String jsonMsg = String.format(
                "{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"capabilityAttribute\": \"%s\", \"value\": %s}",
                thingTypeId, smartthingsName, channelUid.getId(), value);

        return jsonMsg;
    }

    private String surroundWithQuotes(String param) {
        return (new StringBuilder()).append('"').append(param).append('"').toString();
    }

    protected State defaultConvertToOpenHab(String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        // If there is no stateMap the just return null State
        if (dataFromSmartthings == null) {
            return UnDefType.NULL;
        }

        String deviceType = dataFromSmartthings.getCapabilityAttribute();
        Object deviceValue = dataFromSmartthings.getValue();

        switch (acceptedChannelType) {
            case "Color":
                logger.warn(
                        "Conversion of Color Contol-color is not currently supported. Need to provide support for message {}.",
                        deviceValue);
                return UnDefType.UNDEF;
            case "Contact":
                return "open".equals(deviceValue) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case "DateTime":
                return UnDefType.UNDEF;
            case "Dimmer":
                // The value coming in should be a number
                if (deviceValue instanceof String) {
                    return new PercentType((String) deviceValue);
                } else {
                    logger.warn("Failed to convert {} with a value of {} from class {} to an appropriate type.",
                            deviceType, deviceValue, deviceValue.getClass().getName());
                    return UnDefType.UNDEF;
                }
            case "Number":
                if (deviceValue == null) {
                    logger.warn("Failed to convert Number {} because the value is null.", deviceType);
                    return UnDefType.UNDEF;
                } else if (deviceValue instanceof String) {
                    return new DecimalType(Double.parseDouble((String) deviceValue));
                } else if (deviceValue instanceof Double) {
                    return new DecimalType((Double) deviceValue);
                } else if (deviceValue instanceof Long) {
                    return new DecimalType((Long) deviceValue);
                } else {
                    logger.warn("Failed to convert Number {} with a value of {} from class {} to an appropriate type.",
                            deviceType, deviceValue, deviceValue.getClass().getName());
                    return UnDefType.UNDEF;
                }
            case "Player":
                logger.warn("Conversion of Player is not currently supported. Need to provide support for message {}.",
                        deviceValue);
                return UnDefType.UNDEF;
            case "Rollershutter":
                return "open".equals(deviceValue) ? UpDownType.DOWN : UpDownType.UP;
            case "String":
                return new StringType((String) deviceValue);
            case "Switch":
                return "on".equals(deviceValue) ? OnOffType.ON : OnOffType.OFF;

            // Vector3 can't be triggered now but keep it to handle acceleration device
            case "Vector3":
                // This is a weird result from Smartthings. If the messages is from a "state" request the result will
                // look like: "value":{"z":22,"y":-36,"x":-987}
                // But if the result is from sensor change via a subscription to a a threeAxis device the results will
                // be a String of the format "value":"-873,-70,484"
                // which GSON returns as a LinkedTreeMap
                if (deviceValue instanceof String) {
                    return new StringType((String) deviceValue);
                } else if (deviceValue instanceof Map) {
                    Map map = (Map) deviceValue;
                    String s = String.format("%.0f,%.0f,%.0f", map.get("x"), map.get("y"), map.get("z"));
                    return new StringType(s);
                } else {
                    logger.warn(
                            "Unable to convert {} which should be in Smartthings Vector3 format to a string. The returned datatype from Smartthings is {}.",
                            deviceType, deviceValue.getClass().getName());
                    return UnDefType.UNDEF;
                }
            default:
                logger.warn("No type defined to convert {} with a value of {} from class {} to an appropriate type.",
                        deviceType, deviceValue, deviceValue.getClass().getName());
                return UnDefType.UNDEF;
        }
    }

}
