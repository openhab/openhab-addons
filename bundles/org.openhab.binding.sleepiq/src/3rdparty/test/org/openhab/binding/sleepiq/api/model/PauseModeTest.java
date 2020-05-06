/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.model;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.model.PauseMode;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class PauseModeTest extends AbstractTest
{
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception
    {
        PauseMode pauseMode = new PauseMode().withAccountId("-8888888888888888888")
                                             .withBedId("-9999999999999999999")
                                             .withPauseMode("off");
        assertEquals(readJson("pause-mode.json"), gson.toJson(pauseMode));
    }

    @Test
    public void testDeserializeAllFields() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("pause-mode.json")))
        {
            PauseMode pauseMode = gson.fromJson(reader, PauseMode.class);
            assertEquals("-8888888888888888888", pauseMode.getAccountId());
            assertEquals("-9999999999999999999", pauseMode.getBedId());
            assertEquals("off", pauseMode.getPauseMode());
        }
    }
}
