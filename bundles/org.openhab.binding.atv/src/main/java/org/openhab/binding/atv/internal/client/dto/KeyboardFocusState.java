/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.dto;

/**
 * All supported keyboard focus states.
 *
 * <p>
 * Numeric values are fixed for storage compatibility.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum KeyboardFocusState {
    /**
     * Keyboard focus state is not determinable.
     */
    Unknown(0),

    /**
     * Keyboard is not focused.
     */
    Unfocused(1),

    /**
     * Keyboard is focused.
     */
    Focused(2);

    private final int value;

    KeyboardFocusState(int value) {
        this.value = value;
    }

    /**
     * Returns the stored numeric value.
     */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching a stored numeric value.
     *
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static KeyboardFocusState fromValue(int value) {
        for (KeyboardFocusState v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown KeyboardFocusState value: " + value);
    }
}
