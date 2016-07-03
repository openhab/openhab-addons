/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.commandclass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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
 * Handles the Binary Switch command class. Binary switches can be turned on or off and report their status as on (0xFF)
 * or off (0x00).
 * The commands include the possibility to set a given level, get a given level and report a level.
 *
 * @author Chris Jackson
 * @author Jan-Willem Spuij
 */
@XStreamAlias("binarySwitchCommandClass")
public class ZWaveBinarySwitchCommandClass extends ZWaveCommandClass
        implements ZWaveBasicCommands, ZWaveCommandClassDynamicState {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWaveBinarySwitchCommandClass.class);

    private static final int SWITCH_BINARY_SET = 0x01;
    private static final int SWITCH_BINARY_GET = 0x02;
    private static final int SWITCH_BINARY_REPORT = 0x03;

    @XStreamOmitField
    private boolean dynamicDone = false;

    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWaveBinarySwitchCommandClass class.
     *
     * @param node the node this command class belongs to
     * @param controller the controller to use
     * @param endpoint the endpoint this Command class belongs to
     */
    public ZWaveBinarySwitchCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.SWITCH_BINARY;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        logger.debug(String.format("Received Switch Binary Request for Node ID = %d", this.getNode().getNodeId()));
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case SWITCH_BINARY_SET:
                logger.debug("NODE {}: Switch Binary SET", this.getNode().getNodeId());
            case SWITCH_BINARY_REPORT:
                processSwitchBinaryReport(serialMessage, offset, endpoint);

                dynamicDone = true;
                break;
            default:
                logger.warn(String.format("Unsupported Command 0x%02X for command class %s (0x%02X).", command,
                        this.getCommandClass().getLabel(), this.getCommandClass().getKey()));
        }
    }

    /**
     * Processes a SWITCH_BINARY_REPORT / SWITCH_BINARY_SET message.
     *
     * @param serialMessage the incoming message to process.
     * @param offset the offset position from which to start message processing.
     * @param endpoint the endpoint or instance number this message is meant for.
     * @throws ZWaveSerialMessageException
     */
    protected void processSwitchBinaryReport(SerialMessage serialMessage, int offset, int endpoint)
            throws ZWaveSerialMessageException {
        int value = serialMessage.getMessagePayloadByte(offset + 1);
        logger.debug("NODE {}: Switch Binary report, value = {}", this.getNode().getNodeId(), value);
        ZWaveCommandClassValueEvent zEvent = new ZWaveCommandClassValueEvent(this.getNode().getNodeId(), endpoint,
                this.getCommandClass(), value);
        this.getController().notifyEventListeners(zEvent);
    }

    /**
     * Gets a SerialMessage with the SWITCH_BINARY_GET command
     *
     * @return the serial message
     */
    @Override
    public SerialMessage getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command SWITCH_BINARY_GET",
                this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(),
                (byte) SWITCH_BINARY_GET };
        result.setMessagePayload(newPayload);
        return result;
    }

    /**
     * Gets a SerialMessage with the SWITCH_BINARY_SET command
     *
     * @param the level to set. 0 is mapped to off, > 0 is mapped to on.
     * @return the serial message
     */
    @Override
    public SerialMessage setValueMessage(int level) {
        logger.debug("NODE {}: Creating new message for application command SWITCH_BINARY_SET",
                this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.SendData, SerialMessagePriority.Set);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 3, (byte) getCommandClass().getKey(),
                (byte) SWITCH_BINARY_SET, (byte) (level > 0 ? 0xFF : 0x00) };
        result.setMessagePayload(newPayload);
        return result;
    }

    @Override
    public boolean setOptions(Map<String, String> options) {
        if ("false".equals(options.get("getSupported"))) {
            isGetSupported = false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SerialMessage> getDynamicValues(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();

        if (refresh == true || dynamicDone == false) {
            result.add(getValueMessage());
        }
        return result;
    }
}
