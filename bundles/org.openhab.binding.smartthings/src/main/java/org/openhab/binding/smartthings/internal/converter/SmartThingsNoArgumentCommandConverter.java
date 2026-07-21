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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.SmartThingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter for SmartThings commands that have no arguments.
 */
@NonNullByDefault
public class SmartThingsNoArgumentCommandConverter extends SmartThingsDefaultConverter {
    private final Logger logger = LoggerFactory.getLogger(SmartThingsNoArgumentCommandConverter.class);

    public SmartThingsNoArgumentCommandConverter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public String convertToSmartThings(Thing thing, ChannelUID channelUid, Command command)
            throws SmartThingsException {
        if (isIgnoredCommand(command)) {
            return "";
        }

        Channel channel = thing.getChannel(channelUid);
        if (channel == null) {
            logger.error("Channel not found: {}", channelUid);
            return "";
        }

        Map<String, String> properties = channel.getProperties();
        String componentKey = properties.get(SmartThingsBindingConstants.COMPONENT);
        String capaKey = properties.get(SmartThingsBindingConstants.CAPABILITY);
        String commandKey = properties.getOrDefault(SmartThingsBindingConstants.COMMAND, "");

        pushNoArgumentCommand(channelUid, componentKey, capaKey, commandKey);
        return getJSonCommands();
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command,
            SmartThingsCapability capa, SmartThingsAttribute attr, String componentKey, String capaKey, String attrKey,
            String targetType, String commandKey) throws SmartThingsException {
        if (isIgnoredCommand(command)) {
            return;
        }
        pushNoArgumentCommand(channelUid, componentKey, capaKey, commandKey);
    }

    private boolean isIgnoredCommand(Command command) {
        return command instanceof OnOffType onOffType && OnOffType.ON != onOffType;
    }

    private void pushNoArgumentCommand(ChannelUID channelUid, @Nullable String componentKey, @Nullable String capaKey,
            String commandKey) throws SmartThingsException {
        if (componentKey == null) {
            throw new SmartThingsException("componentKey is null on : " + channelUid);
        }
        if (capaKey == null) {
            throw new SmartThingsException("capaKey is null on : " + channelUid);
        }
        if (commandKey.isBlank()) {
            throw new SmartThingsException("commandKey is blank on : " + channelUid);
        }

        pushCommand(componentKey, capaKey, commandKey, null);
    }
}
