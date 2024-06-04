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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.*;

/**
 * Command 13: Sync Gateway time
 * Flow 1 --> GW->Broker->App: Request from Gateway for the current system time
 */
@NonNullByDefault
public class Command13Test {

    /**
     * Command 13: Sync Gateway time
     * Flow 1 --> GW->Broker->App: Response sent from the App to the Gateway with the time information
     */
    @Test
    public void ResponseForAppTimeRequestGenerationTest() {
        HandshakeResp req = new HandshakeResp();
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.command = CMD_DATETIME_SYNC;
        req.date = "20210501";
        req.time = "123055";
        req.wday = 6;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"date\":\"20210501\",\"time\":\"123055\",\"wday\":6,\"cmd\":13,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 13: Sync Gateway time
     * Flow 1 --> GW->Broker->App: Request from Gateway for the current system time
     */
    @Test
    public void RequestForAppTimeDecoding() {
        final TLGatewayFrame decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":13, \"gw_id\":\"CCCCDDDDEEEEFFFF\"\n}",EndpointDeviceResponse.class);
        assertEquals(CMD_DATETIME_SYNC,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
    }
    
}
