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
package org.openhab.binding.knx.internal.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.spi.ServiceProvider;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.serial.spi.SerialCom;

/**
 * The {@link SerialTransportAdapter} provides org.openhab.core.io.transport.serial
 * services to the Calimero library.
 * 
 * {@literal @}ServiceProvider annotation (biz.aQute.bnd.annotation) automatically creates the file
 * /META-INF/services/tuwien.auto.calimero.serial.spi.SerialCom
 * to register SerialTransportAdapter to the service loader.
 * Additional attributes for SerialTransportAdapter can be specified as well, e.g.
 * attribute = { "position=1" }
 * and will be part of MANIFEST.MF
 * 
 * @author Holger Friedrich - Initial contribution
 */
@ServiceProvider(value = SerialCom.class)
@NonNullByDefault
public class SerialTransportAdapter implements SerialCom {
    private static final int OPEN_TIMEOUT_MS = 200;
    private static final int RECEIVE_TIMEOUT_MS = 5;
    private static final int RECEIVE_THRESHOLD = 1024;
    private static final int BAUDRATE = 19200;

    private Logger logger = LoggerFactory.getLogger(SerialTransportAdapter.class);
    @Nullable
    private static SerialPortManager serialPortManager = null;
    @Nullable
    private SerialPort serialPort = null;

    public static void setSerialPortManager(SerialPortManager serialPortManager) {
        SerialTransportAdapter.serialPortManager = serialPortManager;
    }

    public SerialTransportAdapter() {
    }

    @Override
    public void open(@Nullable String portId) throws IOException, KNXException {
        if (portId == null) {
            throw new IOException("Port not available");
        }
        logger = LoggerFactory.getLogger(SerialTransportAdapter.class.getName() + ":" + portId);

        final @Nullable SerialPortManager tmpSerialPortManager = serialPortManager;
        if (tmpSerialPortManager == null) {
            throw new IOException("PortManager not available");
        }
        try {
            SerialPortIdentifier portIdentifier = tmpSerialPortManager.getIdentifier(portId);
            if (portIdentifier != null) {
                if (portIdentifier.isCurrentlyOwned()) {
                    logger.warn("Configured port {} is currently in use by another application: {}", portId,
                            portIdentifier.getCurrentOwner());
                }
                logger.trace("Trying to open port {}", portId);
                SerialPort serialPort = portIdentifier.open(this.getClass().getName(), OPEN_TIMEOUT_MS);
                // apply default settings for com port, may be overwritten by caller
                serialPort.setSerialPortParams(BAUDRATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_EVEN);
                serialPort.enableReceiveThreshold(RECEIVE_THRESHOLD);
                serialPort.enableReceiveTimeout(RECEIVE_TIMEOUT_MS);
                this.serialPort = serialPort;

                // Notification / event listeners are available and may be used to log/trace com failures
                // serialPort.notifyOnDataAvailable(true);
                logger.trace("Port opened successfully");
            } else {
                logger.info("Port {} not available", portId);
                throw new IOException("Port " + portId + " not available");
            }
        } catch (PortInUseException e) {
            logger.info("Port {} already in use", portId);
            throw new IOException("Port " + portId + " already in use", e);
        } catch (UnsupportedCommOperationException e) {
            logger.info("Port {} unsupported com operation", portId);
            throw new IOException("Port " + portId + " unsupported com operation", e);
        }
    }

    // SerialCom extends AutoCloseable, close() throws Exception
    @Override
    public void close() {
        logger.trace("Closing serial port");
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort != null) {
            tmpSerialPort.close();
            serialPort = null;
        }
    }

    @Override
    public @Nullable InputStream inputStream() {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort != null) {
            try {
                return tmpSerialPort.getInputStream();
            } catch (IOException e) {
                logger.info("Cannot open input stream");
            }
        }
        // should not throw, create a dummy return value
        byte[] buf = {};
        return new ByteArrayInputStream(buf);
    }

    @Override
    public @Nullable OutputStream outputStream() {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort != null) {
            try {
                return tmpSerialPort.getOutputStream();
            } catch (IOException e) {
                logger.info("Cannot open output stream");
            }
        }
        // should not throw, create a dummy return value
        return new ByteArrayOutputStream(0);
    }

    // disable NonNullByDefault for this function, legacy interface List<String>
    @NonNullByDefault({})
    @Override
    public List<String> portIdentifiers() {
        final @Nullable SerialPortManager tmpSerialPortManager = serialPortManager;
        if (tmpSerialPortManager == null) {
            return Collections.emptyList();
        }
        // typecast only required to avoid warning about less-annotated type
        return (List<String>) tmpSerialPortManager.getIdentifiers().map(SerialPortIdentifier::getName)
                .collect(Collectors.toList());
    }

    @Override
    public int baudRate() throws IOException {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort == null) {
            throw new IOException("Port not available");
        }
        return tmpSerialPort.getBaudRate();
    }

    @Override
    public void setSerialPortParams(final int baudrate, final int databits, @Nullable StopBits stopbits,
            @Nullable Parity parity) throws IOException {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort == null) {
            throw new IOException("Port not available");
        }
        if ((stopbits == null) || (parity == null)) {
            throw new IOException("Invalid parameters");
        }
        try {
            tmpSerialPort.setSerialPortParams(baudrate, databits, stopbits.value(), parity.value());
        } catch (final UnsupportedCommOperationException e) {
            throw new IOException("Setting serial port parameters for " + tmpSerialPort.getName() + " failed", e);
        }
    }

    @Override
    public void setFlowControlMode(@Nullable FlowControl mode) throws IOException {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort == null) {
            throw new IOException("Port not available");
        }
        if (mode == null) {
            throw new IOException("Invalid parameters");
        }
        if (mode == FlowControl.None) {
            try {
                tmpSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            } catch (final UnsupportedCommOperationException e) {
                throw new IOException("Setting flow control parameters for " + tmpSerialPort.getName() + " failed", e);
            }
        } else {
            logger.warn("Unknown FlowControl mode {}", mode);
            throw new IOException("Invalid flow mode");
        }
    }
}
