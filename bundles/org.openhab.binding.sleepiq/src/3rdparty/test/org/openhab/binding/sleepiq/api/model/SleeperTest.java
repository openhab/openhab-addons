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

import org.junit.BeforeClass;
import org.junit.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.model.Sleeper;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class SleeperTest extends AbstractTest
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
        Sleeper sleeper = new Sleeper().withAccountId("-5555555555555555555")
                                       .withAccountOwner(true)
                                       .withActive(true)
                                       .withAvatar("")
                                       .withBedId("-9999999999999999999")
                                       .withBirthMonth(6)
                                       .withBirthYear("1970")
                                       .withChild(false)
                                       .withDuration("")
                                       .withEmail("alice@domain.com")
                                       .withEmailValidated(true)
                                       .withFirstName("Alice")
                                       .withHeight(64)
                                       .withLastLogin("2017-02-17 20:19:36 CST")
                                       .withLicenseVersion(6L)
                                       .withMale(false)
                                       .withSide(1)
                                       .withSleeperId("-1111111111111111111")
                                       .withSleepGoal(450)
                                       .withTimezone("US/Pacific")
                                       .withUsername("alice@domain.com")
                                       .withWeight(110)
                                       .withZipCode("90210");
        assertEquals(readJson("sleeper.json"), gson.toJson(sleeper));
    }

    @Test
    public void testSerializeLastLoginNull() throws Exception
    {
        Sleeper sleeper = new Sleeper().withLastLogin("null");
        assertEquals(readJson("sleeper-lastlogin-null.json"), gson.toJson(sleeper));
    }

    @Test
    public void testDeserializeAllFields() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("sleeper.json")))
        {
            Sleeper sleeper = gson.fromJson(reader, Sleeper.class);
            assertEquals("-5555555555555555555", sleeper.getAccountId());
            assertEquals(true, sleeper.isAccountOwner());
            assertEquals(true, sleeper.isActive());
            assertEquals("", sleeper.getAvatar());
            assertEquals("-9999999999999999999", sleeper.getBedId());
            assertEquals(Integer.valueOf(6), sleeper.getBirthMonth());
            assertEquals("1970", sleeper.getBirthYear());
            assertEquals(false, sleeper.isChild());
            assertEquals("", sleeper.getDuration());
            assertEquals("alice@domain.com", sleeper.getEmail());
            assertEquals(true, sleeper.isEmailValidated());
            assertEquals("Alice", sleeper.getFirstName());
            assertEquals(Integer.valueOf(64), sleeper.getHeight());
            assertEquals("2017-02-17 20:19:36 CST", sleeper.getLastLogin());
            assertEquals(Long.valueOf(6L), sleeper.getLicenseVersion());
            assertEquals(false, sleeper.isMale());
            assertEquals(Integer.valueOf(1), sleeper.getSide());
            assertEquals("-1111111111111111111", sleeper.getSleeperId());
            assertEquals(Integer.valueOf(450), sleeper.getSleepGoal());
            assertEquals("US/Pacific", sleeper.getTimezone());
            assertEquals("alice@domain.com", sleeper.getUsername());
            assertEquals(Integer.valueOf(110), sleeper.getWeight());
            assertEquals("90210", sleeper.getZipCode());
        }
    }

    @Test
    public void testDeserializeLastLoginNull() throws Exception
    {
        try (FileReader reader = new FileReader(getTestDataFile("sleeper-lastlogin-null.json")))
        {
            Sleeper sleeper = gson.fromJson(reader, Sleeper.class);
            assertEquals("null", sleeper.getLastLogin());
        }
    }
}
