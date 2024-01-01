/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.epsonprojector.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorException;
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
 * Connector for serial port communication.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public class EpsonProjectorSerialConnector implements EpsonProjectorConnector, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorSerialConnector.class);
    private final String serialPortName;
    private final SerialPortManager serialPortManager;

    private @Nullable InputStream in = null;
    private @Nullable OutputStream out = null;
    private @Nullable SerialPort serialPort = null;

    public EpsonProjectorSerialConnector(SerialPortManager serialPortManager, String serialPort) {
        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPort;
    }

    @Override
    public void connect() throws EpsonProjectorException {
        try {
            logger.debug("Open connection to serial port '{}'", serialPortName);

            SerialPortIdentifier serialPortIdentifier = serialPortManager.getIdentifier(serialPortName);

            if (serialPortIdentifier == null) {
                throw new IOException("Unknown serial port");
            }
            SerialPort serialPort = serialPortIdentifier.open(this.getClass().getName(), 2000);
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.enableReceiveThreshold(1);
            serialPort.disableReceiveTimeout();

            InputStream in = serialPort.getInputStream();
            OutputStream out = serialPort.getOutputStream();

            if (in != null && out != null) {
                out.flush();
                if (in.markSupported()) {
                    in.reset();
                }

                serialPort.notifyOnDataAvailable(true);

                this.serialPort = serialPort;
                this.in = in;
                this.out = out;
            }
        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            throw new EpsonProjectorException(e);
        }
    }

    @Override
    public void disconnect() throws EpsonProjectorException {
        InputStream in = this.in;
        OutputStream out = this.out;
        SerialPort serialPort = this.serialPort;

        if (out != null) {
            logger.debug("Close serial out stream");
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("Error occurred when closing serial out stream: {}", e.getMessage());
            }
            this.out = null;
        }
        if (in != null) {
            logger.debug("Close serial in stream");
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("Error occurred when closing serial in stream: {}", e.getMessage());
            }
            this.in = null;
        }
        if (serialPort != null) {
            logger.debug("Close serial port");
            serialPort.close();
            serialPort.removeEventListener();
            this.serialPort = null;
        }

        logger.debug("Closed");
    }

    @Override
    public String sendMessage(String data, int timeout) throws EpsonProjectorException {
        InputStream in = this.in;
        OutputStream out = this.out;

        if (in == null || out == null) {
            connect();
            in = this.in;
            out = this.out;
        }

        try {
            if (in != null && out != null) {
                // flush input stream
                if (in.markSupported()) {
                    in.reset();
                } else {
                    while (in.available() > 0) {
                        int availableBytes = in.available();

                        if (availableBytes > 0) {
                            byte[] tmpData = new byte[availableBytes];
                            in.read(tmpData, 0, availableBytes);
                        }
                    }
                }
                return sendMmsg(data, timeout);
            } else {
                return "";
            }
        } catch (IOException e) {
            logger.debug("IO error occurred...reconnect and resend once: {}", e.getMessage());
            disconnect();
            connect();

            try {
                return sendMmsg(data, timeout);
            } catch (IOException e1) {
                throw new EpsonProjectorException(e);
            }
        }
    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
    }

    private String sendMmsg(String data, int timeout) throws IOException, EpsonProjectorException {
        String resp = "";

        InputStream in = this.in;
        OutputStream out = this.out;

        if (in != null && out != null) {
            out.write(data.getBytes(StandardCharsets.US_ASCII));
            out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0;

            while (elapsedTime < timeout) {
                int availableBytes = in.available();
                if (availableBytes > 0) {
                    byte[] tmpData = new byte[availableBytes];
                    int readBytes = in.read(tmpData, 0, availableBytes);
                    resp = resp.concat(new String(tmpData, 0, readBytes, StandardCharsets.US_ASCII));

                    if (resp.contains(":")) {
                        return resp;
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new EpsonProjectorException(e);
                    }
                }

                elapsedTime = System.currentTimeMillis() - startTime;
            }
        }

        return resp;
    }
}
