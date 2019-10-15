/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.teleinfo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TeleinfoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoBindingConstants {

    private static final String BINDING_ID = "teleinfo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERIAL_CONTROLLER = new ThingTypeUID(BINDING_ID, "serialcontroller");
    // public static final ThingTypeUID THING_TYPE_REMOTE_IP = new ThingTypeUID(BINDING_ID, "remoteip");

    // List of commons channel ids
    public static final String CHANNEL_LAST_UPDATE = "lastUpdate";
    // List of HC frames channel ids
    public static final String CHANNEL_HC_FRAME_HCHC = "hchc";
    public static final String CHANNEL_HC_FRAME_HCHP = "hchp";
    public static final String CHANNEL_HC_FRAME_HHPHC = "hhphc";
    // List of BASE frames channel ids
    public static final String CHANNEL_BASE_FRAME_BASE = "base";
    // List of TEMPO frames channel ids
    public static final String CHANNEL_TEMPO_FRAME_BBRHPJR = "bbrhpjr";
    public static final String CHANNEL_TEMPO_FRAME_BBRHCJR = "bbrhcjr";
    public static final String CHANNEL_TEMPO_FRAME_BBRHPJW = "bbrhpjw";
    public static final String CHANNEL_TEMPO_FRAME_BBRHCJW = "bbrhcjw";
    public static final String CHANNEL_TEMPO_FRAME_BBRHPJB = "bbrhpjb";
    public static final String CHANNEL_TEMPO_FRAME_BBRHCJB = "bbrhcjb";
    public static final String CHANNEL_TEMPO_FRAME_DEMAIN = "demain";
    public static final String CHANNEL_TEMPO_FRAME_HHPHC = "hhphc";
    // List of EJP frames channel ids
    public static final String CHANNEL_EJP_FRAME_PEJP = "pejp";
    public static final String CHANNEL_EJP_FRAME_EJPHPM = "ejphpm";
    public static final String CHANNEL_EJP_FRAME_EJPHN = "ejphn";
    // List of CBEMM Channel ids
    public static final String CHANNEL_CBEMM_ISOUSC = "isousc";
    public static final String CHANNEL_CBEMM_PTEC = "ptec";
    public static final String CHANNEL_CBEMM_IMAX = "imax";
    public static final String CHANNEL_CBEMM_ADPS = "adps";
    public static final String CHANNEL_CBEMM_IINST = "iinst";
    public static final String CHANNEL_CBEMM_MOTDETAT = "motdetat";
    public static final String CHANNEL_CBEMM_CURRENT_POWER = "currentPower";
    public static final String CHANNEL_CBEMM_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR = "powerFactor";
    // List of CBEMM EVOLUTION ICC Channel ids
    public static final String CHANNEL_CBEMM_EVOLUTION_ICC_PAPP = "papp";
    // List of CBETM Channel ids
    public static final String CHANNEL_CBETM_IINST1 = "iinst1";
    public static final String CHANNEL_CBETM_IINST2 = "iinst2";
    public static final String CHANNEL_CBETM_IINST3 = "iinst3";
    public static final String CHANNEL_CBETM_CURRENT_POWER1 = "currentPower1";
    public static final String CHANNEL_CBETM_CURRENT_POWER2 = "currentPower2";
    public static final String CHANNEL_CBETM_CURRENT_POWER3 = "currentPower3";
    public static final String CHANNEL_CBETM_CURRENT_POWER_CONFIG_PARAMETER_POWERFACTOR = "powerFactor";
    public static final String CHANNEL_CBETM_FRAME_TYPE = "frameType";
    public static final String CHANNEL_CBETM_LONG_ISOUSC = "isousc";
    public static final String CHANNEL_CBETM_LONG_IMAX1 = "imax1";
    public static final String CHANNEL_CBETM_LONG_IMAX2 = "imax2";
    public static final String CHANNEL_CBETM_LONG_IMAX3 = "imax3";
    public static final String CHANNEL_CBETM_LONG_PTEC = "ptec";
    public static final String CHANNEL_CBETM_LONG_PMAX = "pmax";
    public static final String CHANNEL_CBETM_LONG_PAPP = "papp";
    public static final String CHANNEL_CBETM_LONG_MOTDETAT = "motdetat";
    public static final String CHANNEL_CBETM_LONG_PPOT = "ppot";
    public static final String CHANNEL_CBETM_SHORT_ADIR1 = "adir1";
    public static final String CHANNEL_CBETM_SHORT_ADIR2 = "adir2";
    public static final String CHANNEL_CBETM_SHORT_ADIR3 = "adir3";

    public static final ThingTypeUID THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "hc_cbemm_electricitymeter");
    public static final String THING_HC_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO = "adco";

    public static final ThingTypeUID THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "base_cbemm_electricitymeter");
    public static final String THING_BASE_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "ejp_cbemm_electricitymeter");
    public static final String THING_EJP_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "tempo_cbemm_electricitymeter");
    public static final String THING_TEMPO_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBEMM_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "hc_cbemm_evo_icc_electricitymeter");
    public static final String THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO = "adco";

    public static final ThingTypeUID THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "base_cbemm_evo_icc_electricitymeter");
    public static final String THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "ejp_cbemm_evo_icc_electricitymeter");
    public static final String THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "tempo_cbemm_evo_icc_electricitymeter");
    public static final String THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "hc_cbetm_electricitymeter");
    public static final String THING_HC_CBETM_ELECTRICITY_METER_PROPERTY_ADCO = "adco";

    public static final ThingTypeUID THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "base_cbetm_electricitymeter");
    public static final String THING_BASE_CBETM_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBETM_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "ejp_cbetm_electricitymeter");
    public static final String THING_EJP_CBETM_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBETM_ELECTRICITY_METER_PROPERTY_ADCO;

    public static final ThingTypeUID THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "tempo_cbetm_electricitymeter");
    public static final String THING_TEMPO_CBETM_ELECTRICITY_METER_PROPERTY_ADCO = THING_HC_CBETM_ELECTRICITY_METER_PROPERTY_ADCO;

    public final static String ERROR_OFFLINE_SERIAL_NOT_FOUND = "@text/teleinfo.thingstate.serial_notfound";
    public final static String ERROR_OFFLINE_SERIAL_INUSE = "@text/teleinfo.thingstate.serial_inuse";
    public final static String ERROR_OFFLINE_SERIAL_UNSUPPORTED = "@text/teleinfo.thingstate.serial_unsupported";
    public final static String ERROR_OFFLINE_SERIAL_LISTENERS = "@text/teleinfo.thingstate.serial_listeners";
    public final static String ERROR_OFFLINE_CONTROLLER_OFFLINE = "@text/teleinfo.thingstate.controller_offline";

}
