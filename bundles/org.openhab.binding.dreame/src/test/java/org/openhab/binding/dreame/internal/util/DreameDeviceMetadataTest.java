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
package org.openhab.binding.dreame.internal.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.core.thing.Thing;

/**
 * Verifies runtime synchronization of cloud device metadata.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
class DreameDeviceMetadataTest {

    @Test
    void updatesModelAndFirmwareWhenCloudMetadataChanges() {
        Map<String, String> properties = new HashMap<>(
                Map.of("model", "dreame.mower.old", Thing.PROPERTY_FIRMWARE_VERSION, "1.0"));
        DreameDevice device = new DreameDevice("device", "Mower", "dreame.mower.new", "2.0", "", "", "");

        assertTrue(DreameDeviceMetadata.updateProperties(properties, device));
        assertEquals("dreame.mower.new", properties.get("model"));
        assertEquals("2.0", properties.get(Thing.PROPERTY_FIRMWARE_VERSION));
        assertFalse(DreameDeviceMetadata.updateProperties(properties, device));
    }

    @Test
    void retainsKnownMetadataWhenCloudFieldsAreTemporarilyEmpty() {
        Map<String, String> properties = new HashMap<>(
                Map.of("model", "dreame.vacuum.test", Thing.PROPERTY_FIRMWARE_VERSION, "1.0"));
        DreameDevice device = new DreameDevice("device", "Vacuum", "", "", "", "", "");

        assertFalse(DreameDeviceMetadata.updateProperties(properties, device));
        assertEquals("dreame.vacuum.test", properties.get("model"));
        assertEquals("1.0", properties.get(Thing.PROPERTY_FIRMWARE_VERSION));
    }
}
