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
    // public static final ThingTypeUID THING_TYPE_ALARM = new ThingTypeUID(BINDING_ID, "alarm");

    // List of all Channel ids
    public static final String CHANNEL_BV = "bv";
    public static final String CHANNEL_ScV = "ScV";
    public static final String CHANNEL_ScS = "ScS";
    public static final String CHANNEL_YT = "YT";
    public static final String CHANNEL_YY = "YY";
    public static final String CHANNEL_ScW = "ScW";
    public static final String CHANNEL_secondsAgo = "secondsAgo";

    // Config Params
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String REFRESH_RATE_SECONDS = "refreshRate";
    public static final String INSTALLID = "installation-id";
    public static final String INSTANCEID = "instance-id";

}
