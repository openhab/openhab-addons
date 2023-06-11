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
package org.openhab.binding.denonmarantz.internal.connector.telnet;

import org.openhab.binding.denonmarantz.internal.connector.DenonMarantzConnector;

/**
 * Listener interface used to notify the {@link DenonMarantzConnector} about received messages over Telnet
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
public interface DenonMarantzTelnetListener {
    /**
     * The telnet client has received a line.
     * 
     * @param line the received line
     */
    void receivedLine(String line);

    /**
     * The telnet client has successfully connected to the receiver.
     * 
     * @param connected whether or not the connection was successful
     */
    void telnetClientConnected(boolean connected);
}
