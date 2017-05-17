/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.samsungtv.internal.service.api;

import java.util.List;

import org.eclipse.smarthome.core.types.Command;

/**
 * Interface for Samsung TV services.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface SamsungTvService {

    /**
     * Procedure to get service name.
     * 
     * @return Service name
     */
    public String getServiceName();

    /**
     * Procedure to get list of supported channel names.
     * 
     * @return List of supported
     */
    public List<String> getSupportedChannelNames();

    /**
     * Procedure for sending command.
     * 
     * @param listener
     *            Event listener instance to handle events.
     */
    public void handleCommand(String channel, Command command);

    /**
     * Procedure for register event listener.
     * 
     * @param listener
     *            Event listener instance to handle events.
     */
    public void addEventListener(ValueReceiver listener);

    /**
     * Procedure for remove event listener.
     * 
     * @param listener
     *            Event listener instance to remove.
     */
    public void removeEventListener(ValueReceiver listener);

    /**
     * Procedure for starting service.
     * 
     */
    public void start();

    /**
     * Procedure for stopping service.
     * 
     */
    public void stop();

    /**
     * Procedure for clearing internal caches.
     * 
     */
    public void clearCache();

}
