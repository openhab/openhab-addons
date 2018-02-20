/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinksmarthome;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DLinkSmartHomeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mike Major - Initial contribution
 */
public class DLinkSmartHomeBindingConstants {

    public static final String BINDING_ID = "dlinksmarthome";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DCHS150 = new ThingTypeUID(BINDING_ID, "DCH-S150");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_DCHS150);

    // Motion trigger channel
    public static final String MOTION = "motion";
}
