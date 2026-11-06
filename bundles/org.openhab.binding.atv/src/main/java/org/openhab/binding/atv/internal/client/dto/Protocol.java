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
 * All supported protocols.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum Protocol {
    /**
     * Protocol constant representing DMAP.
     */
    DMAP(1),

    /**
     * Protocol constant representing MRP.
     */
    MRP(2),

    /**
     * Protocol constant representing AirPlay.
     */
    AirPlay(3),

    /**
     * Protocol constant representing Companion link.
     */
    Companion(4),

    /**
     * Protocol constant representing RAOP.
     */
    RAOP(5);

    private final int value;

    Protocol(int value) {
        this.value = value;
    }

    /**
     * Returns the numeric value stored for this constant.
     */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching a stored numeric value.
     *
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static Protocol fromValue(int value) {
        for (Protocol v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown Protocol value: " + value);
    }
}
