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
 * Channel groups, channel ids and thing type ids of the binding.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCBindingConstants {

    public static final String BINDING_ID = "e3dc";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final ThingTypeUID THING_TYPE_POWER_PLANT = new ThingTypeUID(BINDING_ID, "power-plant");

    public static final ThingTypeUID THING_TYPE_WALLBOX = new ThingTypeUID(BINDING_ID, "wallbox");

    public static final ThingTypeUID THING_TYPE_POWER_METER = new ThingTypeUID(BINDING_ID, "power-meter");

    public static final ThingTypeUID THING_TYPE_BATTERY = new ThingTypeUID(BINDING_ID, "battery");

    public static final ThingTypeUID THING_TYPE_BATTERY_MODULE = new ThingTypeUID(BINDING_ID, "battery-module");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE, THING_TYPE_POWER_PLANT,
            THING_TYPE_WALLBOX, THING_TYPE_POWER_METER, THING_TYPE_BATTERY, THING_TYPE_BATTERY_MODULE);

    public static final String GROUP_POWER = "power";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_INVERTER = "inverter";
    public static final String GROUP_GRID = "grid";
    public static final String GROUP_SYSTEM = "system";

    /** The system channels: the switch says whether any message is stored, the text channel lists them as JSON. */
    public static final String CHANNEL_HAS_MESSAGES = "has-messages";
    public static final String CHANNEL_MESSAGES = "messages";

    /** The power limits and the charging strategy of the battery. */
    public static final String GROUP_BATTERY_LIMITS = "battery-limits";

    /** The operating mode of the energy manager, including a temporary override. */
    public static final String GROUP_BATTERY_MODE = "battery-mode";

    /** The time windows during which the battery is locked against charging or discharging. */
    public static final String GROUP_IDLE_PERIODS = "idle-periods";

    /** The order in which the power plant assigns solar power to the battery and the car. */
    public static final String GROUP_WALLBOX_PRIORITY = "wallbox-priority";

    public static final String GROUP_MANUAL_BATTERY_CHARGE = "manual-battery-charge";

    public static final String GROUP_STATISTICS = "statistics";

    public static final String GROUP_HISTORY = "history";

    public static final String GROUP_EMERGENCY = "emergency";

    /** The request of the wallbox data, which its channel groups share. */
    public static final String GROUP_WALLBOX = "wallbox";

    public static final String GROUP_WALLBOX_STATUS_CONTROL = "status-control";
    public static final String GROUP_WALLBOX_SESSION = "session";
    public static final String GROUP_WALLBOX_COUNTERS = "counters";

    public static final String GROUP_METER = "meter";

    public static final String GROUP_PACK = "pack";

    public static final String GROUP_MODULE = "module";

    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_ENERGY = "energy";

    private E3DCBindingConstants() {
    }
}
