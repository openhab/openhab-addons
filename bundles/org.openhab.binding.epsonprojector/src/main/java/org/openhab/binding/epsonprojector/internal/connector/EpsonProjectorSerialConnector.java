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
package org.openhab.binding.epsonprojector.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.apache.commons.io.IOUtils;
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
 */
public class EpsonProjectorSerialConnector implements EpsonProjectorConnector, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorSerialConnector.class);

    private final String serialPortName;
    private InputStream in = null;
    private OutputStream out = null;
    private SerialPortManager serialPortManager = null;
    private SerialPort serialPort = null;

    public EpsonProjectorSerialConnector(SerialPortManager serialPortManager, String serialPort) {
        this.serialPortManager = serialPortManager;
        this.serialPortName = serialPort;
    }

    @Override
    public void connect() throws EpsonProjectorException {
        try {
            logger.debug("Open connection to serial port '{}'", serialPortName);
            if (serialPortManager == null) {
                throw new IllegalStateException("The SerialPortManager has not been not initialized");
            }

            SerialPortIdentifier serialPortIdentifier = serialPortManager.getIdentifier(serialPortName);

            if (serialPortIdentifier == null) {
                throw new IOException("Unknown serial port");
            }
            serialPort = serialPortIdentifier.open(this.getClass().getName(), 2000);
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.enableReceiveThreshold(1);
            serialPort.disableReceiveTimeout();

            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();

            out.flush();
            if (in.markSupported()) {
                in.reset();
            }

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (PortInUseException | UnsupportedCommOperationException | IOException | TooManyListenersException e) {
            throw new EpsonProjectorException(e);
        }
    }

    @Override
    public void disconnect() throws EpsonProjectorException {
        if (out != null) {
            logger.debug("Close serial out stream");
            IOUtils.closeQuietly(out);
            out = null;
        }
        if (in != null) {
            logger.debug("Close serial in stream");
            IOUtils.closeQuietly(in);
            in = null;
        }
        if (serialPort != null) {
            logger.debug("Close serial port");
            serialPort.close();
            serialPort.removeEventListener();
            serialPort = null;
        }

        logger.debug("Closed");
    }

    @Override
    public String sendMessage(String data, int timeout) throws EpsonProjectorException {
        if (in == null || out == null) {
            connect();
        }

        try {
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
        } catch (IOException e) {
            logger.debug("IO error occurred...reconnect and resend ones");
            disconnect();
            connect();

            try {
                return sendMmsg(data, timeout);
            } catch (IOException e1) {
                throw new EpsonProjectorException(e);
            }
        } catch (Exception e) {
            throw new EpsonProjectorException(e);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
        try {
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
        }
    }

    private String sendMmsg(String data, int timeout) throws IOException, EpsonProjectorException {
        out.write(data.getBytes());
        out.write("\r\n".getBytes());
        out.flush();

        String resp = "";

        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;

        while (elapsedTime < timeout) {
            int availableBytes = in.available();
            if (availableBytes > 0) {
                byte[] tmpData = new byte[availableBytes];
                int readBytes = in.read(tmpData, 0, availableBytes);
                resp = resp.concat(new String(tmpData, 0, readBytes));

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

        return resp;
    }
}
