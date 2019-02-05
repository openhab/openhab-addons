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
 * A MySensorsMessage is defined by its MessageType
 * Presentation: for a new sensors
 * Set: if a value is send by a sensor
 * req: if a value is requested from the sensor
 * internal: for example for battery status
 * stream: for example for firmware updates
 *
 * @author Tim Oberföll
 *
 */
public enum MySensorsMessageType {
    PRESENTATION(0),
    SET(1),
    REQ(2),
    INTERNAL(3),
    STREAM(4);

    private final int id;

    private MySensorsMessageType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    private static final Map<Integer, MySensorsMessageType> ID = new HashMap<Integer, MySensorsMessageType>();
    static {
        for (MySensorsMessageType e : MySensorsMessageType.values()) {
            if (ID.put(e.getId(), e) != null) {
                throw new IllegalArgumentException("duplicate id: " + e.getId());
            }
        }
    }

    public static MySensorsMessageType getById(int id) {
        return ID.get(id);
    }
}
