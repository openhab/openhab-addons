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
import static org.openhab.binding.linktap.protocol.frames.SetDeviceConfigReq.CONFIG_VOLUME_LIMIT;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_PAUSE_WATER_PLAN;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_SET_CONFIGURATION;

/**
 * Command 17: Set Device Configuration parameter
 * Flow 1 --> App->Broker->GW: Sets the given device configuration parameter
 */
@NonNullByDefault
public class Command17Test {

    /**
     * Command 17: Set Device Configuration parameter
     * Flow 1 --> App->Broker->GW: Sets the given device configuration parameter
     */
    @Test
    public void RequestConfigUpdateGenerationTest() {
        SetDeviceConfigReq req = new SetDeviceConfigReq();
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.command = CMD_SET_CONFIGURATION;
        req.tag = CONFIG_VOLUME_LIMIT;
        req.value = 123;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"value\":123,\"tag\":\"volume_limit\",\"dev_id\":\"1111222233334444\",\"cmd\":17,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 17: Set Device Configuration parameter
     * Flow 1 --> App->Broker->GW: Sets the given device configuration parameter
     */
    @Test
    public void RequestConfigUpdateResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":17, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1122334455667788\", \"ret\":0\n}",EndpointDeviceResponse.class);
        assertEquals(CMD_SET_CONFIGURATION,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1122334455667788",decoded.deviceId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }
    
}
