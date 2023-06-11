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
package org.openhab.binding.surepetcare.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.surepetcare.internal.SurePetcareConstants;
import org.openhab.binding.surepetcare.internal.dto.SurePetcareLoginResponse;

/**
 * The {@link SurePetcareLoginResponseTest} class implements unit test case for {@link SurePetcareLoginResponse}
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class SurePetcareLoginResponseTest {

    @Test
    public void testParseLoginResponse() {
        String testReponse = "{\"data\":{\"user\":{\"id\":23412,\"email_address\":\"rene@gugus.com\",\"first_name\":\"Rene\",\"last_name\":\"Scherer\",\"country_id\":77,\"language_id\":37,\"marketing_opt_in\":false,\"terms_accepted\":true,\"weight_units\":0,\"time_format\":0,\"version\":\"MA==\",\"created_at\":\"2019-09-02T08:20:03+00:00\",\"updated_at\":\"2019-09-02T08:20:03+00:00\",\"notifications\":{\"device_status\":true,\"animal_movement\":true,\"intruder_movements\":true,\"new_device_pet\":true,\"household_management\":true,\"photos\":true,\"low_battery\":true,\"curfew\":true,\"feeding_activity\":true}},\"token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczovL2FwcC5hcGkuc3VyZWh1Yi5pby9hcGkvYXV0aC9sb2dpbiIsImlhdCI6MTU2NzYxMjY2OSwiZXhwIjoxNTk5MDYyMjY5LCJuYmYiOjE1Njc2MTI2NjksImp0aSI6IlY4M1lJQlJ5dVRqMUVDcWsiLCJzdWIiOjUyODE1LCJwcnYiOiJiM2VkM2RiMzM0YzJiYzMzYjE4NDI2OTQ3NTU3NTZhM2ZmYmY1YTdkIiwiZGV2aWNlX2lkIjoiNTczODc2MzQifQ.WeRutm8I7gMb21dtrknDh6LGFkwxfrXcak-IoykwvV8\"}}";
        SurePetcareLoginResponse response = SurePetcareConstants.GSON.fromJson(testReponse,
                SurePetcareLoginResponse.class);
        if (response != null) {
            assertEquals("Rene", response.data.user.firstName);
            assertEquals(363, response.data.token.length());
        } else {
            fail("GSON returned null");
        }
    }
}
