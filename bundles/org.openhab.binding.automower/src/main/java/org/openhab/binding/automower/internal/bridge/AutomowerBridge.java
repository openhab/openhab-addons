/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.automower.internal.rest.api.authentication.AuthenticationApi;
import org.openhab.binding.automower.internal.rest.api.authentication.dto.PostOAuth2Response;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.AutomowerConnectApi;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommand;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommandAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommandRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.binding.automower.internal.rest.exceptions.AutomowerCommunicationException;
import org.openhab.binding.automower.internal.rest.exceptions.UnauthorizedException;

/**
 * The {@link AutomowerBridge} allows the communication to the various Husqvarna rest apis like the
 * AutomowerConnectApi or the AuthenticationApi
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerBridge {

    private final String appKey;
    private final String userName;
    private final String password;

    private @Nullable PostOAuth2Response authResponse;
    private final AutomowerConnectApi automowerApi;
    private final AuthenticationApi authApi;

    public AutomowerBridge(String appKey, String userName, String password, HttpClient httpClient,
            ScheduledExecutorService scheduler) {
        this.appKey = appKey;
        this.userName = userName;
        this.password = password;

        this.automowerApi = new AutomowerConnectApi(httpClient);
        this.authApi = new AuthenticationApi(httpClient);
    }

    private PostOAuth2Response authenticate() throws AutomowerCommunicationException {
        PostOAuth2Response result = authResponse;
        if (result == null) {
            result = authApi.loginOAuth2(appKey, userName, password);
            authResponse = result;
        }
        return result;
    }

    private PostOAuth2Response refreshAuthentication() throws AutomowerCommunicationException {
        if (authResponse == null) {
            throw new AutomowerCommunicationException("Unable to refresh authentication. Initial authentication has not been performed");
        } else {
            @SuppressWarnings("null")
            PostOAuth2Response result = authApi.loginWithRefreshToken(appKey, authResponse.getRefresh_token());
            authResponse = result;
            return result;
        }
    }

    /**
     * @return A result containing a list of mowers that are available for the current user
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public MowerListResult getAutomowers() throws AutomowerCommunicationException {
        try {
            return getAutomowersInt();
        } catch (UnauthorizedException e) {
            refreshAuthentication();
            return getAutomowersInt();
        }
    }

    /**
     * @param id The id of the mower to query
     * @return A detailed status of the mower with the specified id
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public Mower getAutomowerStatus(String id) throws AutomowerCommunicationException {
        try {
            return getAutomowerStatusInt(id);
        } catch (UnauthorizedException e) {
            refreshAuthentication();
            return getAutomowerStatusInt(id);
        }
    }

    /**
     * Sends a command to the automower with the specified id
     * 
     * @param id The id of the mower
     * @param command The command that should be sent. Valid values are: "Start", "ResumeSchedule", "Pause", "Park",
     *            "ParkUntilNextSchedule", "ParkUntilFurtherNotice"
     * @param commandDuration The duration of the command. This is only evaluated for "Start" and "Park" commands
     * @return true if the command was enqueued sucessfully
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public boolean sendAutomowerCommand(String id, String command, long commandDuration) throws AutomowerCommunicationException {
        try {
            return sendAutomowerCommandInt(id, command, commandDuration);
        } catch (UnauthorizedException e) {
            refreshAuthentication();
            return sendAutomowerCommandInt(id, command, commandDuration);
        }
    }

    private MowerListResult getAutomowersInt() throws AutomowerCommunicationException {
        return automowerApi.getMowers(appKey, authenticate().getAccess_token());
    }

    private Mower getAutomowerStatusInt(String id) throws AutomowerCommunicationException {
        return automowerApi.getMower(appKey, authenticate().getAccess_token(), id).getData();
    }

    private boolean sendAutomowerCommandInt(String id, String command, long commandDuration) throws AutomowerCommunicationException {
        MowerCommandAttributes attributes = new MowerCommandAttributes();
        attributes.setDuration(commandDuration);

        MowerCommand mowerCommand = new MowerCommand();
        mowerCommand.setType(command);
        mowerCommand.setAttributes(attributes);

        MowerCommandRequest request = new MowerCommandRequest();
        request.setData(mowerCommand);
        return automowerApi.sendCommand(appKey, authenticate().getAccess_token(), id, request);
    }
}
