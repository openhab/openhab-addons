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

import org.eclipse.jdt.annotation.NonNull;
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
    private final @NonNull Object portLock;
    private @Nullable SerialPort serialPort;
    private final SerialPortIdentifier serialPortIdentifier;
    private static final Map<String, @NonNull Object> PORT_LOCKS = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SerialRegoConnection.class);

    public SerialRegoConnection(SerialPortIdentifier serialPortIdentifier, int baudRate) {
        this.serialPortIdentifier = serialPortIdentifier;
        this.portName = serialPortIdentifier.getName();
        this.baudRate = baudRate;
        Object lock = PORT_LOCKS.computeIfAbsent(portName, key -> new Object());
        if (lock == null) {
            throw new IllegalStateException("Failed to initialize serial port lock for '" + portName + "'");
        }
        this.portLock = lock;
    }

    @Override
    public void connect() throws IOException {
        synchronized (portLock) {
            if (serialPort != null) {
                return;
            }

            SerialPort openedPort = null;
            try {
                openedPort = serialPortIdentifier.open(SerialRegoConnection.class.getCanonicalName(), 2000);
                openedPort.enableReceiveTimeout(100);
                openedPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort = openedPort;
            } catch (PortInUseException e) {
                closeSerialPort(openedPort);
                logger.debug("Port '{}' is in use (owner: {}).", portName, e.getMessage());
                throw new IOException("Serial port already used: " + portName, e);
            } catch (UnsupportedCommOperationException e) {
                closeSerialPort(openedPort);
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
        synchronized (portLock) {
            closeSerialPort(serialPort);
            serialPort = null;
        }
    }

    private void closeSerialPort(@Nullable SerialPort serialPortToClose) {
        if (serialPortToClose == null) {
            return;
        }

        try {
            try {
                InputStream inputStream = serialPortToClose.getInputStream();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                logger.debug("Error closing serial input stream on {}: {}", portName, e.getMessage(), e);
            }

            try {
                OutputStream outputStream = serialPortToClose.getOutputStream();
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.debug("Error closing serial output stream on {}: {}", portName, e.getMessage(), e);
            }

            try {
                serialPortToClose.removeEventListener();
            } catch (Exception e) {
                logger.debug("Error removing serial event listener on {}: {}", portName, e.getMessage(), e);
            }
            serialPortToClose.close();
        } catch (Exception e) {
            logger.warn("Error closing serial port {}: {}", portName, e.getMessage(), e);
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
