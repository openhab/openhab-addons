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

    public static final String testGoodLoginResponseBody = """
            {
                "traceId": "1634253816",
                "code": 0,
                "msg": "request success",
                "result": {
                    "isRequiredVerify": true,
                    "accountID": "5328043",
                    "avatarIcon": "https://image.vesync.com/defaultImages/user/avatar_nor.png",
                    "birthday": "",
                    "gender": "",
                    "acceptLanguage": "en",
                    "userType": "1",
                    "nickName": "david.goodyear",
                    "mailConfirmation": true,
                    "termsStatus": true,
                    "gdprStatus": true,
                    "countryCode": "GB",
                    "registerAppVersion": "VeSync 3.1.37 build3",
                    "registerTime": "2021-10-14 17:35:50",
                    "verifyEmail": "david.goodyear@gmail.com",
                    "heightCm": 0.0,
                    "weightTargetSt": 0.0,
                    "heightUnit": "FT",
                    "heightFt": 0.0,
                    "weightTargetKg": 0.0,
                    "weightTargetLb": 0.0,
                    "weightUnit": "LB",
                    "targetBfr": 0.0,
                    "displayFlag": [],
                    "real_weight_kg": 0.0,
                    "real_weight_lb": 0.0,
                    "real_weight_unit": "lb",
                    "heart_rate_zones": 0.0,
                    "run_step_long_cm": 0.0,
                    "walk_step_long_cm": 0.0,
                    "step_target": 0.0,
                    "sleep_target_mins": 0.0,
                    "token": "AccessTokenString="
                }
            }\
            """;

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
        String testReponse = """
                {
                    "traceId": "1634253816",
                    "code": -11201022,
                    "msg": "password incorrect",
                    "result": null
                }\
                """;
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
