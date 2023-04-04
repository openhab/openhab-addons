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
package org.openhab.binding.ring.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.json.simple.parser.ParseException;
import org.openhab.binding.ring.internal.ApiConstants;
import org.openhab.binding.ring.internal.RestClient;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.RingVideoServlet;
import org.openhab.binding.ring.internal.data.Profile;
import org.openhab.binding.ring.internal.data.RingDevices;
import org.openhab.binding.ring.internal.data.RingEvent;
import org.openhab.binding.ring.internal.errors.AuthenticationException;
import org.openhab.binding.ring.internal.errors.DuplicateIdException;
import org.openhab.binding.ring.internal.utils.RingUtils;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.http.HttpService;

/**
 * The {@link RingDoorbellHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 * @author Peter Mietlowski - oAuth upgrade and additional maintenance
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

public class AccountHandler extends AbstractRingHandler implements RingAccount {

    private ScheduledFuture<?> jobTokenRefresh = null;
    private ScheduledFuture<?> eventRefresh = null;
    private Runnable runnableToken = null;
    private Runnable runnableEvent = null;
    private @Nullable RingVideoServlet ringVideoServlet;
    private @Nullable HttpService httpService;
    /**
     * The user profile retrieved when authenticating.
     */
    private Profile userProfile;
    /**
     * The registry.
     */
    private RingDeviceRegistry registry;
    /**
     * The RestClient is used to connect to the Ring Account.
     */
    private RestClient restClient;
    /**
     * The list with events.
     */
    private List<RingEvent> lastEvents;
    /**
     * The index to the current event.
     */
    private int eventIndex;

    /*
     * The number of video files to keep when auto-downloading
     */
    private int videoRetentionCount;

    /*
     * The path of where to save video files
     */
    private String videoStoragePath;

    private NetworkAddressService networkAddressService;

    private int httpPort;

    public AccountHandler(Thing thing, NetworkAddressService networkAddressService, HttpService httpService,
            int httpPort) {
        super(thing);
        this.httpPort = httpPort;
        this.networkAddressService = networkAddressService;
        this.httpService = httpService;
        eventIndex = 0;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            boolean eventListOk = lastEvents != null && lastEvents.size() > eventIndex;
            switch (channelUID.getId()) {
                case CHANNEL_EVENT_URL:
                    if (eventListOk) {
                        String videoFile = restClient.downloadEventVideo(lastEvents.get(eventIndex), userProfile,
                                videoStoragePath, videoRetentionCount);
                        String localIP = networkAddressService.getPrimaryIpv4HostAddress();

                        if (videoFile.endsWith(".mp4")) {
                            updateState(channelUID,
                                    new StringType("http://" + localIP + ":" + httpPort + "/ring/video/" + videoFile));
                        } else {
                            updateState(channelUID, new StringType(videoFile));
                        }
                    }
                    break;
                case CHANNEL_EVENT_CREATED_AT:
                    if (eventListOk) {
                        updateState(channelUID, new DateTimeType(lastEvents.get(eventIndex).getCreatedAt()));
                    }
                    break;
                case CHANNEL_EVENT_KIND:
                    if (eventListOk) {
                        updateState(channelUID, new StringType(lastEvents.get(eventIndex).getKind()));
                    }
                    break;
                case CHANNEL_EVENT_DOORBOT_ID:
                    if (eventListOk) {
                        updateState(channelUID, new StringType(lastEvents.get(eventIndex).getDoorbot().getId()));
                    }
                    break;
                case CHANNEL_EVENT_DOORBOT_DESCRIPTION:
                    if (eventListOk) {
                        updateState(channelUID,
                                new StringType(lastEvents.get(eventIndex).getDoorbot().getDescription()));
                    }
                    break;
                /*
                 * case CHANNEL_CONTROL_STATUS:
                 * updateState(channelUID, status);
                 * break;
                 */
                case CHANNEL_CONTROL_ENABLED:
                    updateState(channelUID, enabled);
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
            refreshState();
        } else if (command instanceof OnOffType) {
            OnOffType xcommand = (OnOffType) command;
            switch (channelUID.getId()) {
                /*
                 * case CHANNEL_CONTROL_STATUS:
                 * status = xcommand;
                 * updateState(channelUID, status);
                 * break;
                 */
                case CHANNEL_CONTROL_ENABLED:
                    if (!enabled.equals(xcommand)) {
                        enabled = xcommand;
                        updateState(channelUID, enabled);
                        if (enabled.equals(OnOffType.ON)) {
                            Configuration config = getThing().getConfiguration();
                            Integer refreshInterval = ((BigDecimal) config.get("refreshInterval")).intValueExact();
                            startAutomaticRefresh(refreshInterval);
                        } else {
                            stopAutomaticRefresh();
                        }
                    }
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    /**
     * Refresh the state of channels that may have changed by (re-)initialization.
     */
    @Override
    protected void refreshState() {
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Ring Account handler");
        super.initialize();

        AccountConfiguration config = getConfigAs(AccountConfiguration.class);
        Integer refreshInterval = config.refreshInterval;
        String username = config.username;
        String password = config.password;
        String hardwareId = config.hardwareId;
        String refreshToken = config.refreshToken;

        String twofactorCode = config.twofactorCode;
        videoRetentionCount = config.videoRetentionCount;
        videoStoragePath = config.videoStoragePath;

        try {
            Configuration updatedConfiguration = getThing().getConfiguration();

            if (hardwareId.isEmpty()) {
                hardwareId = getLocalMAC();
                if ((hardwareId == null) || hardwareId.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Hardware ID missing, check thing config");
                    return;
                }
                // write hardwareId to thing config
                config.hardwareId = hardwareId;
                updatedConfiguration.put("hardwareId", config.hardwareId);
            }
            restClient = new RestClient();
            logger.debug("Logging in with refresh token: {}", RingUtils.sanitizeData(refreshToken));
            userProfile = restClient.getAuthenticatedProfile(username, password, refreshToken, twofactorCode,
                    hardwareId);
            config.refreshToken = userProfile.getRefreshToken();
            updatedConfiguration.put("refreshToken", config.refreshToken);
            if (!config.refreshToken.equals(userProfile.getRefreshToken())) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error saving refresh token to account Thing. See log for details.");
                logger.error(
                        "Error saving refresh token to account Thing. If created with .thing files, add this refreshToken attribute: {}",
                        userProfile.getRefreshToken());
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Retrieving device list");
            config.twofactorCode = "";
            updatedConfiguration.put("twofactorCode", config.twofactorCode);
            updateConfiguration(updatedConfiguration);

            if (this.ringVideoServlet == null) {
                this.ringVideoServlet = new RingVideoServlet(httpService, videoStoragePath);
            }

            // Note: When initialization can NOT be done set the status with more details for further
            // analysis. See also class ThingStatusDetail for all available status details.
            // Add a description to give user information to understand why thing does not work
            // as expected. E.g.
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
            // "Can not access device as username and/or password are invalid");
            startAutomaticRefresh(refreshInterval);
            startSessionRefresh(refreshInterval);
        } catch (AuthenticationException ex) {
            logger.debug("AuthenticationException when initializing Ring Account handler{}", ex.getMessage());
            if (ex.getMessage().startsWith("Two factor")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }
        } catch (ParseException e) {
            logger.debug("Invalid response from api.ring.com when initializing Ring Account handler{}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid response from api.ring.com");
        } catch (Exception e) {
            logger.debug("Initialization failed when initializing Ring Account handler{}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Initialization failed: " + e.getMessage());
        }
    }

    private void refreshRegistry() throws ParseException, AuthenticationException, DuplicateIdException {
        RingDevices ringDevices = restClient.getRingDevices(userProfile, this);
        registry = RingDeviceRegistry.getInstance();
        registry.addRingDevices(ringDevices.getRingDevices());
    }

    @Override
    protected void minuteTick() {
        try {
            // Init the devices
            refreshRegistry();
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException | ParseException e) {
            logger.debug(
                    "AuthenticationException in AccountHandler.minuteTick() when trying refreshRegistry, attempting to reconnect {}",
                    e.getMessage());
            AccountConfiguration config = getConfigAs(AccountConfiguration.class);
            String username = config.username;
            String password = config.password;
            String hardwareId = config.hardwareId;
            String refreshToken = config.refreshToken;

            try {
                userProfile = restClient.getAuthenticatedProfile(username, password, refreshToken, null, hardwareId);
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Retrieving device list");
            } catch (AuthenticationException ex) {
                logger.debug("RestClient reported AuthenticationException trying getAuthenticatedProfile: {}",
                        ex.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid credentials");
            } catch (ParseException e1) {
                logger.debug("RestClient reported ParseException trying getAuthenticatedProfile: {}", e1.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid response from api.ring.com");
            } finally {
                try {
                    refreshRegistry();
                    updateStatus(ThingStatus.ONLINE);
                } catch (DuplicateIdException ignored) {
                    updateStatus(ThingStatus.ONLINE);
                } catch (AuthenticationException ae) {
                    registry = null;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "AuthenticationException response from ring.com");
                    logger.debug("RestClient reported AuthenticationException in finally block: {}", ae.getMessage());
                } catch (ParseException pe1) {
                    registry = null;
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "ParseException response from ring.com");
                    logger.debug("RestClient reported ParseException in finally block: {}", pe1.getMessage());
                }
            }
        } catch (DuplicateIdException ignored) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void eventTick() {
        try {
            String id = lastEvents == null || lastEvents.isEmpty() ? "?" : lastEvents.get(0).getEventId();
            lastEvents = restClient.getHistory(userProfile, 1);
            logger.debug("Event id: {} lastEvents: {}", id, lastEvents.get(0).getEventId().equals(id));
            if (lastEvents != null && !lastEvents.isEmpty() && !lastEvents.get(0).getEventId().equals(id)) {
                logger.debug("New Event");
                updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_CREATED_AT),
                        new DateTimeType(lastEvents.get(0).getCreatedAt()));
                updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_KIND),
                        new StringType(lastEvents.get(0).getKind()));
                updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_DOORBOT_ID),
                        new StringType(lastEvents.get(0).getDoorbot().getId()));
                updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_DOORBOT_DESCRIPTION),
                        new StringType(lastEvents.get(0).getDoorbot().getDescription()));
                StringBuilder vidUrl = new StringBuilder();
                vidUrl.append(ApiConstants.URL_RECORDING_START).append(lastEvents.get(0).getEventId())
                        .append(ApiConstants.URL_RECORDING_END);
                updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_URL), new StringType(vidUrl.toString()));
            }
        } catch (AuthenticationException ex) {
            // registry = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "AuthenticationException response from ring.com");
            logger.debug(
                    "RestClient reported AuthenticationExceptionfrom api.ring.com when retrying refreshRegistry for the second time: {}",
                    ex.getMessage());
        } catch (ParseException ignored) {
            logger.debug(
                    "RestClient reported ParseException api.ring.com when retrying refreshRegistry for the second time: {}",
                    ignored.getMessage());

        }
    }

    /**
     * Refresh the profile every 20 minutes
     */
    protected void startSessionRefresh(int refreshInterval) {
        logger.debug("startSessionRefresh {}", refreshInterval);
        runnableToken = new Runnable() {
            @Override
            public void run() {
                try {
                    if (restClient != null) {
                        if (registry != null) {
                            refreshRegistry();
                        }
                        // restClient.refresh_session(userProfile.getRefreshToken());
                        Configuration config = getThing().getConfiguration();
                        String hardwareId = (String) config.get("hardwareId");
                        userProfile = restClient.getAuthenticatedProfile(null, null, userProfile.getRefreshToken(),
                                null, hardwareId);
                    }
                } catch (Exception e) {
                    logger.debug(
                            "AccountHandler - startSessionRefresh - Exception occurred during execution of refreshRegistry(): {}",
                            e.getMessage(), e);
                }
            }
        };

        runnableEvent = new Runnable() {
            @Override
            public void run() {
                try {
                    eventTick();
                } catch (final Exception e) {
                    logger.debug(
                            "AccountHandler - startSessionRefresh - Exception occurred during execution of eventTick(): {}",
                            e.getMessage(), e);
                }
            }
        };

        jobTokenRefresh = scheduler.scheduleWithFixedDelay(runnableToken, 90, 600, TimeUnit.SECONDS);
        eventRefresh = scheduler.scheduleWithFixedDelay(runnableEvent, refreshInterval, refreshInterval,
                TimeUnit.SECONDS);
    }

    protected void stopSessionRefresh() {
        if (jobTokenRefresh != null) {
            jobTokenRefresh.cancel(true);
            jobTokenRefresh = null;
        }
        if (eventRefresh != null) {
            eventRefresh.cancel(true);
            eventRefresh = null;
        }
    }

    String getLocalMAC() throws Exception {
        // get local ip from OH system settings
        String localIP = networkAddressService.getPrimaryIpv4HostAddress();
        if ((localIP == null) || (localIP.isEmpty())) {
            logger.debug("No local IP selected in openHAB system configuration");
            return "";
        }

        // get MAC address
        InetAddress ip = InetAddress.getByName(localIP);
        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
        if (network != null) {
            byte[] mac = network.getHardwareAddress();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            String localMAC = sb.toString();
            logger.debug("Local IP address='{}', local MAC address = '{}'", localIP, localMAC);
            return localMAC;
        }
        return "";
    }

    @Override
    public RestClient getRestClient() {
        return restClient;
    }

    @Override
    public Profile getProfile() {
        return userProfile;
    }

    /**
     * Dispose off the refreshJob nicely.
     */
    @Override
    public void dispose() {
        stopSessionRefresh();
        super.dispose();
    }
}
