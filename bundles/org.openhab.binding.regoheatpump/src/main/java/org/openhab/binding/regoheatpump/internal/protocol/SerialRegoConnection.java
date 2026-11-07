/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SerialRegoConnection} is responsible for creating serial connections to clients.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class SerialRegoConnection implements RegoConnection {
    private final int baudRate;
    private final String portName;
    private @Nullable SerialPort serialPort;
    private final SerialPortIdentifier serialPortIdentifier;
    private static final Map<String, Object> PORT_LOCKS = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SerialRegoConnection.class);

    public SerialRegoConnection(SerialPortIdentifier serialPortIdentifier, int baudRate) {
        this.serialPortIdentifier = serialPortIdentifier;
        this.portName = serialPortIdentifier.getName();
        this.baudRate = baudRate;
    }

    @Override
    public void connect() throws IOException {
        Object lock = PORT_LOCKS.computeIfAbsent(portName, k -> new Object());

        synchronized (lock) {
            try {
                SerialPort serialPort = serialPortIdentifier.open(SerialRegoConnection.class.getCanonicalName(), 2000);
                serialPort.enableReceiveTimeout(100);
                serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                this.serialPort = serialPort;
            } catch (PortInUseException e) {
                logger.debug("Port '{}' is in use (owner: {}).", portName, e.getMessage());
                throw new IOException("Serial port already used: " + portName, e);
            } catch (UnsupportedCommOperationException e) {
                throw new IOException("Unsupported operation on '" + portName + "': " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isConnected() {
        return serialPort != null;
    }

    @Override
    public void close() {
        SerialPort serialPort = this.serialPort;
        this.serialPort = null; // Nullify first to prevent concurrent access

        if (serialPort != null) {
            try {
                // Close input/output streams first to ensure clean shutdown
                try {
                    InputStream inputStream = serialPort.getInputStream();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    // Log but continue with port closure
                }

                try {
                    OutputStream outputStream = serialPort.getOutputStream();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (Exception e) {
                    // Log but continue with port closure
                }

                // Remove any event listeners to prevent callbacks after closure
                try {
                    serialPort.removeEventListener();
                } catch (Exception e) {
                    // Some implementations may not support this
                }

                // Finally close the serial port
                serialPort.close();

            } catch (Exception e) {
                // Log the error but don't rethrow - we want to ensure the port reference is nullified
                logger.warn("Error closing serial port {}: {}", portName, e.getMessage(), e);
            }
        }
    }

    @Override
    public OutputStream outputStream() throws IOException {
        OutputStream outputStream = getSerialPort().getOutputStream();
        if (outputStream == null) {
            throw new IOException("Sending data is not supported");
        }
        return outputStream;
    }

    @Override
    public InputStream inputStream() throws IOException {
        InputStream inputStream = getSerialPort().getInputStream();
        if (inputStream == null) {
            throw new IOException("Receiving data is not supported");
        }
        return inputStream;
    }

    private SerialPort getSerialPort() throws IOException {
        SerialPort serialPort = this.serialPort;
        if (serialPort == null) {
            throw new IOException("Connection closed");
        }
        return serialPort;
    }
}
