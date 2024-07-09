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
package org.openhab.binding.intellicenter2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link IntelliCenter2BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class IntelliCenter2BindingConstants {

    private static final String BINDING_ID = "intellicenter2";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_POOL = new ThingTypeUID(BINDING_ID, "pool");
    public static final ThingTypeUID THING_TYPE_FEATURE = new ThingTypeUID(BINDING_ID, "feature");
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public static final ThingTypeUID THING_TYPE_PUMP = new ThingTypeUID(BINDING_ID, "pump");
    public static final ThingTypeUID BRIDGE_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_TEMPERATURE = "current-temperature";
    public static final String CHANNEL_TARGET_TEMPERATURE = "target-temperature";
    public static final String CHANNEL_HEATER_STATUS = "heater-status";
    public static final String CHANNEL_FEATURE_ON_OFF = "feature-on-off";
    public static final String CHANNEL_LIGHT_COLOR = "light-color";
    public static final String CHANNEL_LIGHT_POWER = "light-power";
    public static final String CHANNEL_PUMP_GPM = "pump-gpm";
    public static final String CHANNEL_PUMP_RPM = "pump-rpm";
    public static final String CHANNEL_PUMP_POWER = "pump-power";
}
