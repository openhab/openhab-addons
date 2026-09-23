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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.routing.GridStateTransformer;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Tests for {@link EvccSiteHandler}, focused on verifying that updates dispatched by the message
 * router (top-level primitives and the embedded "grid" object) are mapped to the correct,
 * properly-linked site channels.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccSiteHandlerTest {

    private final Thing thing = mock(Thing.class);
    private final ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);

    private EvccSiteHandler handler = createHandler();
    private ThingStatus lastThingStatus = ThingStatus.UNKNOWN;
    private boolean updateStateCalled = false;
    private final Set<String> linkedChannelIdsChecked = new HashSet<>();
    private final List<String> requestedUrls = new ArrayList<>();

    private EvccSiteHandler createHandler() {
        return new EvccSiteHandler(thing, channelTypeRegistry) {

            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail) {
                lastThingStatus = status;
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
            public void updateThing(Thing thing) {
            }

            @Override
            protected void updateState(ChannelUID channelUID, State state) {
                updateStateCalled = true;
            }

            @Override
            protected boolean isLinked(ChannelUID channelUID) {
                linkedChannelIdsChecked.add(channelUID.getId());
                return true;
            }

            @Override
            protected void performApiRequest(String url, String method, JsonElement payload) {
                requestedUrls.add(url);
            }
        };
    }

    @BeforeEach
    public void setup() {
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "pv"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        handler = spy(createHandler());
        lastThingStatus = ThingStatus.UNKNOWN;
        updateStateCalled = false;
        linkedChannelIdsChecked.clear();
        requestedUrls.clear();
    }

    @Test
    public void topLevelPvPowerShouldUpdateThroughSiteHandler() {
        handler.handleUpdate("pvPower", new JsonPrimitive(8476.122));
        assertSame(ThingStatus.ONLINE, lastThingStatus);
        assertSame(true, updateStateCalled);
    }

    @Test
    public void topLevelTariffGridShouldUpdateThroughSiteHandler() {
        handler.handleUpdate("tariffGrid", new JsonPrimitive(0.236));
        assertSame(ThingStatus.ONLINE, lastThingStatus);
        assertSame(true, updateStateCalled);
    }

    @Test
    public void arbitraryTopLevelPrimitiveShouldUpdateThroughSiteHandler() {
        handler.handleUpdate("someNewField", new JsonPrimitive(42));
        assertSame(ThingStatus.ONLINE, lastThingStatus);
        assertSame(true, updateStateCalled);
    }

    @Test
    public void gridObjectUpdateShouldCheckLinkedStateOnMappedChannelIds() {
        JsonObject gridUpdate = new JsonObject();
        gridUpdate.addProperty("power", 2000);
        gridUpdate.addProperty("energy", 10000);

        // The router applies the grid transformer before dispatching the normalized update to the handler.
        handler.applyNormalizedUpdate(new GridStateTransformer().transform(gridUpdate));

        assertSame(true, linkedChannelIdsChecked.contains("site-grid-power"));
        assertSame(true, linkedChannelIdsChecked.contains("site-grid-energy"));
        assertSame(false, linkedChannelIdsChecked.contains("gridPower"));
        assertSame(false, linkedChannelIdsChecked.contains("gridEnergy"));
    }

    @Test
    public void commandsUseSiteApiPaths() {
        handler.endpoint = "http://evcc/api";

        handler.handleCommand(new ChannelUID("test:thing:uid:site-battery-priority"),
                org.openhab.core.library.types.OnOffType.ON);
        handler.handleCommand(new ChannelUID("test:thing:uid:site-battery-soc-limit"),
                new org.openhab.core.library.types.DecimalType(80));

        assertEquals(List.of("http://evcc/api/batterypriority/true", "http://evcc/api/batterysoclimit/80"),
                requestedUrls);
    }
}
