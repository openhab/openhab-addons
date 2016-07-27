/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
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
    public ZWaveMultiLevelSwitchConverter(ZWaveControllerHandler controller) {
        super(controller);
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
        boolean configInvertControl = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_control"));
        boolean configInvertPercent = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_percent"));

        int value = (int) event.getValue();

        // A value of 254 means the device doesn't know it's current position
        if (value == 254) {
            // TODO: Should this return UNDEFINED?
            return null;
        }

        State state = null;
        switch (channel.getDataType()) {
            case PercentType:
                if (value < 0 || value > 100) {
                    break;
                }

                if (configInvertPercent) {
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

                if (configInvertControl) {
                    if (state == OnOffType.ON) {
                        state = OnOffType.OFF;
                    } else {
                        state = OnOffType.ON;
                    }
                }
                break;
            case IncreaseDecreaseType:
                break;
            default:
                logger.warn("No conversion in {} to {}", getClass().getSimpleName(), channel.getDataType());
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
        // boolean restoreLastValue = "true".equalsIgnoreCase(channel.getArguments().get("restoreLastValue"));

        boolean configInvertControl = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_control"));
        boolean configInvertPercent = "true".equalsIgnoreCase(channel.getArguments().get("config_invert_percent"));

        if (command instanceof StopMoveType && command == StopMoveType.STOP) {
            // Special handling for the STOP command
            serialMessage = commandClass.stopLevelChangeMessage();
        } else if (command instanceof UpDownType) {
            if (configInvertControl == false) {
                if (command == UpDownType.UP) {
                    serialMessage = commandClass.startLevelChangeMessage(true, 0xff);
                } else {
                    serialMessage = commandClass.startLevelChangeMessage(false, 0xff);
                }
            } else {
                if (command == UpDownType.UP) {
                    serialMessage = commandClass.startLevelChangeMessage(false, 0xff);
                } else {
                    serialMessage = commandClass.startLevelChangeMessage(true, 0xff);
                }
            }
        } else if (command instanceof PercentType) {
            int value;
            if (configInvertPercent) {
                value = 100 - ((PercentType) command).intValue();
            } else {
                value = ((PercentType) command).intValue();
            }
            // zwave has a max value of 99 for percentages.
            if (value >= 100) {
                value = 99;
            }

            logger.trace("NODE {}: Converted command '{}' to value {} for channel = {}, endpoint = {}.",
                    node.getNodeId(), command.toString(), value, channel.getUID(), channel.getEndpoint());

            serialMessage = commandClass.setValueMessage(value);
        } else if (command instanceof OnOffType) {
            int value;
            if (configInvertControl) {
                value = command == OnOffType.ON ? 0 : 99;
            } else {
                value = command == OnOffType.ON ? 99 : 0;
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

        // Poll an update once we've sent the command if this is a STOP
        // Don't poll immediately since some devices return the original value, and some the new value.
        // This conflicts with OH that will move the slider immediately.
        if (command instanceof StopMoveType && command == StopMoveType.STOP) {
            messages.add(node.encapsulate(commandClass.getValueMessage(), commandClass, channel.getEndpoint()));
        }
        return messages;
    }
}
