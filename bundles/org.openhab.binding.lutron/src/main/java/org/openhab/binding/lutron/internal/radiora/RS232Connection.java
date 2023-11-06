/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lutron.internal.radiora.protocol.RadioRAFeedback;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RS232 connection to the RadioRA Classic system.
 *
 * @author Jeff Lauterbach - Initial Contribution
 *
 */
@NonNullByDefault
public class RS232Connection implements RadioRAConnection, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(RS232Connection.class);

    protected SerialPortManager serialPortManager;
    protected @Nullable SerialPort serialPort;

    protected @Nullable BufferedReader inputReader;

    protected @Nullable RadioRAFeedbackListener listener;
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
            SerialPort serialPort = portIdentifier.open("openhab", 5000);
            this.serialPort = serialPort;
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
        logger.debug("Writing to serial port: {}", command);
        SerialPort serialPort = this.serialPort;

        try {
            if (serialPort != null) {
                OutputStream outputStream = serialPort.getOutputStream();
                if (outputStream != null) {
                    outputStream.write(command.getBytes());
                } else {
                    logger.debug("Cannot write to serial port. outputStream is null.");
                }
            } else {
                logger.debug("Cannot write to serial port. serialPort is null.");
            }
        } catch (IOException e) {
            logger.debug("An error occurred writing to serial port", e);
        }
    }

    @Override
    public void disconnect() {
        SerialPort serialPort = this.serialPort;
        if (serialPort != null) {
            serialPort.close();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent ev) {
        switch (ev.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                BufferedReader inputReader = this.inputReader;
                try {
                    if (inputReader == null || !inputReader.ready()) {
                        logger.debug("Serial Data Available but input reader not ready");
                        return;
                    }

                    String message = inputReader.readLine();
                    logger.debug("Msg Received: {}", message);
                    RadioRAFeedback feedback = parser.parse(message);

                    if (feedback != null) {
                        logger.debug("Msg Parsed as {}", feedback.getClass().getName());
                        RadioRAFeedbackListener listener = this.listener;
                        if (listener != null) {
                            listener.handleRadioRAFeedback(feedback);
                        } else {
                            logger.debug("Cannot handle feedback message. Listener is null.");
                        }
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
