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
import org.openhab.binding.sleepiq.internal.api.dto.BedStatus;
import org.openhab.binding.sleepiq.internal.api.dto.FamilyStatusResponse;
import org.openhab.binding.sleepiq.internal.api.impl.GsonGenerator;

import com.google.gson.Gson;

/**
 * The {@link FamilyStatusText} tests deserialization of a family status object.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class FamilyStatusTest extends AbstractTest {
    private static Gson gson = GsonGenerator.create(true);

    @Test
    public void testSerializeAllFields() throws Exception {
        FamilyStatusResponse familyStatus = new FamilyStatusResponse()
                .withBeds(Arrays.asList(new BedStatus().withStatus(1L)));
        assertEquals(readJson("family-status.json"), gson.toJson(familyStatus));
    }

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("family-status.json"))) {
            FamilyStatusResponse familyStatus = gson.fromJson(reader, FamilyStatusResponse.class);

            List<BedStatus> beds = familyStatus.getBeds();
            assertNotNull(beds);
            assertEquals(1, beds.size());
            assertEquals(Long.valueOf(1L), beds.get(0).getStatus());
        }
    }
}
