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
package org.openhab.binding.bluetooth.generic.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link GenericBluetoothHandlerFactory#resolveGattExtensionsFolder(Map, String)}.
 *
 * @author Vlad Kolotoff - Initial contribution
 */
@NonNullByDefault
class GattExtensionsFolderTest {

    private static final String CONF = "/etc/openhab";

    @Test
    void defaultsToGattExtensionsUnderConfigFolderWhenUnset() {
        File folder = GenericBluetoothHandlerFactory.resolveGattExtensionsFolder(Map.of(), CONF);
        assertEquals(new File(CONF, "gatt-extensions"), folder);
    }

    @Test
    void defaultsWhenValueIsBlank() {
        File folder = GenericBluetoothHandlerFactory.resolveGattExtensionsFolder(Map.of("gattExtensionsFolder", "   "),
                CONF);
        assertEquals(new File(CONF, "gatt-extensions"), folder);
    }

    @Test
    void resolvesRelativeLocationAgainstConfigFolder() {
        File folder = GenericBluetoothHandlerFactory
                .resolveGattExtensionsFolder(Map.of("gattExtensionsFolder", "my-gatt"), CONF);
        assertEquals(new File(CONF, "my-gatt"), folder);
    }

    @Test
    void trimsAndResolvesRelativeLocation() {
        File folder = GenericBluetoothHandlerFactory
                .resolveGattExtensionsFolder(Map.of("gattExtensionsFolder", "  my-gatt  "), CONF);
        assertEquals(new File(CONF, "my-gatt"), folder);
    }

    @Test
    void honoursAbsoluteLocationAsIs() {
        String absolute = new File("/opt/custom-gatt").getAbsolutePath();
        File folder = GenericBluetoothHandlerFactory
                .resolveGattExtensionsFolder(Map.of("gattExtensionsFolder", absolute), CONF);
        assertEquals(new File(absolute), folder);
    }
}
