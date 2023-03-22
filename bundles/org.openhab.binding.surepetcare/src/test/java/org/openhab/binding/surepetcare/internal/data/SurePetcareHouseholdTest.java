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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareHousehold;

/**
 * The {@link SurePetcareHouseholdTest} class implements unit test case for {@link SurePetcareHousehold}
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareHouseholdTest {

    @Test
    public void testJsonDeserialize1() throws ParseException {
        String testReponse = "{\"id\":2491083182,\"name\":\"Test Home\",\"share_code\":\"BFHvjQ8DgvnP\",\"timezone_id\":340,\"version\":\"MA==\",\"created_at\":\"2019-09-02T08:20:45+00:00\",\"updated_at\":\"2019-09-02T08:20:48+00:00\",\"invites\":[{\"id\":12352,\"code\":\"QDEZHNNHFG\",\"email_address\":\"user1@gugus.com\",\"creator_user_id\":32712,\"acceptor_user_id\":87621,\"owner\":false,\"write\":true,\"status\":1,\"version\":\"Mg==\",\"created_at\":\"2019-09-09T10:33:36+00:00\",\"updated_at\":\"2019-09-09T11:59:39+00:00\",\"user\":{\"acceptor\":{\"id\":87621,\"name\":\"User1\"},\"creator\":{\"id\":32712,\"name\":\"Admin User\"}}}],\"users\":[{\"id\":32712,\"owner\":true,\"write\":true,\"version\":\"MA==\",\"created_at\":\"2019-09-02T08:20:45+00:00\",\"updated_at\":\"2019-09-02T08:20:50+00:00\",\"user\":{\"id\":32712,\"name\":\"Admin User\"}},{\"id\":87621,\"owner\":false,\"write\":true,\"version\":\"MA==\",\"created_at\":\"2019-09-09T11:59:39+00:00\",\"updated_at\":\"2019-09-09T11:59:39+00:00\",\"user\":{\"id\":87621,\"name\":\"User1\"}}]}";
        SurePetcareHousehold response = SurePetcareConstants.GSON.fromJson(testReponse, SurePetcareHousehold.class);

        if (response != null) {
            assertEquals(Long.valueOf(2491083182L), response.id);
            assertEquals("Test Home", response.name);
            assertEquals("BFHvjQ8DgvnP", response.shareCode);
            assertEquals(Integer.valueOf(340), response.timezoneId);
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testJsonDeserialize2() throws ParseException {
        String testReponse = "{\"id\":2491083182,\"name\":\"Test Home\",\"share_code\":\"BFHvjQ8DgvnP\",\"timezone_id\":320,\"version\":\"MQ==\",\"created_at\":\"2018-12-21T17:50:07+00:00\",\"updated_at\":\"2018-12-21T17:50:07+00:00\",\"invites\":[{\"id\":10414,\"code\":\"KHDSUYRBKJF\",\"email_address\":\"test@gmx.de\",\"creator_user_id\":22360,\"owner\":false,\"write\":false,\"status\":0,\"version\":\"MA==\",\"created_at\":\"2018-12-22T09:15:10+00:00\",\"updated_at\":\"2018-12-22T09:15:10+00:00\",\"user\":{\"creator\":{\"id\":22360,\"name\":\"Owner Name\"}}}],\"users\":[{\"id\":22360,\"owner\":true,\"write\":true,\"version\":\"MQ==\",\"created_at\":\"2018-12-21T17:50:07+00:00\",\"updated_at\":\"2018-12-21T17:50:13+00:00\",\"user\":{\"id\":22360,\"name\":\"Owner Name\"}}]}";
        SurePetcareHousehold response = SurePetcareConstants.GSON.fromJson(testReponse, SurePetcareHousehold.class);

        if (response != null) {
            assertEquals(Long.valueOf(2491083182L), response.id);
            assertEquals("Test Home", response.name);
            assertEquals("BFHvjQ8DgvnP", response.shareCode);
            assertEquals(Integer.valueOf(320), response.timezoneId);
        } else {
            fail("GSON returned null");
        }
    }
}
