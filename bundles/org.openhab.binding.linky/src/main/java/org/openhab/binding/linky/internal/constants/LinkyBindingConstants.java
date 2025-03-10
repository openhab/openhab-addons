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
package org.openhab.binding.linky.internal.constants;

import java.util.Currency;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LinkyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API *
 */
@NonNullByDefault
public class LinkyBindingConstants {

    public static final String BINDING_ID = "linky";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API_ENEDIS_BRIDGE = new ThingTypeUID(BINDING_ID, "enedis");
    public static final ThingTypeUID THING_TYPE_API_WEB_ENEDIS_BRIDGE = new ThingTypeUID(BINDING_ID, "enedis-web");
    public static final ThingTypeUID THING_TYPE_API_MYELECTRICALDATA_BRIDGE = new ThingTypeUID(BINDING_ID,
            "my-electrical-data");
    public static final ThingTypeUID THING_TYPE_SERIAL_BRIDGE = new ThingTypeUID(BINDING_ID, "serial");
    public static final ThingTypeUID THING_TYPE_D2L_BRIDGE = new ThingTypeUID(BINDING_ID, "d2l");
    public static final ThingTypeUID THING_TYPE_LINKY = new ThingTypeUID(BINDING_ID, "linky");
    public static final ThingTypeUID THING_TYPE_LINKY_LOCAL = new ThingTypeUID(BINDING_ID, "linky-local");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BASE = new ThingTypeUID(BINDING_ID, "base");
    public static final ThingTypeUID THING_TYPE_HPHC = new ThingTypeUID(BINDING_ID, "hphc");
    public static final ThingTypeUID THING_TYPE_TEMPO = new ThingTypeUID(BINDING_ID, "tempo");

    public static final ThingTypeUID THING_TYPE_TEMPO_CALENDAR = new ThingTypeUID(BINDING_ID, "tempo-calendar");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_API_ENEDIS_BRIDGE,
            THING_TYPE_API_WEB_ENEDIS_BRIDGE, THING_TYPE_API_MYELECTRICALDATA_BRIDGE, THING_TYPE_LINKY,
            THING_TYPE_D2L_BRIDGE, THING_TYPE_SERIAL_BRIDGE, THING_TYPE_LINKY_LOCAL, THING_TYPE_BASE, THING_TYPE_HPHC,
            THING_TYPE_TEMPO, THING_TYPE_TEMPO_CALENDAR);

    // Thing properties
    // List of all Channel groups id's
    public static final String PUISSANCE = "puissance";
    public static final String PRM_ID = "prmId";
    public static final String USER_ID = "customerId";
    public static final String AV2_ID = "av2_interne_id";

    public static final String LINKY_REMOTE_DAILY_GROUP = "linky-remote-daily";
    public static final String LINKY_REMOTE_WEEKLY_GROUP = "linky-remote-weekly";
    public static final String LINKY_REMOTE_MONTHLY_GROUP = "linky-remote-monthly";
    public static final String LINKY_REMOTE_YEARLY_GROUP = "linky-remote-yearly";
    public static final String LINKY_REMOTE_MAIN_GROUP = "linky-remote-main";
    public static final String LINKY_TEMPO_CALENDAR_GROUP = "linky-tempo-calendar";
    public static final String LINKY_REMOTE_LOAD_CURVE_GROUP = "linky-remote-load-curve";

    public static final String LINKY_LOCAL_MAIN_GROUP = "linky-local-main";
    public static final String LINKY_LOCAL_3PHASE_GROUP = "linky-local-3phase";
    public static final String LINKY_LOCAL_PRODUCER_GROUP = "linky-local-producer";
    public static final String LINKY_LOCAL_CALC_GROUP = "linky-local-calc";

    // List of all Channel id's
    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_MAX_POWER = "max-power";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_TIMESTAMP_CHANNEL = "power";

    public static final String CHANNEL_DAY_MINUS_1 = "yesterday";
    public static final String CHANNEL_DAY_MINUS_2 = "day-2";
    public static final String CHANNEL_DAY_MINUS_3 = "day-3";

    public static final String CHANNEL_PEAK_POWER_DAY_MINUS_1 = "power";
    public static final String CHANNEL_PEAK_POWER_TS_DAY_MINUS_1 = "timestamp";

    public static final String CHANNEL_PEAK_POWER_DAY_MINUS_2 = "power-2";
    public static final String CHANNEL_PEAK_POWER_TS_DAY_MINUS_2 = "timestamp-2";

    public static final String CHANNEL_PEAK_POWER_DAY_MINUS_3 = "power-3";
    public static final String CHANNEL_PEAK_POWER_TS_DAY_MINUS_3 = "timestamp-3";

    public static final String CHANNEL_WEEK_MINUS_0 = "thisWeek";
    public static final String CHANNEL_WEEK_MINUS_1 = "lastWeek";
    public static final String CHANNEL_WEEK_MINUS_2 = "week-2";

    public static final String CHANNEL_MONTH_MINUS_0 = "thisMonth";
    public static final String CHANNEL_MONTH_MINUS_1 = "lastMonth";
    public static final String CHANNEL_MONTH_MINUS_2 = "month-2";

    public static final String CHANNEL_YEAR_MINUS_0 = "thisYear";
    public static final String CHANNEL_YEAR_MINUS_1 = "lastYear";
    public static final String CHANNEL_YEAR_MINUS_2 = "year-2";

    public static final String CHANNEL_TEMPO_TODAY_INFO = "tempo-info-today";
    public static final String CHANNEL_TEMPO_TOMORROW_INFO = "tempo-info-tomorrow";
    public static final String CHANNEL_TEMPO_TEMPO_INFO_TIME_SERIES = "tempo-info-timeseries";

    public static final String CHANNEL_IDENTITY = "identity";

    public static final String CHANNEL_CONTRACT_SUBSCRIBED_POWER = "contract-subscribed-power";
    public static final String CHANNEL_CONTRACT_LAST_ACTIVATION_DATE = "contract-last-activationdate";
    public static final String CHANNEL_CONTRACT_DISTRIBUTION_TARIFF = "contract-distribution-tariff";
    public static final String CHANNEL_CONTRACT_OFF_PEAK_HOURS = "contract-offpeak-hours";
    public static final String CHANNEL_CONTRACT_CONTRACT_STATUS = "contract-status";
    public static final String CHANNEL_CONTRACT_CONTRACT_TYPE = "contract-type";
    public static final String CHANNEL_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE = "contract-lastdistribution-tariff-changedate";
    public static final String CHANNEL_CONTRACT_SEGMENT = "contract-segment";

    public static final String CHANNEL_USAGEPOINT_ID = "usage-point-id";
    public static final String CHANNEL_USAGEPOINT_STATUS = "usage-point-status";
    public static final String CHANNEL_USAGEPOINT_METER_TYPE = "usage-point-meter-type";

    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_CITY = "usage-point-address-city";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_COUNTRY = "usage-point-address-country";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_INSEE_CODE = "usage-point-address-insee-code";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_POSTAL_CODE = "usage-point-address-postal-code";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_STREET = "usage-point-address-street";

    public static final String CHANNEL_CONTACT_MAIL = "contact-mail";
    public static final String CHANNEL_CONTACT_PHONE = "contact-phone";

    public static final String CHANNEL_NONE = "";

    public static final String CHANNEL_ID_D2L = "id-d2l";
    public static final String CHANNEL_TYPE_TRAME = "type-trame";
    public static final String CHANNEL_DATE_FIRMWARE = "date-firmware";

    // List of Linky historical mode channel ids
    public static final String CHANNEL_ADCO = "adco";

    // List of Linky standard mode channel ids
    public static final String CHANNEL_ADSC = "adsc";
    public static final String CHANNEL_VTIC = "vtic";
    public static final String CHANNEL_DATE = "date";
    public static final String CHANNEL_NGTF = "ngtf";
    public static final String CHANNEL_LTARF = "ltarf";

    public static final String CHANNEL_EAST = "east";

    public static final String CHANNEL_EASF01 = "easf01";
    public static final String CHANNEL_EASF02 = "easf02";
    public static final String CHANNEL_EASF03 = "easf03";
    public static final String CHANNEL_EASF04 = "easf04";
    public static final String CHANNEL_EASF05 = "easf05";
    public static final String CHANNEL_EASF06 = "easf06";
    public static final String CHANNEL_EASF07 = "easf07";
    public static final String CHANNEL_EASF08 = "easf08";
    public static final String CHANNEL_EASF09 = "easf09";
    public static final String CHANNEL_EASF10 = "easf10";

    public static final String CHANNEL_EASD01 = "easd01";
    public static final String CHANNEL_EASD02 = "easd02";
    public static final String CHANNEL_EASD03 = "easd03";
    public static final String CHANNEL_EASD04 = "easd04";

    public static final String CHANNEL_EAIT = "eait";

    public static final String CHANNEL_ERQ1 = "erq1";
    public static final String CHANNEL_ERQ2 = "erq2";
    public static final String CHANNEL_ERQ3 = "erq3";
    public static final String CHANNEL_ERQ4 = "erq4";

    public static final String CHANNEL_IRMS1F = "irms1f";

    public static final String CHANNEL_IRMS1 = "irms1";
    public static final String CHANNEL_IRMS2 = "irms2";
    public static final String CHANNEL_IRMS3 = "irms3";

    public static final String CHANNEL_URMS1 = "urms1";
    public static final String CHANNEL_URMS2 = "urms2";
    public static final String CHANNEL_URMS3 = "urms3";

    public static final String CHANNEL_PREF = "pref";
    public static final String CHANNEL_PCOUP = "pcoup";

    public static final String CHANNEL_COSPHI = "cosphi";
    public static final String CHANNEL_SACTIVE = "sactive";
    public static final String CHANNEL_SREACTIVE = "sreactive";

    public static final String CHANNEL_SINSTS = "sinsts";
    public static final String CHANNEL_SINSTS1 = "sinsts1";
    public static final String CHANNEL_SINSTS2 = "sinsts2";
    public static final String CHANNEL_SINSTS3 = "sinsts3";

    public static final String CHANNEL_SMAXSN = "smaxsn";
    public static final String CHANNEL_SMAXSN1 = "smaxsn1";
    public static final String CHANNEL_SMAXSN2 = "smaxsn2";
    public static final String CHANNEL_SMAXSN3 = "smaxsn3";

    public static final String CHANNEL_SMAXSN_DATE = "smaxsn-date";
    public static final String CHANNEL_SMAXSN1_DATE = "smaxsn1-date";
    public static final String CHANNEL_SMAXSN2_DATE = "smaxsn2-date";
    public static final String CHANNEL_SMAXSN3_DATE = "smaxsn3-date";

    public static final String CHANNEL_SMAXSN_MINUS_1 = "smaxsn-minus1";
    public static final String CHANNEL_SMAXSN1_MINUS_1 = "smaxsn1-minus1";
    public static final String CHANNEL_SMAXSN2_MINUS_1 = "smaxsn2-minus1";
    public static final String CHANNEL_SMAXSN3_MINUS_1 = "smaxsn3-minus1";

    public static final String CHANNEL_SMAXSN_MINUS_1_DATE = "smaxsn-minus1Date";
    public static final String CHANNEL_SMAXSN1_MINUS_1_DATE = "smaxsn1-minus1Date";
    public static final String CHANNEL_SMAXSN2_MINUS_1_DATE = "smaxsn2-minus1Date";
    public static final String CHANNEL_SMAXSN3_MINUS_1_DATE = "smaxsn3-minus1Date";

    public static final String CHANNEL_SINSTI = "sinsti";

    public static final String CHANNEL_SMAXIN = "smaxin";
    public static final String CHANNEL_SMAXIN_DATE = "smaxin-date";

    public static final String CHANNEL_SMAXIN_MINUS_1 = "smaxin-minus1";
    public static final String CHANNEL_SMAXIN_MINUS_1_DATE = "smaxin-minus1-date";

    public static final String CHANNEL_CCASN = "ccasn";
    public static final String CHANNEL_CCASN_DATE = "ccasn-date";

    public static final String CHANNEL_CCASN_MINUS_1 = "ccasn-minus1";
    public static final String CHANNEL_CCASN_MINUS_1_DATE = "ccasn-minus1-date";

    public static final String CHANNEL_CCAIN = "ccain";
    public static final String CHANNEL_CCAIN_DATE = "ccain-date";

    public static final String CHANNEL_CCAIN_MINUS_1 = "ccain-minus1";
    public static final String CHANNEL_CCAIN_MINUS_1_DATE = "ccain-minus1-date";

    public static final String CHANNEL_UMOY1 = "umoy1";
    public static final String CHANNEL_UMOY1_DATE = "umoy1-date";

    public static final String CHANNEL_UMOY2 = "umoy2";
    public static final String CHANNEL_UMOY2_DATE = "umoy2-date";

    public static final String CHANNEL_UMOY3 = "umoy3";
    public static final String CHANNEL_UMOY3_DATE = "umoy3-date";

    public static final String CHANNEL_STGE = "stge";

    public static final String CHANNEL_RELAIS = "relais";

    public static final String CHANNEL_DPM1 = "dpm1";
    public static final String CHANNEL_DPM1_DATE = "dpm1-date";

    public static final String CHANNEL_FPM1 = "fpm1";
    public static final String CHANNEL_FPM1_DATE = "fpm1-date";

    public static final String CHANNEL_DPM2 = "dpm2";
    public static final String CHANNEL_DPM2_DATE = "dpm2-date";

    public static final String CHANNEL_FPM2 = "fpm2";
    public static final String CHANNEL_FPM2_DATE = "fpm2-date";

    public static final String CHANNEL_DPM3 = "dpm3";
    public static final String CHANNEL_DPM3_DATE = "dpm3-date";

    public static final String CHANNEL_FPM3 = "fpm3";
    public static final String CHANNEL_FPM3_DATE = "fpm3-date";

    public static final String CHANNEL_MSG1 = "msg1";
    public static final String CHANNEL_MSG2 = "msg2";

    public static final String CHANNEL_PRM = "prm";

    public static final String CHANNEL_NTARF = "ntarf";
    public static final String CHANNEL_NJOURF = "njourf";
    public static final String CHANNEL_NJOURF_PLUS_1 = "njourf-plus1";
    public static final String CHANNEL_PJOURF_PLUS_1 = "pjourf-plus1";
    public static final String CHANNEL_PPOINTE = "ppointe";

    public static final String CHANNEL_PJOURF_IDX = "pjourf";
    public static final String CHANNEL_PPOINTE_IDX = "ppointe";

    public static final String CHANNEL_CONTACT_SEC = "contact-sec";
    public static final String CHANNEL_CACHE = "cache";
    public static final String CHANNEL_CUT_OFF = "cut-off";
    public static final String CHANNEL_OVER_VOLTAGE = "over-voltage";
    public static final String CHANNEL_EXCEEDING_POWER = "exceeding-power";
    public static final String CHANNEL_FUNCTION = "function";
    public static final String CHANNEL_DIRECTION = "direction";
    public static final String CHANNEL_SUPPLIER_RATE = "supplier-rate";
    public static final String CHANNEL_DISTRIBUTOR_RATE = "distributor-rate";
    public static final String CHANNEL_CLOCK = "clock";
    public static final String CHANNEL_PLC = "plc";
    public static final String CHANNEL_COM_OUTPUT = "outputcom";
    public static final String CHANNEL_PLC_STATE = "plc-state";
    public static final String CHANNEL_PLC_SYNCHRO = "plc-synchro";
    public static final String CHANNEL_TEMPO_TODAY = "tempo-today";
    public static final String CHANNEL_TEMPO_TOMORROW = "tempo-tomorrow";
    public static final String CHANNEL_MOVING_TIPS_ADVICE = "moving-tips";
    public static final String CHANNEL_MOVING_TIPS = "advice-moving-tips";

    // List of EJP frames channel ids
    public static final String CHANNEL_EJP_PEJP = "pejp";
    public static final String CHANNEL_EJP_EJPHPM = "ejphpm";
    public static final String CHANNEL_EJP_EJPHN = "ejphn";

    // List of BASE frames channel ids
    public static final String CHANNEL_BASE_BASE = "base";

    // List of HC frames channel ids
    public static final String CHANNEL_HC_HCHC = "hchc";
    public static final String CHANNEL_HC_HCHP = "hchp";
    public static final String CHANNEL_HHPHC = "hhphc";

    // List of TEMPO frames channel ids
    public static final String CHANNEL_TEMPO_BBRHPJR = "bbrhpjr";
    public static final String CHANNEL_TEMPO_BBRHCJR = "bbrhcjr";
    public static final String CHANNEL_TEMPO_BBRHPJW = "bbrhpjw";
    public static final String CHANNEL_TEMPO_BBRHCJW = "bbrhcjw";
    public static final String CHANNEL_TEMPO_BBRHPJB = "bbrhpjb";
    public static final String CHANNEL_TEMPO_BBRHCJB = "bbrhcjb";
    public static final String CHANNEL_TEMPO_DEMAIN = "demain";
    public static final String CHANNEL_TEMPO_PROGRAMME_CIRCUIT_1 = "programmeCircuit1";
    public static final String CHANNEL_TEMPO_PROGRAMME_CIRCUIT_2 = "programmeCircuit2";

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

    // Authorization related Servlet and resources aliases.
    public static final String LINKY_ALIAS = "/connectlinky";
    public static final String LINKY_IMG_ALIAS = "/img";

    // List of all Channel ids
    public static final String CHANNEL_TARIFF_START = "tariff-start";
    public static final String CHANNEL_FIXED_HT = "fixed-ht";
    public static final String CHANNEL_FIXED_TTC = "fixed-ttc";
    public static final String CHANNEL_VARIABLE_HT = "variable-ht";
    public static final String CHANNEL_VARIABLE_TTC = "variable-ttc";

    public static final String CHANNEL_HC_HT = "hc-ht";
    public static final String CHANNEL_HC_TTC = "hc-ttc";
    public static final String CHANNEL_HP_HT = "hp-ht";
    public static final String CHANNEL_HP_TTC = "hp-ttc";

    public static final String CHANNEL_RED_HC_HT = "red-hc-ht";
    public static final String CHANNEL_RED_HC_TTC = "red-hc-ttc";
    public static final String CHANNEL_RED_HP_HT = "red-hp-ht";
    public static final String CHANNEL_RED_HP_TTC = "red-hp-ttc";

    public static final String CHANNEL_WHITE_HC_HT = "white-hc-ht";
    public static final String CHANNEL_WHITE_HC_TTC = "white-hc-ttc";
    public static final String CHANNEL_WHITE_HP_HT = "white-hp-ht";
    public static final String CHANNEL_WHITE_HP_TTC = "white-hp-ttc";

    public static final String CHANNEL_BLUE_HC_HT = "blue-hc-ht";
    public static final String CHANNEL_BLUE_HC_TTC = "blue-hc-ttc";
    public static final String CHANNEL_BLUE_HP_HT = "blue-hp-ht";
    public static final String CHANNEL_BLUE_HP_TTC = "blue-hp-ttc";

    public static final Currency CURRENCY_EUR = Currency.getInstance("EUR");

    public static final String NOT_A_CHANNEL = "";

    public static final String THING_SERIAL_CONTROLLER_CHANNEL_INVALID_FRAME_COUNTER = "invalidFrameCounter";

    public static final String THING_ELECTRICITY_METER_PROPERTY_ADCO = "adco";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SERIAL_CONTROLLER = new ThingTypeUID(BINDING_ID, "serialcontroller");

    public static final String ERROR_OFFLINE_SERIAL_NOT_FOUND = "@text/linky.thingstate.serial_notfound";
    public static final String ERROR_OFFLINE_SERIAL_INUSE = "@text/linky.thingstate.serial_inuse";
    public static final String ERROR_OFFLINE_SERIAL_UNSUPPORTED = "@text/linky.thingstate.serial_unsupported";
    public static final String ERROR_OFFLINE_SERIAL_LISTENERS = "@text/linky.thingstate.serial_listeners";
    public static final String ERROR_OFFLINE_CONTROLLER_OFFLINE = "@text/linky.thingstate.controller_offline";
    public static final String ERROR_UNKNOWN_RETRY_IN_PROGRESS = "@text/linky.thingstate.controller_unknown_retry_inprogress";

    /**
     * Smartthings scopes needed by this binding to work.
     */
    public static final String LINKY_SCOPES = "am_application_scope default";
}
