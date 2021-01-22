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
package org.openhab.binding.haassohnpelletoven.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HaassohnpelletstoveBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class HaassohnpelletstoveBindingConstants {

    private static final String BINDING_ID = "haassohnpelletoven";

    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");

    public static final String CHANNEL_isTemp = "channelIsTemp";
    public static final String CHANNEL_mode = "channelMode";
    public static final String CHANNEL_spTemp = "channelSpTemp";
    public static final String CHANNEL_prg = "channelPrg";
    public static final String CHANNEL_ecoMode = "channelEcoMode";
    public static final String CHANNEL_ignitions = "channelIgnitions";
    public static final String CHANNEL_maintenanceIn = "channelMaintenanceIn";
    public static final String CHANNEL_cleaningIn = "channelCleaningIn";
    public static final String CHANNEL_consumption = "channelConsumption";
    public static final String CHANNEL_onTime = "channelOnTime";
}
