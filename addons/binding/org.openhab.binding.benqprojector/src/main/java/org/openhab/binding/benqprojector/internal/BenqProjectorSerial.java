/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.benqprojector.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * The {@link BenqProjectorSerial} is responsible for handling the communication over a serial line.
 *
 * @author René Treffer - Initial contribution
 */
public class BenqProjectorSerial implements BenqProjectorSerialInterface, SerialPortEventListener {

    private final Logger logger = LoggerFactory.getLogger(BenqProjectorSerial.class);

    private volatile SerialPort serialPort;
    private volatile OutputStream out;
    private volatile InputStream in;

    private final StringBuilder buffer = new StringBuilder();

    private final ArrayBlockingQueue<String> serialInput = new ArrayBlockingQueue<>(1);

    private final ReentrantLock lock = new ReentrantLock();

    private final String port;
    private final int speed;

    public BenqProjectorSerial(String port, int speed) {
        this.port = port;
        this.speed = speed;
    }

    @Override
    public boolean check() {
        lock.lock();
        try {
            getSerial();
            while (serialInput.poll() != null) {
            }
            for (int i = 0; i < 2; i++) {
                logger.debug("sending projector '\\r'");
                out.write("\r".getBytes());
                out.flush();
                try {
                    if (">".equals(serialInput.poll(1000, TimeUnit.MILLISECONDS))) {
                        return true;
                    }
                } catch (InterruptedException e) {
                    logger.debug("error during check", e);
                }
            }
        } catch (IOException e) {
            logger.debug("error during check", e);
        } finally {
            lock.unlock();
        }
        return false;
    }

    @Override
    public void close() {
        if (serialPort != null) {
            serialPort.close();
        }
        lock.lock();
        try {
            if (serialPort != null) {
                serialPort.close();
                serialPort = null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Response get(String key) throws IOException {
        return put(key, "?");
    }

    @Override
    public Response put(String key, String value) throws IOException {
        lock.lock();
        try {
            if (!check()) {
                return new Response(false, "serial console error");
            }

            String query = "*" + key + "=" + value + "#";

            logger.debug("sending projector '" + query + "\\r'");
            out.write(query.getBytes());
            out.write('\r');
            out.flush();

            String response = serialInput.poll(1000, TimeUnit.MILLISECONDS);
            if (response == null || response.length() == 0 || ">".equals(response)) {
                return new Response(false, "protocol error / invalid response");
            }

            if ("*Block item#".equalsIgnoreCase(response)) {
                return new Response(false, "command failed (block item)");
            }
            if ("*Illegal format#".equalsIgnoreCase(response)) {
                return new Response(false, "command malformed (illegal format)");
            }

            if (query.equals(response)) {
                // the projector echoed back our query, wait for the next response
                response = serialInput.poll(1000, TimeUnit.MILLISECONDS);
            }

            if (response == null || response.length() == 0 || ">".equals(response)) {
                return new Response(false, "protocol error / invalid response");
            }

            if ("*Block item#".equalsIgnoreCase(response)) {
                return new Response(false, "command failed (block item)");
            }
            if ("*Illegal format#".equalsIgnoreCase(response)) {
                return new Response(false, "command malformed (illegal format)");
            }

            if (response.toLowerCase().startsWith(("*" + key + "=").toLowerCase())
                    && response.charAt(response.length() - 1) == '#') {
                String result = response.substring(key.length() + 2, response.length() - 1);
                logger.debug("success: query for " + key + " resulted in \"" + result + "\"");
                return new Response(true, result);
            } else {
                logger.debug("could not parse response: received \"" + response + "\"");
                return new Response(false, "unparsed response for key " + key + ": " + response);
            }
        } catch (InterruptedException e) {
            return new Response(false, "interruptes");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean reset() {
        close();
        return check();
    }

    private final SerialPort getSerial() throws IOException {
        lock.lock();
        try {
            if (serialPort != null) {
                return serialPort;
            }
            CommPortIdentifier portIdentifier;
            try {
                portIdentifier = CommPortIdentifier.getPortIdentifier(port);
            } catch (NoSuchPortException e) {
                throw new IOException("could not find serial port " + port, e);
            }
            CommPort commPort;
            try {
                commPort = portIdentifier.open(this.getClass().getName(), 1000);
            } catch (PortInUseException e) {
                throw new IOException("could not open serial port " + port + ": port already in use", e);
            }
            SerialPort serialPort = (SerialPort) commPort;

            try {
                serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.enableReceiveThreshold(1);
                serialPort.enableReceiveTimeout(250);
                in = serialPort.getInputStream();
                while (in.available() > 0) {
                    in.read();
                }
                serialPort.addEventListener(this);
                serialPort.notifyOnDataAvailable(true);
            } catch (UnsupportedCommOperationException | TooManyListenersException e) {
                logger.error("Could not configure serial port", e);
                serialPort.close();
                throw new IOException("could not configure serial console " + port, e);
            }

            out = serialPort.getOutputStream();
            this.serialPort = serialPort;
            return serialPort;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPortEvent.DATA_AVAILABLE) {
            return;
        }
        try {
            while (in.available() > 0) {
                char c = (char) in.read();
                logger.trace("projector send (" + (c & 0xFF) + ") '" + c + "'");
                if (c == '\r' || c == '\n' || c == '\f') {
                    if (buffer.length() == 0) {
                        continue;
                    }
                    String message = buffer.toString();
                    logger.debug("projector message: '" + message + "'");
                    if (!serialInput.offer(message)) {
                        logger.warn("Discarding projector message: " + message + "'");
                    }
                    buffer.setLength(0);
                    continue;
                }
                if (c == '>' && buffer.length() == 0) {
                    logger.debug("projector message: '>'");
                    if (!serialInput.offer(">")) {
                        logger.warn("Discarding projector message: '>'");
                    }
                    continue;
                }
                buffer.append(c);
                if (buffer.length() > 4096) {
                    logger.warn("discarding data: received more than 4kb of data, is the serial port " + port
                            + " correct?");
                    buffer.setLength(0);
                }
            }
        } catch (IOException e) {
            logger.warn("Error while reading from projector", e);
        }
    }

}
