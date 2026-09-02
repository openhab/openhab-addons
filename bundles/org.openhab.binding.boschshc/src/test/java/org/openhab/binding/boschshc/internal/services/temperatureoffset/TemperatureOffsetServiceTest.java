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
package org.openhab.binding.boschshc.internal.services.temperatureoffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TemperatureOffsetService}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class TemperatureOffsetServiceTest {

    @Test
    void sanitizeOffsetValue() {
        assertEquals(-5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(-6)), 0.001d);
        assertEquals(-5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(-5.1)), 0.001d);

        assertEquals(5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(6)), 0.001d);
        assertEquals(5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(5.1)), 0.001d);

        assertEquals(-5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(-5)), 0.001d);
        assertEquals(-5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(-5d)), 0.001d);

        assertEquals(5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(5)), 0.001d);
        assertEquals(5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(5d)), 0.001d);

        assertEquals(0.6d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(0.56d)), 0.001d);
        assertEquals(0.5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(0.54)), 0.001d);
        assertEquals(0.5d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(0.548)), 0.001d);

        assertEquals(-1.9d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(-1.9d)), 0.001d);
        assertEquals(1.9d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(1.9d)), 0.001d);
        assertEquals(0d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(0)), 0.001d);
        assertEquals(0d, TemperatureOffsetService.sanitizeOffsetValue(BigDecimal.valueOf(0d)), 0.001d);
    }
}
