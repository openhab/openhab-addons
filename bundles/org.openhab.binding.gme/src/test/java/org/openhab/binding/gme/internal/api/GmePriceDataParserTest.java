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
package org.openhab.binding.gme.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.gme.internal.model.GmePriceEntry;

/**
 * Tests for {@link GmePriceDataParser}.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
class GmePriceDataParserTest {

    @Test
    void parsesPriceEntry() {
        String json = """
                [{
                  "FlowDate":"20260910",
                  "Hour":"1",
                  "Market":"MGP",
                  "Zone":"PUN",
                  "Price":"198.020000",
                  "Period":"0"
                }]
                """;

        List<GmePriceEntry> entries = GmePriceDataParser.parse(new StringReader(json));

        assertEquals(1, entries.size());
        GmePriceEntry entry = entries.getFirst();
        assertEquals(LocalDate.of(2026, 9, 10), entry.flowDate());
        assertEquals(1, entry.hour());
        assertEquals("MGP", entry.market());
        assertEquals("PUN", entry.zone());
        assertEquals(new BigDecimal("198.020000"), entry.priceMWh());
        assertEquals(new BigDecimal("0.198020000"), entry.priceKWh());
        assertEquals(0, entry.period());
    }

    @Test
    void ignoresCancelledAuction() {
        String json = """
                [{
                  "FlowDate":"20260716",
                  "Hour":"0",
                  "Market":"MI-A1",
                  "Zone":"",
                  "Price":"0",
                  "Period":"0",
                  "Notes":"Auction cancelled"
                }]
                """;

        List<GmePriceEntry> entries = GmePriceDataParser.parse(new StringReader(json));

        assertTrue(entries.isEmpty());
    }
}
