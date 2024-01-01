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
package org.openhab.binding.lametrictime.internal.api.local.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.internal.api.test.AbstractTest;

import com.google.gson.Gson;

/**
 * bluetooth test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class BluetoothTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        Bluetooth bluetooth = new Bluetooth().withActive(false).withAvailable(true).withDiscoverable(false)
                .withMac("AA:AA:AA:AA:AA:AA").withName("LM9999").withPairable(true);
        assertEquals(readJson("bluetooth-mac-address.json"), gson.toJson(bluetooth));
    }

    @Test
    public void testDeserializeMac() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("bluetooth-mac.json"))) {
            Bluetooth bluetooth = gson.fromJson(reader, Bluetooth.class);
            assertEquals(false, bluetooth.isActive());
            assertEquals(true, bluetooth.isAvailable());
            assertEquals(false, bluetooth.isDiscoverable());
            assertEquals("AA:AA:AA:AA:AA:AA", bluetooth.getMac());
            assertEquals("LM9999", bluetooth.getName());
            assertEquals(true, bluetooth.isPairable());
        }
    }

    @Test
    public void testDeserializeAddress() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("bluetooth-address.json"))) {
            Bluetooth bluetooth = gson.fromJson(reader, Bluetooth.class);
            assertEquals(false, bluetooth.isActive());
            assertEquals(true, bluetooth.isAvailable());
            assertEquals(false, bluetooth.isDiscoverable());
            assertEquals("AA:AA:AA:AA:AA:AA", bluetooth.getMac());
            assertEquals("LM9999", bluetooth.getName());
            assertEquals(true, bluetooth.isPairable());
        }
    }
}
