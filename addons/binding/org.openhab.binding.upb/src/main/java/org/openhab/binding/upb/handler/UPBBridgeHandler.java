/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.handler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.upb.internal.MessageBuilder;
import org.openhab.binding.upb.internal.UPBMessage;
import org.openhab.binding.upb.internal.UPBReader;
import org.openhab.binding.upb.internal.UPBWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Handler for Universal Powerline Bus (UPB) that reads and writes messages to
 * and from the UPB modem.
 *
 * @author Chris Van Orman
 * @since 1.9.0
 */
public class UPBBridgeHandler extends BaseBridgeHandler implements UPBMessageSender, UPBReader.Listener {

    private static final String CONFIG_NETWORK = "network";
    private static final String CONFIG_PORT = "serialPort";

    private static final Logger logger = LoggerFactory.getLogger(UPBBridgeHandler.class);

    private String port;
    private byte network = 0;
    private SerialPort serialPort;
    private List<UPBMessageListener> listeners = new CopyOnWriteArrayList<>();
    private UPBReader upbReader;
    private UPBWriter upbWriter;

    /**
     * Instantiates a new {@link UPBBridgeHandler}.
     * 
     * @param bridge the bridge to be handled
     */
    public UPBBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {

        // Read necessary config values
        this.network = ((Number) getConfig().get(CONFIG_NETWORK)).byteValue();
        this.port = (String) getConfig().get(CONFIG_PORT);

        logger.debug("Parsed UPB configuration:");
        logger.debug("Serial port: {}", port);
        logger.debug("UPB Network: {}", network & 0xff);

        try {
            serialPort = openSerialPort();
            upbReader = new UPBReader(new DataInputStream(serialPort.getInputStream()));
            upbWriter = new UPBWriter(new DataOutputStream(serialPort.getOutputStream()), upbReader);

            upbReader.addListener(this);
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.error("Error opening serial port [{}].", port, e);

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Error opening serial port.");
        }
    }

    @Override
    public void dispose() {
        if (upbReader != null) {
            upbReader.shutdown();
        }

        if (upbWriter != null) {
            upbWriter.shutdown();
        }

        if (serialPort != null) {
            logger.debug("Closing serial port");
            serialPort.close();
        }
    }

    private SerialPort openSerialPort() {
        SerialPort serialPort = null;
        CommPortIdentifier portId;
        try {
            portId = CommPortIdentifier.getPortIdentifier(port);
        } catch (NoSuchPortException e1) {
            String ports = StringUtils.join(findAvailablePorts(), ", ");
            throw new RuntimeException(
                    String.format("Port [%s] does not exist. Found the following ports: [%s].", port, ports), e1);
        }

        if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            if (portId.getName().equals(port)) {
                try {
                    serialPort = portId.open("UPB", 1000);
                } catch (PortInUseException e) {
                    throw new RuntimeException("Port is in use", e);
                }
                try {
                    serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);
                    serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                    serialPort.enableReceiveTimeout(100);
                } catch (UnsupportedCommOperationException e) {
                    throw new RuntimeException("Failed to configure serial port", e);
                }
            }
        }

        return serialPort;
    }

    private List<String> findAvailablePorts() {
        @SuppressWarnings("unchecked")
        Enumeration<CommPortIdentifier> ports = CommPortIdentifier.getPortIdentifiers();

        List<String> portNames = new ArrayList<>();

        while (ports.hasMoreElements()) {
            CommPortIdentifier port = ports.nextElement();
            portNames.add(port.getName());
        }

        return portNames;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, org.eclipse.smarthome.core.types.Command command) {
        // Bridge does not support commands.
    }

    @Override
    public void childHandlerInitialized(ThingHandler childHandler, Thing childThing) {
        if (childHandler instanceof UPBMessageListener) {
            listeners.add((UPBMessageListener) childHandler);
            ((UPBMessageListener) childHandler).setMessageSender(this);
            // childHandler.handleCommand(null, RefreshType.REFRESH);
        }
    }

    @Override
    public void childHandlerDisposed(ThingHandler childHandler, Thing childThing) {
        listeners.remove(childHandler);

        if (childHandler instanceof UPBMessageListener) {
            ((UPBMessageListener) childHandler).setMessageSender(null);
        }
    }

    @Override
    public void sendMessage(MessageBuilder message) {
        if (upbWriter != null) {
            upbWriter.queueMessage(message.network(network));
        }
    }

    @Override
    public void messageReceived(UPBMessage message) {
        for (UPBMessageListener listener : listeners) {
            listener.messageReceived(message);
        }
    }
}
