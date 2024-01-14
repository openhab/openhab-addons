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
package org.openhab.binding.smartthings.internal.converter;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingConfig;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base converter class.
 * The converter classes are responsible for converting "state" messages from the smartthings hub into openHAB States.
 * And, converting handler.handleCommand() into messages to be sent to smartthings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public abstract class SmartthingsConverter {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsConverter.class);

    protected String smartthingsName;
    protected String thingTypeId;

    SmartthingsConverter(Thing thing) {
        smartthingsName = thing.getConfiguration().as(SmartthingsThingConfig.class).smartthingsName;
        thingTypeId = thing.getThingTypeUID().getId();
    }

    public abstract String convertToSmartthings(ChannelUID channelUid, Command command);

    public abstract State convertToOpenHab(@Nullable String acceptedChannelType,
            SmartthingsStateData dataFromSmartthings);

    /**
     * Provide a default converter in the base call so it can be used in sub-classes if needed
     *
     * @param command
     * @return The json string to send to Smartthings
     */
    protected String defaultConvertToSmartthings(ChannelUID channelUid, Command command) {
        String value;

        if (command instanceof DateTimeType dateTimeCommand) {
            value = dateTimeCommand.format("%m/%d/%Y %H.%M.%S");
        } else if (command instanceof HSBType hsbCommand) {
            value = String.format("[%d, %d, %d ]", hsbCommand.getHue().intValue(),
                    hsbCommand.getSaturation().intValue(), hsbCommand.getBrightness().intValue());
        } else if (command instanceof DecimalType) {
            value = command.toString();
        } else if (command instanceof IncreaseDecreaseType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof NextPreviousType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof OnOffType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof OpenClosedType) { // Need to surround with double quotes
            value = surroundWithQuotes(command.toString().toLowerCase());
        } else if (command instanceof PercentType) {
            value = command.toString();
        } else if (command instanceof PointType) { // There is not a comparable type in Smartthings, log and send value
            logger.warn(
                    "Warning - PointType Command is not supported by Smartthings. Please configure to use a different command type. CapabilityKey: {}, displayName: {}, capabilityAttribute {}",
                    thingTypeId, smartthingsName, channelUid.getId());
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
            logger.warn(
                    "Warning - The Smartthings converter does not know how to handle the {} command. The Smartthingsonverter class should be updated.  CapabilityKey: {}, displayName: {}, capabilityAttribute {}",
                    command.getClass().getName(), thingTypeId, smartthingsName, channelUid.getId());
            value = command.toString().toLowerCase();
        }

        return String.format(
                "{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"capabilityAttribute\": \"%s\", \"value\": %s}",
                thingTypeId, smartthingsName, channelUid.getId(), value);
    }

    protected String surroundWithQuotes(String param) {
        return (new StringBuilder()).append('"').append(param).append('"').toString();
    }

    protected State defaultConvertToOpenHab(@Nullable String acceptedChannelType,
            SmartthingsStateData dataFromSmartthings) {
        // If there is no stateMap the just return null State
        if (acceptedChannelType == null) {
            return UnDefType.NULL;
        }

        String deviceType = dataFromSmartthings.capabilityAttribute;
        Object deviceValue = dataFromSmartthings.value;

        // deviceValue can be null, handle that up front
        if (deviceValue == null) {
            return UnDefType.NULL;
        }

        switch (acceptedChannelType) {
            case "Color":
                logger.warn(
                        "Conversion of Color is not supported by the default Smartthings to opemHAB converter. The ThingType should specify an appropriate converter. Device name: {}, Attribute: {}.",
                        dataFromSmartthings.deviceDisplayName, deviceType);
                return UnDefType.UNDEF;
            case "Contact":
                return "open".equals(deviceValue) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case "DateTime":
                return UnDefType.UNDEF;
            case "Dimmer":
                // The value coming in should be a number
                if (deviceValue instanceof String stringCommand) {
                    return new PercentType(stringCommand);
                } else {
                    logger.warn("Failed to convert {} with a value of {} from class {} to an appropriate type.",
                            deviceType, deviceValue, deviceValue.getClass().getName());
                    return UnDefType.UNDEF;
                }
            case "Number":
                if (deviceValue instanceof String stringCommand2) {
                    return new DecimalType(Double.parseDouble(stringCommand2));
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
                return OnOffType.from("on".equals(deviceValue));

            // Vector3 can't be triggered now but keep it to handle acceleration device
            case "Vector3":
                // This is a weird result from Smartthings. If the messages is from a "state" request the result will
                // look like: "value":{"z":22,"y":-36,"x":-987}
                // But if the result is from sensor change via a subscription to a threeAxis device the results will
                // be a String of the format "value":"-873,-70,484"
                // which GSON returns as a LinkedTreeMap
                if (deviceValue instanceof String stringCommand3) {
                    return new StringType(stringCommand3);
                } else if (deviceValue instanceof Map<?, ?>) {
                    Map<String, String> map = (Map<String, String>) deviceValue;
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
