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
package org.openhab.binding.boschshc.internal.services.temperatureoffset.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;

/**
 * Unit tests for {@link TemperatureOffsetServiceState}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class TemperatureOffsetServiceStateTest {
    @Test
    void testSerialization() {
        TemperatureOffsetServiceState state = new TemperatureOffsetServiceState();
        String json = GsonUtils.DEFAULT_GSON_INSTANCE.toJson(state);
        assertTrue(json.contains("offset"));
        assertTrue(json.contains("0.0"));

        // make sure the nullable Double objects are not serialized
        assertFalse(json.contains("stepSize"));
        assertFalse(json.contains("minOffset"));
        assertFalse(json.contains("maxOffset"));
    }
}
