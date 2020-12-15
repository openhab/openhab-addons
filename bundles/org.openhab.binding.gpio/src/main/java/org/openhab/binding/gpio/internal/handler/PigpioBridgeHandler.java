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
package org.openhab.binding.gpio.internal.handler;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;

import eu.xeli.jpigpio.JPigpio;

/**
 * JPigpio Bridge Handler interface
 *
 * Different bridge types (local/remote) should implement this.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
public interface PigpioBridgeHandler {

    /**
     * Gets the JPigpio instance
     *
     * @return the JPigpio instance in an Optional
     */
    public Optional<JPigpio> getJPiGpio();
}
