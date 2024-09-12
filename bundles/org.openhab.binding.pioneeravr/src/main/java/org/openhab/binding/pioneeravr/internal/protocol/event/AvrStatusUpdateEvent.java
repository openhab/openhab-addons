/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
