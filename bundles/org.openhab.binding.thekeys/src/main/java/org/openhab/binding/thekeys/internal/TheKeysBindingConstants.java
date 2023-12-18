/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.thekeys.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TheKeysBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class TheKeysBindingConstants {

    public static final String BINDING_ID = "thekeys";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_SMARTLOCK = new ThingTypeUID(BINDING_ID, "smartlock");

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_LOW_BATTERY = "lowBattery";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_LOCK = "lock";
    public static final String CHANNEL_LAST_SYNC = "lastSync";
    public static final String CHANNEL_SYNC_IN_PROGRESS = "syncInProgress";

    // List of thing configuration
    public static final String CONF_SMARTLOCK_LOCKID = "lockId";

    // List of property
    public static final String PROPERTY_VERSION = "version";
}
