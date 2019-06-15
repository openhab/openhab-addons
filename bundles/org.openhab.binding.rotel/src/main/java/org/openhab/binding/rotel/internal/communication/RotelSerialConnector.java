/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.io.UnsupportedEncodingException;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the Rotel device through a serial connection
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelSerialConnector extends RotelConnector implements SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RotelSerialConnector.class);

    private String serialPortName;
    private SerialPortManager serialPortManager;

    private @NonNullByDefault({}) SerialPort serialPort;

    /**
     * Constructor
     *
     * @param serialPortManager the serial port manager
     * @param serialPortName the serial port name to be used
     * @param model the projector model in use
     * @param protocol the protocol to be used
     */
    public RotelSerialConnector(SerialPortManager serialPortManager, String serialPortName, RotelModel model,
            RotelProtocol protocol) {
        super(model, protocol, false);

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
                logger.debug("Opening serial connection failed: No Such Port: {}", serialPortName);
                throw new RotelException("Opening serial connection failed: No Such Port");
            }

            SerialPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            serialPort = commPort;
            serialPort.setSerialPortParams(getModel().getBaudRate(), SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(100);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            dataIn = serialPort.getInputStream();
            dataOut = serialPort.getOutputStream();

            dataOut.flush();
            if (dataIn.markSupported()) {
                try {
                    dataIn.reset();
                } catch (IOException e) {
                }
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

            setReaderThread(new RotelReaderThread(this));
            getReaderThread().start();

            setConnected(true);

            logger.debug("Serial connection opened");
        } catch (PortInUseException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: Port in Use Exception: {}", e.getMessage(), e);
            throw new RotelException("Opening serial connection failed: Port in Use Exception");
        } catch (UnsupportedCommOperationException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: Unsupported Comm Operation Exception: {}", e.getMessage(),
                    e);
            throw new RotelException("Opening serial connection failed: Unsupported Comm Operation Exception");
        } catch (UnsupportedEncodingException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: Unsupported Encoding Exception: {}", e.getMessage(), e);
            throw new RotelException("Opening serial connection failed: Unsupported Encoding Exception");
        } catch (IOException e) {
            setConnected(false);
            logger.debug("Opening serial connection failed: IO Exception: {}", e.getMessage(), e);
            throw new RotelException("Opening serial connection failed: IO Exception");
        }
    }

    @Override
    public synchronized void close() {
        logger.debug("Closing serial connection");
        if (serialPort != null) {
            serialPort.removeEventListener();
        }
        super.cleanup();
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        setConnected(false);
        logger.debug("Serial connection closed");
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
            logger.debug("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }
}
