/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etherrain;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EtherRainBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Joe Inkenbrandt - Initial contribution
 */
@NonNullByDefault
public class EtherRainBindingConstants {

    private static final String BINDING_ID = "etherrain";

    // List of all Thing ids

    private static final String ETHERRAIN = "etherrain";

    // List of all Thing Type UIDs

    public static final ThingTypeUID ETHERRAIN_THING = new ThingTypeUID(BINDING_ID, ETHERRAIN);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(ETHERRAIN_THING);

    // List of internal default values

    public static final int DEFAULT_WAIT_BEFORE_INITIAL_REFRESH = 30;
    public static final int DEFAULT_REFRESH_RATE = 60;
    public static final short DISCOVERY_SUBNET_MASK = 24;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final int DISCOVERY_THREAD_POOL_SHUTDOWN_WAIT_TIME_SECONDS = 300;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE = 750;

    // List of all Channel ids

    public static final String CHANNEL_ID_COMMAND_STATUS = "commandstatus";
    public static final String CHANNEL_ID_OPERATING_STATUS = "operatingstatus";
    public static final String CHANNEL_ID_OPERATING_RESULT = "operatingresult";
    public static final String CHANNEL_ID_RELAY_INDEX = "relayindex";
    public static final String CHANNEL_ID_SENSOR_RAIN = "rainsensor";
    public static final String CHANNEL_ID_START_DELAY = "startdelay";
    public static final String CHANNEL_ID_EXECUTE = "execute";
    public static final String CHANNEL_ID_CLEAR = "clear";

}
