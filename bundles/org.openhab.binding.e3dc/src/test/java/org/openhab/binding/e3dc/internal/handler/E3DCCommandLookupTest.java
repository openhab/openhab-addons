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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.E3DCCommandRegistry;
import org.openhab.binding.e3dc.internal.E3DCCommandSpec;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

/**
 * Tests that a command arriving for a channel finds its write specification.
 *
 * <p>
 * The registries are keyed by the id of the channel inside its group, but a channel UID carries the group in
 * its id, so a lookup by the full id never finds a channel. Every writable channel is checked with the UID
 * the framework hands to the handler.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCCommandLookupTest {

    private static final ThingUID THING = new ThingUID("e3dc", "wallbox", "483443f62a", "wallbox-0");

    private static void assertEveryChannelIsFound(E3DCCommandRegistry registry) {
        assertFalse(registry.groups().isEmpty(), "the registry must have writable channels");
        for (Map.Entry<String, Map<String, E3DCCommandSpec>> group : registry.groups().entrySet()) {
            for (Map.Entry<String, E3DCCommandSpec> channel : group.getValue().entrySet()) {
                ChannelUID uid = new ChannelUID(THING, group.getKey(), channel.getKey());
                assertSame(channel.getValue(), E3DCThingHandler.findSpec(registry, uid),
                        "the channel " + uid + " must be writable");
            }
        }
    }

    @Test
    void everyWallboxChannelIsFoundByItsUid() {
        assertEveryChannelIsFound(E3DCCommandSpec.wallbox());
    }

    @Test
    void everyPowerPlantChannelIsFoundByItsUid() {
        assertEveryChannelIsFound(E3DCCommandSpec.powerPlant());
    }

    @Test
    void theSunModeOfAWallboxIsWritable() {
        ChannelUID uid = new ChannelUID("e3dc:wallbox:483443f62a:wallbox-0:status-control#sun-mode");

        assertNotNull(E3DCThingHandler.findSpec(E3DCCommandSpec.wallbox(), uid));
    }

    @Test
    void aChannelWithoutSpecificationIsNotWritable() {
        ChannelUID uid = new ChannelUID("e3dc:wallbox:483443f62a:wallbox-0:status-control#plugged");

        assertNull(E3DCThingHandler.findSpec(E3DCCommandSpec.wallbox(), uid));
    }
}
