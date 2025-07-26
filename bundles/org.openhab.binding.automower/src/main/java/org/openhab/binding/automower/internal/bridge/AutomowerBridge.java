/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.AutomowerConnectApi;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Calendar;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.CalendarTask;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Mower;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCalendar;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCalendardRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommand;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommandAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerCommandRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerListResult;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerMessages;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerSettings;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerSettingsRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerStayOutZone;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerStayOutZoneAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerStayOutZoneRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerWorkArea;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerWorkAreaAttributes;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.MowerWorkAreaRequest;
import org.openhab.binding.automower.internal.rest.api.automowerconnect.dto.Settings;
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

    public synchronized AccessTokenResponse authenticate() throws AutomowerCommunicationException {
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
     * @param id The id of the mower to query
     * @return A detailed status of the mower with the specified id
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public MowerMessages getAutomowerMessages(String id) throws AutomowerCommunicationException {
        return automowerApi.getMowerMessages(appKey, authenticate().getAccessToken(), id).getData();
    }

    /**
     * Sends a command to the automower with the specified id
     *
     * @param id The id of the mower
     * @param command The command that should be sent. Valid values are: "Start", "ResumeSchedule", "Pause", "Park",
     *            "ParkUntilNextSchedule", "ParkUntilFurtherNotice"
     * @param commandDuration The duration of the command. This is only evaluated for "Start", "StartInWorkArea" and
     *            "Park" commands
     * @param commandWorkAreaId The work area id to be used for the command. This is only evaluated for
     *            "StartInWorkArea" command
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerCommand(String id, AutomowerCommand command, @Nullable Long commandWorkAreaId,
            @Nullable Long commandDuration) throws AutomowerCommunicationException {
        MowerCommand mowerCommand = new MowerCommand();
        mowerCommand.setType(command.getCommand());

        if (commandDuration != null || commandWorkAreaId != null) {
            MowerCommandAttributes attributes = new MowerCommandAttributes();
            if (commandDuration != null) {
                attributes.setDuration(commandDuration);
            }
            if (commandWorkAreaId != null) {
                attributes.setWorkAreaId(commandWorkAreaId);
            }
            mowerCommand.setAttributes(attributes);
        }

        MowerCommandRequest request = new MowerCommandRequest();
        request.setData(mowerCommand);
        automowerApi.sendCommand(appKey, authenticate().getAccessToken(), id, request);
    }

    /**
     * Sends a calendarTask to the automower
     *
     * @param id The id of the mower
     * @param hasWorkAreas Work area capability of the mower
     * @param workAreaId The Id of the work area this calendar belongs to (or null, if there is no work area support)
     * @param calendarTasks The calendar that should be sent. It is using the same json structure (start, duration, ...)
     *            as provided when reading the channel
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerCalendarTask(String id, boolean hasWorkAreas, @Nullable Long workAreaId,
            List<CalendarTask> calendarTasks) throws AutomowerCommunicationException {
        Calendar calendar = new Calendar();
        calendar.setTasks(calendarTasks);

        MowerCalendar mowerCalendar = new MowerCalendar();
        mowerCalendar.setType("calendar");
        mowerCalendar.setAttributes(calendar);

        MowerCalendardRequest calendarRequest = new MowerCalendardRequest();
        calendarRequest.setData(mowerCalendar);

        automowerApi.sendCalendar(appKey, authenticate().getAccessToken(), id, hasWorkAreas, workAreaId,
                calendarRequest);
    }

    /**
     * Sends Settings to the automower
     *
     * @param id The id of the mower
     * @param settings The Settings that should be sent. It is using the same json structure
     *            as provided when reading the channel
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerSettings(String id, Settings settings) throws AutomowerCommunicationException {
        MowerSettings mowerSettings = new MowerSettings();
        mowerSettings.setType("settings");
        mowerSettings.setAttributes(settings);

        MowerSettingsRequest settingsRequest = new MowerSettingsRequest();
        settingsRequest.setData(mowerSettings);

        automowerApi.sendSettings(appKey, authenticate().getAccessToken(), id, settingsRequest);
    }

    /**
     * Confirm current non fatal error on the mower
     *
     * @param id The id of the mower
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerConfirmError(String id) throws AutomowerCommunicationException {
        automowerApi.sendConfirmError(appKey, authenticate().getAccessToken(), id);
    }

    /**
     * Reset the cutting blade usage time
     *
     * @param id The id of the mower
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerResetCuttingBladeUsageTime(String id) throws AutomowerCommunicationException {
        automowerApi.sendResetCuttingBladeUsageTime(appKey, authenticate().getAccessToken(), id);
    }

    /**
     * Enable or disable stay out zone
     *
     * @param id The id of the mower
     * @param zoneId The id of the stay out zone
     * @param zoneAttributes The new zone status
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerStayOutZone(String id, String zoneId, MowerStayOutZoneAttributes zoneAttributes)
            throws AutomowerCommunicationException {
        MowerStayOutZone zoneData = new MowerStayOutZone();
        zoneData.setType("stayOutZone");
        zoneData.setId(zoneId);
        zoneData.setAttributes(zoneAttributes);
        MowerStayOutZoneRequest zoneRequest = new MowerStayOutZoneRequest();
        zoneRequest.setData(zoneData);

        automowerApi.sendStayOutZone(appKey, authenticate().getAccessToken(), id, zoneId, zoneRequest);
    }

    /**
     * Update a work area setting
     *
     * @param id The id of the mower
     * @param workAreaId The id of the work area
     * @param workAreaAttributes The new work area status
     * @throws AutomowerCommunicationException In case the query cannot be executed successfully
     */
    public void sendAutomowerWorkArea(String id, long workAreaId, MowerWorkAreaAttributes workAreaAttributes)
            throws AutomowerCommunicationException {
        MowerWorkArea workAreaData = new MowerWorkArea();
        workAreaData.setType("workArea");
        workAreaData.setId(workAreaId);
        workAreaData.setAttributes(workAreaAttributes);
        MowerWorkAreaRequest workAreaRequest = new MowerWorkAreaRequest();
        workAreaRequest.setData(workAreaData);

        automowerApi.sendWorkArea(appKey, authenticate().getAccessToken(), id, workAreaId, workAreaRequest);
    }
}
