/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class PauseModeTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        PauseMode pauseMode = new PauseMode().withAccountId("-8888888888888888888").withBedId("-9999999999999999999")
                .withPauseMode("off");
        assertEquals(readJson("pause-mode.json"), gson.toJson(pauseMode));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("pause-mode.json"))) {
            PauseMode pauseMode = gson.fromJson(reader, PauseMode.class);
            assertEquals("-8888888888888888888", pauseMode.getAccountId());
            assertEquals("-9999999999999999999", pauseMode.getBedId());
            assertEquals("off", pauseMode.getPauseMode());
        }
    }
}
