/**
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
package org.openhab.binding.linky.internal;

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
    public static final ThingTypeUID THING_TYPE_D2L_BRIDGE = new ThingTypeUID(BINDING_ID, "d2l");
    public static final ThingTypeUID THING_TYPE_LINKY = new ThingTypeUID(BINDING_ID, "linky");
    public static final ThingTypeUID THING_TYPE_LINKY_LOCAL = new ThingTypeUID(BINDING_ID, "linky-local");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_API_ENEDIS_BRIDGE,
            THING_TYPE_API_WEB_ENEDIS_BRIDGE, THING_TYPE_API_MYELECTRICALDATA_BRIDGE, THING_TYPE_LINKY,
            THING_TYPE_D2L_BRIDGE, THING_TYPE_LINKY_LOCAL);

    // Thing properties
    // List of all Channel groups id's
    public static final String PUISSANCE = "puissance";
    public static final String PRM_ID = "prmId";
    public static final String USER_ID = "customerId";
    public static final String AV2_ID = "av2_interne_id";

    public static final String LINKY_REMOTE_GROUP_DAILY = "linky-remote-daily";
    public static final String LINKY_REMOTE_GROUP_WEEKLY = "linky-remote-weekly";
    public static final String LINKY_REMOTE_GROUP_MONTHLY = "linky-remote-monthly";
    public static final String LINKY_REMOTE_GROUP_YEARLY = "linky-remote-yearly";
    public static final String LINKY_REMOTE_GROUP_MAIN = "linky-remote-main";
    public static final String LINKY_REMOTE_GROUP_TEMPO = "linky-remote-tempo";
    public static final String LINKY_REMOTE_GROUP_LOAD_CURVE = "linky-remote-load-curve";

    public static final String LINKY_LOCAL_MAIN_GROUP = "linky-local-main";
    public static final String LINKY_LOCAL_3PHASE_GROUP = "linky-local-3phase";
    public static final String LINKY_LOCAL_PRODUCER_GROUP = "linky-local-producer";
    public static final String LINKY_LOCAL_CALC_GROUP = "linky-local-calc";

    // List of all Channel id's
    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_MAX_POWER = "maxPower";
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

    public static final String CHANNEL_TEMPO_TODAY_INFO = "tempoInfoToday";
    public static final String CHANNEL_TEMPO_TOMORROW_INFO = "tempoInfoTomorrow";
    public static final String CHANNEL_TEMPO_TEMPO_INFO_TIME_SERIES = "tempoInfoTimeSeries";

    public static final String CHANNEL_IDENTITY = "identity";

    public static final String CHANNEL_CONTRACT_SUBSCRIBED_POWER = "contractSubscribedPower";
    public static final String CHANNEL_CONTRACT_LAST_ACTIVATION_DATE = "contractLastActivationDate";
    public static final String CHANNEL_CONTRACT_DISTRIBUTION_TARIFF = "contractDistributionTariff";
    public static final String CHANNEL_CONTRACT_OFF_PEAK_HOURS = "contractOffpeakHours";
    public static final String CHANNEL_CONTRACT_CONTRACT_STATUS = "contractStatus";
    public static final String CHANNEL_CONTRACT_CONTRACT_TYPE = "contractType";
    public static final String CHANNEL_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE = "contractLastDistributionTariffChangeDate";
    public static final String CHANNEL_CONTRACT_SEGMENT = "contractSegment";

    public static final String CHANNEL_USAGEPOINT_ID = "usagePointId";
    public static final String CHANNEL_USAGEPOINT_STATUS = "usagePointStatus";
    public static final String CHANNEL_USAGEPOINT_METER_TYPE = "usagePointMeterType";

    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_CITY = "usagePointAddressCity";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_COUNTRY = "usagePointAddressCountry";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_INSEE_CODE = "usagePointAddressInseeCode";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_POSTAL_CODE = "usagePointAddressPostalCode";
    public static final String CHANNEL_USAGEPOINT_METER_ADDRESS_STREET = "usagePointAddressStreet";

    public static final String CHANNEL_CONTACT_MAIL = "contactMail";
    public static final String CHANNEL_CONTACT_PHONE = "contactPhone";

    public static final String CHANNEL_NONE = "";

    public static final String CHANNEL_ID_D2L = "id_d2l";
    public static final String CHANNEL_TYPE_TRAME = "type_trame";
    public static final String CHANNEL_DATE_FIRMWARE = "date_firmware";

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

    public static final String CHANNEL_IRMS1 = "irms1";
    public static final String CHANNEL_IRMS2 = "irms2";
    public static final String CHANNEL_IRMS3 = "irms3";

    public static final String CHANNEL_URMS1 = "urms1";
    public static final String CHANNEL_URMS2 = "urms2";
    public static final String CHANNEL_URMS3 = "urms3";

    public static final String CHANNEL_PREF = "pref";
    public static final String CHANNEL_PCOUP = "pcoup";

    public static final String CHANNEL_SINSTS = "sinsts";
    public static final String CHANNEL_SINSTS1 = "sinsts";
    public static final String CHANNEL_SINSTS2 = "sinsts";
    public static final String CHANNEL_SINSTS3 = "sinsts";

    public static final String CHANNEL_SMAXSN = "smaxsn";
    public static final String CHANNEL_SMAXSN1 = "smaxsn1";
    public static final String CHANNEL_SMAXSN2 = "smaxsn2";
    public static final String CHANNEL_SMAXSN3 = "smaxsn3";

    public static final String CHANNEL_SMAXSN_DATE = "smaxsnDate";
    public static final String CHANNEL_SMAXSN1_DATE = "smaxsn1Date";
    public static final String CHANNEL_SMAXSN2_DATE = "smaxsn2Date";
    public static final String CHANNEL_SMAXSN3_DATE = "smaxsn3Date";

    public static final String CHANNEL_SMAXSN_MINUS_1 = "smaxsnMinus1";
    public static final String CHANNEL_SMAXSN1_MINUS_1 = "smaxsn1Minus1";
    public static final String CHANNEL_SMAXSN2_MINUS_1 = "smaxsn2Minus1";
    public static final String CHANNEL_SMAXSN3_MINUS_1 = "smaxsn3Minus1";

    public static final String CHANNEL_SMAXSN_MINUS_1_DATE = "smaxsnMinus1Date";
    public static final String CHANNEL_SMAXSN1_MINUS_1_DATE = "smaxsn1Minus1Date";
    public static final String CHANNEL_SMAXSN2_MINUS_1_DATE = "smaxsn2Minus1Date";
    public static final String CHANNEL_SMAXSN3_MINUS_1_DATE = "smaxsn3Minus1Date";

    public static final String CHANNEL_SINSTI = "sinsti";

    public static final String CHANNEL_SMAXIN = "smaxin";
    public static final String CHANNEL_SMAXIN_DATE = "smaxinDate";

    public static final String CHANNEL_SMAXIN_MINUS_1 = "smaxinMinus1";
    public static final String CHANNEL_SMAXIN_MINUS_1_DATE = "smaxinMinus1Date";

    public static final String CHANNEL_CCASN = "ccasn";
    public static final String CHANNEL_CCASN_DATE = "ccasnDate";

    public static final String CHANNEL_CCASN_MINUS_1 = "ccasnMinus1";
    public static final String CHANNEL_CCASN_MINUS_1_DATE = "ccasnMinus1Date";

    public static final String CHANNEL_CCAIN = "ccain";
    public static final String CHANNEL_CCAIN_DATE = "ccainDate";

    public static final String CHANNEL_CCAIN_MINUS_1 = "ccainMinus1";
    public static final String CHANNEL_CCAIN_MINUS_1_DATE = "ccainMinus1Date";

    public static final String CHANNEL_UMOY1 = "umoy1";
    public static final String CHANNEL_UMOY1_DATE = "umoy1Date";

    public static final String CHANNEL_UMOY2 = "umoy2";
    public static final String CHANNEL_UMOY2_DATE = "umoy2Date";

    public static final String CHANNEL_UMOY3 = "umoy3";
    public static final String CHANNEL_UMOY3_DATE = "umoy3Date";

    public static final String CHANNEL_STGE = "stge";

    public static final String CHANNEL_DPM1 = "dpm1";
    public static final String CHANNEL_DPM1_DATE = "dpm1Date";

    public static final String CHANNEL_FPM1 = "fpm1";
    public static final String CHANNEL_FPM1_DATE = "fpm1Date";

    public static final String CHANNEL_DPM2 = "dpm2";
    public static final String CHANNEL_DPM2_DATE = "dpm2Date";

    public static final String CHANNEL_FPM2 = "fpm2";
    public static final String CHANNEL_FPM2_DATE = "fpm2Date";

    public static final String CHANNEL_DPM3 = "dpm3";
    public static final String CHANNEL_DPM3_DATE = "dpm3Date";

    public static final String CHANNEL_FPM3 = "fpm3";
    public static final String CHANNEL_FPM3_DATE = "fpm3Date";

    public static final String CHANNEL_MSG1 = "msg1";
    public static final String CHANNEL_MSG2 = "msg2";

    public static final String CHANNEL_PRM = "prm";

    public static final String CHANNEL_NTARF = "ntarf";
    public static final String CHANNEL_NJOURF = "njourf";
    public static final String CHANNEL_NJOURF_PLUS_1 = "njourfPlus1";
    public static final String CHANNEL_PJOURF_PLUS_1 = "pjourfPlus1";
    public static final String CHANNEL_PPOINTE = "ppointe";

    // Authorization related Servlet and resources aliases.
    public static final String LINKY_ALIAS = "/connectlinky";
    public static final String LINKY_IMG_ALIAS = "/img";

    /**
     * Smartthings scopes needed by this binding to work.
     */
    public static final String LINKY_SCOPES = "am_application_scope default";
}
