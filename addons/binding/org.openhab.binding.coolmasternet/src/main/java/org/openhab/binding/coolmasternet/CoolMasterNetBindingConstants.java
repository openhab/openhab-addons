/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link coolmasternetBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Angus Gratton - Initial contribution
 */
@NonNullByDefault
public class CoolMasterNetBindingConstants {

    public static final String BINDING_ID = "coolmasternet";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CONTROLLER = new ThingTypeUID(BINDING_ID, "controller");
    public static final ThingTypeUID THING_TYPE_HVAC = new ThingTypeUID(BINDING_ID, "hvac");

    // List of all Channel ids
    public static final String ON = "on";
    public static final String MODE = "mode";
    public static final String SET_TEMP = "set_temp";
    public static final String FAN_SPEED = "fan_speed";
    public static final String LOUVRE = "louvre_angle";
    public static final String CURRENT_TEMP = "current_temp";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream.of(THING_TYPE_CONTROLLER, THING_TYPE_HVAC)
            .collect(Collectors.toSet());

}
