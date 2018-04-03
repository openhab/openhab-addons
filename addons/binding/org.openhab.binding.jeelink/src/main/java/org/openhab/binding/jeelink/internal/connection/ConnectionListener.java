/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
