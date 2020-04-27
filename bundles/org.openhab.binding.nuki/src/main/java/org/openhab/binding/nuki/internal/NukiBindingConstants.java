/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NukiBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Katter - Initial contribution
 */
public class NukiBindingConstants {

    public static final String BINDING_ID = "nuki";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_SMARTLOCK = new ThingTypeUID(BINDING_ID, "smartlock");

    public static final Set<ThingTypeUID> THING_TYPE_BRIDGE_UIDS = Collections.singleton(THING_TYPE_BRIDGE);
    public static final Set<ThingTypeUID> THING_TYPE_SMARTLOCK_UIDS = Collections.singleton(THING_TYPE_SMARTLOCK);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(THING_TYPE_BRIDGE_UIDS.stream(), THING_TYPE_SMARTLOCK_UIDS.stream()).collect(Collectors.toSet());

    // List of all Channel ids
    public static final String CHANNEL_SMARTLOCK_LOCK = "lock";
    public static final String CHANNEL_SMARTLOCK_STATE = "lockState";
    public static final String CHANNEL_SMARTLOCK_LOW_BATTERY = "lowBattery";

    // List of all config-description parameters
    public static final String CONFIG_IP = "ip";
    public static final String CONFIG_PORT = "port";
    public static final String CONFIG_MANAGECB = "manageCallbacks";
    public static final String CONFIG_API_TOKEN = "apiToken";
    public static final String CONFIG_NUKI_ID = "nukiId";
    public static final String CONFIG_UNLATCH = "unlatch";

    // Nuki Bridge API REST Endpoints
    public static final String URI_INFO = "http://%s:%s/info?token=%s";
    public static final String URI_LOCKSTATE = "http://%s:%s/lockState?token=%s&nukiId=%s";
    public static final String URI_LOCKACTION = "http://%s:%s/lockAction?token=%s&nukiId=%s&action=%s";
    public static final String URI_CBADD = "http://%s:%s/callback/add?token=%s&url=%s";
    public static final String URI_CBLIST = "http://%s:%s/callback/list?token=%s";
    public static final String URI_CBREMOVE = "http://%s:%s/callback/remove?token=%s&id=%s";

    // openHAB Callback Endpoint & Nuki Bridge Callback URL
    public static final String CALLBACK_ENDPOINT = "/nuki/bcb";
    public static final String CALLBACK_URL = "http://%s" + CALLBACK_ENDPOINT;

    // Nuki Bridge API Lock Actions
    public static final int LOCK_ACTIONS_UNLOCK = 1;
    public static final int LOCK_ACTIONS_LOCK = 2;
    public static final int LOCK_ACTIONS_UNLATCH = 3;
    public static final int LOCK_ACTIONS_LOCKNGO_UNLOCK = 4;
    public static final int LOCK_ACTIONS_LOCKNGO_UNLATCH = 5;

    // Nuki Bridge API Lock States
    public static final int LOCK_STATES_UNCALIBRATED = 0;
    public static final int LOCK_STATES_LOCKED = 1;
    public static final int LOCK_STATES_UNLOCKING = 2;
    public static final int LOCK_STATES_UNLOCKED = 3;
    public static final int LOCK_STATES_LOCKING = 4;
    public static final int LOCK_STATES_UNLATCHED = 5;
    public static final int LOCK_STATES_UNLOCKED_LOCKNGO = 6;
    public static final int LOCK_STATES_UNLATCHING = 7;
    public static final int LOCK_STATES_MOTOR_BLOCKED = 254;
    public static final int LOCK_STATES_UNDEFINED = 255;

    // Nuki Binding additional Lock States
    public static final int LOCK_STATES_UNLOCKING_LOCKNGO = 1002;
    public static final int LOCK_STATES_UNLATCHING_LOCKNGO = 1007;
}
