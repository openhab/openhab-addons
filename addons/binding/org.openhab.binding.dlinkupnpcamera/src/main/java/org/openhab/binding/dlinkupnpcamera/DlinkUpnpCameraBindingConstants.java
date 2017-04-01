/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinkupnpcamera;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link DlinkUpnpCameraBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Yacine Ndiaye
 * @author Antoine Blanc
 * @author Christopher Law
 */
public class DlinkUpnpCameraBindingConstants {

    public static final String BINDING_ID = "dlinkupnpcamera";

    // List of all Thing Type UIDs
    public final static ThingTypeUID CAMERA_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "camera");

    public static final Set<ThingTypeUID> SUPPORTED_KNOWN_THING_TYPES_UIDS = Sets.newHashSet(CAMERA_THING_TYPE_UID);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            SUPPORTED_KNOWN_THING_TYPES_UIDS);

    // List of all Channel ids
    public final static String PAN = "pan";
    public final static String TILT = "tilt";
    public final static String IMAGE = "image";

}