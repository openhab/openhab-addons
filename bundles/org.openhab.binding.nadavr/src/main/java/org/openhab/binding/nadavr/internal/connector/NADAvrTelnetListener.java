/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nadavr.internal.connector;

/**
 * The {@link NADAvrTelnetListener} Listener interface used to notify the {@link NADAvrConnector} about received messages.
 *
 * @author Dave J Schoepel - Initial contribution
 */
public interface NADAvrTelnetListener {

    /**
     * The telnet client has received a line.
     *
     * @param line the received line
     */
    void receivedLine(String line);

    /**
     * The telnet client has successfully connect to the receiver.
     *
     * @param connected whether or not the connection was successful
     */
    void telnetClientConnected(boolean connected);
}
