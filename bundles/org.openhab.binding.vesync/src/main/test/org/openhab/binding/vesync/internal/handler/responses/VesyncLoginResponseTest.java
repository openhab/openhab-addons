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
package org.openhab.binding.vesync.internal.handler.responses;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.responses.VesyncLoginResponse;

/**
 * The {@link VesyncLoginResponseTest} class implements unit test case for {@link VesyncLoginResponse}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VesyncLoginResponseTest {

    public final static String testGoodLoginResponseBody = "{\r\n" + "    \"traceId\": \"1634253816\",\r\n"
            + "    \"code\": 0,\r\n" + "    \"msg\": \"request success\",\r\n" + "    \"result\": {\r\n"
            + "        \"isRequiredVerify\": true,\r\n" + "        \"accountID\": \"5328043\",\r\n"
            + "        \"avatarIcon\": \"https://image.vesync.com/defaultImages/user/avatar_nor.png\",\r\n"
            + "        \"birthday\": \"\",\r\n" + "        \"gender\": \"\",\r\n"
            + "        \"acceptLanguage\": \"en\",\r\n" + "        \"userType\": \"1\",\r\n"
            + "        \"nickName\": \"david.goodyear\",\r\n" + "        \"mailConfirmation\": true,\r\n"
            + "        \"termsStatus\": true,\r\n" + "        \"gdprStatus\": true,\r\n"
            + "        \"countryCode\": \"GB\",\r\n" + "        \"registerAppVersion\": \"VeSync 3.1.37 build3\",\r\n"
            + "        \"registerTime\": \"2021-10-14 17:35:50\",\r\n"
            + "        \"verifyEmail\": \"david.goodyear@gmail.com\",\r\n" + "        \"heightCm\": 0.0,\r\n"
            + "        \"weightTargetSt\": 0.0,\r\n" + "        \"heightUnit\": \"FT\",\r\n"
            + "        \"heightFt\": 0.0,\r\n" + "        \"weightTargetKg\": 0.0,\r\n"
            + "        \"weightTargetLb\": 0.0,\r\n" + "        \"weightUnit\": \"LB\",\r\n"
            + "        \"targetBfr\": 0.0,\r\n" + "        \"displayFlag\": [],\r\n"
            + "        \"real_weight_kg\": 0.0,\r\n" + "        \"real_weight_lb\": 0.0,\r\n"
            + "        \"real_weight_unit\": \"lb\",\r\n" + "        \"heart_rate_zones\": 0.0,\r\n"
            + "        \"run_step_long_cm\": 0.0,\r\n" + "        \"walk_step_long_cm\": 0.0,\r\n"
            + "        \"step_target\": 0.0,\r\n" + "        \"sleep_target_mins\": 0.0,\r\n"
            + "        \"token\": \"AccessTokenString=\"\r\n" + "    }\r\n" + "}";

    @Test
    public void testParseLoginGoodResponse() {
        VesyncLoginResponse response = VeSyncConstants.GSON.fromJson(testGoodLoginResponseBody,
                VesyncLoginResponse.class);
        if (response != null) {
            assertEquals("1634253816", response.getTraceId());
            assertEquals("AccessTokenString=", response.result.token);
            assertEquals("request success", response.msg);
            assertEquals("5328043", response.result.accountId);
            assertEquals("VeSync 3.1.37 build3", response.result.registerAppVersion);
            assertEquals("GB", response.result.countryCode);
            assertTrue(response.isMsgSuccess());
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void testParseLoginFailResponse() {
        String testReponse = "{\r\n" + "    \"traceId\": \"1634253816\",\r\n" + "    \"code\": -11201022,\r\n"
                + "    \"msg\": \"password incorrect\",\r\n" + "    \"result\": null\r\n" + "}";
        VesyncLoginResponse response = VeSyncConstants.GSON.fromJson(testReponse,
                VesyncLoginResponse.class);
        if (response != null) {
            assertEquals("password incorrect", response.msg);
            assertFalse(response.isMsgSuccess());
        } else {
            fail("GSON returned null");
        }
    }
}
