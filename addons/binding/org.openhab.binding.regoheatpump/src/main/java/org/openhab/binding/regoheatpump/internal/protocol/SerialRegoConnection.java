/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump.internal.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NRSerialPort;

/**
 * The {@link SerialRegoConnection} is responsible for creating serial connections to clients.
 *
 * @author Boris Krivonog - Initial contribution
 */
public class SerialRegoConnection implements RegoConnection {
    private final Logger logger = LoggerFactory.getLogger(SerialRegoConnection.class);
    private final int baudRate;
    private final String portName;
    private NRSerialPort serialPort;

    public SerialRegoConnection(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
    }

    @Override
    public void connect() throws IOException {
        if (isPortNameExist(portName)) {
            serialPort = new NRSerialPort(portName, baudRate);
            if (!serialPort.connect()) {
                throw new IOException("Failed to connect on port " + portName);
            }

            logger.debug("Connected to {}", portName);
        } else {
            throw new IOException("Serial port with name " + portName + " does not exist. Available port names: "
                    + NRSerialPort.getAvailableSerialPorts());
        }
    }

    @Override
    public boolean isConnected() {
        return serialPort != null && serialPort.isConnected();
    }

    @Override
    public void close() {
        if (serialPort != null) {
            serialPort.disconnect();
            serialPort = null;
        }
    }

    @Override
    public OutputStream outputStream() throws IOException {
        return serialPort.getOutputStream();
    }

    @Override
    public InputStream inputStream() throws IOException {
        return serialPort.getInputStream();
    }

    private boolean isPortNameExist(String portName) {
        return NRSerialPort.getAvailableSerialPorts().contains(portName);
    }
}
