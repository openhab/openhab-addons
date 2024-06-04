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

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_ADD_END_DEVICE;

/**
 * Command 1: Add / Register Endpoint Device to Gateway
 * Flow 1 --> App->Broker->GW: Add specified water timer to gateway
 */
@NonNullByDefault
public class Command1Test {

    /**
     * Command 1:
     * Flow 1 --> App->Broker->GW: Add specified water timer to gateway encoding test
     */
    @Test
    public void AddDeviceRequestEncoding() {
        final GatewayEndDevListReq req = new GatewayEndDevListReq();
        req.command = CMD_ADD_END_DEVICE;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.endDevices = new String[] {"11112222333344448888","77778889333366661111"};

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"end_dev\":[\"11112222333344448888\",\"77778889333366661111\"],\"cmd\":1,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 1:
     * Flow 1 --> App->Broker->GW: Add specified water timer to gateway response decoding test
     */
    @Test
    public void AddDeviceRequestResponseDecoding() {
        final GatewayDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":1, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"ret\":0\n" +
                "}",GatewayDeviceResponse.class);

        assertEquals(CMD_ADD_END_DEVICE,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }

}
