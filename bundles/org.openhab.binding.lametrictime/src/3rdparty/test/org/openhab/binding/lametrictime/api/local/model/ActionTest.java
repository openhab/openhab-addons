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

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.lametrictime.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.api.test.AbstractTest;

import com.google.gson.Gson;

public class ActionTest extends AbstractTest
{
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        gson = GsonGenerator.create(true);
    }

    @Test
    @SuppressWarnings("serial")
    public void testSerialize() throws Exception
    {
        // @formatter:off
        Action action = new Action().withParameters(new TreeMap<String, Parameter>(){{put("enabled", new BooleanParameter());
                                                                                      put("time", new StringParameter());}});
        // @formatter:on
        assertEquals(readJson("action.json"), gson.toJson(action));
    }

    @Test
    public void testDeserialize() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("action.json")))
        {
            Action action = gson.fromJson(reader, Action.class);
            SortedMap<String, Parameter> parameters = action.getParameters();
            assertNotNull(parameters);
            assertEquals(2, parameters.size());

            Iterator<String> parametersIter = parameters.keySet().iterator();
            assertEquals("enabled", parametersIter.next());
            assertEquals("time", parametersIter.next());
        }
    }
}
