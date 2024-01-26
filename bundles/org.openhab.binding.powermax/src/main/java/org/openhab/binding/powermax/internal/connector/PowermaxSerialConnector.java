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
package org.openhab.binding.powermax.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for the communication with the Visonic alarm panel through a serial connection
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxSerialConnector extends PowermaxConnector implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(PowermaxSerialConnector.class);

    private final String serialPortName;
    private final int baudRate;
    private final SerialPortManager serialPortManager;
    private @Nullable SerialPort serialPort;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name
     * @param baudRate the baud rate to be used
     * @param readerThreadName the name of thread to be created
     */
    public PowermaxSerialConnector(SerialPortManager serialPortManager, String serialPortName, int baudRate,
            String readerThreadName) {
        super(readerThreadName);
        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPortName;
        this.baudRate = baudRate;
    }

    @Override
    public void open() throws Exception {
        logger.debug("open(): Opening Serial Connection");

        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
        if (portIdentifier == null) {
            throw new IOException("No Such Port: " + serialPortName);
        }

        SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

        serialPort = commPort;
        commPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        commPort.enableReceiveThreshold(1);
        commPort.enableReceiveTimeout(250);

        InputStream inputStream = commPort.getInputStream();
        setInput(inputStream);
        OutputStream outputStream = commPort.getOutputStream();
        setOutput(outputStream);

        if (outputStream != null) {
            outputStream.flush();
        }
        if (inputStream != null && inputStream.markSupported()) {
            inputStream.reset();
        }

        // RXTX serial port library causes high CPU load
        // Start event listener, which will just sleep and slow down event
        // loop
        commPort.addEventListener(this);
        commPort.notifyOnDataAvailable(true);

        PowermaxReaderThread readerThread = new PowermaxReaderThread(this, readerThreadName);
        setReaderThread(readerThread);
        readerThread.start();

        setConnected(true);
    }

    @Override
    public void close() {
        logger.debug("close(): Closing Serial Connection");

        SerialPort commPort = serialPort;
        if (commPort != null) {
            commPort.removeEventListener();
        }

        super.cleanup(true);

        if (commPort != null) {
            commPort.close();
        }

        serialPort = null;

        setConnected(false);

        logger.debug("close(): Serial Connection closed");
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
