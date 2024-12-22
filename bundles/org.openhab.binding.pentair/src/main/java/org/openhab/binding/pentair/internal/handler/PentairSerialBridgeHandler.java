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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.config.PentairSerialBridgeConfig;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentairSerialBridgeHandler } implments the class for the serial bridge. Implements the connect and
 * disconnect abstract methods of {@link PentairBaseBridgeHandler}
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairSerialBridgeHandler extends PentairBaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(PentairSerialBridgeHandler.class);

    public PentairSerialBridgeConfig config = new PentairSerialBridgeConfig();

    private final SerialPortManager serialPortManager;
    @Nullable
    private SerialPort port;
    @Nullable
    private SerialPortIdentifier portIdentifier;

    public PentairSerialBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    protected synchronized boolean connect() {
        config = getConfigAs(PentairSerialBridgeConfig.class);

        if (config.serialPort.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.serial-port-empty");
            return false;
        }

        this.portIdentifier = serialPortManager.getIdentifier(config.serialPort);
        SerialPortIdentifier portIdentifier = this.portIdentifier;
        if (portIdentifier == null) {
            if (getThing().getStatus() != ThingStatus.OFFLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.communication-error.serial-port-not-found" + config.serialPort);
            }
            return false;
        }

        try {
            logger.trace("connect port: {}", config.serialPort);

            if (portIdentifier.isCurrentlyOwned()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.communication-error.serial-port-busy" + config.serialPort);
                return false;
            }

            this.port = portIdentifier.open("org.openhab.binding.pentair", 10000);
            SerialPort port = this.port;

            if (port == null) {
                return false;
            }

            port.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

            InputStream is = port.getInputStream();
            OutputStream os = port.getOutputStream();

            if (is != null) {
                setInputStream(is);
            }

            if (os != null) {
                setOutputStream(os);
            }
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.serial-port-busy" + config.serialPort);
            return false;
        } catch (UnsupportedCommOperationException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.serial-port-error" + config.serialPort + ", " + e.getMessage());
            return false;
        }

        logger.debug("Pentair Bridge connected to serial port: {}", config.serialPort);

        return true;
    }

    @Override
    protected synchronized void disconnect() {
        SerialPort port = this.port;
        if (port != null) {
            port.close();
            port = null;
        }
    }
}
