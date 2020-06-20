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

public class ApplicationTest extends AbstractTest
{
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        gson = GsonGenerator.create(true);
    }

    @Test
    @SuppressWarnings("serial")
    public void testSerializeAllFields() throws Exception
    {
        Application app = new Application().withPackageName("com.lametric.radio")
                                           .withVendor("LaMetric")
                                           .withVersion("1.0.10")
                                           .withVersionCode("22")
                                           // @formatter:off
                                           .withWidgets(new TreeMap<String, Widget>(){{put("589ed1b3fcdaa5180bf4848e55ba8061", new Widget());}})
                                           .withActions(new TreeMap<String, Action>(){{put("radio.next", new Action());
                                                                                       put("radio.play", new Action());
                                                                                       put("radio.prev", new Action());
                                                                                       put("radio.stop", new Action());}});
                                           // @formatter:on
        assertEquals(readJson("application-all.json"), gson.toJson(app));
    }

    @Test
    public void testSerializeNullLists() throws Exception
    {
        Application app = new Application().withPackageName("com.lametric.radio")
                                           .withVendor("LaMetric")
                                           .withVersion("1.0.10")
                                           .withVersionCode("22");
        assertEquals(readJson("application-null-maps.json"), gson.toJson(app));
    }

    @Test
    public void testDeserializeAllFields() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("application-all.json")))
        {
            Application app = gson.fromJson(reader, Application.class);
            assertEquals("com.lametric.radio", app.getPackageName());
            assertEquals("LaMetric", app.getVendor());
            assertEquals("1.0.10", app.getVersion());
            assertEquals("22", app.getVersionCode());

            SortedMap<String, Widget> widgets = app.getWidgets();
            assertNotNull(widgets);
            assertEquals(1, widgets.size());
            assertEquals("589ed1b3fcdaa5180bf4848e55ba8061", widgets.keySet().iterator().next());

            SortedMap<String, Action> actions = app.getActions();
            assertNotNull(actions);
            assertEquals(4, actions.size());

            Iterator<String> actionsIter = actions.keySet().iterator();
            assertEquals("radio.next", actionsIter.next());
            assertEquals("radio.play", actionsIter.next());
            assertEquals("radio.prev", actionsIter.next());
            assertEquals("radio.stop", actionsIter.next());
        }
    }

    @Test
    public void testDeserializeNullLists() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("application-null-maps.json")))
        {
            Application app = gson.fromJson(reader, Application.class);
            assertEquals("com.lametric.radio", app.getPackageName());
            assertEquals("LaMetric", app.getVendor());
            assertEquals("1.0.10", app.getVersion());
            assertEquals("22", app.getVersionCode());
            assertNull(app.getWidgets());
            assertNull(app.getActions());
        }
    }
}
