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
package org.openhab.binding.tibber.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.calculator.CurveEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;

/**
 * The {@link TestParameterConversions} checks the conversion of price calculation parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestParameterConversions {

    @Test
    void testParseDuration() {
        int result = Utils.parseDuration("5");
        assertEquals(5, result, "Parse without unit");
        result = Utils.parseDuration("5 s");
        assertEquals(5, result, "Parse with s unit");
        result = Utils.parseDuration("5 m");
        assertEquals(5 * 60, result, "Parse with m unit");
        result = Utils.parseDuration("5 h");
        assertEquals(5 * 60 * 60, result, "Parse with h unit");
        result = Utils.parseDuration("1 h 3 s");
        assertEquals(1 * 60 * 60 + 3, result, "Parse with h and s unit");
        result = Utils.parseDuration("1 h 5 m 3 s");
        assertEquals(1 * 60 * 60 + 5 * 60 + 3, result, "Parse with h, m and s unit");
        try {
            result = Utils.parseDuration("1h5d3s");
            fail("Wrong input parsed correctly");
        } catch (CalculationParameterException cpe) {
            String message = cpe.getMessage();
            assertNotNull(message);
            assertTrue(message.startsWith("Cannot decode"));
        }
    }

    @Test
    void testParameterConversion() {
        Instant now = Instant.now();
        String json = "{\"earliestStart\":\"" + now.toString() + "\"}";
        Map<String, Object> params = new HashMap<>();
        assertTrue(Utils.convertParameters(json, params), "Parameters are JSON format");
        Object eraliestStart = params.get(PARAM_EARLIEST_START);
        assertTrue(eraliestStart instanceof Instant, "EarliestStart is Instant");

        params.clear();
        json = "{\"earliestStart\":\"" + now.toString() + "\",\"duration\":73}";
        assertTrue(Utils.convertParameters(json, params), "Parameters are JSON format");
        Object duration = params.get(PARAM_DURATION);
        assertTrue(duration instanceof Integer, "Duration is Integer");
    }

    @Test
    void testMapToMapConversion() {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_CURVE, List.of(new CurveEntry(1000, 60)));
        Map<String, Object> convertedParams = new HashMap<>();
        Utils.convertParameters(params, convertedParams);
        List l = (List) convertedParams.get(PARAM_CURVE);
        assertTrue(l.get(0) instanceof CurveEntry);
    }
}
