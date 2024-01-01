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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.responses.VesyncResponse;

/**
 * The {@link VesyncResponseTest} class implements unit test case for {@link VesyncResponse}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VesyncResponseTest {

    @Test
    public void checkBaseFields() {
        String baseTestResponse = "{\"traceId\":\"1234569876\",\r\n\"code\": 142,\r\n\"msg\": \"Response Text\"\r\n}";
        VesyncResponse response = VeSyncConstants.GSON.fromJson(baseTestResponse, VesyncResponse.class);
        if (response != null) {
            assertEquals("1234569876", response.getTraceId());
            assertEquals("142", response.getCode());
            assertEquals("Response Text", response.msg);
            assertEquals(false, response.isMsgSuccess());
        } else {
            fail("GSON returned null");
        }
    }

    @Test
    public void checkResponseSuccessMsg() {
        String baseTestResponse = "{\"traceId\":\"1234569876\",\r\n\"code\": 142,\r\n\"msg\": \"request success\"\r\n}";
        VesyncResponse response = VeSyncConstants.GSON.fromJson(baseTestResponse, VesyncResponse.class);
        if (response != null) {
            assertEquals(true, response.isMsgSuccess());
        } else {
            fail("GSON returned null");
        }
    }
}
