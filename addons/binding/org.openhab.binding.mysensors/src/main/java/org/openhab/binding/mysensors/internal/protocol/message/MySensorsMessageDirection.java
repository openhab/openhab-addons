/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mysensors.internal.protocol.message;

/**
 * Differentiates between the directions of a message.
 * Incoming == from the gateway to the binding
 * Outgoing == from the binding to the gateway
 *
 * @author Tim Oberföll
 *
 */
public enum MySensorsMessageDirection {
    INCOMING(0),
    OUTGOING(1);

    private final int id;

    private MySensorsMessageDirection(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
