/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
