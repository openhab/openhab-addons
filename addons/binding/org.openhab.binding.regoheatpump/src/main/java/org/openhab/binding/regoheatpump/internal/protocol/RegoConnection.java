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

/**
 * The {@link RegoConnection} is responsible for creating connections to clients.
 *
 * @author Boris Krivonog - Initial contribution
 */
public interface RegoConnection {
    /**
     * Connect to the receiver. Return true if the connection has succeeded or if already connected.
     *
     **/
    public void connect() throws IOException;

    /**
     * Return true if this manager is connected to the AVR.
     *
     * @return
     */
    public boolean isConnected();

    /**
     * Closes the connection.
     **/
    public void close();

    /**
     * Returns an output stream for this connection.
     */
    public OutputStream outputStream() throws IOException;

    /**
     * Returns an input stream for this connection.
     */
    public InputStream inputStream() throws IOException;
}
