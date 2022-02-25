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
package org.openhab.binding.sunsa.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SunsaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaBindingConstants {

    public static final String BINDING_ID = "sunsa";

    // --- List of all Thing Type UIDs ---
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "cloudBridge");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // --- List of all Channel IDs ---
    /**
     * The raw position of the device/blinds.
     * -100: closed up.
     * 0: open position.
     * 100: raised down.
     */
    public static final String CHANNEL_RAW_POSITION = "rawPosition";
    /**
     * The configurable position of the device/blinds. Configuration of the <em>Closed<em> and <em>Open</em>
     * positions are done as part of the thing configuration.
     */
    public static final String CHANNEL_CONFIGURABLE_POSITION = "configurablePosition";
    /**
     * Battery level percentage (0-100)
     */
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";

    public static final int CONFIGURABLE_POSITION_MAX_VALUE = 100;
    public static final int CONFIGURABLE_POSITION_MIN_VALUE = -100;
}
