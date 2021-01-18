/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.octopusenergy.internal.OctopusEnergyBindingConstants;

/**
 * The {@link PriceTest} is a test class for {@link Price}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class PriceTest {

    private static final ZonedDateTime UDT = OctopusEnergyBindingConstants.UNDEFINED_TIME;

    private static final List<Price> LIST = new ArrayList<>();

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        LIST.add(new Price(5.2, 5.2, ZonedDateTime.parse("2020-10-09T07:00+00:00"), UDT));
        LIST.add(new Price(2.1, 2.1, ZonedDateTime.parse("2020-10-09T10:00+00:00"), UDT));
        LIST.add(new Price(8.4, 8.4, ZonedDateTime.parse("2020-10-09T03:00+00:00"), UDT));
        LIST.add(new Price(1.8, 1.8, ZonedDateTime.parse("2020-10-09T09:00+00:00"), UDT));
        LIST.add(new Price(7.3, 7.3, ZonedDateTime.parse("2020-10-09T01:00+00:00"), UDT));
    }

    @Test
    void testComparatorPriceOrderAsc() {
        List<Price> l = new ArrayList<>(LIST);
        Collections.sort(l, Price.PRICE_ORDER_ASC);
        assertEquals(BigDecimal.valueOf(1.8), l.get(0).valueExcVat);
        assertEquals(BigDecimal.valueOf(2.1), l.get(1).valueExcVat);
        assertEquals(BigDecimal.valueOf(5.2), l.get(2).valueExcVat);
        assertEquals(BigDecimal.valueOf(7.3), l.get(3).valueExcVat);
        assertEquals(BigDecimal.valueOf(8.4), l.get(4).valueExcVat);
    }

    @Test
    void testComparatorIntervalStartOrderAsc() {
        List<Price> l = new ArrayList<>(LIST);
        Collections.sort(l, Price.INTERVAL_START_ORDER_ASC);
        assertEquals(BigDecimal.valueOf(7.3), l.get(0).valueExcVat);
        assertEquals(BigDecimal.valueOf(8.4), l.get(1).valueExcVat);
        assertEquals(BigDecimal.valueOf(5.2), l.get(2).valueExcVat);
        assertEquals(BigDecimal.valueOf(1.8), l.get(3).valueExcVat);
        assertEquals(BigDecimal.valueOf(2.1), l.get(4).valueExcVat);
    }
}
