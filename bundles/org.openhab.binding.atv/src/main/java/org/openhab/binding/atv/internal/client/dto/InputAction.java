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
 * Type of input when pressing a button.
 *
 * <p>
 * Numeric values are fixed for storage compatibility.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum InputAction {
    /**
     * Press and release quickly.
     */
    SingleTap(0),

    /**
     * Press and release twice quickly.
     */
    DoubleTap(1),

    /**
     * Press and hold for one second before releasing.
     */
    Hold(2);

    private final int value;

    InputAction(int value) {
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
    public static InputAction fromValue(int value) {
        for (InputAction v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown InputAction value: " + value);
    }
}
