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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_FLEX_SCHEDULE_RULE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_FLEX_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_SCHEDULE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioFlexScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests schedule handler status lifecycle around initial refresh failures.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioScheduleHandlerStatusTest {
    @Test
    void scheduleHandlerDoesNotOverwriteFailedInitialRefreshWithOnline() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        TestScheduleHandler handler = new TestScheduleHandler(thing, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void scheduleHandlerGoesOnlineAfterSuccessfulInitialRefresh() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        TestScheduleHandler handler = new TestScheduleHandler(thing, true);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerDoesNotOverwriteFailedInitialRefreshWithOnline() {
        Thing thing = thing(new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex"));
        TestFlexScheduleHandler handler = new TestFlexScheduleHandler(thing, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerGoesOnlineAfterSuccessfulInitialRefresh() {
        Thing thing = thing(new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex"));
        TestFlexScheduleHandler handler = new TestFlexScheduleHandler(thing, true);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerUsesScheduleRuleServiceCommands() throws RachioApiException {
        ThingUID thingUID = new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex");
        RachioFlexScheduleHandler handler = new RachioFlexScheduleHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        handler.cloudHandler = bridgeHandler;
        handler.flexScheduleRuleId = "flex-id";

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_START), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_SKIP), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT), new DecimalType("0.75"));

        verify(bridgeHandler).startScheduleRule("flex-id");
        verify(bridgeHandler).skipScheduleRule("flex-id");
        verify(bridgeHandler).skipForwardZoneRun("flex-id");
        verify(bridgeHandler).setScheduleRuleSeasonalAdjustment("flex-id", 0.75);
    }

    @Test
    void scheduleHandlerUsesScheduleRuleServiceCommands() throws RachioApiException {
        ThingUID thingUID = new ThingUID(THING_TYPE_SCHEDULE, "bridge", "fixed");
        RachioScheduleHandler handler = new RachioScheduleHandler(thing(thingUID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        handler.cloudHandler = bridgeHandler;
        handler.scheduleRuleId = "fixed-id";

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_START), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_SKIP), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT), new DecimalType("0.75"));

        verify(bridgeHandler).startScheduleRule("fixed-id");
        verify(bridgeHandler).skipScheduleRule("fixed-id");
        verify(bridgeHandler).skipForwardZoneRun("fixed-id");
        verify(bridgeHandler).setScheduleRuleSeasonalAdjustment("fixed-id", 0.75);
    }

    @Test
    void scheduleHandlersUseTheirSeparateMetadataReadServices() throws RachioApiException {
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioScheduleRuleResponse fixedResponse = new RachioScheduleRuleResponse();
        RachioFlexScheduleRuleResponse flexResponse = new RachioFlexScheduleRuleResponse();
        when(bridgeHandler.getScheduleRuleForInitialization("fixed-id")).thenReturn(fixedResponse);
        when(bridgeHandler.getFlexScheduleRuleForInitialization("flex-id")).thenReturn(flexResponse);

        RachioScheduleHandler fixedHandler = new RachioScheduleHandler(
                thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "fixed")));
        fixedHandler.cloudHandler = bridgeHandler;
        fixedHandler.scheduleRuleId = "fixed-id";
        RachioFlexScheduleHandler flexHandler = new RachioFlexScheduleHandler(
                thing(new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex")));
        flexHandler.cloudHandler = bridgeHandler;
        flexHandler.flexScheduleRuleId = "flex-id";

        assertThat(fixedHandler.loadScheduleRule(), is(fixedResponse));
        assertThat(flexHandler.loadFlexScheduleRule(), is(flexResponse));
        verify(bridgeHandler).getScheduleRuleForInitialization("fixed-id");
        verify(bridgeHandler).getFlexScheduleRuleForInitialization("flex-id");
    }

    @Test
    void scheduleHandlerDefersInitializationAfterLocalThrottleAndRecovers() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RetryingScheduleHandler handler = new RetryingScheduleHandler(thing, true);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        assertThat(handler.retryScheduled, is(true));
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.INITIALIZING
                        && status.getStatusDetail() == ThingStatusDetail.NONE
                        && String.valueOf(status.getDescription()).contains("bootstrap budget")));

        handler.runScheduledRetry();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerDefersInitializationAfterLocalThrottleAndRecovers() {
        Thing thing = thing(new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex"));
        RetryingFlexScheduleHandler handler = new RetryingFlexScheduleHandler(thing, true);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        assertThat(handler.retryScheduled, is(true));
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.INITIALIZING
                        && status.getStatusDetail() == ThingStatusDetail.NONE
                        && String.valueOf(status.getDescription()).contains("bootstrap budget")));

        handler.runScheduledRetry();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerResolvesUuidFromThingPropertiesDuringInitialization() {
        Thing thing = flexScheduleThing(Map.of(), Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "property-flex-id"));
        InitializingFlexScheduleHandler handler = new InitializingFlexScheduleHandler(thing, false, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        assertThat(handler.loadedFlexScheduleRuleId, is("property-flex-id"));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerPrefersUuidFromThingConfigurationDuringInitialization() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "config-flex-id"),
                Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "property-flex-id"));
        InitializingFlexScheduleHandler handler = new InitializingFlexScheduleHandler(thing, false, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        assertThat(handler.loadedFlexScheduleRuleId, is("config-flex-id"));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerDoesNotGoOnlineAfterFailedInitializationLoad() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "config-flex-id"), Map.of());
        InitializingFlexScheduleHandler handler = new InitializingFlexScheduleHandler(thing, true, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                        && String.valueOf(status.getDescription()).contains("not found")));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerDefersInitializationAndRecoversAfterLocalThrottle() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "config-flex-id"), Map.of());
        InitializingFlexScheduleHandler handler = new InitializingFlexScheduleHandler(thing, false, true);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        assertThat(handler.retryScheduled, is(true));
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.INITIALIZING
                        && status.getStatusDetail() == ThingStatusDetail.NONE
                        && String.valueOf(status.getDescription()).contains("bootstrap budget")));

        handler.runScheduledRetry();

        assertThat(handler.loadedFlexScheduleRuleId, is("config-flex-id"));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void scheduleHandlerDoesNotRetryTrueApiFailure() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RetryingScheduleHandler handler = new RetryingScheduleHandler(thing, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        assertThat(handler.retryScheduled, is(false));
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                        && String.valueOf(status.getDescription()).contains("not found")));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void throttledExceptionCarriesRetryableLocalThrottleDetails() {
        RachioApiThrottledException exception = throttledException();

        assertThat(exception.getPriority(), is(PRIORITY.LOW));
        assertThat(exception.getSuggestedRetryDelay().getSeconds(), is(30L));
        assertThat(exception.getMessage(), containsString("priority LOW"));
    }

    private Thing thing(ThingUID uid) {
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getUID()).thenReturn(uid);
        return thing;
    }

    private Thing flexScheduleThing(Map<String, Object> configuration, Map<String, String> properties) {
        ThingUID uid = new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex");
        Thing thing = thing(uid);
        when(thing.getConfiguration()).thenReturn(new Configuration(configuration));
        when(thing.getProperties()).thenReturn(properties);
        return thing;
    }

    private static RachioApiThrottledException throttledException() {
        return new RachioApiThrottledException(new RateLimitThrottleException(PRIORITY.LOW, 0.1, 0.2),
                new RachioApiResult());
    }

    private static RachioApiThrottledException initializationThrottledException() {
        return new RachioApiThrottledException(
                new RateLimitThrottleException(PRIORITY.MED, RequestPurpose.INITIALIZATION, 0.1, 0.2),
                new RachioApiResult());
    }

    private static class TestScheduleHandler extends RachioScheduleHandler {
        private final boolean refreshSuccess;

        TestScheduleHandler(Thing thing, boolean refreshSuccess) {
            super(thing);
            this.refreshSuccess = refreshSuccess;
        }

        void publicGoOnline() {
            goOnline();
        }

        @Override
        protected boolean refreshScheduleRule() {
            if (!refreshSuccess) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "failed");
            }
            return refreshSuccess;
        }
    }

    private static class RetryingScheduleHandler extends RachioScheduleHandler {
        private final boolean throttleThenRecover;
        private boolean firstLoad = true;
        private boolean retryScheduled;
        private Runnable retryAction = () -> {
        };

        RetryingScheduleHandler(Thing thing, boolean throttleThenRecover) {
            super(thing);
            this.throttleThenRecover = throttleThenRecover;
            this.scheduleRuleId = "schedule-id";
            this.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        }

        void publicGoOnline() {
            goOnline();
        }

        void runScheduledRetry() {
            retryAction.run();
        }

        @Override
        protected RachioScheduleRuleResponse loadScheduleRule() throws RachioApiException {
            if (firstLoad) {
                firstLoad = false;
                if (throttleThenRecover) {
                    throw initializationThrottledException();
                }
                throw new RachioApiException("not found");
            }

            RachioScheduleRuleResponse response = new RachioScheduleRuleResponse();
            response.id = "schedule-id";
            response.name = "Morning";
            return response;
        }

        @Override
        protected long scheduleInitializationThrottleRetry(String operation, Runnable retryAction,
                RachioApiThrottledException throttle) {
            retryScheduled = true;
            this.retryAction = retryAction;
            updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NONE,
                    "Waiting for local Rachio API bootstrap budget; initialization will retry automatically.");
            return throttle.getSuggestedRetryDelay().getSeconds();
        }
    }

    private static class TestFlexScheduleHandler extends RachioFlexScheduleHandler {
        private final boolean refreshSuccess;

        TestFlexScheduleHandler(Thing thing, boolean refreshSuccess) {
            super(thing);
            this.refreshSuccess = refreshSuccess;
        }

        void publicGoOnline() {
            goOnline();
        }

        @Override
        protected boolean refreshFlexScheduleRule() {
            if (!refreshSuccess) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "failed");
            }
            return refreshSuccess;
        }
    }

    private static class RetryingFlexScheduleHandler extends RachioFlexScheduleHandler {
        private final boolean throttleThenRecover;
        private boolean firstLoad = true;
        private boolean retryScheduled;
        private Runnable retryAction = () -> {
        };

        RetryingFlexScheduleHandler(Thing thing, boolean throttleThenRecover) {
            super(thing);
            this.throttleThenRecover = throttleThenRecover;
            this.flexScheduleRuleId = "flex-id";
            this.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        }

        void publicGoOnline() {
            goOnline();
        }

        void runScheduledRetry() {
            retryAction.run();
        }

        @Override
        protected RachioFlexScheduleRuleResponse loadFlexScheduleRule() throws RachioApiException {
            if (firstLoad) {
                firstLoad = false;
                if (throttleThenRecover) {
                    throw initializationThrottledException();
                }
                throw new RachioApiException("not found");
            }

            RachioFlexScheduleRuleResponse response = new RachioFlexScheduleRuleResponse();
            response.id = "flex-id";
            response.name = "Flex";
            return response;
        }

        @Override
        protected long scheduleInitializationThrottleRetry(String operation, Runnable retryAction,
                RachioApiThrottledException throttle) {
            retryScheduled = true;
            this.retryAction = retryAction;
            updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NONE,
                    "Waiting for local Rachio API bootstrap budget; initialization will retry automatically.");
            return throttle.getSuggestedRetryDelay().getSeconds();
        }
    }

    private static class InitializingFlexScheduleHandler extends RachioFlexScheduleHandler {
        private final boolean loadFails;
        private boolean firstLoadThrottled;
        private boolean retryScheduled;
        private String loadedFlexScheduleRuleId = "";
        private Runnable retryAction = () -> {
        };

        InitializingFlexScheduleHandler(Thing thing, boolean loadFails, boolean firstLoadThrottled) {
            super(thing);
            this.loadFails = loadFails;
            this.firstLoadThrottled = firstLoadThrottled;
        }

        void runScheduledRetry() {
            retryAction.run();
        }

        @Override
        protected boolean initializeCloudHandler() {
            this.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
            return true;
        }

        @Override
        protected RachioFlexScheduleRuleResponse loadFlexScheduleRule() throws RachioApiException {
            loadedFlexScheduleRuleId = flexScheduleRuleId;
            if (firstLoadThrottled) {
                firstLoadThrottled = false;
                throw initializationThrottledException();
            }
            if (loadFails) {
                throw new RachioApiException("not found");
            }

            RachioFlexScheduleRuleResponse response = new RachioFlexScheduleRuleResponse();
            response.id = flexScheduleRuleId;
            response.name = "Flex";
            return response;
        }

        @Override
        protected long scheduleInitializationThrottleRetry(String operation, Runnable retryAction,
                RachioApiThrottledException throttle) {
            retryScheduled = true;
            this.retryAction = retryAction;
            updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NONE,
                    "Waiting for local Rachio API bootstrap budget; initialization will retry automatically.");
            return throttle.getSuggestedRetryDelay().getSeconds();
        }
    }
}
