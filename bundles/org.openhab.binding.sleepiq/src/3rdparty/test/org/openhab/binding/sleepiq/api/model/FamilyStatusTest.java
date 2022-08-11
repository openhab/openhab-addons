/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.api.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class FamilyStatusTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        FamilyStatus familyStatus = new FamilyStatus().withBeds(Arrays.asList(new BedStatus().withStatus(1L)));
        assertEquals(readJson("family-status.json"), gson.toJson(familyStatus));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("family-status.json"))) {
            FamilyStatus familyStatus = gson.fromJson(reader, FamilyStatus.class);

            List<BedStatus> beds = familyStatus.getBeds();
            assertNotNull(beds);
            assertEquals(1, beds.size());
            assertEquals(Long.valueOf(1L), beds.get(0).getStatus());
        }
    }
}
