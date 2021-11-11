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
package org.openhab.binding.panasonictv.internal.api;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;

/**
 * Interface for Panasonic TV services.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public interface PanasonicTvService {

    /**
     * Procedure to get list of supported channel names.
     *
     * @return List of supported
     */
    Set<String> getSupportedChannelNames();

    /**
     * Procedure for sending command.
     *
     * @param channel the channel to which the command applies
     * @param command the command to be handled
     */
    void handleCommand(String channel, Command command);

    /**
     * Procedure for starting service.
     *
     */
    void start();

    /**
     * Procedure for stopping service.
     *
     */
    void stop();

    /**
     * Procedure for clearing internal caches.
     *
     */
    void clearCache();

    /**
     * get the service name of this service
     *
     * @return a String containing the service name
     */
    String getServiceName();
}
