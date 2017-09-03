/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link iCloudBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrik Gfeller - Initial contribution
 */
public class iCloudBindingConstants {

    private static final String BINDING_ID = "icloud";

    private static final String BRIDGE_ID = "bridge";
    private static final String DEVICE_ID = "device";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ICLOUD = new ThingTypeUID(BINDING_ID, BRIDGE_ID);
    public static final ThingTypeUID THING_TYPE_ICLOUDDEVICE = new ThingTypeUID(BINDING_ID, DEVICE_ID);

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_ICLOUD,
            THING_TYPE_ICLOUDDEVICE);

    // List of all Channel IDs
    public static final String NUMBEROFDEVICES = "NumberOfDevices";
    public static final String OWNER = "Owner";
    public static final String HTTPSTATUSCODE = "HttpStatusCode";

    public static final String BATTERYSTATUS = "BatteryStatus";
    public static final String BATTERYLEVEL = "BatteryLevel";
    public static final String FINDMYPHONE = "FindMyPhone";
    public static final String LOCATION = "Location";
    public static final String LOCATIONACCURACY = "LocationAccuracy";
    public static final String DISTANCEFROMHOME = "DistanceFromHome";
    public static final String LASTUPDATE = "LastUpdate";
    public static final String ADDRESSSTREET = "AddressStreet";
    public static final String ADDRESSCITY = "AddressCity";
    public static final String ADDRESSCOUNTRY = "AddressCountry";
    public static final String FORMATTEDADDRESS = "FormattedAddress";

}
