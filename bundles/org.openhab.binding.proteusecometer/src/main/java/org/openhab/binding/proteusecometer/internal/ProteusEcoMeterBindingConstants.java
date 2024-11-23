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
package org.openhab.binding.proteusecometer.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ProteusEcoMeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Herrmann - Initial contribution
 */
@NonNullByDefault
public class ProteusEcoMeterBindingConstants {

    private static final String BINDING_ID = "proteusecometer";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ECO_METER_S = new ThingTypeUID(BINDING_ID, "EcoMeterS");

    public static final String TEMPERATURE = "temperature";

    public static final String SENSOR_LEVEL = "sensorLevel";

    public static final String USABLE_LEVEL = "usableLevel";

    public static final String USABLE_LEVEL_IN_PERCENT = "usableLevelInPercent";

    public static final String TOTAL_CAPACITY = "totalCapacity";
}
