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

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.HUMIDITY;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * The {@link SomfyTahomaHumiditySensorHandler} is responsible for handling commands,
 * which are sent to one of the channels of the humidity sensors.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaHumiditySensorHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaHumiditySensorHandler(Thing thing) {
        super(thing);
        stateNames.put(HUMIDITY, "core:RelativeHumidityState");
    }
}
