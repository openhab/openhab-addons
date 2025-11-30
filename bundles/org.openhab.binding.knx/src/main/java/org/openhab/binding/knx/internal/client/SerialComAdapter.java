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
package org.openhab.binding.knx.internal.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.serial.spi.SerialCom;

/**
 * The {@link SerialTransportAdapter} provides org.openhab.core.io.transport.serial
 * services to the Calimero library.
 * 
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class SerialComAdapter implements SerialCom {

    private Logger logger = LoggerFactory.getLogger(SerialComAdapter.class);
    @Nullable
    private SerialPort serialPort = null;

    protected SerialComAdapter(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void close() {
        logger.trace("Closing serial port");
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort != null) {
            tmpSerialPort.close();
            serialPort = null;
        }
    }

    @Override
    public @Nullable InputStream inputStream() {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort != null) {
            try {
                return tmpSerialPort.getInputStream();
            } catch (IOException e) {
                logger.info("Cannot open input stream");
            }
        }
        // should not throw, create a dummy return value
        byte[] buf = {};
        return new ByteArrayInputStream(buf);
    }

    @Override
    public @Nullable OutputStream outputStream() {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort != null) {
            try {
                return tmpSerialPort.getOutputStream();
            } catch (IOException e) {
                logger.info("Cannot open output stream");
            }
        }
        // should not throw, create a dummy return value
        return new ByteArrayOutputStream(0);
    }

    @Override
    public int baudRate() throws IOException {
        final @Nullable SerialPort tmpSerialPort = serialPort;
        if (tmpSerialPort == null) {
            throw new IOException("Port not available");
        }
        return tmpSerialPort.getBaudRate();
    }
}
