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
package org.openhab.binding.icloud.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudAccountDataResponse;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudDeviceInformation;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests the status handling of {@link ICloudDeviceHandler} for bridge status changes.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
class ICloudDeviceHandlerTest {

    private static final ThingTypeUID BRIDGE_TYPE = new ThingTypeUID("icloud", "account");
    private static final ThingTypeUID DEVICE_TYPE = new ThingTypeUID("icloud", "device");
    private static final ThingStatusInfo ONLINE = status(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    private static final ThingStatusInfo BRIDGE_OFFLINE = status(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
            null);
    private static final ThingStatusInfo UNKNOWN = status(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);

    private final ICloudAccountBridgeHandler bridgeHandler = mock(ICloudAccountBridgeHandler.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final Bridge bridge = BridgeBuilder.create(BRIDGE_TYPE, new ThingUID(BRIDGE_TYPE, "account")).build();
    private final Thing thing = ThingBuilder.create(DEVICE_TYPE, new ThingUID(DEVICE_TYPE, bridge.getUID(), "device"))
            .withBridge(bridge.getUID()).withConfiguration(new Configuration(Map.of("deviceId", "device-1"))).build();
    private final ICloudDeviceHandler handler = new ICloudDeviceHandler(thing);

    @BeforeEach
    void setUp() {
        bridge.setHandler(bridgeHandler);
        setBridgeStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        when(callback.getBridge(bridge.getUID())).thenReturn(bridge);
        // Apply status updates like the framework does, so the handler sees its own status.
        doAnswer(invocation -> {
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(eq(thing), any());
        thing.setStatusInfo(status(ThingStatus.INITIALIZING, ThingStatusDetail.NONE, null));
        handler.setCallback(callback);
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    private static ThingStatusInfo status(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
        return new ThingStatusInfo(status, detail, description);
    }

    private void setBridgeStatus(ThingStatus status, ThingStatusDetail detail) {
        bridge.setStatusInfo(status(status, detail, null));
    }

    private static List<ICloudDeviceInformation> deviceData(int deviceStatus) {
        ICloudAccountDataResponse response = JsonUtils.fromJson("""
                {"statusCode":"200","content":[{"deviceDiscoveryId":"device-1","name":"Phone","deviceStatus":"%d",\
                "batteryLevel":0.5,"batteryStatus":"Charging"}]}
                """.formatted(deviceStatus), ICloudAccountDataResponse.class);
        assertNotNull(response);
        return response.getICloudDeviceInformationList();
    }

    @Test
    void initializeFetchesDataAsynchronouslyAndIsUnknownUntilDataArrives() {
        AtomicReference<Thread> refreshThread = new AtomicReference<>();
        doAnswer(invocation -> {
            refreshThread.set(Thread.currentThread());
            return null;
        }).when(bridgeHandler).refreshData();

        handler.initialize();

        assertEquals(UNKNOWN, thing.getStatusInfo());
        verify(bridgeHandler).registerListener(handler);
        verify(bridgeHandler, timeout(10_000)).refreshData();
        assertNotSame(Thread.currentThread(), refreshThread.get());

        handler.deviceInformationUpdate(deviceData(200));

        assertEquals(ONLINE, thing.getStatusInfo());
    }

    @Test
    void initializeWithOfflineBridgeWaitsForBridge() {
        setBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);

        handler.initialize();

        assertEquals(BRIDGE_OFFLINE, thing.getStatusInfo());

        setBridgeStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        handler.bridgeStatusChanged(bridge.getStatusInfo());

        assertEquals(UNKNOWN, thing.getStatusInfo());
    }

    @Test
    void bridgeOfflineAndOnlineAgainRestoresOnlineDevice() {
        handler.initialize();
        handler.deviceInformationUpdate(deviceData(200));

        setBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        handler.bridgeStatusChanged(bridge.getStatusInfo());
        assertEquals(BRIDGE_OFFLINE, thing.getStatusInfo());

        setBridgeStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        handler.bridgeStatusChanged(bridge.getStatusInfo());
        assertEquals(ONLINE, thing.getStatusInfo());
    }

    @Test
    void bridgeOnlineAgainRestoresStatusReportedByICloud() {
        handler.initialize();
        handler.deviceInformationUpdate(deviceData(201));
        ThingStatusInfo reportedOffline = thing.getStatusInfo();
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, reportedOffline.getStatusDetail());

        setBridgeStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        handler.bridgeStatusChanged(bridge.getStatusInfo());
        setBridgeStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        handler.bridgeStatusChanged(bridge.getStatusInfo());

        assertEquals(reportedOffline, thing.getStatusInfo());
    }

    @Test
    void outOfOrderBridgeNotificationsDoNotLeaveDeviceOffline() {
        handler.initialize();
        handler.deviceInformationUpdate(deviceData(200));
        ThingStatusInfo bridgeOfflineSnapshot = status(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                null);
        ThingStatusInfo bridgeOnlineSnapshot = status(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // The bridge went offline and online again, but the notifications are delivered in reverse order.
        setBridgeStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        handler.bridgeStatusChanged(bridgeOnlineSnapshot);
        handler.bridgeStatusChanged(bridgeOfflineSnapshot);

        assertEquals(ONLINE, thing.getStatusInfo());
    }

    @Test
    void reinitializingBridgeKeepsDeviceStatus() {
        handler.initialize();
        handler.deviceInformationUpdate(deviceData(200));

        setBridgeStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        handler.bridgeStatusChanged(bridge.getStatusInfo());

        assertEquals(ONLINE, thing.getStatusInfo());
    }

    @Test
    void deviceMissingInAccountIsConfigurationError() {
        handler.initialize();

        handler.deviceInformationUpdate(List.of());

        assertEquals(status(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "The device is not included in the current account"), thing.getStatusInfo());
    }

    @Test
    void knownDeviceTemporarilyMissingKeepsItsStatus() {
        handler.initialize();
        handler.deviceInformationUpdate(deviceData(200));

        for (int i = 1; i < ICloudDeviceHandler.MISSING_DEVICE_TOLERANCE; i++) {
            handler.deviceInformationUpdate(List.of());
            assertEquals(ONLINE, thing.getStatusInfo());
        }
        handler.deviceInformationUpdate(List.of());

        assertEquals(status(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "The device is not included in the current account"), thing.getStatusInfo());
    }

    @Test
    void reappearingDeviceResetsMissingCount() {
        handler.initialize();
        handler.deviceInformationUpdate(deviceData(200));
        for (int i = 1; i < ICloudDeviceHandler.MISSING_DEVICE_TOLERANCE; i++) {
            handler.deviceInformationUpdate(List.of());
        }
        handler.deviceInformationUpdate(deviceData(200));

        for (int i = 1; i < ICloudDeviceHandler.MISSING_DEVICE_TOLERANCE; i++) {
            handler.deviceInformationUpdate(List.of());
        }

        assertEquals(ONLINE, thing.getStatusInfo());
    }

    @Test
    void reportedOfflineIncludesDeviceStatus() {
        handler.initialize();

        handler.deviceInformationUpdate(deviceData(201));

        assertEquals(status(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Reported offline by iCloud webservice (device status 201)"), thing.getStatusInfo());
    }

    @Test
    void pendingLocationUpdateIsOnline() {
        handler.initialize();
        handler.deviceInformationUpdate(deviceData(200));

        handler.deviceInformationUpdate(deviceData(203));

        assertEquals(ONLINE, thing.getStatusInfo());
    }
}
