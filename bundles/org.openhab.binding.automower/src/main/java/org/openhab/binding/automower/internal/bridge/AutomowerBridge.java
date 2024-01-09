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
package org.openhab.binding.automower.internal.bridge;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.AutomowerConnectApi;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommand;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommandAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommandRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.binding.automower.internal.things.AutomowerCommand;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;

/**
 * The {@link AutomowerBridge} allows the communication to the various Husqvarna rest apis like the
 * AutomowerConnectApi or the AuthenticationApi
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerBridge {
    private final OAuthClientService authService;
    private final String appKey;

    private final AutomowerConnectApi automowerApi;

    public AutomowerBridge(OAuthClientService authService, String appKey, HttpClient httpClient,
            ScheduledExecutorService scheduler) {
        this.authService = authService;
        this.appKey = appKey;

        this.automowerApi = new AutomowerConnectApi(httpClient);
    }

    private AccessTokenResponse authenticate() throws AutomowerCommunicationException {
        try {
            AccessTokenResponse result = authService.getAccessTokenResponse();
            if (result == null || result.isExpired(Instant.now(), 120)) {
                result = authService.getAccessTokenByClientCredentials(null);
            }
            return result;
        } catch (OAuthException | IOException | OAuthResponseException e) {
            throw new AutomowerCommunicationException("Unable to authenticate", e);
        }
    }

    /**
     * @return A result containing a list of mowers that are available for the current user
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public MowerListResult getAutomowers() throws AutomowerCommunicationException {
        return automowerApi.getMowers(appKey, authenticate().getAccessToken());
    }

    /**
     * @param id The id of the mower to query
     * @return A detailed status of the mower with the specified id
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public Mower getAutomowerStatus(String id) throws AutomowerCommunicationException {
        return automowerApi.getMower(appKey, authenticate().getAccessToken(), id).getData();
    }

    /**
     * Sends a command to the automower with the specified id
     *
     * @param id The id of the mower
     * @param command The command that should be sent. Valid values are: "Start", "ResumeSchedule", "Pause", "Park",
     *            "ParkUntilNextSchedule", "ParkUntilFurtherNotice"
     * @param commandDuration The duration of the command. This is only evaluated for "Start" and "Park" commands
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerCommand(String id, AutomowerCommand command, long commandDuration)
            throws AutomowerCommunicationException {
        MowerCommandAttributes attributes = new MowerCommandAttributes();
        attributes.setDuration(commandDuration);

        MowerCommand mowerCommand = new MowerCommand();
        mowerCommand.setType(command.getCommand());
        mowerCommand.setAttributes(attributes);

        MowerCommandRequest request = new MowerCommandRequest();
        request.setData(mowerCommand);
        automowerApi.sendCommand(appKey, authenticate().getAccessToken(), id, request);
    }
}
