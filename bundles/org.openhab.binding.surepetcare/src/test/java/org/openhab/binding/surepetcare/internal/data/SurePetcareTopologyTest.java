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
 * The {@link SurePetcareTopologyTest} class implements unit test case for {@link SurePetcareTopology}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareTopologyTest {

    @Test
    public void testTopologyPopulated() {
        String testResponse = "{\"devices\":[{\"id\":23912},{\"id\":23481}],\"households\":[{\"id\":83271}],\"pets\":[{\"id\":12345}],\"photos\":[{\"id\":64257,\"version\":\"MA==\",\"created_at\":\"2019-10-04T16:03:20+00:00\",\"updated_at\":\"2019-10-04T16:03:20+00:00\"}],\"user\":{\"id\":33421,\"version\":\"MA==\",\"created_at\":\"2019-09-18T16:09:30+00:00\",\"updated_at\":\"2019-09-18T16:09:30+00:00\"}}";
        SurePetcareTopology response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareTopology.class);

        assertNotNull(response.getDevices());
        assertEquals(2, response.getDevices().size());
        assertNotNull(response.getHouseholds());
        assertEquals(1, response.getHouseholds().size());
        assertNotNull(response.getPets());
        assertEquals(1, response.getPets().size());
        assertNotNull(response.getPhotos());
        assertEquals(1, response.getPhotos().size());

        assertNotNull(response.getUser());
    }

    @Test
    public void testTopologyEmpty() {
        String testResponse = "{}";
        SurePetcareTopology response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareTopology.class);

        assertNotNull(response.getTags());
        assertEquals(0, response.getTags().size());
        assertNotNull(response.getHouseholds());
        assertEquals(0, response.getHouseholds().size());
        assertNotNull(response.getPets());
        assertEquals(0, response.getPets().size());
        assertNotNull(response.getDevices());
        assertEquals(0, response.getDevices().size());

        assertNull(response.getUser());
    }

    @Test
    public void testGetUserNull() {
        String testResponse = "{\"devices\":[{\"id\":23912},{\"id\":23481}],\"households\":[{\"id\":83271}],\"pets\":[{\"id\":12345}],\"photos\":[{\"id\":64257,\"version\":\"MA==\",\"created_at\":\"2019-10-04T16:03:20+00:00\",\"updated_at\":\"2019-10-04T16:03:20+00:00\"}]}";
        SurePetcareTopology response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareTopology.class);

        assertNull(response.getUser());
    }

}
