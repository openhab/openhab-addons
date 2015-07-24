/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo;

import java.util.Collection;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link NetatmoBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NetatmoBindingConstants {

    public static final String BINDING_ID = "netatmo";

    // List of configuration elements for this binding
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String REFRESH_TOKEN = "refreshToken";

    // Module config properties
    public static final String MODULE_ID = "moduleId";
    public static final String DEVICE_ID = "deviceId";

    // public static final String USER_ID = "userId";

    // List of main device types
    public static final String BRIDGE_API = "netatmoapi";

    /**
     * <code>type</code> constant of the main indoor station.
     */
    public static final String THING_TYPE_MAIN = "NAMain";

    /**
     * <code>type</code> constant of the outdoor module
     */
    public static final String THING_TYPE_MODULE_1 = "NAModule1";

    /**
     * <code>type</code> constant of the rain gauge module
     */
    public static final String THING_TYPE_MODULE_3 = "NAModule3";

    /**
     * <code>type</code> constant of the additional indoor module
     */
    public static final String THING_TYPE_MODULE_4 = "NAModule4";

    /**
     * <code>type</code> constant of the thermostat relay/plug
     */
    public static final String THING_TYPE_PLUG = "NAPlug";

    /**
     * <code>type</code> constant of the thermostat module
     */
    public static final String THING_TYPE_THERM_1 = "NATherm1";

    // List of all Thing Type UIDs
    public final static ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE_API);
    public final static ThingTypeUID MAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, THING_TYPE_MAIN);
    public final static ThingTypeUID MODULE1_THING_TYPE = new ThingTypeUID(BINDING_ID, THING_TYPE_MODULE_1);
    public final static ThingTypeUID MODULE3_THING_TYPE = new ThingTypeUID(BINDING_ID, THING_TYPE_MODULE_3);
    public final static ThingTypeUID MODULE4_THING_TYPE = new ThingTypeUID(BINDING_ID, THING_TYPE_MODULE_4);
    public final static ThingTypeUID PLUG_THING_TYPE = new ThingTypeUID(BINDING_ID, THING_TYPE_PLUG);
    public final static ThingTypeUID THERM1_THING_TYPE = new ThingTypeUID(BINDING_ID, THING_TYPE_THERM_1);

    // List of all Channel ids
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";

    public final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(MAIN_THING_TYPE,
            MODULE1_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE, PLUG_THING_TYPE, THERM1_THING_TYPE,
            APIBRIDGE_THING_TYPE);

    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(MAIN_THING_TYPE,
            MODULE1_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE, PLUG_THING_TYPE, THERM1_THING_TYPE);

    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(APIBRIDGE_THING_TYPE);

}
