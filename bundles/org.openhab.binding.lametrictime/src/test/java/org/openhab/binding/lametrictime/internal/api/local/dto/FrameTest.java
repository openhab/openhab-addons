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
import java.util.Arrays;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.internal.api.test.AbstractTest;

import com.google.gson.Gson;

/**
 * frame test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class FrameTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeSimple() throws Exception {
        Frame frame = new Frame().withIcon("i87").withText("Hello world!");
        assertEquals(readJson("frame-simple.json"), gson.toJson(frame));
    }

    @Test
    public void testSerializeGoal() throws Exception {
        Frame frame = new Frame().withIcon("i120").withGoalData(new GoalData());
        assertEquals(readJson("frame-goal.json"), gson.toJson(frame));
    }

    @Test
    public void testSerializeChart() throws Exception {
        Frame frame = new Frame().withChartData(Arrays.asList(1, 2, 3, 4, 5, 6, 7));
        assertEquals(readJson("frame-chart.json"), gson.toJson(frame));
    }

    @Test
    public void testDeserializeSimple() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("frame-simple.json"))) {
            Frame frame = gson.fromJson(reader, Frame.class);
            assertEquals("i87", frame.getIcon());
            assertEquals("Hello world!", frame.getText());
            assertEquals(null, frame.getGoalData());
            assertEquals(null, frame.getChartData());
        }
    }

    @Test
    public void testDeserializeGoal() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("frame-goal.json"))) {
            Frame frame = gson.fromJson(reader, Frame.class);
            assertEquals("i120", frame.getIcon());
            assertEquals(new GoalData(), frame.getGoalData());
        }
    }

    @Test
    public void testDeserializeChart() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("frame-chart.json"))) {
            Frame frame = gson.fromJson(reader, Frame.class);
            assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7), frame.getChartData());
        }
    }
}
