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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_BASE_STATION_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_EVENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_EVENTTS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_CANCEL_NEXT_PLANNED_RUN_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_DEFAULT_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_FLOW_DETECTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_LAST_END_REASON;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_LAST_RUN_TYPE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_NEXT_PLANNED_RUN_PROGRAM_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_NEXT_PLANNED_RUN_SKIPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_DURATION;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_CANCELED_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_NEXT_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_NEXT_RUN_SKIPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_SKIP_NEXT_PLANNED_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_PROGRAM_VALVE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_VALVE_SKIP_NEXT_PLANNED_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_PROGRAM_RAIN_SKIP_CANCELED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_PROGRAM_RAIN_SKIP_CREATED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_VALVE_RUN_END;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_VALVE_RUN_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_BASE_STATION_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_VALVE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_VALVE_PROGRAM_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_BASE_STATION;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE_PROGRAM;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO.RachioWebhookPayload;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioBaseStation;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayRun;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayView;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveDayViewsResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveState;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests polling-based Smart Hose Timer handler behavior.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class RachioSmartHoseTimerHandlerTest {
    private static final String BASE_STATION_ID = "base-station-id";
    private static final String VALVE_ID = "valve-id";
    private static final String PROGRAM_ID = "program-id";

    @Test
    void baseStationRefreshUsesPollingDataAndKeepsThingOnline() throws Exception {
        Thing thing = thing(THING_TYPE_BASE_STATION, "base-station", Map.of(PROPERTY_BASE_STATION_ID, BASE_STATION_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestBaseStationHandler handler = new TestBaseStationHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getBaseStationForInitialization(BASE_STATION_ID))
                .thenReturn(baseStation(BASE_STATION_ID, "Base station", true));
        when(bridgeHandler.getBaseStation(BASE_STATION_ID))
                .thenReturn(baseStation(BASE_STATION_ID, "Updated base station", true));
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        clearInvocations(callback, bridgeHandler);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_BASE_STATION_NAME), RefreshType.REFRESH);

        verify(bridgeHandler).getBaseStation(BASE_STATION_ID);
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_BASE_STATION_NAME),
                new StringType("Updated base station"));
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void unresolvedBaseStationDoesNotReportOnline() throws Exception {
        Thing thing = thing(THING_TYPE_BASE_STATION, "base-station", Map.of(PROPERTY_BASE_STATION_ID, BASE_STATION_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestBaseStationHandler handler = new TestBaseStationHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getBaseStationForInitialization(BASE_STATION_ID)).thenReturn(new RachioBaseStation());
        handler.setCallback(callback);

        handler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void valveRefreshUsesPollingDataWithoutWebhookRenewal() throws Exception {
        Thing thing = thing(THING_TYPE_VALVE, "valve", Map.of(PROPERTY_VALVE_ID, VALVE_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveHandler handler = new TestValveHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getValveForInitialization(VALVE_ID)).thenReturn(valve(VALVE_ID, "Valve", 300));
        when(bridgeHandler.getValve(VALVE_ID)).thenReturn(valve(VALVE_ID, "Updated valve", 900));
        when(bridgeHandler.getValveDayViews(VALVE_ID)).thenReturn(
                summary(plannedRun("old-program", futureTimestamp(), 300, true, "planned-run-old")),
                summary(plannedRun(PROGRAM_ID, futureTimestamp(), 600, false, "planned-run-new")));
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_VALVE_NAME), RefreshType.REFRESH);

        verify(bridgeHandler).getValve(VALVE_ID);
        verify(bridgeHandler).getValveDayViews(VALVE_ID);
        verify(bridgeHandler, never()).registerValveWebHook(anyString(), any(RequestPurpose.class));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_NAME),
                new StringType("Updated valve"));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_DEFAULT_RUNTIME),
                RachioQuantityTypes.seconds(900));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_NEXT_PLANNED_RUN_PROGRAM_ID),
                new StringType(PROGRAM_ID));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_NEXT_PLANNED_RUN_SKIPPED),
                OnOffType.OFF);
    }

    @Test
    void valveSkipCommandNoOpsWhenPlannedRunIdentifiersAreMissing() throws Exception {
        Thing thing = thing(THING_TYPE_VALVE, "valve", Map.of(PROPERTY_VALVE_ID, VALVE_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveHandler handler = new TestValveHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getValveForInitialization(VALVE_ID)).thenReturn(valve(VALVE_ID, "Valve", 300));
        when(bridgeHandler.getValveDayViews(VALVE_ID))
                .thenReturn(summary(runWithoutSkipIdentifier(futureTimestamp(), false)));
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_VALVE_SKIP_NEXT_PLANNED_RUN), OnOffType.ON);

        verify(bridgeHandler, never()).createPlannedRunSkipOverride(anyString(), anyString());
        verify(bridgeHandler, never()).createSkipOverride(anyString(), anyString());
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_SKIP_NEXT_PLANNED_RUN),
                OnOffType.OFF);
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void valveCancelSkipCommandNoOpsWhenSkippedRunIdentifiersAreMissing() throws Exception {
        Thing thing = thing(THING_TYPE_VALVE, "valve", Map.of(PROPERTY_VALVE_ID, VALVE_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveHandler handler = new TestValveHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getValveForInitialization(VALVE_ID)).thenReturn(valve(VALVE_ID, "Valve", 300));
        when(bridgeHandler.getValveDayViews(VALVE_ID))
                .thenReturn(summary(runWithoutSkipIdentifier(futureTimestamp(), true)));
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_VALVE_CANCEL_NEXT_PLANNED_RUN_SKIP), OnOffType.ON);

        verify(bridgeHandler, never()).deletePlannedRunSkipOverride(anyString(), anyString());
        verify(bridgeHandler, never()).deleteSkipOverride(anyString(), anyString());
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_CANCEL_NEXT_PLANNED_RUN_SKIP),
                OnOffType.OFF);
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void valveProgramRefreshUsesPollingDataWithoutWebhookRenewal() throws Exception {
        Thing thing = thing(THING_TYPE_VALVE_PROGRAM, "program", Map.of(PROPERTY_VALVE_PROGRAM_ID, PROGRAM_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveProgramHandler handler = new TestValveProgramHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getValveProgramForInitialization(PROGRAM_ID))
                .thenReturn(program(PROGRAM_ID, "Program", "", 300));
        when(bridgeHandler.getValveProgram(PROGRAM_ID))
                .thenReturn(program(PROGRAM_ID, "Updated program", VALVE_ID, 900));
        when(bridgeHandler.getValveDayViews(VALVE_ID))
                .thenReturn(summary(plannedRun(PROGRAM_ID, futureTimestamp(), 900, false, "planned-run-new")));
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_NAME), RefreshType.REFRESH);

        verify(bridgeHandler).getValveProgram(PROGRAM_ID);
        verify(bridgeHandler).getValveDayViews(VALVE_ID);
        verify(bridgeHandler, never()).registerValveProgramWebHook(anyString(), any(RequestPurpose.class));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_NAME),
                new StringType("Updated program"));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_VALVE_ID),
                new StringType(VALVE_ID));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_DURATION),
                RachioQuantityTypes.seconds(900));
        verify(callback).stateUpdated(eq(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_NEXT_RUNTIME)),
                any(State.class));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_NEXT_RUN_SKIPPED),
                OnOffType.OFF);
    }

    @Test
    void valveProgramWithoutValveAssociationKeepsSummaryChannelsEmpty() throws Exception {
        Thing thing = thing(THING_TYPE_VALVE_PROGRAM, "program", Map.of(PROPERTY_VALVE_PROGRAM_ID, PROGRAM_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveProgramHandler handler = new TestValveProgramHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getValveProgramForInitialization(PROGRAM_ID))
                .thenReturn(program(PROGRAM_ID, "Program without valve", "", 300));
        handler.setCallback(callback);

        handler.initialize();

        verify(bridgeHandler, never()).getValveDayViews(anyString());
        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_VALVE_ID), UnDefType.UNDEF);
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_NEXT_RUNTIME),
                UnDefType.UNDEF);
    }

    @Test
    void valveProgramSkipCommandNoOpsWhenSummaryIdentifiersAreMissing() throws Exception {
        Thing thing = thing(THING_TYPE_VALVE_PROGRAM, "program", Map.of(PROPERTY_VALVE_PROGRAM_ID, PROGRAM_ID));
        RachioBridgeHandler bridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveProgramHandler handler = new TestValveProgramHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        when(bridgeHandler.getValveProgramForInitialization(PROGRAM_ID))
                .thenReturn(program(PROGRAM_ID, "Program", VALVE_ID, 300));
        when(bridgeHandler.getValveDayViews(VALVE_ID))
                .thenReturn(summary(programRunWithoutTimestamp(PROGRAM_ID, false)));
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);

        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_SKIP_NEXT_PLANNED_RUN),
                OnOffType.ON);

        verify(bridgeHandler, never()).createPlannedRunSkipOverride(anyString(), anyString());
        verify(bridgeHandler, never()).createSkipOverride(anyString(), anyString());
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), CHANNEL_VALVE_PROGRAM_SKIP_NEXT_PLANNED_RUN),
                OnOffType.OFF);
        verify(callback, never()).statusUpdated(eq(thing),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    @Test
    void modernValveRunStartEventDirectlyUpdatesValveThing() throws Exception {
        SmartHoseValveFixture fixture = smartHoseValveFixture();
        RachioEventGsonDTO event = valveEvent(EVENT_VALVE_RUN_START, "181", "PROGRAM", "", Boolean.TRUE);

        assertThat(fixture.bridgeHandler.webHookEvent(event), is(true));

        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_RUN), OnOffType.ON);
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_RUNTIME),
                RachioQuantityTypes.seconds(181));
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_LAST_RUN_TYPE),
                new StringType("PROGRAM"));
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_FLOW_DETECTED),
                OnOffType.ON);
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_LAST_EVENT),
                new StringType(EVENT_VALVE_RUN_START));
        verify(fixture.callback).stateUpdated(eq(new ChannelUID(fixture.thing.getUID(), CHANNEL_LAST_EVENTTS)),
                any(DateTimeType.class));
        verify(fixture.bridgeHandler).getValveDayViews(VALVE_ID);
        verify(fixture.bridgeHandler, never()).refreshDeviceStatus(any());
    }

    @Test
    void modernValveRunEventIsIgnoredWhenHoseTimerWebhooksAreDisabled() throws Exception {
        SmartHoseValveFixture fixture = smartHoseValveFixture(false);
        RachioEventGsonDTO event = valveEvent(EVENT_VALVE_RUN_START, "181", "PROGRAM", "", Boolean.TRUE);

        assertThat(fixture.bridgeHandler.webHookEvent(event), is(true));

        verify(fixture.callback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
        verify(fixture.bridgeHandler, never()).getValveDayViews(VALVE_ID);
        verify(fixture.bridgeHandler, never()).refreshDeviceStatus(any());
    }

    @Test
    void modernValveRunEndEventDirectlyUpdatesValveThing() throws Exception {
        SmartHoseValveFixture fixture = smartHoseValveFixture();
        assertThat(fixture.bridgeHandler
                .webHookEvent(valveEvent(EVENT_VALVE_RUN_START, "181", "PROGRAM", "", Boolean.TRUE)), is(true));
        clearInvocations(fixture.callback, fixture.bridgeHandler);

        RachioEventGsonDTO event = valveEvent(EVENT_VALVE_RUN_END, "42", "MANUAL", "USER_STOPPED", Boolean.FALSE);

        assertThat(fixture.bridgeHandler.webHookEvent(event), is(true));

        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_RUN), OnOffType.OFF);
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_RUNTIME),
                RachioQuantityTypes.seconds(42));
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_LAST_RUN_TYPE),
                new StringType("MANUAL"));
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_LAST_END_REASON),
                new StringType("USER_STOPPED"));
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_FLOW_DETECTED),
                OnOffType.OFF);
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_LAST_EVENT),
                new StringType(EVENT_VALVE_RUN_END));
        verify(fixture.bridgeHandler).getValveDayViews(VALVE_ID);
        verify(fixture.bridgeHandler, never()).refreshDeviceStatus(any());
    }

    @Test
    void modernProgramRainSkipCreatedEventDirectlyUpdatesProgramThing() throws Exception {
        SmartHoseProgramFixture fixture = smartHoseProgramFixture();
        RachioEventGsonDTO event = programEvent(EVENT_PROGRAM_RAIN_SKIP_CREATED, "2026-06-20T10:00:00Z");

        assertThat(fixture.bridgeHandler.webHookEvent(event), is(true));

        verify(fixture.callback).stateUpdated(
                new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_START),
                new DateTimeType("2026-06-20T10:00:00Z"));
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_LAST_EVENT),
                new StringType(EVENT_PROGRAM_RAIN_SKIP_CREATED));
        verify(fixture.callback).stateUpdated(eq(new ChannelUID(fixture.thing.getUID(), CHANNEL_LAST_EVENTTS)),
                any(DateTimeType.class));
        verify(fixture.bridgeHandler).getValveDayViews(VALVE_ID);
        verify(fixture.bridgeHandler, never()).refreshDeviceStatus(any());
    }

    @Test
    void modernProgramEventIsIgnoredWhenHoseTimerWebhooksAreDisabled() throws Exception {
        SmartHoseProgramFixture fixture = smartHoseProgramFixture(false);
        RachioEventGsonDTO event = programEvent(EVENT_PROGRAM_RAIN_SKIP_CREATED, "2026-06-20T10:00:00Z");

        assertThat(fixture.bridgeHandler.webHookEvent(event), is(true));

        verify(fixture.callback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
        verify(fixture.bridgeHandler, never()).getValveDayViews(VALVE_ID);
        verify(fixture.bridgeHandler, never()).refreshDeviceStatus(any());
    }

    @Test
    void modernProgramRainSkipCanceledEventDirectlyUpdatesProgramThing() throws Exception {
        SmartHoseProgramFixture fixture = smartHoseProgramFixture();
        RachioEventGsonDTO event = programEvent(EVENT_PROGRAM_RAIN_SKIP_CANCELED, "2026-06-20T10:00:00Z");

        assertThat(fixture.bridgeHandler.webHookEvent(event), is(true));

        verify(fixture.callback).stateUpdated(
                new ChannelUID(fixture.thing.getUID(), CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_CANCELED_START),
                new DateTimeType("2026-06-20T10:00:00Z"));
        verify(fixture.callback).stateUpdated(new ChannelUID(fixture.thing.getUID(), CHANNEL_LAST_EVENT),
                new StringType(EVENT_PROGRAM_RAIN_SKIP_CANCELED));
        verify(fixture.bridgeHandler).getValveDayViews(VALVE_ID);
        verify(fixture.bridgeHandler, never()).refreshDeviceStatus(any());
    }

    @Test
    void modernValveOrProgramEventWithoutThingHandlerIsAcknowledgedSafely() throws Exception {
        RachioBridgeHandler bridgeHandler = Mockito
                .spy(new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build()));
        configureHoseTimerWebhookMode(bridgeHandler, true);
        Mockito.doNothing().when(bridgeHandler).refreshDeviceStatus(any());

        assertThat(bridgeHandler.webHookEvent(valveEvent(EVENT_VALVE_RUN_START, "181", "PROGRAM", "", Boolean.TRUE)),
                is(true));
        assertThat(bridgeHandler.webHookEvent(programEvent(EVENT_PROGRAM_RAIN_SKIP_CREATED, "2026-06-20T10:00:00Z")),
                is(true));

        verify(bridgeHandler, never()).refreshDeviceStatus(any());
    }

    private SmartHoseValveFixture smartHoseValveFixture() throws Exception {
        return smartHoseValveFixture(true);
    }

    private SmartHoseValveFixture smartHoseValveFixture(boolean hoseTimerWebhooksEnabled) throws Exception {
        Thing thing = thing(THING_TYPE_VALVE, "valve", Map.of(PROPERTY_VALVE_ID, VALVE_ID));
        RachioBridgeHandler bridgeHandler = Mockito
                .spy(new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build()));
        configureHoseTimerWebhookMode(bridgeHandler, hoseTimerWebhooksEnabled);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveHandler handler = new TestValveHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        Mockito.doReturn(valve(VALVE_ID, "Valve", 300)).when(bridgeHandler).getValveForInitialization(VALVE_ID);
        Mockito.doReturn(summary()).when(bridgeHandler).getValveDayViews(VALVE_ID);
        Mockito.doNothing().when(bridgeHandler).registerValveWebHook(anyString(), any(RequestPurpose.class));
        Mockito.doNothing().when(bridgeHandler).refreshDeviceStatus(any());
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);
        return new SmartHoseValveFixture(bridgeHandler, thing, callback);
    }

    private SmartHoseProgramFixture smartHoseProgramFixture() throws Exception {
        return smartHoseProgramFixture(true);
    }

    private SmartHoseProgramFixture smartHoseProgramFixture(boolean hoseTimerWebhooksEnabled) throws Exception {
        Thing thing = thing(THING_TYPE_VALVE_PROGRAM, "program", Map.of(PROPERTY_VALVE_PROGRAM_ID, PROGRAM_ID));
        RachioBridgeHandler bridgeHandler = Mockito
                .spy(new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build()));
        configureHoseTimerWebhookMode(bridgeHandler, hoseTimerWebhooksEnabled);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        TestValveProgramHandler handler = new TestValveProgramHandler(thing, bridgeHandler, ThingStatus.ONLINE);
        Mockito.doReturn(program(PROGRAM_ID, "Program", VALVE_ID, 300)).when(bridgeHandler)
                .getValveProgramForInitialization(PROGRAM_ID);
        Mockito.doReturn(summary(plannedRun(PROGRAM_ID, futureTimestamp(), 300, false, "planned-run")))
                .when(bridgeHandler).getValveDayViews(VALVE_ID);
        Mockito.doNothing().when(bridgeHandler).registerValveProgramWebHook(anyString(), any(RequestPurpose.class));
        Mockito.doNothing().when(bridgeHandler).refreshDeviceStatus(any());
        handler.setCallback(callback);
        handler.initialize();
        clearInvocations(callback, bridgeHandler);
        return new SmartHoseProgramFixture(bridgeHandler, thing, callback);
    }

    private void configureHoseTimerWebhookMode(RachioBridgeHandler bridgeHandler, boolean hoseTimerWebhooksEnabled)
            throws Exception {
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.autoConfigureHoseTimerWebhooks = hoseTimerWebhooksEnabled;
        config.publicWebhookUrl = "https://example.org/rachio/webhook";
        setField(bridgeHandler, "thingConfig", config);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> type = target.getClass();
        Field field = null;
        while (type != null && field == null) {
            try {
                field = type.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }
        field.setAccessible(true);
        field.set(target, value);
    }

    private RachioEventGsonDTO valveEvent(String eventType, String durationSeconds, String runType, String endReason,
            Boolean flowDetected) {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.eventId = eventType + "-id";
        event.eventType = eventType;
        event.resourceType = "VALVE";
        event.resourceId = VALVE_ID;
        event.timestamp = "2026-06-20T10:00:00Z";
        RachioWebhookPayload payload = new RachioWebhookPayload();
        payload.valveId = VALVE_ID;
        payload.durationSeconds = durationSeconds;
        payload.runType = runType;
        payload.endReason = endReason;
        payload.flowDetected = flowDetected;
        event.payload = payload;
        return event;
    }

    private RachioEventGsonDTO programEvent(String eventType, String plannedRunStartTime) {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.eventId = eventType + "-id";
        event.eventType = eventType;
        event.resourceType = "PROGRAM";
        event.resourceId = PROGRAM_ID;
        event.timestamp = "2026-06-20T09:00:00Z";
        RachioWebhookPayload payload = new RachioWebhookPayload();
        payload.programId = PROGRAM_ID;
        payload.plannedRunStartTime = plannedRunStartTime;
        event.payload = payload;
        return event;
    }

    private Thing thing(ThingTypeUID type, String id, Map<String, Object> configuration) {
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(type, "bridge", id));
        when(thing.getConfiguration()).thenReturn(new Configuration(configuration));
        when(thing.getProperties()).thenReturn(new HashMap<>());
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        return thing;
    }

    private RachioBaseStation baseStation(String id, String name, boolean online) {
        RachioBaseStation baseStation = new RachioBaseStation();
        baseStation.id = id;
        baseStation.name = name;
        baseStation.online = Boolean.valueOf(online);
        return baseStation;
    }

    private RachioValve valve(String id, String name, int defaultRuntimeSeconds) {
        RachioValve valve = new RachioValve();
        valve.id = id;
        valve.baseStationId = BASE_STATION_ID;
        valve.name = name;
        valve.online = Boolean.TRUE;
        valve.serialNumber = "valve-serial";
        valve.batteryLevel = Double.valueOf(87.0);
        valve.defaultRuntimeSeconds = Integer.valueOf(defaultRuntimeSeconds);
        RachioValveState state = new RachioValveState();
        state.matches = Boolean.TRUE;
        state.flowDetected = Boolean.FALSE;
        valve.state = state;
        return valve;
    }

    private RachioValveProgram program(String id, String name, String valveId, int durationSeconds) {
        RachioValveProgram program = new RachioValveProgram();
        program.id = id;
        program.name = name;
        program.enabled = Boolean.TRUE;
        program.type = "INTERVAL";
        program.valveId = valveId;
        program.startTime = "06:00";
        program.durationSeconds = durationSeconds;
        program.intervalDays = 2;
        program.seasonalAdjustment = 1.0;
        return program;
    }

    private RachioValveDayViewsResponse summary(RachioValveDayRun... runs) {
        RachioValveDayView dayView = new RachioValveDayView();
        dayView.runs.addAll(List.of(runs));
        RachioValveDayViewsResponse response = new RachioValveDayViewsResponse();
        response.dayViews.add(dayView);
        return response;
    }

    private RachioValveDayRun plannedRun(String programId, String startTime, int durationSeconds, boolean skipped,
            String plannedRunId) {
        RachioValveDayRun run = new RachioValveDayRun();
        run.id = plannedRunId;
        run.plannedRunId = plannedRunId;
        run.programId = programId;
        run.valveId = VALVE_ID;
        run.startTime = startTime;
        run.durationSeconds = durationSeconds;
        run.skipped = Boolean.valueOf(skipped);
        return run;
    }

    private RachioValveDayRun runWithoutSkipIdentifier(String startTime, boolean skipped) {
        RachioValveDayRun run = new RachioValveDayRun();
        run.valveId = VALVE_ID;
        run.startTime = startTime;
        run.durationSeconds = 300;
        run.skipped = Boolean.valueOf(skipped);
        return run;
    }

    private RachioValveDayRun programRunWithoutTimestamp(String programId, boolean skipped) {
        RachioValveDayRun run = new RachioValveDayRun();
        run.programId = programId;
        run.valveId = VALVE_ID;
        run.durationSeconds = 300;
        run.skipped = Boolean.valueOf(skipped);
        return run;
    }

    private String futureTimestamp() {
        return Instant.now().plus(Duration.ofDays(1)).toString();
    }

    private record SmartHoseValveFixture(RachioBridgeHandler bridgeHandler, Thing thing,
            ThingHandlerCallback callback) {
    }

    private record SmartHoseProgramFixture(RachioBridgeHandler bridgeHandler, Thing thing,
            ThingHandlerCallback callback) {
    }

    private static class TestBaseStationHandler extends RachioBaseStationHandler {
        private final RachioBridgeHandler bridgeHandler;
        private final Bridge bridgeThing = Mockito.mock(Bridge.class);

        TestBaseStationHandler(Thing thing, RachioBridgeHandler bridgeHandler, ThingStatus bridgeStatus) {
            super(thing);
            this.bridgeHandler = bridgeHandler;
            when(bridgeThing.getStatus()).thenReturn(bridgeStatus);
        }

        @Override
        protected boolean initializeCloudHandler() {
            cloudHandler = bridgeHandler;
            bridge = bridgeThing;
            return true;
        }
    }

    private static class TestValveHandler extends RachioValveHandler {
        private final RachioBridgeHandler bridgeHandler;
        private final Bridge bridgeThing = Mockito.mock(Bridge.class);

        TestValveHandler(Thing thing, RachioBridgeHandler bridgeHandler, ThingStatus bridgeStatus) {
            super(thing);
            this.bridgeHandler = bridgeHandler;
            when(bridgeThing.getStatus()).thenReturn(bridgeStatus);
        }

        @Override
        protected boolean initializeCloudHandler() {
            cloudHandler = bridgeHandler;
            bridge = bridgeThing;
            return true;
        }
    }

    private static class TestValveProgramHandler extends RachioValveProgramHandler {
        private final RachioBridgeHandler bridgeHandler;
        private final Bridge bridgeThing = Mockito.mock(Bridge.class);

        TestValveProgramHandler(Thing thing, RachioBridgeHandler bridgeHandler, ThingStatus bridgeStatus) {
            super(thing);
            this.bridgeHandler = bridgeHandler;
            when(bridgeThing.getStatus()).thenReturn(bridgeStatus);
        }

        @Override
        protected boolean initializeCloudHandler() {
            cloudHandler = bridgeHandler;
            bridge = bridgeThing;
            return true;
        }
    }
}
