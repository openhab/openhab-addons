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
package org.openhab.binding.omnikinverter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OmnikInverterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans van den Bogert - Initial contribution
 */
@NonNullByDefault
public class OmnikInverterBindingConstants {

    private static final String BINDING_ID = "omnikinverter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OMNIK = new ThingTypeUID(BINDING_ID, "omnik");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_PV1 = "currentPV1";
    public static final String CHANNEL_CURRENT_PV2 = "currentPV2";
    public static final String CHANNEL_CURRENT_PV3 = "currentPV3";

    public static final String CHANNEL_VOLTAGE_PV1 = "voltagePV1";
    public static final String CHANNEL_VOLTAGE_PV2 = "voltagePV2";
    public static final String CHANNEL_VOLTAGE_PV3 = "voltagePV3";

    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_POWER_AC1 = "powerAC1";
    public static final String CHANNEL_POWER_AC2 = "powerAC2";
    public static final String CHANNEL_POWER_AC3 = "powerAC3";

    public static final String CHANNEL_ENERGY_TODAY = "energyToday";

    public static final String CHANNEL_ENERGY_TOTAL = "energyTotal";
}
