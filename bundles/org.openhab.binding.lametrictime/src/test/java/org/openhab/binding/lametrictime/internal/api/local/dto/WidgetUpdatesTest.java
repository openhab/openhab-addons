/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.internal.api.test.AbstractTest;

import com.google.gson.Gson;

/**
 * widget updates test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class WidgetUpdatesTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerialize() throws Exception {
        WidgetUpdates widgetUpdates = new WidgetUpdates()
                .withFrames(Arrays.asList(new Frame().withIcon("i120").withText("12°").withIndex(0)));

        assertEquals(readJson("widget-updates.json"), gson.toJson(widgetUpdates));
    }

    @Test
    public void testDeserialize() throws Exception {
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(getTestDataFile("widget-updates.json")), StandardCharsets.UTF_8)) {
            WidgetUpdates widgetUpdates = gson.fromJson(reader, WidgetUpdates.class);
            assertEquals("i120", widgetUpdates.getFrames().get(0).getIcon());
            assertEquals("12°", widgetUpdates.getFrames().get(0).getText());
            assertEquals(null, widgetUpdates.getFrames().get(0).getGoalData());
            assertEquals(null, widgetUpdates.getFrames().get(0).getChartData());
        }
    }
}
