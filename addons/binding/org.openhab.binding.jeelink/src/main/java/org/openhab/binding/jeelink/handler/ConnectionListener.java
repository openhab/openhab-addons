/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jeelink.handler;

/**
 * Listener that is notified on connection status changed of JeeLinkConnections.
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
}
