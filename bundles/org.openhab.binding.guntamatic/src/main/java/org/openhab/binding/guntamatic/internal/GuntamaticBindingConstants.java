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

    // List of all Channel ids
    public static final String CHANNEL_CONTROLBOILERAPPROVAL = "controlBoilerApproval";
    public static final String CHANNEL_CONTROLPROGRAM = "controlProgram";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM0 = "controlHeatCircProgram0";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM1 = "controlHeatCircProgram1";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM2 = "controlHeatCircProgram2";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM3 = "controlHeatCircProgram3";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM4 = "controlHeatCircProgram4";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM5 = "controlHeatCircProgram5";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM6 = "controlHeatCircProgram6";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM7 = "controlHeatCircProgram7";
    public static final String CHANNEL_CONTROLHEATCIRCPROGRAM8 = "controlHeatCircProgram8";
    public static final String CHANNEL_CONTROLWWHEAT0 = "controlWwHeat0";
    public static final String CHANNEL_CONTROLWWHEAT1 = "controlWwHeat1";
    public static final String CHANNEL_CONTROLWWHEAT2 = "controlWwHeat2";
    public static final String CHANNEL_CONTROLEXTRAWWHEAT0 = "controlExtraWwHeat0";
    public static final String CHANNEL_CONTROLEXTRAWWHEAT1 = "controlExtraWwHeat1";
    public static final String CHANNEL_CONTROLEXTRAWWHEAT2 = "controlExtraWwHeat2";

    public static final List<String> STATIC_CHANNEL_IDS = Arrays.asList(CHANNEL_CONTROLBOILERAPPROVAL,
            CHANNEL_CONTROLPROGRAM, CHANNEL_CONTROLHEATCIRCPROGRAM0, CHANNEL_CONTROLHEATCIRCPROGRAM1,
            CHANNEL_CONTROLHEATCIRCPROGRAM2, CHANNEL_CONTROLHEATCIRCPROGRAM3, CHANNEL_CONTROLHEATCIRCPROGRAM4,
            CHANNEL_CONTROLHEATCIRCPROGRAM5, CHANNEL_CONTROLHEATCIRCPROGRAM6, CHANNEL_CONTROLHEATCIRCPROGRAM7,
            CHANNEL_CONTROLHEATCIRCPROGRAM8, CHANNEL_CONTROLWWHEAT0, CHANNEL_CONTROLWWHEAT1, CHANNEL_CONTROLWWHEAT2,
            CHANNEL_CONTROLEXTRAWWHEAT0, CHANNEL_CONTROLEXTRAWWHEAT1, CHANNEL_CONTROLEXTRAWWHEAT2);

    public static final List<String> STATIC_CHANNEL_IDS_WOBOILERAPP = Arrays.asList(CHANNEL_CONTROLPROGRAM,
            CHANNEL_CONTROLHEATCIRCPROGRAM0, CHANNEL_CONTROLHEATCIRCPROGRAM1, CHANNEL_CONTROLHEATCIRCPROGRAM2,
            CHANNEL_CONTROLHEATCIRCPROGRAM3, CHANNEL_CONTROLHEATCIRCPROGRAM4, CHANNEL_CONTROLHEATCIRCPROGRAM5,
            CHANNEL_CONTROLHEATCIRCPROGRAM6, CHANNEL_CONTROLHEATCIRCPROGRAM7, CHANNEL_CONTROLHEATCIRCPROGRAM8,
            CHANNEL_CONTROLWWHEAT0, CHANNEL_CONTROLWWHEAT1, CHANNEL_CONTROLWWHEAT2, CHANNEL_CONTROLEXTRAWWHEAT0,
            CHANNEL_CONTROLEXTRAWWHEAT1, CHANNEL_CONTROLEXTRAWWHEAT2);

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
