/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.gpio.internal.configuration;

import static org.openhab.binding.gpio.internal.GPIOBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GPIOInputConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nils Bauer - Initial contribution
 * @author Martin Dagarin - Pull Up/Down GPIO pin
 */
@NonNullByDefault
public class GPIOInputConfiguration extends GPIOConfiguration {
    /**
     * Time in ms to double check if value hasn't changed
     */
    public int debouncingTime = 10;

    /**
     * Setup a pullup resistor on the GPIO pin
     * OFF = PI_PUD_OFF, DOWN = PI_PUD_DOWN, UP = PI_PUD_UP
     */
    public String pullupdown = PUD_OFF;

    /**
     * Sets the input detection type.
     * EDGE_EITHER = PI_EITHER_EDGE, EDGE_FALLING = PI_FALLING_EDGE,
     * EDGE_RISING = PI_RISING_EDGE
     */
    public String edgeMode = EDGE_EITHER;
}
