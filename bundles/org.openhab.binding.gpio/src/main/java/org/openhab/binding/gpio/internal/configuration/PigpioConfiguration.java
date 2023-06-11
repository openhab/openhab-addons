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
package org.openhab.binding.gpio.internal.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link PigpioConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public class PigpioConfiguration {

    /**
     * Network address of the raspberry pi
     */
    public @Nullable String host;

    /**
     * Port of pigpio on the remote raspberry pi
     */
    public int port = 8888;
}
