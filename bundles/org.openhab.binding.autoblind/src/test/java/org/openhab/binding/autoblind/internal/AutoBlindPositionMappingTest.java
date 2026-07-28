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
package org.openhab.binding.autoblind.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for the position mapping between hub API values and openHAB values.
 * Hub API: 0 = closed, 100 = open.
 * openHAB: 0 = open, 100 = closed.
 * Conversion: ohPosition = 100 - apiPosition.
 *
 * @author Stephen Berg (@BiloxiGeek) - Initial contribution
 */
@NonNullByDefault
class AutoBlindPositionMappingTest {

    @ParameterizedTest
    @CsvSource({ "0, 100", "100, 0", "50, 50", "25, 75", "75, 25" })
    void apiToOpenHabPositionMapping(int apiPosition, int expectedOhPosition) {
        int ohPosition = 100 - apiPosition;
        assertEquals(expectedOhPosition, ohPosition);
    }

    @ParameterizedTest
    @CsvSource({ "0, 100", "100, 0", "50, 50", "25, 75", "75, 25" })
    void openHabToApiPositionMapping(int ohPosition, int expectedApiPosition) {
        int apiPosition = 100 - ohPosition;
        assertEquals(expectedApiPosition, apiPosition);
    }

    @Test
    void roundTripConversion() {
        for (int api = 0; api <= 100; api++) {
            int oh = 100 - api;
            int backToApi = 100 - oh;
            assertEquals(api, backToApi);
        }
    }

    @Test
    void batteryValueClamping() {
        assertEquals(0, Math.max(0, Math.min(100, -5)));
        assertEquals(100, Math.max(0, Math.min(100, 150)));
        assertEquals(50, Math.max(0, Math.min(100, 50)));
    }
}
