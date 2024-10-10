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
package org.openhab.binding.insteon.internal.device.database;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RecordFlags} represents Insteon all-link record flags
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public enum RecordFlags {
    CONTROLLER(0xE2),
    RESPONDER(0xA2),
    INACTIVE(0x22),
    HIGH_WATER_MARK(0x00);

    private final int value;

    private RecordFlags(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public RecordType getRecordType() {
        return new RecordType(value);
    }
}
