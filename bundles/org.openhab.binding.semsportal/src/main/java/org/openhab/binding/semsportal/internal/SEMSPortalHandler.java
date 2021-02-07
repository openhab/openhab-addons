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
package org.openhab.binding.semsportal.internal;

import static org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
import org.openhab.binding.semsportal.internal.dto.SEMSLoginResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSStatus;
import org.openhab.binding.semsportal.internal.dto.SEMSStatusResponse;
import org.openhab.binding.semsportal.internal.dto.SEMSToken;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link SEMSPortalHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class SEMSPortalHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(SEMSPortalHandler.class);
    // the settings that are needed when you do not have avalid session token yet
    private static final SEMSToken SESSIONLESS_TOKEN = new SEMSToken("v2.1.0", "ios", "en");
    // the url of the SEMS portal API
    private static final String BASE_URL = "https://www.semsportal.com/";
    // url for the login request, to get a valid session token
    private static final String LOGIN_URL = BASE_URL + "api/v2/Common/CrossLogin";
    // url to get the status of a specific power station
    private static final String STATUS_URL = BASE_URL + "api/v2/PowerStation/GetMonitorDetailByPowerstationId";
    // the token holds the credential information for the portal
    private static final String HTTP_HEADER_TOKEN = "Token";
    // cache the status for this amount of minutes, get a new status if the status
    // is this amount of minutes old
    private static final long MAX_STATUS_AGE_MINUTES = 1;

    // used to parse json from / to the SEMS portal API
    private final Gson gson;
    private final HttpClient httpClient;

    // configuration as provided by the openhab framework: initialize with defaults to prevent compiler check errors
    private SEMSPortalConfiguration config = new SEMSPortalConfiguration();
    private boolean loggedIn;
    private boolean stationOnline = false;
    private SEMSToken sessionToken = SESSIONLESS_TOKEN;// gets the default, it is needed for the login
    private @Nullable SEMSStatus currentStatus;

    private LocalDateTime lastUpdate = LocalDateTime.MIN;
    private @Nullable ScheduledFuture<?> pollingJob;

    public SEMSPortalHandler(Thing thing, HttpClientFactory httpClientFactory) {
        super(thing);
        httpClient = httpClientFactory.getCommonHttpClient();
        gson = new GsonBuilder().setDateFormat(SEMSPortalBindingConstants.DATE_FORMAT).create();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!loggedIn) {
            logger.debug("Not logged in. Ignoring command {} for channel {}", command, channelUID);
            return;
        }
        if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                updateChannelState(channelUID);
            });
        }
    }

    private void updateChannelState(ChannelUID channelUID) {
        if (!loggedIn) {
            logger.debug("Not logged in. Ignoring update request for channel {}", channelUID.getAsString());
            return;
        }
        ensureRecentStatus();
        if (CHANNEL_CURRENT_OUTPUT.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getCurrentOutput(currentStatus));
        } else if (CHANNEL_TODAY_TOTAL.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getDayTotal(currentStatus));
        } else if (CHANNEL_MONTH_TOTAL.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getMonthTotal(currentStatus));
        } else if (CHANNEL_OVERALL_TOTAL.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getOverallTotal(currentStatus));
        } else if (CHANNEL_ONLINE_STATE.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getOnline(currentStatus));
            if (currentStatus != null && currentStatus.isOnline() != stationOnline) {
                stationOnline = currentStatus.isOnline();
                triggerChannel(channelUID, OnOffType.from(stationOnline).name());
            }
        } else if (CHANNEL_TODAY_INCOME.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getDayIncome(currentStatus));
        } else if (CHANNEL_TOTAL_INCOME.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getTotalIncome(currentStatus));
        } else if (CHANNEL_LASTUPDATE.equals(channelUID.getId())) {
            updateState(channelUID.getId(), StateHelper.getLastUpdate(currentStatus));
        }
    }

    private void ensureRecentStatus() {
        if (lastUpdate.isBefore(LocalDateTime.now().minusMinutes(MAX_STATUS_AGE_MINUTES))) {
            updateStation();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SEMSPortalConfiguration.class);
        if (!config.isProperlyInitialized()) {
            logger.warn("Username, password or station missing. Please provide the right values in the configuration.");
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Check username / password / station settings");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            try {
                login();
                if (loggedIn) {
                    pollingJob = scheduler.scheduleWithFixedDelay(this::updateStation, 0, config.update,
                            TimeUnit.MINUTES);
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                        "Error when loggin in. Check your username and password");
            }
        });
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        super.dispose();
    }

    private void login() {
        loggedIn = false;
        String payload = gson.toJson(new LoginRequestBody(config.username, config.password));
        String response = sendPost(LOGIN_URL, payload);
        if (response == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid response from SEMS portal");
            return;
        }
        SEMSLoginResponse loginResponse = gson.fromJson(response, SEMSLoginResponse.class);
        if (loginResponse == null) {
            logger.debug("SEMSPortal answer not understood: {}", loginResponse);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Check username / password");
            return;
        }
        if (loginResponse.isOk()) {
            logger.info("Successfuly logged in to SEMS portal");
            if (loginResponse.getToken() != null) {
                sessionToken = loginResponse.getToken();
            }
            loggedIn = true;
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.warn("Invalid credentials provided. Server returned error.");
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.CONFIGURATION_ERROR, "Check username / password");
        }
    }

    private void updateStation() {
        String response = sendPost(STATUS_URL, gson.toJson(new StatusRequestBody(config.station)));
        if (response == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid response from SEMS portal");
            return;
        }
        SEMSResponse semsResponse = gson.fromJson(response, SEMSResponse.class);
        if (semsResponse == null) {
            logger.debug("SEMSPortal answer not uderstood: {}", response);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid response from SEMS portal");
            return;
        }
        if (semsResponse.isOk()) {
            SEMSStatusResponse statusResponse = gson.fromJson(response, SEMSStatusResponse.class);
            if (statusResponse == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid response from SEMS portal");
                return;
            }
            lastUpdate = LocalDateTime.now();
            currentStatus = statusResponse.getStatus();
            updateStatus(ThingStatus.ONLINE);
            updateAllChannels();
        } else if (semsResponse.isSessionInvalid()) {
            logger.debug("Session is invalidated. Attempting new login.");
            login();
            updateStation();
        } else if (semsResponse.isError()) {
            String errorMessage = "ERROR status code received from SEMS portal. Please check your station ID";
            logger.error(errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
        } else {
            String errorMessage = String.format("Unknown status code received from SEMS portal: %s - %s",
                    semsResponse.getCode(), semsResponse.getMsg());
            logger.error(errorMessage);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    private void updateAllChannels() {
        for (String channelName : ALL_CHANNELS) {
            Channel channel = thing.getChannel(channelName);
            if (channel != null) {
                updateChannelState(channel.getUID());
            }
        }
    }

    private @Nullable String sendPost(String url, String payload) {
        try {
            Request request = httpClient.POST(url).header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header(HTTP_HEADER_TOKEN, gson.toJson(sessionToken))
                    .content(new StringContentProvider(payload, StandardCharsets.UTF_8.name()), "application/json");
            request.getHeaders().remove(HttpHeader.ACCEPT_ENCODING);
            ContentResponse response = request.send();
            logger.debug("received response: {}", response.getContentAsString());
            return response.getContentAsString();
        } catch (Exception e) {
            logger.debug("{} when posting to url {}", e.getClass().getSimpleName(), url, e);
        }
        return null;
    }
}
