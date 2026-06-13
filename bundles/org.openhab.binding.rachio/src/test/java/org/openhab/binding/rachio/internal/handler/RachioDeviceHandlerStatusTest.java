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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ONLINE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_DEV_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioCurrentScheduleResponse;
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
    void initializationKeepsRealWebhookApiFailureFatal() throws Exception {
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
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                        && String.valueOf(status.getDescription()).contains("server rate limit")));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
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
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.type = "DEVICE_STATUS";
        event.subType = subType;
        event.deviceId = DEVICE_ID;
        return event;
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
        PollingDeviceHandler(Thing thing, RachioBridgeHandler bridgeHandler, RachioDevice device) {
            super(thing);
            cloudHandler = bridgeHandler;
            dev = device;
            thingId = device.name;
        }
    }
}
