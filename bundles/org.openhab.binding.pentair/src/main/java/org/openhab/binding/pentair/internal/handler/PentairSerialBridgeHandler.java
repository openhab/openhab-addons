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
package org.openhab.binding.pentair.internal.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.pentair.internal.config.PentairSerialBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for the IPBridge. Implements the connect and disconnect abstract methods of {@link PentairBaseBridgeHandler}
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairSerialBridgeHandler extends PentairBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairSerialBridgeHandler.class);

    public PentairSerialBridgeConfig config = new PentairSerialBridgeConfig();
    /** SerialPort object representing the port where the RS485 adapter is connected */
    private final SerialPortManager serialPortManager;
    private @Nullable SerialPort serialPort;
    private @NonNullByDefault({}) SerialPortIdentifier portIdentifier;

    public PentairSerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    protected synchronized int connect() {
        logger.debug("PentairSerialBridgeHander: connect");
        config = getConfigAs(PentairSerialBridgeConfig.class);

        if (config.serialPort.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "no serial port configured");
            return -1;
        }

        this.id = config.id;
        logger.debug("Serial port id: {}", id);
        this.discovery = config.discovery;

        portIdentifier = serialPortManager.getIdentifier(config.serialPort);
        if (portIdentifier == null) {
            logger.debug("Serial Error: Port {} does not exist.", config.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Configured serial port does not exist");
            return -1;
        }

        try {
            logger.debug("connect port: {}", config.serialPort);

            SerialPort serialPort = portIdentifier.open("org.openhab.binding.pentair", 10000);
            serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            // Note: The V1 code called disableReceiveFraming() and disableReceiveThreshold() here
            // port.disableReceiveFraming();
            // port.disableReceiveThreshold();

            reader = new BufferedInputStream(serialPort.getInputStream());
            writer = new BufferedOutputStream(serialPort.getOutputStream());
        } catch (PortInUseException e) {
            String msg = String.format("cannot open serial port: %s", config.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return -1;
        } catch (UnsupportedCommOperationException e) {
            String msg = String.format("got unsupported operation %s on port %s", e.getMessage(), config.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return -2;
        } catch (IOException e) {
            String msg = String.format("got IOException %s on port %s", e.getMessage(), config.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return -2;
        }

        // if you have gotten this far, you should be connected to the serial port
        logger.info("Pentair Bridge connected to serial port: {}", config.serialPort);

        parser = new Parser();
        thread = new Thread(parser);
        thread.start();

        updateStatus(ThingStatus.ONLINE);

        return 0;
    }

    @Override
    protected synchronized void disconnect() {
        logger.debug("PentairSerialBridgeHandler: disconnect");
        updateStatus(ThingStatus.OFFLINE);

        if (thread != null) {
            try {
                thread.interrupt();
                if (thread != null) {
                    thread.join(); // wait for thread to complete
                }
            } catch (InterruptedException e) {
                // do nothing
            }
            thread = null;
            parser = null;
        }

        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                logger.trace("IOException when closing serial reader: {}", e.toString());
            }
            reader = null;
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.trace("IOException when closing serial writer: {}", e.toString());
            }
            writer = null;
        }

        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }
}
