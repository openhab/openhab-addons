/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SolarEdgeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - Initial contribution
 */
public class SolarEdgeBindingConstants {

    private static final @NonNull String BINDING_ID = "solaredge";

    // List of main device types
    public static final @NonNull String DEVICE_GENERIC = "generic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, DEVICE_GENERIC);

    // List of all Channel ids ==> see DataChannels

    // // URLs
    public static final String PRE_LOGIN_URL = "https://monitoring.solaredge.com/solaredge-web/p/submitLogin";
    public static final String POST_LOGIN_SESSION_TOKEN_URL = "https://monitoring.solaredge.com/solaredge-web/p/login";
    public static final String POST_LOGIN_CLIENT_COOKIE_URL = "https://monitoring.solaredge.com/solaredge-web/p/initClient";

    public static final String DATA_API_URL = "https://monitoring.solaredge.com/solaredge-apigw/api/site/";
    public static final String DATA_API_LEGACY_LIVE_DATA_URL = "https://monitoring.solaredge.com/solaredge-web/p/overviewPanel/";
    public static final String DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX = "/powerDashboardChart";
    public static final String DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX = "/energyDashboardChart";
    public static final String DATA_API_URL_LIVE_DATA_SUFFIX = "/currentPowerFlow.json";

    // login field names
    public static final String TOKEN_COOKIE_NAME = "SPRING_SECURITY_REMEMBER_ME_COOKIE";
    public static final String CLIENT_COOKIE_NAME_PREFIX = "SolarEdge_Client";

    public static final String LOGIN_COMMAND_FIELD = "cmd";
    public static final String LOGIN_COMMAND_VALUE = "login";
    public static final String LOGIN_DEMO_FIELD = "demo";
    public static final String LOGIN_DEMO_VALUE = "false";
    public static final String LOGIN_USERNAME_FIELD = "username";
    public static final String LOGIN_PASSWORD_FIELD = "password";

    public static final String POST_LOGIN_CLIENT_CMD_FIELD = "cmd";
    public static final String POST_LOGIN_CLIENT_CMD_VALUE = "createCookie";
    public static final String POST_LOGIN_CLIENT_TARGET_FIELD = "target";
    public static final String POST_LOGIN_CLIENT_TARGET_VALUE = "site/";
    public static final String POST_LOGIN_CLIENT_CLIENT_FIELD = "client";
    public static final String POST_LOGIN_CLIENT_CLIENT_VALUE = "touch:false|csstransforms3d:true|generatedcontent:true|fontface:true|flexbox:true|canvas:true|canvastext:true|webgl:true|geolocation:true|postmessage:true|websqldatabase:false|indexeddb:true|hashchange:true|history:true|draganddrop:true|websockets:true|rgba:true|hsla:true|multiplebgs:true|backgroundsize:true|borderimage:true|borderradius:true|boxshadow:true|textshadow:true|opacity:true|cssanimations:true|csscolumns:true|cssgradients:true|cssreflections:false|csstransforms:true|csstransitions:true|video:|ogg:true|h264:true|webm:true|audio:|ogg:true|mp3:true|wav:true|m4a:true|localstorage:true|sessionstorage:true|webworkers:true|applicationcache:true|svg:true|inlinesvg:true|smil:true|svgclippaths:true|input:|autocomplete:true|autofocus:true|list:true|placeholder:true|max:true|min:true|multiple:true|pattern:true|required:true|step:true|inputtypes:|search:true|tel:true|url:true|email:true|datetime:false|date:false|month:false|week:false|time:false|datetime-local:false|number:true|range:true|color:true|fileapi:true|fullscreen:true|clientWidth:1920|clientHeight:1080|windowInnerWidth:1920|windowInnerHeight:416|windowMaxWidth:1920|windowMaxHeight:1080|flash:false|mobile:false|phone:false|tablet:false|ie11:false";

    public static final String DATA_API_LEGACY_LIVE_DATA_FIELDID_FIELD = "fieldId";
    public static final String DATA_API_AGGREGATE_DATA_CHARTFIELD_FIELD = "chartField";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_GENERIC);

}
