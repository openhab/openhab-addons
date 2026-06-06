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
package org.openhab.binding.evcc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_SUBTYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * The {@link EvccStatisticsHandlerTest} is responsible for testing the EvccSiteHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccForecastHandlerTest extends AbstractThingHandlerTestClass<EvccForecastHandler> {

    private boolean updateStateCalled = false;
    private int updateStateCounter = 0;
    private boolean sendTimeSeriesCalled = false;
    private long timeSeriesCount = 0;

    @Override
    protected EvccForecastHandler createHandler() {
        return new EvccForecastHandler(thing, channelTypeRegistry) {

            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
                lastThingStatus = status;
                lastThingStatusDetail = detail;
            }

            @Override
            protected void updateStatus(ThingStatus status) {
                lastThingStatus = status;
            }

            @Override
            public void logUnknownChannelXmlAsync(String key, String itemType) {
            }

            @Nullable
            @Override
            protected Bridge getBridge() {
                return null;
            }

            @Override
            public void updateState(ChannelUID uid, State state) {
                updateStateCalled = true;
                updateStateCounter++;
            }

            @Override
            protected void sendTimeSeries(ChannelUID channelUID, TimeSeries timeSeries) {
                sendTimeSeriesCalled = true;
                timeSeriesCount = timeSeries.getStates().count();
            }

            @Override
            protected boolean isLinked(ChannelUID channelUID) {
                return true;
            }
        };
    }

    @SuppressWarnings("null")
    @Nested
    public class TestPrepareApiResponseForChannelStateUpdate {

        @BeforeEach
        @SuppressWarnings("null")
        public void setup() {
            when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
            when(thing.getProperties()).thenReturn(Map.of("type", "forecast", "subType", "solar"));
            Configuration configuration = mock(Configuration.class);
            when(configuration.get(PROPERTY_SUBTYPE)).thenReturn("solar");
            when(thing.getConfiguration()).thenReturn(configuration);
            when(thing.getChannels()).thenReturn(new ArrayList<>());
            handler = spy(createHandler());
            EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
            handler.bridgeHandler = bridgeHandler;
            when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);
        }

        @Test
        public void handlerIsInitialized() {
            handler.isInitialized = true;

            handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
            assertSame(ThingStatus.ONLINE, lastThingStatus);
        }

        @Test
        public void handlerIsNotInitialized() {
            handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
            assertSame(ThingStatus.OFFLINE, lastThingStatus);
        }
    }

    @SuppressWarnings("null")
    @Nested
    public class TestSubTypes {

        public void setup(String forecastSubtype) {
            when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
            when(thing.getProperties()).thenReturn(Map.of("type", "forecast", "subType", forecastSubtype));
            Configuration configuration = mock(Configuration.class);
            when(configuration.get(PROPERTY_SUBTYPE)).thenReturn(forecastSubtype);
            when(thing.getConfiguration()).thenReturn(configuration);
            if ("solar".equals(forecastSubtype)) {
                Channel scaleChannel = mock(Channel.class);
                ChannelUID uid = new ChannelUID(thing.getUID(), "forecast-scale");
                when(scaleChannel.getUID()).thenReturn(uid);
                Channel todayChannel = mock(Channel.class);
                uid = new ChannelUID(thing.getUID(), "forecast-today");
                when(todayChannel.getUID()).thenReturn(uid);
                Channel tomorrowChannel = mock(Channel.class);
                uid = new ChannelUID(thing.getUID(), "forecast-tomorrow");
                when(tomorrowChannel.getUID()).thenReturn(uid);
                Channel dayChannel = mock(Channel.class);
                uid = new ChannelUID(thing.getUID(), "forecast-day-after-tomorrow");
                when(dayChannel.getUID()).thenReturn(uid);
                List<Channel> channels = new ArrayList<>(
                        List.of(scaleChannel, todayChannel, tomorrowChannel, dayChannel));
                when(thing.getChannels()).thenReturn(channels);
            } else {
                when(thing.getChannels()).thenReturn(new ArrayList<>());
            }
            handler = spy(createHandler());
            EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
            handler.bridgeHandler = bridgeHandler;
            when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse.deepCopy());
        }

        @Test
        public void co2ForecastSubtype() {
            setup("co2");
            handler.isInitialized = true;
            handler.prepareApiResponseForChannelStateUpdate(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(1, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(72, timeSeriesCount);
        }

        @Test
        public void feedinForecastSubtype() {
            setup("feedin");
            handler.isInitialized = true;
            handler.prepareApiResponseForChannelStateUpdate(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(1, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(24, timeSeriesCount);
        }

        @Test
        public void gridForecastSubtype() {
            setup("grid");
            handler.isInitialized = true;
            handler.prepareApiResponseForChannelStateUpdate(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(1, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(24, timeSeriesCount);
        }

        @Test
        public void solarForecastSubtype() {
            setup("solar");
            handler.isInitialized = true;
            handler.prepareApiResponseForChannelStateUpdate(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(6, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(72, timeSeriesCount);
        }
    }
}
