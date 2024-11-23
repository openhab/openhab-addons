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
package org.openhab.binding.haassohnpelletstove.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HaasSohnpelletstoveBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class HaasSohnpelletstoveBindingConstants {

    private static final String BINDING_ID = "haassohnpelletstove";

    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");

    public static final String CHANNELISTEMP = "channelIsTemp";
    public static final String CHANNELMODE = "channelMode";
    public static final String CHANNELSPTEMP = "channelSpTemp";
    public static final String CHANNELPOWER = "power";
    public static final String CHANNELECOMODE = "channelEcoMode";
    public static final String CHANNELIGNITIONS = "channelIgnitions";
    public static final String CHANNELMAINTENANCEIN = "channelMaintenanceIn";
    public static final String CHANNELCLEANINGIN = "channelCleaningIn";
    public static final String CHANNELCONSUMPTION = "channelConsumption";
    public static final String CHANNELONTIME = "channelOnTime";
}
