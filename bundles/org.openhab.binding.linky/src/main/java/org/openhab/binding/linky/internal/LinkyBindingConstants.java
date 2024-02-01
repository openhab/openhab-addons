/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public static final ThingTypeUID THING_TYPE_LINKY = new ThingTypeUID(BINDING_ID, "linky");

    // Thing properties
    public static final String PUISSANCE = "puissance";
    public static final String PRM_ID = "prmId";
    public static final String USER_ID = "customerId";

    // List of all Channel id's
    public static final String DAY_MINUS_1 = "daily#day-1";
    public static final String DAY_MINUS_2 = "daily#day-2";
    public static final String DAY_MINUS_3 = "daily#day-3";

    public static final String PEAK_POWER_DAY_MINUS_1 = "daily#maxPower-day-1";
    public static final String PEAK_POWER_TS_DAY_MINUS_1 = "daily#maxPowerTs-day-1";
    public static final String PEAK_POWER_DAY_MINUS_2 = "daily#maxPower-day-2";
    public static final String PEAK_POWER_TS_DAY_MINUS_2 = "daily#maxPowerTs-day-2";
    public static final String PEAK_POWER_DAY_MINUS_3 = "daily#maxPower-day-3";
    public static final String PEAK_POWER_TS_DAY_MINUS_3 = "daily#maxPowerTs-day-3";

    public static final String WEEK_MINUS_0 = "weekly#week-0";
    public static final String WEEK_MINUS_1 = "weekly#week-1";
    public static final String WEEK_MINUS_2 = "weekly#week-2";
    public static final String MONTH_MINUS_0 = "monthly#month-0";
    public static final String MONTH_MINUS_1 = "monthly#month-1";
    public static final String MONTH_MINUS_2 = "monthly#month-2";
    public static final String YEAR_MINUS_0 = "yearly#year-0";
    public static final String YEAR_MINUS_1 = "yearly#year-1";
    public static final String YEAR_MINUS_2 = "yearly#year-2";

    public static final String TEST_SELECT = "main#linkyTestSelect";

    // Authorization related Servlet and resources aliases.
    public static final String LINKY_ALIAS = "/connectlinky";
    public static final String LINKY_IMG_ALIAS = "/img";

    /**
     * Smartthings scopes needed by this binding to work.
     */
    public static final String LINKY_SCOPES = Stream.of("r:devices:*", "w:devices:*", "x:devices:*", "r:hubs:*",
            "r:locations:*", "w:locations:*", "x:locations:*", "r:scenes:*", "x:scenes:*", "r:rules:*", "w:rules:*",
            "r:installedapps", "w:installedapps").collect(Collectors.joining(" "));

    // List of Spotify services related urls, information
    public static final String LINKY_ACCOUNT_URL = "https://www.myelectricaldata.fr/";
    public static final String LINKY_AUTHORIZE_URL = LINKY_ACCOUNT_URL + "v1/oauth2/authorize";
    public static final String LINKY_API_TOKEN_URL = LINKY_ACCOUNT_URL + "token";

    public static final String ENEDIS_ACCOUNT_URL = "https://mon-compte-particulier.enedis.fr/";
    public static final String ENEDIS_AUTHORIZE_URL = ENEDIS_ACCOUNT_URL + "dataconnect/v1/oauth2/authorize";
    public static final String ENEDIS_API_TOKEN_URL = ENEDIS_ACCOUNT_URL + "token";
}
