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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ONLINE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_RAIN_SENSOR_TRIPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_DEV_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import com.google.gson.Gson;

/**
 * Tests controller status and deferred webhook registration lifecycle.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioDeviceHandlerStatusTest {
    private static final String DEVICE_ID = "fc284477-53a1-4fa7-8afd-ed057a8ff2ea";

    @Test
    void initializationDefersLocalWebhookThrottleButAllowsOnlineControllerToBecomeOnline() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        assertThat(handler.retryScheduled, is(true));
        assertThat(handler.isWebhookRegistrationPending(), is(true));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && String.valueOf(status.getDescription()).contains("Throttling REST API call")));

        Mockito.clearInvocations(callback);
        handler.runScheduledRetry();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void initializationKeepsControllerOnlineWhenWebhookApiFails() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(serverRateLimitException()).when(bridgeHandler).registerWebHook(DEVICE_ID,
                RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && String.valueOf(status.getDescription()).contains("server rate limit")));
    }

    @Test
    void initializationKeepsControllerOnlineWhenWebhookParsingFails() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(new IllegalStateException("Expected STRING but was BEGIN_OBJECT")).when(bridgeHandler)
                .registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && String.valueOf(status.getDescription()).contains("Expected STRING")));
    }

    @Test
    void initializationKeepsThingOnlineWhenRachioControllerReportsOffline() {
        Thing thing = thing();
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler(thing, device("OFFLINE")));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_DEVICE_ONLINE), OnOffType.OFF);
    }

    @Test
    void deviceStatusWebhookEventsUpdateReportedStatusChannelWithoutTakingThingOffline() {
        Thing thing = thing();
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler(thing, device("ONLINE")));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        handler.initialize();
        Mockito.clearInvocations(callback);

        handler.webhookEvent(deviceStatusEvent("OFFLINE"));

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_DEVICE_ONLINE), OnOffType.OFF);
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));

        handler.webhookEvent(deviceStatusEvent("ONLINE"));

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_DEVICE_ONLINE), OnOffType.ON);
    }

    @Test
    void legacyRainSensorDetectionEventsUpdateRainSensorStateForBothTypeForms() {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler(thing, device), device);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        assertThat(handler.webhookEvent(legacyControllerEvent("DEVICE_STATUS", "RAIN_SENSOR_DETECTION_ON")), is(true));
        assertThat(device.rainSensorTripped, is(true));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_DEVICE_RAIN_SENSOR_TRIPPED), OnOffType.ON);

        assertThat(handler.webhookEvent(legacyControllerEvent("RAIN_SENSOR_DETECTION", "RAIN_SENSOR_DETECTION_OFF")),
                is(true));
        assertThat(device.rainSensorTripped, is(false));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_DEVICE_RAIN_SENSOR_TRIPPED),
                OnOffType.OFF);
    }

    @Test
    void legacyZoneStartedEventUsesExistingZoneHandlerPath() {
        assertLegacyZoneEventUsesExistingHandler("ZONE_STARTED");
    }

    @Test
    void legacyZoneStoppedEventUsesExistingZoneHandlerPath() {
        assertLegacyZoneEventUsesExistingHandler("ZONE_STOPPED");
    }

    @Test
    void missingConfiguredControllerRemainsAConfigurationFailure() {
        Thing thing = thing();
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device("ONLINE"));
        when(bridgeHandler.getDevByConfiguredDeviceId(thing, DEVICE_ID)).thenReturn(null);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR
                        && String.valueOf(status.getDescription()).contains("deviceId was not found")));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void currentScheduleRefreshRunsEveryPollWhileOptionalEnrichmentKeepsLongerCadence() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        currentSchedule.running = true;
        currentSchedule.scheduleId = "schedule-id";
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL)).thenReturn(currentSchedule);
        doThrow(optionalThrottle()).when(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.currentScheduleRunning, is(true));
        verify(bridgeHandler, times(2)).getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL);
        verify(bridgeHandler, times(1)).getDeviceForecast(DEVICE_ID, "US");
    }

    @Test
    void immediateWebhookReconciliationPreservesFreshLegacyZoneRunSummary() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());

        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));
        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.BACKGROUND_REFRESH,
                RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);

        assertWebhookRunSummary(context.device, "Quick Run", 120);
    }

    @Test
    void immediateWebhookReconciliationPreservesFreshLegacyScheduleStartedSummary() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());

        assertThat(context.handler.webhookEvent(legacyScheduleStatusEvent("SCHEDULE_STARTED")), is(true));
        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.BACKGROUND_REFRESH,
                RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);

        assertScheduleRunSummary(context.device, "Quick Run", 120);

        assertThat(context.handler.webhookEvent(legacyScheduleStatusEvent("SCHEDULE_COMPLETED")), is(true));
        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.BACKGROUND_REFRESH,
                RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);

        assertIdleRunSummary(context.device);
    }

    @Test
    void scheduledPollPreservesProtectedLegacyRunSummaryBeforeExpectedEnd() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());

        assertThat(context.handler.webhookEvent(legacyScheduleStatusEvent("SCHEDULE_STARTED")), is(true));
        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));
        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL,
                RachioBridgeHandler.RefreshReason.SCHEDULED_POLL);

        assertWebhookRunSummary(context.device, "Quick Run", 120);
    }

    @Test
    void legacyZoneCyclingPausesAndResumesWithoutEndingActiveRun() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());
        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));

        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_CYCLING")), is(true));

        assertThat(context.device.paused, is(true));
        assertWebhookRunSummary(context.device, "Quick Run", 120);

        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_CYCLING_COMPLETED")), is(true));

        assertThat(context.device.paused, is(false));
        assertWebhookRunSummary(context.device, "Quick Run", 120);

        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_COMPLETED")), is(true));

        assertIdleRunSummary(context.device);
    }

    @Test
    void knownLegacyRefreshOnlyEventsAreHandledWithoutChangingActiveRunSummary() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());
        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));

        String[][] events = { { "DEVICE_DELTA", "DEVICE_DELTA" }, { "SCHEDULE_DELTA", "SCHEDULE_DELTA" },
                { "ZONE_DELTA", "ZONE_DELTA" }, { "WATER_BUDGET", "WATER_BUDGET" },
                { "DEVICE_STATUS", "BROWNOUT_VALVE" } };

        for (String[] event : events) {
            assertThat(context.handler.webhookEvent(legacyControllerEvent(event[0], event[1])), is(true));
            assertWebhookRunSummary(context.device, "Quick Run", 120);
        }
    }

    @Test
    void zoneStartedPreservesScheduleStartedNameWhenAlreadyKnown() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());
        RachioEventGsonDTO scheduleStarted = legacyScheduleStatusEvent("SCHEDULE_STARTED");
        scheduleStarted.scheduleName = "Schedule From Start";

        assertThat(context.handler.webhookEvent(scheduleStarted), is(true));
        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));

        assertThat(context.device.currentScheduleName, is("Schedule From Start"));
        assertThat(context.device.activeZoneName, is("Front lawn"));
    }

    @Test
    void runningRestScheduleReplacesFreshWebhookRunSummary() throws Exception {
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        currentSchedule.running = true;
        currentSchedule.scheduleId = "rest-schedule-id";
        currentSchedule.scheduleName = "REST Schedule";
        currentSchedule.scheduleType = "FIXED";
        currentSchedule.duration = 300;
        WebhookRunContext context = webhookRunContext(currentSchedule);

        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));
        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.BACKGROUND_REFRESH,
                RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);

        assertWebhookRunSummary(context.device, "REST Schedule", 300);
        assertThat(context.device.currentScheduleId, is("rest-schedule-id"));
        assertThat(context.device.currentScheduleType, is("FIXED"));
    }

    @Test
    void explicitScheduleStoppedClearsFreshWebhookRunSummary() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());
        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));

        RachioEventGsonDTO stoppedEvent = new RachioEventGsonDTO();
        stoppedEvent.type = "SCHEDULE_STATUS";
        stoppedEvent.subType = "SCHEDULE_STOPPED";
        assertThat(context.handler.webhookEvent(stoppedEvent), is(true));
        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.BACKGROUND_REFRESH,
                RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);

        assertIdleRunSummary(context.device);
    }

    @Test
    void explicitZoneStoppedClearsFreshWebhookRunSummary() throws Exception {
        assertExplicitZoneEndClearsRunSummary("ZONE_STOPPED");
    }

    @Test
    void explicitZoneCompletedClearsFreshWebhookRunSummary() throws Exception {
        assertExplicitZoneEndClearsRunSummary("ZONE_COMPLETED");
    }

    @Test
    void expiredWebhookRunSummaryCanBeClearedByReconciliation() throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());
        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));
        context.handler.advanceTime(181_000);

        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.BACKGROUND_REFRESH,
                RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);

        assertIdleRunSummary(context.device);
    }

    private Thing thing() {
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_DEVICE, "bridge", "controller"));
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of(PROPERTY_DEV_ID, DEVICE_ID)));
        when(thing.getProperties()).thenReturn(new HashMap<>());
        when(thing.getStatus()).thenReturn(ThingStatus.INITIALIZING);
        return thing;
    }

    private RachioBridgeHandler bridgeHandler(Thing thing, RachioDevice device) {
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        when(bridgeHandler.getDevByConfiguredDeviceId(thing, DEVICE_ID)).thenReturn(device);
        when(bridgeHandler.getForecastUnits()).thenReturn("US");
        return bridgeHandler;
    }

    private RachioDevice device(String status) {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = DEVICE_ID;
        cloudDevice.name = "Rachio-E0ACCA";
        cloudDevice.status = status;
        cloudDevice.macAddress = "ABCDEF123456";
        cloudDevice.serialNumber = "serial";
        cloudDevice.model = "GENERATION2_8ZONE";
        return new RachioDevice(cloudDevice);
    }

    private RachioEventGsonDTO deviceStatusEvent(String subType) {
        return legacyControllerEvent("DEVICE_STATUS", subType);
    }

    private RachioEventGsonDTO legacyControllerEvent(String type, String subType) {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.type = type;
        event.subType = subType;
        event.deviceId = DEVICE_ID;
        return event;
    }

    private void assertLegacyZoneEventUsesExistingHandler(String subType) {
        Thing thing = thing();
        RachioDevice device = deviceWithZone();
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(7));
        RachioZoneHandler zoneHandler = Mockito.mock(RachioZoneHandler.class);
        zone.setThingHandler(zoneHandler);
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.type = "ZONE_STATUS";
        event.subType = subType;
        event.deviceId = DEVICE_ID;
        event.zoneId = zone.id;
        event.zoneNumber = zone.zoneNumber;
        when(zoneHandler.webhookEvent(event)).thenReturn(true);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler(thing, device), device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        assertThat(handler.webhookEvent(event), is(true));

        verify(zoneHandler).webhookEvent(event);
    }

    private RachioDevice deviceWithZone() {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = DEVICE_ID;
        cloudDevice.name = "Rachio-E0ACCA";
        cloudDevice.status = "ONLINE";
        RachioCloudZone cloudZone = new RachioCloudZone();
        cloudZone.id = "zone-id";
        cloudZone.name = "Front lawn";
        cloudZone.zoneNumber = 7;
        cloudDevice.zones.add(cloudZone);
        return new RachioDevice(cloudDevice);
    }

    private WebhookRunContext webhookRunContext(RachioCurrentScheduleResponse currentSchedule) throws Exception {
        Thing thing = thing();
        RachioDevice device = deviceWithZone();
        RachioZone zone = Objects.requireNonNull(device.getZoneByNumber(7));
        RachioZoneHandler zoneHandler = Mockito.mock(RachioZoneHandler.class);
        when(zoneHandler.webhookEvent(any())).thenReturn(true);
        zone.setThingHandler(zoneHandler);
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH))
                .thenReturn(currentSchedule);
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL)).thenReturn(currentSchedule);
        doThrow(optionalThrottle()).when(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        return new WebhookRunContext(device, handler);
    }

    private RachioEventGsonDTO legacyZoneRunEvent(String subType) {
        String eventType = switch (subType) {
            case "ZONE_COMPLETED" -> "DEVICE_ZONE_RUN_COMPLETED_EVENT";
            case "ZONE_STOPPED" -> "DEVICE_ZONE_RUN_STOPPED_EVENT";
            case "ZONE_CYCLING" -> "DEVICE_ZONE_RUN_PAUSED_EVENT";
            case "ZONE_CYCLING_COMPLETED" -> "DEVICE_ZONE_RUN_RESUMED_EVENT";
            default -> "DEVICE_ZONE_RUN_STARTED_EVENT";
        };
        return Objects.requireNonNull(new Gson().fromJson("""
                {
                  "type": "ZONE_STATUS",
                  "subType": "%s",
                  "eventType": "%s",
                  "deviceId": "%s",
                  "zoneId": "zone-id",
                  "zoneNumber": 7,
                  "zoneName": "Front lawn",
                  "scheduleName": "Quick Run",
                  "scheduleType": "MANUAL",
                  "startTime": "2026-06-16T10:00:00Z",
                  "endTime": "2026-06-16T10:02:00Z",
                  "duration": 120
                }
                """.formatted(subType, eventType, DEVICE_ID), RachioEventGsonDTO.class));
    }

    private RachioEventGsonDTO legacyScheduleStatusEvent(String subType) {
        return Objects.requireNonNull(new Gson().fromJson("""
                {
                  "type": "SCHEDULE_STATUS",
                  "subType": "%s",
                  "eventType": "%s_EVENT",
                  "deviceId": "%s",
                  "scheduleId": "schedule-id",
                  "scheduleName": "Quick Run",
                  "scheduleType": "MANUAL",
                  "summary": "Quick Run started",
                  "startTime": "2026-06-16T10:00:00Z",
                  "endTime": "2026-06-16T10:02:00Z",
                  "duration": 120
                }
                """.formatted(subType, subType, DEVICE_ID), RachioEventGsonDTO.class));
    }

    private void assertWebhookRunSummary(RachioDevice device, String scheduleName, int duration) {
        assertThat(device.currentScheduleRunning, is(true));
        assertThat(device.currentScheduleName, is(scheduleName));
        assertThat(device.currentScheduleDuration, is(duration));
        assertThat(device.activeZoneNumber, is(7));
        assertThat(device.activeZoneName, is("Front lawn"));
        assertThat(device.activeZoneId, is("zone-id"));
    }

    private void assertScheduleRunSummary(RachioDevice device, String scheduleName, int duration) {
        assertThat(device.currentScheduleRunning, is(true));
        assertThat(device.currentScheduleName, is(scheduleName));
        assertThat(device.currentScheduleDuration, is(duration));
        assertThat(device.currentScheduleId, is("schedule-id"));
        assertThat(device.currentScheduleType, is("MANUAL"));
        assertThat(device.activeZoneNumber, is(-1));
        assertThat(device.activeZoneName, is(""));
        assertThat(device.activeZoneId, is(""));
    }

    private void assertIdleRunSummary(RachioDevice device) {
        assertThat(device.currentScheduleRunning, is(false));
        assertThat(device.currentScheduleName, is(""));
        assertThat(device.currentScheduleDuration, is(0));
        assertThat(device.activeZoneNumber, is(-1));
        assertThat(device.activeZoneName, is(""));
        assertThat(device.activeZoneId, is(""));
    }

    private void assertExplicitZoneEndClearsRunSummary(String subType) throws Exception {
        WebhookRunContext context = webhookRunContext(new RachioCurrentScheduleResponse());
        assertThat(context.handler.webhookEvent(legacyZoneRunEvent("ZONE_STARTED")), is(true));

        assertThat(context.handler.webhookEvent(legacyZoneRunEvent(subType)), is(true));
        context.handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.BACKGROUND_REFRESH,
                RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);

        assertIdleRunSummary(context.device);
    }

    private static RachioApiThrottledException throttledException() {
        return new RachioApiThrottledException(
                new RateLimitThrottleException(PRIORITY.MED, RequestPurpose.INITIALIZATION, 0.1, 0.2),
                new RachioApiResult());
    }

    private static RachioApiThrottledException optionalThrottle() {
        return new RachioApiThrottledException(
                new RateLimitThrottleException(PRIORITY.LOW, RequestPurpose.BACKGROUND_REFRESH, 0.1, 0.2),
                new RachioApiResult());
    }

    private static RachioApiException serverRateLimitException() {
        RachioApiResult result = new RachioApiResult();
        result.responseCode = HttpStatus.TOO_MANY_REQUESTS_429;
        return new RachioApiException("server rate limit", result);
    }

    private static class TestDeviceHandler extends RachioDeviceHandler {
        private final RachioBridgeHandler bridgeHandler;
        private final Bridge bridgeThing = Mockito.mock(Bridge.class);
        private boolean retryScheduled;
        private Runnable retryAction = () -> {
        };

        TestDeviceHandler(Thing thing, RachioBridgeHandler bridgeHandler) {
            super(thing);
            this.bridgeHandler = bridgeHandler;
            when(bridgeThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        }

        void runScheduledRetry() {
            retryAction.run();
        }

        @Override
        protected boolean initializeCloudHandler() {
            cloudHandler = bridgeHandler;
            bridge = bridgeThing;
            return true;
        }

        @Override
        public void refreshSmartIrrigationReadExtensions(boolean force) {
        }

        @Override
        protected void scheduleWebhookRegistrationRetry(String deviceId, long delaySeconds) {
            retryScheduled = true;
            retryAction = () -> retryDeferredWebhookRegistration();
        }
    }

    private static class PollingDeviceHandler extends RachioDeviceHandler {
        private long now = Instant.parse("2026-06-16T10:00:00Z").toEpochMilli();

        PollingDeviceHandler(Thing thing, RachioBridgeHandler bridgeHandler, RachioDevice device) {
            super(thing);
            cloudHandler = bridgeHandler;
            dev = device;
            thingId = device.name;
        }

        void advanceTime(long millis) {
            now += millis;
        }

        @Override
        protected long currentTimeMillis() {
            return now;
        }
    }

    private record WebhookRunContext(RachioDevice device, PollingDeviceHandler handler) {
    }
}
