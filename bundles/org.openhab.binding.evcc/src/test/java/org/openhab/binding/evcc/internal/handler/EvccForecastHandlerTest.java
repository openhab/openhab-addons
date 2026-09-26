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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.routing.MessageRouter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Tests for {@link EvccForecastHandler}, focused on verifying that the "scaled" forecast
 * channel applies the fixture-reported scale factor while the plain solar forecast channel
 * remains unscaled, and that neither channel is reset to UNDEF when the underlying JSON object
 * that drives channel creation does not itself contain a matching top-level key for them.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccForecastHandlerTest {

    private final Thing thing = mock(Thing.class);
    private final ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
    private final Map<String, TimeSeries> sentTimeSeries = new HashMap<>();
    private final Map<String, List<State>> updatedStates = new HashMap<>();
    @Nullable
    private EvccForecastHandler handler;

    private EvccForecastHandler createHandler() {
        return new EvccForecastHandler(thing, channelTypeRegistry) {
            @Override
            public void logUnknownChannelXmlAsync(String key, String itemType) {
            }

            @Nullable
            @Override
            protected Bridge getBridge() {
                return null;
            }

            @Override
            public void updateThing(Thing thing) {
            }

            @Override
            protected boolean isLinked(ChannelUID channelUID) {
                return true;
            }

            @Override
            protected void updateState(ChannelUID channelUID, State state) {
                updatedStates.computeIfAbsent(channelUID.getId(), id -> new ArrayList<>()).add(state);
            }

            @Override
            protected void sendTimeSeries(ChannelUID channelUID, TimeSeries timeSeries) {
                sentTimeSeries.put(channelUID.getId(), timeSeries);
            }
        };
    }

    @BeforeEach
    public void setup() {
        sentTimeSeries.clear();
        updatedStates.clear();
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("type", "forecast"));
        // The "forecast-solar" and "forecast-scaled" channels already exist on the Thing (as they
        // would after the first full-state update) but are never literal keys of the "solar" JSON
        // object passed to createChannelsAndSetStatesFromApiResponse, since their values are derived
        // from the forecast TimeSeries instead.
        when(thing.getChannels()).thenReturn(new ArrayList<>(List.of(
                ChannelBuilder.create(new ChannelUID("test:thing:uid:forecast-solar"))
                        .withAcceptedItemType("Number:Energy").build(),
                ChannelBuilder.create(new ChannelUID("test:thing:uid:forecast-scaled"))
                        .withAcceptedItemType("Number:Energy").build())));
        Configuration configuration = mock(Configuration.class);
        when(configuration.get("subType")).thenReturn("solar");
        when(thing.getConfiguration()).thenReturn(configuration);
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        when(bridgeHandler.getBaseURL()).thenReturn("http://evcc/api");
        when(bridgeHandler.getMessageRouter()).thenReturn(mock(MessageRouter.class));
        EvccForecastHandler localHandler = createHandler();
        localHandler.bridgeHandler = bridgeHandler;
        localHandler.initialize();
        handler = localHandler;
    }

    @Test
    public void scaledForecastChannelAppliesFixtureScaleWhileSolarRemainsUnscaled() {
        JsonObject entry = new JsonObject();
        entry.addProperty("ts", "2026-01-01T10:00:00Z");
        entry.addProperty("val", 100);

        JsonArray timeseries = new JsonArray();
        timeseries.add(entry);

        JsonObject solar = new JsonObject();
        solar.addProperty("scale", 2.5);
        solar.add("timeseries", timeseries);

        Objects.requireNonNull(handler).handleUpdate("solar", solar);

        assertTrue(sentTimeSeries.containsKey("forecast-solar"));
        assertTrue(sentTimeSeries.containsKey("forecast-scaled"));

        double unscaledValue = extractSingleValue(Objects.requireNonNull(sentTimeSeries.get("forecast-solar")));
        double scaledValue = extractSingleValue(Objects.requireNonNull(sentTimeSeries.get("forecast-scaled")));

        assertEquals(100.0, unscaledValue, 0.0001);
        assertEquals(250.0, scaledValue, 0.0001);
    }

    @Test
    public void solarAndScaledChannelsAreNeverResetToUndef() {
        JsonObject entry = new JsonObject();
        entry.addProperty("ts", "2026-01-01T10:00:00Z");
        entry.addProperty("val", 100);

        JsonArray timeseries = new JsonArray();
        timeseries.add(entry);

        JsonObject solar = new JsonObject();
        solar.addProperty("scale", 2.5);
        solar.add("timeseries", timeseries);

        Objects.requireNonNull(handler).handleUpdate("solar", solar);

        assertFalse(updatedStates.getOrDefault("forecast-solar", List.of()).contains(UnDefType.UNDEF),
                "forecast-solar must not be reset to UNDEF by createChannelsAndSetStatesFromApiResponse");
        assertFalse(updatedStates.getOrDefault("forecast-scaled", List.of()).contains(UnDefType.UNDEF),
                "forecast-scaled must not be reset to UNDEF by createChannelsAndSetStatesFromApiResponse");
    }

    private double extractSingleValue(TimeSeries timeSeries) {
        return timeSeries.getStates().findFirst().map(e -> e.state().as(QuantityType.class)).map(q -> q.doubleValue())
                .orElseThrow();
    }
}
