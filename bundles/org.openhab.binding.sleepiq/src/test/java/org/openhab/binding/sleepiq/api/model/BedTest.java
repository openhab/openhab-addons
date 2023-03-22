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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.test.AbstractTest;
import org.openhab.binding.sleepiq.internal.api.dto.Bed;
import org.openhab.binding.sleepiq.internal.api.impl.GsonGenerator;

import com.google.gson.Gson;

/**
 * The {@link BedTest} tests deserialization of a bed object.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class BedTest extends AbstractTest {
    private static Gson gson = GsonGenerator.create(true);

    @Test
    public void testSerializeAllFields() throws Exception {
        Bed bed = new Bed().withAccountId("-8888888888888888888").withBase("MODULAR").withBedId("-9999999999999999999")
                .withDualSleep(true).withKidsBed(false).withMacAddress("AABBCCDDEEFF").withModel("P5").withName("Bed")
                .withPurchaseDate(ZonedDateTime.of(2017, 2, 2, 0, 0, 1, 0, ZoneId.of("Z").normalized()))
                .withReference("55555555555-5")
                .withRegistrationDate(ZonedDateTime.of(2017, 2, 17, 2, 14, 10, 0, ZoneId.of("Z").normalized()))
                .withReturnRequestStatus(0L).withSerial("").withSize("QUEEN").withSku("QP5")
                .withSleeperLeftId("-2222222222222222222").withSleeperRightId("-1111111111111111111").withStatus(1L)
                .withTimezone("US/Pacific").withVersion("").withZipCode("90210");
        assertEquals(readJson("bed.json"), gson.toJson(bed));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("bed.json"))) {
            Bed bed = gson.fromJson(reader, Bed.class);
            assertEquals("-8888888888888888888", bed.getAccountId());
            assertEquals("MODULAR", bed.getBase());
            assertEquals("-9999999999999999999", bed.getBedId());
            assertEquals(true, bed.isDualSleep());
            assertEquals(false, bed.isKidsBed());
            assertEquals("AABBCCDDEEFF", bed.getMacAddress());
            assertEquals("P5", bed.getModel());
            assertEquals("Bed", bed.getName());
            assertEquals(ZonedDateTime.of(2017, 2, 2, 0, 0, 1, 0, ZoneId.of("Z").normalized()), bed.getPurchaseDate());
            assertEquals("55555555555-5", bed.getReference());
            assertEquals(ZonedDateTime.of(2017, 2, 17, 2, 14, 10, 0, ZoneId.of("Z").normalized()),
                    bed.getRegistrationDate());
            assertEquals(Long.valueOf(0L), bed.getReturnRequestStatus());
            assertEquals("", bed.getSerial());
            assertEquals("QUEEN", bed.getSize());
            assertEquals("QP5", bed.getSku());
            assertEquals("-2222222222222222222", bed.getSleeperLeftId());
            assertEquals("-1111111111111111111", bed.getSleeperRightId());
            assertEquals(Long.valueOf(1L), bed.getStatus());
            assertEquals("US/Pacific", bed.getTimezone());
            assertEquals("", bed.getVersion());
            assertEquals("90210", bed.getZipCode());
        }
    }
}
