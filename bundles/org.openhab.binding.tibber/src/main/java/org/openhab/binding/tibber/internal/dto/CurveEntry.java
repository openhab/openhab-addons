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
package org.openhab.binding.tibber.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link CurveEntry} represents one entry of a curve with power and duration.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CurveEntry {
    public int powerWatts;
    public long durationSeconds;

    public CurveEntry(int powerWatts, long durationSeconds) {
        this.powerWatts = powerWatts;
        this.durationSeconds = durationSeconds;
    }

    @Override
    public String toString() {
        return "{\"power\":" + powerWatts + ",\"duration\":" + durationSeconds + "}";
    }
}
