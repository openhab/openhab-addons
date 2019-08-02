/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sinope;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link sinopeBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pascal Larin - Initial contribution
 */
public class SinopeBindingConstants {

    public static final String BINDING_ID = "sinope";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_THERMO = new ThingTypeUID(BINDING_ID, "thermostat");
    public final static ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    // List of all Channel ids
    public final static String CHANNEL_HEATINGLEVEL = "heatingLevel";
    public final static String CHANNEL_SETTEMP = "setpointTemperature";
    public final static String CHANNEL_SETMODE = "setpointMode";
    public final static String CHANNEL_INTEMP = "insideTemperature";
    public final static String CHANNEL_OUTTEMP = "outsideTemperature";

    public static final String CONFIG_PROPERTY_HOST = "ipAddress";
    public static final String CONFIG_PROPERTY_PORT = "ipPort";
    public static final String CONFIG_PROPERTY_GATEWAY_ID = "gatewayID";
    public static final String CONFIG_PROPERTY_API_KEY = "apiKey";
    public static final String CONFIG_PROPERTY_POLLING_INTERVAL = "pollingInterval";
    public static final String CONFIG_PROPERTY_DEVICE_ID = "deviceId";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_GATEWAY);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_THERMO);
    }

}
