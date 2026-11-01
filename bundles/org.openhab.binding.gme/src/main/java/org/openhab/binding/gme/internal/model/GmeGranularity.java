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
package org.openhab.binding.gme.internal.model;

import java.time.Duration;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * GME MGP price granularity.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public enum GmeGranularity {
    PT15(15),
    PT30(30),
    PT60(60);

    private final int minutes;

    GmeGranularity(int minutes) {
        this.minutes = minutes;
    }

    public int minutes() {
        return minutes;
    }

    public Duration duration() {
        return Duration.ofMinutes(minutes);
    }

    public String apiValue() {
        return name();
    }

    public static GmeGranularity fromApiValue(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported GME granularity: " + value, e);
        }
    }
}
