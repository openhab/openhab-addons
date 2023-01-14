/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.jeelink.internal.connection;

/**
 * Listener that is notified on connection status changes of JeeLinkConnections
 * as well as when input has been read from the connection.
 *
 * @author Volker Bier - Initial contribution
 */
public interface ConnectionListener {
    /**
     * Called when the connection has been opened.
     */
    void connectionOpened();

    /**
     * Called when the connection has been closed.
     */
    void connectionClosed();

    /**
     * Called when the connection has been aborted.
     *
     * @param cause a text describing the cause of the abort.
     */
    void connectionAborted(String cause);

    /**
     * Called whenever input has been read from the connection.
     */
    public void handleInput(String input);
}
