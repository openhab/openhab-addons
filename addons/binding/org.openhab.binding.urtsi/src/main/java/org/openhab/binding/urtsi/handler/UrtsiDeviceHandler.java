/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.urtsi.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.urtsi.internal.config.RtsDeviceConfig;
import org.openhab.binding.urtsi.internal.config.UrtsiDeviceConfig;
import org.openhab.binding.urtsi.internal.mapping.UrtsiChannelMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * The {@link UrtsiDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class UrtsiDeviceHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(UrtsiDeviceHandler.class);

    private static final String GNU_IO_RXTX_SERIAL_PORTS = "gnu.io.rxtx.SerialPorts";

    private static final int baud = 9600;
    private static final int databits = SerialPort.DATABITS_8;
    private static final int stopbit = SerialPort.STOPBITS_1;
    private static final int parity = SerialPort.PARITY_NONE;

    private int commandInterval;
    private String address;

    private long lastCommandTime = 0;

    private CommPortIdentifier portId;
    private SerialPort serialPort;
    private OutputStream outputStream;
    private InputStream inputStream;

    public UrtsiDeviceHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge does not have any channels
    }

    /**
     * Executes the given {@link RtsCommand} for the given {@link Thing} (RTS Device).
     *
     * @param rtsDevice the RTS Device which is the receiver of the command.
     * @param rtsCommand the command to be executed
     * @return
     */
    public boolean executeRtsCommand(Thing rtsDevice, RtsCommand rtsCommand) {
        RtsDeviceConfig rtsDeviceConfig = rtsDevice.getConfiguration().as(RtsDeviceConfig.class);
        String mappedChannel = UrtsiChannelMapping.getMappedChannel(rtsDeviceConfig.channel);
        if (mappedChannel == null) {
            return false;
        }
        String urtsiCommand = new StringBuilder(address).append(mappedChannel).append(rtsCommand.getActionKey())
                .toString();
        boolean executedSuccessfully = writeString(urtsiCommand);
        return executedSuccessfully;
    }

    /**
     * Sends a string to the serial port of this device.
     * The writing of the msg is executed synchronized, so it's guaranteed that the device doesn't get
     * multiple messages concurrently.
     *
     * @param msg
     *            the string to send
     * @return true, if the message has been transmitted successfully, otherwise false.
     */
    protected synchronized boolean writeString(final String msg) {
        logger.debug("Writing '{}' to serial port {}", msg, portId.getName());

        final long earliestNextExecution = lastCommandTime + commandInterval;
        while (earliestNextExecution > System.currentTimeMillis()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                return false;
            }
        }
        try {
            final List<Boolean> listenerResult = new ArrayList<Boolean>();
            serialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                        // we get here if data has been received
                        final StringBuilder sb = new StringBuilder();
                        final byte[] readBuffer = new byte[20];
                        try {
                            do {
                                // read data from serial device
                                while (inputStream.available() > 0) {
                                    final int bytes = inputStream.read(readBuffer);
                                    sb.append(new String(readBuffer, 0, bytes));
                                }
                                try {
                                    // add wait states around reading the stream, so that interrupted transmissions are
                                    // merged
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    // ignore interruption
                                }
                            } while (inputStream.available() > 0);
                            final String result = sb.toString();
                            if (result.equals(msg)) {
                                listenerResult.add(true);
                            }
                        } catch (IOException e) {
                            logger.debug("Error receiving data on serial port {}: {}", portId.getName(),
                                    e.getMessage());
                        }
                    }
                }
            });
            serialPort.notifyOnDataAvailable(true);
            outputStream.write(msg.getBytes());
            outputStream.flush();
            lastCommandTime = System.currentTimeMillis();
            final long timeout = lastCommandTime + 1000;
            while (listenerResult.isEmpty() && System.currentTimeMillis() < timeout) {
                // Waiting for response
                Thread.sleep(100);
            }
            return !listenerResult.isEmpty();
        } catch (Exception e) {
            logger.error("Error writing '{}' to serial port {}: {}", msg, portId.getName(), e.getMessage());
        } finally {
            serialPort.removeEventListener();
        }
        return false;
    }

    /**
     * Registers the given port as system property {@value #GNU_IO_RXTX_SERIAL_PORTS}. The method is capable of
     * extending the system property, if any other ports are already registered.
     *
     * @param port the port to be registered
     */
    private void initSerialPort(String port) {
        String serialPortsProperty = System.getProperty(GNU_IO_RXTX_SERIAL_PORTS);
        Set<String> serialPorts = null;
        if (serialPortsProperty != null) {
            serialPorts = Sets.newHashSet(Splitter.on(":").split(serialPortsProperty));
        } else {
            serialPorts = new HashSet<String>();
        }
        if (serialPorts.add(port)) {
            logger.debug("Added {} to the {} system property.", port, GNU_IO_RXTX_SERIAL_PORTS);
            System.setProperty(GNU_IO_RXTX_SERIAL_PORTS, Joiner.on(":").join(serialPorts));
        }
    }

    @Override
    public void initialize() {
        address = getThing().getProperties().get("address");
        UrtsiDeviceConfig urtsiDeviceConfig = getConfigAs(UrtsiDeviceConfig.class);
        commandInterval = urtsiDeviceConfig.commandInterval;
        String port = urtsiDeviceConfig.port;
        initSerialPort(port);
        try {
            portId = CommPortIdentifier.getPortIdentifier(port);
            // initialize serial port
            serialPort = portId.open("openHAB", 2000);
            // set port parameters
            serialPort.setSerialPortParams(baud, databits, stopbit, parity);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            updateStatus(ThingStatus.ONLINE);
        } catch (NoSuchPortException e) {
            // enumerate the port identifiers in the exception to be helpful
            final StringBuilder sb = new StringBuilder();
            @SuppressWarnings("unchecked")
            Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();
            while (portList.hasMoreElements()) {
                final CommPortIdentifier id = portList.nextElement();
                if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    sb.append(id.getName() + "\n");
                }
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Serial port '" + port + "' could not be found. Available ports are:\n" + sb.toString());
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("An error occurred while initializing the Urtsi II connection.", e);
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "An error occurred while initializing the Urtsi II connection: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        IOUtils.closeQuietly(outputStream);
        IOUtils.closeQuietly(inputStream);
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }
}
