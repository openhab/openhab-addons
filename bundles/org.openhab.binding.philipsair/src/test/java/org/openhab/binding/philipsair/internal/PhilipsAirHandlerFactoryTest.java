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
package org.openhab.binding.philipsair.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests the thing types {@link PhilipsAirHandlerFactory} creates handlers for.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class PhilipsAirHandlerFactoryTest {

    private @Mock @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider;

    private @NonNullByDefault({}) PhilipsAirHandlerFactory factory;

    @BeforeEach
    public void setUp() {
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        factory = new PhilipsAirHandlerFactory(httpClientFactory, stateDescriptionProvider);
    }

    private static Thing thing(ThingTypeUID thingTypeUID) {
        return ThingBuilder.create(thingTypeUID, new ThingUID(thingTypeUID, "test"))
                .withConfiguration(new Configuration()).build();
    }

    @Test
    public void supportsExactlyTheSupportedThingTypes() {
        assertEquals(Set.of("universal", "coap", "ac2889-10", "ac2729", "ac1214-10", "ac3829-10"),
                SUPPORTED_THING_TYPES_UIDS.stream().map(ThingTypeUID::getId).collect(Collectors.toSet()));
        for (ThingTypeUID thingTypeUID : SUPPORTED_THING_TYPES_UIDS) {
            assertTrue(factory.supportsThingType(thingTypeUID), thingTypeUID.toString());
        }
        assertFalse(factory.supportsThingType(new ThingTypeUID(BINDING_ID, "ac9999")));
        assertFalse(factory.supportsThingType(new ThingTypeUID("other", SUPPORTED_MODEL_COAP)));
    }

    @Test
    public void handlerIsCreatedForSupportedThingTypes() {
        for (ThingTypeUID thingTypeUID : SUPPORTED_THING_TYPES_UIDS) {
            ThingHandler handler = factory.createHandler(thing(thingTypeUID));

            assertInstanceOf(PhilipsAirHandler.class, handler, thingTypeUID.toString());
        }
    }

    @Test
    public void noHandlerIsCreatedForOtherThingTypes() {
        assertNull(factory.createHandler(thing(new ThingTypeUID(BINDING_ID, "ac9999"))));
        assertNull(factory.createHandler(thing(new ThingTypeUID("other", SUPPORTED_MODEL_COAP))));
    }
}
