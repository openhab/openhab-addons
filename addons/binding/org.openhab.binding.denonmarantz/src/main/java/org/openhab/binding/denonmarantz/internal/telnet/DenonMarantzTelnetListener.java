/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.telnet;

import org.openhab.binding.denonmarantz.internal.DenonMarantzConnector;

/**
 * Listener interface used to notify the {@link DenonMarantzConnector} about received messages over Telnet
 *
 * @author Jan-Willem Veldhuis
 *
 */
public interface DenonMarantzTelnetListener {
    /**
     *
     */
    public void receivedLine(String line);

    /**
     * The telnet client has successfully connected to the receiver.
     */
    public void telnetClientConnected(boolean connected);

}
