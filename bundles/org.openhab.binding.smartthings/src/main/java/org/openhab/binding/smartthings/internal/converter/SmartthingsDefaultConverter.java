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

import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.dto.SmartthingsArgument;
import org.openhab.binding.smartthings.internal.dto.SmartthingsAttribute;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapabilitie;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCommand;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
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
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This "Converter" is assigned to a channel when a special converter is not needed.
 * A channel specific converter is specified in the thing-type channel property smartthings-converter then that channel
 * is used.
 * If a channel specific converter is not found a convert based on the channel ID is used.
 * If there is no convert found then this Default converter is used.
 * Yes, it would be possible to change the SamrtthingsConverter class to not being abstract and implement these methods
 * there. But, this makes it explicit that the default converter is being used.
 * See SmartthingsThingHandler.initialize() for details
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsDefaultConverter extends SmartthingsConverter {
    private final Logger logger = LoggerFactory.getLogger(SmartthingsDefaultConverter.class);

    public SmartthingsDefaultConverter(SmartthingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public void convertToSmartthingsInternal(Thing thing, ChannelUID channelUid, Command command) {
        Object value;

        if (command instanceof DateTimeType dateTimeCommand) {
            value = dateTimeCommand.format("%m/%d/%Y %H.%M.%S");
        } else if (command instanceof HSBType hsbCommand) {
            value = String.format("[%d, %d, %d ]", hsbCommand.getHue().intValue(),
                    hsbCommand.getSaturation().intValue(), hsbCommand.getBrightness().intValue());
        } else if (command instanceof DecimalType) {
            DecimalType dc = (DecimalType) command;
            value = dc.intValue();
        } else if (command instanceof IncreaseDecreaseType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof NextPreviousType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof OnOffType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof OpenClosedType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof PercentType) {
            value = command.toString();
        } else if (command instanceof PointType) {
            logger.warn(
                    "Warning - PointType Command is not supported by Smartthings. Please configure to use a different command type. CapabilityKey: {}, capabilityAttribute {}",
                    thing.getThingTypeUID(), channelUid.getId());
            value = command.toFullString();
        } else if (command instanceof RefreshType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof RewindFastforwardType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof StopMoveType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof PlayPauseType) {
            value = command.toString().toLowerCase();
        } else if (command instanceof StringListType) {
            value = command.toString();
        } else if (command instanceof StringType) {
            value = command.toString();
        } else if (command instanceof UpDownType) {
            value = command.toString().toLowerCase();
        } else {
            logger.warn(
                    "Warning - The Smartthings converter does not know how to handle the {} command. The Smartthingsonverter class should be updated.  CapabilityKey: {},  capabilityAttribute {}",
                    command.getClass().getName(), thing.getThingTypeUID(), channelUid.getId());
            value = command.toString().toLowerCase();
        }

        String jsonMsg = "";

        Channel channel = thing.getChannel(channelUid);
        Map<String, String> properties = channel.getProperties();
        String componentKey = properties.get("component");
        String capaKey = properties.get("capability");
        String attrKey = properties.get("attribute");

        SmartthingsCapabilitie capa = null;
        SmartthingsAttribute attr = null;
        if (capaKey != null) {
            capa = typeRegistry.getCapabilities(capaKey);
        }
        if (attrKey != null) {
            attr = capa.attributes.get(attrKey);
        }

        String cmdName = "";
        Object[] arguments = null;

        if (capa != null && attr != null) {
            if (attrKey.equals("color")) {
                attr.setter = "setColor";
            }

            if (attr.setter != null) {
                SmartthingsCommand cmd = capa.commands.get(attr.setter);
                cmdName = cmd.name;

                Stack<Object> stack = new Stack<Object>();
                for (SmartthingsArgument arg : cmd.arguments) {
                    if (arg.optional) {
                        continue;
                    }
                    stack.push(value);
                }
                arguments = stack.toArray();
            } else {
                cmdName = value.toString();
            }
        }

        pushCommand(componentKey, capaKey, cmdName, arguments);
    }

    @Override
    public State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartthings) {
        return defaultConvertToOpenHab(thing, channelUid, dataFromSmartthings);
    }
}
