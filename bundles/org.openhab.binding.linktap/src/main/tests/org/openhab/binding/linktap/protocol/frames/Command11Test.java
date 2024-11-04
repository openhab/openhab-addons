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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_RAINFALL_DATA;

/**
 * Command 11: Dismiss Alert
 * Flow 1 --> App->Broker->GW: Dismiss the specified alert type
 */
@NonNullByDefault
public class Command11Test {

    /**
     * Command 11: Dismiss Alert
     * Flow 1 --> App->Broker->GW: Dismiss the specified alert type
     */
    @Test
    public void DismissAlertGenerationTest() {
        DismissAlertReq req = new DismissAlertReq();
        req.command = CMD_ALERT_DISMISS;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.alert = 0;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"alert\":0,\"dev_id\":\"1111222233334444\",\"cmd\":11,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 11: Dismiss Alert
     * Flow 1 --> App->Broker->GW: Dismiss the specified alert type reply
     */
    @Test
    public void DismissAlertResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":11, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1111222233334444\", \"ret\":0\n}",EndpointDeviceResponse.class);

        assertEquals(CMD_ALERT_DISMISS,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444",decoded.deviceId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }


}
