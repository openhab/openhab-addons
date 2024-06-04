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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_NOTIFICATION_WATERING_SKIPPED;

/**
 * Command 9: Watering Skipped Notification
 * Flow 1 --> App->Broker->GW: Notification that watering was skipped with the rainfall data
 */
@NonNullByDefault
public class Command9Test {

    /**
     * Command 9:
     * Flow 1 --> GW->Broker->App: Notification that watering was skipped with the rainfall data
     */
    @Test
    public void NotificationWateringSkippedRequestDecoding() {
        final WateringSkippedNotification decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":9, \"gw_id\":\"CCCCDDDDEEEEFFFF\",\n\"dev_id\":\"1111222233334444\", \"rain\":[2.5,6.3]\n}",WateringSkippedNotification.class);

        assertNotNull(decoded);
        assertEquals(CMD_NOTIFICATION_WATERING_SKIPPED,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals("1111222233334444", decoded.deviceId);
        assertEquals(2, decoded.rainfallData.length);
        assertEquals(2.5,decoded.rainfallData[0]);
        assertEquals(6.3,decoded.rainfallData[1]);
    }
}
