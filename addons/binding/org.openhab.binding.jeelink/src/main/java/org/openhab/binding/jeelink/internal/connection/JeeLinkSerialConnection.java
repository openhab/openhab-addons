/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.internal.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.TooManyListenersException;

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
 * Reads lines from serial port and propagates them to registered InputListeners.
 *
 * @author Volker Bier - Initial contribution
 */
public class JeeLinkSerialConnection extends AbstractJeeLinkConnection {
    private final Logger logger = LoggerFactory.getLogger(JeeLinkSerialConnection.class);

    private int baudRate;

    private SerialPort serialPort;
    private boolean open;

    public JeeLinkSerialConnection(String portName, int baudRate, ConnectionListener l) {
        super(portName, l);

        logger.debug("Creating serial connection for port {} with baud rate {}...", portName, baudRate);
        this.baudRate = baudRate;
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

                CommPortIdentifier portIdentifier;

                portIdentifier = CommPortIdentifier.getPortIdentifier(port);
                serialPort = portIdentifier.open("openhab", 3000);
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
        } catch (NoSuchPortException ex) {
            notifyAbort("Port not found: " + port);
        } catch (PortInUseException ex) {
            notifyAbort("Port in use: " + port);
        }
    }

    @Override
    public OutputStream getInitStream() throws IOException {
        return open ? serialPort.getOutputStream() : null;
    }
}
