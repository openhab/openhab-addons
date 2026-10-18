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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Entry;
import org.openhab.core.types.TimeSeries.Policy;

/**
 * Basic unit tests for {@link RoundStateProfile}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class RoundStateProfileTest {

    @BeforeEach
    public void setup() {
        // initialize parser with ImperialUnits, otherwise units like °F are unknown
        @SuppressWarnings("unused")
        Unit<Temperature> fahrenheit = ImperialUnits.FAHRENHEIT;
    }

    @Test
    public void testParsingParameters() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 4, 2, "NOT_SUPPORTED");

        assertThat(roundProfile.precision, is(4));
        assertThat(roundProfile.scale, is(2));
        assertThat(roundProfile.roundingMode, is(RoundingMode.HALF_UP));
    }

    @Test
    public void testDecimalTypeOnCommandFromItem() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 2);

        Command cmd = new DecimalType(23.333);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(23.33));
    }

    @Test
    public void testDecimalTypeOnCommandFromItemForPrecision() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 1, null);

        Command cmd = new DecimalType(24.444);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(20.0));
    }

    @Test
    public void testDecimalTypeOnCommandFromItemWithNegativeScale() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, -2);

        Command cmd = new DecimalType(1234.333);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(1200.0));
    }

    @Test
    public void testDecimalTypeOnCommandFromItemWithCeiling() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 0, RoundingMode.CEILING.name());

        Command cmd = new DecimalType(23.3);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(24.0));
    }

    @Test
    public void testDecimalTypeOnCommandFromItemWithFloor() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 0, RoundingMode.FLOOR.name());

        Command cmd = new DecimalType(23.6);
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(23.0));
    }

    @Test
    public void testQuantityTypeOnCommandFromItem() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 1);

        Command cmd = new QuantityType<Temperature>("23.333 °C");
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        Command result = capture.getValue();
        @SuppressWarnings("unchecked")
        QuantityType<Temperature> qtResult = (QuantityType<Temperature>) result;
        assertThat(qtResult.doubleValue(), is(23.3));
        assertThat(qtResult.getUnit(), is(SIUnits.CELSIUS));
    }

    @Test
    public void testDateTimeTypeOnCommandFromItemForHours() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 1);

        Command cmd = new DateTimeType(ZonedDateTime.parse("2026-04-12T10:31:30+02:00"));
        roundProfile.onCommandFromItem(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).handleCommand(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        assertEquals(Instant.parse("2026-04-12T09:00:00Z"), result.getInstant());
    }

    @Test
    public void testDateTimeTypeOnStateUpdateFromHandlerForMinutes() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 2);

        State state = new DateTimeType(ZonedDateTime.parse("2026-04-12T10:29:31+02:00"));
        roundProfile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        assertEquals(Instant.parse("2026-04-12T08:30:00Z"), result.getInstant());
    }

    @Test
    public void testDateTimeTypeOnCommandFromHandlerForDays() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 0);

        Command cmd = new DateTimeType(ZonedDateTime.parse("2026-04-12T15:00:00+02:00"));
        roundProfile.onCommandFromHandler(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).sendCommand(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        assertEquals(Instant.parse("2026-04-13T00:00:00Z"), result.getInstant());
    }

    @Test
    public void testDateTimeTypeOnStateUpdateFromHandlerForMilliseconds() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 4);

        State state = new DateTimeType(ZonedDateTime.parse("2026-04-12T10:29:30.123600+02:00"));
        roundProfile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        assertEquals(Instant.parse("2026-04-12T08:29:30.124Z"), result.getInstant());
    }

    @Test
    public void testDateTimeTypeDefaultsToSecondsWhenScaleMissing() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, null);

        State state = new DateTimeType(ZonedDateTime.parse("2026-04-12T10:29:30.600+02:00"));
        roundProfile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        assertEquals(Instant.parse("2026-04-12T08:29:31Z"), result.getInstant());
    }

    @Test
    public void testDateTimeTypeIgnoresPrecision() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, 5, 3);

        State state = new DateTimeType(ZonedDateTime.parse("2026-04-12T10:29:30.600+02:00"));
        roundProfile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        assertEquals(Instant.parse("2026-04-12T08:29:31Z"), result.getInstant());
    }

    @Test
    public void testDecimalTypeOnStateUpdateFromHandler() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 1);

        State state = new DecimalType(23.333);
        roundProfile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        State result = capture.getValue();
        DecimalType dtResult = (DecimalType) result;
        assertThat(dtResult.doubleValue(), is(23.3));
    }

    @Test
    public void testQuantityTypeOnCommandFromHandler() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile offsetProfile = createProfile(callback, null, 1);

        Command cmd = new QuantityType<>("23.333 °C");
        offsetProfile.onCommandFromHandler(cmd);

        ArgumentCaptor<Command> capture = ArgumentCaptor.forClass(Command.class);
        verify(callback, times(1)).sendCommand(capture.capture());

        Command result = capture.getValue();
        QuantityType<?> qtResult = (QuantityType<?>) result;
        assertThat(qtResult.doubleValue(), is(23.3));
        assertThat(qtResult.getUnit(), is(SIUnits.CELSIUS));
    }

    @Test
    public void testTimeSeriesFromHandler() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 1);

        TimeSeries ts = new TimeSeries(Policy.ADD);
        Instant now = Instant.now();
        ts.add(now, new DecimalType(23.333));

        roundProfile.onTimeSeriesFromHandler(ts);

        ArgumentCaptor<TimeSeries> capture = ArgumentCaptor.forClass(TimeSeries.class);
        verify(callback, times(1)).sendTimeSeries(capture.capture());

        TimeSeries result = capture.getValue();
        assertEquals(ts.getStates().count(), result.getStates().count());
        Entry entry = result.getStates().findFirst().get();
        assertNotNull(entry);
        assertEquals(now, entry.timestamp());
        DecimalType dtResult = (DecimalType) entry.state();
        assertThat(dtResult.doubleValue(), is(23.3));
    }

    @Test
    public void testDateTimeTimeSeriesFromHandler() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile roundProfile = createProfile(callback, null, 3);

        TimeSeries ts = new TimeSeries(Policy.ADD);
        Instant now = Instant.now();
        ts.add(now, new DateTimeType(ZonedDateTime.parse("2026-04-12T10:29:30.600+02:00")));

        roundProfile.onTimeSeriesFromHandler(ts);

        ArgumentCaptor<TimeSeries> capture = ArgumentCaptor.forClass(TimeSeries.class);
        verify(callback, times(1)).sendTimeSeries(capture.capture());

        TimeSeries result = capture.getValue();
        assertEquals(ts.getStates().count(), result.getStates().count());
        Entry entry = result.getStates().findFirst().get();
        assertNotNull(entry);
        assertEquals(now, entry.timestamp());
        DateTimeType dtResult = (DateTimeType) entry.state();
        assertEquals(Instant.parse("2026-04-12T08:29:31Z"), dtResult.getInstant());
    }

    @Test
    public void testDateTimeFloorToMinutes() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile profile = createProfile(callback, null, 2, RoundingMode.FLOOR.name());

        ZonedDateTime input = ZonedDateTime.of(2025, 9, 27, 14, 16, 28, 500_000_000, ZoneId.of("Europe/Berlin"));
        State state = new DateTimeType(input);
        profile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        ZonedDateTime expected = ZonedDateTime.of(2025, 9, 27, 14, 16, 0, 0, ZoneId.of("Europe/Berlin"));
        assertThat(result.getInstant(), is(expected.toInstant()));
    }

    @Test
    public void testDateTimeCeilingToHours() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile profile = createProfile(callback, null, 1, RoundingMode.CEILING.name());

        ZonedDateTime input = ZonedDateTime.of(2025, 9, 27, 14, 16, 28, 0, ZoneId.of("Europe/Berlin"));
        State state = new DateTimeType(input);
        profile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        ZonedDateTime expected = ZonedDateTime.of(2025, 9, 27, 15, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        assertThat(result.getInstant(), is(expected.toInstant()));
    }

    @Test
    public void testDateTimeCeilingToHoursWhenAlreadyOnBoundary() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile profile = createProfile(callback, null, 1, RoundingMode.CEILING.name());

        ZonedDateTime input = ZonedDateTime.of(2025, 9, 27, 14, 0, 0, 0, ZoneId.of("Europe/Berlin"));
        State state = new DateTimeType(input);
        profile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        assertThat(result.getInstant(), is(input.toInstant()));
    }

    @Test
    public void testDateTimeHalfUpToMinutesRoundsUp() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile profile = createProfile(callback, null, 2, RoundingMode.HALF_UP.name());

        ZonedDateTime input = ZonedDateTime.of(2025, 9, 27, 14, 16, 30, 0, ZoneId.of("UTC"));
        State state = new DateTimeType(input);
        profile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        ZonedDateTime expected = ZonedDateTime.of(2025, 9, 27, 14, 17, 0, 0, ZoneId.of("UTC"));
        assertThat(result.getInstant(), is(expected.toInstant()));
    }

    @Test
    public void testDateTimeHalfUpToMinutesRoundsDown() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile profile = createProfile(callback, null, 2, RoundingMode.HALF_UP.name());

        ZonedDateTime input = ZonedDateTime.of(2025, 9, 27, 14, 16, 29, 999_000_000, ZoneId.of("UTC"));
        State state = new DateTimeType(input);
        profile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        DateTimeType result = (DateTimeType) capture.getValue();
        ZonedDateTime expected = ZonedDateTime.of(2025, 9, 27, 14, 16, 0, 0, ZoneId.of("UTC"));
        assertThat(result.getInstant(), is(expected.toInstant()));
    }

    @Test
    public void testDateTimeInvalidScaleReturnsOriginal() {
        ProfileCallback callback = mock(ProfileCallback.class);
        RoundStateProfile profile = createProfile(callback, null, 5);

        ZonedDateTime input = ZonedDateTime.of(2025, 9, 27, 14, 16, 28, 0, ZoneId.of("UTC"));
        State state = new DateTimeType(input);
        profile.onStateUpdateFromHandler(state);

        ArgumentCaptor<State> capture = ArgumentCaptor.forClass(State.class);
        verify(callback, times(1)).sendUpdate(capture.capture());

        assertThat(capture.getValue(), is(state));
    }

    private RoundStateProfile createProfile(ProfileCallback callback, @Nullable Integer precision,
            @Nullable Integer scale) {
        return createProfile(callback, precision, scale, null);
    }

    private RoundStateProfile createProfile(ProfileCallback callback, @Nullable Integer precision,
            @Nullable Integer scale, @Nullable String mode) {
        ProfileContext context = mock(ProfileContext.class);
        Configuration config = new Configuration();

        config.put(RoundStateProfile.PARAM_PRECISION, precision);
        config.put(RoundStateProfile.PARAM_SCALE, scale);
        config.put(RoundStateProfile.PARAM_MODE, mode == null ? RoundingMode.HALF_UP.name() : mode);
        when(context.getConfiguration()).thenReturn(config);

        return new RoundStateProfile(callback, context);
    }
}
