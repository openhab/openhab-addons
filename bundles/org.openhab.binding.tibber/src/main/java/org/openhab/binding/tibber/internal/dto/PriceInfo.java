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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link PriceInfo} object holds values of Tibber price response.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PriceInfo {
    public double price;
    public int durationSeconds;
    public Instant startsAt;
    public int level;

    public PriceInfo(double price, int durationSeconds, Instant startsAt, int level) {
        this.price = price;
        this.durationSeconds = durationSeconds;
        this.startsAt = startsAt;
        this.level = level;
    }

    public void adjust(Instant earliestStart, Instant latestEnd) {
        if (startsAt.isBefore(earliestStart)) {
            // adjust start time and duration to fit exactly to earliestStart
            int adjustDuration = (int) Duration.between(startsAt, earliestStart).getSeconds();
            durationSeconds -= adjustDuration;
            startsAt = earliestStart;
        }
        if (startsAt.plus(durationSeconds, ChronoUnit.SECONDS).isAfter(latestEnd)) {
            // adjust duration according to latestEnd
            durationSeconds = (int) Duration.between(startsAt, latestEnd).getSeconds();
        }
    }

    @Override
    public String toString() {
        return "{\"price\":" + price + ",\"duration\":" + durationSeconds + ",\"level\":" + level + ",\"startsAt\":\""
                + startsAt + "\"}";
    }
}
