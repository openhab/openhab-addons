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
package org.openhab.binding.dreame.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests mower handler update ordering.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameMowerHandlerTest {

    @Test
    void parsesAndDeduplicatesZoneIds() {
        assertEquals(java.util.List.of(1, 2), DreameMowerHandler.parseZoneIds("1, 2,1"));
        assertThrows(IllegalArgumentException.class, () -> DreameMowerHandler.parseZoneIds("0"));
        assertThrows(NumberFormatException.class, () -> DreameMowerHandler.parseZoneIds("garden"));
    }

    @Test
    void rejectsOnlyRestPropertiesSupersededByMqtt() {
        assertTrue(DreameMowerHandler.isPollPropertyCurrent(10, null));
        assertTrue(DreameMowerHandler.isPollPropertyCurrent(10, 10L));
        assertFalse(DreameMowerHandler.isPollPropertyCurrent(10, 11L));
    }
}
