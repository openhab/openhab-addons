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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * The {@link SomfyTahomaThermostatHandler} is responsible for handling commands,
 * which are sent to one of the channels of the Somfy thermostat thing.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class SomfyTahomaThermostatHandler extends SomfyTahomaBaseThingHandler {

    public SomfyTahomaThermostatHandler(Thing thing) {
        super(thing);
        stateNames.put(TARGET_TEMPERATURE, TARGET_TEMPERATURE_STATE);
        stateNames.put(BATTERY_LEVEL, BATTERY_LEVEL_STATE);
        stateNames.put(HEATING_MODE, "somfythermostat:HeatingModeState");
        stateNames.put(DEROGATION_HEATING_MODE, "somfythermostat:DerogationHeatingModeState");
        stateNames.put(DEROGATION_ACTIVATION, "core:DerogationActivationState");
        stateNames.put(DEROGATED_TARGET_TEMPERATURE, "core:DerogatedTargetTemperatureState");
    }
}
