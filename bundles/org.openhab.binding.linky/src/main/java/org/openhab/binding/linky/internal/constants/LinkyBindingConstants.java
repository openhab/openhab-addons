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
    public static final ThingTypeUID THING_TYPE_API_ENEDIS_BRIDGE = new ThingTypeUID(BINDING_ID, "enedis-api");
    public static final ThingTypeUID THING_TYPE_API_MYELECTRICALDATA_BRIDGE = new ThingTypeUID(BINDING_ID,
            "my-electrical-data");
    public static final ThingTypeUID THING_TYPE_WEB_ENEDIS_BRIDGE = new ThingTypeUID(BINDING_ID, "enedis");
    public static final ThingTypeUID THING_TYPE_LINKY = new ThingTypeUID(BINDING_ID, "linky");

    public static final ThingTypeUID THING_TYPE_TEMPO_CALENDAR = new ThingTypeUID(BINDING_ID, "tempo-calendar");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_API_ENEDIS_BRIDGE,
            THING_TYPE_WEB_ENEDIS_BRIDGE, THING_TYPE_API_MYELECTRICALDATA_BRIDGE, THING_TYPE_LINKY,
            THING_TYPE_TEMPO_CALENDAR);

    // Thing properties
    // List of all Channel groups id's
    public static final String PUISSANCE = "puissance";
    public static final String PRM_ID = "prmId";
    public static final String USER_ID = "customerId";
    public static final String AV2_ID = "av2_interne_id";

    public static final String LINKY_REMOTE_DAILY_GROUP = "daily";
    public static final String LINKY_REMOTE_WEEKLY_GROUP = "weekly";
    public static final String LINKY_REMOTE_MONTHLY_GROUP = "monthly";
    public static final String LINKY_REMOTE_YEARLY_GROUP = "yearly";
    public static final String LINKY_TEMPO_CALENDAR_GROUP = "tempo-calendar";
    public static final String LINKY_REMOTE_LOAD_CURVE_GROUP = "load-curve";

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

    public static final String PROPERTY_IDENTITY = "identity";

    public static final String PROPERTY_CONTRACT_LAST_ACTIVATION_DATE = "contractLastActivationdate";
    public static final String PROPERTY_CONTRACT_DISTRIBUTION_TARIFF = "contractDistributionTariff";
    public static final String PROPERTY_CONTRACT_OFF_PEAK_HOURS = "contractOffpeakHours";
    public static final String PROPERTY_CONTRACT_CONTRACT_STATUS = "contractStatus";
    public static final String PROPERTY_CONTRACT_CONTRACT_TYPE = "contractType";
    public static final String PROPERTY_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE = "contractLastdistributionTariffChangedate";
    public static final String PROPERTY_CONTRACT_SEGMENT = "contractSegment";
    public static final String PROPERTY_CONTRACT_SUBSCRIBED_POWER = "contractSubscribedPower";

    public static final String PROPERTY_USAGEPOINT_ID = "usagePointId";
    public static final String PROPERTY_USAGEPOINT_STATUS = "usagePointStatus";
    public static final String PROPERTY_USAGEPOINT_METER_TYPE = "usagePointMeterType";

    public static final String PROPERTY_USAGEPOINT_METER_ADDRESS_CITY = "usagePointCity";
    public static final String PROPERTY_USAGEPOINT_METER_ADDRESS_COUNTRY = "usagePointCountry";
    public static final String PROPERTY_USAGEPOINT_METER_ADDRESS_POSTAL_CODE = "usagePointPostalCode";
    public static final String PROPERTY_USAGEPOINT_METER_ADDRESS_STREET = "usagePointStreet";

    public static final String PROPERTY_CONTACT_MAIL = "contactMail";
    public static final String PROPERTY_CONTACT_PHONE = "contactPhone";

    // Authorization related Servlet and resources aliases.
    public static final String LINKY_ALIAS = "/connectlinky";
    public static final String LINKY_IMG_ALIAS = "/img";

    // List of all Channel ids

    public static final Currency CURRENCY_EUR = Currency.getInstance("EUR");

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
