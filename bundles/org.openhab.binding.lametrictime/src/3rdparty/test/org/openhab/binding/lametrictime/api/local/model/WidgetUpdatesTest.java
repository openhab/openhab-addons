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
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.lametrictime.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.api.test.AbstractTest;

import com.google.gson.Gson;

public class WidgetUpdatesTest extends AbstractTest
{
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerialize() throws Exception
    {
        WidgetUpdates widgetUpdates = new WidgetUpdates().withFrames(Arrays.asList(new Frame().withIcon("i120")
                                                                                              .withText("12°")
                                                                                              .withIndex(0)));

        assertEquals(readJson("widget-updates.json"), gson.toJson(widgetUpdates));
    }

    @Test
    public void testDeserialize() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("widget-updates.json")))
        {
            WidgetUpdates widgetUpdates = gson.fromJson(reader, WidgetUpdates.class);
            assertEquals("i120", widgetUpdates.getFrames().get(0).getIcon());
            assertEquals("12°", widgetUpdates.getFrames().get(0).getText());
            assertEquals(null, widgetUpdates.getFrames().get(0).getGoalData());
            assertEquals(null, widgetUpdates.getFrames().get(0).getChartData());
        }
    }
}
