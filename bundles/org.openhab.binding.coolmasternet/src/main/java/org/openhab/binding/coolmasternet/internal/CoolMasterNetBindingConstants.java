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
package org.openhab.binding.coolmasternet.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Defines constants used across the whole binding.
 *
 * @author Angus Gratton - Initial contribution
 */
@NonNullByDefault
public class CoolMasterNetBindingConstants {

    public static final String BINDING_ID = "coolmasternet";

    // list of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_HVAC = new ThingTypeUID(BINDING_ID, "hvac");

    // list of all Channel ids
    public static final String ON = "on";
    public static final String MODE = "mode";
    public static final String SET_TEMP = "set_temp";
    public static final String FAN_SPEED = "fan_speed";
    public static final String LOUVRE = "louvre";
    public static final String CURRENT_TEMP = "current_temp";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_CONTROLLER, THING_TYPE_HVAC)
            .collect(Collectors.toSet());
}
