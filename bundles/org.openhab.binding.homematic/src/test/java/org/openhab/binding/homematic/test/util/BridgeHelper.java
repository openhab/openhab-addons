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
package org.openhab.binding.homematic.test.util;

import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Class that contains static helper methods to create several kinds of objects
 * (e.g. {@link ThingTypeUID}, {@link ThingUID}) related to a homematic bridge.
 * 
 * @author Florian Stolte - Initial Contribution
 * 
 */
public class BridgeHelper {

    public static Bridge createHomematicBridge() {
        return BridgeBuilder.create(createHomematicBridgeThingTypeUID(), createHomematicBridgeUID()).build();
    }

    public static ThingUID createHomematicBridgeUID() {
        return new ThingUID(createHomematicBridgeThingTypeUID(), "myBridge");
    }

    public static ThingTypeUID createHomematicBridgeThingTypeUID() {
        return new ThingTypeUID("homematic:bridge");
    }
}
