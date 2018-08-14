/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.transceiver;

import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.RXTXCommDriver;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanSerialTransceiver extends EnOceanTransceiver implements SerialPortEventListener {

    protected String path;
    SerialPort serialPort;
    private static final int ENOCEAN_DEFAULT_BAUD = 57600;

    private Logger logger = LoggerFactory.getLogger(EnOceanSerialTransceiver.class);

    public EnOceanSerialTransceiver(String path, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler) {
        super(errorListener, scheduler);
        this.path = path;
    }

    @Override
    public void Initialize() throws UnsupportedCommOperationException, NoSuchPortException, PortInUseException,
            IOException, TooManyListenersException {

        CommPort commPort;

        // There is currently a bug in nrjavaserial (https://github.com/NeuronRobotics/nrjavaserial/pull/121) so
        // directly use RXTXCommDriver on windows os
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
            RXTXCommDriver RXTXDriver = new RXTXCommDriver();
            RXTXDriver.initialize();
            commPort = RXTXDriver.getCommPort(path, CommPortIdentifier.PORT_SERIAL);
        } else {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(path);
            commPort = portIdentifier.open(this.getClass().getName(), 2000);
        }

        serialPort = (SerialPort) commPort;
        serialPort.setSerialPortParams(ENOCEAN_DEFAULT_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);
        serialPort.enableReceiveThreshold(1);
        serialPort.enableReceiveTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.

        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();

        logger.info("EnOceanSerialTransceiver initialized");
    }

    @Override
    public void ShutDown() {

        logger.debug("shutting down transceiver");
        super.ShutDown();

        if (outputStream != null) {
            logger.debug("Closing serial output stream");
            IOUtils.closeQuietly(outputStream);
        }
        if (inputStream != null) {
            logger.debug("Closeing serial input stream");
            IOUtils.closeQuietly(inputStream);
        }

        if (serialPort != null) {
            logger.debug("Closing serial port");
            serialPort.close();
        }

        serialPort = null;
        outputStream = null;
        inputStream = null;

        logger.info("Transceiver shutdown");

    }

    @Override
    public void serialEvent(SerialPortEvent event) {

        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

            synchronized (this) {
                this.notify();
            }
        }
    }

    @Override
    protected int read(byte[] buffer, int length) {
        try {
            return this.inputStream.read(buffer, 0, length);
        } catch (IOException e) {
            return 0;
        }
    }
}
