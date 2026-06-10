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
import static org.mockito.Mockito.when;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_ID;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_INDEX;
import static org.openhab.binding.evcc.internal.EvccBindingConstants.PROPERTY_VEHICLE_ID;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * The {@link EvccVehicleHandlerTest} is responsible for testing the EvccSiteHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccVehicleHandlerTest extends AbstractThingHandlerTestClass<EvccVehicleHandler> {

    private @Nullable Configuration lastUpdatedConfiguration;

    @Override
    protected EvccVehicleHandler createHandler() {
        return new EvccVehicleHandler(thing, channelTypeRegistry) {

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
            public void updateThing(Thing thing) {
            }

            @Override
            protected void updateState(ChannelUID channelUID, State state) {
            }

            @Override
            public void updateConfiguration(Configuration configuration) {
                lastUpdatedConfiguration = configuration;
            }
        };
    }

    @SuppressWarnings("null")
    @BeforeEach
    public void setup() {
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of(PROPERTY_VEHICLE_ID, "vehicle_1", "type", "vehicle"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        Configuration configuration = mock(Configuration.class);
        when(configuration.get(PROPERTY_INDEX)).thenReturn("0");
        when(configuration.get(PROPERTY_VEHICLE_ID)).thenReturn("vehicle_1");
        when(thing.getConfiguration()).thenReturn(configuration);
        handler = spy(createHandler());
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        handler.bridgeHandler = bridgeHandler;
        when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);
    }

    @SuppressWarnings("null")
    @Test
    public void testInitializeWithBridgeHandlerWithValidState() {
        handler.initialize();
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }

    @Test
    public void testMigrationFromIdToVehicleId() {
        Configuration config = new Configuration();
        config.put(PROPERTY_ID, "vehicle_1");

        when(thing.getConfiguration()).thenReturn(config);
        when(thing.getProperties()).thenReturn(Map.of("type", "vehicle"));

        handler = spy(createHandler());
        handler.bridgeHandler = mock(EvccBridgeHandler.class);

        when(handler.bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);

        handler.initialize();

        assertEquals("vehicle_1", config.get(PROPERTY_VEHICLE_ID));
        assertNull(config.get(PROPERTY_ID));
        assertSame(config, lastUpdatedConfiguration);
        assertSame(ThingStatus.ONLINE, lastThingStatus);
    }
}
