/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.wakeonlan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AirQualityBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ganesh Ingle - Initial contribution
 */
public class WakeOnLanBindingConstants {

    public static final String BINDING_ID = "wakeonlan";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WOLDEVICE = new ThingTypeUID(BINDING_ID, "wol-device");

    // List of all Channel id's
    public static final String CHANNEL_WAKEUP = "wakeup";
    public static final String CHANNEL_STATUS = "status";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
            Arrays.asList(THING_TYPE_WOLDEVICE));

}
