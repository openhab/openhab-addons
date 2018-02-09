/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AmazonDashButtonBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class AmazonDashButtonBindingConstants {

    public static final String BINDING_ID = "amazondashbutton";

    // List of all Thing Type UIDs
    public static final ThingTypeUID DASH_BUTTON_THING_TYPE = new ThingTypeUID(BINDING_ID, "dashbutton");

    // List of all Channel ids
    public static final String PRESS = "press";

    // Custom Properties
    public static final String PROPERTY_MAC_ADDRESS = "macAddress";
    public static final String PROPERTY_NETWORK_INTERFACE_NAME = "pcapNetworkInterfaceName";
    public static final String PROPERTY_PACKET_INTERVAL = "packetInterval";

}
