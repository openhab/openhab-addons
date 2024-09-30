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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_REMOVE_END_DEVICE;

/**
 * Command 2: Delete / Unregister Endpoint Device to Gateway
 * Flow 1 --> App->Broker->GW: Delete specified water timer to gateway
 */
@NonNullByDefault
public class Command2Test {

    /**
     * Command 1:
     * Flow 1 --> App->Broker->GW: Delete specified water timer to gateway encoding test
     */
    @Test
    public void DeleteDeviceRequestEncoding() {
        final GatewayEndDevListReq req = new GatewayEndDevListReq();
        req.command = CMD_REMOVE_END_DEVICE;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.endDevices = new String[] {"1111222233334444","7777888933336666"};

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"end_dev\":[\"1111222233334444\",\"7777888933336666\"],\"cmd\":2,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 2:
     * Flow 2 --> App->Broker->GW: Delete specified water timer to gateway response decoding test
     */
    @Test
    public void DeleteDeviceRequestResponseDecoding() {
        final GatewayDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":2, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"ret\":0\n}",GatewayDeviceResponse.class);

        assertEquals(CMD_REMOVE_END_DEVICE,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }

}
