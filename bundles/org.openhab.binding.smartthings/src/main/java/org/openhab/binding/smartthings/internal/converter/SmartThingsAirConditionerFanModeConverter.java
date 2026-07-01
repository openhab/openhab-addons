/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartThingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Converter for the static Samsung A/C fan-mode channel.
 */
@NonNullByDefault
public class SmartThingsAirConditionerFanModeConverter extends SmartThingsDefaultConverter {
    static final String CAPABILITY_OPTIONAL_MODE = "custom.airConditionerOptionalMode";
    static final String COMMAND_SET_OPTIONAL_MODE = "setAcOptionalMode";
    private static final String CAPABILITY_FAN_MODE = "airConditionerFanMode";
    private static final String ATTRIBUTE_FAN_MODE = "fanMode";
    private static final Set<String> OPTIONAL_MODES = Set.of("off", "quiet", "sleep", "windFree");

    public SmartThingsAirConditionerFanModeConverter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public String convertToSmartThings(Thing thing, ChannelUID channelUid, Command command)
            throws SmartThingsException {
        Channel channel = thing.getChannel(channelUid);
        if (!isAirConditionerFanModeChannel(channel) || !isOptionalMode(command)) {
            return super.convertToSmartThings(thing, channelUid, command);
        }

        String componentKey = channel.getProperties().get(SmartThingsBindingConstants.COMPONENT);
        if (componentKey == null) {
            throw new SmartThingsException("componentKey is null on : " + channelUid);
        }

        pushCommand(componentKey, CAPABILITY_OPTIONAL_MODE, COMMAND_SET_OPTIONAL_MODE,
                new Object[] { convertStaticCommandArgument(command) });
        return getJSonCommands();
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command,
            SmartThingsCapability capa, SmartThingsAttribute attr, String componentKey, String capaKey, String attrKey,
            String targetType, String commandKey) throws SmartThingsException {
        if (isAirConditionerFanMode(capaKey, attrKey) && isOptionalMode(command)) {
            pushCommand(componentKey, CAPABILITY_OPTIONAL_MODE, COMMAND_SET_OPTIONAL_MODE,
                    new Object[] { convertStaticCommandArgument(command) });
            return;
        }

        super.convertToSmartThingsInternal(thing, channelUid, command, capa, attr, componentKey, capaKey, attrKey,
                targetType, commandKey);
    }

    private boolean isOptionalMode(Command command) {
        return OPTIONAL_MODES.contains(command.toString());
    }

    private boolean isAirConditionerFanModeChannel(@Nullable Channel channel) {
        if (channel == null) {
            return false;
        }

        return CAPABILITY_FAN_MODE.equals(channel.getProperties().get(SmartThingsBindingConstants.CAPABILITY))
                && ATTRIBUTE_FAN_MODE.equals(channel.getProperties().get(SmartThingsBindingConstants.ATTRIBUTE));
    }

    private boolean isAirConditionerFanMode(String capability, String attribute) {
        return CAPABILITY_FAN_MODE.equals(capability) && ATTRIBUTE_FAN_MODE.equals(attribute);
    }
}
