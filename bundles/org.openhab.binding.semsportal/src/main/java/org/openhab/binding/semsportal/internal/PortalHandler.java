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
package org.openhab.binding.semsportal.internal;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.semsportal.internal.dto.BaseResponse;
import org.openhab.binding.semsportal.internal.dto.LoginRequest;
import org.openhab.binding.semsportal.internal.dto.LoginResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSToken;
import org.openhab.binding.semsportal.internal.dto.Station;
import org.openhab.binding.semsportal.internal.dto.StationListRequest;
import org.openhab.binding.semsportal.internal.dto.StationListResponse;
import org.openhab.binding.semsportal.internal.dto.StationStatus;
import org.openhab.binding.semsportal.internal.dto.StatusRequest;
import org.openhab.binding.semsportal.internal.dto.StatusResponse;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link PortalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class PortalHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(PortalHandler.class);
    // the settings that are needed when you do not have avalid session token yet
    private static final SEMSToken SESSIONLESS_TOKEN = new SEMSToken("v2.1.0", "ios", "en");
    // the url of the SEMS portal API
    private static final String BASE_URL = "https://www.semsportal.com/";
    // url for the login request, to get a valid session token
    private static final String LOGIN_URL = BASE_URL + "api/v2/Common/CrossLogin";
    // url to get the status of a specific power station
    private static final String STATUS_URL = BASE_URL + "api/v2/PowerStation/GetMonitorDetailByPowerstationId";
    private static final String LIST_URL = BASE_URL + "api/PowerStationMonitor/QueryPowerStationMonitorForApp";
    // the token holds the credential information for the portal
    private static final String HTTP_HEADER_TOKEN = "Token";
    private static final int REQUEST_TIMEOUT_MS = 10_000;

    // used to parse json from / to the SEMS portal API
    private final Gson gson;
    private final HttpClient httpClient;

    // configuration as provided by the openhab framework: initialize with defaults to prevent compiler check errors
    private SEMSPortalConfiguration config = new SEMSPortalConfiguration();
    private boolean loggedIn;
    private SEMSToken sessionToken = SESSIONLESS_TOKEN;// gets the default, it is needed for the login
    private @Nullable StationStatus currentStatus;

    private @Nullable ScheduledFuture<?> pollingJob;

    public PortalHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge);
        httpClient = httpClientFactory.getCommonHttpClient();
        gson = new GsonBuilder().create();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("No supported commands. Ignoring command {} for channel {}", command, channelUID);
        return;
    }

    @Override
    public void initialize() {
        config = getConfigAs(SEMSPortalConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                login();
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Error when loggin in. Check your username and password");
            }
        });
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localPollingJob = pollingJob;
        if (localPollingJob != null) {
            localPollingJob.cancel(true);
        }
        super.dispose();
    }

    private void login() {
        loggedIn = false;
        String payload = gson.toJson(new LoginRequest(config.username, config.password));
        String response = sendPost(LOGIN_URL, payload);
        if (response == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid response from SEMS portal");
            return;
        }
        LoginResponse loginResponse = gson.fromJson(response, LoginResponse.class);
        if (loginResponse == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Check username / password");
            return;
        }
        if (loginResponse.isOk()) {
            logger.debug("Successfuly logged in to SEMS portal");
            if (loginResponse.getToken() != null) {
                sessionToken = loginResponse.getToken();
            }
            loggedIn = true;
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Check username / password");
        }
    }

    private @Nullable String sendPost(String url, String payload) {
        try {
            Request request = httpClient.POST(url).timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HTTP_HEADER_TOKEN, gson.toJson(sessionToken))
                    .content(new StringContentProvider(payload, StandardCharsets.UTF_8.name()),
                            MediaType.APPLICATION_JSON);
            request.getHeaders().remove(HttpHeader.ACCEPT_ENCODING);
            ContentResponse response = request.send();
            logger.trace("received response: {}", response.getContentAsString());
            return response.getContentAsString();
        } catch (Exception e) {
            logger.debug("{} when posting to url {}", e.getClass().getSimpleName(), url, e);
        }
        return null;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public @Nullable StationStatus getStationStatus(String stationUUID)
            throws CommunicationException, ConfigurationException {
        if (!loggedIn) {
            logger.debug("Not logged in. Not updating.");
            return null;
        }
        String response = sendPost(STATUS_URL, gson.toJson(new StatusRequest(stationUUID)));
        if (response == null) {
            throw new CommunicationException("No response received from portal");
        }
        BaseResponse semsResponse = gson.fromJson(response, BaseResponse.class);
        if (semsResponse == null) {
            throw new CommunicationException("Portal reponse not understood");
        }
        if (semsResponse.isOk()) {
            StatusResponse statusResponse = gson.fromJson(response, StatusResponse.class);
            if (statusResponse == null) {
                throw new CommunicationException("Portal reponse not understood");
            }
            currentStatus = statusResponse.getStatus();
            updateStatus(ThingStatus.ONLINE); // we got a valid response, register as online
            return currentStatus;
        } else if (semsResponse.isSessionInvalid()) {
            logger.debug("Session is invalidated. Attempting new login.");
            login();
            return getStationStatus(stationUUID);
        } else if (semsResponse.isError()) {
            throw new ConfigurationException(
                    "ERROR status code received from SEMS portal. Please check your station ID");
        } else {
            throw new CommunicationException(String.format("Unknown status code received from SEMS portal: %s - %s",
                    semsResponse.getCode(), semsResponse.getMsg()));
        }
    }

    public long getUpdateInterval() {
        return config.interval;
    }

    public List<Station> getAllStations() {
        String payload = gson.toJson(new StationListRequest());
        String response = sendPost(LIST_URL, payload);
        if (response == null) {
            logger.debug("Received empty list stations response from SEMS portal");
            return Collections.emptyList();
        }
        StationListResponse listResponse = gson.fromJson(response, StationListResponse.class);
        if (listResponse == null) {
            logger.debug("Unable to read list stations response from SEMS portal");
            return Collections.emptyList();
        }
        if (listResponse.isOk()) {
            logger.debug("Received list of {} stations from SEMS portal", listResponse.getStations().size());
            loggedIn = true;
            updateStatus(ThingStatus.ONLINE);
            return listResponse.getStations();
        } else {
            logger.debug("Received error response with code {} and message {} from SEMS portal", listResponse.getCode(),
                    listResponse.getMsg());
            return Collections.emptyList();
        }
    }
}
