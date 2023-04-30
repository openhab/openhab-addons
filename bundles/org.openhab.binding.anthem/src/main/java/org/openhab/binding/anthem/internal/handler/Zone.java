/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.anthem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Zone} defines the zones supported by the Anthem processor.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum Zone {
    MAIN("1"),
    ZONE2("2");

    private final String value;

    Zone(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Zone fromValue(String value) {
        for (Zone m : Zone.values()) {
            if (m.getValue().equals(value)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Invalid or null zone: " + value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}
