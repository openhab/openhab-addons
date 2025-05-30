/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
    public static final ThingTypeUID THING_TYPE_BIOSMART = new ThingTypeUID(BINDING_ID, "biosmart");
    public static final ThingTypeUID THING_TYPE_POWERCHIP = new ThingTypeUID(BINDING_ID, "powerchip");
    public static final ThingTypeUID THING_TYPE_POWERCORN = new ThingTypeUID(BINDING_ID, "powercorn");
    public static final ThingTypeUID THING_TYPE_BIOCOM = new ThingTypeUID(BINDING_ID, "biocom");
    public static final ThingTypeUID THING_TYPE_PRO = new ThingTypeUID(BINDING_ID, "pro");
    public static final ThingTypeUID THING_TYPE_THERM = new ThingTypeUID(BINDING_ID, "therm");
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");

    public static final String GROUP_CONTROL = "control#";

    // List of all Channel ids
    public static final String CHANNEL_CONTROL_BOILERAPPROVAL = GROUP_CONTROL + "boiler-approval";
    public static final String CHANNEL_CONTROL_PROGRAM = GROUP_CONTROL + "program";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM0 = GROUP_CONTROL + "heat-circ-program-0";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM1 = GROUP_CONTROL + "heat-circ-program-1";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM2 = GROUP_CONTROL + "heat-circ-program-2";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM3 = GROUP_CONTROL + "heat-circ-program-3";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM4 = GROUP_CONTROL + "heat-circ-program-4";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM5 = GROUP_CONTROL + "heat-circ-program-5";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM6 = GROUP_CONTROL + "heat-circ-program-6";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM7 = GROUP_CONTROL + "heat-circ-program-7";
    public static final String CHANNEL_CONTROL_HEATCIRCPROGRAM8 = GROUP_CONTROL + "heat-circ-program-8";
    public static final String CHANNEL_CONTROL_WWHEAT0 = GROUP_CONTROL + "ww-heat-0";
    public static final String CHANNEL_CONTROL_WWHEAT1 = GROUP_CONTROL + "ww-heat-1";
    public static final String CHANNEL_CONTROL_WWHEAT2 = GROUP_CONTROL + "ww-heat-2";
    public static final String CHANNEL_CONTROL_EXTRAWWHEAT0 = GROUP_CONTROL + "extra-ww-heat-0";
    public static final String CHANNEL_CONTROL_EXTRAWWHEAT1 = GROUP_CONTROL + "extra-ww-heat-1";
    public static final String CHANNEL_CONTROL_EXTRAWWHEAT2 = GROUP_CONTROL + "extra-ww-heat-2";

    public static final List<String> STATIC_CHANNEL_IDS = Arrays.asList(CHANNEL_CONTROL_BOILERAPPROVAL,
            CHANNEL_CONTROL_PROGRAM, CHANNEL_CONTROL_HEATCIRCPROGRAM0, CHANNEL_CONTROL_HEATCIRCPROGRAM1,
            CHANNEL_CONTROL_HEATCIRCPROGRAM2, CHANNEL_CONTROL_HEATCIRCPROGRAM3, CHANNEL_CONTROL_HEATCIRCPROGRAM4,
            CHANNEL_CONTROL_HEATCIRCPROGRAM5, CHANNEL_CONTROL_HEATCIRCPROGRAM6, CHANNEL_CONTROL_HEATCIRCPROGRAM7,
            CHANNEL_CONTROL_HEATCIRCPROGRAM8, CHANNEL_CONTROL_WWHEAT0, CHANNEL_CONTROL_WWHEAT1, CHANNEL_CONTROL_WWHEAT2,
            CHANNEL_CONTROL_EXTRAWWHEAT0, CHANNEL_CONTROL_EXTRAWWHEAT1, CHANNEL_CONTROL_EXTRAWWHEAT2);

    public static final List<String> STATIC_CHANNEL_IDS_WOBOILERAPP = Arrays.asList(CHANNEL_CONTROL_PROGRAM,
            CHANNEL_CONTROL_HEATCIRCPROGRAM0, CHANNEL_CONTROL_HEATCIRCPROGRAM1, CHANNEL_CONTROL_HEATCIRCPROGRAM2,
            CHANNEL_CONTROL_HEATCIRCPROGRAM3, CHANNEL_CONTROL_HEATCIRCPROGRAM4, CHANNEL_CONTROL_HEATCIRCPROGRAM5,
            CHANNEL_CONTROL_HEATCIRCPROGRAM6, CHANNEL_CONTROL_HEATCIRCPROGRAM7, CHANNEL_CONTROL_HEATCIRCPROGRAM8,
            CHANNEL_CONTROL_WWHEAT0, CHANNEL_CONTROL_WWHEAT1, CHANNEL_CONTROL_WWHEAT2, CHANNEL_CONTROL_EXTRAWWHEAT0,
            CHANNEL_CONTROL_EXTRAWWHEAT1, CHANNEL_CONTROL_EXTRAWWHEAT2);

    public static final String GROUP_STATUS = "status#";

    public static final String PARAMETER_BOILERAPPROVAL = "boilerApproval";
    public static final String PARAMETER_PROGRAM = "program";
    public static final String PARAMETER_HEATCIRCPROGRAM = "heatCircProgram";
    public static final String PARAMETER_WWHEAT = "wwHeat";
    public static final String PARAMETER_EXTRAWWHEAT = "extraWwHeat";

    public static final String DAQDATA_URL = "/daqdata.cgi";
    public static final String DAQDESC_URL = "/daqdesc.cgi";
    public static final String DAQEXTDESC_URL = "/ext/daqdesc.cgi";
    public static final String PARSET_URL = "/ext/parset.cgi";
}
