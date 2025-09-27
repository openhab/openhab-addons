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
package org.openhab.binding.rfxcom.internal.connector;

import static org.openhab.binding.rfxcom.internal.RFXComBindingConstants.MAX_RFXCOM_MESSAGE_LEN;

import java.io.IOException;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM connector for serial port communication.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComSerialConnector extends RFXComBaseConnector implements SerialPortEventListener {
    private final Logger logger = LoggerFactory.getLogger(RFXComSerialConnector.class);

    private OutputStream out;
    private SerialPort serialPort;
    private final SerialPortManager serialPortManager;

    private final String readerThreadName;
    private Thread readerThread;

    public RFXComSerialConnector(SerialPortManager serialPortManager, String readerThreadName) {
        this.serialPortManager = serialPortManager;
        this.readerThreadName = readerThreadName;
    }

    @Override
    public void connect(RFXComBridgeConfiguration device)
            throws RFXComException, PortInUseException, UnsupportedCommOperationException, IOException {
        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(device.serialPort);
        if (portIdentifier == null) {
            logger.debug("No serial port {}", device.serialPort);
            throw new RFXComException("No serial port " + device.serialPort);
        }

        SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

        serialPort = commPort;
        serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.enableReceiveThreshold(MAX_RFXCOM_MESSAGE_LEN);
        serialPort.enableReceiveTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.

        in = serialPort.getInputStream();
        out = serialPort.getOutputStream();

        out.flush();
        if (in.markSupported()) {
            in.reset();
        }

        // RXTX serial port library causes high CPU load
        // Start event listener, which will just sleep and slow down event loop
        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            logger.debug("Serial port event listener started");
        } catch (TooManyListenersException e) {
        }

        readerThread = new RFXComStreamReader(this, readerThreadName);
        readerThread.start();
    }

    @Override
    public void disconnect() {
        logger.debug("Disconnecting");

        if (serialPort != null) {
            serialPort.removeEventListener();
            logger.debug("Serial port event listener stopped");
        }

        if (readerThread != null) {
            logger.debug("Interrupt serial listener");
            readerThread.interrupt();
            try {
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }

        if (out != null) {
            logger.debug("Close serial out stream");
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("Error while closing the out stream: {}", e.getMessage());
            }
        }
        if (in != null) {
            logger.debug("Close serial in stream");
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("Error while closing the in stream: {}", e.getMessage());
            }
        }

        if (serialPort != null) {
            logger.debug("Close serial port");
            serialPort.close();
        }

        readerThread = null;
        serialPort = null;
        out = null;
        in = null;

        logger.debug("Closed");
    }

    @Override
    public void sendMessage(byte[] data) throws IOException {
        if (out == null) {
            throw new IOException("Not connected sending messages is not possible");
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Send data (len={}): {}", data.length, HexUtils.bytesToHex(data));
        }

        out.write(data);
        out.flush();
    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
        try {
            /*
             * See more details from
             * https://github.com/NeuronRobotics/nrjavaserial/issues/22
             */
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignore) {
        }
    }
}
