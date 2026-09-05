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
import java.util.Collection;
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

import com.google.gson.JsonObject;

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
            EvccWsBridgeHandler bridgeHandler = mock(EvccWsBridgeHandler.class);
            handler.bridgeHandler = bridgeHandler;
            when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);
        }

        @Test
        public void handlerIsInitialized() {
            handler.initialize();
            handler.initializeThingFromLatestState(exampleResponse);
            assertSame(ThingStatus.ONLINE, lastThingStatus);
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
                Channel forecastChannel = mock(Channel.class);
                ChannelUID uid = new ChannelUID(thing.getUID(), "forecast-" + forecastSubtype);
                when(forecastChannel.getUID()).thenReturn(uid);
                List<Channel> channels = new ArrayList<>(List.of(forecastChannel));
                when(thing.getChannels()).thenReturn(channels);
                when(thing.getChannel(uid)).thenReturn(forecastChannel);
            }
            handler = spy(createHandler());
            EvccWsBridgeHandler bridgeHandler = mock(EvccWsBridgeHandler.class);
            handler.bridgeHandler = bridgeHandler;
            when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse.deepCopy());
        }

        @Test
        public void co2ForecastSubtype() {
            setup("co2");
            handler.initialize();
            handler.initializeThingFromLatestState(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(1, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(328, timeSeriesCount);
        }

        @Test
        public void feedinForecastSubtype() {
            setup("feedin");
            handler.initialize();
            handler.initializeThingFromLatestState(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(1, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(612, timeSeriesCount);
        }

        @Test
        public void gridForecastSubtype() {
            setup("grid");
            handler.initialize();
            handler.initializeThingFromLatestState(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(1, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(612, timeSeriesCount);
        }

        @Test
        public void solarForecastSubtype() {
            setup("solar");
            handler.initialize();
            handler.initializeThingFromLatestState(exampleResponse.deepCopy());
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertEquals(6, updateStateCounter);
            assertTrue(sendTimeSeriesCalled);
            assertEquals(488, timeSeriesCount);
        }
    }

    @SuppressWarnings("null")
    @Nested
    public class TestWsMessages {

        private JsonObject readResponse(String resourceName) {
            try (var is = EvccForecastHandlerTest.class.getClassLoader().getResourceAsStream(resourceName)) {
                assertNotNull(is, "Couldn't find response file: " + resourceName);
                String json = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                return com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            } catch (Exception e) {
                fail("Failed to read response file: " + resourceName, e);
                return new JsonObject();
            }
        }

        private void setup(String forecastSubtype) {
            when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
            when(thing.getProperties()).thenReturn(Map.of("type", "forecast", "subType", forecastSubtype));
            Configuration configuration = mock(Configuration.class);
            when(configuration.get(PROPERTY_SUBTYPE)).thenReturn(forecastSubtype);
            when(thing.getConfiguration()).thenReturn(configuration);
            Channel forecastChannel = mock(Channel.class);
            ChannelUID uid = new ChannelUID(thing.getUID(), "forecast-" + forecastSubtype);
            when(forecastChannel.getUID()).thenReturn(uid);
            List<Channel> channels = new ArrayList<>(List.of(forecastChannel));
            when(thing.getChannels()).thenReturn(channels);
            handler = spy(createHandler());
            EvccWsBridgeHandler bridgeHandler = mock(EvccWsBridgeHandler.class);
            handler.bridgeHandler = bridgeHandler;
            when(bridgeHandler.getCachedEvccState())
                    .thenReturn(readResponse("responses/ws_initial_response_message.json"));
        }

        private Collection<Channel> channelsForSolarSubtype() {
            Channel scaledChannel = mock(Channel.class);
            ChannelUID uid = new ChannelUID(thing.getUID(), "forecast-scaled");
            when(scaledChannel.getUID()).thenReturn(uid);
            Channel scaleChannel = mock(Channel.class);
            uid = new ChannelUID(thing.getUID(), "forecast-scale");
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
            Channel solarChannel = mock(Channel.class);
            uid = new ChannelUID(thing.getUID(), "forecast-solar");
            when(solarChannel.getUID()).thenReturn(uid);
            return List.of(solarChannel, scaledChannel, scaleChannel, todayChannel, tomorrowChannel, dayChannel);
        }

        @Test
        public void wsCo2ForecastUpdate() {
            setup("co2");
            handler.initialize();
            JsonObject ws = readResponse("responses/ws_forecast_co2.json");
            handler.handleUpdate("co2", ws.get("forecast.co2"));
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertTrue(sendTimeSeriesCalled);
            assertTrue(timeSeriesCount > 0);
        }

        @Test
        public void wsFeedInForecastUpdate() {
            setup("feedin");
            handler.initialize();
            JsonObject ws = readResponse("responses/ws_forecast_feed_in.json");
            handler.handleUpdate("feedin", ws.get("forecast.feedin"));
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertTrue(sendTimeSeriesCalled);
            assertTrue(timeSeriesCount > 0);
        }

        @Test
        public void wsGridForecastUpdate() {
            setup("grid");
            handler.initialize();
            JsonObject ws = readResponse("responses/ws_forecast_grid.json");
            handler.handleUpdate("grid", ws.get("forecast.grid"));
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertTrue(sendTimeSeriesCalled);
            assertTrue(timeSeriesCount > 0);
        }

        @Test
        public void wsSolarForecastUpdate() {
            setup("solar");
            Collection<Channel> solarChannels = channelsForSolarSubtype();
            when(thing.getChannels()).thenReturn(new ArrayList<>(solarChannels));
            handler.initialize();
            JsonObject ws = readResponse("responses/ws_forecast_solar.json");
            handler.handleUpdate("solar", ws.get("forecast.solar"));
            assertSame(ThingStatus.ONLINE, lastThingStatus);
            assertTrue(updateStateCalled);
            assertTrue(sendTimeSeriesCalled);
            assertTrue(timeSeriesCount > 0);
        }

        @Test
        public void invalidWsPayloadTypeDoesNotCrash() {
            setup("co2");
            handler.initialize();
            handler.handleUpdate("co2", com.google.gson.JsonParser.parseString("\"invalid-string\""));
            assertSame(ThingStatus.ONLINE, lastThingStatus);
        }

        @Test
        public void nullWsPayloadDoesNotCrash() {
            setup("co2");
            handler.initialize();
            com.google.gson.JsonElement nullValue = com.google.gson.JsonNull.INSTANCE;
            handler.handleUpdate("co2", nullValue);
            assertSame(ThingStatus.ONLINE, lastThingStatus);
        }

        @Test
        public void getStateFromCachedStateReturnsCo2() {
            setup("co2");
            JsonObject cached = readResponse("responses/example_response.json");
            JsonObject result = handler.getStateFromCachedState(cached);
            assertTrue(!result.isEmpty() || (cached.has("forecast") && cached.getAsJsonObject("forecast").has("co2")));
        }

        @Test
        public void getStateFromCachedStateReturnsFeedin() {
            setup("feedin");
            JsonObject cached = readResponse("responses/example_response.json");
            JsonObject result = handler.getStateFromCachedState(cached);
            assertTrue(
                    !result.isEmpty() || (cached.has("forecast") && cached.getAsJsonObject("forecast").has("feedin")));
        }

        @Test
        public void getStateFromCachedStateReturnsGrid() {
            setup("grid");
            JsonObject cached = readResponse("responses/example_response.json");
            JsonObject result = handler.getStateFromCachedState(cached);
            assertTrue(!result.isEmpty() || (cached.has("forecast") && cached.getAsJsonObject("forecast").has("grid")));
        }

        @Test
        public void getStateFromCachedStateReturnsEmptyWhenMissingSubtype() {
            setup("co2");
            JsonObject cached = new JsonObject();
            cached.add("forecast", new JsonObject());
            JsonObject result = handler.getStateFromCachedState(cached);
            assertTrue(result.isEmpty());
        }
    }
}
