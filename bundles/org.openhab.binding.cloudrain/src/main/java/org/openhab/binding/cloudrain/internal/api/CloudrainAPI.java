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
package org.openhab.binding.cloudrain.internal.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cloudrain.internal.api.model.AuthParams;
import org.openhab.binding.cloudrain.internal.api.model.Controller;
import org.openhab.binding.cloudrain.internal.api.model.Irrigation;
import org.openhab.binding.cloudrain.internal.api.model.Zone;

/**
 * A Java representation of the Cloudrain Developer API as documented in
 * {@link https://developer.cloudrain.com/documentation/api-documentation}.
 *
 * @author Till Koellmann - Initial contribution
 *
 */
@NonNullByDefault
public interface CloudrainAPI {

    /**
     * Initialize the API and pass required control parameters.
     *
     * @param config the API configuration settings
     * @throws CloudrainAPIException the exception in case of an error
     */
    public void initialize(CloudrainAPIConfig config) throws CloudrainAPIException;

    /**
     * Authenticate to obtain a valid access token. The token will be managed by the API implementation
     * and used internally for subsequent API calls. Users of the API must authenticate first before executing other
     * requests.
     *
     * @param authParams the authentication parameters
     * @throws CloudrainAPIException the exception in case of an error
     */
    public void authenticate(AuthParams authParams) throws CloudrainAPIException;

    /**
     * Get the list of Cloudrain controllers managed in the user's account.
     *
     * @return the list of controllers
     * @throws CloudrainAPIException the exception in case of an error
     */
    public List<Controller> getControllers() throws CloudrainAPIException;

    /**
     * Get zone details for the given zone id.
     *
     * @param id the zone id
     * @return the zone details
     * @throws CloudrainAPIException the exception in case of an error
     */
    public @Nullable Zone getZone(String id) throws CloudrainAPIException;

    /**
     * Get all zones defined in the user's account.
     *
     * @return the list on zones
     * @throws CloudrainAPIException the exception in case of an error
     */
    public List<Zone> getZones() throws CloudrainAPIException;

    /**
     * Get the list of currently running irrigations for the account.
     *
     * @return the list of currently running irrigations. Empty list or Null if no irrigation is running
     * @throws CloudrainAPIException the exception in case of an error
     */
    public List<Irrigation> getIrrigations() throws CloudrainAPIException;

    /**
     * Get details of a currently running irrigations in a given zone.
     *
     * @param zoneId the zone id
     * @return the irrigation if an active irrigation exists. Null otherwise.
     * @throws CloudrainAPIException the exception in case of an error
     */
    public @Nullable Irrigation getIrrigation(String zoneId) throws CloudrainAPIException;

    /**
     * Start an irrigation in a given zone with a defined duration.
     *
     * @param zoneId the id of the zone in which the irrigation shall be started
     * @param duration the duration in seconds for the irrigation
     * @throws CloudrainAPIException the exception in case of an error
     */
    public void startIrrigation(String zoneId, int duration) throws CloudrainAPIException;

    /**
     * Adjust the duration of an active irrigation in a given zone. This call has no effect if no irrigation is
     * currently active
     * in that zone.
     *
     * @param zoneId the id of the zone in which the irrigation shall be adjusted
     * @param duration the new duration in seconds for the irrigation starting with the command execution
     * @throws CloudrainAPIException the exception in case of an error
     */
    public void adjustIrrigation(String zoneId, int duration) throws CloudrainAPIException;

    /**
     * Stops an active irrigation in a given zone. This call has no effect if no irrigation is currently active in that
     * zone.
     *
     * @param zoneId the id of the zone in which an active irrigation shall be stopped
     * @throws CloudrainAPIException the exception in case of an error
     */
    public void stopIrrigation(String zoneId) throws CloudrainAPIException;
}
