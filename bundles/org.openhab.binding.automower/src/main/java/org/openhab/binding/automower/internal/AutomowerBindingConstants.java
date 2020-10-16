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
package org.openhab.binding.automower.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AutomowerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerBindingConstants {

    private static final String BINDING_ID = "automower";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_AUTOMOWER = new ThingTypeUID(BINDING_ID, "automower");

    // List of all Channel ids
    public static final String CHANNEL_MOWER_NAME = "name";
    public static final String CHANNEL_STATUS_MODE = "mode";
    public static final String CHANNEL_STATUS_ACTIVITY = "activity";
    public static final String CHANNEL_STATUS_STATE = "state";
    public static final String CHANNEL_STATUS_LAST_UPDATE = "last-update";
    public static final String CHANNEL_STATUS_BATTERY = "battery";
    public static final String CHANNEL_STATUS_ERROR_CODE = "error-code";
    public static final String CHANNEL_STATUS_ERROR_TIMESTAMP = "error-timestamp";

    // Automower properties
    public static final String AUTOMOWER_ID = "mowerId";
    public static final String AUTOMOWER_NAME = "mowerName";
    public static final String AUTOMOWER_MODEL = "mowerModel";
    public static final String AUTOMOWER_SERIAL_NUMBER = "mowerSerialNumber";
}
