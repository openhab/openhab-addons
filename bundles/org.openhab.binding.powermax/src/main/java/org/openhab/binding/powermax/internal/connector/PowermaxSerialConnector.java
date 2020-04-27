/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.io.UnsupportedEncodingException;
import java.util.TooManyListenersException;

import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
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
    private SerialPort serialPort;
    private SerialPortManager serialPortManager;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name
     * @param baudRate the baud rate to be used
     */
    public PowermaxSerialConnector(SerialPortManager serialPortManager, String serialPortName, int baudRate) {
        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPortName;
        this.baudRate = baudRate;
        this.serialPort = null;
    }

    @Override
    public void open() {
        logger.debug("open(): Opening Serial Connection");

        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier != null) {
                SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

                serialPort = commPort;
                serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
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
                try {
                    serialPort.addEventListener(this);
                    serialPort.notifyOnDataAvailable(true);
                } catch (TooManyListenersException e) {
                    logger.debug("Too Many Listeners Exception: {}", e.getMessage(), e);
                }

                setReaderThread(new PowermaxReaderThread(this));
                getReaderThread().start();

                setConnected(true);
            } else {
                logger.debug("open(): No Such Port: {}", serialPortName);
                setConnected(false);
            }
        } catch (PortInUseException e) {
            logger.debug("open(): Port in Use Exception: {}", e.getMessage(), e);
            setConnected(false);
        } catch (UnsupportedCommOperationException e) {
            logger.debug("open(): Unsupported Comm Operation Exception: {}", e.getMessage(), e);
            setConnected(false);
        } catch (UnsupportedEncodingException e) {
            logger.debug("open(): Unsupported Encoding Exception: {}", e.getMessage(), e);
            setConnected(false);
        } catch (IOException e) {
            logger.debug("open(): IO Exception: {}", e.getMessage(), e);
            setConnected(false);
        }
    }

    @Override
    public void close() {
        logger.debug("close(): Closing Serial Connection");

        if (serialPort != null) {
            serialPort.removeEventListener();
        }

        super.cleanup();

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
