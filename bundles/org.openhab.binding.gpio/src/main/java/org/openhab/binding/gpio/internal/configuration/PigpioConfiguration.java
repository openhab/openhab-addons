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

    /**
     * Interval to send heartbeat checks
     */
    public int heartBeatInterval = 60000;

    /**
     * Input channel action on connect
     * (First connect after INITIALIATION)
     */
    public @Nullable String inputConnectAction;

    /**
     * Input channel action on reconnect
     */
    public @Nullable String inputReconnectAction;

    /**
     * Input channel action on disconnect
     */
    public @Nullable String inputDisconnectAction;

    /**
     * Output channel action on connect
     * (First connect after INITIALIATION)
     */
    public @Nullable String outputConnectAction;

    /**
     * Output channel action on reconnect
     */
    public @Nullable String outputReconnectAction;

    /**
     * Output channel action on disconnect
     */
    public @Nullable String outputDisconnectAction;
}
