/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package gnu.io.factory;

import java.io.IOException;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 *
 * @author MatthiasS
 *
 */
public interface SerialPortFactory {

    /**
     * Creates a {@link SerialPort} instance out of the given <code>portName</code>.
     *
     * @param portName The port's name to parse out whether to create a serial connection or a remote (rfc2217)
     *            connection.
     * @param expectedClass The {@link SerialPort} class that is expected to return.
     * @return The newly created and opened SerialPort.
     * @throws IOException
     * @throws PortInUseException
     * @throws NoSuchPortException
     * @throws UnsupportedCommOperationException
     */
    <T extends SerialPort> T createSerialPort(String portName, Class<T> expectedClass)
            throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException;

    /**
     * Creates a {@link SerialPort} instance out of the given <code>portName</code>.
     *
     * @param portName The port's name to parse out whether to create a serial connection or a remote (rfc2217)
     *            connection.
     * @return The newly created and opened SerialPort.
     * @throws IOException
     * @throws PortInUseException
     * @throws NoSuchPortException
     * @throws UnsupportedCommOperationException
     */
    SerialPort createSerialPort(String portName)
            throws PortInUseException, NoSuchPortException, UnsupportedCommOperationException;
}