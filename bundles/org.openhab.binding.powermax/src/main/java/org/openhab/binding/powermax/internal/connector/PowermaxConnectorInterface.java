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
package org.openhab.binding.powermax.internal.connector;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.powermax.internal.message.PowermaxMessageEventListener;

/**
 * Interface for communication with the Visonic alarm panel
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface PowermaxConnectorInterface {

    /**
     * Method for opening a connection to the Visonic alarm panel.
     */
    public void open() throws Exception;

    /**
     * Method for closing a connection to the Visonic alarm panel.
     */
    public void close();

    /**
     * Returns connection status
     *
     * @return: true if connected or false if not
     **/
    public boolean isConnected();

    /**
     * Method for sending a message to the Visonic alarm panel
     *
     * @param data the message as a table of bytes
     **/
    public void sendMessage(byte[] data);

    /**
     * Method for reading data from the Visonic alarm panel
     *
     * @param buffer the buffer into which the data is read
     **/
    public int read(byte[] buffer) throws IOException;

    /**
     * Method for registering an event listener
     *
     * @param listener the listener to be registered
     */
    public void addEventListener(PowermaxMessageEventListener listener);

    /**
     * Method for removing an event listener
     *
     * @param listener the listener to be removed
     */
    public void removeEventListener(PowermaxMessageEventListener listener);
}
