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

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.linktap.protocol.frames.TLGatewayFrame.CMD_UPDATE_WATER_TIMER_STATUS;

/**
 * Command 3: Water Timer Status Update Notification
 * Flow 1 --> GW->Broker->App: Notification that there is a update to one or more water timer's
 *   Default format --> object's within an array
 *   Optional format --> object not wrapped within an array
 * Flow 2 --> App->Broker->GW: Request Water Timer Status
 *
 * (ret is only provided in case of an error so -1 would be the same as if 0 was provided)
 */
@NonNullByDefault
public class Command3Test {

    /**
     * Command 3: Water Timer Status Update Notification
     * Flow 1 --> GW->Broker->App: Notification that there is a update to one or more water timer's
     *   Default format --> object's within an array
     *   Optional format --> object not wrapped within an array
     * Flow 2 --> App->Broker->GW: Request Water Timer Status
     */
    @Test
    public void NotificationTimerUpdateRequest1Decoding() {
        final WaterMeterStatus decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":3, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_stat\": [ { \"dev_id\":\"1111222233334444\", \"plan_mode\":2, \"plan_sn\":3134, \"is_rf_linked\":true, \"is_flm_plugin\":false, \"is_fall\":false, \"is_broken\":false, \"is_cutoff\":false, \"is_leak\":false, \"is_clog\":false, \"signal\":100, \"battery\":0, \"child_lock\":0, \"is_manual_mode\":false, \"is_watering\":false, \"is_final\":true, \"total_duration\":0, \"remain_duration\":0, \"speed\":0, \"volume\":0\n} ]\n}",WaterMeterStatus.class);

        assertEquals(CMD_UPDATE_WATER_TIMER_STATUS,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertNotNull(decoded.deviceStatuses);
        assertEquals(1,decoded.deviceStatuses.size());
        assertEquals("1111222233334444",decoded.deviceStatuses.get(0).deviceId);
        assertEquals(2,decoded.deviceStatuses.get(0).planMode);
        assertEquals(3134,decoded.deviceStatuses.get(0).planSerialNo);
        assertTrue(decoded.deviceStatuses.get(0).isRfLinked);
        assertFalse(decoded.deviceStatuses.get(0).isFlmPlugin);
        assertFalse(decoded.deviceStatuses.get(0).isBroken);
        assertFalse(decoded.deviceStatuses.get(0).isCutoff);
        assertFalse(decoded.deviceStatuses.get(0).isLeak);
        assertFalse(decoded.deviceStatuses.get(0).isClog);
        assertEquals(100,decoded.deviceStatuses.get(0).signal);
        assertEquals(0,decoded.deviceStatuses.get(0).battery);
        assertEquals(0,decoded.deviceStatuses.get(0).childLock);
        assertFalse(decoded.deviceStatuses.get(0).isManualMode);
        assertFalse(decoded.deviceStatuses.get(0).isWatering);
        assertTrue(decoded.deviceStatuses.get(0).isFinal);
        assertEquals(0,decoded.deviceStatuses.get(0).totalDuration);
        assertEquals(0,decoded.deviceStatuses.get(0).remainDuration);
        assertEquals(0,decoded.deviceStatuses.get(0).speed);
        assertEquals(0,decoded.deviceStatuses.get(0).volume);
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes()); // Only given in case of error
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }

    /**
     * Command 3: Water Timer Status Update Notification
     * Flow 1 --> GW->Broker->App: Notification that there is a update to one water timer
     * Optional format --> object's without array wrapper
     */
    @Test
    public void NotificationTimerUpdateRequest2Decoding() {
        final WaterMeterStatus decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":3, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"dev_stat\": { \"dev_id\":\"1111222233334444\", \"plan_mode\":2, \"plan_sn\":3134, \"is_rf_linked\":true, \"is_flm_plugin\":false, \"is_fall\":false, \"is_broken\":false, \"is_cutoff\":false, \"is_leak\":false, \"is_clog\":false, \"signal\":100, \"battery\":0, \"child_lock\":0, \"is_manual_mode\":false, \"is_watering\":false, \"is_final\":true, \"total_duration\":0, \"remain_duration\":0, \"speed\":0, \"volume\":0\n}\n}",WaterMeterStatus.class);

        assertEquals(CMD_UPDATE_WATER_TIMER_STATUS,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals(1,decoded.deviceStatuses.size());
        assertEquals("1111222233334444",decoded.deviceStatuses.get(0).deviceId);
        assertEquals(2,decoded.deviceStatuses.get(0).planMode);
        assertEquals(3134,decoded.deviceStatuses.get(0).planSerialNo);
        assertTrue(decoded.deviceStatuses.get(0).isRfLinked);
        assertFalse(decoded.deviceStatuses.get(0).isFlmPlugin);
        assertFalse(decoded.deviceStatuses.get(0).isBroken);
        assertFalse(decoded.deviceStatuses.get(0).isCutoff);
        assertFalse(decoded.deviceStatuses.get(0).isLeak);
        assertFalse(decoded.deviceStatuses.get(0).isClog);
        assertEquals(100,decoded.deviceStatuses.get(0).signal);
        assertEquals(0,decoded.deviceStatuses.get(0).battery);
        assertEquals(0,decoded.deviceStatuses.get(0).childLock);
        assertFalse(decoded.deviceStatuses.get(0).isManualMode);
        assertFalse(decoded.deviceStatuses.get(0).isWatering);
        assertTrue(decoded.deviceStatuses.get(0).isFinal);
        assertEquals(0,decoded.deviceStatuses.get(0).totalDuration);
        assertEquals(0,decoded.deviceStatuses.get(0).remainDuration);
        assertEquals(0,decoded.deviceStatuses.get(0).speed);
        assertEquals(0,decoded.deviceStatuses.get(0).volume);
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes()); // Only given in case of error
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_SUCCESS,decoded.getRes());
    }

    /**
     * Command 3: Water Timer Status Update Notification
     * Flow 2 --> App->Broker->GW: Request Water Timer Status
     */
    @Test
    public void RequestWaterMeterStatusGenerationTest() {
        DeviceCmdReq req = new DeviceCmdReq();
        req.gatewayId = "CCCCDDDDEEEEFFFF";
        req.deviceId = "1111222233334444";
        req.command = CMD_UPDATE_WATER_TIMER_STATUS;

        String encoded = LinkTapBindingConstants.GSON.toJson(req);

        assertEquals("{\"dev_id\":\"1111222233334444\",\"cmd\":3,\"gw_id\":\"CCCCDDDDEEEEFFFF\"}",
                encoded);
    }

    /**
     * Command 3: Water Timer Status Update Notification
     * Flow 2 --> App->Broker->GW: Request Water Timer Status
     */
    @Test
    public void RequestWaterMeterStatusErrorResponseDecoding() {
        final WaterMeterStatus decoded = LinkTapBindingConstants.GSON.fromJson("{ \"cmd\":3, \"gw_id\":\"CCCCDDDDEEEEFFFF\", \"ret\":5\n}",WaterMeterStatus.class);

        assertEquals(CMD_UPDATE_WATER_TIMER_STATUS,decoded.command);
        assertEquals("CCCCDDDDEEEEFFFF",decoded.gatewayId );
        assertEquals(GatewayDeviceResponse.ResultStatus.RET_DEVICE_NOT_FOUND,decoded.getRes());
    }
}
