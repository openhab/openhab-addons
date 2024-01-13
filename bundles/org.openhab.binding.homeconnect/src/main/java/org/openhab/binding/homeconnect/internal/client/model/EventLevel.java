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
package org.openhab.binding.homeconnect.internal.client.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Event level model.
 *
 * @author Jonas Brüstel - Initial contribution
 *
 */
@NonNullByDefault
public enum EventLevel {
    CRITICAL("critical"),
    ALERT("alert"),
    WARNING("warning"),
    HINT("hint"),
    INFO("info");

    private final String level;

    EventLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return this.level;
    }

    public static @Nullable EventLevel valueOfLevel(String type) {
        for (EventLevel eventType : EventLevel.values()) {
            if (eventType.level.equalsIgnoreCase(type)) {
                return eventType;
            }
        }
        return null;
    }
}
