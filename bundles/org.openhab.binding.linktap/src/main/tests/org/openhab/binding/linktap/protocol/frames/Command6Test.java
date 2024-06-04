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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_IMMEDIATE_WATER_START;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_IMMEDIATE_WATER_STOP;

/**
 * Command 6: Start watering immediately
 * Flow 1 --> App->Broker->GW: Start watering immediately, once time only for the given parameters
 */
@NonNullByDefault
public class Command6Test {

    /**
     * Command 6:
     * Flow 1 --> App->Broker->GW: Start watering immediately, once time only for the given parameters
     */
    @Test
    public void StartWateringRequestEncoding() {
        final StartWateringReq req = new StartWateringReq();
        req.command = CMD_IMMEDIATE_WATER_START;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.duration = 60;
        req.volume = 0;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"duration\":60,\"volume\":0,\"dev_id\":\"1111222233334444\",\"cmd\":6,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 6:
     * Flow 1 --> App->Broker->GW: Start watering immediately, once time only for the given parameters
     */
    @Test
    public void StartWateringRequestResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":6, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1111222233334444\", \"ret\":0\n}",EndpointDeviceResponse.class);

        assertEquals(CMD_IMMEDIATE_WATER_START,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444",decoded.deviceId);
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }

}
