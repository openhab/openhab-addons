/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mysensors.test;

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.mysensors.MySensorsBindingConstants.THING_UID_MAP;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.junit.Test;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;

public class DiscoveryServiceTest {

    @Test
    public void testThingUidMap() {

        THING_UID_MAP.forEach((MySensorsMessageSubType entry, ThingTypeUID thingTypeUid) -> {
            assertEquals(true, thingTypeUid.getId().matches("^[a-zA-Z-]*$"));
        });
    }

}
