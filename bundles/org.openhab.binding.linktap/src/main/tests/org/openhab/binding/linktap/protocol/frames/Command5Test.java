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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_REMOVE_WATER_PLAN;

/**
 * Command 5: Delete Watering Plan from Device Endpoint
 * Flow 1 --> App->Broker->GW: Delete existing watering plan from device
 */
@NonNullByDefault
public class Command5Test {

    /**
     * Command 5:
     * Flow 1 --> App->Broker->GW: Delete existing watering plan from device encoding test
     */
    @Test
    public void DeleteWateringPlanRequestEncoding() {
        final DeviceCmdReq req = new DeviceCmdReq();
        req.command = CMD_REMOVE_WATER_PLAN;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"dev_id\":\"1111222233334444\",\"cmd\":5,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 5:
     * Flow 1 --> App->Broker->GW: Delete existing watering plan from device response decoding test
     */
    @Test
    public void DeleteWateringPlanRequestResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":5, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1111222233334444\", \"ret\":0\n}",EndpointDeviceResponse.class);

        assertEquals(CMD_REMOVE_WATER_PLAN,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444",decoded.deviceId);
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }

}
