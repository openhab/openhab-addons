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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * The {@link SomfyTahomaTemperatureSensorHandler} is responsible for handling commands,
 * which are sent to one of the channels of the temperature sensor thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaTemperatureSensorHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaTemperatureSensorHandler(Thing thing) {
        super(thing);
        stateNames.put(TEMPERATURE, TEMPERATURE_STATE);

        // override state type because the cloud sends both percent & decimal
        cacheStateType(TEMPERATURE_STATE, TYPE_DECIMAL);
    }
}
