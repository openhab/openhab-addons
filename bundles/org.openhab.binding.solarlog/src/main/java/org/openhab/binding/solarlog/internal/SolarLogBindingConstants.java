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
package org.openhab.binding.solarlog.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolarLogBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johann Richard - Initial contribution
 */
public class SolarLogBindingConstants {

    public static final String BINDING_ID = "solarlog";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_SOLARLOG = new ThingTypeUID(BINDING_ID, "meter");

    // List of all Channel ids
    public static final String CHANNEL_ID_LASTUPDATETIME = "lastupdate";
    public static final String CHANNEL_ID_PAC = "pac";
    public static final String CHANNEL_ID_PDC = "pdc";
    public static final String CHANNEL_ID_UAC = "uac";
    public static final String CHANNEL_ID_UDC = "udc";
    public static final String CHANNEL_ID_YIELDDAY = "yieldday";
    public static final String CHANNEL_ID_YIELDYESTERDAY = "yieldyesterday";
    public static final String CHANNEL_ID_YIELDMONTH = "yieldmonth";
    public static final String CHANNEL_ID_YIELDYEAR = "yieldyear";
    public static final String CHANNEL_ID_YIELDTOTAL = "yieldtotal";
    public static final String CHANNEL_ID_CONSPAC = "conspac";
    public static final String CHANNEL_ID_CONSYIELDDAY = "consyieldday";
    public static final String CHANNEL_ID_CONSYIELDYESTERDAY = "consyieldyesterday";
    public static final String CHANNEL_ID_CONSYIELDMONTH = "consyieldmonth";
    public static final String CHANNEL_ID_CONSYIELDYEAR = "consyieldyear";
    public static final String CHANNEL_ID_CONSYIELDTOTAL = "consyieldtotal";
    public static final String CHANNEL_ID_TOTALPOWER = "totalpower";

    // List of all JSON Id's for channels
    public static final String CHANNEL_LASTUPDATETIME = "100";
    public static final String CHANNEL_PAC = "101";
    public static final String CHANNEL_PDC = "102";
    public static final String CHANNEL_UAC = "103";
    public static final String CHANNEL_UDC = "104";
    public static final String CHANNEL_YIELDDAY = "105";
    public static final String CHANNEL_YIELDYESTERDAY = "106";
    public static final String CHANNEL_YIELDMONTH = "107";
    public static final String CHANNEL_YIELDYEAR = "108";
    public static final String CHANNEL_YIELDTOTAL = "109";
    public static final String CHANNEL_CONSPAC = "110";
    public static final String CHANNEL_CONSYIELDDAY = "111";
    public static final String CHANNEL_CONSYIELDYESTERDAY = "112";
    public static final String CHANNEL_CONSYIELDMONTH = "113";
    public static final String CHANNEL_CONSYIELDYEAR = "114";
    public static final String CHANNEL_CONSYIELDTOTAL = "115";
    public static final String CHANNEL_TOTALPOWER = "116";

    // CHannel Type (DateTime or Number
    public static final String CHANNEL_TYPE_LASTUPDATETIME = "DateTime";
    public static final String CHANNEL_TYPE_PAC = "Number";
    public static final String CHANNEL_TYPE_PDC = "Number";
    public static final String CHANNEL_TYPE_UAC = "Number";
    public static final String CHANNEL_TYPE_UDC = "Number";
    public static final String CHANNEL_TYPE_YIELDDAY = "Number";
    public static final String CHANNEL_TYPE_YIELDYESTERDAY = "Number";
    public static final String CHANNEL_TYPE_YIELDMONTH = "Number";
    public static final String CHANNEL_TYPE_YIELDYEAR = "Number";
    public static final String CHANNEL_TYPE_YIELDTOTAL = "Number";
    public static final String CHANNEL_TYPE_CONSPAC = "Number";
    public static final String CHANNEL_TYPE_CONSYIELDDAY = "Number";
    public static final String CHANNEL_TYPE_CONSYIELDYESTERDAY = "Number";
    public static final String CHANNEL_TYPE_CONSYIELDMONTH = "Number";
    public static final String CHANNEL_TYPE_CONSYIELDYEAR = "Number";
    public static final String CHANNEL_TYPE_CONSYIELDTOTAL = "Number";
    public static final String CHANNEL_TYPE_TOTALPOWER = "Number";

    // Some basic constants (JSON ID)
    public static final String SOLARLOG_JSON_ROOT = "801";
    public static final String SOLARLOG_JSON_PROPERTIES = "170";
}
