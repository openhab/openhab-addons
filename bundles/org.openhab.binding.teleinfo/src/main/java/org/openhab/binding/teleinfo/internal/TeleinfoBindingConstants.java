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
package org.openhab.binding.teleinfo.internal;

import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TeleinfoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
@NonNullByDefault
public class TeleinfoBindingConstants {

    private TeleinfoBindingConstants() {
    }

    private static final String BINDING_ID = "teleinfo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERIAL_CONTROLLER = new ThingTypeUID(BINDING_ID, "serialcontroller");
    public static final String THING_SERIAL_CONTROLLER_CHANNEL_INVALID_FRAME_COUNTER = "invalidFrameCounter";

    // List of commons channel ids
    public static final String CHANNEL_LAST_UPDATE = "lastUpdate";
    // List of HC frames channel ids
    public static final String CHANNEL_HC_FRAME_HCHC = "hchc";
    public static final String CHANNEL_HC_FRAME_HCHP = "hchp";
    public static final String CHANNEL_HHPHC = "hhphc";
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
    public static final String CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_1 = "programmeCircuit1";
    public static final String CHANNEL_TEMPO_FRAME_PROGRAMME_CIRCUIT_2 = "programmeCircuit2";
    // List of EJP frames channel ids
    public static final String CHANNEL_EJP_FRAME_PEJP = "pejp";
    public static final String CHANNEL_EJP_FRAME_EJPHPM = "ejphpm";
    public static final String CHANNEL_EJP_FRAME_EJPHN = "ejphn";
    // List of CBEMM Channel ids
    public static final String CHANNEL_ISOUSC = "isousc";
    public static final String CHANNEL_PTEC = "ptec";
    public static final String CHANNEL_CBEMM_IMAX = "imax";
    public static final String CHANNEL_CBEMM_ADPS = "adps";
    public static final String CHANNEL_CBEMM_IINST = "iinst";
    public static final String CHANNEL_MOTDETAT = "motdetat";
    // List of CBEMM EVOLUTION ICC Channel ids
    public static final String CHANNEL_PAPP = "papp";
    // List of CBETM Channel ids
    public static final String CHANNEL_CBETM_IINST1 = "iinst1";
    public static final String CHANNEL_CBETM_IINST2 = "iinst2";
    public static final String CHANNEL_CBETM_IINST3 = "iinst3";
    public static final String CHANNEL_CBETM_LONG_IMAX1 = "imax1";
    public static final String CHANNEL_CBETM_LONG_IMAX2 = "imax2";
    public static final String CHANNEL_CBETM_LONG_IMAX3 = "imax3";
    public static final String CHANNEL_CBETM_LONG_PMAX = "pmax";
    public static final String CHANNEL_CBETM_LONG_PPOT = "ppot";
    public static final String CHANNEL_CBETM_SHORT_ADIR1 = "adir1";
    public static final String CHANNEL_CBETM_SHORT_ADIR2 = "adir2";
    public static final String CHANNEL_CBETM_SHORT_ADIR3 = "adir3";
    // List of Linky standard mode channel ids
    public static final String CHANNEL_LSM_NGTF = "commonLSMGroup#ngtf";
    public static final String CHANNEL_LSM_LTARF = "commonLSMGroup#ltarf";
    public static final String CHANNEL_LSM_EAST = "commonLSMGroup#east";
    public static final String CHANNEL_LSM_EASF01 = "commonLSMGroup#easf01";
    public static final String CHANNEL_LSM_EASF02 = "commonLSMGroup#easf02";
    public static final String CHANNEL_LSM_EASF03 = "commonLSMGroup#easf03";
    public static final String CHANNEL_LSM_EASF04 = "commonLSMGroup#easf04";
    public static final String CHANNEL_LSM_EASF05 = "commonLSMGroup#easf05";
    public static final String CHANNEL_LSM_EASF06 = "commonLSMGroup#easf06";
    public static final String CHANNEL_LSM_EASF07 = "commonLSMGroup#easf07";
    public static final String CHANNEL_LSM_EASF08 = "commonLSMGroup#easf08";
    public static final String CHANNEL_LSM_EASF09 = "commonLSMGroup#easf09";
    public static final String CHANNEL_LSM_EASF10 = "commonLSMGroup#easf10";
    public static final String CHANNEL_LSM_EASD01 = "commonLSMGroup#easd01";
    public static final String CHANNEL_LSM_EASD02 = "commonLSMGroup#easd02";
    public static final String CHANNEL_LSM_EASD03 = "commonLSMGroup#easd03";
    public static final String CHANNEL_LSM_EASD04 = "commonLSMGroup#easd04";
    public static final String CHANNEL_LSM_IRMS1 = "commonLSMGroup#irms1";
    public static final String CHANNEL_LSM_URMS1 = "commonLSMGroup#urms1";
    public static final String CHANNEL_LSM_PREF = "commonLSMGroup#pref";
    public static final String CHANNEL_LSM_PCOUP = "commonLSMGroup#pcoup";
    public static final String CHANNEL_LSM_SINSTS = "commonLSMGroup#sinsts";
    public static final String CHANNEL_LSM_SMAXSN = "commonLSMGroup#smaxsn";
    public static final String CHANNEL_LSM_SMAXSN_MINUS_1 = "commonLSMGroup#smaxsnMinus1";
    public static final String CHANNEL_LSM_CCASN = "commonLSMGroup#ccasn";
    public static final String CHANNEL_LSM_CCASN_MINUS_1 = "commonLSMGroup#ccasnMinus1";
    public static final String CHANNEL_LSM_UMOY1 = "commonLSMGroup#umoy1";
    public static final String CHANNEL_LSM_STGE = "commonLSMGroup#stge";
    public static final String CHANNEL_LSM_DPM1 = "commonLSMGroup#dpm1";
    public static final String CHANNEL_LSM_FPM1 = "commonLSMGroup#fpm1";
    public static final String CHANNEL_LSM_DPM2 = "commonLSMGroup#dpm2";
    public static final String CHANNEL_LSM_FPM2 = "commonLSMGroup#fpm2";
    public static final String CHANNEL_LSM_DPM3 = "commonLSMGroup#dpm3";
    public static final String CHANNEL_LSM_FPM3 = "commonLSMGroup#fpm3";
    public static final String CHANNEL_LSM_MSG1 = "commonLSMGroup#msg1";
    public static final String CHANNEL_LSM_MSG2 = "commonLSMGroup#msg2";
    public static final String CHANNEL_LSM_PRM = "commonLSMGroup#prm";
    public static final String[] CHANNELS_LSM_RELAIS = IntStream.range(1, 9).mapToObj(i -> "commonLSMGroup#relais" + i)
            .toArray(String[]::new);
    public static final String CHANNEL_LSM_NTARF = "commonLSMGroup#ntarf";
    public static final String CHANNEL_LSM_NJOURF = "commonLSMGroup#njourf";
    public static final String CHANNEL_LSM_NJOURF_PLUS_1 = "commonLSMGroup#njourfPlus1";
    public static final String CHANNEL_LSM_PJOURF_PLUS_1 = "commonLSMGroup#pjourfPlus1";
    public static final String CHANNEL_LSM_PPOINTE = "commonLSMGroup#ppointe";

    public static final String CHANNEL_LSM_IRMS2 = "threePhasedLSMGroup#irms2";
    public static final String CHANNEL_LSM_IRMS3 = "threePhasedLSMGroup#irms3";
    public static final String CHANNEL_LSM_URMS2 = "threePhasedLSMGroup#urms2";
    public static final String CHANNEL_LSM_URMS3 = "threePhasedLSMGroup#urms3";
    public static final String CHANNEL_LSM_SINSTS1 = "threePhasedLSMGroup#sinsts1";
    public static final String CHANNEL_LSM_SINSTS2 = "threePhasedLSMGroup#sinsts2";
    public static final String CHANNEL_LSM_SINSTS3 = "threePhasedLSMGroup#sinsts3";
    public static final String CHANNEL_LSM_SMAXSN1 = "threePhasedLSMGroup#smaxsn1";
    public static final String CHANNEL_LSM_SMAXSN2 = "threePhasedLSMGroup#smaxsn2";
    public static final String CHANNEL_LSM_SMAXSN3 = "threePhasedLSMGroup#smaxsn3";
    public static final String CHANNEL_LSM_SMAXSN1_MINUS_1 = "threePhasedLSMGroup#smaxsn1Minus1";
    public static final String CHANNEL_LSM_SMAXSN2_MINUS_1 = "threePhasedLSMGroup#smaxsn2Minus1";
    public static final String CHANNEL_LSM_SMAXSN3_MINUS_1 = "threePhasedLSMGroup#smaxsn3Minus1";
    public static final String CHANNEL_LSM_UMOY2 = "threePhasedLSMGroup#umoy2";
    public static final String CHANNEL_LSM_UMOY3 = "threePhasedLSMGroup#umoy3";

    public static final String CHANNEL_LSM_EAIT = "producerLSMGroup#eait";
    public static final String CHANNEL_LSM_ERQ1 = "producerLSMGroup#erq1";
    public static final String CHANNEL_LSM_ERQ2 = "producerLSMGroup#erq2";
    public static final String CHANNEL_LSM_ERQ3 = "producerLSMGroup#erq3";
    public static final String CHANNEL_LSM_ERQ4 = "producerLSMGroup#erq4";
    public static final String CHANNEL_LSM_SINSTI = "producerLSMGroup#sinsti";
    public static final String CHANNEL_LSM_SMAXIN = "producerLSMGroup#smaxin";
    public static final String CHANNEL_LSM_SMAXIN_MINUS_1 = "producerLSMGroup#smaxinMinus1";
    public static final String CHANNEL_LSM_CCAIN = "producerLSMGroup#ccain";
    public static final String CHANNEL_LSM_CCAIN_MINUS_1 = "producerLSMGroup#ccainMinus1";

    public static final String CHANNEL_LSM_DATE = "commonLSMGroup#date";
    public static final String CHANNEL_LSM_SMAXSN_DATE = "commonLSMGroup#smaxsnDate";
    public static final String CHANNEL_LSM_SMAXSN_MINUS_1_DATE = "commonLSMGroup#smaxsnMinus1Date";
    public static final String CHANNEL_LSM_CCASN_DATE = "commonLSMGroup#ccasnDate";
    public static final String CHANNEL_LSM_CCASN_MINUS_1_DATE = "commonLSMGroup#ccasnMinus1Date";
    public static final String CHANNEL_LSM_UMOY1_DATE = "commonLSMGroup#umoy1Date";
    public static final String CHANNEL_LSM_DPM1_DATE = "commonLSMGroup#dpm1Date";
    public static final String CHANNEL_LSM_FPM1_DATE = "commonLSMGroup#fpm1Date";
    public static final String CHANNEL_LSM_DPM2_DATE = "commonLSMGroup#dpm2Date";
    public static final String CHANNEL_LSM_FPM2_DATE = "commonLSMGroup#fpm2Date";
    public static final String CHANNEL_LSM_DPM3_DATE = "commonLSMGroup#dpm3Date";
    public static final String CHANNEL_LSM_FPM3_DATE = "commonLSMGroup#fpm3Date";

    public static final String CHANNEL_LSM_SMAXIN_DATE = "producerLSMGroup#smaxinDate";
    public static final String CHANNEL_LSM_SMAXIN_MINUS_1_DATE = "producerLSMGroup#smaxinMinus1Date";
    public static final String CHANNEL_LSM_CCAIN_DATE = "producerLSMGroup#ccainDate";
    public static final String CHANNEL_LSM_CCAIN_MINUS_1_DATE = "producerLSMGroup#ccainMinus1Date";

    public static final String CHANNEL_LSM_SMAXSN1_DATE = "threePhasedLSMGroup#smaxsn1Date";
    public static final String CHANNEL_LSM_SMAXSN2_DATE = "threePhasedLSMGroup#smaxsn2Date";
    public static final String CHANNEL_LSM_SMAXSN3_DATE = "threePhasedLSMGroup#smaxsn3Date";
    public static final String CHANNEL_LSM_SMAXSN1_MINUS_1_DATE = "threePhasedLSMGroup#smaxsn1Minus1Date";
    public static final String CHANNEL_LSM_SMAXSN2_MINUS_1_DATE = "threePhasedLSMGroup#smaxsn2Minus1Date";
    public static final String CHANNEL_LSM_SMAXSN3_MINUS_1_DATE = "threePhasedLSMGroup#smaxsn3Minus1Date";
    public static final String CHANNEL_LSM_UMOY2_DATE = "threePhasedLSMGroup#umoy2Date";
    public static final String CHANNEL_LSM_UMOY3_DATE = "threePhasedLSMGroup#umoy3Date";

    public static final String NOT_A_CHANNEL = "";

    public static final String THING_ELECTRICITY_METER_PROPERTY_ADCO = "adco";

    public static final ThingTypeUID THING_HC_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_hc_electricitymeter");

    public static final ThingTypeUID THING_BASE_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_base_electricitymeter");

    public static final ThingTypeUID THING_EJP_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_ejp_electricitymeter");

    public static final ThingTypeUID THING_TEMPO_CBEMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_tempo_electricitymeter");

    public static final ThingTypeUID THING_HC_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_evolution_icc_hc_electricitymeter");

    public static final ThingTypeUID THING_BASE_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_evolution_icc_base_electricitymeter");

    public static final ThingTypeUID THING_EJP_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_evolution_icc_ejp_electricitymeter");

    public static final ThingTypeUID THING_TEMPO_CBEMM_EVO_ICC_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbemm_evolution_icc_tempo_electricitymeter");

    public static final ThingTypeUID THING_HC_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbetm_hc_electricitymeter");

    public static final ThingTypeUID THING_BASE_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbetm_base_electricitymeter");

    public static final ThingTypeUID THING_EJP_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbetm_ejp_electricitymeter");

    public static final ThingTypeUID THING_TEMPO_CBETM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "cbetm_tempo_electricitymeter");

    public static final ThingTypeUID THING_LSMT_PROD_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "lsmt_prod_electricitymeter");

    public static final ThingTypeUID THING_LSMM_PROD_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "lsmm_prod_electricitymeter");

    public static final ThingTypeUID THING_LSMT_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "lsmt_electricitymeter");

    public static final ThingTypeUID THING_LSMM_ELECTRICITY_METER_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "lsmm_electricitymeter");

    public static final String ERROR_OFFLINE_SERIAL_NOT_FOUND = "@text/teleinfo.thingstate.serial_notfound";
    public static final String ERROR_OFFLINE_SERIAL_INUSE = "@text/teleinfo.thingstate.serial_inuse";
    public static final String ERROR_OFFLINE_SERIAL_UNSUPPORTED = "@text/teleinfo.thingstate.serial_unsupported";
    public static final String ERROR_OFFLINE_SERIAL_LISTENERS = "@text/teleinfo.thingstate.serial_listeners";
    public static final String ERROR_OFFLINE_CONTROLLER_OFFLINE = "@text/teleinfo.thingstate.controller_offline";
    public static final String ERROR_UNKNOWN_RETRY_IN_PROGRESS = "@text/teleinfo.thingstate.controller_unknown_retry_inprogress";
}
