/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EvccBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Florian Hotze - Initial contribution
 * @author Luca Arnecke - Update to evcc version 0.123.1
 */
@NonNullByDefault
public class EvccBindingConstants {

    public static final String BINDING_ID = "evcc";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SITE = new ThingTypeUID(BINDING_ID, "site");
    public static final ThingTypeUID THING_TYPE_LOADPOINT = new ThingTypeUID(BINDING_ID, "loadpoint");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");
    public static final ThingTypeUID THING_TYPE_PV = new ThingTypeUID(BINDING_ID, "pv");
    public static final ThingTypeUID THING_TYPE_BATTERY = new ThingTypeUID(BINDING_ID, "battery");

    public static final String CHANNEL_PV_POWER = "pvPower";
    public static final String CHANNEL_GRID_POWER = "gridPower";
    public static final String CHANNEL_BATTERY_SOC = "batterySoc";
}
