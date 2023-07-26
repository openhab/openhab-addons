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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileReader;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.lametrictime.internal.api.common.impl.GsonGenerator;
import org.openhab.binding.lametrictime.internal.api.test.AbstractTest;

import com.google.gson.Gson;

/**
 * update action test.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class UpdateActionTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    @SuppressWarnings("serial")
    public void testSerialize() throws Exception {
        UpdateAction action = new UpdateAction().withId("countdown.configure")
        // @formatter:off
                                                .withParameters(new TreeMap<String, Parameter>(){{put("duration", new IntegerParameter().withValue(30));}});
                                                // @formatter:on
        assertEquals(readJson("update-action.json"), gson.toJson(action));
    }

    @Test
    public void testDeserialize() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("update-action.json"))) {
            assertThrows(UnsupportedOperationException.class, () -> gson.fromJson(reader, UpdateAction.class));
        }
    }
}
