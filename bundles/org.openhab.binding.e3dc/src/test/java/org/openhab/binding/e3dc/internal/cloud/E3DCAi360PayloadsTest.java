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
package org.openhab.binding.e3dc.internal.cloud;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the mapping between the AI 360 JSON bodies and the channel state.
 *
 * <p>
 * The bodies are the ones the E3/DC portal sends; a write always carries the whole object, so these tests
 * pin that a write keeps the fields no channel covers and that the month bit mask starts with January.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCAi360PayloadsTest {

    private static JsonObject json(String text) {
        return JsonParser.parseString(text).getAsJsonObject();
    }

    private static JsonObject emsBody() {
        return json("""
                {"battery":true,"sgReady":false,"feedInTariffType":"FIXED","isActive":true}
                """);
    }

    private static JsonObject pricesBody() {
        return json("""
                {"priceLimitWb":25.0,"gridfee":12.3,"surcharge":2.5,"furthertaxes":1.9,
                 "dppEnabledWb":true,"dppEnabledBattery":true,"enableDynamicEms":true,
                 "priceLimitBattery":22.5,"activeMonthsBattery":4095,"maxSocBattery":100}
                """);
    }

    @Test
    void stateJoinsBothBodies() {
        E3DCAi360State state = E3DCAi360Payloads.state(emsBody(), pricesBody());

        assertTrue(state.enabled());
        assertTrue(state.batteryPriceCharging());
        assertFalse(state.sgReady());
        assertEquals("FIXED", state.feedInTariffType());
        assertEquals(22.5, state.priceLimit(), 0.0001);
        assertEquals(100, state.chargeUntilSoc());
        assertEquals(E3DCAi360State.ALL_MONTHS, state.activeMonths());
    }

    @Test
    void stateWithoutCloudSettingsIsEmptyRatherThanFailing() {
        E3DCAi360State state = E3DCAi360Payloads.state(null, null);

        assertFalse(state.enabled());
        assertFalse(state.batteryPriceCharging());
        assertEquals(E3DCAi360State.NO_FEED_IN_TARIFF, state.feedInTariffType());
        assertEquals(0, state.priceLimit(), 0.0001);
        assertEquals(E3DCAi360State.NO_MONTHS, state.activeMonths());
    }

    @Test
    void monthsStartWithJanuary() {
        assertEquals("100000000000", E3DCAi360Payloads.monthsText(1));
        assertEquals("000000000001", E3DCAi360Payloads.monthsText(1 << 11));
        assertEquals("111111111111", E3DCAi360Payloads.monthsText(E3DCAi360State.ALL_MONTHS));
        assertEquals(E3DCAi360State.ALL_MONTHS, E3DCAi360Payloads.monthsMask("111111111111"));
        assertEquals(1, E3DCAi360Payloads.monthsMask("100000000000"));
    }

    @Test
    void monthsRejectAnythingButTwelveFlags() {
        assertThrows(IllegalArgumentException.class, () -> E3DCAi360Payloads.monthsMask("1111"));
        assertThrows(IllegalArgumentException.class, () -> E3DCAi360Payloads.monthsMask("11111111111x"));
    }

    @Test
    void writingAPriceLimitKeepsTheWallboxFields() {
        JsonObject written = E3DCAi360Payloads.prices(pricesBody(), true, true, 18.25, null, null);

        assertNotNull(written);
        assertEquals(18.25, written.get("priceLimitBattery").getAsDouble(), 0.0001);
        // the fields no channel covers have to survive the write
        assertEquals(25.0, written.get("priceLimitWb").getAsDouble(), 0.0001);
        assertEquals(12.3, written.get("gridfee").getAsDouble(), 0.0001);
        assertTrue(written.get("dppEnabledWb").getAsBoolean());
        assertEquals(100, written.get("maxSocBattery").getAsInt());
    }

    @Test
    void enablingAi360SetsBothFlags() {
        JsonObject written = E3DCAi360Payloads.pricesEnabled(pricesBody(), true);

        assertNotNull(written);
        assertTrue(written.get(E3DCAi360Payloads.PRICES_DYNAMIC_EMS).getAsBoolean());
        assertTrue(written.get(E3DCAi360Payloads.PRICES_BATTERY_ENABLED).getAsBoolean());
    }

    @Test
    void clearingTheFeedInTariffSendsNull() {
        JsonObject written = E3DCAi360Payloads.ems(emsBody(), false, false, null);

        assertNotNull(written);
        assertFalse(written.get(E3DCAi360Payloads.EMS_BATTERY).getAsBoolean());
        assertTrue(written.get(E3DCAi360Payloads.EMS_FEED_IN_TARIFF).isJsonNull());
    }

    @Test
    void writeWithoutReadBodyIsSkipped() {
        // without the read body the base fields of the storage object are unknown, so it must not be written
        assertNull(E3DCAi360Payloads.prices(null, true, true, 20.0, null, null));
        assertNull(E3DCAi360Payloads.ems(null, true, false, "FIXED"));
    }
}
