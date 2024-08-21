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
package org.openhab.binding.surepetcare.internal.data;

import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareTopology;

/**
 * The {@link SurePetcareTopologyTest} class implements unit test case for {@link SurePetcareTopology}
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareTopologyTest {

    @Test
    public void testTopologyPopulated() {
        String testResponse = "{\"devices\":[{\"id\":23912},{\"id\":23481}],\"households\":[{\"id\":83271}],\"pets\":[{\"id\":12345}],\"photos\":[{\"id\":64257,\"version\":\"MA==\",\"created_at\":\"2019-10-04T16:03:20+00:00\",\"updated_at\":\"2019-10-04T16:03:20+00:00\"}],\"user\":{\"id\":33421,\"version\":\"MA==\",\"created_at\":\"2019-09-18T16:09:30+00:00\",\"updated_at\":\"2019-09-18T16:09:30+00:00\"}}";
        SurePetcareTopology response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareTopology.class);

        if (response != null) {
            assertNotNull(response.devices);
            assertEquals(2, response.devices.size());
            assertNotNull(response.households);
            assertEquals(1, response.households.size());
            assertNotNull(response.pets);
            assertEquals(1, response.pets.size());
            assertNotNull(response.photos);
            assertEquals(1, response.photos.size());
            assertNotNull(response.user);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testTopologyEmpty() {
        String testResponse = "{}";
        SurePetcareTopology response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareTopology.class);

        if (response != null) {
            assertNotNull(response.tags);
            assertEquals(0, response.tags.size());
            assertNotNull(response.households);
            assertEquals(0, response.households.size());
            assertNotNull(response.pets);
            assertEquals(0, response.pets.size());
            assertNotNull(response.devices);
            assertEquals(0, response.devices.size());
            assertNull(response.user);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testGetUserNull() {
        String testResponse = "{\"devices\":[{\"id\":23912},{\"id\":23481}],\"households\":[{\"id\":83271}],\"pets\":[{\"id\":12345}],\"photos\":[{\"id\":64257,\"version\":\"MA==\",\"created_at\":\"2019-10-04T16:03:20+00:00\",\"updated_at\":\"2019-10-04T16:03:20+00:00\"}]}";
        SurePetcareTopology response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareTopology.class);

        if (response != null) {
            assertNull(response.user);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testDateFormats() {
        String testResponse = """
                {
                    "devices": [],
                    "households": [
                        {
                            "id": 0,
                            "name": "***",
                            "share_code": "***",
                            "created_user_id": 0,
                            "timezone_id": 374,
                            "version": "MTE=",
                            "created_at": "2021-04-24T11:41:15+00:00",
                            "updated_at": "2023-12-16T21:08:19.637892+00:00",
                            "invites": [],
                            "users": [],
                            "timezone": {
                                "id": 374,
                                "name": "(UTC+02:00) Europe/Zurich",
                                "timezone": "Europe/Zurich",
                                "utc_offset": 7200,
                                "created_at": "2017-08-03T08:35:34+00:00",
                                "updated_at": "2017-08-03T08:37:15+00:00"
                            }
                        }
                    ],
                    "pets": [],
                    "photos": [],
                    "tags": [],
                    "user": {}
                }
                """;

        SurePetcareTopology response = SurePetcareConstants.GSON.fromJson(testResponse, SurePetcareTopology.class);

        assertNotNull(response);
        assertNotNull(response.households);
        assertEquals(1, response.households.size());
        assertEquals(ZonedDateTime.of(2021, 4, 24, 11, 41, 15, 0, ZoneOffset.UTC),
                response.households.get(0).createdAt);
        assertEquals(ZonedDateTime.of(2023, 12, 16, 21, 8, 19, 637892000, ZoneOffset.UTC),
                response.households.get(0).updatedAt);
    }
}
