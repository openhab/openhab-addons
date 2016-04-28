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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveControllerHandler;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.LocalProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.RfProtectionType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveProtectionCommandClass.Type;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveProtectionConverter class. Converters between binding items and the Z-Wave API for protection.
 *
 * @author Jorg de Jong
 */
public class ZWaveProtectionConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveProtectionConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveConverterBase} class.
     *
     */
    public ZWaveProtectionConverter(ZWaveControllerHandler controller) {
        super(controller);
    }

    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        String type = channel.getArguments().get("type");

        // Don't trigger event if this item is bound to another type
        if (type != null && !event.getType().equals(Type.valueOf(type))) {
            return null;
        }

        Enum<?> e = (Enum<?>) event.getValue();
        return new DecimalType(e.ordinal());
    }

    @Override
    public List<SerialMessage> receiveCommand(ZWaveThingChannel channel, ZWaveNode node, Command command) {
        String type = channel.getArguments().get("type");

        ZWaveProtectionCommandClass commandClass = (ZWaveProtectionCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.PROTECTION, channel.getEndpoint());

        if (commandClass == null) {
            return null;
        }

        SerialMessage serialMessage = null;

        if (type != null) {
            if (Type.PROTECTION_LOCAL.name().equals(type)) {
                logger.debug("NODE {}: Local Protection command received for {}", node.getNodeId(), command.toString());

                int value = ((DecimalType) command).intValue();
                if (value >= 0 && value < LocalProtectionType.values().length) {
                    serialMessage = node.encapsulate(
                            commandClass.setValueMessage(LocalProtectionType.values()[value], null), commandClass,
                            channel.getEndpoint());
                }

            }
            if (Type.PROTECTION_RF.name().equals(type)) {
                logger.debug("NODE {}: rf Protection command received for {}", node.getNodeId(), command.toString());

                int value = ((DecimalType) command).intValue();
                if (value >= 0 && value < RfProtectionType.values().length) {
                    serialMessage = node.encapsulate(
                            commandClass.setValueMessage(null, RfProtectionType.values()[value]), commandClass,
                            channel.getEndpoint());
                }
            }
        }

        if (serialMessage == null) {
            logger.warn("NODE {}: Generating message failed for command class = {}, endpoint = {}", node.getNodeId(),
                    commandClass.getCommandClass().getLabel(), channel.getEndpoint());
            return null;
        }

        logger.debug("NODE {}: Sending Message: {}", node.getNodeId(), serialMessage);
        List<SerialMessage> messages = new ArrayList<SerialMessage>();
        messages.add(serialMessage);

        // Request an update so that OH knows when the protection settings has changed.
        serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass, channel.getEndpoint());

        if (serialMessage != null) {
            messages.add(serialMessage);
        }

        return messages;
    }
}
