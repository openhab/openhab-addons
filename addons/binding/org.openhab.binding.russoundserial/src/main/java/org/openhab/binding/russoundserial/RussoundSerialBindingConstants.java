/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russoundserial;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RussoundSerialBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jason Holmes - Initial contribution
 */
public class RussoundSerialBindingConstants {
    private static final String BINDING_ID = "russoundserial";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SERIAL_BRIDGE = new ThingTypeUID(BINDING_ID, "russound-serial-bridge");
    public static final ThingTypeUID ZONE_HANDLER = new ThingTypeUID(BINDING_ID, "zone");

    // ZONE CHANNELS
    public static final String CHANNEL_ZONEPOWER = "power"; // OFF/ON/MASTER
    public static final String CHANNEL_ZONEVOLUME = "volume"; // 0-100
    public static final String CHANNEL_ZONESOURCE = "source"; // 0-12

    public static final String CHANNEL_ZONEBASS = "bass"; // -10 to 10
    public static final String CHANNEL_ZONETREBLE = "treble"; // -10 to 10
    public static final String CHANNEL_ZONEBALANCE = "balance"; // -10 to 10
    public static final String CHANNEL_ZONELOUDNESS = "loudness"; // OFF/ON
    public static final String CHANNEL_ZONETURNONVOLUME = "turnonvolume"; // 0 to 100
}
