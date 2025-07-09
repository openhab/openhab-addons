/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport.stream;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.InsteonBindingConstants;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;

/**
 * Implements IOStream for serial devices
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class SerialIOStream extends IOStream {
    private static final int RATE_LIMIT_TIME = 800; // in milliseconds

    private String name;
    private int baudRate;
    private SerialPortManager serialPortManager;
    private @Nullable SerialPort port;

    public SerialIOStream(String name, int baudRate, SerialPortManager serialPortManager) {
        super(RATE_LIMIT_TIME);
        this.name = name;
        this.baudRate = baudRate;
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean open() {
        if (port != null) {
            logger.warn("serial port is already open");
            return false;
        }

        try {
            SerialPortIdentifier spi = serialPortManager.getIdentifier(name);
            if (spi == null) {
                logger.warn("{} is not a valid serial port.", name);
                return false;
            }

            SerialPort port = spi.open(InsteonBindingConstants.BINDING_ID, 1000);
            logger.debug("setting {} baud rate to {}", name, baudRate);
            port.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            port.enableReceiveThreshold(1);
            port.enableReceiveTimeout(1000);
            this.in = port.getInputStream();
            this.out = port.getOutputStream();
            this.port = port;
            logger.debug("successfully opened port {}", name);
            return true;
        } catch (IOException e) {
            logger.warn("cannot open port: {}, got IOException {}", name, e.getMessage());
        } catch (PortInUseException e) {
            logger.warn("cannot open port: {}, it is in use!", name);
        } catch (UnsupportedCommOperationException e) {
            logger.warn("got unsupported operation {} on port {}", e.getMessage(), name);
        }

        return false;
    }

    @Override
    public void close() {
        super.close();

        SerialPort port = this.port;
        if (port != null) {
            port.close();
            this.port = null;
        }
    }
}
