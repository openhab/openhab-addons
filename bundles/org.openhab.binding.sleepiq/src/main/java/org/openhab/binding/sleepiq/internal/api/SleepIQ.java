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
package org.openhab.binding.sleepiq.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.sleepiq.internal.api.dto.Bed;
import org.openhab.binding.sleepiq.internal.api.dto.FamilyStatusResponse;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationFeaturesResponse;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationStatusResponse;
import org.openhab.binding.sleepiq.internal.api.dto.LoginInfo;
import org.openhab.binding.sleepiq.internal.api.dto.PauseModeResponse;
import org.openhab.binding.sleepiq.internal.api.dto.SleepDataResponse;
import org.openhab.binding.sleepiq.internal.api.dto.Sleeper;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuator;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationActuatorSpeed;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutlet;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationOutletOperation;
import org.openhab.binding.sleepiq.internal.api.enums.FoundationPreset;
import org.openhab.binding.sleepiq.internal.api.enums.Side;
import org.openhab.binding.sleepiq.internal.api.enums.SleepDataInterval;
import org.openhab.binding.sleepiq.internal.api.impl.SleepIQImpl;

/**
 * This interface is the main API to access the SleepIQ system.
 *
 * @author Gregory Moyer - Initial contribution
 * @author Mark Hilbush - Added foundation functionality
 */
@NonNullByDefault
public interface SleepIQ {
    /**
     * Login to the {@link Configuration configured} account. This method is not
     * required to be called before other methods because all methods must
     * ensure login before acting. However, when the only desired action is to
     * login and not retrieve other data, this method is the most efficient
     * option.
     *
     * @return basic information about the logged in user
     * @throws UnauthorizedException
     *             if the credentials provided are not valid
     * @throws LoginException
     *             if the login request fails for any reason other than bad
     *             credentials (including missing credentials)
     */
    @Nullable
    LoginInfo login() throws LoginException, UnauthorizedException;

    /**
     * Get a list of beds connected to the account.
     *
     * @return the list of beds
     * @throws LoginException
     *             if the login request fails for any reason other than bad
     *             credentials (including missing credentials)
     * @throws SleepIQException
     */
    List<Bed> getBeds() throws LoginException, SleepIQException, BedNotFoundException;

    /**
     * Get a list of sleepers registered to this account for beds or bed positions
     * (left or right side).
     *
     * @return the list of sleepers
     * @throws LoginException
     * @throws SleepIQException
     */
    List<Sleeper> getSleepers() throws LoginException, SleepIQException;

    /**
     * Get the status of all beds and all air chambers registered to this
     * account.
     *
     * @return the complete status of beds on the account
     * @throws LoginException
     * @throws SleepIQException
     */
    FamilyStatusResponse getFamilyStatus() throws LoginException, SleepIQException;

    /**
     * Get the Sleep Data for a sleeper registered to this account.
     *
     * @param sleeperId the sleeper Id to query
     * @param interval The time period for which data is to be queried
     * @return the Sleep Data
     * @throws BedNotFoundException
     *             if the bed identifier was not found on the account
     * @throws LoginException
     * @throws SleepIQException
     */
    SleepDataResponse getSleepData(String sleeperId, SleepDataInterval interval)
            throws BedNotFoundException, LoginException, SleepIQException;

    /**
     * Get the status of "pause mode" (disabling SleepIQ data upload) for a
     * specific bed. A bed in pause mode will send no information to the SleepIQ
     * cloud services. For example, if a sleeper is in bed and disables SleepIQ
     * (enables pause mode), the service will continue to report that the bed is
     * occupied even after the sleeper exits the bed until pause mode is
     * disabled.
     *
     * @return the status of pause mode for the specified bed
     * @throws BedNotFoundException
     *             if the bed identifier was not found on the account
     * @throws LoginException
     * @throws SleepIQException
     */
    PauseModeResponse getPauseMode(String bedId) throws BedNotFoundException, LoginException, SleepIQException;

    /**
     * Set the sleep number for a chamber of a bed
     *
     * @param bedId the unique identifier of the bed
     * @param side thethe chamber of the bed
     * @param sleepNumber the new sleep number
     *
     * @throws LoginException
     * @throws SleepIQException
     */
    void setSleepNumber(String bedId, Side side, int sleepNumber) throws LoginException, SleepIQException;

    /**
     * Set the pause (privacy) mode for a bed
     *
     * @param bedId the unique identifier of the bed
     *
     * @throws LoginException
     * @throws SleepIQException
     */
    void setPauseMode(String bedId, boolean command) throws LoginException, SleepIQException;

    /**
     * Get the foundation features for a bed
     *
     * @param bedId the unique identifier of the bed
     *
     * @throws LoginException
     * @throws SleepIQException
     */
    FoundationFeaturesResponse getFoundationFeatures(String bedId) throws LoginException, SleepIQException;

    /**
     * Get the foundation status for a bed
     *
     * @param bedId the unique identifier of the bed
     *
     * @throws LoginException
     * @throws SleepIQException
     */
    FoundationStatusResponse getFoundationStatus(String bedId) throws LoginException, SleepIQException;

    /**
     * Set a foundation preset for the side of a bed
     *
     * @param bedId the unique identifier of the bed
     * @param side the chamber of the bed
     * @param preset the foundation preset
     * @param speed the speed with which the adjustment is made
     *
     * @throws LoginException
     * @throws SleepIQException
     */
    void setFoundationPreset(String bedId, Side side, FoundationPreset preset, FoundationActuatorSpeed speed)
            throws LoginException, SleepIQException;

    /**
     * Set a foundation position for the side of a bed
     *
     * @param bedId the unique identifier of the bed
     * @param side the chamber of the bed
     * @param actuator the head or foot of the bed
     * @param position the new position of the actuator
     * @param speed the speed with which the adjustment is made
     *
     * @throws LoginException
     * @throws SleepIQException
     */
    void setFoundationPosition(String bedId, Side side, FoundationActuator actuator, int position,
            FoundationActuatorSpeed speed) throws LoginException, SleepIQException;

    /**
     * Turn a foundation outlet on or off
     *
     * @param bedId the unique identifier of the bed
     * @param outlet the foundation outlet to control
     * @param operation set the outlet on or off
     *
     * @throws LoginException
     * @throws SleepIQException
     */
    void setFoundationOutlet(String bedId, FoundationOutlet outlet, FoundationOutletOperation operation)
            throws LoginException, SleepIQException;

    /**
     * Create a default implementation instance of this interface. Each call to
     * this method will create a new object.
     *
     * @param config the configuration to use for the new instance
     * @param httpClient handle to the Jetty http client
     * @return a concrete implementation of this interface
     */
    static SleepIQ create(Configuration config, HttpClient httpClient) {
        return new SleepIQImpl(config, httpClient);
    }

    /**
     * Close down the cloud service
     */
    void shutdown();
}
