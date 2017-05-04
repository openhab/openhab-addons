/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hs110;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link HS110Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Fischer - Initial contribution
 */
public class HS110BindingConstants {

    public static final String BINDING_ID = "hs110";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HS100 = new ThingTypeUID(BINDING_ID, "HS100");
    public static final ThingTypeUID THING_TYPE_HS110 = new ThingTypeUID(BINDING_ID, "HS110");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_HS100,
            THING_TYPE_HS110);

    // List of all Channel ids
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SYSINFO = "sysinfo";
    public static final String CHANNEL_WATTAGE = "wattage";
    public static final String CHANNEL_TOTAL = "total";

    public static final String CONFIG_IP = "ip";
    public static final String CONFIG_REFRESH = "refresh";

}
