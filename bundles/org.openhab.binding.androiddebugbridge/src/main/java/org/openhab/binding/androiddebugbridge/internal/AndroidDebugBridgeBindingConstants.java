/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.androiddebugbridge.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AndroidDebugBridgeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeBindingConstants {

    private static final String BINDING_ID = "androiddebugbridge";
    public static final String BINDING_CONFIGURATION_PID = "binding.androiddebugbridge";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ANDROID_DEVICE = new ThingTypeUID(BINDING_ID, "android");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ANDROID_DEVICE);
    // List of all Channel ids
    public static final String START_INTENT_CHANNEL = "start-intent";
    public static final String KEY_EVENT_CHANNEL = "key-event";
    public static final String TEXT_CHANNEL = "text";
    public static final String TAP_CHANNEL = "tap";
    public static final String URL_CHANNEL = "url";
    public static final String MEDIA_VOLUME_CHANNEL = "media-volume";
    public static final String MEDIA_CONTROL_CHANNEL = "media-control";
    public static final String START_PACKAGE_CHANNEL = "start-package";
    public static final String STOP_PACKAGE_CHANNEL = "stop-package";
    public static final String STOP_CURRENT_PACKAGE_CHANNEL = "stop-current-package";
    public static final String CURRENT_PACKAGE_CHANNEL = "current-package";
    public static final String AWAKE_STATE_CHANNEL = "awake-state";
    public static final String WAKE_LOCK_CHANNEL = "wake-lock";
    public static final String SCREEN_STATE_CHANNEL = "screen-state";
    public static final String SHUTDOWN_CHANNEL = "shutdown";
    public static final String RECORD_INPUT_CHANNEL = "record-input";
    public static final String RECORDED_INPUT_CHANNEL = "recorded-input";
    // List of all Parameters
    public static final String PARAMETER_IP = "ip";
    public static final String PARAMETER_PORT = "port";
}
