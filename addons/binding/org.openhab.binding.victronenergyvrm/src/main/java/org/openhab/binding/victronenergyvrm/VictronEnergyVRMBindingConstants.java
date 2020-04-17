/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.victronenergyvrm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VictronEnergyVRMBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Samuel Lueckoff - Initial contribution
 */
@NonNullByDefault
public class VictronEnergyVRMBindingConstants {

    private static final String BINDING_ID = "victronenergyvrm";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INSTALLATION = new ThingTypeUID(BINDING_ID, "sc");
    public static final ThingTypeUID THING_TYPE_INSTALLATION_BM = new ThingTypeUID(BINDING_ID, "bm");
    // public static final ThingTypeUID THING_TYPE_ALARM = new ThingTypeUID(BINDING_ID, "alarm");

    // List of all Channel ids SC
    public static final String CHANNEL_BV = "bv";
    public static final String CHANNEL_ScV = "ScV";
    public static final String CHANNEL_ScS = "ScS";
    public static final String CHANNEL_YT = "YT";
    public static final String CHANNEL_YY = "YY";
    public static final String CHANNEL_ScW = "ScW";
    public static final String CHANNEL_secondsAgo = "secondsAgo";

    // List of all Channel ids BM
    public static final String CHANNEL_BmV = "BmV";
    public static final String CHANNEL_BmVS = "BmVS";
    public static final String CHANNEL_BmI = "BmI";
    public static final String CHANNEL_BmCE = "BmCE";
    public static final String CHANNEL_BmSOC = "BmSOC";
    public static final String CHANNEL_BmTTG = "BmTTG";
    public static final String CHANNEL_BmAL = "BmAL";
    public static final String CHANNEL_BmAH = "BmAH";
    public static final String CHANNEL_BmALS = "BmALS";
    public static final String CHANNEL_BmAHS = "BmAHS";
    public static final String CHANNEL_BmASoc = "BmASoc";
    public static final String CHANNEL_BmALT = "BmALT";
    public static final String CHANNEL_BmAHT = "BmAHT";
    public static final String CHANNEL_BmAM = "BmAM";
    public static final String CHANNEL_BmSecondsAgo = "BmSecondsAgo";

    // Config Params
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String REFRESH_RATE_SECONDS = "refreshRate";
    public static final String INSTALLID = "installation-id";
    public static final String INSTANCEID = "instance-id";

}
