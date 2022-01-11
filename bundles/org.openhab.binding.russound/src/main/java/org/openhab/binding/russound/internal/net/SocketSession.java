/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.russound.internal.net;

import java.io.IOException;

/**
 * This is a socket session interface that defines the contract for a socket session. A socket session will initiate
 * communications with the underlying device and provide message back via the {@link SocketSessionListener}
 *
 * @author Tim Roberts - Initial contribution
 */
public interface SocketSession {

    /**
     * Adds a {@link SocketSessionListener} to call when responses/exceptions have been received
     *
     * @param listener a non-null {@link SocketSessionListener} to use
     */
    void addListener(SocketSessionListener listener);

    /**
     * Clears all listeners
     */
    void clearListeners();

    /**
     * Removes a {@link SocketSessionListener} from this session
     *
     * @param listener a non-null {@link SocketSessionListener} to remove
     * @return true if removed, false otherwise
     */
    boolean removeListener(SocketSessionListener listener);

    /**
     * Will attempt to connect to the {@link #_host} on port {@link #_port}. Simply calls {@link #connect(int)} with
     * a 2 second timeout
     *
     * @throws java.io.IOException if an exception occurs during the connection attempt
     */
    void connect() throws IOException;

    /**
     * Will attempt to connect to the {@link #_host} on port {@link #_port}. If we are current connected, will
     * {@link #disconnect()} first. Once connected, the {@link #_writer} and {@link #_reader} will be created, the
     * {@link #_dispatcher} and {@link #_responseReader} will be started.
     *
     * @param timeout a connection timeout (in milliseconds)
     * @throws java.io.IOException if an exception occurs during the connection attempt
     */
    void connect(int timeout) throws IOException;

    /**
     * Disconnects from the {@link #_host} if we are {@link #isConnected()}. The {@link #_writer}, {@link #_reader} and
     * {@link #_client}
     * will be closed and set to null. The {@link #_dispatcher} and {@link #_responseReader} will be stopped, the
     * {@link #_listeners} will be nulled and the {@link #_responses} will be cleared.
     *
     * @throws java.io.IOException if an exception occurs during the disconnect attempt
     */
    void disconnect() throws IOException;

    /**
     * Returns true if we are connected ({@link #_client} is not null and is connected)
     *
     * @return true if connected, false otherwise
     */
    boolean isConnected();

    /**
     * Sends the specified command to the underlying socket
     *
     * @param command a non-null, non-empty command
     * @throws java.io.IOException an exception that occurred while sending
     */
    void sendCommand(String command) throws IOException;
}
