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
package org.openhab.binding.jeelink.internal.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads lines from serial port and propagates them to registered InputListeners.
 *
 * @author Volker Bier - Initial contribution
 */
public class JeeLinkSerialConnection extends AbstractJeeLinkConnection {
    private final Logger logger = LoggerFactory.getLogger(JeeLinkSerialConnection.class);

    private final int baudRate;
    private SerialPort serialPort;
    private final SerialPortIdentifier serialPortIdentifier;
    private boolean open;

    public JeeLinkSerialConnection(SerialPortIdentifier serialPortIdentifier, int baudRate,
            ConnectionListener listener) {
        super(serialPortIdentifier.getName(), listener);

        logger.debug("Creating serial connection for port {} with baud rate {}...", port, baudRate);
        this.baudRate = baudRate;
        this.serialPortIdentifier = serialPortIdentifier;
    }

    @Override
    public synchronized void closeConnection() {
        if (open) {
            logger.debug("Closing serial connection to port {}...", port);

            serialPort.notifyOnDataAvailable(false);
            serialPort.removeEventListener();

            serialPort.close();
            notifyClosed();
            open = false;
        }
    }

    @Override
    public synchronized void openConnection() {
        try {
            if (!open) {
                logger.debug("Opening serial connection to port {} with baud rate {}...", port, baudRate);
                serialPort = serialPortIdentifier.open("openhab", 3000);
                open = true;

                serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                final BufferedReader input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));

                serialPort.addEventListener(new SerialPortEventListener() {
                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        try {
                            if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                                propagateLine(input.readLine());
                            }
                        } catch (IOException ex) {
                            logger.debug("Error reading from serial port!", ex);
                            closeConnection();
                            notifyAbort("propagate: " + ex.getMessage());
                        }
                    }
                });

                serialPort.notifyOnDataAvailable(true);
                notifyOpen();
            }
        } catch (UnsupportedCommOperationException | IOException | TooManyListenersException ex) {
            closeConnection();
            notifyAbort(ex.getMessage());
        } catch (PortInUseException ex) {
            notifyAbort("Port in use: " + port);
        }
    }

    @Override
    public OutputStream getInitStream() throws IOException {
        return open ? serialPort.getOutputStream() : null;
    }
}
