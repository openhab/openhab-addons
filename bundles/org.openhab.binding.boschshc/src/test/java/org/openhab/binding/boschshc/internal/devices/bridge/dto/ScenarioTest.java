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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Scenario}.
 * 
 * @author David Pace - Initial contribution
 *
 */
class ScenarioTest {

    private Scenario fixture;

    @BeforeEach
    protected void setUp() throws Exception {
        fixture = Scenario.createScenario("abc", "My Scenario", "1708845918493");
    }

    @Test
    void isValid() {
        assertTrue(Scenario.isValid(new Scenario[] { fixture }));
        assertFalse(Scenario.isValid(new Scenario[] { fixture, new Scenario() }));
    }

    @Test
    void testToString() {
        assertEquals("Scenario{name='My Scenario', id='abc', lastTimeTriggered='1708845918493'}", fixture.toString());
    }
}
