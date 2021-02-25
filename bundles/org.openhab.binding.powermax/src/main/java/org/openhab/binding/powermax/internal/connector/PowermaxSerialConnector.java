/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
public class PowermaxSerialConnector extends PowermaxConnector implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(PowermaxSerialConnector.class);

    private final String serialPortName;
    private final int baudRate;
    private final SerialPortManager serialPortManager;
    private SerialPort serialPort;

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
        this.serialPort = null;
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
        serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        serialPort.enableReceiveThreshold(1);
        serialPort.enableReceiveTimeout(250);

        setInput(serialPort.getInputStream());
        setOutput(serialPort.getOutputStream());

        getOutput().flush();
        if (getInput().markSupported()) {
            getInput().reset();
        }

        // RXTX serial port library causes high CPU load
        // Start event listener, which will just sleep and slow down event
        // loop
        serialPort.addEventListener(this);
        serialPort.notifyOnDataAvailable(true);

        setReaderThread(new PowermaxReaderThread(this, readerThreadName));
        getReaderThread().start();

        setConnected(true);
    }

    @Override
    public void close() {
        logger.debug("close(): Closing Serial Connection");

        if (serialPort != null) {
            serialPort.removeEventListener();
        }

        super.cleanup(true);

        if (serialPort != null) {
            serialPort.close();
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
        }
    }
}
