/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba.service;

import org.openhab.binding.roomba.model.exception.RoombaCommunicationException;

/**
 * Provides an interface for using Roomba's various features
 *
 * @author Stephen Liang
 *
 */
public interface RoombaService {
    /**
     * Pings the Roomba to if it is ok or not
     *
     * @return True if the Roomba is ok, false otherwise.
     */
    public boolean isOk() throws RoombaCommunicationException;

    /**
     * Starts the Roomba's cleaning process
     */
    public void start() throws RoombaCommunicationException;

    /**
     * Pauses the Roomba's cleaning process
     */
    public void pause() throws RoombaCommunicationException;

    /**
     * Stops the Roomba's cleaning process
     */
    public void stop() throws RoombaCommunicationException;

    /**
     * Resumes a previously paused Roomba's cleaning
     */
    public void resume() throws RoombaCommunicationException;

    /**
     * Tells the Roomba to return to the charging dock
     */
    public void dock() throws RoombaCommunicationException;
}
