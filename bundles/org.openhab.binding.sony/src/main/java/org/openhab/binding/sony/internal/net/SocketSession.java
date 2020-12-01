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
package org.openhab.binding.sony.internal.net;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is a socket session interface that defines the contract for a socket session. A socket session will initiate
 * communications with the underlying device and provide message back via the {@link SocketSessionListener}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public interface SocketSession {

    /**
     * Adds a {@link SocketSessionListener} to call when responses/exceptions have been received.
     *
     * @param listener a non-null {@link SocketSessionListener} to use
     */
    void addListener(SocketSessionListener listener);

    /**
     * Clears all listeners.
     */
    void clearListeners();

    /**
     * Removes a {@link SocketSessionListener} from this session.
     *
     * @param listener a non-null {@link SocketSessionListener} to remove
     * @return true if removed, false otherwise
     */
    boolean removeListener(SocketSessionListener listener);

    /**
     * Will attempt to connect to the underlying host/port with default timeout
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void connect() throws IOException;

    /**
     * Will attempt to connect to the underlying host/port
     *
     * @param timeout a connection timeout (in milliseconds)
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void connect(int timeout) throws IOException;

    /**
     * Disconnects from the host/port
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void disconnect() throws IOException;

    /**
     * Returns true if connected, false otherwise
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Sends the specified command to the underlying socket.
     *
     * @param command a non-null, possibly empty command
     * @throws IOException Signals that an I/O exception has occurred.
     */
    void sendCommand(String command) throws IOException;
}
