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

import org.junit.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;

/**
 * The {@link SurePetcareHouseholdTest} class implements unit test case for {@link SurePetcareHousehold}
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareHouseholdTest {

    @Test
    public void testJsonDeserialize() throws ParseException {
        String testReponse = "{\"id\":67241,\"name\":\"Test Home\",\"share_code\":\"BFHvjQ8DgvnP\",\"timezone_id\":340,\"version\":\"MA==\",\"created_at\":\"2019-09-02T08:20:45+00:00\",\"updated_at\":\"2019-09-02T08:20:48+00:00\",\"invites\":[{\"id\":12352,\"code\":\"QDEZHNNHFG\",\"email_address\":\"user1@gugus.com\",\"creator_user_id\":32712,\"acceptor_user_id\":87621,\"owner\":false,\"write\":true,\"status\":1,\"version\":\"Mg==\",\"created_at\":\"2019-09-09T10:33:36+00:00\",\"updated_at\":\"2019-09-09T11:59:39+00:00\",\"user\":{\"acceptor\":{\"id\":87621,\"name\":\"User1\"},\"creator\":{\"id\":32712,\"name\":\"Admin User\"}}}],\"users\":[{\"id\":32712,\"owner\":true,\"write\":true,\"version\":\"MA==\",\"created_at\":\"2019-09-02T08:20:45+00:00\",\"updated_at\":\"2019-09-02T08:20:50+00:00\",\"user\":{\"id\":32712,\"name\":\"Admin User\"}},{\"id\":87621,\"owner\":false,\"write\":true,\"version\":\"MA==\",\"created_at\":\"2019-09-09T11:59:39+00:00\",\"updated_at\":\"2019-09-09T11:59:39+00:00\",\"user\":{\"id\":87621,\"name\":\"User1\"}}]}";
        SurePetcareHousehold response = SurePetcareConstants.GSON.fromJson(testReponse, SurePetcareHousehold.class);

        assertEquals(new Integer(67241), response.getId());
        assertEquals("Test Home", response.getName());
        assertEquals("BFHvjQ8DgvnP", response.getShareCode());
        assertEquals(new Integer(340), response.getTimezoneId());
    }

}
