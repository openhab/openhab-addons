/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.urtsi.internal.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;
import java.util.stream.Collectors;

import org.openhab.binding.urtsi.internal.config.RtsDeviceConfig;
import org.openhab.binding.urtsi.internal.config.UrtsiDeviceConfig;
import org.openhab.binding.urtsi.internal.mapping.UrtsiChannelMapping;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UrtsiDeviceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class UrtsiDeviceHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(UrtsiDeviceHandler.class);

    private static final int BAUD = 9600;
    private static final int DATABITS = SerialPort.DATABITS_8;
    private static final int STOPBIT = SerialPort.STOPBITS_1;
    private static final int PARITY = SerialPort.PARITY_NONE;

    private int commandInterval;
    private String address;

    private long lastCommandTime;

    private SerialPortIdentifier portId;
    private SerialPort serialPort;
    private final SerialPortManager serialPortManager;
    private OutputStream outputStream;
    private InputStream inputStream;

    public UrtsiDeviceHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
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
     * @param msg the string to send
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
            final List<Boolean> listenerResult = new ArrayList<>();
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
        } catch (IOException | TooManyListenersException | InterruptedException e) {
            logger.error("Error writing '{}' to serial port {}: {}", msg, portId.getName(), e.getMessage());
        } finally {
            serialPort.removeEventListener();
        }
        return false;
    }

    @Override
    public void initialize() {
        address = getThing().getProperties().get("address");
        UrtsiDeviceConfig urtsiDeviceConfig = getConfigAs(UrtsiDeviceConfig.class);
        commandInterval = urtsiDeviceConfig.commandInterval;
        String port = urtsiDeviceConfig.port;

        portId = serialPortManager.getIdentifier(port);

        if (portId == null) {
            String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                    .collect(Collectors.joining(System.lineSeparator()));
            String description = String.format("Serial port '%s' could not be found. Available ports are:%n%s", port,
                    availablePorts);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
            return;
        }

        try {
            serialPort = portId.open("openHAB", 2000);
            serialPort.setSerialPortParams(BAUD, DATABITS, STOPBIT, PARITY);
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Error: " + e.getMessage());
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port already used: " + port);
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unsupported operation on port '" + port + "': " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
        }
        if (serialPort != null) {
            serialPort.close();
        }
        outputStream = null;
        inputStream = null;
        serialPort = null;
    }
}
