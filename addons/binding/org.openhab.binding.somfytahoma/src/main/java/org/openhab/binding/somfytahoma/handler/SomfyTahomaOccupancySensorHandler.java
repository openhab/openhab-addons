/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.thing.Thing;

import java.util.HashMap;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.CONTACT;

/**
 * The {@link SomfyTahomaOccupancySensorHandler} is responsible for handling commands,
 * which are sent to one of the channels of the occupancy sensor thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public class SomfyTahomaOccupancySensorHandler extends SomfyTahomaContactSensorHandler {

    public SomfyTahomaOccupancySensorHandler(Thing thing) {
        super(thing);
        stateNames = new HashMap<String, String>() {{
            put(CONTACT, "core:OccupancyState");
        }};
    }

}
