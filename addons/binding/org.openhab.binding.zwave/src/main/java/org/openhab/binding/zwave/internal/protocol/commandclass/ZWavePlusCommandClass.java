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

import org.openhab.binding.zwave.internal.HexToIntegerConverter;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageClass;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessagePriority;
import org.openhab.binding.zwave.internal.protocol.SerialMessage.SerialMessageType;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEndpoint;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.ZWavePlusDeviceClass.ZWavePlusDeviceType;
import org.openhab.binding.zwave.internal.protocol.ZWaveSerialMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Handles the ZWave Plus Command command class.
 *
 * @author Chris Jackson
 */
@XStreamAlias("zwavePlusCommandClass")
public class ZWavePlusCommandClass extends ZWaveCommandClass
        implements ZWaveGetCommands, ZWaveCommandClassInitialization {

    @XStreamOmitField
    private static final Logger logger = LoggerFactory.getLogger(ZWavePlusCommandClass.class);

    private static final byte ZWAVE_PLUS_GET = 0x01;
    private static final byte ZWAVE_PLUS_REPORT = 0x02;

    @SuppressWarnings("unused")
    private int zwPlusVersion = 0;
    @SuppressWarnings("unused")
    private int zwPlusRole = 0;
    @SuppressWarnings("unused")
    private int zwPlusNodeType = 0;
    @XStreamConverter(HexToIntegerConverter.class)
    private int zwPlusDeviceType = 0;
    @XStreamConverter(HexToIntegerConverter.class)
    private int zwPlusInstallerIcon = 0;

    @XStreamOmitField
    private boolean initialiseDone = false;
    private boolean isGetSupported = true;

    /**
     * Creates a new instance of the ZWavePlusCommandClass class.
     *
     * @param node
     *            the node this command class belongs to
     * @param controller
     *            the controller to use
     * @param endpoint
     *            the endpoint this Command class belongs to
     */
    public ZWavePlusCommandClass(ZWaveNode node, ZWaveController controller, ZWaveEndpoint endpoint) {
        super(node, controller, endpoint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandClass getCommandClass() {
        return CommandClass.ZWAVE_PLUS_INFO;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ZWaveSerialMessageException
     */
    @Override
    public void handleApplicationCommandRequest(SerialMessage serialMessage, int offset, int endpointId)
            throws ZWaveSerialMessageException {
        logger.debug("NODE {}: Received ZWave Plus Request", this.getNode().getNodeId());
        int command = serialMessage.getMessagePayloadByte(offset);
        switch (command) {
            case ZWAVE_PLUS_REPORT:
                handleZWavePlusReport(serialMessage, offset + 1);
                break;
        }
    }

    /**
     * Handle the crc16 encapsulated message. This processes the received frame, checks the crc and forwards to the real
     * command class.
     *
     * @param serialMessage
     *            The received message
     * @param offset
     *            The starting offset into the payload
     * @throws ZWaveSerialMessageException
     */
    private void handleZWavePlusReport(SerialMessage serialMessage, int offset) throws ZWaveSerialMessageException {

        zwPlusVersion = serialMessage.getMessagePayloadByte(offset + 0);
        zwPlusRole = serialMessage.getMessagePayloadByte(offset + 1);
        zwPlusNodeType = serialMessage.getMessagePayloadByte(offset + 2);
        zwPlusInstallerIcon = (serialMessage.getMessagePayloadByte(offset + 3) << 8)
                | serialMessage.getMessagePayloadByte(offset + 4);
        zwPlusDeviceType = (serialMessage.getMessagePayloadByte(offset + 5) << 8)
                | serialMessage.getMessagePayloadByte(offset + 6);

        ZWavePlusDeviceType deviceType = ZWavePlusDeviceType.getZWavePlusDeviceType(zwPlusDeviceType);
        if (deviceType != null) {
            logger.debug("NODE {}: Adding mandatory command classes for ZWavePlus device type {}",
                    getNode().getNodeId(), deviceType);

            // Add all missing mandatory plus command classes
            for (CommandClass commandClass : deviceType.getMandatoryCommandClasses()) {
                ZWaveCommandClass zwaveCommandClass = this.getNode().getCommandClass(commandClass);

                // Add the mandatory class missing, ie not set via NIF
                if (zwaveCommandClass == null) {
                    zwaveCommandClass = ZWaveCommandClass.getInstance(commandClass.getKey(), getNode(),
                            getController());
                    if (zwaveCommandClass != null) {
                        logger.debug(String.format("NODE %d: Adding command class %s (0x%02x)", getNode().getNodeId(),
                                commandClass.getLabel(), commandClass.getKey()));
                        getNode().addCommandClass(zwaveCommandClass);
                    }
                }
            }
        } else {
            logger.info("NODE {}: unknown ZWavePlus device type: {}", getNode().getNodeId(), zwPlusDeviceType);
        }

        initialiseDone = true;
    }

    @Override
    public Collection<SerialMessage> initialize(boolean refresh) {
        ArrayList<SerialMessage> result = new ArrayList<SerialMessage>();
        // If we're already initialized, then don't do it again unless we're refreshing
        if (refresh == true || initialiseDone == false) {
            result.add(this.getValueMessage());
        }
        return result;
    }

    @Override
    public SerialMessage getValueMessage() {
        if (isGetSupported == false) {
            logger.debug("NODE {}: Node doesn't support get requests", this.getNode().getNodeId());
            return null;
        }

        logger.debug("NODE {}: Creating new message for application command ZWAVE_PLUS_GET",
                this.getNode().getNodeId());
        SerialMessage result = new SerialMessage(this.getNode().getNodeId(), SerialMessageClass.SendData,
                SerialMessageType.Request, SerialMessageClass.ApplicationCommandHandler, SerialMessagePriority.Get);
        byte[] newPayload = { (byte) this.getNode().getNodeId(), 2, (byte) getCommandClass().getKey(), ZWAVE_PLUS_GET };
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
     * Return the ZWave Plus Device Type
     * 
     * @return {@link ZWavePlusDeviceType}
     */
    public ZWavePlusDeviceType getZWavePlusDeviceType() {
        ZWavePlusDeviceType deviceType = ZWavePlusDeviceType.getZWavePlusDeviceType(zwPlusDeviceType);
        if (deviceType == null) {
            deviceType = ZWavePlusDeviceType.UNKNOWN_TYPE;
        }

        return deviceType;
    }
}
