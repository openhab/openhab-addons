/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.somfycul.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
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
 * The {@link CULHandler} is responsible for handling commands, which are
 * sent via the CUL stick.
 *
 * @author Marc Klasser - Initial contribution
 *
 */
@NonNullByDefault
public class CULHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(CULHandler.class);

    private static final int COMMAND_DELAY_MS = 100;

    private long lastCommandTime = 0;

    @Nullable
    private SerialPortIdentifier portId;
    @Nullable
    private SerialPort serialPort;
    private final SerialPortManager serialPortManager;
    @Nullable
    private OutputStream outputStream;
    @Nullable
    private InputStream inputStream;

    public CULHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge does not have any channels
    }

    /**
     * Executes the given {@link SomfyCommand} for the given {@link Thing} (RTS Device).
     *
     * @param somfyDevice the RTS Device which is the receiver of the command.
     * @param somfyCommand the command to execute
     * @param rollingCode the current rolling code for the device
     * @param address the device address
     * @return true if the command was successfully transmitted to the CUL device, false otherwise
     */
    public boolean executeCULCommand(Thing somfyDevice, SomfyCommand somfyCommand, String rollingCode, String address) {
        String culCommand = "Ys" + "A1" + somfyCommand.getActionKey() + "0" + rollingCode + address;
        logger.debug("Send message {} for thing {}", culCommand, somfyDevice.getLabel());
        return writeString(culCommand);
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
        final SerialPortIdentifier localPortId = portId;
        final OutputStream localOutputStream = outputStream;

        if (localPortId == null || localOutputStream == null) {
            logger.warn("Cannot write to serial port - port or stream not initialized");
            return false;
        }

        logger.debug("Trying to write '{}' to serial port {}", msg, localPortId.getName());

        final long earliestNextExecution = lastCommandTime + COMMAND_DELAY_MS;
        while (earliestNextExecution > System.currentTimeMillis()) {
            try {
                Thread.sleep(COMMAND_DELAY_MS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        try {
            localOutputStream.write((msg + "\n").getBytes());
            localOutputStream.flush();
            lastCommandTime = System.currentTimeMillis();
            return true;
        } catch (IOException e) {
            logger.error("Error writing '{}' to serial port {}: {}", msg, localPortId.getName(), e.getMessage());
        }
        return false;
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing!");
        CULConfiguration config = getConfigAs(CULConfiguration.class);
        if (validConfiguration(config)) {
            String port = config.port;
            SerialPortIdentifier localPortId = serialPortManager.getIdentifier(port);
            if (localPortId == null) {
                String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                        .collect(Collectors.joining(System.lineSeparator()));
                String description = String.format("Serial port '%s' could not be found. Available ports are:%n%s",
                        port, availablePorts);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, description);
                return;
            }
            portId = localPortId;
            logger.debug("got port: {}", config.port);

            try {
                SerialPort localSerialPort = localPortId.open("openHAB", 2000);
                // set port parameters
                int baudRate = config.baudrate;
                localSerialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                InputStream localInputStream = localSerialPort.getInputStream();
                OutputStream localOutputStream = localSerialPort.getOutputStream();

                // Only set instance variables after successful initialization
                serialPort = localSerialPort;
                inputStream = localInputStream;
                outputStream = localOutputStream;

                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "IO Error: " + e.getMessage());
            } catch (PortInUseException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Port already used: " + port);
            } catch (UnsupportedCommOperationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unsupported operation on port '" + port + "': " + e.getMessage());
            }
        }
    }

    private boolean validConfiguration(@Nullable CULConfiguration config) {
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "somfycul configuration missing");
            return false;
        }
        if (config.port.isEmpty() || config.baudrate <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "somfycul port or baudrate not specified");
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {
        super.dispose();

        // Store references to avoid NPE if they're nulled during disposal
        final SerialPort localSerialPort = serialPort;
        final OutputStream localOutputStream = outputStream;
        final InputStream localInputStream = inputStream;

        // Close resources in reverse order of acquisition
        if (localOutputStream != null) {
            try {
                localOutputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the output stream: {}", e.getMessage());
            }
        }

        if (localInputStream != null) {
            try {
                localInputStream.close();
            } catch (IOException e) {
                logger.debug("Error while closing the input stream: {}", e.getMessage());
            }
        }

        if (localSerialPort != null) {
            localSerialPort.removeEventListener();
            localSerialPort.close();
        }

        // Clear all references
        outputStream = null;
        inputStream = null;
        serialPort = null;
        portId = null;
    }
}
