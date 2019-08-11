/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.opengarage.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenGarageBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class OpenGarageBindingConstants {

    private static final String BINDING_ID = "opengarage";

    // List of all Thing Type UIDs
    public static final ThingTypeUID OPENGARAGE_THING  = new ThingTypeUID(BINDING_ID, "opengarage");

    // List of all Channel ids
    public static final String CHANNEL_OG_DISTANCE = "distance";
    public static final String CHANNEL_OG_STATUS = "status";
    public static final String CHANNEL_OG_VEHICLE = "vehicle";


    public static final int DEFAULT_WAIT_BEFORE_INITIAL_REFRESH = 30;
    public static final int DEFAULT_REFRESH_RATE = 60;
    public static final short DISCOVERY_SUBNET_MASK = 24;
    public static final int DISCOVERY_THREAD_POOL_SIZE = 15;
    public static final int DISCOVERY_THREAD_POOL_SHUTDOWN_WAIT_TIME_SECONDS = 300;
    public static final boolean DISCOVERY_DEFAULT_AUTO_DISCOVER = false;
    public static final int DISCOVERY_DEFAULT_TIMEOUT_RATE = 500;
    public static final int DISCOVERY_DEFAULT_IP_TIMEOUT_RATE = 750;
    public static final String DEFAULT_ADMIN_PASSWORD = "opendoor";
    public static final int DEFAULT_API_PORT = 80;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(OPENGARAGE_THING);
}
