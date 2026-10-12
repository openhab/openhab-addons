/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Constants shared across the binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCBindingConstants {

    public static final String BINDING_ID = "e3dc";

    /** The device connection. All other things depend on it. */
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    /** The home power plant itself. */
    public static final ThingTypeUID THING_TYPE_POWER_PLANT = new ThingTypeUID(BINDING_ID, "power-plant");

    /** One wallbox attached to the power plant. */
    public static final ThingTypeUID THING_TYPE_WALLBOX = new ThingTypeUID(BINDING_ID, "wallbox");

    /** One power meter attached to the power plant. */
    public static final ThingTypeUID THING_TYPE_POWER_METER = new ThingTypeUID(BINDING_ID, "power-meter");

    /** One battery cell module of the power plant. */
    public static final ThingTypeUID THING_TYPE_BATTERY_MODULE = new ThingTypeUID(BINDING_ID, "battery-module");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE, THING_TYPE_POWER_PLANT,
            THING_TYPE_WALLBOX, THING_TYPE_POWER_METER, THING_TYPE_BATTERY_MODULE);

    /** Channel group ids of the power plant thing. */
    public static final String GROUP_POWER = "power";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_INVERTER = "inverter";
    public static final String GROUP_GRID = "grid";
    public static final String GROUP_SYSTEM = "system";

    /** The energy counters and shares of the completed day, week, month and year. */
    public static final String GROUP_HISTORY = "history";

    /** The battery reserve kept for emergency power. */
    public static final String GROUP_RESERVE = "reserve";

    /** Channel group ids of the wallbox thing. */
    public static final String GROUP_WALLBOX = "wallbox";

    /** Channel group ids of the power meter thing. */
    public static final String GROUP_METER = "meter";

    /** Channel group ids of the battery module thing. */
    public static final String GROUP_MODULE = "module";

    /** Channel ids used by more than one thing type. */
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_ENERGY = "energy";

    private E3DCBindingConstants() {
    }
}
