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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * The {@link EvccStatisticsHandlerTest} is responsible for testing the EvccSiteHandler implementation
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccStatisticsHandlerTest extends AbstractThingHandlerTestClass<EvccStatisticsHandler> {

    private boolean updateStateCalled = false;
    private int updateStateCounter = 0;

    @Override
    protected EvccStatisticsHandler createHandler() {
        return new EvccStatisticsHandler(thing, channelTypeRegistry) {

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
        };
    }

    @SuppressWarnings("null")
    @BeforeEach
    public void setup() {
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "statistics"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
        handler = spy(createHandler());
        EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
        handler.bridgeHandler = bridgeHandler;
        when(bridgeHandler.getCachedEvccState()).thenReturn(exampleResponse);
    }

    @SuppressWarnings("null")
    @Nested
    public class TestPrepareApiResponseForChannelStateUpdate {

        @Test
        public void handlerIsInitialized() {
            handler.isInitialized = true;

            handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
            assertTrue(updateStateCalled);
            assertEquals(16, updateStateCounter);
            assertSame(ThingStatus.ONLINE, lastThingStatus);
        }

        @Test
        public void handlerIsNotInitialized() {
            handler.prepareApiResponseForChannelStateUpdate(exampleResponse);
            assertSame(ThingStatus.OFFLINE, lastThingStatus);
        }
    }
}
