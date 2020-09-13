/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.model;

import static org.junit.Assert.assertEquals;

import java.io.FileReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.model.Bed;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class BedTest extends AbstractTest
{
    private static Gson gson;

    @BeforeClass
    public static void setUpBeforeClass()
    {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception
    {
        Bed bed = new Bed().withAccountId("-8888888888888888888")
                           .withBase("MODULAR")
                           .withBedId("-9999999999999999999")
                           .withDualSleep(true)
                           .withKidsBed(false)
                           .withMacAddress("AABBCCDDEEFF")
                           .withModel("P5")
                           .withName("Bed")
                           .withPurchaseDate(ZonedDateTime.of(2017,
                                                              2,
                                                              2,
                                                              0,
                                                              0,
                                                              1,
                                                              0,
                                                              ZoneId.of("Z").normalized()))
                           .withReference("55555555555-5")
                           .withRegistrationDate(ZonedDateTime.of(2017,
                                                                  2,
                                                                  17,
                                                                  2,
                                                                  14,
                                                                  10,
                                                                  0,
                                                                  ZoneId.of("Z").normalized()))
                           .withReturnRequestStatus(0L)
                           .withSerial("")
                           .withSize("QUEEN")
                           .withSku("QP5")
                           .withSleeperLeftId("-2222222222222222222")
                           .withSleeperRightId("-1111111111111111111")
                           .withStatus(1L)
                           .withTimezone("US/Pacific")
                           .withVersion("")
                           .withZipCode("90210");
        assertEquals(readJson("bed.json"), gson.toJson(bed));
    }

    @Test
    public void testDeserializeAllFields() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("bed.json")))
        {
            Bed bed = gson.fromJson(reader, Bed.class);
            assertEquals("-8888888888888888888", bed.getAccountId());
            assertEquals("MODULAR", bed.getBase());
            assertEquals("-9999999999999999999", bed.getBedId());
            assertEquals(true, bed.isDualSleep());
            assertEquals(false, bed.isKidsBed());
            assertEquals("AABBCCDDEEFF", bed.getMacAddress());
            assertEquals("P5", bed.getModel());
            assertEquals("Bed", bed.getName());
            assertEquals(ZonedDateTime.of(2017, 2, 2, 0, 0, 1, 0, ZoneId.of("Z").normalized()),
                         bed.getPurchaseDate());
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
