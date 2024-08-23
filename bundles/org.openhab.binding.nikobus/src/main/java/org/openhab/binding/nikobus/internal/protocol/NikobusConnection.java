/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikobus.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikobusConnection } is responsible for creating connections to clients.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class NikobusConnection implements SerialPortEventListener {
    private final Logger logger = LoggerFactory.getLogger(NikobusConnection.class);
    private final SerialPortManager serialPortManager;
    private final String portName;
    private final Consumer<Byte> processData;
    private @Nullable SerialPort serialPort;

    public NikobusConnection(SerialPortManager serialPortManager, String portName, Consumer<Byte> processData) {
        this.serialPortManager = serialPortManager;
        this.portName = portName;
        this.processData = processData;
    }

    /**
     * Return true if this manager is connected.
     *
     * @return
     */
    public boolean isConnected() {
        return serialPort != null;
    }

    /**
     * Connect to the receiver.
     *
     **/
    public void connect() throws IOException {
        if (isConnected()) {
            return;
        }

        SerialPortIdentifier portId = serialPortManager.getIdentifier(portName);
        if (portId == null) {
            throw new IOException(String.format("Port '%s' is not known!", portName));
        }

        logger.info("Connecting to {}", portName);

        try {
            SerialPort serialPort = portId.open("org.openhab.binding.nikobus.pc-link", 2000);
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            this.serialPort = serialPort;
            logger.info("Connected to {}", portName);
        } catch (PortInUseException e) {
            throw new IOException(String.format("Port '%s' is in use!", portName), e);
        } catch (TooManyListenersException e) {
            throw new IOException(String.format("Cannot attach listener to port '%s'!", portName), e);
        }
    }

    /**
     * Closes the connection.
     **/
    public void close() {
        SerialPort serialPort = this.serialPort;
        this.serialPort = null;

        if (serialPort != null) {
            try {
                serialPort.removeEventListener();
                OutputStream outputStream = serialPort.getOutputStream();
                if (outputStream != null) {
                    outputStream.close();
                }
                InputStream inputStream = serialPort.getInputStream();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.debug("Error closing serial port.", e);
            } finally {
                serialPort.close();
                logger.debug("Closed serial port.");
            }
        }
    }

    /**
     * Returns an output stream for this connection.
     */
    public @Nullable OutputStream getOutputStream() throws IOException {
        SerialPort serialPort = this.serialPort;
        if (serialPort == null) {
            return null;
        }
        return serialPort.getOutputStream();
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
            return;
        }
        SerialPort serialPort = this.serialPort;
        if (serialPort == null) {
            return;
        }
        try {
            InputStream inputStream = serialPort.getInputStream();
            if (inputStream == null) {
                return;
            }
            byte[] readBuffer = new byte[64];
            while (inputStream.available() > 0) {
                int length = inputStream.read(readBuffer);
                for (int i = 0; i < length; ++i) {
                    processData.accept(readBuffer[i]);
                }
            }
        } catch (IOException e) {
            logger.debug("Error reading from serial port: {}", e.getMessage(), e);
        }
    }
}
