/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.autelis.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AutelisBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AutelisBindingConstants {

    public static final String BINDING_ID = "autelis";

    // poolcontrol is here for backwards compatibility before we had separate things for jandy and pentair
    public static final ThingTypeUID POOLCONTROL_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "poolcontrol");
    public static final ThingTypeUID PENTAIR_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "pentair");
    public static final ThingTypeUID JANDY_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "jandy");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(POOLCONTROL_THING_TYPE_UID,
            PENTAIR_THING_TYPE_UID, JANDY_THING_TYPE_UID);

    public static final String CMD_LIGHTS = "lightscmd";
    public static final String CMD_REBOOT = "reboot";
    public static final String CMD_EQUIPMENT = "equipment";
    public static final String CMD_TEMP = "temp";
    public static final String CMD_CHEM = "chem";
    public static final String CMD_PUMPS = "pumps";

}
