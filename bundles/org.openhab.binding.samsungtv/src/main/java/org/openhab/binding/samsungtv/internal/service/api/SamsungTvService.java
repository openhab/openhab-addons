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
package org.openhab.binding.samsungtv.internal.service.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;

/**
 * Interface for Samsung TV services.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Nick Waterton - add checkConnection(), getServiceName(), refactoring
 */
@NonNullByDefault
public interface SamsungTvService {

    /**
     * Procedure to get list of supported channel names.
     *
     * @return List of supported
     */
    List<String> getSupportedChannelNames(boolean refresh);

    /**
     * Procedure for sending command.
     *
     * @param channel the channel to which the command applies
     * @param command the command to be handled
     */
    boolean handleCommand(String channel, Command command);

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
     * Is this an UPnP configured service
     *
     * @return whether this service is an UPnP configured / discovered service
     */
    boolean isUpnp();

    /**
     * Is service connected
     *
     * @return whether this service is connected or not
     */
    boolean checkConnection();

    /**
     * get service name.
     *
     * @return String SERVICE_NAME
     */
    String getServiceName();
}
