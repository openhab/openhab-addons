/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.dto.SurePetcarePetActivity;

/**
 * The {@link SurePetcarePetLocationTest} class implements unit test case for {@link SurePetcarePetLocation}
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcarePetActivityTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Test
    public void testJsonDeserialize() throws ParseException {
        String testReponse = "{\"tag_id\":60126,\"device_id\":376236,\"where\":2,\"since\":\"2019-09-11T13:09:07+00:00\"}";
        SurePetcarePetActivity response = SurePetcareConstants.GSON.fromJson(testReponse, SurePetcarePetActivity.class);

        if (response != null) {
            assertEquals(Long.valueOf(60126), response.tagId);
            assertEquals(Long.valueOf(376236), response.deviceId);
            assertEquals(Integer.valueOf(2), response.where);
            ZonedDateTime since = FORMATTER.parse("2019-09-11T13:09:07+00:00", ZonedDateTime::from);
            assertEquals(since, response.since);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testJsonFullSerialize() throws ParseException {
        ZonedDateTime since = ZonedDateTime.parse("2019-09-11T13:09:07+00:00");
        SurePetcarePetActivity location = new SurePetcarePetActivity(2, since);

        String json = SurePetcareConstants.GSON.toJson(location, SurePetcarePetActivity.class);

        assertEquals("{\"where\":2,\"since\":\"2019-09-11T13:09:07+00:00\"}", json);
    }
}
