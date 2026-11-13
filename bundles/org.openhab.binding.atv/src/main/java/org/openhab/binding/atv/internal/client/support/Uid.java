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
package org.openhab.binding.atv.internal.client.support;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A UID value inside a binary property list.
 *
 * <p>
 * UIDs are used by NSKeyedArchiver serialized data to reference entries in the
 * {@code $objects} table.
 *
 * @param value the referenced object index (unsigned, at most 8 bytes on the wire)
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record Uid(long value) {

    /**
     * Validates the UID value.
     *
     * @throws IllegalArgumentException if the value is negative
     */
    public Uid {
        if (value < 0) {
            throw new IllegalArgumentException("UIDs must be positive");
        }
    }
}
