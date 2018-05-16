/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal;

/**
 * A names of all GardenaSmart commands.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public enum GardenaSmartCommandName {
    // mower
    PARK_UNTIL_FURTHER_NOTICE,
    PARK_UNTIL_NEXT_TIMER,
    START_OVERRIDE_TIMER,
    START_RESUME_SCHEDULE,
    DURATION_PROPERTY,

    // sensor
    MEASURE_AMBIENT_TEMPERATURE,
    MEASURE_LIGHT,
    MEASURE_SOIL_HUMIDITY,
    MEASURE_SOIL_TEMPERATURE,

    // outlet
    OUTLET_MANUAL_OVERRIDE_TIME,
    OUTLET_VALVE,

    // power
    POWER_TIMER;
}
