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
package org.openhab.binding.smartthings.internal.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for dynamic SmartThings type UID helpers.
 */
@NonNullByDefault
class SmartThingsTypeRegistryImplTest {

    @Test
    void getChannelGroupIdMatchesDynamicTypeGeneration() {
        assertEquals("Sound_Bar_main_audioVolume",
                SmartThingsTypeRegistryImpl.getChannelGroupId("Sound_Bar", "main", "audioVolume"));
    }

    @Test
    void getChannelGroupIdIncludesCapabilityNamespace() {
        assertEquals("Sound_Bar_main_custom_soundmode",
                SmartThingsTypeRegistryImpl.getChannelGroupId("Sound_Bar", "main", "custom.soundmode"));
    }
}
