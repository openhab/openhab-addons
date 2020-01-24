/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.revogismartstripcontrol.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RevogiSmartStripControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class RevogiSmartStripControlBindingConstants {

    private static final String BINDING_ID = "revogismartstrip";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SMART_STRIP_THING_TYPE = new ThingTypeUID(BINDING_ID, "smartstrip");

    // List of all Channel ids
    public static final String PLUG_1 = "plug1";
    public static final String PLUG_2 = "plug2";
    public static final String PLUG_3 = "plug3";
    public static final String PLUG_4 = "plug4";
    public static final String PLUG_5 = "plug5";
    public static final String PLUG_6 = "plug6";
    public static final String ALL_PLUGS = "allplugs";
    public static final String STATS = "stats";
}
