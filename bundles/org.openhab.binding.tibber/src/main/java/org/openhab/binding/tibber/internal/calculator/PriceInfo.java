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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PriceInfo} represents one entry of a curve with power and duration.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PriceInfo {
    public double price;
    public int durationSeconds;
    public Instant timestamp;

    public PriceInfo(double price, int durationSeconds, Instant timestamp) {
        this.price = price;
        this.durationSeconds = durationSeconds;
        this.timestamp = timestamp;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> priceInfoMap = new HashMap<>();
        priceInfoMap.put("price", price);
        priceInfoMap.put("duration", durationSeconds);
        priceInfoMap.put("timestamp", timestamp);
        return priceInfoMap;
    }

    public void adjust(Instant earliestStart, Instant latestEnd) {
        if (timestamp.isBefore(earliestStart)) {
            // adjust start time and duration to fit exactly to earliestStart
            int adjustDuration = (int) Duration.between(timestamp, earliestStart).getSeconds();
            System.out.println("Adjust entry to " + earliestStart + " minus " + adjustDuration);
            durationSeconds -= adjustDuration;
            timestamp = earliestStart;
        }
        if (timestamp.plus(durationSeconds, ChronoUnit.SECONDS).isAfter(latestEnd)) {
            // adjust duration according to latestEnd
            durationSeconds = (int) Duration.between(timestamp, latestEnd).getSeconds();
            System.out.println("Adjust entry to " + durationSeconds);
        }
    }

    public String toJSON() {
        return "{\"price\":" + price + ",\"duration\":" + durationSeconds + ",\"timestamp\":\"" + timestamp + "\"}";
    }

    @Override
    public String toString() {
        return "{\"price\":" + price + ",\"duration\":" + durationSeconds + ",\"timestamp\":\"" + timestamp + "\"}";
    }
}
