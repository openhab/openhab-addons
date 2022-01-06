/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BsbLanHandlerFactoryTests} class implements tests
 * for {@link BsbLanHandlerFactory}.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanHandlerFactoryTests {

    public static final ThingTypeUID UNKNOWN_THING_TYPE_UID = new ThingTypeUID("bsblan", "unknown");

    /**
     * Test if factory knows all thing types.
     */
    @Test
    public void supportsThingType() {
        BsbLanHandlerFactory factory = new BsbLanHandlerFactory();
        assertFalse(factory.supportsThingType(UNKNOWN_THING_TYPE_UID));

        assertTrue(factory.supportsThingType(BsbLanBindingConstants.THING_TYPE_BRIDGE));
        assertTrue(factory.supportsThingType(BsbLanBindingConstants.THING_TYPE_PARAMETER));
    }
}
