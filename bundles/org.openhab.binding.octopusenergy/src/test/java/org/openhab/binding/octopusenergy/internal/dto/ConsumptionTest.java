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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * The {@link ConsumptionTest} is a test class for {@link Consumption}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class ConsumptionTest {

    @Test
    void testCompare() {
        Consumption c1 = new Consumption(BigDecimal.valueOf(12.5), ZonedDateTime.parse("2020-10-09T01:00:00Z"),
                ZonedDateTime.parse("2020-10-09T01:00:00Z"));
        Consumption c2 = new Consumption(BigDecimal.valueOf(23.4), ZonedDateTime.parse("2020-10-09T02:00:00Z"),
                ZonedDateTime.parse("2020-10-09T03:00:00Z"));
        Consumption c3 = new Consumption(BigDecimal.valueOf(65.5), ZonedDateTime.parse("2020-10-09T01:00:00Z"),
                ZonedDateTime.parse("2020-10-09T01:00:00Z"));

        // c1 is less than c2
        assertEquals(-1, Consumption.INTERVAL_START_ORDER_ASC.compare(c1, c2));
        // c1 is equal to c3
        assertEquals(0, Consumption.INTERVAL_START_ORDER_ASC.compare(c1, c3));
        // c2 is greater than c1
        assertEquals(1, Consumption.INTERVAL_START_ORDER_ASC.compare(c2, c1));

        // c1 is less than c2
        assertEquals(-1, Consumption.CONSUMPTION_ORDER_ASC.compare(c1, c2));
        // c1 is less than c3
        assertEquals(-1, Consumption.CONSUMPTION_ORDER_ASC.compare(c1, c3));
        // c2 is greater than c1
        assertEquals(1, Consumption.CONSUMPTION_ORDER_ASC.compare(c2, c1));
    }

    @Test
    void testSort() {
        Consumption c1 = new Consumption(BigDecimal.valueOf(1.2), ZonedDateTime.parse("2020-10-09T01:00:00Z"),
                ZonedDateTime.parse("2020-10-09T02:00:00Z"));
        Consumption c2 = new Consumption(BigDecimal.valueOf(2.3), ZonedDateTime.parse("2020-10-09T02:00:00Z"),
                ZonedDateTime.parse("2020-10-09T03:00:00Z"));
        Consumption c3 = new Consumption(BigDecimal.valueOf(3.4), ZonedDateTime.parse("2020-10-09T03:00:00Z"),
                ZonedDateTime.parse("2020-10-09T04:00:00Z"));

        Consumption c4 = new Consumption(BigDecimal.valueOf(4.5), ZonedDateTime.parse("2020-10-09T01:30:00Z"),
                ZonedDateTime.parse("2020-10-09T02:30:00Z"));
        Consumption c5 = new Consumption(BigDecimal.valueOf(5.6), ZonedDateTime.parse("2020-10-09T02:30:00Z"),
                ZonedDateTime.parse("2020-10-09T03:30:00Z"));
        Consumption c6 = new Consumption(BigDecimal.valueOf(6.7), ZonedDateTime.parse("2020-10-09T03:30:00Z"),
                ZonedDateTime.parse("2020-10-09T04:30:00Z"));

        List<Consumption> set1 = new ArrayList<>();
        List<Consumption> set2 = new ArrayList<>();
        set1.add(c6);
        set1.add(c1);
        set1.add(c4);
        set2.add(c2);
        set2.add(c5);
        set2.add(c3);

        List<Consumption> cset1 = new ArrayList<Consumption>(set1);
        cset1.addAll(set2);
        Collections.sort(cset1, Consumption.INTERVAL_START_ORDER_ASC);
        List<Consumption> rset1 = new ArrayList<Consumption>();
        rset1.add(c1);
        rset1.add(c4);
        rset1.add(c2);
        rset1.add(c5);
        rset1.add(c3);
        rset1.add(c6);
        assertArrayEquals(rset1.toArray(), cset1.toArray());

        List<Consumption> cset2 = new ArrayList<Consumption>(set1);
        cset2.addAll(set2);
        Collections.sort(cset2, Consumption.CONSUMPTION_ORDER_ASC);
        List<Consumption> rset2 = new ArrayList<Consumption>();
        rset2.add(c1);
        rset2.add(c2);
        rset2.add(c3);
        rset2.add(c4);
        rset2.add(c5);
        rset2.add(c6);
        assertArrayEquals(rset2.toArray(), cset2.toArray());
    }

    @Test
    void testAggregate() {
        Consumption c1 = new Consumption(BigDecimal.valueOf(1.2), ZonedDateTime.parse("2020-10-09T01:00:00Z"),
                ZonedDateTime.parse("2020-10-09T02:00:00Z"));
        Consumption c2 = new Consumption(BigDecimal.valueOf(2.3), ZonedDateTime.parse("2020-10-09T02:00:00Z"),
                ZonedDateTime.parse("2020-10-09T03:00:00Z"));

        Consumption c4 = new Consumption(BigDecimal.valueOf(4.5), ZonedDateTime.parse("2020-10-09T02:00:00Z"),
                ZonedDateTime.parse("2020-10-09T03:00:00Z"));
        Consumption c5 = new Consumption(BigDecimal.valueOf(5.6), ZonedDateTime.parse("2020-10-09T03:00:00Z"),
                ZonedDateTime.parse("2020-10-09T04:00:00Z"));

        List<Consumption> set1 = new ArrayList<>();
        List<Consumption> set2 = new ArrayList<>();
        set1.add(c1);
        set1.add(c2);
        set2.add(c4);
        set2.add(c5);

        @NonNull
        List<@NonNull Consumption> cset1 = Consumption.aggregate(set1, set2);
        Collections.sort(cset1, Consumption.INTERVAL_START_ORDER_ASC);

        assertEquals(3, cset1.size());
        assertEquals(ZonedDateTime.parse("2020-10-09T01:00:00Z"), cset1.get(0).intervalStart);
        assertEquals(BigDecimal.valueOf(1.2), cset1.get(0).consumption);
        assertEquals(ZonedDateTime.parse("2020-10-09T02:00:00Z"), cset1.get(1).intervalStart);
        assertEquals(BigDecimal.valueOf(6.8), cset1.get(1).consumption);
        assertEquals(ZonedDateTime.parse("2020-10-09T03:00:00Z"), cset1.get(2).intervalStart);
        assertEquals(BigDecimal.valueOf(5.6), cset1.get(2).consumption);
    }
}
