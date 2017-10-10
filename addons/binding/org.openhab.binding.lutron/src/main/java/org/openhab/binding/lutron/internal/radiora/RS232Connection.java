/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.radiora;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.TooManyListenersException;

import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * RS232 connection to the RadioRA Classic system.
 * 
 * @author Jeff Lauterbach
 *
 */
public class RS232Connection implements RadioRAConnection, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RS232Connection.class);

    protected SerialPort serialPort;

    protected BufferedReader inputReader;

    protected RadioRAFeedbackListener listener;
    protected RS232MessageParser parser = new RS232MessageParser();

    @Override
    public boolean open(String portName, int baud) {
        CommPortIdentifier commPort = findSerialPort(portName);

        if (commPort == null) {
            logger.error("Failed to find port {}", portName);
            logAvailablePorts();
            return false;
        }

        try {
            serialPort = commPort.open("openhab", 5000);
        } catch (PortInUseException e) {
            logger.error("Port {} is already in use", commPort.getName());
            return false;
        }

        serialPort.notifyOnDataAvailable(true);
        try {
            serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {
            logger.error("Error initializing - Failed to set serial port params");
            return false;
        }

        try {
            serialPort.addEventListener(this);
        } catch (TooManyListenersException e) {
            logger.error("Error initializing - Failed to add event listener");
            return false;
        }

        try {
            inputReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        } catch (IOException e) {
            logger.error("Error initializing - Failed to get input stream");
            return false;
        }

        return true;
    }

    @Override
    public void write(String command) {
        logger.debug("Writing to serial port: {}", command.toString());
        try {
            serialPort.getOutputStream().write(command.getBytes());
        } catch (IOException e) {
            logger.error("An error occurred writing to serial port", e);
        }
    }

    @Override
    public void disconnect() {
        serialPort.close();
    }

    private void logAvailablePorts() {
        logger.debug("Available ports:");
        for (CommPortIdentifier port : getAvailableSerialPorts()) {
            logger.debug("{}", port.getName());
        }
    }

    protected CommPortIdentifier findSerialPort(String portName) {
        for (CommPortIdentifier port : getAvailableSerialPorts()) {
            if (port.getName().equals(portName)) {
                return port;
            }
        }

        return null;
    }

    protected List<CommPortIdentifier> getAvailableSerialPorts() {
        List<CommPortIdentifier> ports = new ArrayList<>();
        Enumeration<?> portIds = CommPortIdentifier.getPortIdentifiers();
        while (portIds.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portIds.nextElement();
            if (CommPortIdentifier.PORT_SERIAL == id.getPortType()) {
                ports.add(id);
            }
        }

        return ports;
    }

    @Override
    public void serialEvent(SerialPortEvent ev) {
        switch (ev.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                try {
                    if (!inputReader.ready()) {
                        logger.warn("Serial Data Available but input reader not ready");
                        return;
                    }

                    String message = inputReader.readLine();
                    logger.debug("Msg Received: {}", message);
                    RadioRAFeedback feedback = parser.parse(message);

                    if (feedback != null) {
                        logger.debug("Msg Parsed as {}", feedback.getClass().getName());
                        listener.handleRadioRAFeedback(feedback);
                    }
                } catch (IOException e) {
                    logger.error("IOException occurred", e);
                    return;
                }
                logger.debug("Finished handling feedback");
                break;
            default:
                logger.warn("Unhandled SerialPortEvent raised [{}]", ev.getEventType());
                break;
        }
    }

    @Override
    public void setListener(RadioRAFeedbackListener listener) {
        this.listener = listener;
    }

}
