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
package org.openhab.binding.linktap.protocol.frames;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.linktap.internal.LinkTapBindingConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_DATETIME_SYNC;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_WIRELESS_CHECK;

/**
 * Command 15: Test wireless performance of end device
 * Flow 1 --> App->Broker->GW: Request a ping pong test is done to measure wireless performance for a end device
 */
@NonNullByDefault
public class Command15Test {

    /**
     * Command 15: Test wireless performance of end device
     * Flow 1 --> App->Broker->GW: Request a ping pong test is done to measure wireless performance for a end device
     */
    @Test
    public void RequestWirelessCheckGenerationTest() {
        DeviceCmdReq req = new DeviceCmdReq();
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.command = CMD_WIRELESS_CHECK;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"dev_id\":\"1111222233334444\",\"cmd\":15,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 15: Test wireless performance of end device
     * Flow 1 --> App->Broker->GW: Request a ping pong test is done to measure wireless performance for a end device response
     */
    @Test
    public void RequestWirelessCheckResponseDecoding() {
        final WirelessTestResp decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":15, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1111222233334444\", \"ret\":0, \"final\":false, \"ping\":5, \"pong\":4\n}",WirelessTestResp.class);
        assertEquals(CMD_WIRELESS_CHECK,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444",decoded.deviceId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
        assertFalse(decoded.testComplete);
        assertEquals(5, decoded.pingCount);
        assertEquals(4, decoded.pongCount);
    }
    
}
