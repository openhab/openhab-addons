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

<<<<<<< HEAD
import java.util.Arrays;
import java.util.List;

=======
>>>>>>> inital commit of skeleton
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

<<<<<<< HEAD
    public static final String BINDING_ID = "guntamatic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BIOSTAR = new ThingTypeUID(BINDING_ID, "biostar");
    public static final ThingTypeUID THING_TYPE_POWERCHIP = new ThingTypeUID(BINDING_ID, "powerchip");
    public static final ThingTypeUID THING_TYPE_POWERCORN = new ThingTypeUID(BINDING_ID, "powercorn");
    public static final ThingTypeUID THING_TYPE_BIOCOM = new ThingTypeUID(BINDING_ID, "biocom");
    public static final ThingTypeUID THING_TYPE_PRO = new ThingTypeUID(BINDING_ID, "pro");
    public static final ThingTypeUID THING_TYPE_THERM = new ThingTypeUID(BINDING_ID, "therm");

    // List of all Channel ids
    public static final String CHANNEL_CONTROLBOILERAPPROVAL = "controlboilerapproval";
    public static final String CHANNEL_CONTROLPROGRAM = "controlprogram";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM0 = "controlheatcircprogram0";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM1 = "controlheatcircprogram1";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM2 = "controlheatcircprogram2";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM3 = "controlheatcircprogram3";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM4 = "controlheatcircprogram4";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM5 = "controlheatcircprogram5";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM6 = "controlheatcircprogram6";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM7 = "controlheatcircprogram7";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM8 = "controlheatcircprogram8";
    public static final String CHANNEL_CONTROLWWHEAT0 = "controlwwheat0";
    public static final String CHANNEL_CONTROLWWHEAT1 = "controlwwheat1";
    public static final String CHANNEL_CONTROLWWHEAT2 = "controlwwheat2";
    public static final String CHANNEL_CONTROLEXTRAWWHEAT0 = "controlextrawwheat0";
    public static final String CHANNEL_CONTROLEXTRAWWHEAT1 = "controlextrawwheat1";
    public static final String CHANNEL_CONTROLEXTRAWWHEAT2 = "controlextrawwheat2";

    public static final List<String> CHANNELIDS = Arrays.asList(CHANNEL_CONTROLBOILERAPPROVAL, CHANNEL_CONTROLPROGRAM,
            CHANNEL_CONTROLHEATCIRCPROGRAM0, CHANNEL_CONTROLHEATCIRCPROGRAM1, CHANNEL_CONTROLHEATCIRCPROGRAM2,
            CHANNEL_CONTROLHEATCIRCPROGRAM3, CHANNEL_CONTROLHEATCIRCPROGRAM4, CHANNEL_CONTROLHEATCIRCPROGRAM5,
            CHANNEL_CONTROLHEATCIRCPROGRAM6, CHANNEL_CONTROLHEATCIRCPROGRAM7, CHANNEL_CONTROLHEATCIRCPROGRAM8,
            CHANNEL_CONTROLWWHEAT0, CHANNEL_CONTROLWWHEAT1, CHANNEL_CONTROLWWHEAT2, CHANNEL_CONTROLEXTRAWWHEAT0,
            CHANNEL_CONTROLEXTRAWWHEAT1, CHANNEL_CONTROLEXTRAWWHEAT2);

    public static final String PARAMETER_BOILERAPPROVAL = "boilerapproval";
    public static final String PARAMETER_PROGRAM = "program";
    public static final String PARAMETER_HEATCIRCPROGRAM = "heatcircprogram";
    public static final String PARAMETER_WWHEAT = "wwheat";
    public static final String PARAMETER_EXTRAWWHEAT = "extrawwheat";

    public static final String DAQDATA_URL = "/daqdata.cgi";
    public static final String DAQDESC_URL = "/daqdesc.cgi";
    public static final String DAQEXTDESC_URL = "/ext/daqdesc.cgi";
    public static final String PARSET_URL = "/ext/parset.cgi";
=======
    private static final String BINDING_ID = "guntamatic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";
>>>>>>> inital commit of skeleton
}
