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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class BedSideStatusTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        BedSideStatus bedSideStatus = new BedSideStatus().withAlertDetailedMessage("No Alert").withAlertId(0L)
                .withInBed(false).withLastLink(new TimeSince().withDuration(3, 5, 4, 38)).withPressure(573)
                .withSleepNumber(55);
        assertEquals(readJson("bed-side-status.json"), gson.toJson(bedSideStatus));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("bed-side-status.json"))) {
            BedSideStatus bedSideStatus = gson.fromJson(reader, BedSideStatus.class);
            assertEquals("No Alert", bedSideStatus.getAlertDetailedMessage());
            assertEquals(Long.valueOf(0L), bedSideStatus.getAlertId());
            assertFalse(bedSideStatus.isInBed());
            assertEquals(new TimeSince().withDuration(3, 5, 4, 38), bedSideStatus.getLastLink());
            assertEquals(Integer.valueOf(573), bedSideStatus.getPressure());
            assertEquals(Integer.valueOf(55), bedSideStatus.getSleepNumber());
        }
    }
}
