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
 * audio test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class AudioTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        Audio audio = new Audio().withVolume(42);
        assertEquals(readJson("audio.json"), gson.toJson(audio));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("audio.json"))) {
            Audio audio = gson.fromJson(reader, Audio.class);
            assertEquals(Integer.valueOf(42), audio.getVolume());
        }
    }
}
