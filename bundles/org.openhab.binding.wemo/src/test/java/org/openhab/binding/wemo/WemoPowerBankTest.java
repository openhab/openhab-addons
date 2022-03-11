/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.wemo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.wemo.internal.WemoPowerBank;

/**
 * Unit tests for {@link WemoPowerBank}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class WemoPowerBankTest {

    @Test
    public void getCalculatedAverageOneMinuteEvenLoad() {
        var bank = new WemoPowerBank();

        bank.apply(22, getInstantOf("2022-03-08T22:00:00Z"));
        bank.apply(23, getInstantOf("2022-03-08T22:00:30Z"));
        bank.apply(99, getInstantOf("2022-03-08T22:01:00Z"));

        assertEquals(22.5, bank.getCalculatedAverage(0));
    }

    @Test
    public void getCalculatedAverageOlderValuesAreIgnored() {
        var bank = new WemoPowerBank();

        bank.apply(99, getInstantOf("2022-03-08T21:59:59Z"));
        bank.apply(22, getInstantOf("2022-03-08T22:00:00Z"));
        bank.apply(23, getInstantOf("2022-03-08T22:00:30Z"));
        bank.apply(99, getInstantOf("2022-03-08T22:01:00Z"));

        assertEquals(22.5, bank.getCalculatedAverage(0));
    }

    @Test
    public void getCalculatedAveragePreviousValueBeforeWindowIsConsidered() {
        var bank = new WemoPowerBank();

        bank.apply(22, getInstantOf("2022-03-08T21:59:59Z"));
        bank.apply(23, getInstantOf("2022-03-08T22:00:30Z"));
        bank.apply(99, getInstantOf("2022-03-08T22:01:00Z"));

        assertEquals(22.5, bank.getCalculatedAverage(0));
    }

    @Test
    public void getCalculatedAverageOneMinuteUnevenLoad() {
        var bank = new WemoPowerBank();

        bank.apply(20, getInstantOf("2022-03-08T22:00:00Z"));
        bank.apply(26, getInstantOf("2022-03-08T22:00:20Z"));
        bank.apply(99, getInstantOf("2022-03-08T22:01:00Z"));

        assertEquals(24, bank.getCalculatedAverage(0));
    }

    @Test
    public void getCalculatedAverageSingleValue() {
        var bank = new WemoPowerBank();

        bank.apply(20, getInstantOf("2022-03-08T22:00:00Z"));

        assertEquals(50, bank.getCalculatedAverage(50));
    }

    @Test
    public void getCalculatedAverageDuplicateInstants() {
        var bank = new WemoPowerBank();

        bank.apply(22, getInstantOf("2022-03-08T22:00:00Z"));
        bank.apply(99, getInstantOf("2022-03-08T22:00:30Z"));
        bank.apply(23, getInstantOf("2022-03-08T22:00:30Z"));
        bank.apply(99, getInstantOf("2022-03-08T22:01:00Z"));

        assertEquals(22.5, bank.getCalculatedAverage(0));
    }

    private Instant getInstantOf(String time) {
        Clock clock = Clock.fixed(Instant.parse(time), ZoneId.of("UTC"));
        return Instant.now(clock);
    }
}
