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
package org.openhab.binding.enocean.internal.transceiver;

import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortEvent;
import org.eclipse.smarthome.io.transport.serial.SerialPortEventListener;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanSerialTransceiver extends EnOceanTransceiver implements SerialPortEventListener {

    protected String path;
    SerialPort serialPort;

    private static final int ENOCEAN_DEFAULT_BAUD = 57600;

    private Logger logger = LoggerFactory.getLogger(EnOceanSerialTransceiver.class);
    private SerialPortManager serialPortManager;

    public EnOceanSerialTransceiver(String path, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler, SerialPortManager serialPortManager) {
        super(errorListener, scheduler);
        this.path = path;
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void Initialize()
            throws UnsupportedCommOperationException, PortInUseException, IOException, TooManyListenersException {

        SerialPortIdentifier id = serialPortManager.getIdentifier(path);
        if (id == null) {
            throw new IOException("Could not find a gateway on given path '" + path + "', "
                    + serialPortManager.getIdentifiers().count() + " ports available.");
        }

        serialPort = id.open(EnOceanBindingConstants.BINDING_ID, 1000);
        serialPort.setSerialPortParams(ENOCEAN_DEFAULT_BAUD, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        try {
            serialPort.enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.
        } catch (UnsupportedCommOperationException e) {
            // rfc connections do not allow a ReceiveThreshold
        }

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
