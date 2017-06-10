/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.service.api;

import org.eclipse.smarthome.core.types.Command;

import java.util.List;

/**
 * Interface for Samsung TV services.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface SamsungTvService {

    /**
     * Procedure to get list of supported channel names.
     *
     * @return List of supported
     */
    List<String> getSupportedChannelNames();

    /**
     * Procedure for sending command.
     *
     * @param channel the channel to which the command applies
     * @param command the command to be handled
     */
    void handleCommand(String channel, Command command);

    /**
     * Procedure for register event listener.
     *
     * @param listener
     *            Event listener instance to handle events.
     */
    void addEventListener(EventListener listener);

    /**
     * Procedure for remove event listener.
     *
     * @param listener
     *            Event listener instance to remove.
     */
    void removeEventListener(EventListener listener);

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
     * Is this a manual configured service
     *
     * @return whether this service was a manual override
     */
    boolean isManual();
}
