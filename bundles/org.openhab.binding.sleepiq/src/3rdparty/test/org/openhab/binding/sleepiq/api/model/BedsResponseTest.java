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
import static org.junit.Assert.assertNotNull;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.model.Bed;
import org.openhab.binding.sleepiq.api.model.BedsResponse;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class BedsResponseTest extends AbstractTest
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
        BedsResponse bedsResponse = new BedsResponse().withBeds(Arrays.asList(new Bed().withName("Bed1"),
                                                                              new Bed().withName("Bed2")));
        assertEquals(readJson("beds-response.json"), gson.toJson(bedsResponse));
    }

    @Test
    public void testDeserializeAllFields() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("beds-response.json")))
        {
            BedsResponse bedsResponse = gson.fromJson(reader, BedsResponse.class);

            List<Bed> beds = bedsResponse.getBeds();
            assertNotNull(beds);
            assertEquals(2, beds.size());
            assertEquals("Bed1", beds.get(0).getName());
            assertEquals("Bed2", beds.get(1).getName());
        }
    }
}
