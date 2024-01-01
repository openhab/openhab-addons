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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.api.test.AbstractTest;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationStatusResponse;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationPreset;
import org.openhab.binding.sleepiq.internal.api.impl.GsonGenerator;

import com.google.gson.Gson;

/**
 * The {@link FoundationStatusResponseTest} tests deserialization of a foundation status
 * response object.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class FoundationStatusResponseTest extends AbstractTest {
    private static Gson gson = GsonGenerator.create(true);

    @Test
    public void testDeserializeAllFields() throws Exception {
        try (FileReader reader = new FileReader(getTestDataFile("foundation-status-response.json"))) {
            FoundationStatusResponse status = gson.fromJson(reader, FoundationStatusResponse.class);

            assertNotNull(status);
            assertEquals(0x0f, status.getLeftHeadPosition());
            assertEquals(0x21, status.getLeftFootPosition());
            assertEquals(0x0d, status.getRightHeadPosition());
            assertEquals(0x04, status.getRightFootPosition());
            assertEquals(FoundationPreset.SNORE, status.getCurrentPositionPresetLeft());
            assertEquals(FoundationPreset.READ, status.getCurrentPositionPresetRight());
        }
    }
}
