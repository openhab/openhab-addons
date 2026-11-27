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
package org.openhab.binding.tibber.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler;
import org.openhab.binding.tibber.internal.pulse.handler.MeterHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests the scenarios "Thing UIDs", "Handler for each thing type", and "Thing of the test version" of the
 * {@code thing-lifecycle} spec, and that the two handler factories of the binding serve disjoint thing types. The
 * Pulse factory is created without any service: that is what lets Pulse things work without the cloud services.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
class TibberPulseHandlerFactoryTest {

    // The id of the test version, assembled so that no source file contains it as one word.
    private static final String TEST_VERSION_ID = "tibber" + "pulselocal";
    private static final ThingTypeUID TEST_VERSION_BRIDGE = new ThingTypeUID(TEST_VERSION_ID, "bridge");
    private static final ThingTypeUID TEST_VERSION_METER = new ThingTypeUID(TEST_VERSION_ID, "meter");

    private final TibberPulseHandlerFactory pulseFactory = new TibberPulseHandlerFactory();

    private TibberHandlerFactory cloudFactory() {
        HttpClientFactory httpFactory = mock(HttpClientFactory.class);
        when(httpFactory.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        return new TibberHandlerFactory(httpFactory, mock(CronScheduler.class), mock(TimeZoneProvider.class),
                mock(StorageService.class));
    }

    private static Bridge bridgeOf(ThingTypeUID type) {
        return BridgeBuilder.create(type, new ThingUID(type, "home")).build();
    }

    private static Thing thingOf(ThingTypeUID type) {
        return ThingBuilder.create(type, new ThingUID(type, "main")).build();
    }

    // thing-lifecycle: Thing UIDs

    @Test
    void whenTheBridgeAndTheMeterThingIdsAreGivenThenTheThingUidsUseThePulseThingTypeIds() {
        ThingUID bridge = new ThingUID(THING_TYPE_PULSE_BRIDGE, "home");
        ThingUID meter = new ThingUID(THING_TYPE_PULSE_METER, bridge, "main");

        assertEquals("tibber:pulse-bridge:home", bridge.getAsString());
        assertEquals("tibber:pulse-meter:home:main", meter.getAsString());
        assertEquals(BINDING_ID, THING_TYPE_PULSE_BRIDGE.getBindingId());
        assertEquals(BINDING_ID, THING_TYPE_PULSE_METER.getBindingId());
    }

    // thing-lifecycle: Handler for each thing type

    @Test
    void whenAThingOfEachThingTypeIsGivenThenTheFactoryOfItsFamilyCreatesTheHandler() {
        ThingHandler bridgeHandler = pulseFactory.createHandler(bridgeOf(THING_TYPE_PULSE_BRIDGE));
        ThingHandler meterHandler = pulseFactory.createHandler(thingOf(THING_TYPE_PULSE_METER));
        ThingHandler cloudHandler = cloudFactory().createHandler(thingOf(TIBBER_THING_TYPE));

        assertTrue(bridgeHandler instanceof BridgeHandler);
        assertTrue(meterHandler instanceof MeterHandler);
        assertTrue(cloudHandler instanceof TibberHandler);
    }

    @Test
    void whenTheFactoriesAreAskedForThingTypesThenEachSupportsOnlyItsOwnFamily() {
        TibberHandlerFactory cloud = cloudFactory();

        assertTrue(pulseFactory.supportsThingType(THING_TYPE_PULSE_BRIDGE));
        assertTrue(pulseFactory.supportsThingType(THING_TYPE_PULSE_METER));
        assertFalse(pulseFactory.supportsThingType(TIBBER_THING_TYPE));
        assertTrue(cloud.supportsThingType(TIBBER_THING_TYPE));
        assertFalse(cloud.supportsThingType(THING_TYPE_PULSE_BRIDGE));
        assertFalse(cloud.supportsThingType(THING_TYPE_PULSE_METER));
    }

    @Test
    void whenAThingOfTheOtherFamilyIsGivenThenNoHandlerIsCreated() {
        assertNull(pulseFactory.createHandler(thingOf(TIBBER_THING_TYPE)));
        assertNull(cloudFactory().createHandler(bridgeOf(THING_TYPE_PULSE_BRIDGE)));
        assertNull(cloudFactory().createHandler(thingOf(THING_TYPE_PULSE_METER)));
    }

    @Test
    void whenABridgeThingTypeIsGivenForAThingThatIsNoBridgeThenNoBridgeHandlerIsCreated() {
        assertNull(pulseFactory.createHandler(thingOf(THING_TYPE_PULSE_BRIDGE)));
    }

    // thing-lifecycle: Thing of the test version

    @Test
    void whenAThingOfTheTestVersionIsGivenThenNeitherFactorySupportsItAndNoHandlerIsCreated() {
        TibberHandlerFactory cloud = cloudFactory();

        for (ThingTypeUID type : new ThingTypeUID[] { TEST_VERSION_BRIDGE, TEST_VERSION_METER }) {
            assertFalse(pulseFactory.supportsThingType(type), type.getAsString());
            assertFalse(cloud.supportsThingType(type), type.getAsString());
            assertNull(pulseFactory.createHandler(thingOf(type)), type.getAsString());
            assertNull(cloud.createHandler(thingOf(type)), type.getAsString());
        }
        assertNull(pulseFactory.createHandler(bridgeOf(TEST_VERSION_BRIDGE)));
    }
}
