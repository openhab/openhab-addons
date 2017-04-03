/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.grxprg;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.lutron.LutronBindingConstants;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Tim Roberts
 */
public class PrgConstants {

    public final static ThingTypeUID THING_TYPE_PRGBRIDGE = new ThingTypeUID(LutronBindingConstants.BINDING_ID,
            "prgbridge");
    public final static ThingTypeUID THING_TYPE_GRAFIKEYE = new ThingTypeUID(LutronBindingConstants.BINDING_ID,
            "grafikeye");

    // Channels for the PRG Interface
    final static String CHANNEL_BUTTONPRESS = "buttonpress";
    final static String CHANNEL_ZONELOWERSTOP = "zonelowerstop";
    final static String CHANNEL_ZONERAISESTOP = "zoneraisestop";
    final static String CHANNEL_TIMECLOCK = "timeclock";
    final static String CHANNEL_SCHEDULE = "schedule";
    final static String CHANNEL_SUNRISE = "sunrise";
    final static String CHANNEL_SUNSET = "sunset";
    final static String CHANNEL_SUPERSEQUENCESTART = "ssstart";
    final static String CHANNEL_SUPERSEQUENCEPAUSE = "sspause";
    final static String CHANNEL_SUPERSEQUENCERESUME = "ssresume";

    final static String CHANNEL_SUPERSEQUENCESTATUS = "ssstatus";
    final static String CHANNEL_SUPERSEQUENCENEXTSTEP = "ssnextstep";
    final static String CHANNEL_SUPERSEQUENCENEXTMIN = "ssnextminute";
    final static String CHANNEL_SUPERSEQUENCENEXTSEC = "ssnextsecond";

    // Channels for the Grafik Eye
    final static String CHANNEL_SCENE = "scene";
    final static String CHANNEL_SCENELOCK = "scenelock";
    final static String CHANNEL_SCENESEQ = "sceneseq";
    final static String CHANNEL_ZONELOCK = "zonelock";
    final static String CHANNEL_ZONELOWER = "zonelower";
    final static String CHANNEL_ZONERAISE = "zoneraise";
    final static String CHANNEL_ZONEFADE = "zonefade";
    final static String CHANNEL_ZONEINTENSITY = "zoneintensity";
    final static String CHANNEL_ZONESHADE = "zoneshade";
}
