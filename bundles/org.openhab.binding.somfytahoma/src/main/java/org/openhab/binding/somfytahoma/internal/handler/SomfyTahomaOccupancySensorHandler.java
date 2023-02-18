/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * The {@link SomfyTahomaOccupancySensorHandler} is responsible for handling commands,
 * which are sent to one of the channels of the occupancy sensor thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaOccupancySensorHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaOccupancySensorHandler(Thing thing) {
        super(thing);
        stateNames.put(CONTACT, "core:OccupancyState");
        stateNames.put(SENSOR_DEFECT, SENSOR_DEFECT_STATE);
    }
}
