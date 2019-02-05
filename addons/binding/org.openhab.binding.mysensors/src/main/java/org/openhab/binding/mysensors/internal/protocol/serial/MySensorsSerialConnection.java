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
package org.openhab.binding.mysensors.internal.protocol.serial;

import java.io.IOException;
import java.util.TooManyListenersException;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.gateway.MySensorsGatewayConfig;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Connection to the serial interface where the MySensors Gateway is connected.
 *
 * @author Tim Oberföll
 * @author Andrea Cioni
 *
 */
public class MySensorsSerialConnection extends MySensorsAbstractConnection implements SerialPortEventListener {

    private SerialPort serialConnection = null;

    public MySensorsSerialConnection(MySensorsGatewayConfig myConf, MySensorsEventRegister myEventRegister) {
        super(myConf, myEventRegister);
    }

    /**
     * Tries to accomplish a connection via a serial port to the serial gateway.
     */
    @Override
    public boolean establishConnection() {
        logger.debug("Connecting to {} [baudRate:{}]", myGatewayConfig.getSerialPort(), myGatewayConfig.getBaudRate());

        CommPortIdentifier portIdentifier;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(myGatewayConfig.getSerialPort());

            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            serialConnection = (SerialPort) commPort;
            serialConnection.setSerialPortParams(myGatewayConfig.getBaudRate(), SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialConnection.enableReceiveThreshold(1);
            serialConnection.enableReceiveTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            try {
                serialConnection.addEventListener(this);
                serialConnection.notifyOnDataAvailable(true);
                logger.debug("Serial port event listener started");
            } catch (TooManyListenersException e) {
            }

            logger.debug("Successfully connected to serial port.");

            mysConReader = new MySensorsReader(serialConnection.getInputStream());
            mysConWriter = new MySensorsWriter(serialConnection.getOutputStream());

            return startReaderWriterThread(mysConReader, mysConWriter);
        } catch (NoSuchPortException e) {
            logger.error("No such port: {}", myGatewayConfig.getSerialPort(), e);
        } catch (PortInUseException e) {
            logger.error("Port: {} is already in use", myGatewayConfig.getSerialPort(), e);
        } catch (UnsupportedCommOperationException e) {
            logger.error("Comm operation on port: {} not supported", myGatewayConfig.getSerialPort(), e);
        } catch (IOException e) {
            logger.error("IOException on port: {}", myGatewayConfig.getSerialPort(), e);
        }
        return false;
    }

    /**
     * Initiates a clean disconnect from the serial gateway.
     */
    @Override
    public void stopConnection() {
        logger.debug("Shutting down serial connection!");

        if (mysConWriter != null) {
            mysConWriter.stopWriting();
            mysConWriter = null;
        }

        if (mysConReader != null) {
            mysConReader.stopReader();
            mysConReader = null;
        }

        if (myGatewayConfig.isHardReset()) {
            resetAttachedGateway();
        }

        if (serialConnection != null) {
            try {
                serialConnection.removeEventListener();
                serialConnection.close();
            } catch (Exception e) {
            }
            serialConnection = null;
        }
    }

    @Override
    public void serialEvent(SerialPortEvent arg0) {
        try {
            /*
             * See more details from
             * https://github.com/NeuronRobotics/nrjavaserial/issues/22
             */
            logger.trace("RXTX library CPU load workaround, sleep forever");
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            logger.warn("RXTX library CPU load workaround, sleep forever", e);
        }
    }

    /**
     * Try to reset the attached gateway by using DTR
     *
     */
    public void resetAttachedGateway() {
        logger.debug("Trying to reset of attached gateway with DTR");
        serialConnection.setDTR(true);
        try {
            Thread.sleep(RESET_TIME_IN_MILLISECONDS);
        } catch (InterruptedException e) {
            logger.warn("Wait for reset of attached gateway interrupted!", e);
        }
        serialConnection.setDTR(false);
        logger.debug("Finished reset of attached gateway with DTR");
    }

    @Override
    public String toString() {
        return "MySensorsSerialConnection [serialPort=" + myGatewayConfig.getSerialPort() + ", baudRate="
                + myGatewayConfig.getBaudRate() + "]";
    }
}
