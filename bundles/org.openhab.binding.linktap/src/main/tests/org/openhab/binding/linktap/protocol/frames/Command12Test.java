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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_ALERT_DISMISS;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_LOCKOUT_STATE;

/**
 * Command 12: Set lock state
 * Flow 1 --> App->Broker->GW: Request lock status is changed - G15 and G25 models only
 */
@NonNullByDefault
public class Command12Test {

    /**
     * Command 12: Set lock state
     * Flow 1 --> App->Broker->GW: Request lock status is changed - G15 and G25 models only
     */
    @Test
    public void SetLockStateGenerationTest() {
        LockReq req = new LockReq();
        req.command = CMD_LOCKOUT_STATE;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.lock = 0;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"lock\":0,\"dev_id\":\"1111222233334444\",\"cmd\":12,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 12: Set lock state
     * Flow 1 --> App->Broker->GW: Request lock status is changed response decoding
     */
    @Test
    public void SetLockStateResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":12, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1111222233334444\", \"ret\":0\n}",EndpointDeviceResponse.class);

        assertEquals(CMD_LOCKOUT_STATE,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444",decoded.deviceId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }
    
}
