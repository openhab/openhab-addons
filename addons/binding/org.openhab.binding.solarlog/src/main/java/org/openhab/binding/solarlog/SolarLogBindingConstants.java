/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SolarLogBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johann Richard - Initial contribution
 */
public class SolarLogBindingConstants {

    public static final String BINDING_ID = "solarlog";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_SOLARLOG = new ThingTypeUID(BINDING_ID, "solarlog_js");

    // List of all Channel ids
    public final static String CHANNEL_ID_LASTUPDATETIME = "lastupdate";
    public final static String CHANNEL_ID_PAC = "pac";
    public final static String CHANNEL_ID_PDC = "pdc";
    public final static String CHANNEL_ID_UAC = "uac";
    public final static String CHANNEL_ID_UDC = "udc";
    public final static String CHANNEL_ID_YIELDDAY = "yieldday";
    public final static String CHANNEL_ID_YIELDYESTERDAY = "yieldyesterday";
    public final static String CHANNEL_ID_YIELDMONTH = "yieldmonth";
    public final static String CHANNEL_ID_YIELDYEAR = "yieldyear";
    public final static String CHANNEL_ID_YIELDTOTAL = "yieldtotal";
    public final static String CHANNEL_ID_CONSPAC = "conspac";
    public final static String CHANNEL_ID_CONSYIELDDAY = "consyieldday";
    public final static String CHANNEL_ID_CONSYIELDYESTERDAY = "consyieldyesterday";
    public final static String CHANNEL_ID_CONSYIELDMONTH = "consyieldmonth";
    public final static String CHANNEL_ID_CONSYIELDYEAR = "consyieldyear";
    public final static String CHANNEL_ID_CONSYIELDTOTAL = "consyieldtotal";
    public final static String CHANNEL_ID_TOTALPOWER = "totalpower";

    // List of all JSON Id's for channels
    public final static String CHANNEL_LASTUPDATETIME = "100";
    public final static String CHANNEL_PAC = "101";
    public final static String CHANNEL_PDC = "102";
    public final static String CHANNEL_UAC = "103";
    public final static String CHANNEL_UDC = "104";
    public final static String CHANNEL_YIELDDAY = "105";
    public final static String CHANNEL_YIELDYESTERDAY = "106";
    public final static String CHANNEL_YIELDMONTH = "107";
    public final static String CHANNEL_YIELDYEAR = "108";
    public final static String CHANNEL_YIELDTOTAL = "109";
    public final static String CHANNEL_CONSPAC = "110";
    public final static String CHANNEL_CONSYIELDDAY = "111";
    public final static String CHANNEL_CONSYIELDYESTERDAY = "112";
    public final static String CHANNEL_CONSYIELDMONTH = "113";
    public final static String CHANNEL_CONSYIELDYEAR = "114";
    public final static String CHANNEL_CONSYIELDTOTAL = "115";
    public final static String CHANNEL_TOTALPOWER = "116";

    // CHannel Type (DateTime or Number
    public final static String CHANNEL_TYPE_LASTUPDATETIME = "DateTime";
    public final static String CHANNEL_TYPE_PAC = "Number";
    public final static String CHANNEL_TYPE_PDC = "Number";
    public final static String CHANNEL_TYPE_UAC = "Number";
    public final static String CHANNEL_TYPE_UDC = "Number";
    public final static String CHANNEL_TYPE_YIELDDAY = "Number";
    public final static String CHANNEL_TYPE_YIELDYESTERDAY = "Number";
    public final static String CHANNEL_TYPE_YIELDMONTH = "Number";
    public final static String CHANNEL_TYPE_YIELDYEAR = "Number";
    public final static String CHANNEL_TYPE_YIELDTOTAL = "Number";
    public final static String CHANNEL_TYPE_CONSPAC = "Number";
    public final static String CHANNEL_TYPE_CONSYIELDDAY = "Number";
    public final static String CHANNEL_TYPE_CONSYIELDYESTERDAY = "Number";
    public final static String CHANNEL_TYPE_CONSYIELDMONTH = "Number";
    public final static String CHANNEL_TYPE_CONSYIELDYEAR = "Number";
    public final static String CHANNEL_TYPE_CONSYIELDTOTAL = "Number";
    public final static String CHANNEL_TYPE_TOTALPOWER = "Number";

    // Some basic constants (JSON ID)
    public final static String SOLARLOG_JSON_ROOT = "801";
    public final static String SOLARLOG_JSON_PROPERTIES = "170";
}
