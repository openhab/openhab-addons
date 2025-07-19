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
package org.openhab.binding.matter.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Tests for {@link MatterUIDUtils}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class MatterUIDUtilsTest {

    @Test
    void nodeThingTypeUIDReturnsThingTypeWithNodeId() {
        ThingTypeUID actual = MatterUIDUtils.nodeThingTypeUID(new BigInteger("12345678901234567890"));
        assertEquals("matter:node-12345678901234567890", actual.getAsString());
    }

    @Test
    void endpointThingTypeUIDReturnsThingTypeWithNodeAndEndpointId() {
        ThingTypeUID actual = MatterUIDUtils.endpointThingTypeUID(new BigInteger("12345678901234567890"), 2);
        assertEquals("matter:endpoint-12345678901234567890-2", actual.getAsString());
    }

    @Test
    void baseTypeForThingTypeReturnsNullForUnrelatedBinding() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("zwavejs", "node");
        assertEquals(null, MatterUIDUtils.baseTypeForThingType(thingTypeUID));
    }

    @Test
    void baseTypeForThingTypeReturnsMatterNodeForDynamicThingType() {
        ThingTypeUID thingTypeUID = new ThingTypeUID("matter", "node-12345678901234567890");
        assertEquals(MatterBindingConstants.THING_TYPE_NODE, MatterUIDUtils.baseTypeForThingType(thingTypeUID));
    }
}
