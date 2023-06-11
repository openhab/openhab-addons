/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bsblan.internal.api.dto.BsbLanApiParameterDTO;
import org.openhab.binding.bsblan.internal.handler.BsbLanParameterHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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

    public static @Nullable State getState(String channelId, BsbLanApiParameterDTO parameter) {
        switch (channelId) {
            case PARAMETER_CHANNEL_NAME:
                return getStateForNameChannel(parameter);

            case PARAMETER_CHANNEL_DESCRIPTION:
                return getStateForDescriptionChannel(parameter);

            case PARAMETER_CHANNEL_DATATYPE:
                return getStateForDatatypeChannel(parameter);

            case PARAMETER_CHANNEL_NUMBER_VALUE:
                return getStateForNumberValueChannel(parameter);

            case PARAMETER_CHANNEL_STRING_VALUE:
                return getStateForStringValueChannel(parameter);

            case PARAMETER_CHANNEL_SWITCH_VALUE:
                return getStateForSwitchValueChannel(parameter);

            case PARAMETER_CHANNEL_UNIT:
                return getStateForUnitChannel(parameter);
        }

        LOGGER.debug("unsupported channel '{}' while updating state", channelId);
        return null;
    }

    private static State getStateForNameChannel(BsbLanApiParameterDTO parameter) {
        return new StringType(parameter.name);
    }

    private static State getStateForDescriptionChannel(BsbLanApiParameterDTO parameter) {
        return new StringType(parameter.description);
    }

    private static State getStateForUnitChannel(BsbLanApiParameterDTO parameter) {
        String value = StringEscapeUtils.unescapeHtml4(parameter.unit);
        return new StringType(value);
    }

    private static State getStateForDatatypeChannel(BsbLanApiParameterDTO parameter) {
        int value = parameter.dataType.getValue();
        return new DecimalType(value);
    }

    private static @Nullable State getStateForNumberValueChannel(BsbLanApiParameterDTO parameter) {
        try {
            switch (parameter.dataType) {
                // parse enum data type as integer
                case DT_ENUM:
                    return new DecimalType(Integer.parseInt(parameter.value));

                default:
                    return new DecimalType(Double.parseDouble(parameter.value));
            }
        } catch (NumberFormatException e) {
            // silently ignore - there is not "tryParse"
        }
        return null;
    }

    private static State getStateForStringValueChannel(BsbLanApiParameterDTO parameter) {
        return new StringType(parameter.value);
    }

    private static State getStateForSwitchValueChannel(BsbLanApiParameterDTO parameter) {
        // treat "0" as OFF and everything else as ON
        return parameter.value.equals("0") ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * Converts a Command back to a value which is sent to the BSB-LAN device afterwards.
     *
     * @param channelId
     * @param command
     * @return null if conversion fails or channel is readonly.
     */
    public static @Nullable String getValue(String channelId, Command command) {
        switch (channelId) {
            case PARAMETER_CHANNEL_NUMBER_VALUE:
                return getValueForNumberValueChannel(command);

            case PARAMETER_CHANNEL_STRING_VALUE:
                return getValueForStringValueChannel(command);

            case PARAMETER_CHANNEL_SWITCH_VALUE:
                return getValueForSwitchValueChannel(command);

            default:
                LOGGER.debug("Channel '{}' is read only. Ignoring command", channelId);
                return null;
        }
    }

    private static @Nullable String getValueForNumberValueChannel(Command command) {
        if (command instanceof QuantityType<?>) {
            // the target unit is yet unknown, so just use the value as is (without converting based on the unit)
            QuantityType<?> quantity = (QuantityType<?>) command;
            return String.valueOf(quantity.doubleValue());
        }
        // check if numeric
        else if (command.toString().matches("-?\\d+(\\.\\d+)?")) {
            return command.toString();
        }
        LOGGER.warn("Command '{}' is not a valid number value", command);
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
        LOGGER.warn("Command '{}' is not a valid switch value", command);
        return null;
    }
}
