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
package org.openhab.binding.pentair.internal.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import org.openhab.binding.pentair.internal.config.PentairSerialBridgeConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * Handler for the IPBridge. Implements the connect and disconnect abstract methods of {@link PentairBaseBridgeHandler}
 *
 * @author Jeff James - initial contribution
 *
 */
public class PentairSerialBridgeHandler extends PentairBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairSerialBridgeHandler.class);

    /** SerialPort object representing the port where the RS485 adapter is connected */
    SerialPort port;

    public PentairSerialBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    protected synchronized void connect() {
        PentairSerialBridgeConfig configuration = getConfigAs(PentairSerialBridgeConfig.class);

        try {
            CommPortIdentifier ci = CommPortIdentifier.getPortIdentifier(configuration.serialPort);
            CommPort cp = ci.open("openhabpentairbridge", 10000);
            if (cp == null) {
                throw new IllegalStateException("cannot open serial port!");
            }

            if (cp instanceof SerialPort serialPort) {
                port = serialPort;
            } else {
                throw new IllegalStateException("unknown port type");
            }
            port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.disableReceiveFraming();
            port.disableReceiveThreshold();

            reader = new BufferedInputStream(port.getInputStream());
            writer = new BufferedOutputStream(port.getOutputStream());
            logger.info("Pentair Bridge connected to serial port: {}", configuration.serialPort);
        } catch (PortInUseException e) {
            String msg = String.format("cannot open serial port: %s", configuration.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        } catch (UnsupportedCommOperationException e) {
            String msg = String.format("got unsupported operation %s on port %s", e.getMessage(),
                    configuration.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        } catch (NoSuchPortException e) {
            String msg = String.format("got no such port for %s", configuration.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        } catch (IllegalStateException e) {
            String msg = String.format("receive IllegalStateException for port %s", configuration.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        } catch (IOException e) {
            String msg = String.format("IOException on port %s", configuration.serialPort);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        parser = new Parser();
        thread = new Thread(parser);
        thread.start();

        if (port != null && reader != null && writer != null) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to connect");
        }
    }

    @Override
    protected synchronized void disconnect() {
        updateStatus(ThingStatus.OFFLINE);

        if (thread != null) {
            try {
                thread.interrupt();
                thread.join(); // wait for thread to complete
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
                logger.trace("IOException when closing serial reader", e);
            }
            reader = null;
        }

        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                logger.trace("IOException when closing serial writer", e);
            }
            writer = null;
        }

        if (port != null) {
            port.close();
            port = null;
        }
    }
}
