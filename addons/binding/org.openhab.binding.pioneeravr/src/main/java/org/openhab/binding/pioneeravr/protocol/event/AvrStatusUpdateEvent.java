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
 * The event fired when a status is received from the AVR.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class AvrStatusUpdateEvent {

    private AvrConnection connection;
    private String data;

    public AvrStatusUpdateEvent(AvrConnection connection, String data) {
        this.connection = connection;
        this.data = data;
    }

    public AvrConnection getConnection() {
        return connection;
    }

    public String getData() {
        return data;
    }

}
