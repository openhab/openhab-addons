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
package org.openhab.binding.surepetcare.internal.data;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareBaseObject;

/**
 * The {@link SurePetcareBaseObjectTest} class implements unit test case for {@link SurePetcareBaseObject}
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareBaseObjectTest {

    @Test
    public void testNotNullFromJson() {
        String testResponse = "{\"id\":2491083182,\"version\":\"MA==\",\"created_at\":\"2019-09-18T16:09:30+00:00\",\"updated_at\":\"2019-09-18T16:09:30+00:00\"}";
        SurePetcareBaseObject response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareBaseObject.class);
        if (response != null) {
            assertEquals(Long.valueOf(2491083182L), response.id);
            assertEquals("MA==", response.version);
            assertNotNull(response.createdAt);
            assertNotNull(response.updatedAt);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testNullAttributesFromJson() {
        String testResponse = "{\"id\":33421}";
        SurePetcareBaseObject response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareBaseObject.class);

        if (response != null) {
            assertEquals(Long.valueOf(33421), response.id);
            assertEquals("", response.version);
            assertNotNull(response.createdAt);
            assertNotNull(response.updatedAt);
        } else {
            fail("GSON returned null");
        }
    }
}
