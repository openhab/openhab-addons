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
package org.openhab.transform.vat.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RateProvider}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class RateProviderTest {

    @Test
    void getPercentageWhenNoPeriods() {
        RateProvider rateProvider = new RateProvider();
        Instant time = LocalDateTime.of(2024, 10, 27, 22, 5, 0).atZone(ZoneId.of("Europe/Copenhagen")).toInstant();
        @Nullable
        BigDecimal actual = rateProvider.getPercentage("DK", time);
        assertThat(actual, is(equalTo(new BigDecimal(25))));
    }

    @Test
    void getPercentageJustBeforeNewRateComesIntoEffect() {
        RateProvider rateProvider = new RateProvider();
        Instant time = LocalDateTime.of(2025, 1, 1, 0, 0, 0).minusNanos(1).atZone(ZoneId.of("Asia/Jerusalem"))
                .toInstant();
        @Nullable
        BigDecimal actual = rateProvider.getPercentage("IL", time);
        assertThat(actual, is(equalTo(new BigDecimal(17))));
    }

    @Test
    void getPercentageAtMomentOfNewRateComingIntoEffect() {
        RateProvider rateProvider = new RateProvider();
        Instant time = LocalDateTime.of(2025, 1, 1, 0, 0, 0).atZone(ZoneId.of("Asia/Jerusalem")).toInstant();
        @Nullable
        BigDecimal actual = rateProvider.getPercentage("IL", time);
        assertThat(actual, is(equalTo(new BigDecimal(18))));
    }
}
