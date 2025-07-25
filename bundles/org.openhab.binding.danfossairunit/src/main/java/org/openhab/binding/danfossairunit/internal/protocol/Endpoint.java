/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This represents the different endpoints.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public enum Endpoint {
    ENDPOINT_0((byte) 0x00),
    ENDPOINT_1((byte) 0x01),
    ENDPOINT_4((byte) 0x04);

    private final byte value;

    Endpoint(byte value) {
        this.value = value;
    }

    /**
     * Returns the byte value representing this endpoint.
     *
     * @return the byte value of the endpoint
     */
    public byte getValue() {
        return value;
    }
}
