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
package org.openhab.binding.remehaheating.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link RemehaHeatingHandlerFactory}.
 *
 * @author Michael Fraedrich - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class RemehaHeatingHandlerFactoryTest {

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @Mock @NonNullByDefault({}) org.eclipse.jetty.client.HttpClient httpClient;
    private @NonNullByDefault({}) RemehaHeatingHandlerFactory factory;

    @BeforeEach
    public void setUp() {
        lenient().when(httpClientFactory.createHttpClient("remehaheating")).thenReturn(httpClient);
        factory = new RemehaHeatingHandlerFactory(httpClientFactory);
    }

    @Test
    public void testSupportsThingType() {
        assertTrue(factory.supportsThingType(RemehaHeatingBindingConstants.THING_TYPE_BOILER));
        assertFalse(factory.supportsThingType(new ThingTypeUID("other", "thing")));
    }

    @Test
    public void testCreateHandler() {
        when(thing.getThingTypeUID()).thenReturn(RemehaHeatingBindingConstants.THING_TYPE_BOILER);

        ThingHandler handler = factory.createHandler(thing);

        assertNotNull(handler);
        assertInstanceOf(RemehaHeatingHandler.class, handler);
        verify(httpClient).setRequestBufferSize(16384);
        verify(httpClient).setResponseBufferSize(16384);
    }

    @Test
    public void testCreateHandlerForUnsupportedThing() {
        when(thing.getThingTypeUID()).thenReturn(new ThingTypeUID("other", "thing"));

        ThingHandler handler = factory.createHandler(thing);

        assertNull(handler);
    }
}
