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
package org.openhab.binding.openwebnet.internal.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortEvent;
import org.openhab.core.io.transport.serial.SerialPortEventListener;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openwebnet4j SerialPort implementation based on OH serial transport
 *
 * @author M. Valla - Initial contribution
 */

@NonNullByDefault
public class SerialPortAdapter implements org.openwebnet4j.communication.serial.spi.SerialPort {

    private final Logger logger = LoggerFactory.getLogger(SerialPortAdapter.class);

    private static final int OPEN_TIMEOUT_MS = 200;

    private final SerialPortIdentifier spid;

    private @Nullable SerialPort sp = null;

    public SerialPortAdapter(final SerialPortIdentifier spid) {
        this.spid = spid;
    }

    @Override
    public boolean setSerialPortParams(int baudrate, int dataBits, int stopBits, int parity) {
        @Nullable
        SerialPort lsp = sp;
        if (lsp != null) {
            try {
                lsp.setSerialPortParams(baudrate, dataBits, stopBits, parity);
                return true;
            } catch (UnsupportedCommOperationException e) {
                logger.error("UnsupportedCommOperationException while setting port params in setSerialPortParams: {}",
                        e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean addEventListener(org.openwebnet4j.communication.serial.spi.SerialPortEventListener listener) {
        @Nullable
        SerialPort lsp = sp;
        if (lsp != null) {
            try {
                lsp.addEventListener(new SerialPortEventListener() {

                    @Override
                    public void serialEvent(SerialPortEvent event) {
                        if (event != null) {
                            listener.serialEvent(new SerialPortEventAdapter(event));
                        }
                    }
                });
                lsp.notifyOnDataAvailable(true);
                return true;
            } catch (TooManyListenersException e) {
                logger.error("TooManyListenersException while adding event listener: {}", e.getMessage());
            }
        }
        return false;
    }

    @Override
    public boolean open() {
        try {
            sp = spid.open(this.getClass().getName(), OPEN_TIMEOUT_MS);
        } catch (PortInUseException e) {
            logger.error("PortInUseException while opening serial port {}: {}", spid.getName(), e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public @Nullable String getName() {
        @Nullable
        SerialPort lsp = sp;
        if (lsp != null) {
            return lsp.getName();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable InputStream getInputStream() throws IOException {
        @Nullable
        SerialPort lsp = sp;
        if (lsp != null) {
            return lsp.getInputStream();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable OutputStream getOutputStream() throws IOException {
        @Nullable
        SerialPort lsp = sp;
        if (lsp != null) {
            return lsp.getOutputStream();
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        @Nullable
        SerialPort lsp = sp;
        if (lsp != null) {
            lsp.close();
        }
    }
}
