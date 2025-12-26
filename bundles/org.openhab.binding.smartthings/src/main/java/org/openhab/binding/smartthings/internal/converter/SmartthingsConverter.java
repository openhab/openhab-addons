/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

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

    protected SmartthingsTypeRegistry typeRegistry;
    protected Gson gson = new Gson();

    SmartthingsConverter(SmartthingsTypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public String convertToSmartthings(Thing thing, ChannelUID channelUid, Command command)
            throws SmartthingsException {
        convertToSmartthingsInternal(thing, channelUid, command);
        String jsonMsg = getJSonCommands();
        return jsonMsg;
    }

    public State convertToOpenHab(Thing thing, ChannelUID channelUid, Object dataFromSmartthings)
            throws SmartthingsException {
        State result = convertToOpenHabInternal(thing, channelUid, dataFromSmartthings);
        return result;
    }

    public abstract void convertToSmartthingsInternal(Thing thing, ChannelUID channelUid, Command command)
            throws SmartthingsException;

    public abstract State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartthings)
            throws SmartthingsException;

    private SmartthingsActions smartthingsActions = new SmartthingsActions();

    private class SmartthingsActions {
        Queue<SmartthingsAction> commands = new LinkedList<SmartthingsAction>();

        public void pushCommand(@Nullable String component, @Nullable String capability, @Nullable String cmdName,
                Object @Nullable [] arguments) {
            commands.add(new SmartthingsAction(component, capability, cmdName, arguments));
        }
    }

    private class SmartthingsAction {
        @Nullable
        public String component;
        @Nullable
        public String capability;
        @Nullable
        public String command;
        @Nullable
        public Object @Nullable [] arguments;

        public SmartthingsAction(@Nullable String component, @Nullable String capability, @Nullable String command,
                Object @Nullable [] arguments) {
            this.component = component;
            this.capability = capability;
            this.command = command;
            this.arguments = arguments;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("component:" + component + "\n");
            builder.append("capability:" + capability + "\n");
            builder.append("command:" + command + "\n");
            builder.append("arguments:" + arguments + "\n");
            return builder.toString();
        }
    }

    public void pushCommand(@Nullable String component, @Nullable String capability, @Nullable String command,
            Object @Nullable [] arguments) {
        smartthingsActions.pushCommand(component, capability, command, arguments);
    }

    private String getJSonCommands() {
        String result = gson.toJson(smartthingsActions);
        result = result.replace("\\u003d", "=");
        smartthingsActions.commands.clear();
        return result;
    }

    protected String surroundWithQuotes(String param) {
        return (new StringBuilder()).append('"').append(param).append('"').toString();
    }

    protected State defaultConvertToOpenHab(Thing thing, ChannelUID channelUid, Object dataFromSmartthings) {
        Channel channel = thing.getChannel(channelUid);
        if (channel == null) {
            // @todo : review, need handling this case
            logger.error("Channel not found: {}", channelUid);
            return UnDefType.UNDEF;
        }
        String acceptedChannelType = channel.getAcceptedItemType();
        String uoM = "";
        if (acceptedChannelType == null) {
            return UnDefType.NULL;
        }

        if (acceptedChannelType.contains(":")) {
            int posSep = acceptedChannelType.indexOf(":");
            if (posSep < acceptedChannelType.length() - 1) {
                uoM = acceptedChannelType.substring(posSep + 1);
            } else {
                uoM = "";
            }
            if (posSep > 0) {
                acceptedChannelType = acceptedChannelType.substring(0, posSep);
            } else {
                acceptedChannelType = "";
            }
        }

        switch (acceptedChannelType) {
            case SmartthingsBindingConstants.TYPE_COLOR:
                logger.warn(
                        "Conversion of Color is not supported by the default Smartthings to opemHAB converter. The ThingType should specify an appropriate converter");
                return UnDefType.UNDEF;

            case SmartthingsBindingConstants.TYPE_CONTACT:
                return SmartthingsBindingConstants.OPEN_VALUE.equals(dataFromSmartthings) ? OpenClosedType.OPEN
                        : OpenClosedType.CLOSED;

            case SmartthingsBindingConstants.TYPE_DATETIME:
                return UnDefType.UNDEF;

            case SmartthingsBindingConstants.TYPE_DIMMER:
                // The value coming in should be a number
                if (dataFromSmartthings instanceof String stringCommandDimmer) {
                    return new PercentType(stringCommandDimmer);
                }
                if (dataFromSmartthings instanceof Double doubleCommandDimmer) {
                    return new PercentType(new BigDecimal(doubleCommandDimmer));
                } else {
                    logger.warn("Failed to convert with a value of {} from class {} to an appropriate type.",
                            dataFromSmartthings, dataFromSmartthings.getClass().getName());
                    return UnDefType.UNDEF;
                }

            case SmartthingsBindingConstants.TYPE_NUMBER:
                if (dataFromSmartthings instanceof String stringCommandNumber) {
                    // review this, oven return a bad value of format 00:00:00
                    try {
                        return new DecimalType(Double.parseDouble(stringCommandNumber));
                    } catch (NumberFormatException ex) {
                        return new DecimalType(0.00);
                    }
                } else if (dataFromSmartthings instanceof Double) {
                    return new DecimalType((Double) dataFromSmartthings);
                } else if (dataFromSmartthings instanceof Long) {
                    return new DecimalType((Long) dataFromSmartthings);
                } else {
                    logger.warn("Failed to convert Number with a value of {} from class {} to an appropriate type.",
                            dataFromSmartthings, dataFromSmartthings.getClass().getName());
                    return UnDefType.UNDEF;
                }

            case SmartthingsBindingConstants.TYPE_PLAYER:
                logger.warn("Conversion of Player is not currently supported. Need to provide support for message {}.",
                        dataFromSmartthings);
                return UnDefType.UNDEF;

            case SmartthingsBindingConstants.TYPE_ROLLERSHUTTER:
                return SmartthingsBindingConstants.OPEN_VALUE.equals(dataFromSmartthings) ? UpDownType.DOWN
                        : UpDownType.UP;

            case SmartthingsBindingConstants.TYPE_STRING:
                // temp fixes, need review
                if (dataFromSmartthings instanceof Double) {
                    return new StringType(((Double) dataFromSmartthings).toString());
                } else if (dataFromSmartthings instanceof String) {
                    return new StringType((String) dataFromSmartthings);
                } else if (dataFromSmartthings instanceof ArrayList array) {
                    StringBuffer result = new StringBuffer();
                    for (Object val : array) {
                        if (val instanceof String st) {
                            result.append(st);
                            result.append(", ");
                        } else {
                            logger.error("@todo : handle this case: {} inputClass: {}", channelUid,
                                    dataFromSmartthings.getClass());

                        }
                    }

                    String resultSt = result.toString();
                    return new StringType(resultSt);

                } else if (dataFromSmartthings instanceof LinkedTreeMap map) {

                    String resultSt = gson.toJson(map).toString();
                    // if (map.keySet().contains("deltaEnergy")) {
                    // SmartthingsTestType ttype = new SmartthingsTestType();
                    // ttype.setEnergy(new BigDecimal(100));
                    // ttype.setDeltaEnergy(new BigDecimal(10));
                    // return ttype;
                    // }
                    return new StringType(resultSt);

                } else {
                    logger.error("@todo : handle this case: {} inputClass: {}", channelUid,
                            dataFromSmartthings.getClass());
                }

            case SmartthingsBindingConstants.TYPE_SWITCH:
                return OnOffType.from("on".equals(dataFromSmartthings));

            // Vector3 can't be triggered now but keep it to handle acceleration device
            case SmartthingsBindingConstants.TYPE_VECTOR3:
                // This is a weird result from Smartthings. If the messages is from a "state" request the result will
                // look like: "value":{"z":22,"y":-36,"x":-987}
                // But if the result is from sensor change via a subscription to a threeAxis device the results will
                // be a String of the format "value":"-873,-70,484"
                // which GSON returns as a
                if (dataFromSmartthings instanceof String stringCommandVector) {
                    return new StringType(stringCommandVector);
                } else if (dataFromSmartthings instanceof Map<?, ?> map) {
                    String s = String.format("%.0f,%.0f,%.0f", map.get("x"), map.get("y"), map.get("z"));
                    return new StringType(s);
                } else {
                    logger.warn(
                            "Unable to convert which should be in Smartthings Vector3 format to a string. The returned datatype from Smartthings is {}.",
                            dataFromSmartthings.getClass().getName());
                    return UnDefType.UNDEF;
                }

            default:
                logger.warn("No type defined to convert with a value of {} from class {} to an appropriate type.",
                        dataFromSmartthings, dataFromSmartthings.getClass().getName());
                return UnDefType.UNDEF;
        }
    }
}
