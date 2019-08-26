/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal.api;

import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.model.StationProgram;

/**
 * The {@link OpenSprinklerApi} interface defines the functions which are
 * controllable on the OpenSprinkler API interface.
 *
 * @author Chris Graham - Initial contribution
 */
public interface OpenSprinklerApi {
    /**
     * Whether the devie entered manual mode and accepts API requests to control the stations.
     *
     * @return True if this API interface is connected to the Open Sprinkler API. False otherwise.
     */
    public abstract boolean isManualModeEnabled();

    /**
     * Enters the "manual" mode of the device so that API requests are accepted.
     *
     * @throws Exception
     */
    public abstract void enterManualMode() throws CommunicationApiException;

    /**
     * Disables the manual mode, if it is enabled.
     *
     * @throws Exception
     */
    public abstract void leaveManualMode() throws CommunicationApiException;

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
    public abstract boolean isStationOpen(int station) throws GeneralApiException, CommunicationApiException;

    /**
     * Returns the current program data of the requested station.
     *
     * @param station Index of the station to request data from
     * @return StationProgram
     * @throws Exception
     */
    public abstract StationProgram retrieveProgram(int station) throws CommunicationApiException;

    /**
     * Returns the state of rain detection on the OpenSprinkler device.
     *
     * @return True if rain is detected, false if not or cannot determine.
     * @throws Exception
     */
    public abstract boolean isRainDetected() throws CommunicationApiException;

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
    public abstract int getFirmwareVersion() throws CommunicationApiException;
}
