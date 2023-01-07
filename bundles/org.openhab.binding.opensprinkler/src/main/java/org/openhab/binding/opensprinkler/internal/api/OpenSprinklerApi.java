/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.util.List;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.opensprinkler.internal.OpenSprinklerState.JnResponse;
import org.openhab.binding.opensprinkler.internal.api.exception.CommunicationApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.GeneralApiException;
import org.openhab.binding.opensprinkler.internal.api.exception.UnauthorizedApiException;
import org.openhab.binding.opensprinkler.internal.model.StationProgram;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;

/**
 * The {@link OpenSprinklerApi} interface defines the functions which are
 * controllable on the OpenSprinkler API interface.
 *
 * @author Chris Graham - Initial contribution
 * @author Florian Schmidt - Refactoring
 */
@NonNullByDefault
public interface OpenSprinklerApi {

    /**
     * Whether the device entered manual mode and accepts API requests to control the stations.
     *
     * @return True if this API interface is connected to the Open Sprinkler API. False otherwise.
     */
    public abstract boolean isManualModeEnabled();

    /**
     * Enters the "manual" mode of the device so that API requests are accepted.
     *
     * @throws Exception
     */
    public abstract void enterManualMode() throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Disables the manual mode, if it is enabled.
     *
     * @throws Exception
     */
    public abstract void leaveManualMode() throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Starts a station on the OpenSprinkler device for the specified duration.
     *
     * @param station Index of the station to open starting at 0.
     * @param duration The duration in seconds for how long the station should be turned on.
     * @throws Exception
     */
    public abstract void openStation(int station, BigDecimal duration)
            throws CommunicationApiException, GeneralApiException;

    /**
     * Closes a station on the OpenSprinkler device.
     *
     * @param station Index of the station to open starting at 0.
     * @throws Exception
     */
    public abstract void closeStation(int station) throws CommunicationApiException, GeneralApiException;

    /**
     * Returns the state of a station on the OpenSprinkler device.
     *
     * @param station Index of the station to open starting at 0.
     * @return True if the station is open, false if it is closed or cannot determine.
     * @throws Exception
     */
    public abstract boolean isStationOpen(int station) throws CommunicationApiException, GeneralApiException;

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
    public abstract boolean isRainDetected();

    /**
     * Returns the current draw of all connected zones of the OpenSprinkler device in milliamperes.
     *
     * @return current draw in milliamperes or -1 if sensor not supported
     */
    public abstract int currentDraw();

    /**
     * Returns the state of the second sensor.
     *
     * @return 1: sensor is active; 0: sensor is inactive; -1: no sensor.
     */
    public abstract int getSensor2State();

    /**
     *
     * @return The Wifi signal strength in -dB or 0 if not supported by firmware
     */
    public abstract int signalStrength();

    /**
     *
     * @return The pulses that the flow sensor has given in the last time period, -1 if not supported.
     */
    public abstract int flowSensorCount();

    /**
     * CLOSES all stations turning them all off.
     *
     */
    public abstract void resetStations() throws UnauthorizedApiException, CommunicationApiException;

    /**
     * Returns true if the internal programs are allowed to auto start.
     *
     * @return true if enabled
     */
    public abstract boolean getIsEnabled();

    public abstract void enablePrograms(Command command) throws UnauthorizedApiException, CommunicationApiException;

    /**
     * Returns the water level in %.
     *
     * @return waterLevel in %
     */
    public abstract int waterLevel();

    /**
     * Returns the number of total stations that are controllable from the OpenSprinkler
     * device.
     *
     * @return Number of stations as an int.
     */
    public abstract int getNumberOfStations();

    /**
     * Returns the firmware version number.
     *
     * @return The firmware version of the OpenSprinkler device as an int.
     * @throws Exception
     */
    public abstract int getFirmwareVersion() throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Sends all the GET requests and stores/cache the responses for use by the API to prevent the need for multiple
     * requests.
     *
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    public abstract void refresh() throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Ask the OpenSprinkler for the program names and store these for future use in a List.
     *
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    public abstract void getProgramData() throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Returns a list of all internal programs as a list of StateOptions.
     *
     * @return List<StateOption>
     */
    public abstract List<StateOption> getPrograms();

    /**
     * Return a list of all the stations the device has as List of StateOptions
     *
     * @return List<StateOption>
     */
    public abstract List<StateOption> getStations();

    /**
     * Runs a Program that is setup and stored inside the OpenSprinkler
     *
     * @param Program index number that you wish to run.
     *
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    public abstract void runProgram(Command command) throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Fetch the station names and place them in a list of List<StateOption>.
     * Use getStations() to retrieve this list.
     *
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    public abstract JnResponse getStationNames() throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Tells a single station to ignore the rain delay.
     *
     * @param station
     * @param command
     * @throws CommunicationApiException
     * @throws UnauthorizedApiException
     */
    public void ignoreRain(int station, boolean command) throws CommunicationApiException, UnauthorizedApiException;

    /**
     * Asks if a single station is set to ignore rain delays.
     *
     * @param station
     * @return
     */
    public abstract boolean isIgnoringRain(int station);

    /**
     * Sets how long the OpenSprinkler device will stop running programs for.
     *
     * @param hours
     * @throws UnauthorizedApiException
     * @throws CommunicationApiException
     */
    public abstract void setRainDelay(int hours) throws UnauthorizedApiException, CommunicationApiException;

    /**
     * Gets the rain delay in hours from the OpenSprinkler device.
     *
     * @return QuantityType<Time>
     */
    public abstract QuantityType<Time> getRainDelay();
}
