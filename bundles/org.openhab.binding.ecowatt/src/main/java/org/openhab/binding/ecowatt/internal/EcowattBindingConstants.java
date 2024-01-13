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
package org.openhab.binding.ecowatt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EcowattBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class EcowattBindingConstants {

    private static final String BINDING_ID = "ecowatt";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SIGNALS = new ThingTypeUID(BINDING_ID, "signals");

    // List of all Channel ids
    public static final String CHANNEL_TODAY_SIGNAL = "todaySignal";
    public static final String CHANNEL_TOMORROW_SIGNAL = "tomorrowSignal";
    public static final String CHANNEL_IN_TWO_DAYS_SIGNAL = "inTwoDaysSignal";
    public static final String CHANNEL_IN_THREE_DAYS_SIGNAL = "inThreeDaysSignal";
    public static final String CHANNEL_CURRENT_HOUR_SIGNAL = "currentHourSignal";
}
