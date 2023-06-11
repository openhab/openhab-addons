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
     * sends the specified commands to the receiver (commands are semicolon separated)
     */
    void sendCommands(String initCommands);
}
