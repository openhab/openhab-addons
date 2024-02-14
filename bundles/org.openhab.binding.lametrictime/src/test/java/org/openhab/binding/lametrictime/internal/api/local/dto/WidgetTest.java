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
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.internal.api.test.AbstractTest;

import com.google.gson.Gson;
import com.google.gson.JsonPrimitive;

/**
 * widget test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class WidgetTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    @SuppressWarnings("serial")
    public void testSerialize() throws Exception {
        Widget widget = new Widget().withPackageName("com.lametric.radio").withIndex(Integer.valueOf(-1))
                .withSettings(new HashMap<>() {
                    {
                        put("_title", new JsonPrimitive("Radio"));
                    }
                });
        assertEquals(readJson("widget.json"), gson.toJson(widget));
    }

    @Test
    @SuppressWarnings("serial")
    public void testDeserialize() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("widget.json"))) {
            Widget widget = gson.fromJson(reader, Widget.class);
            assertEquals("com.lametric.radio", widget.getPackageName());
            assertEquals(Integer.valueOf(-1), widget.getIndex());
            assertEquals(new HashMap<String, JsonPrimitive>() {
                {
                    put("_title", new JsonPrimitive("Radio"));
                }
            }, widget.getSettings());
        }
    }
}
