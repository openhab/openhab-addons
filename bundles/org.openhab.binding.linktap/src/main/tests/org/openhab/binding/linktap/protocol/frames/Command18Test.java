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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_PAUSE_WATER_PLAN;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_WIRELESS_CHECK;

/**
 * Command 18: Pause watering plan for given duration
 * Flow 1 --> App->Broker->GW: Pause watering plan for given duration 0.1 -> 240 hours
 */
@NonNullByDefault
public class Command18Test {

    /**
     * Command 18: Pause watering plan for given duration
     * Flow 1 --> App->Broker->GW: Pause watering plan for given duration 0.1 -> 240 hours
     */
    @Test
    public void RequestWateringPlanPauseGenerationTest() {
        PauseWateringPlanReq req = new PauseWateringPlanReq();
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.command = CMD_PAUSE_WATER_PLAN;
        req.duration = 12d;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"duration\":12.0,\"dev_id\":\"1111222233334444\",\"cmd\":18,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 18: Pause watering plan for given duration
     * Flow 1 --> App->Broker->GW: Pause watering plan for given duration 0.1 -> 240 hours
     */
    @Test
    public void RequestWateringPlanPauseResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":18, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1122334455667788\", \"ret\":0\n}",EndpointDeviceResponse.class);
        assertEquals(CMD_PAUSE_WATER_PLAN,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1122334455667788",decoded.deviceId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }
    
}
