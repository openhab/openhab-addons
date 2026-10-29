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
package org.openhab.binding.ondilo.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ondilo.internal.OndiloBindingConstants.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ondilo.internal.dto.LastMeasure;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link OndiloHandler#updateLastMeasuresChannels(LastMeasure[])}.
 *
 * @author Michael Weger - Initial contribution
 */
@SuppressWarnings({ "null" })
@NonNullByDefault
public class OndiloHandlerTest {

    private static final DateTimeFormatter VALUE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    private @Nullable OndiloHandler handler;
    private @Nullable ThingHandlerCallback callbackMock;
    private final ThingUID thingUID = new ThingUID(THING_TYPE_ONDILO, "12345");

    @BeforeEach
    public void setUp() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("id", 12345)));

        LocaleProvider localeProvider = mock(LocaleProvider.class);
        handler = new OndiloHandler(thing, localeProvider);

        callbackMock = mock(ThingHandlerCallback.class);
        handler.setCallback(callbackMock);
    }

    private static LastMeasure createMeasure(String dataType, double value, String valueTime, boolean isValid,
            @Nullable String exclusionReason) {
        LastMeasure measure = new LastMeasure();
        measure.dataType = dataType;
        measure.value = value;
        measure.valueTime = valueTime;
        measure.isValid = isValid;
        measure.exclusionReason = exclusionReason;
        return measure;
    }

    @Test
    public void validMeasurementUpdatesChannel() {
        LastMeasure[] measures = { createMeasure("temperature", 25.5, "2025-07-13 04:33:39", true, null) };

        handler.updateLastMeasuresChannels(measures);

        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_TEMPERATURE),
                new QuantityType<>(25.5, SIUnits.CELSIUS));
    }

    @Test
    public void invalidMeasurementIsIgnoredButStillCountsForSchedulingTime() {
        String valueTime = "2025-07-13 04:30:00";
        LastMeasure[] measures = { createMeasure("ph", 99.9, valueTime, false, "OUT_OF_RANGE") };

        Instant earliestValueTime = Objects.requireNonNull(handler.updateLastMeasuresChannels(measures));

        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_PH)), any(State.class));
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_PH_TREND)), any(State.class));
        assertEquals(Instant.from(VALUE_TIME_FORMATTER.parse(valueTime)), earliestValueTime);
        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_VALUE_TIME),
                new DateTimeType(earliestValueTime));
    }

    @Test
    public void refreshRetainsLastValidMeasurementAndTrendAfterInvalidResponse() {
        handler.updateLastMeasuresChannels(
                new LastMeasure[] { createMeasure("temperature", 24.0, "2025-07-13 04:00:00", true, null) });
        handler.updateLastMeasuresChannels(
                new LastMeasure[] { createMeasure("temperature", 25.5, "2025-07-13 04:15:00", true, null) });
        handler.updateLastMeasuresChannels(
                new LastMeasure[] { createMeasure("temperature", 99.9, "2025-07-13 04:30:00", false, "OUT_OF_RANGE") });
        clearInvocations(callbackMock);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_TEMPERATURE), RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_TEMPERATURE),
                new QuantityType<>(25.5, SIUnits.CELSIUS));
        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_TEMPERATURE_TREND),
                new QuantityType<>(1.5, SIUnits.CELSIUS));
        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_VALUE_TIME),
                new DateTimeType(Instant.from(VALUE_TIME_FORMATTER.parse("2025-07-13 04:30:00"))));

        clearInvocations(callbackMock);
        handler.updateLastMeasuresChannels(
                new LastMeasure[] { createMeasure("temperature", 26.0, "2025-07-13 04:45:00", true, null) });

        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_TEMPERATURE_TREND),
                new QuantityType<>(0.5, SIUnits.CELSIUS));
    }

    @Test
    public void refreshRetainsInvalidChannelAlongsideUpdatedValidChannel() {
        handler.updateLastMeasuresChannels(
                new LastMeasure[] { createMeasure("temperature", 25.5, "2025-07-13 04:00:00", true, null),
                        createMeasure("ph", 7.0, "2025-07-13 04:00:00", true, null) });
        handler.updateLastMeasuresChannels(
                new LastMeasure[] { createMeasure("temperature", 99.9, "2025-07-13 04:15:00", false, "OUT_OF_RANGE"),
                        createMeasure("ph", 7.2, "2025-07-13 04:15:00", true, null) });
        clearInvocations(callbackMock);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_TEMPERATURE), RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_TEMPERATURE),
                new QuantityType<>(25.5, SIUnits.CELSIUS));
        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_PH), new DecimalType(7.2));
    }

    @Test
    public void refreshDoesNotRestoreMeasurementAfterClearing() {
        handler.updateLastMeasuresChannels(
                new LastMeasure[] { createMeasure("temperature", 25.5, "2025-07-13 04:00:00", true, null) });
        handler.clearLastMeasuresChannels();
        clearInvocations(callbackMock);

        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_TEMPERATURE), RefreshType.REFRESH);

        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_TEMPERATURE), UnDefType.UNDEF);
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_TEMPERATURE)),
                eq(new QuantityType<>(25.5, SIUnits.CELSIUS)));
    }

    @Test
    public void invalidMeasurementDoesNotSuppressValidMeasurementInSameResponse() {
        LastMeasure[] measures = { createMeasure("temperature", 25.5, "2025-07-13 04:33:39", true, null),
                createMeasure("ph", 99.9, "2025-07-13 04:30:00", false, "OUT_OF_RANGE") };

        Instant earliestValueTime = Objects.requireNonNull(handler.updateLastMeasuresChannels(measures));

        verify(callbackMock).stateUpdated(new ChannelUID(thingUID, CHANNEL_TEMPERATURE),
                new QuantityType<>(25.5, SIUnits.CELSIUS));
        verify(callbackMock, never()).stateUpdated(eq(new ChannelUID(thingUID, CHANNEL_PH)), any(State.class));
        // The invalid ph measure is the earliest of the two and must still drive the scheduling time.
        assertEquals(Instant.from(VALUE_TIME_FORMATTER.parse("2025-07-13 04:30:00")), earliestValueTime);
    }
}
