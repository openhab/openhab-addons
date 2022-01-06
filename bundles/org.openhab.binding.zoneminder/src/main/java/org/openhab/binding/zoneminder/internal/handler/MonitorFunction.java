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
package org.openhab.binding.zoneminder.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MonitorFunction} represents the valid functions for a Zoneminder monitor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum MonitorFunction {

    NONE("None"),
    MONITOR("Monitor"),
    MODECT("Modect"),
    RECORD("Record"),
    MOCORD("Mocord"),
    NODECT("Nodect");

    private final String type;

    private MonitorFunction(String type) {
        this.type = type;
    }

    public static MonitorFunction forValue(@Nullable String v) {
        if (v != null) {
            for (MonitorFunction at : MonitorFunction.values()) {
                if (at.type.equals(v)) {
                    return at;
                }
            }
        }
        throw new IllegalArgumentException(String.format("Invalid or null monitor function: %s" + v));
    }

    @Override
    public String toString() {
        return this.type;
    }
}
