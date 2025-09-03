/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonObject;

/**
 * Abstract base test for EvccBaseThingHandler implementations.
 * Extend this class in your handler tests and override createHandler().
 *
 * @author Marcel Goerentz - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public abstract class AbstractThingHandlerTestClass<T extends EvccBaseThingHandler> {

    protected Thing thing = mock(Thing.class);
    protected ChannelTypeRegistry channelTypeRegistry = mock(ChannelTypeRegistry.class);
    @Nullable
    protected T handler = null;

    protected ThingStatus lastThingStatus = ThingStatus.UNKNOWN;
    protected ThingStatusDetail lastThingStatusDetail = ThingStatusDetail.NONE;

    /**
     * Implement this to provide a handler instance for testing.
     */
    protected abstract T createHandler();

    @BeforeEach
    public void setUp() {
        handler = createHandler();
        when(thing.getUID()).thenReturn(new ThingUID("test:thing:uid"));
        when(thing.getProperties()).thenReturn(Map.of("index", "0", "type", "battery"));
        when(thing.getChannels()).thenReturn(new ArrayList<>());
    }

    @Nested
    class InitializeTests {

        @Test
        public void initializeWithoutBridgeHandler() {
            handler.initialize();
            assertEquals(ThingStatus.OFFLINE, lastThingStatus);
            assertEquals(ThingStatusDetail.BRIDGE_UNINITIALIZED, lastThingStatusDetail);
        }

        @Test
        public void initializeWithBridgeHandlerWithoutCachedState() {
            EvccBridgeHandler bridgeHandler = mock(EvccBridgeHandler.class);
            handler.bridgeHandler = bridgeHandler;
            when(bridgeHandler.getCachedEvccState()).thenReturn(new JsonObject());

            handler.initialize();
            assertEquals(ThingStatus.OFFLINE, lastThingStatus);
            assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, lastThingStatusDetail);
        }
    }
}
