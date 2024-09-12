/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.plug.PlugHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit tests for {@link BoschSHCHandlerFactory}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class BoschSHCHandlerFactoryTest {

    private @NonNullByDefault({}) BoschSHCHandlerFactory fixture;

    @BeforeEach
    public void setUp() throws Exception {
        fixture = new BoschSHCHandlerFactory(() -> ZoneId.systemDefault());
    }

    @Test
    void testSupportsThingType() {
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_SHC));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_TWINGUARD));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_THERMOSTAT));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_CAMERA_360));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_CAMERA_EYES));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_INTRUSION_DETECTION_SYSTEM));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_SMART_PLUG_COMPACT));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_SMART_BULB));
        assertTrue(fixture.supportsThingType(BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR));

        assertFalse(fixture.supportsThingType(new ThingTypeUID(BoschSHCBindingConstants.BINDING_ID, "foo")));
    }

    @Test
    void testCreateHandler() {
        Thing thing = mock(Thing.class);
        when(thing.getThingTypeUID()).thenReturn(BoschSHCBindingConstants.THING_TYPE_SMART_PLUG_COMPACT);
        assertTrue(fixture.createHandler(thing) instanceof PlugHandler);
    }
}
