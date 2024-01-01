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
package org.openhab.binding.lifx.internal.dto;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public enum LightLastHevCycleResult {

    SUCCESS(0),
    BUSY(1),
    INTERRUPTED_BY_RESET(2),
    INTERRUPTED_BY_HOMEKIT(3),
    INTERRUPTED_BY_LAN(4),
    INTERRUPTED_BY_CLOUD(5),
    NONE(255);

    private final int type;

    LightLastHevCycleResult(int type) {
        this.type = type;
    }

    public static LightLastHevCycleResult fromValue(int type) {
        Optional<LightLastHevCycleResult> result = Arrays.stream(values()).filter((value) -> value.type == type)
                .findFirst();

        if (result.isEmpty()) {
            throw new IllegalArgumentException("Invalid LightLastHevCycleResult type: " + type);
        }

        return result.get();
    }
}
