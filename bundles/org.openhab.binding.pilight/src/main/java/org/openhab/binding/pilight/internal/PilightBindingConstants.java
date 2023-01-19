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
package org.openhab.binding.pilight.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PilightBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Röllin - Initial contribution
 * @author Niklas Dörfler - Port pilight binding to openHAB 3 + add device discovery
 */
@NonNullByDefault
public class PilightBindingConstants {

    public static final String BINDING_ID = "pilight";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");

    // List of property names
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_NAME = "name";

    // List of all Channel ids
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_DIMLEVEL = "dimlevel";
}
