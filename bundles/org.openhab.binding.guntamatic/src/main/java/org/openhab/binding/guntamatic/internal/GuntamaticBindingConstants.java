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
package org.openhab.binding.guntamatic.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GuntamaticBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Weger Michael - Initial contribution
 */
@NonNullByDefault
public class GuntamaticBindingConstants {

    public static final String BINDING_ID = "guntamatic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BIOSTAR = new ThingTypeUID(BINDING_ID, "biostar");
    public static final ThingTypeUID THING_TYPE_POWERCHIP = new ThingTypeUID(BINDING_ID, "powerchip");
    public static final ThingTypeUID THING_TYPE_POWERCORN = new ThingTypeUID(BINDING_ID, "powercorn");
    public static final ThingTypeUID THING_TYPE_BIOCOM = new ThingTypeUID(BINDING_ID, "biocom");
    public static final ThingTypeUID THING_TYPE_PRO = new ThingTypeUID(BINDING_ID, "pro");
    public static final ThingTypeUID THING_TYPE_THERM = new ThingTypeUID(BINDING_ID, "therm");

    // List of all Channel ids
    public static final String CHANNEL_SETBOILERAPPROVAL = "setboilerapproval";
    public static final String CHANNEL_SETPROGRAM = "setprogram";
    public static final String CHANNEL_SETHEATCIRCPROGRAM0 = "setheatcircprogram0";
    public static final String CHANNEL_SETHEATCIRCPROGRAM1 = "setheatcircprogram1";
    public static final String CHANNEL_SETHEATCIRCPROGRAM2 = "setheatcircprogram2";
    public static final String CHANNEL_SETHEATCIRCPROGRAM3 = "setheatcircprogram3";
    public static final String CHANNEL_SETHEATCIRCPROGRAM4 = "setheatcircprogram4";
    public static final String CHANNEL_SETHEATCIRCPROGRAM5 = "setheatcircprogram5";
    public static final String CHANNEL_SETHEATCIRCPROGRAM6 = "setheatcircprogram6";
    public static final String CHANNEL_SETHEATCIRCPROGRAM7 = "setheatcircprogram7";
    public static final String CHANNEL_SETHEATCIRCPROGRAM8 = "setheatcircprogram8";
    public static final String CHANNEL_SETWWHEAT0 = "setwwheat0";
    public static final String CHANNEL_SETWWHEAT1 = "setwwheat1";
    public static final String CHANNEL_SETWWHEAT2 = "setwwheat2";
    public static final String CHANNEL_SETEXTRAWWHEAT0 = "setextrawwheat0";
    public static final String CHANNEL_SETEXTRAWWHEAT1 = "setextrawwheat1";
    public static final String CHANNEL_SETEXTRAWWHEAT2 = "setextrawwheat2";

    public static final List<String> CHANNELIDS = Arrays.asList(CHANNEL_SETBOILERAPPROVAL, CHANNEL_SETPROGRAM,
            CHANNEL_SETHEATCIRCPROGRAM0, CHANNEL_SETHEATCIRCPROGRAM1, CHANNEL_SETHEATCIRCPROGRAM2,
            CHANNEL_SETHEATCIRCPROGRAM3, CHANNEL_SETHEATCIRCPROGRAM4, CHANNEL_SETHEATCIRCPROGRAM5,
            CHANNEL_SETHEATCIRCPROGRAM6, CHANNEL_SETHEATCIRCPROGRAM7, CHANNEL_SETHEATCIRCPROGRAM8, CHANNEL_SETWWHEAT0,
            CHANNEL_SETWWHEAT1, CHANNEL_SETWWHEAT2, CHANNEL_SETEXTRAWWHEAT0, CHANNEL_SETEXTRAWWHEAT1,
            CHANNEL_SETEXTRAWWHEAT2);

    public static final String PARAMETER_BOILERAPPROVAL = "boilerapproval";
    public static final String PARAMETER_PROGRAM = "program";
    public static final String PARAMETER_HEATCIRCPROGRAM = "heatcircprogram";
    public static final String PARAMETER_WWHEAT = "wwheat";
    public static final String PARAMETER_EXTRAWWHEAT = "extrawwheat";

    public static final String DAQDATA_URL = "/daqdata.cgi";
    public static final String DAQDESC_URL = "/daqdesc.cgi";
    public static final String DAQEXTDESC_URL = "/ext/daqdesc.cgi";
    public static final String PARSET_URL = "/ext/parset.cgi";
}
