/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol.event;

import org.openhab.binding.pioneeravr.protocol.AvrConnection;

/**
 * An event fired when an AVR is disconnected.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class AvrDisconnectionEvent {

    private AvrConnection connection;
    private Throwable cause;

    public AvrDisconnectionEvent(AvrConnection connection, Throwable cause) {
        this.connection = connection;
        this.cause = cause;
    }

    public AvrConnection getConnection() {
        return connection;
    }

    public Throwable getCause() {
        return cause;
    }

}
