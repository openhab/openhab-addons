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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileReader;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.internal.api.test.AbstractTest;

import com.google.gson.Gson;

/**
 * application test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class ApplicationTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    @SuppressWarnings("serial")
    public void testSerializeAllFields() throws Exception {
        Application app = new Application().withPackageName("com.lametric.radio").withVendor("LaMetric")
                .withVersion("1.0.10").withVersionCode("22")
                // @formatter:off
                                           .withWidgets(new TreeMap<>(){{put("589ed1b3fcdaa5180bf4848e55ba8061", new Widget());}})
                                           .withActions(new TreeMap<>(){{put("radio.next", new Action());
                                                                                       put("radio.play", new Action());
                                                                                       put("radio.prev", new Action());
                                                                                       put("radio.stop", new Action());}});
                                           // @formatter:on
        assertEquals(readJson("application-all.json"), gson.toJson(app));
    }

    @Test
    public void testSerializeNullLists() throws Exception {
        Application app = new Application().withPackageName("com.lametric.radio").withVendor("LaMetric")
                .withVersion("1.0.10").withVersionCode("22");
        assertEquals(readJson("application-null-maps.json"), gson.toJson(app));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("application-all.json"))) {
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
    public void testDeserializeNullLists() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("application-null-maps.json"))) {
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
