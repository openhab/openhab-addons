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
package org.openhab.binding.rachio.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_BASE_STATION;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_FLEX_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE_PROGRAM;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_ZONE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.handler.RachioFlexScheduleHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Tests Rachio handler factory Thing type support.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioHandlerFactoryTest {
    @Test
    void supportsFlexScheduleThingType() {
        RachioHandlerFactory factory = new RachioHandlerFactory();

        assertThat(factory.supportsThingType(THING_TYPE_FLEX_SCHEDULE), is(true));
        assertThat(factory.supportsThingType(new ThingTypeUID("rachio", "unsupported")), is(false));
    }

    @Test
    void doesNotSupportTemporaryFlexScheduleThingType() {
        RachioHandlerFactory factory = new RachioHandlerFactory();
        ThingTypeUID temporaryFlexScheduleType = new ThingTypeUID("rachio", "flexschedule");

        assertThat(factory.supportsThingType(temporaryFlexScheduleType), is(false));
    }

    @Test
    void eachSupportedThingTypeHasThingXmlMetadata() {
        Map<ThingTypeUID, String> metadataFiles = Map.of(THING_TYPE_CLOUD, "cloud.xml", THING_TYPE_DEVICE, "device.xml",
                THING_TYPE_ZONE, "zone.xml", THING_TYPE_SCHEDULE, "schedule.xml", THING_TYPE_FLEX_SCHEDULE,
                "flex-schedule.xml", THING_TYPE_BASE_STATION, "base-station.xml", THING_TYPE_VALVE, "valve.xml",
                THING_TYPE_VALVE_PROGRAM, "valve-program.xml");

        assertThat(metadataFiles.keySet(), is(SUPPORTED_THING_TYPES_UIDS));
        for (String metadataFile : metadataFiles.values()) {
            assertThat(getClass().getResource("/OH-INF/thing/" + metadataFile), notNullValue());
        }
    }

    @Test
    void createsFlexScheduleHandlerForFlexScheduleThingType() {
        RachioHandlerFactory factory = new RachioHandlerFactory();
        ThingUID bridgeUID = new ThingUID(THING_TYPE_CLOUD, "bridge");
        ThingUID thingUID = new ThingUID(THING_TYPE_FLEX_SCHEDULE, bridgeUID, "flex-id");
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_FLEX_SCHEDULE);
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getBridgeUID()).thenReturn(bridgeUID);

        ThingHandler handler = factory.createHandler(thing);

        assertThat(handler, instanceOf(RachioFlexScheduleHandler.class));
    }

    @Test
    void doesNotCreateHandlerForTemporaryFlexScheduleThingType() {
        RachioHandlerFactory factory = new RachioHandlerFactory();
        ThingTypeUID temporaryFlexScheduleType = new ThingTypeUID("rachio", "flexschedule");
        ThingUID thingUID = new ThingUID(temporaryFlexScheduleType, "bridge", "flex-id");
        Thing thing = Mockito.mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(temporaryFlexScheduleType);
        when(thing.getUID()).thenReturn(thingUID);

        ThingHandler handler = factory.createHandler(thing);

        assertThat(handler, nullValue());
    }
}
