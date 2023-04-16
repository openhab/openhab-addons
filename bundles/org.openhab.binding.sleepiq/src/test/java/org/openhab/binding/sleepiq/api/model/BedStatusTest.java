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
package org.openhab.binding.sleepiq.api.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.test.AbstractTest;
import org.openhab.binding.sleepiq.internal.api.dto.BedSideStatus;
import org.openhab.binding.sleepiq.internal.api.dto.BedStatus;
import org.openhab.binding.sleepiq.internal.api.impl.GsonGenerator;

import com.google.gson.Gson;

/**
 * The {@link BedStatusText} tests deserialization of a bed status object.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class BedStatusTest extends AbstractTest {
    private static Gson gson = GsonGenerator.create(true);

    @Test
    public void testSerializeAllFields() throws Exception {
        BedStatus bedStatus = new BedStatus().withBedId("-9999999999999999999")
                .withLeftSide(new BedSideStatus().withInBed(true)).withRightSide(new BedSideStatus().withInBed(false))
                .withStatus(1L);
        assertEquals(readJson("bed-status.json"), gson.toJson(bedStatus));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("bed-status.json"))) {
            BedStatus bedStatus = gson.fromJson(reader, BedStatus.class);
            assertEquals("-9999999999999999999", bedStatus.getBedId());
            assertEquals(Long.valueOf(1L), bedStatus.getStatus());

            BedSideStatus leftSide = bedStatus.getLeftSide();
            assertNotNull(leftSide);
            assertTrue(leftSide.isInBed());

            BedSideStatus rightSide = bedStatus.getRightSide();
            assertNotNull(rightSide);
            assertFalse(rightSide.isInBed());
        }
    }
}
