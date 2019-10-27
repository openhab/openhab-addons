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
package org.openhab.binding.surepetcare.internal.data;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;

/**
 * The {@link SurePetcareBaseObjectTest} class implements unit test case for {@link SurePetcareBaseObject}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareBaseObjectTest {

    @Test
    public void testNotNullFromJson() {
        String testResponse = "{\"id\":33421,\"version\":\"MA==\",\"created_at\":\"2019-09-18T16:09:30+00:00\",\"updated_at\":\"2019-09-18T16:09:30+00:00\"}";
        SurePetcareBaseObject response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareBaseObject.class);

        assertEquals(new Integer(33421), response.getId());
        assertEquals("MA==", response.getVersion());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    public void testNullAttributesFromJson() {
        String testResponse = "{\"id\":33421}";
        SurePetcareBaseObject response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareBaseObject.class);

        assertEquals(new Integer(33421), response.getId());
        assertEquals("", response.getVersion());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

}
