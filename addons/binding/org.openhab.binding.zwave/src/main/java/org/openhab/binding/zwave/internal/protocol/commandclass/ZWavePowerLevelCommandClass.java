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
import java.util.ArrayList;
import java.util.Collection;

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
 * Handles the power level command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("powerLevelCommandClass")
public class ZWavePowerLevelCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWavePowerLevelCommandClass.class);

    private static final int POWERLEVEL_SET = 1;
    private static final int POWERLEVEL_GET = 2;
    private static final int POWERLEVEL_REPORT = 3;
    private static final int POWERLEVEL_TEST_SET = 4;
    private static final int POWERLEVEL_TEST_GET = 5;
    private static final int POWERLEVEL_TEST_REPORT = 6;

    private int powerLevel = 0;
    private int powerTimeout = 0;

    @XStreamOmitField
    private boolean initialiseDone = false;

    /**
     * Creates a new instance of the ZWavePowerLevelCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWavePowerLevelCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.POWERLEVEL;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received POWERLEVEL command V{}", getNode().getNodeId(), getVersion());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case POWERLEVEL_REPORT:
                powerLevel = serialMessage.getMessagePayloadByte(offset + 1);
                powerTimeout = serialMessage.getMessagePayloadByte(offset + 2);
                logger.debug("NODE {}: Received POWERLEVEL report -{}dB with {} second timeout", getNode().getNodeId(),
                        powerLevel, powerTimeout);
                ZWavePowerLevelCommandClassChangeEvent event = new ZWavePowerLevelCommandClassChangeEvent(
                        getNode().getNodeId(), powerLevel, powerTimeout);
                getController().notifyEventListeners(event);
                initialiseDone = true;
                break;
            default:
                logger.warn(String.format("NODE %d: Unsupported Command %d for command class %s (0x%02X).",
                        getNode().getNodeId(), command, getCommandClass().getLabel(), getCommandClass().getKey()));
                break;
        }
    }

    public SerialMessage setValueMessage(int level, int timeout) {
        logger.debug("NODE {}: Creating new message for application command POWERLEVEL_SET, level={}, timeout={}",
                getNode().getNodeId(), level, timeout);

        if (level < 0 || level > 9) {
            logger.debug("NODE {}: Invalid power level {}.", getNode().getNodeId(), level);
            return null;
        }

        if (timeout < 0 || timeout > 255) {
            logger.debug("NODE {}: Invalid timeout {}.", getNode().getNodeId(), timeout);
            return null;
        }

        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Set);
        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) getNode().getNodeId());
        outputData.write(4);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write((byte) POWERLEVEL_SET);
        outputData.write((byte) level);
        outputData.write((byte) timeout);
        message.setMessagePayload(outputData.toByteArray());
        return message;
    }

    @Override
    public SerialMessage getValueMessage() {
        logger.debug("NODE {}: Creating new message for application command POWERLEVEL_GET", getNode().getNodeId());
        SerialMessage message = new SerialMessage(getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);

        ByteArrayOutputStream outputData = new ByteArrayOutputStream();
        outputData.write((byte) getNode().getNodeId());
        outputData.write(2);
        outputData.write((byte) getCommandClass().getKey());
        outputData.write((byte) POWERLEVEL_GET);
        message.setMessagePayload(outputData.toByteArray());
        return message;
    }

    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        if (refresh == true || initialiseDone == false) {
            result.add(getValueMessage());
        }

        return result;
    }

    /**
     * return the current power level setting
     *
     * @return power level setting
     */
    public int getLevel() {
        return powerLevel;
    }

    /**
     * Return the current timeout in seconds (0 - 255)
     *
     * @return timeout
     */
    public int getTimeout() {
        return powerTimeout;
    }

    public class ZWavePowerLevelCommandClassChangeEvent extends ZWaveCommandClassValueEvent {
        private int timeout;

        public ZWavePowerLevelCommandClassChangeEvent(int nodeId, int level, int timeout) {
            super(nodeId, 0, CommandClass.POWERLEVEL, level);
            this.timeout = timeout;
        }

        public int getLevel() {
            return (int) getValue();
        }

        public int getTimeout() {
            return timeout;
        }
    }
}
