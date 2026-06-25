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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ONLINE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_RAIN_SENSOR_TRIPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FORECAST_PRECIPITATION;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FORECAST_PRECIPITATION_PROBABILITY;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FORECAST_SUMMARY;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FORECAST_TODAY_HIGH;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FORECAST_TODAY_LOW;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FORECAST_UPDATED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FORECAST_WIND;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_DEV_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_FLEX_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_SCHEDULE;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudScheduleRule;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioDeviceEventListResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastEntry;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioForecastResponse;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
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
    private static final List<String> FORECAST_CHANNELS = List.of(CHANNEL_FORECAST_SUMMARY, CHANNEL_FORECAST_TODAY_HIGH,
            CHANNEL_FORECAST_TODAY_LOW, CHANNEL_FORECAST_PRECIPITATION, CHANNEL_FORECAST_PRECIPITATION_PROBABILITY,
            CHANNEL_FORECAST_WIND, CHANNEL_FORECAST_UPDATED);

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
    void throttledLegacyWebhookRegistrationSchedulesBudgetFriendlyRetryDelay() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();

        assertThat(handler.retryScheduled, is(true));
        assertThat(handler.scheduledRetryDelaySeconds >= 60, is(true));
        assertThat(handler.retryScheduleCount, is(1));
        assertThat(handler.isWebhookRegistrationPending(), is(true));
    }

    @Test
    void repeatedWebhookThrottleUsesBackoffAndAvoidsDuplicateRetryTasks() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.USER_COMMAND);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        long firstDelay = handler.scheduledRetryDelaySeconds;

        handler.runScheduledRetry();
        long secondDelay = handler.scheduledRetryDelaySeconds;
        int scheduleCountAfterSecondDefer = handler.retryScheduleCount;

        handler.onConfigurationUpdated();

        assertThat(secondDelay >= firstDelay, is(true));
        assertThat(firstDelay >= 60, is(true));
        assertThat(secondDelay >= 60, is(true));
        assertThat(handler.retryScheduleCount, is(scheduleCountAfterSecondDefer));
        assertThat(handler.isWebhookRegistrationPending(), is(true));
    }

    @Test
    void staleLegacyRetryIsSkippedAfterSwitchingToModernMode() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        AtomicReference<RachioWebhookMode> modeRef = new AtomicReference<>(
                RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE);
        AtomicReference<String> callbackRef = new AtomicReference<>("https://legacy.example.org/callback");
        when(bridgeHandler.getActiveIrrigationWebhookProcessingMode()).thenAnswer(invocation -> modeRef.get());
        when(bridgeHandler.getCallbackUrl()).thenAnswer(invocation -> callbackRef.get());
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        modeRef.set(RachioWebhookMode.WEBHOOK_SERVICE);

        handler.runScheduledRetry();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(bridgeHandler, never()).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
        assertThat(handler.retryScheduleCount, is(1));
    }

    @Test
    void staleModernRetryIsSkippedAfterSwitchingToLegacyMode() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        AtomicReference<RachioWebhookMode> modeRef = new AtomicReference<>(RachioWebhookMode.WEBHOOK_SERVICE);
        AtomicReference<String> callbackRef = new AtomicReference<>("https://legacy.example.org/callback");
        when(bridgeHandler.getActiveIrrigationWebhookProcessingMode()).thenAnswer(invocation -> modeRef.get());
        when(bridgeHandler.getCallbackUrl()).thenAnswer(invocation -> callbackRef.get());
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        modeRef.set(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE);

        handler.runScheduledRetry();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(bridgeHandler, never()).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
    }

    @Test
    void legacyRetryIsSkippedWhenCallbackUrlIsCleared() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        AtomicReference<String> callbackRef = new AtomicReference<>("https://legacy.example.org/callback");
        when(bridgeHandler.getActiveIrrigationWebhookProcessingMode())
                .thenReturn(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE);
        when(bridgeHandler.getCallbackUrl()).thenAnswer(invocation -> callbackRef.get());
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        callbackRef.set("");

        handler.runScheduledRetry();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(bridgeHandler, never()).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
    }

    @Test
    void deferredRetryExitsSafelyWhenBridgeHandlerIsDisposed() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        when(bridgeHandler.isDisposed()).thenReturn(true);

        handler.runScheduledRetry();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(bridgeHandler, never()).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
    }

    @Test
    void legacyRetrySuccessClearsPendingAndResetsBackoff() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.USER_COMMAND);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        handler.runScheduledRetry();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        handler.onConfigurationUpdated();

        assertThat(handler.isWebhookRegistrationPending(), is(true));
        assertThat(handler.scheduledRetryDelaySeconds >= 60, is(true));
    }

    @Test
    void deferredRetryClearsWhenHandlerIsDisposed() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        handler.dispose();
        handler.runScheduledRetry();

        assertThat(handler.isWebhookRegistrationPending(), is(false));
        verify(bridgeHandler, never()).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
    }

    @Test
    void deferredRetrySkipsWhileBridgeOfflineWithoutDuplicateTasksAndRetriesWhenOnline() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        doThrow(throttledException()).when(bridgeHandler).registerWebHook(DEVICE_ID, RequestPurpose.INITIALIZATION);
        TestDeviceHandler handler = new TestDeviceHandler(thing, bridgeHandler);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.initialize();
        assertThat(handler.retryScheduleCount, is(1));
        assertThat(handler.isWebhookRegistrationPending(), is(true));

        handler.setBridgeStatus(ThingStatus.OFFLINE);
        handler.runScheduledRetry();

        verify(bridgeHandler, never()).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
        assertThat(handler.isWebhookRegistrationPending(), is(true));
        assertThat(handler.retryScheduleCount, is(1));

        handler.setBridgeStatus(ThingStatus.ONLINE);
        handler.runScheduledRetry();

        verify(bridgeHandler, times(1)).registerWebHook(DEVICE_ID, RequestPurpose.BACKGROUND_REFRESH);
        assertThat(handler.isWebhookRegistrationPending(), is(false));
        assertThat(handler.retryScheduleCount, is(1));
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
    void usefulForecastEnrichmentPostsForecastChannelsBeforeEventEnrichment() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL))
                .thenReturn(new RachioCurrentScheduleResponse());
        when(bridgeHandler.getDeviceForecast(DEVICE_ID, "US")).thenReturn(fullForecast("Sunny"));
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(24);
        when(bridgeHandler.getDeviceEvents(eq(DEVICE_ID), anyLong(), anyLong()))
                .thenReturn(new RachioDeviceEventListResponse());
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        InOrder order = Mockito.inOrder(bridgeHandler, callback);

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        order.verify(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        for (String channel : FORECAST_CHANNELS) {
            order.verify(callback).stateUpdated(eq(new ChannelUID(thing.getUID(), channel)), any());
        }
        order.verify(bridgeHandler).getDeviceEvents(eq(DEVICE_ID), anyLong(), anyLong());
        for (String channel : FORECAST_CHANNELS) {
            verify(callback, times(1)).stateUpdated(eq(new ChannelUID(thing.getUID(), channel)), any());
        }
    }

    @Test
    void throttledOptionalEnrichmentRetriesAfterShortCooldown() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL)).thenReturn(currentSchedule);
        doThrow(optionalThrottle()).when(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        handler.advanceTime(59_000);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        handler.advanceTime(1_000);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.forecastSummary, is(""));
        verify(bridgeHandler, times(3)).getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL);
        verify(bridgeHandler, times(2)).getDeviceForecast(DEVICE_ID, "US");
        verifyNoForecastChannelUpdates(callback, thing);
    }

    @Test
    void successfulOptionalEnrichmentStartsNormalInterval() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL)).thenReturn(currentSchedule);
        when(bridgeHandler.getDeviceForecast(DEVICE_ID, "US")).thenReturn(forecast("Sunny"));
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        handler.advanceTime(14 * 60_000L);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        handler.advanceTime(60_000);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.forecastSummary, is("Sunny"));
        verify(bridgeHandler, times(2)).getDeviceForecast(DEVICE_ID, "US");
    }

    @Test
    void existingForecastValuesAreRetainedAfterLaterThrottle() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL)).thenReturn(currentSchedule);
        when(bridgeHandler.getDeviceForecast(DEVICE_ID, "US")).thenReturn(forecast("Sunny"))
                .thenThrow(optionalThrottle());
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        Mockito.clearInvocations(callback);
        handler.advanceTime(15 * 60_000L);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.forecastSummary, is("Sunny"));
        verify(bridgeHandler, times(2)).getDeviceForecast(DEVICE_ID, "US");
        verifyNoForecastChannelUpdates(callback, thing);
    }

    @Test
    void usefulForecastResponseUpdatesAllForecastFields() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL))
                .thenReturn(new RachioCurrentScheduleResponse());
        when(bridgeHandler.getDeviceForecast(DEVICE_ID, "US")).thenReturn(fullForecast("Sunny"));
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.forecastSummary, is("Sunny"));
        assertThat(device.forecastUpdated, is("2026-06-16T09:55:00Z"));
        assertThat(device.forecastTodayHigh, is(82.0));
        assertThat(device.forecastTodayLow, is(61.0));
        assertThat(device.forecastPrecipitation, is(0.25));
        assertThat(device.forecastPrecipitationProbability, is(0.7));
        assertThat(device.forecastWind, is(8.5));
    }

    @Test
    void numericForecastWithoutSummaryUsesSyntheticSummaryAndRetrievalTimestamp() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL))
                .thenReturn(new RachioCurrentScheduleResponse());
        when(bridgeHandler.getDeviceForecast(DEVICE_ID, "US")).thenReturn(numericForecast(Double.NaN, 0));
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.forecastSummary, is("61-82 \u00B0F, precipitation chance 0%, wind 8.5 mph"));
        assertThat(device.forecastUpdated, is("2026-06-16T10:00:00Z"));
        assertThat(device.forecastPrecipitation, is(0.0));
        assertThat(device.forecastPrecipitationProbability, is(0.0));
    }

    @Test
    void generatedForecastSummariesUseConfiguredDisplayUnits() {
        RachioForecastResponse metricForecast = numericForecast(2.5, 0);
        RachioForecastEntry metricToday = Objects.requireNonNull(metricForecast.today);
        metricToday.lowTemperature = 18;
        metricToday.highTemperature = 30;
        metricToday.windSpeed = 4;

        assertThat(metricForecast.buildSummary("METRIC"),
                is("18-30 \u00B0C, precipitation chance 0%, precipitation 2.5 mm, wind 14.4 km/h"));

        RachioForecastResponse usForecast = numericForecast(0.25, 0);
        assertThat(usForecast.buildSummary("US"),
                is("61-82 \u00B0F, precipitation chance 0%, precipitation 0.25 in, wind 8.5 mph"));
    }

    @Test
    void missingPrecipitationAmountWithNonZeroChanceRemainsUndefined() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL))
                .thenReturn(new RachioCurrentScheduleResponse());
        when(bridgeHandler.getDeviceForecast(DEVICE_ID, "US")).thenReturn(numericForecast(Double.NaN, 0.7));
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(Double.isNaN(device.forecastPrecipitation), is(true));
        assertThat(device.forecastPrecipitationProbability, is(0.7));
    }

    @Test
    void timestampOnlyForecastRetainsExistingValuesAndRetriesAfterShortCooldown() throws Exception {
        Thing thing = thing();
        RachioDevice device = device("ONLINE");
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL))
                .thenReturn(new RachioCurrentScheduleResponse());
        when(bridgeHandler.getDeviceForecast(DEVICE_ID, "US")).thenReturn(fullForecast("Sunny"))
                .thenReturn(timestampOnlyForecast()).thenReturn(fullForecast("Cloudy"));
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        Mockito.clearInvocations(callback);
        handler.advanceTime(15 * 60_000L);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        assertThat(device.forecastSummary, is("Sunny"));
        assertThat(device.forecastUpdated, is("2026-06-16T09:55:00Z"));
        assertThat(device.forecastTodayHigh, is(82.0));
        assertThat(device.forecastPrecipitation, is(0.25));
        verifyNoForecastChannelUpdates(callback, thing);
        Mockito.clearInvocations(callback);
        handler.advanceTime(59_000);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);
        handler.advanceTime(1_000);
        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.forecastSummary, is("Cloudy"));
        assertThat(device.forecastTodayHigh, is(82.0));
        verify(bridgeHandler, times(3)).getDeviceForecast(DEVICE_ID, "US");
    }

    private void verifyNoForecastChannelUpdates(ThingHandlerCallback callback, Thing thing) {
        for (String channel : FORECAST_CHANNELS) {
            verify(callback, never()).stateUpdated(eq(new ChannelUID(thing.getUID(), channel)), any());
        }
    }

    @Test
    void runningCurrentScheduleWithScheduleIdResolvesKnownScheduleName() throws Exception {
        Thing thing = thing();
        RachioDevice device = deviceWithZone();
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        currentSchedule.running = true;
        currentSchedule.scheduleId = "schedule-id";
        currentSchedule.duration = 600;
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL)).thenReturn(currentSchedule);
        doThrow(optionalThrottle()).when(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.currentScheduleRunning, is(true));
        assertThat(device.currentScheduleName, is("Morning Lawn"));
        assertThat(device.currentScheduleType, is("FIXED"));
        assertThat(device.currentScheduleDuration, is(600));
    }

    @Test
    void runningCurrentScheduleWithBlankNameUsesScheduleThingLabelFallback() throws Exception {
        Thing thing = thing();
        RachioDevice device = deviceWithZone();
        device.scheduleRules.clear();
        RachioBridgeHandler bridgeHandler = realBridgeHandler(thing, device);
        RachioScheduleHandler scheduleHandler = scheduleHandler("schedule-id", "Schedule Thing Label");
        bridgeHandler.rachioStatusListeners.add(scheduleHandler);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        currentSchedule.running = true;
        currentSchedule.scheduleId = "schedule-id";
        currentSchedule.duration = 600;
        Mockito.doReturn(currentSchedule).when(bridgeHandler).getCurrentSchedule(DEVICE_ID,
                RequestPurpose.CORE_STATUS_POLL);
        doThrow(optionalThrottle()).when(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        Mockito.doReturn(0).when(bridgeHandler).getEventHistoryLookbackHours();
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.currentScheduleName, is("Schedule Thing Label"));
        assertThat(device.currentScheduleType, is("FIXED"));
    }

    @Test
    void runningCurrentScheduleWithBlankNameUsesFlexScheduleThingLabelFallback() throws Exception {
        Thing thing = thing();
        RachioDevice device = deviceWithZone();
        device.scheduleRules.clear();
        device.flexScheduleRules.clear();
        RachioBridgeHandler bridgeHandler = realBridgeHandler(thing, device);
        RachioFlexScheduleHandler flexScheduleHandler = flexScheduleHandler("schedule-id", "Flex Thing Label");
        bridgeHandler.rachioStatusListeners.add(flexScheduleHandler);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        currentSchedule.running = true;
        currentSchedule.scheduleId = "schedule-id";
        currentSchedule.duration = 600;
        Mockito.doReturn(currentSchedule).when(bridgeHandler).getCurrentSchedule(DEVICE_ID,
                RequestPurpose.CORE_STATUS_POLL);
        doThrow(optionalThrottle()).when(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        Mockito.doReturn(0).when(bridgeHandler).getEventHistoryLookbackHours();
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.currentScheduleName, is("Flex Thing Label"));
        assertThat(device.currentScheduleType, is("FLEX"));
    }

    @Test
    void runningBlankCurrentSchedulePreservesExistingWebhookMetadata() throws Exception {
        Thing thing = thing();
        RachioDevice device = deviceWithZone();
        device.currentScheduleId = "unknown-schedule-id";
        device.currentScheduleName = "Webhook Schedule";
        device.currentScheduleStartTime = "2026-06-16T10:00:00Z";
        device.currentScheduleEndTime = "2026-06-16T10:10:00Z";
        device.currentScheduleDuration = 600;
        device.currentScheduleRunning = true;
        RachioBridgeHandler bridgeHandler = bridgeHandler(thing, device);
        RachioCurrentScheduleResponse currentSchedule = new RachioCurrentScheduleResponse();
        currentSchedule.running = true;
        when(bridgeHandler.getCurrentSchedule(DEVICE_ID, RequestPurpose.CORE_STATUS_POLL)).thenReturn(currentSchedule);
        doThrow(optionalThrottle()).when(bridgeHandler).getDeviceForecast(DEVICE_ID, "US");
        when(bridgeHandler.getEventHistoryLookbackHours()).thenReturn(0);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));

        handler.refreshSmartIrrigationReadExtensions(false, RequestPurpose.CORE_STATUS_POLL);

        assertThat(device.currentScheduleName, is("Webhook Schedule"));
        assertThat(device.currentScheduleStartTime, is("2026-06-16T10:00:00Z"));
        assertThat(device.currentScheduleEndTime, is("2026-06-16T10:10:00Z"));
        assertThat(device.currentScheduleDuration, is(600));
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
    void scheduleStartedWebhookWithBlankPayloadNameUsesScheduleHandlerFallback() throws Exception {
        Thing thing = thing();
        RachioDevice device = deviceWithZone();
        device.scheduleRules.clear();
        RachioBridgeHandler bridgeHandler = realBridgeHandler(thing, device);
        RachioScheduleHandler scheduleHandler = scheduleHandler("schedule-id", "Schedule Thing Label");
        scheduleHandler.scheduleRule.externalName = "External Schedule";
        bridgeHandler.rachioStatusListeners.add(scheduleHandler);
        PollingDeviceHandler handler = new PollingDeviceHandler(thing, bridgeHandler, device);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioEventGsonDTO event = legacyScheduleStatusEvent("SCHEDULE_STARTED");
        event.scheduleName = "";
        event.scheduleType = "";

        assertThat(handler.webhookEvent(event), is(true));

        assertThat(device.currentScheduleName, is("External Schedule"));
        assertThat(device.currentScheduleType, is("FIXED"));
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
        when(bridgeHandler.getActiveIrrigationWebhookProcessingMode())
                .thenReturn(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE);
        when(bridgeHandler.getCallbackUrl()).thenReturn("https://legacy.example.org/callback");
        when(bridgeHandler.isDisposed()).thenReturn(false);
        return bridgeHandler;
    }

    private RachioBridgeHandler realBridgeHandler(Thing thing, RachioDevice device) {
        Bridge bridge = Mockito.mock(Bridge.class);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        RachioBridgeHandler bridgeHandler = Mockito.spy(new RachioBridgeHandler(bridge));
        Mockito.doReturn(device).when(bridgeHandler).getDevByConfiguredDeviceId(thing, DEVICE_ID);
        Mockito.doReturn("US").when(bridgeHandler).getForecastUnits();
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
        RachioCloudScheduleRule scheduleRule = new RachioCloudScheduleRule();
        scheduleRule.id = "schedule-id";
        scheduleRule.name = "Morning Lawn";
        scheduleRule.type = "FIXED";
        cloudDevice.scheduleRules.add(scheduleRule);
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

    private RachioForecastResponse forecast(String summary) {
        RachioForecastResponse forecast = new RachioForecastResponse();
        forecast.summary = summary;
        RachioForecastEntry today = new RachioForecastEntry();
        today.summary = summary;
        forecast.today = today;
        return forecast;
    }

    private RachioForecastResponse fullForecast(String summary) {
        RachioForecastResponse forecast = forecast(summary);
        forecast.updated = "2026-06-16T09:55:00Z";
        RachioForecastEntry today = Objects.requireNonNull(forecast.today);
        today.highTemperature = 82.0;
        today.lowTemperature = 61.0;
        today.precipitation = 0.25;
        today.precipitationProbability = 0.7;
        today.windSpeed = 8.5;
        return forecast;
    }

    private RachioForecastResponse numericForecast(double precipitation, double precipitationProbability) {
        RachioForecastResponse forecast = new RachioForecastResponse();
        RachioForecastEntry today = new RachioForecastEntry();
        today.highTemperature = 82.0;
        today.lowTemperature = 61.0;
        today.precipitation = precipitation;
        today.precipitationProbability = precipitationProbability;
        today.windSpeed = 8.5;
        forecast.today = today;
        return forecast;
    }

    private RachioForecastResponse timestampOnlyForecast() {
        RachioForecastResponse forecast = new RachioForecastResponse();
        forecast.updated = "2026-06-16T10:00:00Z";
        RachioForecastEntry today = new RachioForecastEntry();
        today.time = "2026-06-16T00:00:00Z";
        forecast.today = today;
        return forecast;
    }

    private RachioScheduleHandler scheduleHandler(String scheduleId, String label) {
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        when(thing.getLabel()).thenReturn(label);
        RachioScheduleHandler scheduleHandler = new RachioScheduleHandler(thing);
        scheduleHandler.scheduleRuleId = scheduleId;
        return scheduleHandler;
    }

    private RachioFlexScheduleHandler flexScheduleHandler(String scheduleId, String label) {
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex"));
        when(thing.getLabel()).thenReturn(label);
        RachioFlexScheduleHandler flexScheduleHandler = new RachioFlexScheduleHandler(thing);
        flexScheduleHandler.flexScheduleRuleId = scheduleId;
        return flexScheduleHandler;
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
        private int retryScheduleCount;
        private long scheduledRetryDelaySeconds;
        private Runnable retryAction = () -> {
        };

        TestDeviceHandler(Thing thing, RachioBridgeHandler bridgeHandler) {
            super(thing);
            this.bridgeHandler = bridgeHandler;
            when(bridgeThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        }

        void setBridgeStatus(ThingStatus status) {
            when(bridgeThing.getStatus()).thenReturn(status);
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
            retryScheduleCount++;
            scheduledRetryDelaySeconds = delaySeconds;
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
