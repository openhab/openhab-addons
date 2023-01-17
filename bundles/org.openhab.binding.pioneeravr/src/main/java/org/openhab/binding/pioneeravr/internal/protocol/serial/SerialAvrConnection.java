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
package org.openhab.binding.pioneeravr.internal.protocol.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pioneeravr.internal.protocol.StreamAvrConnection;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that wraps the communication to a Pioneer AVR devices through a serial port
 *
 * @author Antoine Besnard - Initial contribution
 */
@NonNullByDefault
public class SerialAvrConnection extends StreamAvrConnection {

    private final Logger logger = LoggerFactory.getLogger(SerialAvrConnection.class);

    private static final Integer LINK_SPEED = 9600;

    private final String portName;

    private @Nullable SerialPort serialPort;

    private final SerialPortManager serialPortManager;

    public SerialAvrConnection(String portName, SerialPortManager serialPortManager) {
        this.portName = portName;
        this.serialPortManager = serialPortManager;
    }

    @Override
    protected void openConnection() throws IOException {
        SerialPortIdentifier serialPortIdentifier = serialPortManager.getIdentifier(portName);
        if (serialPortIdentifier == null) {
            String availablePorts = serialPortManager.getIdentifiers().map(id -> id.getName())
                    .collect(Collectors.joining(", "));
            throw new IOException(
                    "Serial port with name " + portName + " does not exist. Available port names: " + availablePorts);

        }

        try {
            SerialPort localSerialPort = serialPortIdentifier.open(SerialAvrConnection.class.getSimpleName(), 2000);
            localSerialPort.setSerialPortParams(LINK_SPEED, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort = localSerialPort;
        } catch (PortInUseException | UnsupportedCommOperationException e) {
            throw new IOException("Failed to connect on port " + portName);
        }

        logger.debug("Connected to {}", getConnectionName());
    }

    @Override
    public boolean isConnected() {
        return serialPort != null;
    }

    @Override
    public void close() {
        super.close();
        SerialPort localSerialPort = serialPort;
        if (localSerialPort != null) {
            localSerialPort.close();
            serialPort = null;
            logger.debug("Closed port {}", portName);
        }
    }

    @Override
    public String getConnectionName() {
        return portName;
    }

    @Override
    protected @Nullable InputStream getInputStream() throws IOException {
        SerialPort localSerialPort = serialPort;
        return localSerialPort != null ? localSerialPort.getInputStream() : null;
    }

    @Override
    protected @Nullable OutputStream getOutputStream() throws IOException {
        SerialPort localSerialPort = serialPort;
        return localSerialPort != null ? localSerialPort.getOutputStream() : null;
    }
}
