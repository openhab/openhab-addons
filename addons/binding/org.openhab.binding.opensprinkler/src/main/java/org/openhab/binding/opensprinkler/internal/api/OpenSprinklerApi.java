/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.internal.api;

/**
 * The {@link OpenSprinklerApi} interface defines the functions which are
 * controllable on the OpenSprinkler API interface.
 *
 * @author Chris Graham - Initial contribution
 */
public interface OpenSprinklerApi {
    /**
     * Returns the state of this API connection to the OpenSprinkler device.
     *
     * @return True if this API interface is connected to the Open Sprinkler API. False otherwise.
     */
    public abstract boolean isConnected();

    /**
     * Opens a connection to the OpenSprinkler device.
     *
     * @throws Exception
     */
    public abstract void openConnection() throws Exception;

    /**
     * Closes the connection to the OpenSprinkler device.
     *
     * @throws Exception
     */
    public abstract void closeConnection() throws Exception;

    /**
     * Starts a station on the OpenSprinkler device.
     *
     * @param station Index of the station to open starting at 0.
     * @throws Exception
     */
    public abstract void openStation(int station) throws Exception;

    /**
     * Closes a station on the OpenSprinkler device.
     *
     * @param station Index of the station to open starting at 0.
     * @throws Exception
     */
    public abstract void closeStation(int station) throws Exception;

    /**
     * Returns the state of a station on the OpenSprinkler device.
     *
     * @param station Index of the station to open starting at 0.
     * @return True if the station is open, false if it is closed or cannot determine.
     * @throws Exception
     */
    public abstract boolean isStationOpen(int station) throws Exception;

    /**
     * Returns the state of rain detection on the OpenSprinkler device.
     *
     * @return True if rain is detected, false if not or cannot determine.
     * @throws Exception
     */
    public abstract boolean isRainDetected() throws Exception;

    /**
     * Returns the number of total stations that are controllable from the OpenSprinkler
     * device.
     *
     * @return Number of stations as an int.
     * @throws Exception
     */
    public abstract int getNumberOfStations() throws Exception;

    /**
     * Returns the firmware version number.
     *
     * @return The firmware version of the OpenSprinkler device as an int.
     * @throws Exception
     */
    public abstract int getFirmwareVersion() throws Exception;
}
