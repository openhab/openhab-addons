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
package org.openhab.binding.homeconnect.internal.client.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Event type model.
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public enum EventType {
    KEEP_ALIVE("KEEP-ALIVE"),
    STATUS("STATUS"),
    EVENT("EVENT"),
    NOTIFY("NOTIFY"),
    DISCONNECTED("DISCONNECTED"),
    CONNECTED("CONNECTED"),
    PAIRED("PAIRED"),
    DEPAIRED("DEPAIRED");

    private final String type;

    EventType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static @Nullable EventType valueOfType(@Nullable String type) {
        for (EventType eventType : EventType.values()) {
            if (eventType.type.equalsIgnoreCase(type)) {
                return eventType;
            }
        }
        return null;
    }
}
