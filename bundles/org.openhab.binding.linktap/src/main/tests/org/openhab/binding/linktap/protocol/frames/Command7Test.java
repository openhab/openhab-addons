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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_IMMEDIATE_WATER_STOP;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_REMOVE_WATER_PLAN;

/**
 * Command 7: Stop watering immediately
 * Flow 1 --> App->Broker->GW: Stop watering immediately, next cycled watering plan will still run
 */
@NonNullByDefault
public class Command7Test {

    /**
     * Command 7:
     * Flow 1 --> App->Broker->GW: Stop watering immediately, next cycled watering plan will still run
     */
    @Test
    public void StopWateringRequestEncoding() {
        final DeviceCmdReq req = new DeviceCmdReq();
        req.command = CMD_IMMEDIATE_WATER_STOP;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"dev_id\":\"1111222233334444\",\"cmd\":7,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 7:
     * Flow 1 --> App->Broker->GW: Stop watering immediately, next cycled watering plan will still run
     */
    @Test
    public void StopWateringRequestResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":7, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1111222233334444\", \"ret\":0\n}",EndpointDeviceResponse.class);

        assertEquals(CMD_IMMEDIATE_WATER_STOP,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444",decoded.deviceId);
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }

}
