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
package org.openhab.binding.bsblan.internal.helper;

import org.apache.commons.lang.StringEscapeUtils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;

import org.openhab.binding.bsblan.internal.BsbLanBindingConstants.Channels;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BsbLanParameterHandler} is responsible for updating the data, which are
 * sent to one of the channels.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanParameterConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BsbLanParameterConverter.class);

    public static @Nullable State getState(String channelId, BsbLanApiParameter parameter) {
        switch (channelId) {
            case Channels.Parameter.NAME:
                return getStateForNameChannel(parameter);

            case Channels.Parameter.DESCRIPTION:
                return getStateForDescriptionChannel(parameter);

            case Channels.Parameter.DATATYPE:
                return getStateForDatatypeChannel(parameter);

            case Channels.Parameter.NUMBER_VALUE:
                return getStateForNumberValueChannel(parameter);

            case Channels.Parameter.STRING_VALUE:
                return getStateForStringValueChannel(parameter);

            case Channels.Parameter.SWITCH_VALUE:
                return getStateForSwitchValueChannel(parameter);

            case Channels.Parameter.UNIT:
                return getStateForUnitChannel(parameter);
        }

        LOGGER.warn("unsupported channel '{}' while updating state", channelId);
        return null;
    }

    private static State getStateForNameChannel(BsbLanApiParameter parameter) {
        return new StringType(parameter.name);
    }

    private static State getStateForDescriptionChannel(BsbLanApiParameter parameter) {
        return new StringType(parameter.description);
    }

    private static State getStateForUnitChannel(BsbLanApiParameter parameter) {
        String value = StringEscapeUtils.unescapeHtml(parameter.unit);
        return new StringType(value);
    }

    private static State getStateForDatatypeChannel(BsbLanApiParameter parameter) {
        int value = parameter.dataType.getValue();
        return new DecimalType(value);
    }

    private static @Nullable State getStateForNumberValueChannel(BsbLanApiParameter parameter) {
        try {
            switch (parameter.dataType) {
                // parse enum data type as integer
                case DT_ENUM:
                    return new DecimalType(Integer.parseInt(parameter.value));

                default:
                    return new DecimalType(Double.parseDouble(parameter.value));
            }
        }
        catch (NumberFormatException e) {
            // silently ignore - there is not "tryParse"
        }
        return null;
    }

    private static State getStateForStringValueChannel(BsbLanApiParameter parameter) {
        return new StringType(parameter.value);
    }

    private static State getStateForSwitchValueChannel(BsbLanApiParameter parameter) {
        // treat "0" as OFF and everything else as ON
        return parameter.value.equals("0") ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * Converts a Command back to a value which is sent to the BSB-LAN device afterwards.
     * @param channelId
     * @param command
     * @return null if conversion fails or channel is readonly.
     */
    public static @Nullable String getValue(String channelId, Command command) {
        switch (channelId) {
            case Channels.Parameter.NUMBER_VALUE:
                return getValueForNumberValueChannel(command);

            case Channels.Parameter.STRING_VALUE:
                return getValueForStringValueChannel(command);

            case Channels.Parameter.SWITCH_VALUE:
                return getValueForSwitchValueChannel(command);

            default:
                LOGGER.debug("Channel '{}' is read only. Ignoring command", channelId);
                return null;
        }
    }

    private static @Nullable String getValueForNumberValueChannel(Command command) {
        // check if numeric
        if (command.toString().matches("-?\\d+(\\.\\d+)?")) {
            return command.toString();
        }
        LOGGER.debug("Command '{}' is not a valid number value", command);
        return null;
    }

    private static String getValueForStringValueChannel(Command command) {
        // special OnOffType handling
        if (command.equals(OnOffType.ON)) {
            return "1";
        } else if (command.equals(OnOffType.OFF)) {
            return "0";
        }
        return command.toString();
    }

    private static @Nullable String getValueForSwitchValueChannel(Command command) {
        if (command.equals(OnOffType.ON)) {
            return "1";
        } else if (command.equals(OnOffType.OFF)) {
            return "0";
        }
        LOGGER.debug("Command '{}' is not a valid switch value", command);
        return null;
    }
}
