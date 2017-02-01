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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.handler.ZWaveThingChannel.DataType;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.proprietary.FibaroFGRM222CommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chris Jackson
 * @author wenzel
 * @author Markus Rathgeb <maggu2810@gmail.com>
 */
public class FibaroFGRM222Converter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(FibaroFGRM222Converter.class);

    public FibaroFGRM222Converter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String sensorType = channel.getArguments().get("type");
        FibaroFGRM222CommandClass.FibaroFGRM222ValueEvent sensorEvent = (FibaroFGRM222CommandClass.FibaroFGRM222ValueEvent) event;
        // Don't trigger event if this item is bound to another sensor type
        if (sensorType != null && !sensorType.equalsIgnoreCase(sensorEvent.getSensorType().name())) {
            return null;
        }

        State state = new DecimalType(((Integer) event.getValue()).intValue());
        if (channel.getDataType() == DataType.DecimalType) {
            state = new PercentType(100 - ((DecimalType) state).intValue());
        }

        return state;
    }

    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        FibaroFGRM222CommandClass commandClass = (FibaroFGRM222CommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.FIBARO_FGRM_222, channel.getEndpoint());

        logger.debug("NODE {}: receiveCommand()", node.getNodeId());
        Command internalCommand = command;
        SerialMessage serialMessage = null;

        if (internalCommand instanceof StopMoveType && (StopMoveType) internalCommand == StopMoveType.STOP) {
            // Special handling for the STOP command
            serialMessage = commandClass.stopLevelChangeMessage(channel.getArguments().get("type"));
        } else {
            int value;

            if (command instanceof PercentType) {
                if ("true".equalsIgnoreCase(channel.getArguments().get("invert_percent"))) {
                    value = 100 - ((PercentType) command).intValue();
                } else {
                    value = ((PercentType) command).intValue();
                }

            } else if (command instanceof UpDownType) {
                if ("true".equalsIgnoreCase(channel.getArguments().get("invert_state"))) {
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
            if (value == 0) {
                value = 1;
            }
            logger.trace("NODE {}: Converted command '{}' to value {} for item = {}, endpoint = {}.", node.getNodeId(),
                    internalCommand.toString(), channel.getEndpoint());

            serialMessage = commandClass.setValueMessage(value, channel.getArguments().get("type"));
        }

        // encapsulate the message in case this is a multi-instance node
        serialMessage = node.encapsulate(serialMessage, commandClass, channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, node = {}, endpoint = {}",
                    node.getNodeId(), commandClass.getCommandClass().getLabel(), channel.getEndpoint());
            return null;
        }

        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        messages.add(serialMessage);
        return messages;
    }
}