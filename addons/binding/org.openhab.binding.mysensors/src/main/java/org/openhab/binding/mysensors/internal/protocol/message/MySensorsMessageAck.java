/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
