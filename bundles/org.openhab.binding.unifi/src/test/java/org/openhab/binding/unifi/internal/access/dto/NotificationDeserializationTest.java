/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.access.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Tests Gson deserialization of {@link Notification} payloads including
 * {@link Notification.RemoteViewChangeData.Reason} custom adapter.
 *
 * @author Dan Cunningham - Initial contribution
 */
class NotificationDeserializationTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    // --- Reason custom adapter: int code -> enum ---

    @Test
    void reasonDoorbellTimedOut() {
        Notification.RemoteViewChangeData.Reason result = gson.fromJson("105",
                Notification.RemoteViewChangeData.Reason.class);
        assertEquals(Notification.RemoteViewChangeData.Reason.DOORBELL_TIMED_OUT, result);
    }

    @Test
    void reasonAdminRejectedUnlock() {
        Notification.RemoteViewChangeData.Reason result = gson.fromJson("106",
                Notification.RemoteViewChangeData.Reason.class);
        assertEquals(Notification.RemoteViewChangeData.Reason.ADMIN_REJECTED_UNLOCK, result);
    }

    @Test
    void reasonAdminUnlockSucceeded() {
        Notification.RemoteViewChangeData.Reason result = gson.fromJson("107",
                Notification.RemoteViewChangeData.Reason.class);
        assertEquals(Notification.RemoteViewChangeData.Reason.ADMIN_UNLOCK_SUCCEEDED, result);
    }

    @Test
    void reasonVisitorCanceledDoorbell() {
        Notification.RemoteViewChangeData.Reason result = gson.fromJson("108",
                Notification.RemoteViewChangeData.Reason.class);
        assertEquals(Notification.RemoteViewChangeData.Reason.VISITOR_CANCELED_DOORBELL, result);
    }

    @Test
    void reasonAnsweredByAnotherAdmin() {
        Notification.RemoteViewChangeData.Reason result = gson.fromJson("400",
                Notification.RemoteViewChangeData.Reason.class);
        assertEquals(Notification.RemoteViewChangeData.Reason.ANSWERED_BY_ANOTHER_ADMIN, result);
    }

    @Test
    void reasonUnknownCodeFallsBackToUnknown() {
        Notification.RemoteViewChangeData.Reason result = gson.fromJson("999",
                Notification.RemoteViewChangeData.Reason.class);
        assertEquals(Notification.RemoteViewChangeData.Reason.UNKNOWN, result);
    }

    // --- Full Notification with access.remote_view ---

    @Test
    void remoteViewNotificationDeserialization() {
        String json = """
                {
                    "event": "access.remote_view",
                    "receiver_id": "recv-001",
                    "event_object_id": "obj-001",
                    "save_to_history": true,
                    "data": {
                        "device_id": "dev-abc",
                        "device_type": "UAH",
                        "device_name": "Front Door Hub",
                        "door_name": "Main Entrance",
                        "controller_id": "ctrl-001",
                        "floor_name": "Ground Floor",
                        "request_id": "req-123",
                        "in_or_out": "in",
                        "create_time": 1700000000,
                        "reason_code": 0,
                        "door_guard_ids": ["guard-1", "guard-2"],
                        "connected_uah_id": "uah-001",
                        "room_id": "room-001",
                        "host_device_mac": "AA:BB:CC:DD:EE:FF"
                    }
                }
                """;

        Notification notification = gson.fromJson(json, Notification.class);

        assertNotNull(notification);
        assertEquals("access.remote_view", notification.event);
        assertEquals("recv-001", notification.receiverId);
        assertEquals("obj-001", notification.eventObjectId);
        assertEquals(Boolean.TRUE, notification.saveToHistory);

        Notification.RemoteViewData data = notification.dataAsRemoteView(gson);
        assertNotNull(data);
        assertEquals("dev-abc", data.deviceId);
        assertEquals("UAH", data.deviceType);
        assertEquals("Front Door Hub", data.deviceName);
        assertEquals("Main Entrance", data.doorName);
        assertEquals("ctrl-001", data.controllerId);
        assertEquals("Ground Floor", data.floorName);
        assertEquals("req-123", data.requestId);
        assertEquals("in", data.inOrOut);
        assertEquals(1700000000L, data.createTime);
        assertEquals(0, data.reasonCode);
        assertNotNull(data.doorGuardIds);
        assertEquals(2, data.doorGuardIds.size());
        assertEquals("guard-1", data.doorGuardIds.get(0));
        assertEquals("guard-2", data.doorGuardIds.get(1));
        assertEquals("uah-001", data.connectedUahId);
        assertEquals("room-001", data.roomId);
        assertEquals("AA:BB:CC:DD:EE:FF", data.hostDeviceMac);
    }

    // --- Full Notification with access.remote_view.change ---

    @Test
    void remoteViewChangeNotificationDeserialization() {
        String json = """
                {
                    "event": "access.remote_view.change",
                    "receiver_id": "recv-002",
                    "event_object_id": "obj-002",
                    "save_to_history": false,
                    "data": {
                        "reason": 107,
                        "remote_call_request_id": "call-456"
                    }
                }
                """;

        Notification notification = gson.fromJson(json, Notification.class);

        assertNotNull(notification);
        assertEquals("access.remote_view.change", notification.event);
        assertEquals("recv-002", notification.receiverId);
        assertEquals(Boolean.FALSE, notification.saveToHistory);

        Notification.RemoteViewChangeData data = notification.dataAsRemoteViewChange(gson);
        assertNotNull(data);
        assertEquals(Notification.RemoteViewChangeData.Reason.ADMIN_UNLOCK_SUCCEEDED, data.reason);
        assertEquals("call-456", data.remoteCallRequestId);
    }

    // --- Full Notification with access.logs.insights.add (metadata.device is an ARRAY) ---

    @Test
    void insightsAddNotificationWithMultipleDevicesDeserialization() {
        // Regression: the Access API sends metadata.device as an array (e.g. the hub plus the
        // reader). It used to be modelled as a single object, so deserialization failed with
        // "Expected BEGIN_OBJECT but was BEGIN_ARRAY at path $.metadata.device" and no insight
        // event was ever routed.
        String json = """
                {
                    "event": "access.logs.insights.add",
                    "receiver_id": "recv-003",
                    "event_object_id": "obj-003",
                    "save_to_history": true,
                    "data": {
                        "log_key": "dashboard.access.door.unlock.success",
                        "event_type": "access.door.unlock",
                        "message": "Front Door unlocked",
                        "published": 1700000000000,
                        "result": "ACCESS",
                        "metadata": {
                            "actor": {
                                "display_name": "Jane Doe"
                            },
                            "door": {
                                "id": "door-1",
                                "type": "door",
                                "display_name": "Front Door"
                            },
                            "device": [
                                {
                                    "id": "hub-1",
                                    "type": "UAH",
                                    "display_name": "Front Door Hub"
                                },
                                {
                                    "id": "reader-1",
                                    "type": "UA-G2-PRO",
                                    "display_name": "Front Door Reader"
                                }
                            ]
                        }
                    }
                }
                """;

        Notification notification = gson.fromJson(json, Notification.class);

        assertNotNull(notification);
        assertEquals("access.logs.insights.add", notification.event);

        Notification.InsightLogsAddData data = notification.dataAsInsightLogsAdd(gson);
        assertNotNull(data);
        assertEquals("dashboard.access.door.unlock.success", data.logKey);
        assertEquals("access.door.unlock", data.eventType);
        assertEquals("ACCESS", data.result);
        assertEquals(Long.valueOf(1700000000000L), data.published);

        assertNotNull(data.metadata);
        assertNotNull(data.metadata.door);
        assertEquals("Front Door", data.metadata.door.displayName);

        // the array must deserialize into a list, preserving order
        assertNotNull(data.metadata.device);
        assertEquals(2, data.metadata.device.size());
        assertEquals("hub-1", data.metadata.device.get(0).id);
        assertEquals("UAH", data.metadata.device.get(0).type);
        assertEquals("reader-1", data.metadata.device.get(1).id);
        assertEquals("Front Door Reader", data.metadata.device.get(1).displayName);
    }
}
