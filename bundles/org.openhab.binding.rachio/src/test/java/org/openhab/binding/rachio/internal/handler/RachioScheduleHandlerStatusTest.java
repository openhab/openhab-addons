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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_ENABLED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_LAST_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_LAST_UPDATE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_NEXT_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_FLEX_SCHEDULE_START_TIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_LAST_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_NEXT_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_START_TIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_FLEX_SCHEDULE_RULE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_FLEX_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_SCHEDULE;

import java.time.Instant;
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
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * Tests schedule handler status lifecycle around initial refresh failures.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioScheduleHandlerStatusTest {
    private static final long START_EPOCH_MILLIS = 1_767_225_600_000L;
    private static final long LAST_RUN_EPOCH_MILLIS = 1_767_139_200_000L;
    private static final long NEXT_RUN_EPOCH_SECONDS = 1_767_312_000L;

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
    void multipleScheduleRefreshCommandsLoadMissingCacheOnlyOnce() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        CountingScheduleHandler handler = new CountingScheduleHandler(thing, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_NAME), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START_TIME), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_LAST_RUN), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT),
                RefreshType.REFRESH);

        assertThat(handler.loadCount, is(1));
    }

    @Test
    void multipleFlexScheduleRefreshCommandsLoadMissingCacheOnlyOnce() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "flex-id"), Map.of());
        CountingFlexScheduleHandler handler = new CountingFlexScheduleHandler(thing, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_NAME), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_START_TIME), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_LAST_RUN), RefreshType.REFRESH);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT),
                RefreshType.REFRESH);

        assertThat(handler.loadCount, is(1));
    }

    @Test
    void flexScheduleCommandsUseScheduleRuleServiceMethods() throws RachioApiException {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "flex-id"), Map.of());
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        CommandFlexScheduleHandler handler = new CommandFlexScheduleHandler(thing, bridgeHandler);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_START), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_SKIP), OnOffType.ON);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN),
                OnOffType.ON);
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT),
                new DecimalType("0.75"));

        verify(bridgeHandler).startScheduleRule("flex-id");
        verify(bridgeHandler).skipScheduleRule("flex-id");
        verify(bridgeHandler).skipForwardZoneRun("flex-id");
        verify(bridgeHandler).setScheduleRuleSeasonalAdjustment("flex-id", 0.75);
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_START), OnOffType.OFF);
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_SKIP), OnOffType.OFF);
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN),
                OnOffType.OFF);
    }

    @Test
    void scheduleInitializationStillLoadsMissingCache() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        CountingScheduleHandler handler = new CountingScheduleHandler(thing, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        assertThat(handler.loadCount, is(1));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleInitializationStillLoadsMissingCache() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "flex-id"), Map.of());
        CountingFlexScheduleHandler handler = new CountingFlexScheduleHandler(thing, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        assertThat(handler.loadCount, is(1));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void failedScheduleLoadDoesNotPublishCachedChannelData() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        CountingScheduleHandler handler = new CountingScheduleHandler(thing, true);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_NAME), RefreshType.REFRESH);

        assertThat(handler.loadCount, is(1));
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                        && String.valueOf(status.getDescription()).contains("not found")));
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any());
    }

    @Test
    void failedFlexScheduleLoadDoesNotPublishCachedChannelData() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "flex-id"), Map.of());
        CountingFlexScheduleHandler handler = new CountingFlexScheduleHandler(thing, true);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_NAME), RefreshType.REFRESH);

        assertThat(handler.loadCount, is(1));
        verify(callback).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                        && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR
                        && String.valueOf(status.getDescription()).contains("not found")));
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any());
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
    void currentFlexScheduleHandlerResolvesUuidFromThingUidDuringInitialization() {
        ThingUID uid = new ThingUID(THING_TYPE_FLEX_SCHEDULE, new ThingUID(THING_TYPE_CLOUD, "bridge"), "uid-flex-id");
        Thing thing = flexScheduleThing(uid, Map.of(), Map.of());
        InitializingFlexScheduleHandler handler = new InitializingFlexScheduleHandler(thing, false, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        assertThat(handler.loadedFlexScheduleRuleId, is("uid-flex-id"));
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void flexScheduleHandlerPublishesCanonicalChannelIdsDuringInitialization() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "config-flex-id"), Map.of());
        InitializingFlexScheduleHandler handler = new InitializingFlexScheduleHandler(thing, false, false);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_NAME),
                new StringType("Flex"));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_ENABLED), OnOffType.ON);
        verify(callback).stateUpdated(eq(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_START_TIME)),
                argThat(DateTimeType.class::isInstance));
        verify(callback).stateUpdated(eq(new ChannelUID(thing.getUID(), CHANNEL_FLEX_SCHEDULE_LAST_UPDATE)),
                argThat(DateTimeType.class::isInstance));
    }

    @Test
    void scheduleStartDateEpochMillisecondsUpdatesStartTime() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RachioScheduleRuleResponse response = scheduleResponse();
        response.startDate = Long.toString(START_EPOCH_MILLIS);
        ResponseScheduleHandler handler = new ResponseScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_SCHEDULE_START_TIME, Instant.ofEpochMilli(START_EPOCH_MILLIS));
    }

    @Test
    void scheduleStartDateEpochSecondsUpdatesStartTime() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RachioScheduleRuleResponse response = scheduleResponse();
        response.startDate = Long.toString(START_EPOCH_MILLIS / 1000L);
        ResponseScheduleHandler handler = new ResponseScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_SCHEDULE_START_TIME, Instant.ofEpochMilli(START_EPOCH_MILLIS));
    }

    @Test
    void scheduleStartTimeIsoFallbackUpdatesStartTime() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RachioScheduleRuleResponse response = scheduleResponse();
        response.startTime = "2026-05-30T08:00:00Z";
        ResponseScheduleHandler handler = new ResponseScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_SCHEDULE_START_TIME, Instant.parse("2026-05-30T08:00:00Z"));
    }

    @Test
    void scheduleLastRunEpochMillisecondsUpdatesLastRun() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RachioScheduleRuleResponse response = scheduleResponse();
        response.lastRun = Long.toString(LAST_RUN_EPOCH_MILLIS);
        ResponseScheduleHandler handler = new ResponseScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_SCHEDULE_LAST_RUN, Instant.ofEpochMilli(LAST_RUN_EPOCH_MILLIS));
    }

    @Test
    void scheduleNextRunEpochSecondsUpdatesNextRun() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RachioScheduleRuleResponse response = scheduleResponse();
        response.nextRun = Long.toString(NEXT_RUN_EPOCH_SECONDS);
        ResponseScheduleHandler handler = new ResponseScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_SCHEDULE_NEXT_RUN, Instant.ofEpochSecond(NEXT_RUN_EPOCH_SECONDS));
    }

    @Test
    void flexScheduleStartDateEpochMillisecondsUpdatesStartTime() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "flex-id"), Map.of());
        RachioFlexScheduleRuleResponse response = flexScheduleResponse();
        response.startDate = Long.toString(START_EPOCH_MILLIS);
        ResponseFlexScheduleHandler handler = new ResponseFlexScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_FLEX_SCHEDULE_START_TIME,
                Instant.ofEpochMilli(START_EPOCH_MILLIS));
    }

    @Test
    void flexScheduleLastRunEpochValueUpdatesLastRun() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "flex-id"), Map.of());
        RachioFlexScheduleRuleResponse response = flexScheduleResponse();
        response.lastRun = Long.toString(LAST_RUN_EPOCH_MILLIS);
        ResponseFlexScheduleHandler handler = new ResponseFlexScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_FLEX_SCHEDULE_LAST_RUN,
                Instant.ofEpochMilli(LAST_RUN_EPOCH_MILLIS));
    }

    @Test
    void flexScheduleNextRunEpochValueUpdatesNextRun() {
        Thing thing = flexScheduleThing(Map.of(PROPERTY_FLEX_SCHEDULE_RULE_ID, "flex-id"), Map.of());
        RachioFlexScheduleRuleResponse response = flexScheduleResponse();
        response.nextRun = Long.toString(NEXT_RUN_EPOCH_SECONDS);
        ResponseFlexScheduleHandler handler = new ResponseFlexScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verifyDateTimeState(callback, thing, CHANNEL_FLEX_SCHEDULE_NEXT_RUN,
                Instant.ofEpochSecond(NEXT_RUN_EPOCH_SECONDS));
    }

    @Test
    void missingDateFieldsPublishUndef() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        ResponseScheduleHandler handler = new ResponseScheduleHandler(thing, scheduleResponse());
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START_TIME), UnDefType.UNDEF);
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_LAST_RUN), UnDefType.UNDEF);
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_NEXT_RUN), UnDefType.UNDEF);
    }

    @Test
    void invalidDateStringPublishesUndef() {
        Thing thing = thing(new ThingUID(THING_TYPE_SCHEDULE, "bridge", "schedule"));
        RachioScheduleRuleResponse response = scheduleResponse();
        response.startDate = "not-a-date";
        ResponseScheduleHandler handler = new ResponseScheduleHandler(thing, response);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.publicGoOnline();

        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_SCHEDULE_START_TIME), UnDefType.UNDEF);
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
        return flexScheduleThing(new ThingUID(THING_TYPE_FLEX_SCHEDULE, "bridge", "flex"), configuration, properties);
    }

    private Thing flexScheduleThing(ThingUID uid, Map<String, Object> configuration, Map<String, String> properties) {
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

    private static RachioScheduleRuleResponse scheduleResponse() {
        RachioScheduleRuleResponse response = new RachioScheduleRuleResponse();
        response.id = "schedule-id";
        response.name = "Morning";
        response.enabled = true;
        response.type = "FIXED";
        response.seasonalAdjustment = 1;
        return response;
    }

    private static RachioFlexScheduleRuleResponse flexScheduleResponse() {
        RachioFlexScheduleRuleResponse response = new RachioFlexScheduleRuleResponse();
        response.id = "flex-id";
        response.name = "Flex";
        response.enabled = true;
        response.type = "FLEX";
        response.seasonalAdjustment = 1;
        return response;
    }

    private static void verifyDateTimeState(ThingHandlerCallback callback, Thing thing, String channel,
            Instant expectedInstant) {
        verify(callback).stateUpdated(eq(new ChannelUID(thing.getUID(), channel)), argThat(
                state -> state instanceof DateTimeType dateTime && dateTime.getInstant().equals(expectedInstant)));
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

    private static class ResponseScheduleHandler extends RachioScheduleHandler {
        private final RachioScheduleRuleResponse response;

        ResponseScheduleHandler(Thing thing, RachioScheduleRuleResponse response) {
            super(thing);
            this.response = response;
            this.scheduleRuleId = "schedule-id";
            this.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        }

        void publicGoOnline() {
            goOnline();
        }

        @Override
        protected RachioScheduleRuleResponse loadScheduleRule() {
            return response;
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

    private static class CountingScheduleHandler extends RachioScheduleHandler {
        private final boolean loadFails;
        private int loadCount;

        CountingScheduleHandler(Thing thing, boolean loadFails) {
            super(thing);
            this.loadFails = loadFails;
            this.scheduleRuleId = "schedule-id";
            this.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        }

        void publicGoOnline() {
            goOnline();
        }

        @Override
        protected RachioScheduleRuleResponse loadScheduleRule() throws RachioApiException {
            loadCount++;
            if (loadFails) {
                throw new RachioApiException("not found");
            }

            RachioScheduleRuleResponse response = new RachioScheduleRuleResponse();
            response.id = "schedule-id";
            response.name = "Morning";
            response.enabled = true;
            response.type = "FIXED";
            response.startTime = "2026-05-30T08:00:00Z";
            response.lastRun = "2026-05-29T08:00:00Z";
            response.nextRun = "2026-05-31T08:00:00Z";
            response.seasonalAdjustment = 1;
            return response;
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

    private static class CommandFlexScheduleHandler extends RachioFlexScheduleHandler {
        CommandFlexScheduleHandler(Thing thing, RachioBridgeHandler bridgeHandler) {
            super(thing);
            this.flexScheduleRuleId = "flex-id";
            this.cloudHandler = bridgeHandler;
        }
    }

    private static class CountingFlexScheduleHandler extends RachioFlexScheduleHandler {
        private final boolean loadFails;
        private int loadCount;

        CountingFlexScheduleHandler(Thing thing, boolean loadFails) {
            super(thing);
            this.loadFails = loadFails;
            this.flexScheduleRuleId = "flex-id";
            this.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        }

        void publicGoOnline() {
            goOnline();
        }

        @Override
        protected RachioFlexScheduleRuleResponse loadFlexScheduleRule() throws RachioApiException {
            loadCount++;
            if (loadFails) {
                throw new RachioApiException("not found");
            }

            RachioFlexScheduleRuleResponse response = new RachioFlexScheduleRuleResponse();
            response.id = "flex-id";
            response.name = "Flex";
            response.enabled = true;
            response.type = "FLEX";
            response.startTime = "2026-05-30T08:00:00Z";
            response.lastRun = "2026-05-29T08:00:00Z";
            response.nextRun = "2026-05-31T08:00:00Z";
            response.seasonalAdjustment = 1;
            return response;
        }
    }

    private static class ResponseFlexScheduleHandler extends RachioFlexScheduleHandler {
        private final RachioFlexScheduleRuleResponse response;

        ResponseFlexScheduleHandler(Thing thing, RachioFlexScheduleRuleResponse response) {
            super(thing);
            this.response = response;
            this.flexScheduleRuleId = "flex-id";
            this.cloudHandler = Mockito.mock(RachioBridgeHandler.class);
        }

        void publicGoOnline() {
            goOnline();
        }

        @Override
        protected RachioFlexScheduleRuleResponse loadFlexScheduleRule() {
            return response;
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
            response.enabled = true;
            response.type = "FLEX";
            response.startTime = "2026-05-30T08:00:00Z";
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
