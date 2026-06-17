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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_DURATION;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_RUNNING;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_TYPE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ACTIVE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ACTIVE_ZONE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_COMPLETED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_STARTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_STOPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_SCHEDULE_STARTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_SCHEDULE_STOPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_FORECAST_UNITS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_ZONE;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.RefreshReason;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Tests Cloud Connector configuration update behavior.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioBridgeHandlerConfigurationTest {
    @Test
    void controllerUsesLegacyNotificationServiceWhileSmartHoseResourcesRemainPollingOnly() {
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true),
                is(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.VALVE, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, false),
                is(RachioWebhookMode.DISABLED));
    }

    @Test
    void validLegacyZoneStartedDispatchesBeforeReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("ZONE_STATUS", "ZONE_STARTED");
        Mockito.doReturn(true).when(handler).webHookEvent(event);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, event);
    }

    @Test
    void validLegacyZoneStoppedDispatchesBeforeReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("ZONE_STATUS", "ZONE_STOPPED");
        Mockito.doReturn(true).when(handler).webHookEvent(event);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, event);
    }

    @Test
    void validLegacyScheduleStartedDispatchesBeforeReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STARTED");
        Mockito.doReturn(true).when(handler).webHookEvent(event);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, event);
    }

    @Test
    void legacyControllerValidationUsesCaseInsensitiveDeviceIdentity() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STARTED");
        event.deviceId = "CONTROLLER-ID";
        Mockito.doReturn(true).when(handler).webHookEvent(event);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, event);
    }

    @Test
    void unhandledLegacyEventStillTriggersReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("ZONE_STATUS", "ZONE_STARTED");
        Mockito.doReturn(false).when(handler).webHookEvent(event);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, event);
    }

    @Test
    void legacyZoneStartedDirectlyUpdatesMatchingZoneBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandling("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED, "\"zoneNumber\": 7,",
                OnOffType.ON);
    }

    @Test
    void legacyZoneCompletedDirectlyUpdatesMatchingZoneBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandling("ZONE_COMPLETED", EVENT_DEVICE_ZONE_RUN_COMPLETED, "\"zoneId\": \"zone-id\",",
                OnOffType.OFF);
    }

    @Test
    void legacyZoneStoppedClearsControllerSummaryBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandling("ZONE_STOPPED", EVENT_DEVICE_ZONE_RUN_STOPPED, "\"zoneNumber\": 7,",
                OnOffType.OFF);
    }

    @Test
    void legacyScheduleStartedResolvesRegisteredControllerHandlerBeforeReconciliation() {
        verifyRegisteredControllerHandlerResolution("SCHEDULE_STATUS", "SCHEDULE_STARTED", EVENT_SCHEDULE_STARTED);
    }

    @Test
    void legacyScheduleStoppedResolvesRegisteredControllerHandlerBeforeReconciliation() {
        verifyRegisteredControllerHandlerResolution("SCHEDULE_STATUS", "SCHEDULE_STOPPED", EVENT_SCHEDULE_STOPPED);
    }

    @Test
    void legacyZoneRunWithoutZoneIdentityStillReconciles() {
        RachioBridgeHandler handler = legacyHandler();
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone());
        handler.rachioStatusListeners.add(deviceHandler);
        RachioEventGsonDTO event = realShapedLegacyZoneEvent("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED, "");

        assertThat(handler.legacyWebHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(handler).webHookEvent(event);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void legacyZoneRunWithAmbiguousZoneIdentityStillReconciles() {
        RachioBridgeHandler handler = legacyHandler();
        ThingHandlerCallback deviceCallback = Mockito.mock(ThingHandlerCallback.class);
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone(), deviceCallback);
        handler.rachioStatusListeners.add(deviceHandler);
        RachioEventGsonDTO event = realShapedLegacyZoneEvent("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED,
                "\"zoneId\": \"zone-id\", \"zoneNumber\": 8,");

        assertThat(handler.legacyWebHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(handler).webHookEvent(event);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        verify(deviceCallback, never()).stateUpdated(
                new ChannelUID(deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER),
                new DecimalType(7));
        verify(deviceCallback, never()).stateUpdated(
                new ChannelUID(deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.ON);
    }

    @Test
    void legacyEventForUnknownControllerIsRejectedWithoutRefresh() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge));
        Mockito.doReturn(new HashMap<String, RachioDevice>()).when(handler).getDevices();
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.deviceId = "unknown-controller";
        event.type = "ZONE_STATUS";

        assertThat(handler.legacyWebHookEvent(event), is(false));

        verify(handler, never()).webHookEvent(event);
        verify(handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void scheduledCorePollUsesMediumPriority() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge);

        assertThat(handler.getRefreshPriority(RefreshReason.SCHEDULED_POLL), is(PRIORITY.MED));
        assertThat(handler.getRequestPurpose(RefreshReason.SCHEDULED_POLL), is(RequestPurpose.CORE_STATUS_POLL));
    }

    @Test
    void handleConfigurationUpdatePersistsCloudThingConfiguration() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge")
                .withConfiguration(new Configuration(Map.of(PARAM_FORECAST_UNITS, "METRIC"))).build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleConfigurationUpdate(Map.of(PARAM_FORECAST_UNITS, "US"));

        assertThat(bridge.getConfiguration().get(PARAM_FORECAST_UNITS), is("US"));
        verify(callback).thingUpdated(bridge);
    }

    private RachioBridgeHandler legacyHandler() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge));
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(cloudDevice.id, new RachioDevice(cloudDevice));
        Mockito.doReturn(devices).when(handler).getDevices();
        Mockito.doNothing().when(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        return handler;
    }

    private RachioEventGsonDTO legacyEvent(String type, String subType) {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.deviceId = "controller-id";
        event.type = type;
        event.subType = subType;
        return event;
    }

    private void verifyLegacyZoneRunDirectHandling(String subType, String eventType, String zoneIdentity,
            OnOffType expectedState) throws Exception {
        RachioBridgeHandler handler = legacyHandler();
        RachioDevice boundDevice = deviceWithZone();
        RachioZone zone = Objects.requireNonNull(boundDevice.getZoneByNumber(7));
        if (expectedState == OnOffType.OFF) {
            boundDevice.applyActiveZoneEvent("ZONE_STARTED", 7, zone);
            boundDevice.currentScheduleName = "Quick Run";
            boundDevice.currentScheduleType = "MANUAL";
            boundDevice.currentScheduleDuration = 120;
            boundDevice.currentScheduleRunning = true;
        }
        Thing zoneThing = thing(THING_TYPE_ZONE, "zone");
        RachioZoneHandler zoneHandler = new RachioZoneHandler(zoneThing);
        setField(zoneHandler, "dev", boundDevice);
        setField(zoneHandler, "zone", zone);
        ThingHandlerCallback zoneCallback = Mockito.mock(ThingHandlerCallback.class);
        zoneHandler.setCallback(zoneCallback);
        zone.setThingHandler(zoneHandler);

        ThingHandlerCallback deviceCallback = Mockito.mock(ThingHandlerCallback.class);
        RachioDeviceHandler deviceHandler = deviceHandler(boundDevice, deviceCallback);
        handler.rachioStatusListeners.add(deviceHandler);
        RachioEventGsonDTO event = realShapedLegacyZoneEvent(subType, eventType, zoneIdentity);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        Thing deviceThing = deviceHandler.getThing();
        InOrder order = Mockito.inOrder(handler, deviceHandler, zoneCallback, deviceCallback);
        order.verify(handler).webHookEvent(event);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(zoneCallback).stateUpdated(new ChannelUID(zoneThing.getUID(), CHANNEL_ZONE_RUN), expectedState);
        order.verify(deviceCallback).stateUpdated(new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE),
                OnOffType.ON);
        if (expectedState == OnOffType.ON) {
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER), new DecimalType(7));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NAME),
                    new StringType("Front lawn"));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_NAME), new StringType("Quick Run"));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_TYPE), new StringType("MANUAL"));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_DURATION),
                    RachioQuantityTypes.seconds(120));
            order.verify(deviceCallback)
                    .stateUpdated(new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.ON);
        } else {
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER), UnDefType.NULL);
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NAME), UnDefType.NULL);
            order.verify(deviceCallback)
                    .stateUpdated(new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_NAME), UnDefType.UNDEF);
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_DURATION),
                    RachioQuantityTypes.seconds(0));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.OFF);
        }
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private void verifyRegisteredControllerHandlerResolution(String type, String subType, String eventType) {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent(type, subType);
        event.eventType = eventType;
        RachioCloudDevice boundCloudDevice = new RachioCloudDevice();
        boundCloudDevice.id = event.deviceId;
        RachioDeviceHandler deviceHandler = Mockito.spy(new RachioDeviceHandler(Mockito.mock(Thing.class)));
        deviceHandler.dev = new RachioDevice(boundCloudDevice);
        Mockito.doReturn(true).when(deviceHandler).webhookEvent(event);
        handler.rachioStatusListeners.add(deviceHandler);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(handler).webHookEvent(event);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private RachioDeviceHandler deviceHandler(RachioDevice device) {
        return deviceHandler(device, Mockito.mock(ThingHandlerCallback.class));
    }

    private RachioDeviceHandler deviceHandler(RachioDevice device, ThingHandlerCallback callback) {
        RachioDeviceHandler deviceHandler = Mockito
                .spy(new RachioDeviceHandler(thing(THING_TYPE_DEVICE, "controller")));
        deviceHandler.dev = device;
        deviceHandler.thingId = device.name;
        deviceHandler.setCallback(callback);
        return deviceHandler;
    }

    private RachioDevice deviceWithZone() {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        cloudDevice.name = "Controller";
        cloudDevice.status = "ONLINE";
        RachioCloudZone cloudZone = new RachioCloudZone();
        cloudZone.id = "zone-id";
        cloudZone.name = "Front lawn";
        cloudZone.zoneNumber = 7;
        cloudDevice.zones.add(cloudZone);
        return new RachioDevice(cloudDevice);
    }

    private RachioEventGsonDTO realShapedLegacyZoneEvent(String subType, String eventType, String zoneIdentity) {
        return Objects.requireNonNull(new Gson().fromJson("""
                {
                  "type": "ZONE_STATUS",
                  "subType": "%s",
                  "category": "ZONE",
                  "eventType": "%s",
                  "deviceId": "controller-id",
                  "externalId": "external-id",
                  "zoneName": "Front lawn",
                  "scheduleName": "Quick Run",
                  "scheduleType": "MANUAL",
                  %s
                  "zoneRunStatus": {
                    "duration": 120,
                    "scheduleType": "MANUAL",
                    "zoneNumber": 0,
                    "state": ""
                  }
                }
                """.formatted(subType, eventType, zoneIdentity), RachioEventGsonDTO.class));
    }

    private Thing thing(ThingTypeUID thingType, String id) {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID(thingType, "bridge", id));
        return thing;
    }

    private void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void verifyDispatchBeforeReconciliation(RachioBridgeHandler handler, RachioEventGsonDTO event) {
        InOrder order = Mockito.inOrder(handler);
        order.verify(handler).webHookEvent(event);
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }
}
