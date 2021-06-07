/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GPIOInputConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public class GPIOInputConfiguration extends GPIOConfiguration {
    /**
     * Time in ms to double check if value hasn't changed
     */
    public int debouncingTime = 10;

    /**
     * Setup a pullup resistor on the GPIO pin
     * 0 = PI_PUD_OFF, 1 = PI_PUD_DOWN, 2 = PI_PUD_UP
     */
    public int pullupdown = 0;
}
