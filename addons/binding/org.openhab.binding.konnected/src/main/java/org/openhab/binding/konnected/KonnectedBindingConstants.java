/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KonnectedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class KonnectedBindingConstants {

    private static final String BINDING_ID = "konnected";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_MODULE = new ThingTypeUID(BINDING_ID, "module");

    // List of all Channel ids
    public static final String Zone_1 = "zone1";
    public static final String Zone_2 = "zone2";
    public static final String Zone_3 = "zone3";
    public static final String Zone_4 = "zone4";
    public static final String Zone_5 = "zone5";
    public static final String Zone_6 = "zone6";

    // Bridge config properties
    public static final String HOST = "ipAddress";
    public static final String MAC_ADDR = "macAddress";

    // PIN_TO_ZONE array, this array maps an index location as a zone to the corresponding
    // pin location
    public static final Integer[] PIN_TO_ZONE = { 0, 1, 2, 5, 6, 7, 9, 8 };

    // public static final String Auth_Token = "authToken";

    public static final String WEBHOOK_APP = "app_security";
}
