/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.protocol.RotelAbstractProtocolHandler;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the Rotel device through a serial connection
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelSerialConnector extends RotelConnector {

    private final Logger logger = LoggerFactory.getLogger(RotelSerialConnector.class);

    private String serialPortName;
    private SerialPortManager serialPortManager;

    private int baudRate;

    private @Nullable SerialPort serialPort;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name to be used
     * @param baudRate the baud rate to be used
     * @param protocolHandler the protocol handler
     * @param readerThreadName the name of thread to be created
     */
    public RotelSerialConnector(SerialPortManager serialPortManager, String serialPortName, int baudRate,
            RotelAbstractProtocolHandler protocolHandler, String readerThreadName) {
        super(protocolHandler, false, readerThreadName);

        this.baudRate = baudRate;
        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPortName;
    }

    @Override
    public synchronized void open() throws RotelException {
        logger.debug("Opening serial connection on port {}", serialPortName);
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier == null) {
                setConnected(false);
                throw new RotelException("Opening serial connection failed: no port " + serialPortName);
            }

            SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            commPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            commPort.enableReceiveThreshold(1);
            commPort.enableReceiveTimeout(100);
            commPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            InputStream dataIn = commPort.getInputStream();
            OutputStream dataOut = commPort.getOutputStream();

            if (dataOut != null) {
                dataOut.flush();
            }
            if (dataIn != null && dataIn.markSupported()) {
                try {
                    dataIn.reset();
                } catch (IOException e) {
                }
            }

            readerThread.start();

            this.serialPort = commPort;
            this.dataIn = dataIn;
            this.dataOut = dataOut;

            setConnected(true);

            logger.debug("Serial connection opened");
        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            setConnected(false);
            throw new RotelException("Opening serial connection failed", e);
        }
    }

    @Override
    public synchronized void close() {
        logger.debug("Closing serial connection");
        SerialPort serialPort = this.serialPort;
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        super.cleanup();
        if (serialPort != null) {
            serialPort.close();
            this.serialPort = null;
        }
        setConnected(false);
        logger.debug("Serial connection closed");
    }
}
