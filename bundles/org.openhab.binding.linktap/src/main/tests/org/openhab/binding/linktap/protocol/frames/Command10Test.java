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
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_ALERT_ENABLEMENT;

/**
 * Command 10: Enable or Disable alert type
 * Flow 1 --> App->Broker->GW: Enable or Disable the given alert type
 */
@NonNullByDefault
public class Command10Test {

    /**
     * Command 10: Enable or Disable alert type
     * Flow 1 --> App->Broker->GW: Enable or Disable the given alert type
     */
    @Test
    public void ChangeAlertStateGenerationTest() {
        AlertStateReq req = new AlertStateReq();
        req.command = CMD_ALERT_ENABLEMENT;
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.alert = 0;
        req.enable = true;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"enable\":true,\"alert\":0,\"dev_id\":\"1111222233334444\",\"cmd\":10,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 11: Dismiss Alert
     * Flow 1 --> App->Broker->GW: Dismiss the specified alert type reply
     */
    @Test
    public void ChangeAlertStateResponseDecoding() {
        final EndpointDeviceResponse decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":10, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_id\":\"1111222233334444\", \"ret\":0\n}",EndpointDeviceResponse.class);

        assertEquals(CMD_ALERT_ENABLEMENT,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444",decoded.deviceId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }


}
