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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_GET_CONFIGURATION;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_WIRELESS_CHECK;

/**
 * Command 16: Get gateway configuration
 * Flow 1 --> App->Broker->GW: Request the configuration of the Gateway
 */
@NonNullByDefault
public class Command16Test {

    /**
     * Command 16: Get gateway configuration
     * Flow 1 --> App->Broker->GW: Request the configuration of the Gateway
     */
    @Test
    public void RequestGatewayConfigGenerationTest() {
        TLGatewayFrame req = new TLGatewayFrame();
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.command = CMD_GET_CONFIGURATION;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"cmd\":16,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 16: Get gateway configuration
     * Flow 1 --> App->Broker->GW: Request the configuration of the Gateway
     */
    @Test
    public void RequestGatewayConfigResponseDecoding() {
        final GatewayConfigResp decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":16, \"gw_id\":\"1234E607004B1200\", \"ver\":\"G0608062305191832I\", \"vol_unit\":\"gal\", \"end_dev\":[ \"1234A923004B1200\", \"56787022004B1200\", \"ABCD6D13004B1200\"], \"dev_name\":[ \"Name_Of_Device_1234A923004B1200\", \"Name_Of_Device_56787022004B1200\", \"Name_Of_Device_ABCD6D13004B1200\"] }",GatewayConfigResp.class);
        assertEquals(CMD_GET_CONFIGURATION,decoded.command);
        assertEquals("1234E607004B1200",decoded.gatewayId );
        assertEquals("G0608062305191832I",decoded.version );
        assertEquals("gal", decoded.volumeUnit);
        assertEquals(3, decoded.endDevices.length);
        assertTrue(Arrays.asList(decoded.endDevices).contains("1234A923004B1200"));
        assertTrue(Arrays.asList(decoded.endDevices).contains("56787022004B1200"));
        assertTrue(Arrays.asList(decoded.endDevices).contains("ABCD6D13004B1200"));
        assertEquals(3, decoded.deviceNames.length);
        assertTrue(Arrays.asList(decoded.deviceNames).contains("Name_Of_Device_1234A923004B1200"));
        assertTrue(Arrays.asList(decoded.deviceNames).contains("Name_Of_Device_56787022004B1200"));
        assertTrue(Arrays.asList(decoded.deviceNames).contains("Name_Of_Device_ABCD6D13004B1200"));
    }
    
}
