/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

    private static final String BINDING_ID = "haassohnpellestove";

    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");

    public static final String CHANNELisTemp = "channelIsTemp";
    public static final String CHANNELmode = "channelMode";
    public static final String CHANNELspTemp = "channelSpTemp";
    public static final String CHANNELpower = "system.power";
    public static final String CHANNELecoMode = "channelEcoMode";
    public static final String CHANNELignitions = "channelIgnitions";
    public static final String CHANNELmaintenanceIn = "channelMaintenanceIn";
    public static final String CHANNELcleaningIn = "channelCleaningIn";
    public static final String CHANNELconsumption = "channelConsumption";
    public static final String CHANNELonTime = "channelOnTime";
}
