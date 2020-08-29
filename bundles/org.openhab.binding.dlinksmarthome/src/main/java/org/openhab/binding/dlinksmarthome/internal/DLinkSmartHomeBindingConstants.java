/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dlinksmarthome.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DLinkSmartHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mike Major - Initial contribution
 * @author Pascal Bies - Add DSP-W215 thing type
 */
@NonNullByDefault
public class DLinkSmartHomeBindingConstants {

    public static final String BINDING_ID = "dlinksmarthome";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DCHS150 = new ThingTypeUID(BINDING_ID, "DCH-S150");
    public static final ThingTypeUID THING_TYPE_DSPW215 = new ThingTypeUID(BINDING_ID, "DSP-W215");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_DCHS150, THING_TYPE_DSPW215));

    // channel names
    public static final String MOTION = "motion";

    public static final String CURRENT_CONSUMPTION = "current_consumption";
    public static final String TOTAL_CONSUMPTION = "total_consumption";
    public static final String TEMPERATURE = "temperature";
    public static final String STATE = "state";
    public static final List<String> SMART_PLUG_CHANNEL_IDS = Arrays.asList(CURRENT_CONSUMPTION, TOTAL_CONSUMPTION,
            TEMPERATURE, STATE);
}
