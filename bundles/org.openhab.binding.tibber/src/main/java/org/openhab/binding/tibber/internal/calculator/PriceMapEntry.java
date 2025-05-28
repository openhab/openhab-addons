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
package org.openhab.binding.tibber.internal.calculator;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PriceMapEntry} represents one entry of a curve with power and duration.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PriceMapEntry {
    public Instant startAt;
    public double total;
    public int duration;
    public int level;

    public PriceMapEntry(Instant start, int duration, double total, int level) {
        this.startAt = start;
        this.duration = duration;
        this.total = total;
        this.level = level;
    }

    @Override
    public String toString() {
        return "{\"startAt\":\"" + startAt + "\",\"duration\":" + duration + ",\"cost\":" + total + "}";
    }
}
