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

import java.io.FileReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.internal.api.test.AbstractTest;

import com.google.gson.Gson;

/**
 * goal data test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class GoalDataTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeSimple() throws Exception {
        GoalData goalData = new GoalData().withStart(0).withEnd(100).withCurrent(50).withUnit("%");
        assertEquals(readJson("goal-data.json"), gson.toJson(goalData));
    }

    @Test
    public void testDeserializeSimple() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("goal-data.json"))) {
            GoalData goalData = gson.fromJson(reader, GoalData.class);
            assertEquals(Integer.valueOf(0), goalData.getStart());
            assertEquals(Integer.valueOf(100), goalData.getEnd());
            assertEquals(Integer.valueOf(50), goalData.getCurrent());
            assertEquals("%", goalData.getUnit());
        }
    }
}
