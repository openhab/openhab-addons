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
package org.openhab.binding.evohome.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.evohome.internal.EvohomeBindingConstants.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.evohome.internal.api.EvohomeApiClient.CachedSchedule;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneSchedule;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneStatus;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

/**
 * Tests temperature availability, override commands and cloud-confirmed state publication.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class EvohomeHeatingZoneHandlerTest {
    private final Thing thing = ThingBuilder.create(THING_TYPE_EVOHOME_HEATING_ZONE, "zone").build();
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final EvohomeHeatingZoneHandler handler = new EvohomeHeatingZoneHandler(thing);

    private ZoneStatus status(String temperature) {
        return Objects.requireNonNull(new Gson().fromJson("""
                {"zoneId":"zone","activeFaults":[],"temperatureStatus":%s,
                "setpointStatus":{"targetHeatTemperature":21,"setpointMode":"FollowSchedule"}}
                """.formatted(temperature), ZoneStatus.class));
    }

    private CachedSchedule schedule(@Nullable ZoneId timeZone) {
        String json = "{\"dailySchedules\":["
                + List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").stream()
                        .map(day -> """
                                {"dayOfWeek":"%s","switchpoints":[{"timeOfDay":"06:00:00","heatSetpoint":18.5},
                                {"timeOfDay":"18:00:00","heatSetpoint":20.5}]}
                                """.formatted(day)).reduce((left, right) -> left + "," + right).orElseThrow()
                + "]}";
        ZoneSchedule schedule = Objects.requireNonNull(new Gson().fromJson(json, ZoneSchedule.class));
        schedule.validate();
        return new CachedSchedule(schedule, Instant.parse("2026-10-11T13:00:00Z"), timeZone);
    }

    @Test
    void scheduleReadbackPublishesWeeklyProgramAndMatchingNextTimeTemperaturePair() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("PermanentOverride", null);
        CachedSchedule cache = schedule(ZoneId.of("Europe/Budapest"));

        fixture.handler.updateSchedule(cache, Instant.parse("2026-10-11T15:00:00Z"));

        ArgumentCaptor<State> weekly = ArgumentCaptor.forClass(State.class);
        verify(fixture.callback).stateUpdated(eq(fixture.channel("WeeklySchedule")), weekly.capture());
        var days = JsonParser.parseString(Objects.requireNonNull(weekly.getValue()).toString()).getAsJsonObject()
                .getAsJsonArray("dailySchedules");
        assertEquals(7, days.size());
        assertEquals("Monday", days.get(0).getAsJsonObject().get("dayOfWeek").getAsString());
        assertEquals(20.5, days.get(0).getAsJsonObject().getAsJsonArray("switchpoints").get(1).getAsJsonObject()
                .get("heatSetpoint").getAsDouble());
        verify(fixture.callback).stateUpdated(fixture.channel("NextSwitchpoint"),
                new DateTimeType(Instant.parse("2026-10-11T16:00:00Z").atZone(ZoneOffset.UTC)));
        verify(fixture.callback).stateUpdated(fixture.channel("NextScheduledTemperature"),
                new QuantityType<>(20.5, SIUnits.CELSIUS));
        verify(fixture.callback).stateUpdated(fixture.channel("ScheduleLastUpdated"),
                new DateTimeType(cache.updatedAt().atZone(ZoneOffset.UTC)));
        verify(fixture.callback, never()).stateUpdated(eq(fixture.channel("SetPoint")), any());
        verifyNoInteractions(fixture.bridge);
    }

    @Test
    void cachedNextPointRollsOverAtTheSwitchpointWithoutACloudRequest() {
        CommandFixture fixture = new CommandFixture(0);
        CachedSchedule cache = schedule(ZoneId.of("Europe/Budapest"));
        fixture.handler.updateSchedule(cache, Instant.parse("2026-10-11T15:59:59Z"));
        clearInvocations(fixture.callback);

        fixture.handler.updateSchedule(cache, Instant.parse("2026-10-11T16:00:00Z"));

        verify(fixture.callback).stateUpdated(fixture.channel("NextSwitchpoint"),
                new DateTimeType(Instant.parse("2026-10-12T04:00:00Z").atZone(ZoneOffset.UTC)));
        verify(fixture.callback).stateUpdated(fixture.channel("NextScheduledTemperature"),
                new QuantityType<>(18.5, SIUnits.CELSIUS));
        verifyNoInteractions(fixture.bridge);
    }

    @Test
    void missingScheduleCacheClearsAllFourReadonlyStates() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.handler.updateSchedule(null, Instant.parse("2026-10-11T15:00:00Z"));
        for (String channel : List.of("WeeklySchedule", "NextSwitchpoint", "NextScheduledTemperature",
                "ScheduleLastUpdated")) {
            verify(fixture.callback).stateUpdated(fixture.channel(channel), UnDefType.UNDEF);
        }
        verify(fixture.callback, never()).statusUpdated(any(), any());
        verifyNoInteractions(fixture.bridge);
    }

    @Test
    void unknownTimezoneStillPublishesWeeklyProgramAndItsActualFetchTimestamp() {
        CommandFixture fixture = new CommandFixture(0);
        CachedSchedule cache = schedule(null);

        fixture.handler.updateSchedule(cache, Instant.parse("2026-10-11T15:00:00Z"));

        verify(fixture.callback).stateUpdated(fixture.channel("WeeklySchedule"),
                new StringType(cache.schedule().toJson()));
        verify(fixture.callback).stateUpdated(fixture.channel("ScheduleLastUpdated"),
                new DateTimeType(cache.updatedAt().atZone(ZoneOffset.UTC)));
        verify(fixture.callback).stateUpdated(fixture.channel("NextSwitchpoint"), UnDefType.UNDEF);
        verify(fixture.callback).stateUpdated(fixture.channel("NextScheduledTemperature"), UnDefType.UNDEF);
        verify(fixture.callback, never()).statusUpdated(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "WeeklySchedule", "NextSwitchpoint", "NextScheduledTemperature", "ScheduleLastUpdated" })
    void refreshReadonlyScheduleChannelsQueuesOnlyTheRequestedZone(String channel) {
        CommandFixture fixture = new CommandFixture(0);
        when(fixture.bridge.requestScheduleRefresh("zone")).thenReturn(true);

        fixture.command(channel, RefreshType.REFRESH);

        verify(fixture.bridge).requestScheduleRefresh("zone");
        verify(fixture.bridge, never()).requestRefresh();
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void readonlyScheduleChannelsIgnoreWriteCommands() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command("WeeklySchedule", new StringType("{}"));
        fixture.command("NextSwitchpoint", new DateTimeType(Instant.now().atZone(ZoneOffset.UTC)));
        fixture.command("NextScheduledTemperature", new QuantityType<>(20.5, SIUnits.CELSIUS));
        fixture.command("ScheduleLastUpdated", new DateTimeType(Instant.now().atZone(ZoneOffset.UTC)));
        verifyNoInteractions(fixture.bridge);
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = { "WeeklySchedule", "NextSwitchpoint", "NextScheduledTemperature", "ScheduleLastUpdated" })
    void eachScheduleLinkEnablesOptionalReadsAndTriggersANormalPoll(String channel) {
        CommandFixture fixture = new CommandFixture(0);
        assertFalse(fixture.handler.hasScheduleLinks());
        when(fixture.callback.isChannelLinked(fixture.channel(channel))).thenReturn(true);
        assertTrue(fixture.handler.hasScheduleLinks());

        fixture.handler.channelLinked(fixture.channel(channel));

        verify(fixture.bridge).requestRefresh();
        verify(fixture.bridge, never()).requestScheduleRefresh(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "{}", "{\"isAvailable\":false,\"temperature\":19}", "{\"isAvailable\":true}",
            "{\"isAvailable\":true,\"temperature\":null}" })
    void unavailableTemperatureIsUndefinedWhileSetPointIsUpdated(String temperature) {
        handler.setCallback(callback);
        handler.update(null, status(temperature));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), ZONE_TEMPERATURE_CHANNEL), UnDefType.UNDEF);
        verify(callback).stateUpdated(eq(new ChannelUID(thing.getUID(), ZONE_SET_POINT_CHANNEL)),
                eq(new QuantityType<>(21, SIUnits.CELSIUS)));
        verify(callback).stateUpdated(eq(new ChannelUID(thing.getUID(), ZONE_TEMPORARY_SET_POINT_CHANNEL)),
                eq(new QuantityType<>(21, SIUnits.CELSIUS)));
    }

    @Test
    void genuineZeroTemperatureRemainsValid() {
        handler.setCallback(callback);
        handler.update(null, status("{\"isAvailable\":true,\"temperature\":0}"));
        verify(callback).stateUpdated(new ChannelUID(thing.getUID(), ZONE_TEMPERATURE_CHANNEL),
                new QuantityType<>(0, SIUnits.CELSIUS));
    }

    @Test
    void ordinarySetPointCreatesPermanentOverrideFromSchedule() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("FollowSchedule", null);
        fixture.command("SetPoint", new QuantityType<>(21.5, SIUnits.CELSIUS));
        verify(fixture.bridge).setPermanentSetPoint("zone", 21.5);
        verify(fixture.bridge, never()).cancelSetPointOverride(anyString());
        verify(fixture.bridge, never()).setTemporarySetPoint(anyString(), anyDouble(), any());
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void fahrenheitSetPointIsConvertedToCelsius() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command("SetPoint", new QuantityType<>("68 °F"));
        verify(fixture.bridge).setPermanentSetPoint("zone", 20);
    }

    @Test
    void unitlessDecimalSetPointRetainsLegacyCelsiusInterpretation() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command("SetPoint", new DecimalType("21.5"));
        verify(fixture.bridge).setPermanentSetPoint("zone", 21.5);
    }

    @Test
    void legacyZeroCelsiusCancelsOnlyTheZoneOverride() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command("SetPoint", new QuantityType<>(0, SIUnits.CELSIUS));
        verify(fixture.bridge).cancelSetPointOverride("zone");
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
        verify(fixture.bridge, never()).setTemporarySetPointUntilNextSwitchpoint(anyString(), anyDouble());
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void rejectedTemperatureIsNotSilentlyRaisedToFrostSetPoint() {
        CommandFixture fixture = new CommandFixture(0);
        when(fixture.bridge.setPermanentSetPoint("zone", 4)).thenReturn(false);
        fixture.command("SetPoint", new QuantityType<>(4, SIUnits.CELSIUS));
        verify(fixture.bridge).setPermanentSetPoint("zone", 4);
        verify(fixture.bridge, never()).setPermanentSetPoint("zone", 5);
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void incompatibleUnitIsIgnoredWithoutSendingACommand() {
        CommandFixture fixture = new CommandFixture(0);
        assertDoesNotThrow(() -> fixture.command("SetPoint", new QuantityType<>("10 W")));
        verifyNoInteractions(fixture.bridge);
    }

    @ParameterizedTest
    @ValueSource(strings = { "1e9999", "-1e9999" })
    void nonfiniteConvertedTemperatureDoesNotSendACommand(String temperature) {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command("SetPoint", new DecimalType(temperature));
        verifyNoInteractions(fixture.bridge);
    }

    @Test
    void explicitlyConfiguredZeroDurationUsesNextSwitchpoint() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command("TemporarySetPoint", new DecimalType(22));
        verify(fixture.bridge).setTemporarySetPointUntilNextSwitchpoint("zone", 22);
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
        verify(fixture.bridge, never()).cancelSetPointOverride(anyString());
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void missingDurationConfigurationUsesTwoHourOverride() {
        CommandFixture fixture = new CommandFixture();
        Instant earliest = Instant.now().plusSeconds(7200);
        fixture.command("TemporarySetPoint", new DecimalType(22));
        Instant latest = Instant.now().plusSeconds(7200);
        ArgumentCaptor<Instant> deadline = ArgumentCaptor.forClass(Instant.class);
        verify(fixture.bridge).setTemporarySetPoint(eq("zone"), eq(22.0), deadline.capture());
        assertFalse(deadline.getValue().isBefore(earliest));
        assertFalse(deadline.getValue().isAfter(latest));
        verify(fixture.bridge, never()).setTemporarySetPointUntilNextSwitchpoint(anyString(), anyDouble());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 1441 })
    void invalidConfiguredDurationMarksConfigurationOfflineAndDoesNotSendOverride(int duration) {
        CommandFixture fixture = new CommandFixture(duration);
        fixture.handler.initialize();
        ArgumentCaptor<ThingStatusInfo> status = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(fixture.callback).statusUpdated(eq(fixture.thing), status.capture());
        assertEquals(ThingStatus.OFFLINE, status.getValue().getStatus());
        assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, status.getValue().getStatusDetail());
        fixture.command("TemporarySetPoint", new DecimalType(22));
        verifyNoInteractions(fixture.bridge);
    }

    @Test
    void configuredTemporaryDurationIsSentAsAnAbsoluteDeadline() {
        CommandFixture fixture = new CommandFixture(30);
        Instant earliest = Instant.now().plusSeconds(1800);
        fixture.command("TemporarySetPoint", new DecimalType(22));
        Instant latest = Instant.now().plusSeconds(1800);
        ArgumentCaptor<Instant> deadline = ArgumentCaptor.forClass(Instant.class);
        verify(fixture.bridge).setTemporarySetPoint(eq("zone"), eq(22.0), deadline.capture());
        assertFalse(deadline.getValue().isBefore(earliest));
        assertFalse(deadline.getValue().isAfter(latest));
        verify(fixture.bridge, never()).setTemporarySetPointUntilNextSwitchpoint(anyString(), anyDouble());
    }

    @Test
    void ordinarySetPointPreservesExistingTemporaryDeadline() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("TemporaryOverride", fixture.nextSwitchpoint.toString());
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setTemporarySetPoint("zone", 23, fixture.nextSwitchpoint);
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
    }

    @Test
    void confirmedTemporaryExpiryUsesApiSecondPrecisionBeforeExternalCancellation() {
        CommandFixture fixture = new CommandFixture(30);
        fixture.command("TemporarySetPoint", new DecimalType(21));
        ArgumentCaptor<Instant> deadline = ArgumentCaptor.forClass(Instant.class);
        verify(fixture.bridge).setTemporarySetPoint(eq("zone"), eq(21.0), deadline.capture());
        fixture.update("TemporaryOverride", deadline.getValue().truncatedTo(ChronoUnit.SECONDS).toString());
        fixture.update("FollowSchedule", null);
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setPermanentSetPoint("zone", 23);
        verify(fixture.bridge, never()).setTemporarySetPoint(anyString(), anyDouble(), any());
    }

    @Test
    void rapidSetPointAfterAcceptedTemporaryCommandPreservesReturnedDeadline() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command("TemporarySetPoint", new DecimalType(22));
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setTemporarySetPointUntilNextSwitchpoint("zone", 22);
        verify(fixture.bridge).setTemporarySetPoint("zone", 23, fixture.nextSwitchpoint);
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void failedTemporaryCommandDoesNotChangeTheContextOfTheNextSetPoint() {
        CommandFixture fixture = new CommandFixture(0);
        when(fixture.bridge.setTemporarySetPointUntilNextSwitchpoint("zone", 22)).thenReturn(null);
        fixture.command("TemporarySetPoint", new DecimalType(22));
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setPermanentSetPoint("zone", 23);
        verify(fixture.bridge, never()).setTemporarySetPoint(anyString(), anyDouble(), any());
    }

    @Test
    void acceptedCancellationClearsTemporaryContextForTheNextSetPoint() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("TemporaryOverride", fixture.nextSwitchpoint.toString());
        fixture.command("SetPointStatus", new StringType("FollowSchedule"));
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).cancelSetPointOverride("zone");
        verify(fixture.bridge).setPermanentSetPoint("zone", 23);
        verify(fixture.bridge, never()).setTemporarySetPoint(anyString(), anyDouble(), any());
    }

    @Test
    void fullSystemResetClearsAcceptedTemporaryContextWithoutPublishingOptimisticState() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("TemporaryOverride", fixture.nextSwitchpoint.toString());
        fixture.command("TemporarySetPoint", new DecimalType(22));
        fixture.handler.resetCommandContext();
        fixture.handler.update(null, fixture.status("TemporaryOverride",
                fixture.nextSwitchpoint.atOffset(ZoneOffset.ofHours(2)).toString()));
        clearInvocations(fixture.callback);
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setPermanentSetPoint("zone", 23);
        verify(fixture.bridge, never()).setTemporarySetPoint(anyString(), anyDouble(), any());
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void failedCancellationRetainsTemporaryContextForTheNextSetPoint() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("TemporaryOverride", fixture.nextSwitchpoint.toString());
        when(fixture.bridge.cancelSetPointOverride("zone")).thenReturn(false);
        fixture.command("SetPointStatus", new StringType("FollowSchedule"));
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setTemporarySetPoint("zone", 23, fixture.nextSwitchpoint);
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
    }

    @Test
    void switchingToPermanentUsesLatestTargetAndClearsTemporaryContext() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("TemporaryOverride", fixture.nextSwitchpoint.toString());
        fixture.command("SetPointStatus", new StringType("PermanentOverride"));
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setPermanentSetPoint("zone", 21);
        verify(fixture.bridge).setPermanentSetPoint("zone", 23);
        verify(fixture.bridge, never()).setTemporarySetPoint(anyString(), anyDouble(), any());
    }

    @Test
    void switchingToTemporaryUsesTheLastCloudTarget() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("FollowSchedule", null);
        fixture.command("SetPointStatus", new StringType("TemporaryOverride"));
        verify(fixture.bridge).setTemporarySetPointUntilNextSwitchpoint("zone", 21);
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void expiredTemporaryDeadlineRestartsDefaultTemporaryOverride() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("TemporaryOverride", Instant.now().minusSeconds(3600).toString());
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setTemporarySetPointUntilNextSwitchpoint("zone", 23);
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
        verify(fixture.bridge, never()).setTemporarySetPoint(anyString(), anyDouble(), any());
    }

    @Test
    void malformedTemporaryDeadlineDoesNotTurnTheOverridePermanent() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("TemporaryOverride", "not-a-time");
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setTemporarySetPointUntilNextSwitchpoint("zone", 23);
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
    }

    @Test
    void delayedCloudReadbackDoesNotEraseAcceptedTemporaryCommandContext() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("FollowSchedule", null);
        fixture.command("TemporarySetPoint", new DecimalType(22));
        fixture.handler.update(null, fixture.status("FollowSchedule", null));
        verify(fixture.callback).stateUpdated(fixture.channel("SetPointStatus"), new StringType("FollowSchedule"));
        verify(fixture.callback).stateUpdated(fixture.channel("SetPoint"), new QuantityType<>(21, SIUnits.CELSIUS));
        fixture.command("SetPoint", new DecimalType(23));
        verify(fixture.bridge).setTemporarySetPoint("zone", 23, fixture.nextSwitchpoint);
        verify(fixture.bridge, never()).setPermanentSetPoint(anyString(), anyDouble());
    }

    @Test
    void unknownOverrideModeDoesNotSendACommand() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.update("FollowSchedule", null);
        fixture.command("SetPointStatus", new StringType("UnknownOverride"));
        verifyNoInteractions(fixture.bridge);
    }

    @ParameterizedTest
    @ValueSource(strings = { "SetPoint", "TemporarySetPoint", "SetPointStatus", "SetPointUntil", "Temperature" })
    void refreshReadsCloudStateWithoutSendingOverrideCommands(String channel) {
        CommandFixture fixture = new CommandFixture(0);
        fixture.command(channel, RefreshType.REFRESH);
        verify(fixture.bridge).requestRefresh();
        verifyNoMoreInteractions(fixture.bridge);
        verify(fixture.callback, never()).stateUpdated(any(), any());
    }

    @Test
    void validTemporaryUntilIsPublishedAsDateTime() {
        CommandFixture fixture = new CommandFixture(0);
        fixture.handler.update(null, fixture.status("TemporaryOverride",
                fixture.nextSwitchpoint.atOffset(ZoneOffset.ofHours(2)).toString()));
        ArgumentCaptor<State> state = ArgumentCaptor.forClass(State.class);
        verify(fixture.callback).stateUpdated(eq(fixture.channel("SetPointUntil")), state.capture());
        assertInstanceOf(DateTimeType.class, state.getValue());
        assertEquals(fixture.nextSwitchpoint, ((DateTimeType) state.getValue()).getInstant());
    }

    @Test
    void validPastTemporaryUntilRemainsVisibleUntilScheduleStateArrives() {
        CommandFixture fixture = new CommandFixture(0);
        Instant previousDeadline = Instant.now().minusSeconds(3600);
        fixture.handler.update(null, fixture.status("TemporaryOverride", previousDeadline.toString()));
        ArgumentCaptor<State> state = ArgumentCaptor.forClass(State.class);
        verify(fixture.callback).stateUpdated(eq(fixture.channel("SetPointUntil")), state.capture());
        assertInstanceOf(DateTimeType.class, state.getValue());
        assertEquals(previousDeadline, ((DateTimeType) state.getValue()).getInstant());
        clearInvocations(fixture.callback);
        fixture.handler.update(null, fixture.status("FollowSchedule", null));
        verify(fixture.callback).stateUpdated(fixture.channel("SetPointUntil"), UnDefType.UNDEF);
    }

    @ParameterizedTest
    @CsvSource({ "FollowSchedule,2030-01-01T12:00:00Z", "PermanentOverride,2030-01-01T12:00:00Z",
            "TemporaryOverride,not-a-time", "TemporaryOverride," })
    void expiryIsUndefinedOutsideAValidTemporaryOverride(String mode, @Nullable String until) {
        CommandFixture fixture = new CommandFixture(0);
        fixture.handler.update(null, fixture.status(mode, until));
        verify(fixture.callback).stateUpdated(fixture.channel("SetPointUntil"), UnDefType.UNDEF);
    }

    private static class CommandFixture {
        private final EvohomeAccountBridgeHandler bridge = mock(EvohomeAccountBridgeHandler.class);
        private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        private final Instant nextSwitchpoint = Instant.now().plusSeconds(3600).truncatedTo(ChronoUnit.SECONDS);
        private final Thing thing;
        private final EvohomeHeatingZoneHandler handler;

        private CommandFixture(int duration) {
            this(Map.of("id", "zone", "temporaryOverrideDuration", duration));
        }

        private CommandFixture() {
            this(Map.of("id", "zone"));
        }

        private CommandFixture(Map<String, Object> configuration) {
            thing = ThingBuilder.create(THING_TYPE_EVOHOME_HEATING_ZONE, "commands")
                    .withConfiguration(new Configuration(configuration)).build();
            handler = new EvohomeHeatingZoneHandler(thing) {
                @Override
                protected @Nullable EvohomeAccountBridgeHandler getEvohomeBridge() {
                    return bridge;
                }
            };
            handler.setCallback(callback);
            handler.initialize();
            when(bridge.setPermanentSetPoint(eq("zone"), anyDouble())).thenReturn(true);
            when(bridge.setTemporarySetPoint(eq("zone"), anyDouble(), any())).thenReturn(true);
            when(bridge.setTemporarySetPointUntilNextSwitchpoint(eq("zone"), anyDouble())).thenReturn(nextSwitchpoint);
            when(bridge.cancelSetPointOverride("zone")).thenReturn(true);
            doAnswer(invocation -> {
                boolean accepted = bridge.setPermanentSetPoint("zone", invocation.getArgument(1, Double.class));
                if (accepted) {
                    invocation.getArgument(2, Runnable.class).run();
                }
                return accepted;
            }).when(bridge).setPermanentSetPoint(eq("zone"), anyDouble(), any(Runnable.class));
            doAnswer(invocation -> {
                boolean accepted = bridge.setTemporarySetPoint("zone", invocation.getArgument(1, Double.class),
                        invocation.getArgument(2, Instant.class));
                if (accepted) {
                    invocation.getArgument(3, Runnable.class).run();
                }
                return accepted;
            }).when(bridge).setTemporarySetPoint(eq("zone"), anyDouble(), any(Instant.class), any(Runnable.class));
            doAnswer(invocation -> {
                Instant until = bridge.setTemporarySetPointUntilNextSwitchpoint("zone",
                        invocation.getArgument(1, Double.class));
                if (until != null) {
                    invocation.<Consumer<Instant>> getArgument(2).accept(until);
                }
                return until;
            }).when(bridge).setTemporarySetPointUntilNextSwitchpoint(eq("zone"), anyDouble(), any());
            doAnswer(invocation -> {
                boolean accepted = bridge.cancelSetPointOverride("zone");
                if (accepted) {
                    invocation.getArgument(1, Runnable.class).run();
                }
                return accepted;
            }).when(bridge).cancelSetPointOverride(eq("zone"), any(Runnable.class));
            clearInvocations(callback, bridge);
        }

        private ChannelUID channel(String id) {
            return new ChannelUID(thing.getUID(), id);
        }

        private void command(String channel, Command command) {
            handler.handleCommand(channel(channel), command);
        }

        private ZoneStatus status(String mode, @Nullable String until) {
            return Objects.requireNonNull(new Gson().fromJson("""
                    {"zoneId":"zone","activeFaults":[],
                    "temperatureStatus":{"isAvailable":true,"temperature":19},
                    "setpointStatus":{"targetHeatTemperature":21,"setpointMode":"%s","until":%s}}
                    """.formatted(mode, until == null ? "null" : '"' + until + '"'), ZoneStatus.class));
        }

        private void update(String mode, @Nullable String until) {
            handler.update(null, status(mode, until));
            clearInvocations(callback, bridge);
        }
    }
}
