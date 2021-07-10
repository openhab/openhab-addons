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
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.octopusenergy.internal.exception.RecordNotFoundException;

/**
 * The {@link ElectricityMeterPointTest} is a test class for {@link ElectricityMeterPoint}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class ElectricityMeterPointTest {

    private static final List<Agreement> AGREEMENT_LIST = new ArrayList<>();
    private static final List<Consumption> CONSUMPTION_LIST = new ArrayList<>();
    private static final List<Price> PRICE_LIST = new ArrayList<>();
    private static final ElectricityMeterPoint METER_POINT = new ElectricityMeterPoint();

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        AGREEMENT_LIST.add(new Agreement("E-1R-GREEN-12M-20-09-22-A", ZonedDateTime.parse("2020-10-09T00:00:00Z"),
                ZonedDateTime.parse("2020-10-19T00:00:00Z")));
        AGREEMENT_LIST.add(new Agreement("E-1R-VAR-19-04-12-N", ZonedDateTime.parse("2020-10-19T00:00:00Z"),
                ZonedDateTime.parse("2020-10-29T00:00:00Z")));
        AGREEMENT_LIST.add(new Agreement("E-1R-AGILE-18-02-21-A", ZonedDateTime.parse("2020-10-29T00:00:00Z"),
                ZonedDateTime.parse("2020-10-31T00:00:00Z")));
        METER_POINT.agreements = AGREEMENT_LIST;

        // setting up a consumption list
        CONSUMPTION_LIST.add(new Consumption(BigDecimal.valueOf(1.5), ZonedDateTime.parse("2020-10-09T00:00Z"),
                ZonedDateTime.parse("2020-10-09T00:30Z")));
        CONSUMPTION_LIST.add(new Consumption(BigDecimal.valueOf(2.5), ZonedDateTime.parse("2020-10-09T00:30Z"),
                ZonedDateTime.parse("2020-10-09T01:00Z")));
        CONSUMPTION_LIST.add(new Consumption(BigDecimal.valueOf(3.5), ZonedDateTime.parse("2020-10-09T01:00Z"),
                ZonedDateTime.parse("2020-10-09T01:30Z")));
        CONSUMPTION_LIST.add(new Consumption(BigDecimal.valueOf(4.5), ZonedDateTime.parse("2020-10-09T01:30Z"),
                ZonedDateTime.parse("2020-10-09T02:00Z")));
        CONSUMPTION_LIST.add(new Consumption(BigDecimal.valueOf(1), ZonedDateTime.parse("2020-10-09T02:00Z"),
                ZonedDateTime.parse("2020-10-09T02:30Z")));
        CONSUMPTION_LIST.add(new Consumption(BigDecimal.valueOf(3), ZonedDateTime.parse("2020-10-09T02:30Z"),
                ZonedDateTime.parse("2020-10-09T03:00Z")));
        METER_POINT.consumptionList = CONSUMPTION_LIST;

        // setting up a price profile of 1,1, 1,9, 9,2, 2,2, 2,3, 1,1, 8,8
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T00:00Z"),
                ZonedDateTime.parse("2020-10-09T00:30Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T00:30Z"),
                ZonedDateTime.parse("2020-10-09T01:00Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T01:00Z"),
                ZonedDateTime.parse("2020-10-09T01:30Z")));
        PRICE_LIST.add(new Price(9.0, 13.5, ZonedDateTime.parse("2020-10-09T01:30Z"),
                ZonedDateTime.parse("2020-10-09T02:00Z")));
        PRICE_LIST.add(new Price(9.0, 13.5, ZonedDateTime.parse("2020-10-09T02:00Z"),
                ZonedDateTime.parse("2020-10-09T02:30Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T02:30Z"),
                ZonedDateTime.parse("2020-10-09T03:00Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T03:00Z"),
                ZonedDateTime.parse("2020-10-09T03:30Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T03:30Z"),
                ZonedDateTime.parse("2020-10-09T04:00Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T04:00Z"),
                ZonedDateTime.parse("2020-10-09T04:30Z")));
        PRICE_LIST.add(new Price(3.0, 4.5, ZonedDateTime.parse("2020-10-09T04:30Z"),
                ZonedDateTime.parse("2020-10-09T05:00Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T05:00Z"),
                ZonedDateTime.parse("2020-10-09T05:30Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T05:30Z"),
                ZonedDateTime.parse("2020-10-09T06:00Z")));
        PRICE_LIST.add(new Price(8.0, 12.0, ZonedDateTime.parse("2020-10-09T06:00Z"),
                ZonedDateTime.parse("2020-10-09T06:30Z")));
        PRICE_LIST.add(new Price(8.0, 12.0, ZonedDateTime.parse("2020-10-09T06:30Z"),
                ZonedDateTime.parse("2020-10-09T07:00Z")));
        METER_POINT.priceList = PRICE_LIST;
    }

    @Test
    void testGetAgreementAsOf() {
        try {
            assertEquals("E-1R-GREEN-12M-20-09-22-A",
                    METER_POINT.getAgreementAsOf(ZonedDateTime.parse("2020-10-09T00:00:00Z")).tariffCode);
            assertEquals("E-1R-VAR-19-04-12-N",
                    METER_POINT.getAgreementAsOf(ZonedDateTime.parse("2020-10-19T00:00:00Z")).tariffCode);
            assertEquals("E-1R-AGILE-18-02-21-A",
                    METER_POINT.getAgreementAsOf(ZonedDateTime.parse("2020-10-29T00:00:00Z")).tariffCode);
        } catch (RecordNotFoundException e) {
            fail(e);
        }

        assertThrows(RecordNotFoundException.class, () -> {
            METER_POINT.getAgreementAsOf(ZonedDateTime.parse("2020-10-08T00:00:00Z"));
        });
        assertThrows(RecordNotFoundException.class, () -> {
            METER_POINT.getAgreementAsOf(ZonedDateTime.parse("2020-10-31T00:00:01Z"));
        });
    }

    @Test
    void testGetProductAsOf() {
        try {
            assertEquals("GREEN-12M-20-09-22", METER_POINT.getProductAsOf(ZonedDateTime.parse("2020-10-09T00:00:00Z")));
            assertEquals("VAR-19-04-12", METER_POINT.getProductAsOf(ZonedDateTime.parse("2020-10-19T00:00:00Z")));
            assertEquals("AGILE-18-02-21", METER_POINT.getProductAsOf(ZonedDateTime.parse("2020-10-29T00:00:00Z")));
        } catch (RecordNotFoundException e) {
            fail(e);
        }

        assertThrows(RecordNotFoundException.class, () -> {
            METER_POINT.getProductAsOf(ZonedDateTime.parse("2020-10-08T00:00:00Z"));
        });
        assertThrows(RecordNotFoundException.class, () -> {
            METER_POINT.getProductAsOf(ZonedDateTime.parse("2020-10-31T00:00:01Z"));
        });
    }

    @Test
    void testGetMostRecentConsumption() {
        try {
            assertEquals(BigDecimal.valueOf(3), METER_POINT.getMostRecentConsumption().consumption);
        } catch (RecordNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testGetMaxPrice() {
        try {
            assertEquals(BigDecimal.valueOf(13.5), METER_POINT.getMaxPrice(true));
        } catch (RecordNotFoundException e) {
            fail(e);
        }
    }

    @Test
    void testGetMinPrice() {
        try {
            assertEquals(BigDecimal.valueOf(1.5), METER_POINT.getMinPrice(true));
        } catch (RecordNotFoundException e) {
            fail(e);
        }
    }
}
