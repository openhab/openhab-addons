/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.exception.CalculationParameterException;

/**
 * The {@link TestParameterConversions} tests the conversion of price calculation parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TestParameterConversions {

    @Test
    void testParameterConversion() throws CalculationParameterException {
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
        assertTrue(duration instanceof Long, "Duration is Integer");
    }

    @Test
    void testMapToMapConversion() throws CalculationParameterException {
        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_CURVE, List.of(new CurveEntry(1000, 60)));
        Map<String, Object> convertedParams = new HashMap<>();
        Utils.convertParameters(params, convertedParams);
        List<?> l = (List<?>) convertedParams.get(PARAM_CURVE);
        assertNotNull(l);
        assertTrue(l.get(0) instanceof CurveEntry);
    }
}
