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
package org.openhab.binding.pioneeravr.internal.protocol.event;

import org.openhab.binding.pioneeravr.internal.protocol.avr.AvrConnection;

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
