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
 * Interface for connections to JeeLink USB Receivers.
 *
 * @author Volker Bier - Initial contribution
 */
public interface JeeLinkConnection {
    /**
     * closes the connection to the receiver.
     */
    void closeConnection();

    /**
     * opens the connection to the receiver.
     */
    void openConnection();

    /**
     * returns port to which the receiver is connected.
     */
    String getPort();

    /**
     * sends the specified init commands to the receiver.
     */
    void sendInitCommands(String initCommands);
}
