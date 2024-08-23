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
package org.openhab.binding.gpio.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.Command;

import eu.xeli.jpigpio.JPigpio;
import eu.xeli.jpigpio.PigpioException;

/**
 * The {@link ChannelHandler} provides an interface for different pin
 * configuration handlers
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public interface ChannelHandler {

    /**
     * Handles a Command being sent from the
     * Openhab framework.
     */
    void handleCommand(Command command) throws PigpioException;

    /**
     * (Re)Establishes the JPigpio listeners.
     */
    void listen(@Nullable JPigpio jPigpio) throws PigpioException;

    /**
     * Terminates sending Channels status updates and
     * shuts down any JPigpio listeners.
     */
    void dispose();
}
