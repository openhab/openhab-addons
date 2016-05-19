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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBatteryCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveBinarySwitchCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
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
public class ZWaveBinarySwitchConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveBinarySwitchConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveBinarySwitchConverter} class.
     *
     */
    public ZWaveBinarySwitchConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveBinarySwitchCommandClass commandClass = (ZWaveBinarySwitchCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.SWITCH_BINARY, channel.getEndpoint());
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
        return (Integer) event.getValue() == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        ZWaveBinarySwitchCommandClass commandClass = (ZWaveBinarySwitchCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.SWITCH_BINARY, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        int value = 0;
        if (command instanceof OnOffType) {
            value = command == OnOffType.ON ? 0xff : 0x00;
        }
        SerialMessage serialMessage = node.encapsulate(commandClass.setValueMessage(value), commandClass,
                channel.getEndpoint());

        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass().getLabel(), channel.getEndpoint());
            return null;
        }

        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        messages.add(serialMessage);
        return messages;
    }
}
