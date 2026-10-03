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
package org.openhab.io.eebus.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link EEBusSettings#requiresRestart}.
 */
@NonNullByDefault
class EEBusSettingsTest {

    @Test
    void unchangedSettingsDoNotRestart() {
        assertFalse(new EEBusSettings().requiresRestart(new EEBusSettings()));
    }

    @Test
    void trustedSkiChangesRestart() {
        EEBusSettings withSki = new EEBusSettings();
        withSki.trustedSkis = "4d5a3c";
        assertTrue(new EEBusSettings().requiresRestart(withSki));
        assertTrue(withSki.requiresRestart(new EEBusSettings()));
    }

    @Test
    void connectPolicyAppliesWithoutRestart() {
        EEBusSettings policy = new EEBusSettings();
        policy.connectPolicy = "NONE";
        assertFalse(new EEBusSettings().requiresRestart(policy));
    }
}
