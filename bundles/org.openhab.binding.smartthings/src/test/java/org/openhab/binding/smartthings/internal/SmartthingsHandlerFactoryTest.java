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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.smartthings.internal.SmartthingsBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.local.LocalApplianceHandler;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Verifies local-only standalone appliance dispatch.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class SmartthingsHandlerFactoryTest {
    private final SmartthingsHandlerFactory factory = new SmartthingsHandlerFactory();

    @Test
    void supportsOnlyLocalAppliances() {
        assertTrue(factory.supportsThingType(THING_TYPE_LOCAL_APPLIANCE));
        for (String removed : new String[] { "smartthings", "switch", "airConditionerMode", "unknown" }) {
            assertFalse(factory.supportsThingType(new ThingTypeUID(BINDING_ID, removed)));
        }
    }

    @Test
    void createsStandaloneLocalHandlersWithoutABridge() {
        for (String id : new String[] { "first", "second" }) {
            assertInstanceOf(LocalApplianceHandler.class, factory.createHandler(ThingBuilder
                    .create(THING_TYPE_LOCAL_APPLIANCE, new ThingUID(THING_TYPE_LOCAL_APPLIANCE, id)).build()));
        }
    }

    @Test
    void removedAndUnsupportedTypesHaveNoHandler() {
        for (String removed : new String[] { "smartthings", "switch", "unknown" }) {
            ThingTypeUID unsupported = new ThingTypeUID(BINDING_ID, removed);
            assertNull(
                    factory.createHandler(ThingBuilder.create(unsupported, new ThingUID(unsupported, "test")).build()));
        }
    }
}
