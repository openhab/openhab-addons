/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.omnikinverter.internal.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.omnikinverter.internal.OmnikInverterMessage;

public class OmnikInverterMessageTest {

    private OmnikInverterMessage message;

    @Before
    public void setUp() throws IOException {
        File file = new File("src/test/resources/omnik.output");
        message = new OmnikInverterMessage(Files.readAllBytes(file.toPath()));
    }

    @Test
    public void testGetPower() {
        assertEquals(137.0, message.getPower(), 0.01);
    }

    @Test
    public void testGetTotalEnergy() {
        assertEquals(12412.7, message.getTotalEnergy(), 0.01);
    }

    @Test
    public void testGetEnergyToday() {
        assertEquals(11.13, message.getEnergyToday(), 0.01);
    }

}
