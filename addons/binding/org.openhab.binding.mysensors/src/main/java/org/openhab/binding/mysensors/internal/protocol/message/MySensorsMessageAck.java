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

import java.util.HashMap;
import java.util.Map;

/**
 * Every message contains a field with which the sender is able to indicate that it requests an
 * Acknowledgement for the message.
 *
 * @author Tim Oberföll
 *
 */
public enum MySensorsMessageAck {
    TRUE(1),
    FALSE(0);

    private final int id;

    private MySensorsMessageAck(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    private static final Map<Integer, MySensorsMessageAck> ID = new HashMap<Integer, MySensorsMessageAck>();
    static {
        for (MySensorsMessageAck e : MySensorsMessageAck.values()) {
            if (ID.put(e.getId(), e) != null) {
                throw new IllegalArgumentException("duplicate id: " + e.getId());
            }
        }
    }

    public static MySensorsMessageAck getById(int id) {
        return ID.get(id);
    }

}
