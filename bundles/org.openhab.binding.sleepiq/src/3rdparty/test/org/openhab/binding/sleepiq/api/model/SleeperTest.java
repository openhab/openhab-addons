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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileReader;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.impl.GsonGenerator;
import org.openhab.binding.sleepiq.api.test.AbstractTest;

import com.google.gson.Gson;

public class SleeperTest extends AbstractTest {
    private static Gson gson;

    @BeforeAll
    public static void setUpBeforeClass() {
        gson = GsonGenerator.create(true);
    }

    @Test
    public void testSerializeAllFields() throws Exception {
        Sleeper sleeper = new Sleeper().withAccountId("-5555555555555555555").withAccountOwner(true).withActive(true)
                .withAvatar("").withBedId("-9999999999999999999").withBirthMonth(6).withBirthYear("1970")
                .withChild(false).withDuration("").withEmail("alice@domain.com").withEmailValidated(true)
                .withFirstName("Alice").withHeight(64).withLastLogin("2017-02-17 20:19:36 CST").withLicenseVersion(6L)
                .withMale(false).withSide(1).withSleeperId("-1111111111111111111").withSleepGoal(450)
                .withTimezone("US/Pacific").withUsername("alice@domain.com").withWeight(110).withZipCode("90210");
        assertEquals(readJson("sleeper.json"), gson.toJson(sleeper));
    }

    @Test
    public void testSerializeLastLoginNull() throws Exception {
        Sleeper sleeper = new Sleeper().withLastLogin("null");
        assertEquals(readJson("sleeper-lastlogin-null.json"), gson.toJson(sleeper));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("sleeper.json"))) {
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
    public void testDeserializeLastLoginNull() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("sleeper-lastlogin-null.json"))) {
            Sleeper sleeper = gson.fromJson(reader, Sleeper.class);
            assertEquals("null", sleeper.getLastLogin());
        }
    }
}
