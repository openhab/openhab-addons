/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.revogi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RevogiSmartStripControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andi Bräu - Initial contribution
 */
@NonNullByDefault
public class RevogiSmartStripControlBindingConstants {

    private static final String BINDING_ID = "revogi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SMART_STRIP_THING_TYPE = new ThingTypeUID(BINDING_ID, "smartstrip");

    // List of all Channel ids
    public static final String PLUG_1_SWITCH = "plug1#switch";
    public static final String PLUG_2_SWITCH = "plug2#switch";
    public static final String PLUG_3_SWITCH = "plug3#switch";
    public static final String PLUG_4_SWITCH = "plug4#switch";
    public static final String PLUG_5_SWITCH = "plug5#switch";
    public static final String PLUG_6_SWITCH = "plug6#switch";
    public static final String ALL_PLUGS = "overallPlug#switch";

    public static final String SERIAL_NUMBER = "serialNumber";
}
