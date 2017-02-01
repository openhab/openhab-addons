/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.io.ByteArrayOutputStream;

import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the protection command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("meterPulseCommandClass")
public class ZWaveMeterPulseCommandClass extends ZWaveCommandClass implements ZWaveGetCommands {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveMeterPulseCommandClass.class);

    private static final int METER_PULSE_GET = 4;
    private static final int METER_PULSE_REPORT = 5;

    /**
     * Creates a new instance of the ZWaveMeterPulseCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveMeterPulseCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.METER_PULSE;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received METER_PULSE command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case METER_PULSE_REPORT:
                int count = (serialMessage.getMessagePayloadByte(offset + 1) << 24)
                        + (serialMessage.getMessagePayloadByte(offset + 2) << 16)
                        + (serialMessage.getMessagePayloadByte(offset + 3) << 8)
                        + serialMessage.getMessagePayloadByte(offset + 4);
                logger.debug("NODE {}: Received meter pulse count {}", getNode().getNodeId(), count);
                ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(getNode().getNodeId(), endpoint,
                        getCommandClass(), count);
                getController().notifyEventListeners(zEvent);
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
        }
    }

    @Override
    public SerialMessage getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command METER_PULSE_GET", getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) getNode().getNodeId());
        outputData.write(2);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write((byte) METER_PULSE_GET);
        message.setMessagePayload(outputData.toByteArray());
        return message;
    }
}
