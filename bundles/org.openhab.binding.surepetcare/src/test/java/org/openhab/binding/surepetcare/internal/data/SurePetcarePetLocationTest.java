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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;

/**
 * The {@link SurePetcarePetLocationTest} class implements unit test case for {@link SurePetcarePetLocation}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcarePetLocationTest {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    @Test
    public void testJsonDeserialize() throws ParseException {
        String testReponse = "{\"tag_id\":60126,\"device_id\":376236,\"where\":2,\"since\":\"2019-09-11T13:09:07+00:00\"}";
        SurePetcarePetActivity response = SurePetcareConstants.GSON.fromJson(testReponse, SurePetcarePetActivity.class);

        assertEquals(new Integer(60126), response.getTagId());
        assertEquals(new Integer(376236), response.getDeviceId());
        assertEquals(new Integer(2), response.getWhere());
        Date sinceDate = simpleDateFormat.parse("2019-09-11T13:09:07+0000");
        assertEquals(sinceDate, response.getSince());
    }

    @Test
    public void testJsonFullSerialize() throws ParseException {

        Date since = simpleDateFormat.parse("2019-09-11T13:09:07+0000");

        SurePetcarePetActivity location = new SurePetcarePetActivity(2, since);

        String json = SurePetcareConstants.GSON.toJson(location, SurePetcarePetActivity.class);

        assertEquals("{\"where\":2,\"since\":\"2019-09-11T13:09:07+00:00\"}", json);
    }

}
