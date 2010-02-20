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
package org.openhab.binding.insteon.internal.driver;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements IOStream for serial devices.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
@SuppressWarnings("null")
public class SerialIOStream extends IOStream {
    private final Logger logger = LoggerFactory.getLogger(SerialIOStream.class);
    private @Nullable SerialPort port = null;
    private final String appName = "PLM";
    private int baudRate = 19200;
    private String devName;
    private boolean validConfig = true;
    private @Nullable SerialPortManager serialPortManager;

    public SerialIOStream(@Nullable SerialPortManager serialPortManager, String config) {
        this.serialPortManager = serialPortManager;

        String[] parts = config.split(",");
        devName = parts[0];
        for (int i = 1; i < parts.length; i++) {
            String parameter = parts[i];
            String[] paramParts = parameter.split("=");
            if (paramParts.length != 2) {
                logger.warn("{} invalid parameter format '{}', must be 'key=value'.", config, parameter);

                validConfig = false;
            } else {
                String key = paramParts[0];
                String value = paramParts[1];
                if (key.equals("baudRate")) {
                    try {
                        baudRate = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        logger.warn("{} baudRate {} must be an integer.", config, value);

                        validConfig = false;
                    }
                } else {
                    logger.warn("{} invalid parameter '{}'.", config, parameter);

                    validConfig = false;
                }
            }
        }
    }

    @Override
    public boolean open() {
        if (!validConfig) {
            logger.warn("{} has an invalid configuration.", devName);
            return false;
        }

        try {
            SerialPortIdentifier spi = serialPortManager.getIdentifier(devName);
            if (spi == null) {
                logger.warn("{} is not a valid serial port.", devName);
                return false;
            }

            port = spi.open(appName, 1000);
            port.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            logger.debug("setting {} baud rate to {}", devName, baudRate);
            port.enableReceiveThreshold(1);
            port.enableReceiveTimeout(1000);
            in = port.getInputStream();
            out = port.getOutputStream();
            logger.debug("successfully opened port {}", devName);
            return true;
        } catch (IOException e) {
            logger.warn("cannot open port: {}, got IOException {}", devName, e.getMessage());
        } catch (PortInUseException e) {
            logger.warn("cannot open port: {}, it is in use!", devName);
        } catch (UnsupportedCommOperationException e) {
            logger.warn("got unsupported operation {} on port {}", e.getMessage(), devName);
        }

        return false;
    }

    @Override
    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.warn("failed to close input stream", e);
            }
            in = null;
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                logger.warn("failed to close output stream", e);
            }
            out = null;
        }

        if (port != null) {
            port.close();
            port = null;
        }
    }
}
