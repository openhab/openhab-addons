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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TooManyListenersException;

import org.apache.commons.lang.ArrayUtils;
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
 * @author Volker Bier - Initial contribution
 */
public class SerialConnection {
    private final Logger logger = LoggerFactory.getLogger(SerialConnection.class);

    private SerialPort serialPort;
    private boolean open;
    private String portName;
    private final ArrayList<Byte> bytes = new ArrayList<>();
    private Response response = null;

    public SerialConnection(String portName) {
        this.portName = portName;
    }

    public synchronized void open() throws ConnectException {
        try {
            if (!open) {
                logger.debug("Trying to open serial connection to port {}...", portName);

                CommPortIdentifier portIdentifier;

                try {
                    portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
                    serialPort = portIdentifier.open("openhab", 3000);
                    open = true;
                    logger.debug("Serial connection to port {} opened.", portName);

                    serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);

                    serialPort.addEventListener(new SerialPortEventListener() {
                        @Override
                        public void serialEvent(SerialPortEvent event) {
                            try {
                                if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
                                    parseInput();
                                }
                            } catch (IOException ex) {
                                logger.warn("IOException reading from port {}!", portName);
                            }
                        }
                    });

                    serialPort.notifyOnDataAvailable(true);
                } catch (UnsupportedCommOperationException | TooManyListenersException ex) {
                    close();
                    throw new ConnectException(ex);
                }
            }
        } catch (NoSuchPortException | PortInUseException ex) {
            throw new ConnectException(ex);
        }
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized void close() {
        if (open) {
            logger.debug("Closing serial connection to port {}...", portName);

            serialPort.notifyOnDataAvailable(false);
            serialPort.removeEventListener();
            serialPort.close();
            open = false;
        }
    }

    // send a packet to the stick and wait for the response
    public synchronized Response sendPacket(CommandPacket p) throws IOException {
        if (open) {
            Response r = response;

            synchronized (bytes) {
                response = null;
                logger.debug("Writing packet to stick: {}", p);
                serialPort.getOutputStream().write(p.getBytes());

                if (r != null) {
                    return r;
                }

                final long responseTimeout = p.getResponseTimeout();
                try {
                    logger.trace("Waiting {} ms for answer from stick...", responseTimeout);
                    bytes.wait(responseTimeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                r = response;
                response = null;
            }

            logger.debug("Stick answered {} for packet {}.", r, p);
            return r;
        }

        logger.warn("Stick skipped packet {}. Connection is not open.", p);
        return null;
    }

    private void parseInput() throws IOException {
        logger.trace("parsing input...");
        while (serialPort.getInputStream().available() > 0) {
            byte b = (byte) serialPort.getInputStream().read();
            bytes.add(b);
        }
        logger.trace("input parsed. buffer contains {} bytes.", bytes.size());
        analyzeBuffer();
    }

    private void analyzeBuffer() {
        // drop everything before the beginning of the packet header 0xAA
        while (!bytes.isEmpty() && bytes.get(0) != (byte) 0xAA) {
            logger.trace("dropping byte {} from buffer", bytes.get(0));
            bytes.remove(0);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("buffer contains {} bytes: {}", bytes.size(),
                    ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()])));
        }
        if (bytes.size() > 1) {
            // second byte should be length byte (has to be either 0x04 or 0x05)
            int len = bytes.get(1);
            logger.trace("packet length is {} bytes.", len);

            if (len != 4 && len != 5) {
                // invalid length, drop packet
                bytes.remove(0);
                analyzeBuffer();
            } else if (bytes.size() > len + 1) {
                // we have a complete packet in the buffer, analyze it
                // third byte should be response type byte (has to be either EASY_CONFIRM or EASY_ACK)
                byte respType = bytes.get(2);

                synchronized (bytes) {
                    if (respType == ResponseStatus.EASY_CONFIRM) {
                        logger.trace("response type is EASY_CONFIRM.");

                        long val = bytes.get(0) + bytes.get(1) + bytes.get(2) + bytes.get(3) + bytes.get(4)
                                + bytes.get(5);
                        if (val % 256 == 0) {
                            response = ResponseUtil.createResponse(bytes.get(3), bytes.get(4));
                        } else {
                            logger.warn("invalid response checksum. Skipping response.");
                        }

                        bytes.notify();
                    } else if (respType == ResponseStatus.EASY_ACK) {
                        logger.trace("response type is EASY_ACK.");

                        long val = bytes.get(0) + bytes.get(1) + bytes.get(2) + bytes.get(3) + bytes.get(4)
                                + bytes.get(5) + bytes.get(6);
                        if (val % 256 == 0) {
                            response = ResponseUtil.createResponse(bytes.get(3), bytes.get(4), bytes.get(5));
                        } else {
                            logger.warn("invalid response checksum. Skipping response.");
                        }

                        bytes.notify();
                    } else {
                        logger.warn("invalid response type {}. Skipping response.", respType);
                    }
                }

                bytes.remove(0);
                analyzeBuffer();
            }
        }
    }

}
