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
package org.openhab.binding.myvaillant.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.myvaillant.internal.api.EnergyDataType;
import org.openhab.binding.myvaillant.internal.api.JsonUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests the energy calculations of {@link SystemHandler}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
class SystemHandlerTest {

    private static final EnergyDataType CONSUMED = new EnergyDataType("HEATING", "CONSUMED_ELECTRICAL_ENERGY");
    private static final EnergyDataType GENERATED = new EnergyDataType("HEATING", "HEAT_GENERATED");

    @Test
    void sumsTodaysBucketsInTheSystemTimeZone() throws IOException {
        JsonObject buckets;
        try (InputStream stream = SystemHandlerTest.class
                .getResourceAsStream("/org/openhab/binding/myvaillant/internal/api/buckets.json")) {
            assertNotNull(stream);
            buckets = JsonUtil
                    .toSnakeCase(JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)))
                    .getAsJsonObject();
        }
        // 2026-11-19T23:00Z is midnight in Berlin, so the 400 Wh of that hour belong to the 20th there
        assertEquals(600.0, SystemHandler.sumBuckets(buckets, ZoneId.of("Europe/Berlin"), LocalDate.of(2026, 11, 20)));
        assertEquals(1000.0, SystemHandler.sumBuckets(buckets, ZoneId.of("Europe/Berlin"), LocalDate.of(2026, 11, 19)));
        assertEquals(200.0, SystemHandler.sumBuckets(buckets, ZoneId.of("UTC"), LocalDate.of(2026, 11, 20)));
    }

    @Test
    void heatingEfficiencyOverDevices() {
        Map<EnergyDataType, Double> heatPump = Map.of(CONSUMED, 1000.0, GENERATED, 3000.0);
        Map<EnergyDataType, Double> backupHeater = Map.of(CONSUMED, 500.0);
        assertEquals(new DecimalType(3.0), SystemHandler.getHeatingEfficiency(List.of(heatPump)));
        assertEquals(new DecimalType(2.0), SystemHandler.getHeatingEfficiency(List.of(heatPump, backupHeater)));
        assertEquals(UnDefType.UNDEF, SystemHandler.getHeatingEfficiency(List.of(Map.of(GENERATED, 10.0))));
    }

    @Test
    void efficiencyTypes() {
        assertTrue(SystemHandler.isHeatingEfficiencyType(CONSUMED));
        assertTrue(SystemHandler.isHeatingEfficiencyType(GENERATED));
        assertFalse(SystemHandler.isHeatingEfficiencyType(new EnergyDataType("HEATING", "EARNED_ENVIRONMENT_ENERGY")));
        assertFalse(SystemHandler
                .isHeatingEfficiencyType(new EnergyDataType("DOMESTIC_HOT_WATER", "CONSUMED_ELECTRICAL_ENERGY")));
    }
}
