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
package org.openhab.binding.remehaheating.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RemehaHeatingConfiguration}.
 *
 * @author Michael Fraedrich - Initial contribution
 */
public class RemehaHeatingConfigurationTest {

    @Test
    public void testDefaultValues() {
        RemehaHeatingConfiguration config = new RemehaHeatingConfiguration();

        assertEquals("", config.email);
        assertEquals("", config.password);
        assertEquals(60, config.refreshInterval);
    }

    @Test
    public void testConfigurationValues() {
        RemehaHeatingConfiguration config = new RemehaHeatingConfiguration();

        config.email = "test@example.com";
        config.password = "testpassword";
        config.refreshInterval = 120;

        assertEquals("test@example.com", config.email);
        assertEquals("testpassword", config.password);
        assertEquals(120, config.refreshInterval);
    }
}
