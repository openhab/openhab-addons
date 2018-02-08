/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.exception.PortNotConnected;
import org.openhab.binding.openwebnet.internal.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/**
 * The {@link SerialGateway} class manages the connection to a serial gateway (mainly used for Zigbee)
 *
 * @author Antoine Laydier
 *
 */
@NonNullByDefault
class SerialGateway extends InternalGateway implements SerialPortEventListener, Runnable {

    private static final int BAUD_RATE = 19200; // Baud rate as per documentation

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(SerialGateway.class);
    @SuppressWarnings("null")
    private final ByteBuffer inputBuffer = ByteBuffer.allocate(200);;

    private final NRSerialPort serial;
    private final String portName;
    private final Thread readerThead;

    public SerialGateway(Parser parser, String serialPortName) {
        super(parser);
        this.serial = new NRSerialPort(serialPortName, BAUD_RATE);
        this.portName = serialPortName;
        readerThead = new Thread(this);
    }

    @Override
    public void connect() {
        serial.connect();
        try {
            serial.addEventListener(this);
            readerThead.start();
        } catch (TooManyListenersException e) {
            // if this happens, the registration have already been done.
        }
        notifyConnected();
    }

    @Override
    public void write(@Nullable String data) throws IOException {
        if (data == null) {
            logger.warn("Try to write an 'null' message");
            return;
        }
        if (serial.isConnected()) {
            logger.debug("Writing \"{}\" on serial port {}", data, portName);
            serial.getOutputStream().write(data.getBytes());
        } else {
            logger.warn("Write attempt of \"{}\" failed to to unconnected serial port {}", data, portName);
            throw new PortNotConnected("Serial port " + this.toString() + " not connected. Write cannot be performed.");
        }
    }

    @Override
    public void close() {
        if (serial.isConnected()) {
            serial.disconnect();
            readerThead.interrupt();
            notifyDisconnected();
        }
    }

    /* SerialPortEventListener */
    @Override
    public void serialEvent(@Nullable SerialPortEvent arg0) {
        try {
            /*
             * See more details from
             * https://github.com/NeuronRobotics/nrjavaserial/issues/22
             */
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public String toString() {
        return "Serial gateway on " + portName;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Serial Reader Thread");
        InputStream evStream = serial.getInputStream();

        while (!Thread.interrupted()) {
            try {
                inputBuffer.clear();
                int position = evStream.read(inputBuffer.array(), inputBuffer.position(), inputBuffer.remaining());
                if (position > 0) {
                    inputBuffer.position(position);
                    inputBuffer.flip();
                    logger.debug("<{}> received.", inputBuffer.toString());
                    getParser().checkInput(evStream);
                    getParser().parse(inputBuffer);
                }
            } catch (IOException e) {
                // FIXME: silent fail for now.
                logger.warn("read failure ({})", e.getLocalizedMessage());
            }
        }
        logger.debug("Reader thread ended.");

    }
}
