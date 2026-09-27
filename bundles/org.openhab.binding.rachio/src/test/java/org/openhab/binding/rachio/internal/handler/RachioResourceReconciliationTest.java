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
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.RefreshReason;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests resource disappearance and recovery through successive successful bridge polls.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class RachioResourceReconciliationTest {
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final RachioApi polledApi = mock(RachioApi.class);
    private final Bridge bridge = mock(Bridge.class);
    private final RachioBridgeHandler handler;

    RachioResourceReconciliationTest() {
        when(bridge.getUID()).thenReturn(new ThingUID(THING_TYPE_CLOUD, "bridge"));
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridge.getProperties()).thenReturn(new HashMap<>());
        handler = new RachioBridgeHandler(bridge, mock(HttpClient.class)) {
            @Override
            RachioApi createRachioApi(String personId) {
                return polledApi;
            }

            @Override
            protected void updateListenerManagement() {
                // Polls are driven explicitly by each test.
            }
        };
        handler.getWebhookConfiguration().apikey = "api-key";
        handler.setCallback(callback);
        when(polledApi.getLastApiResult()).thenReturn(new RachioApiResult());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void controllerAndZonesRecoverAfterPartialOrEmptyPoll(boolean emptyAccount) {
        RachioDevice original = device("controller", "zone");
        RachioDevice sibling = device("other", "other-zone");
        poll(Map.of(original.id, original, sibling.id, sibling));
        RachioDeviceHandler controller = controller("controller");
        RachioZoneHandler zone = zone("zone");
        RachioDeviceHandler otherController = controller("other");
        RachioZoneHandler otherZone = zone("other-zone");
        clearInvocations(callback);

        poll(emptyAccount ? Map.of() : Map.of(sibling.id, device("other", "other-zone")));

        assertOffline(controller.getThing());
        assertOffline(zone.getThing());
        if (!emptyAccount) {
            assertNeverOffline(otherController.getThing());
            assertNeverOffline(otherZone.getThing());
        }
        assertThat(handler.getWebhookApi().getDevices().containsKey(original.id), is(false));
        clearInvocations(callback);

        RachioDevice returned = device("controller", "zone");
        poll(Map.of(returned.id, returned, sibling.id, device("other", "other-zone")));

        assertThat(returned.getThingHandler(), sameInstance(controller));
        assertThat(returned.getZones().get("zone").getThingHandler(), sameInstance(zone));
        assertThat(returned.getUID(), is(controller.getThing().getUID()));
        assertThat(returned.getZones().get("zone").getUID(), is(zone.getThing().getUID()));
        assertOnline(controller.getThing());
        assertOnline(zone.getThing());
        clearInvocations(callback);

        RachioDevice changed = device("controller", "zone");
        changed.name = "Renamed controller";
        changed.status = "OFFLINE";
        changed.getZones().get("zone").name = "Renamed zone";
        poll(Map.of(changed.id, changed, sibling.id, device("other", "other-zone")));

        verify(callback).stateUpdated(new ChannelUID(controller.getThing().getUID(), CHANNEL_DEVICE_NAME),
                new StringType("Renamed controller"));
        verify(callback).stateUpdated(new ChannelUID(zone.getThing().getUID(), CHANNEL_ZONE_NAME),
                new StringType("Renamed zone"));
    }

    @Test
    void zoneRecoversWhileItsControllerAndSiblingZoneStayPresent() {
        RachioDevice original = device("controller", "zone", "sibling-zone");
        poll(Map.of(original.id, original));
        RachioDeviceHandler controller = controller("controller");
        RachioZoneHandler zone = zone("zone");
        RachioZoneHandler sibling = zone("sibling-zone");
        clearInvocations(callback);

        poll(Map.of(original.id, device("controller", "sibling-zone")));

        assertOffline(zone.getThing());
        assertNeverOffline(controller.getThing());
        assertNeverOffline(sibling.getThing());
        clearInvocations(callback);

        poll(Map.of(original.id, device("controller", "zone", "sibling-zone")));

        assertThat(original.getZones().get("zone").getThingHandler(), sameInstance(zone));
        assertOnline(zone.getThing());
        assertNeverOffline(controller.getThing());
        assertNeverOffline(sibling.getThing());
    }

    @Test
    void failedPollRetainsCatalogAndDoesNotReportResourcesAsMissing() throws Exception {
        RachioDevice original = device("controller", "zone");
        poll(Map.of(original.id, original));
        RachioDeviceHandler controller = controller("controller");
        RachioZoneHandler zone = zone("zone");
        clearInvocations(callback);
        ThingUID bridgeUID = bridge.getUID();
        doThrow(new RachioApiException("request failed")).when(polledApi).initialize(eq("api-key"), eq(bridgeUID),
                any(Priority.class), any(RequestPurpose.class));

        poll(Map.of());

        assertThat(handler.getWebhookApi().getDevices().get(original.id), sameInstance(original));
        assertThat(original.getThingHandler(), sameInstance(controller));
        assertThat(original.getZones().get("zone").getThingHandler(), sameInstance(zone));
        assertOffline(bridge);
        assertNeverOffline(controller.getThing());
        assertNeverOffline(zone.getThing());
    }

    private void poll(Map<String, RachioDevice> devices) {
        when(polledApi.getDevices()).thenReturn(devices);
        handler.refreshDeviceStatus(RefreshReason.SCHEDULED_POLL);
    }

    private RachioDeviceHandler controller(String id) {
        RachioDeviceHandler child = new RachioDeviceHandler(thing(THING_TYPE_DEVICE, PROPERTY_DEV_ID, id)) {
            @Override
            protected boolean initializeCloudHandler() {
                cloudHandler = handler;
                bridge = RachioResourceReconciliationTest.this.bridge;
                return true;
            }

            @Override
            protected void scheduleHandlerTask(long generation, Runnable task) {
                // Webhook registration and optional REST enrichment are outside catalogue reconciliation.
            }

            @Override
            public void refreshSmartIrrigationReadExtensions(boolean force, RequestPurpose purpose,
                    RefreshReason reason) {
            }
        };
        child.setCallback(callback);
        child.initialize();
        return child;
    }

    private RachioZoneHandler zone(String id) {
        RachioZoneHandler child = new RachioZoneHandler(thing(THING_TYPE_ZONE, PROPERTY_ZONE_ID, id)) {
            @Override
            protected boolean initializeCloudHandler() {
                cloudHandler = handler;
                bridge = RachioResourceReconciliationTest.this.bridge;
                return true;
            }
        };
        child.setCallback(callback);
        child.initialize();
        return child;
    }

    private Thing thing(ThingTypeUID type, String idProperty, String id) {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(type, "bridge", "custom-" + id));
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of(idProperty, id)));
        when(thing.getProperties()).thenReturn(new HashMap<>());
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        return thing;
    }

    private RachioDevice device(String id, String... zoneIds) {
        RachioCloudDevice device = new RachioCloudDevice();
        device.id = id;
        device.name = id;
        device.status = "ONLINE";
        for (String zoneId : zoneIds) {
            RachioCloudZone zone = new RachioCloudZone();
            zone.id = zoneId;
            zone.name = zoneId;
            zone.zoneNumber = device.zones.size() + 1;
            device.zones.add(zone);
        }
        return new RachioDevice(device);
    }

    private void assertOffline(Thing thing) {
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
    }

    private void assertOnline(Thing thing) {
        verify(callback, atLeastOnce()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    private void assertNeverOffline(Thing thing) {
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }
}
