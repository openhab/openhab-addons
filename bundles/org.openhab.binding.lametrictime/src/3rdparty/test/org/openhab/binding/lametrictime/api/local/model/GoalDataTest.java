/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
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
package org.openhab.binding.lametrictime.api.local.model;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.lametrictime.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.api.test.AbstractTest;

import com.google.gson.Gson;

public class GoalDataTest extends AbstractTest
{
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeSimple() throws Exception
    {
        GoalData goalData = new GoalData().withStart(0).withEnd(100).withCurrent(50).withUnit("%");
        assertEquals(readJson("goal-data.json"), gson.toJson(goalData));
    }

    @Test
    public void testDeserializeSimple() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("goal-data.json")))
        {
            GoalData goalData = gson.fromJson(reader, GoalData.class);
            assertEquals(Integer.valueOf(0), goalData.getStart());
            assertEquals(Integer.valueOf(100), goalData.getEnd());
            assertEquals(Integer.valueOf(50), goalData.getCurrent());
            assertEquals("%", goalData.getUnit());
        }
    }
}
