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
package org.openhab.binding.amazondashbutton.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AmazonDashButtonBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oliver Libutzki - Initial contribution
 */
@NonNullByDefault
public class AmazonDashButtonBindingConstants {

    public static final String BINDING_ID = "amazondashbutton";

    // List of all Thing Type UIDs
    public static final ThingTypeUID DASH_BUTTON_THING_TYPE = new ThingTypeUID(BINDING_ID, "dashbutton");

    // List of all Channel ids
    public static final String PRESS = "press";

    // Custom Properties
    public static final String PROPERTY_NETWORK_INTERFACE_NAME = "pcapNetworkInterfaceName";
    public static final String PROPERTY_PACKET_INTERVAL = "packetInterval";
}
