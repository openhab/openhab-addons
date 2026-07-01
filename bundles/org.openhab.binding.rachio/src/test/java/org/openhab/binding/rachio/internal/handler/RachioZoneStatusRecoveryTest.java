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
package org.openhab.binding.rachio.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_RUN_TOTAL;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_ZONE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_ZONE;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.RefreshReason;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.State;

/**
 * Tests zone Thing status recovery after successful cloud polling.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioZoneStatusRecoveryTest {
    private static final String DEVICE_ID = "controller-id";
    private static final String ZONE_ID = "zone-id";

    @Test
    void unchangedZoneAndDeviceHandlersAreRefreshedAfterSuccessfulPoll() {
        RachioBridgeHandler bridgeHandler = bridgeHandler();
        RachioDevice currentDevice = device("ONLINE", 60);
        RachioDevice polledDevice = device("ONLINE", 60);
        RachioDeviceHandler deviceHandler = Mockito.mock(RachioDeviceHandler.class);
        RachioZoneHandler zoneHandler = Mockito.mock(RachioZoneHandler.class);
        currentDevice.setThingHandler(deviceHandler);
        currentDevice.getZones().get(ZONE_ID).setThingHandler(zoneHandler);

        bridgeHandler.reconcileDeviceAndZones(currentDevice, polledDevice, RefreshReason.SCHEDULED_POLL);

        verify(deviceHandler).refreshThingStatusAfterSuccessfulCommunication();
        verify(zoneHandler).refreshThingStatusAfterSuccessfulCommunication();
        verify(zoneHandler, never()).onThingStateChanged(any(), any());
        verify(deviceHandler).retryDeferredWebhookRegistrationIfDue();
        verify(deviceHandler).refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL,
                RefreshReason.SCHEDULED_POLL);
    }

    @Test
    void changedZoneStillUsesStateChangePath() {
        RachioBridgeHandler bridgeHandler = bridgeHandler();
        RachioDevice currentDevice = device("ONLINE", 60);
        RachioDevice polledDevice = device("ONLINE", 90);
        RachioDeviceHandler deviceHandler = Mockito.mock(RachioDeviceHandler.class);
        RachioZoneHandler zoneHandler = Mockito.mock(RachioZoneHandler.class);
        currentDevice.setThingHandler(deviceHandler);
        currentDevice.getZones().get(ZONE_ID).setThingHandler(zoneHandler);
        RachioZone polledZone = polledDevice.getZones().get(ZONE_ID);

        bridgeHandler.reconcileDeviceAndZones(currentDevice, polledDevice, RefreshReason.SCHEDULED_POLL);

        verify(zoneHandler).onThingStateChanged(null, polledZone);
        verify(zoneHandler, never()).refreshThingStatusAfterSuccessfulCommunication();
        verify(deviceHandler).refreshThingStatusAfterSuccessfulCommunication();
    }

    @Test
    void offlineZoneRecoversOnlineWithoutChannelUpdatesWhenControllerIsOnline() {
        Thing thing = zoneThing();
        RachioDevice device = device("ONLINE", 60);
        TestZoneHandler handler = initializedHandler(thing, device, device.getZones().get(ZONE_ID), ThingStatus.ONLINE);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        handler.initialize();
        handler.markOffline();
        clearInvocations(callback);

        handler.refreshThingStatusAfterSuccessfulCommunication();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
    }

    @Test
    void successfulPollRebindsZoneHandlerToFreshBridgeModel() {
        Thing thing = zoneThing();
        RachioDevice staleDevice = device("ONLINE", 60);
        RachioDevice freshDevice = device("ONLINE", 60);
        RachioZone staleZone = Objects.requireNonNull(staleDevice.getZones().get(ZONE_ID));
        RachioZone freshZone = Objects.requireNonNull(freshDevice.getZones().get(ZONE_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        when(bridgeHandler.getZoneByThing(thing)).thenReturn(staleZone, freshZone);
        when(bridgeHandler.getDevForZone(staleZone)).thenReturn(staleDevice);
        when(bridgeHandler.getDevForZone(freshZone)).thenReturn(freshDevice);
        TestZoneHandler handler = new TestZoneHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();
        assertThat(staleZone.getThingHandler(), is(handler));
        clearInvocations(callback);

        handler.refreshThingStatusAfterSuccessfulCommunication();

        assertThat(freshZone.getThingHandler(), is(handler));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void changedZoneStillUpdatesChannelsAndKeepsThingOnlineWhenControllerTelemetryIsOffline() {
        Thing thing = zoneThing();
        RachioDevice device = device("OFFLINE", 60);
        TestZoneHandler handler = initializedHandler(thing, device, device.getZones().get(ZONE_ID), ThingStatus.ONLINE);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback);

        boolean handled = handler.onThingStateChanged(null, new RachioZone(cloudZone(90), DEVICE_ID));

        assertThat(handled, is(true));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        verify(callback, atLeastOnce()).stateUpdated(eq(new ChannelUID(thing.getUID(), CHANNEL_ZONE_RUN_TOTAL)),
                any(State.class));
    }

    @Test
    void unresolvedZoneKeepsConfigurationErrorAndIsNotMarkedOnline() {
        Thing thing = zoneThing();
        RachioDevice device = device("ONLINE", 60);
        TestZoneHandler handler = initializedHandler(thing, device, null, ThingStatus.ONLINE);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
        clearInvocations(callback);

        handler.refreshThingStatusAfterSuccessfulCommunication();

        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void unresolvedControllerIsNotMarkedOnline() {
        Thing thing = zoneThing();
        RachioZone zone = device("ONLINE", 60).getZones().get(ZONE_ID);
        TestZoneHandler handler = initializedHandler(thing, null, zone, ThingStatus.ONLINE);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback);

        handler.refreshThingStatusAfterSuccessfulCommunication();

        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void offlineBridgePreventsZoneFromBeingMarkedOnline() {
        Thing thing = zoneThing();
        RachioDevice device = device("ONLINE", 60);
        TestZoneHandler handler = initializedHandler(thing, device, device.getZones().get(ZONE_ID),
                ThingStatus.OFFLINE);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        handler.initialize();
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE));
        clearInvocations(callback);

        handler.refreshThingStatusAfterSuccessfulCommunication();

        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void offlineControllerTelemetryDoesNotMarkResolvedZoneOfflineAfterSuccessfulPoll() {
        Thing thing = zoneThing();
        RachioDevice device = device("OFFLINE", 60);
        TestZoneHandler handler = initializedHandler(thing, device, device.getZones().get(ZONE_ID), ThingStatus.ONLINE);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback);

        handler.refreshThingStatusAfterSuccessfulCommunication();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    private RachioBridgeHandler bridgeHandler() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        return new RachioBridgeHandler(bridge);
    }

    private TestZoneHandler initializedHandler(Thing thing, @Nullable RachioDevice device, @Nullable RachioZone zone,
            ThingStatus bridgeStatus) {
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        when(bridgeHandler.getZoneByThing(thing)).thenReturn(zone);
        if (zone != null) {
            when(bridgeHandler.getDevForZone(zone)).thenReturn(device);
        }
        return new TestZoneHandler(thing, bridgeHandler, bridgeStatus);
    }

    private Thing zoneThing() {
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_ZONE, "bridge", "zone"));
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of(PROPERTY_ZONE_ID, ZONE_ID)));
        when(thing.getProperties()).thenReturn(new HashMap<>());
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        return thing;
    }

    private RachioDevice device(String status, int zoneRuntime) {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = DEVICE_ID;
        cloudDevice.name = "Controller";
        cloudDevice.status = status;
        cloudDevice.zones.add(cloudZone(zoneRuntime));
        return new RachioDevice(cloudDevice);
    }

    private RachioCloudZone cloudZone(int runtime) {
        RachioCloudZone zone = new RachioCloudZone();
        zone.id = ZONE_ID;
        zone.name = "Front lawn";
        zone.zoneNumber = 1;
        zone.runtime = runtime;
        return zone;
    }

    private static class TestZoneHandler extends RachioZoneHandler {
        private final RachioBridgeHandler bridgeHandler;
        private final Bridge bridgeThing = Mockito.mock(Bridge.class);

        TestZoneHandler(Thing thing, RachioBridgeHandler bridgeHandler, ThingStatus bridgeStatus) {
            super(thing);
            this.bridgeHandler = bridgeHandler;
            when(bridgeThing.getStatus()).thenReturn(bridgeStatus);
        }

        void markOffline() {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        @Override
        protected boolean initializeCloudHandler() {
            cloudHandler = bridgeHandler;
            bridge = bridgeThing;
            return true;
        }
    }
}
