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
package org.openhab.binding.eyeonwater.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.eyeonwater.internal.EyeOnWaterBindingConstants.*;

import java.time.ZoneId;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.eyeonwater.internal.api.EyeOnWaterClient.EyeOnWaterMeterData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Unit tests for {@link EyeOnWaterMeterHandler}.
 *
 * @author Richard Koshak - Initial contribution
 */
@NonNullByDefault
class EyeOnWaterMeterHandlerTest {

    private final TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
    private final Thing thing = mock(Thing.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final Bridge bridge = mock(Bridge.class);
    private final EyeOnWaterBridgeHandler bridgeHandler = mock(EyeOnWaterBridgeHandler.class);

    private final ThingUID bridgeUid = new ThingUID("eyeonwater", "bridge", "test-bridge");
    private final ThingUID meterUid = new ThingUID(THING_TYPE_METER, "test-meter");

    private EyeOnWaterMeterHandler handler = new EyeOnWaterMeterHandler(thing, timeZoneProvider);

    @BeforeEach
    void setUp() {
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("UTC"));
        when(thing.getUID()).thenReturn(meterUid);
        when(thing.getBridgeUID()).thenReturn(bridgeUid);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("meterUuid", "uuid-1", "meterId", "id-1")));
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        when(callback.getBridge(bridgeUid)).thenReturn(bridge);

        handler = new EyeOnWaterMeterHandler(thing, timeZoneProvider);
        handler.setCallback(callback);
        handler.initialize();
    }

    @Test
    void testInitializeSetsMeterUuidAndId() {
        assertEquals("uuid-1", handler.getMeterUuid());
        assertEquals("id-1", handler.getMeterId());
        verify(bridgeHandler).registerMeterHandler(handler);
    }

    @Test
    void testUpdateStateWithMatchingUuidAndIdUpdatesChannels() {
        EyeOnWaterMeterData data = new EyeOnWaterMeterData("uuid-1", "id-1");
        data.setReadingValue(1234.5);
        data.setReadingUnit("GAL");
        data.setLeakAlert(true);
        data.setLowBatteryAlert(false);
        data.setReverseFlowAlert(false);
        data.setReadTime("2026-10-16T12:00:00Z");

        handler.updateState(data);

        verify(callback).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback).stateUpdated(eq(new ChannelUID(meterUid, CHANNEL_READING)), any(QuantityType.class));
        verify(callback).stateUpdated(new ChannelUID(meterUid, CHANNEL_LEAK_ALERT), OnOffType.ON);
        verify(callback).stateUpdated(new ChannelUID(meterUid, CHANNEL_LOW_BATTERY), OnOffType.OFF);
        verify(callback).stateUpdated(new ChannelUID(meterUid, CHANNEL_REVERSE_FLOW), OnOffType.OFF);
        verify(callback).stateUpdated(eq(new ChannelUID(meterUid, CHANNEL_LAST_READ_TIME)), any(DateTimeType.class));
    }

    @Test
    void testUpdateStateWithMismatchedUuidDiscardsData() {
        EyeOnWaterMeterData staleData = new EyeOnWaterMeterData("old-uuid", "id-1");
        staleData.setReadingValue(999.0);
        staleData.setReadingUnit("GAL");

        handler.updateState(staleData);

        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any());
    }

    @Test
    void testUpdateStateWithMismatchedIdDiscardsData() {
        EyeOnWaterMeterData staleData = new EyeOnWaterMeterData("uuid-1", "old-id");
        staleData.setReadingValue(999.0);
        staleData.setReadingUnit("GAL");

        handler.updateState(staleData);

        verify(callback, never()).statusUpdated(eq(thing), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).stateUpdated(any(ChannelUID.class), any());
    }
}
