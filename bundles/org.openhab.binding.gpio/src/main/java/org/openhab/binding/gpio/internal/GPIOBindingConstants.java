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
package org.openhab.binding.gpio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link gpioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nils Bauer - Initial contribution
 * @author Martin Dagarin - Pull Up/Down GPIO pin
 */
@NonNullByDefault
public class GPIOBindingConstants {

    private static final String BINDING_ID = "gpio";

    public static final ThingTypeUID THING_TYPE_PIGPIO_REMOTE_THING = new ThingTypeUID(BINDING_ID, "pigpio-remote");

    // List of all Thing Type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_DIGITAL_INPUT = new ChannelTypeUID(BINDING_ID,
            "pigpio-digital-input");
    public static final ChannelTypeUID CHANNEL_TYPE_DIGITAL_OUTPUT = new ChannelTypeUID(BINDING_ID,
            "pigpio-digital-output");

    // Thing config properties
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String INVERT = "invert";
    public static final String DEBOUNCING_TIME = "debouncing_time";
    public static final String STRICT_DEBOUNCING = "debouncing_strict";
    public static final String PULLUPDOWN_RESISTOR = "pullupdown";

    // Pull Up/Down modes
    public static final String PUD_OFF = "OFF";
    public static final String PUD_DOWN = "DOWN";
    public static final String PUD_UP = "UP";

    // GPIO config properties
    public static final String GPIO_ID = "gpioId";
}
