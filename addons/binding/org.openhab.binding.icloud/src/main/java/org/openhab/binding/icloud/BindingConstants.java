/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class BindingConstants {

    private static final String BINDING_ID = "icloud";

    public static final String BRIDGE_ID = "account";
    public static final String DEVICE_ID = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ICLOUD = new ThingTypeUID(BINDING_ID, BRIDGE_ID);
    public static final ThingTypeUID THING_TYPE_ICLOUDDEVICE = new ThingTypeUID(BINDING_ID, DEVICE_ID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_ICLOUD, THING_TYPE_ICLOUDDEVICE));

    // List of all Channel IDs
    public static final String BATTERY_STATUS = "batteryStatus";
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String FIND_MY_PHONE = "findMyPhone";
    public static final String LOCATION = "location";
    public static final String LOCATION_ACCURACY = "locationAccuracy";
    public static final String LOCATION_LASTUPDATE = "locationLastUpdate";
    public static final String DEVICE_NAME = "deviceName";

    // Device properties
    public static final String DEVICE_PROPERTY_IDHASH = "deviceIdHash";
    public static final String DEVICE_PROPERTY_ID = "deviceId";
}
