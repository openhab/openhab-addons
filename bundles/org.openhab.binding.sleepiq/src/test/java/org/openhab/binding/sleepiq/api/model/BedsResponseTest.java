/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.test.AbstractTest;
import org.openhab.binding.sleepiq.internal.api.dto.Bed;
import org.openhab.binding.sleepiq.internal.api.dto.BedsResponse;
import org.openhab.binding.sleepiq.internal.api.impl.GsonGenerator;

import com.google.gson.Gson;

/**
 * The {@link BedsResponseTest} tests deserialization of a beds response object.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class BedsResponseTest extends AbstractTest {
    private static Gson gson = GsonGenerator.create(true);

    @Test
    public void testSerializeAllFields() throws Exception {
        BedsResponse bedsResponse = new BedsResponse()
                .withBeds(Arrays.asList(new Bed().withName("Bed1"), new Bed().withName("Bed2")));
        assertEquals(readJson("beds-response.json"), gson.toJson(bedsResponse));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("beds-response.json"))) {
            BedsResponse bedsResponse = gson.fromJson(reader, BedsResponse.class);

            List<Bed> beds = bedsResponse.getBeds();
            assertNotNull(beds);
            assertEquals(2, beds.size());
            assertEquals("Bed1", beds.get(0).getName());
            assertEquals("Bed2", beds.get(1).getName());
        }
    }
}
