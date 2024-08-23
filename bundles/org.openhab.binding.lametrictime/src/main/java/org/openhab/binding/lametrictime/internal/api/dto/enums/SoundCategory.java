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
package org.openhab.binding.lametrictime.internal.api.dto.enums;

import org.openhab.binding.lametrictime.internal.api.dto.ApiValue;

/**
 * Enum for sound category.
 *
 * @author Gregory Moyer - Initial contribution
 */
public enum SoundCategory implements ApiValue {
    NOTIFICATIONS,
    ALARMS;

    @Override
    public String toRaw() {
        return name().toLowerCase();
    }

    public static SoundCategory toEnum(String raw) {
        if (raw == null) {
            return null;
        }

        try {
            return valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            // not a valid raw string
            return null;
        }
    }
}
