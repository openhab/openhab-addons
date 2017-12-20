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
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * RS232 connection to the RadioRA Classic system.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class RS232Connection implements RadioRAConnection, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RS232Connection.class);

    protected SerialPort serialPort;

    protected BufferedReader inputReader;

    protected RadioRAFeedbackListener listener;
    protected RS232MessageParser parser = new RS232MessageParser();

    @Override
    public void open(String portName, int baud) throws RadioRAConnectionException {
        CommPortIdentifier commPort = null;

        try {
            commPort = CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException e) {
            logAvailablePorts();
            throw new RadioRAConnectionException(String.format("Port not found", portName));
        }

        try {
            serialPort = commPort.open("openhab", 5000);
            serialPort.notifyOnDataAvailable(true);
            serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.addEventListener(this);
            inputReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        } catch (PortInUseException e) {
            throw new RadioRAConnectionException(String.format("Port %s already in use", commPort.getName()));
        } catch (UnsupportedCommOperationException e) {
            throw new RadioRAConnectionException("Error initializing - Failed to set serial port params");
        } catch (TooManyListenersException e) {
            throw new RadioRAConnectionException("Error initializing - Failed to add event listener");
        } catch (IOException e) {
            throw new RadioRAConnectionException("Error initializing - Failed to get input stream");
        }
    }

    @Override
    public void write(String command) {
        logger.debug("Writing to serial port: {}", command.toString());
        try {
            serialPort.getOutputStream().write(command.getBytes());
        } catch (IOException e) {
            logger.debug("An error occurred writing to serial port", e);
        }
    }

    @Override
    public void disconnect() {
        serialPort.close();
    }

    private void logAvailablePorts() {
        if (logger.isDebugEnabled()) {
            logger.debug("Available ports:");
            for (CommPortIdentifier port : getAvailableSerialPorts()) {
                logger.debug("{}", port.getName());
            }
        }
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
                        logger.debug("Serial Data Available but input reader not ready");
                        return;
                    }

                    String message = inputReader.readLine();
                    logger.debug("Msg Received: {}", message);
                    RadioRAFeedback feedback = parser.parse(message);

                    if (feedback != null) {
                        logger.debug("Msg Parsed as {}", feedback.getClass().getName());
                        listener.handleRadioRAFeedback(feedback);
                    }
                    logger.debug("Finished handling feedback");
                } catch (IOException e) {
                    logger.debug("IOException occurred", e);
                }
                break;
            default:
                logger.debug("Unhandled SerialPortEvent raised [{}]", ev.getEventType());
                break;
        }
    }

    @Override
    public void setListener(RadioRAFeedbackListener listener) {
        this.listener = listener;
    }

}
