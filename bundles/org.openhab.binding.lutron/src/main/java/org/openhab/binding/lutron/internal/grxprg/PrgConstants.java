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
package org.openhab.binding.lutron.internal.grxprg;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lutron.internal.LutronBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PrgConstants {

    public static final ThingTypeUID THING_TYPE_PRGBRIDGE = new ThingTypeUID(LutronBindingConstants.BINDING_ID,
            "prgbridge");
    public static final ThingTypeUID THING_TYPE_GRAFIKEYE = new ThingTypeUID(LutronBindingConstants.BINDING_ID,
            "grafikeye");

    // Channels for the PRG Interface
    static final String CHANNEL_BUTTONPRESS = "buttonpress";
    static final String CHANNEL_ZONELOWERSTOP = "zonelowerstop";
    static final String CHANNEL_ZONERAISESTOP = "zoneraisestop";
    static final String CHANNEL_TIMECLOCK = "timeclock";
    static final String CHANNEL_SCHEDULE = "schedule";
    static final String CHANNEL_SUNRISE = "sunrise";
    static final String CHANNEL_SUNSET = "sunset";
    static final String CHANNEL_SUPERSEQUENCESTART = "ssstart";
    static final String CHANNEL_SUPERSEQUENCEPAUSE = "sspause";
    static final String CHANNEL_SUPERSEQUENCERESUME = "ssresume";

    static final String CHANNEL_SUPERSEQUENCESTATUS = "ssstatus";
    static final String CHANNEL_SUPERSEQUENCENEXTSTEP = "ssnextstep";
    static final String CHANNEL_SUPERSEQUENCENEXTMIN = "ssnextminute";
    static final String CHANNEL_SUPERSEQUENCENEXTSEC = "ssnextsecond";

    // Channels for the Grafik Eye
    static final String CHANNEL_SCENE = "scene";
    static final String CHANNEL_SCENELOCK = "scenelock";
    static final String CHANNEL_SCENESEQ = "sceneseq";
    static final String CHANNEL_ZONELOCK = "zonelock";
    static final String CHANNEL_ZONELOWER = "zonelower";
    static final String CHANNEL_ZONERAISE = "zoneraise";
    static final String CHANNEL_ZONEFADE = "zonefade";
    static final String CHANNEL_ZONEINTENSITY = "zoneintensity";
    static final String CHANNEL_ZONESHADE = "zoneshade";
}
