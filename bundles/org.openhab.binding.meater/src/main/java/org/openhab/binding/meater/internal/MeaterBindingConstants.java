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
package org.openhab.binding.meater.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MeaterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan Gustafsson - Initial contribution
 */
@NonNullByDefault
public class MeaterBindingConstants {

    public static final String BINDING_ID = "meater";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MEATER_PROBE = new ThingTypeUID(BINDING_ID, "meaterprobe");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "meaterapi");

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_LAST_CONNECTION = "lastConnection";
    public static final String CHANNEL_INTERNAL_TEMPERATURE = "internalTemperature";
    public static final String CHANNEL_AMBIENT_TEMPERATURE = "ambientTemperature";
    public static final String CHANNEL_COOK_ID = "cookId";
    public static final String CHANNEL_COOK_NAME = "cookName";
    public static final String CHANNEL_COOK_STATE = "cookState";
    public static final String CHANNEL_COOK_TARGET_TEMPERATURE = "cookTargetTemperature";
    public static final String CHANNEL_COOK_PEAK_TEMPERATURE = "cookPeakTemperature";
    public static final String CHANNEL_COOK_ELAPSED_TIME = "cookElapsedTime";
    public static final String CHANNEL_COOK_REMAINING_TIME = "cookRemainingTime";
    public static final String CHANNEL_COOK_ESTIMATED_END_TIME = "cookEstimatedEndTime";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE,
            THING_TYPE_MEATER_PROBE);
}
