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
package org.openhab.binding.meteostick.internal.handler;

import static java.util.Objects.requireNonNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.meteostick.internal.MeteostickBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.*;
import static org.openhab.core.library.unit.Units.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests the packet handling of the {@link MeteostickSensorHandler}.
 *
 * @author Jens Erat - Initial contribution
 */
@NonNullByDefault
public class MeteostickSensorHandlerTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_DAVIS, "test");

    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private @Nullable MeteostickSensorHandler handler;

    @BeforeEach
    public void setUp() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(THING_UID);

        handler = new MeteostickSensorHandler(thing, mock(CronScheduler.class));
        handler.setCallback(callback);
    }

    @AfterEach
    public void tearDown() {
        requireNonNull(handler).dispose();
    }

    @Test
    public void testRainPacketIsPublished() {
        requireNonNull(handler).onDataReceived(new String[] { "R", "1", "44", "-66" });

        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_RAIN_RAW), new DecimalType(44));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_SIGNAL_STRENGTH), new DecimalType(3));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_LOW_BATTERY), OnOffType.OFF);
    }

    @Test
    public void testWindPacketIsPublished() {
        requireNonNull(handler).onDataReceived(new String[] { "W", "1", "0.44", "241", "-67" });

        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_WIND_SPEED),
                new QuantityType<>(0.44, METRE_PER_SECOND));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_WIND_DIRECTION),
                new QuantityType<>(241, DEGREE_ANGLE));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_SIGNAL_STRENGTH), new DecimalType(3));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_LOW_BATTERY), OnOffType.OFF);
    }

    @Test
    public void testTemperaturePacketIsPublished() {
        requireNonNull(handler).onDataReceived(new String[] { "T", "3", "20.8", "36", "-53", "L" });

        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_OUTDOOR_TEMPERATURE),
                new QuantityType<>(20.8, CELSIUS));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_HUMIDITY), new DecimalType(36.0));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_SIGNAL_STRENGTH), new DecimalType(4));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_LOW_BATTERY), OnOffType.ON);
    }

    @Test
    public void testPowerPacketIsPublished() {
        requireNonNull(handler).onDataReceived(new String[] { "P", "1", "21.1", "-68" });

        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_SOLAR_POWER), new DecimalType(21.1));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_SIGNAL_STRENGTH), new DecimalType(3));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_LOW_BATTERY), OnOffType.OFF);
    }

    @Test
    public void testShortRainPacketIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "R", "1", "44" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testShortWindPacketIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "W", "1", "0.44", "241" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testShortTemperaturePacketIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "T", "3", "20.8", "36" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testShortPowerPacketIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "P", "1", "21.1" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testEmptyPacketIsRejected() {
        requireNonNull(handler).onDataReceived(new String[0]);

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testNonNumericWindPacketIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "W", "1", "abc", "241", "-67" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleRainCounterIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "R", "1", "300", "-66" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleWindSpeedIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "W", "1", "-0.44", "241", "-67" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleWindDirectionIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "W", "1", "0.44", "361", "-67" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleTemperatureIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "T", "3", "200", "36", "-53" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleHumidityIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "T", "3", "20.8", "101", "-53" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleSolarPowerIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "P", "1", "101", "-68" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testImplausibleSignalStrengthIsRejected() {
        requireNonNull(handler).onDataReceived(new String[] { "W", "1", "0.44", "241", "5" });

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void testSensorValuesAtRangeBoundsArePublished() {
        requireNonNull(handler).onDataReceived(new String[] { "W", "1", "200", "360", "0" });

        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_WIND_SPEED),
                new QuantityType<>(200, METRE_PER_SECOND));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_WIND_DIRECTION),
                new QuantityType<>(360, DEGREE_ANGLE));
        verify(callback).stateUpdated(new ChannelUID(THING_UID, CHANNEL_SIGNAL_STRENGTH), new DecimalType(4));
    }
}
