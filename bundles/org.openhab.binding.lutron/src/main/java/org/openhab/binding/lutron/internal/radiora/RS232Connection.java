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
package org.openhab.binding.lutron.internal.radiora;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RS232 connection to the RadioRA Classic system.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
public class RS232Connection implements RadioRAConnection, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RS232Connection.class);

    protected SerialPortManager serialPortManager;
    protected SerialPort serialPort;

    protected BufferedReader inputReader;

    protected RadioRAFeedbackListener listener;
    protected RS232MessageParser parser = new RS232MessageParser();

    public RS232Connection(SerialPortManager serialPortManager) {
        super();
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void open(String portName, int baud) throws RadioRAConnectionException {
        SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(portName);
        if (portIdentifier == null) {
            throw new RadioRAConnectionException(String.format("Port not found", portName));
        }

        try {
            serialPort = portIdentifier.open("openhab", 5000);
            serialPort.notifyOnDataAvailable(true);
            serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.addEventListener(this);
            inputReader = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        } catch (PortInUseException e) {
            throw new RadioRAConnectionException(String.format("Port %s already in use", portIdentifier.getName()));
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
