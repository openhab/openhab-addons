/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveThingHandler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBatteryCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveMultiLevelSwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveBinarySwitchConverter class. Converter for communication with the {@link ZWaveBatteryCommandClass}. Implements
 * polling of the battery status and receiving of battery events.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
public class ZWaveMultiLevelSwitchConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveMultiLevelSwitchConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveMultiLevelSwitchConverter} class.
     *
     */
    public ZWaveMultiLevelSwitchConverter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveMultiLevelSwitchCommandClass commandClass = (ZWaveMultiLevelSwitchCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.SWITCH_MULTILEVEL, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        logger.debug("NODE {}: Generating poll message for {}, endpoint {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint());
        SerialMessage serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass,
                channel.getEndpoint());
        List<SerialMessage> response = new ArrayList<SerialMessage>(1);
        response.add(serialMessage);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        int value = (int) event.getValue();
        State state;
        switch (channel.getDataType()) {
            case PercentType:
                if ("true".equalsIgnoreCase(channel.getArguments().get("invertPercent"))) {
                    state = new PercentType(100 - value);
                } else {
                    state = new PercentType(value);
                }

                // If we read greater than 99%, then change it to 100%
                // This just appears better in OH otherwise you can't get 100%!
                if (((PercentType) state).intValue() >= 99) {
                    state = new PercentType(100);
                }
                break;
            case OnOffType:
                if (value == 0) {
                    state = OnOffType.OFF;
                } else {
                    state = OnOffType.ON;
                }
                break;
            case IncreaseDecreaseType:
                state = null;
                break;
            default:
                state = null;
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        ZWaveMultiLevelSwitchCommandClass commandClass = (ZWaveMultiLevelSwitchCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.SWITCH_MULTILEVEL, channel.getEndpoint());

        SerialMessage serialMessage = null;
        boolean restoreLastValue = "true".equalsIgnoreCase(channel.getArguments().get("restoreLastValue"));

        if (command instanceof StopMoveType && (StopMoveType) command == StopMoveType.STOP) {
            // Special handling for the STOP command
            serialMessage = commandClass.stopLevelChangeMessage();
        } else {
            int value;
            if (command instanceof OnOffType) {
                if (restoreLastValue) {
                    value = command == OnOffType.ON ? 0xff : 0x00;
                } else {
                    value = command == OnOffType.ON ? 0x63 : 0x00;
                }
            } else if (command instanceof PercentType) {
                if ("true".equalsIgnoreCase(channel.getArguments().get("invertPercent"))) {
                    value = 100 - ((PercentType) command).intValue();
                } else {
                    value = ((PercentType) command).intValue();
                }
            } else if (command instanceof UpDownType) {
                if ("true".equalsIgnoreCase(channel.getArguments().get("invertState"))) {
                    if (command == UpDownType.UP) {
                        command = UpDownType.DOWN;
                    } else {
                        command = UpDownType.UP;
                    }
                }

                value = command != UpDownType.DOWN ? 0x63 : 0x00;
            } else {
                logger.warn("NODE {}: No conversion for channel {}", node.getNodeId(), channel.getUID());
                return null;
            }

            logger.trace("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), value, channel.getUID(), channel.getEndpoint());

            serialMessage = commandClass.setValueMessage(value);
        }

        // encapsulate the message in case this is a multi-instance node
        serialMessage = node.encapsulate(serialMessage, commandClass, channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("Generating message failed for command class = {}, node = {}, endpoint = {}",
                    commandClass.getCommandClass().getLabel(), node.getNodeId(), channel.getEndpoint());
            return null;
        }

        // Queue the command
        List<SerialMessage> messages = new ArrayList<SerialMessage>(2);
        messages.add(serialMessage);

        // Poll an update once we've sent the command
        messages.add(node.encapsulate(commandClass.getValueMessage(), commandClass, channel.getEndpoint()));
        return messages;
    }
}
