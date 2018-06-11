/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.qivicon.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link QiviconBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Claudius Ellsel - Initial contribution
 */
@NonNullByDefault
public class QiviconBindingConstants {

    private static final String BINDING_ID = "qivicon";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_CONNECTED_DEVICE = new ThingTypeUID(BINDING_ID, "connectedDevice");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_CONNECTED_DEVICE));

    // List of thing Parameters names
    public static final String PARAMETER_NETWORK_ADDRESS = "networkAddress";
    public static final String PARAMETER_AUTHORIZATION_KEY = "authKey";

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperatureChannel";
    public static final String CHANNEL_SWITCH = "switchChannel";
    public static final String CHANNEL_GENERIC_STRING = "genericStringChannel";
}
