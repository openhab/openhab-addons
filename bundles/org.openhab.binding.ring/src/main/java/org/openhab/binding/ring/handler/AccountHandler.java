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
package org.openhab.binding.ring.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ring.internal.RestClient;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.RingVideoServlet;
import org.openhab.binding.ring.internal.data.Profile;
import org.openhab.binding.ring.internal.data.RingDevices;
import org.openhab.binding.ring.internal.data.RingEventTO;
import org.openhab.binding.ring.internal.errors.AuthenticationException;
import org.openhab.binding.ring.internal.errors.DuplicateIdException;
import org.openhab.binding.ring.internal.utils.RingUtils;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.ConfigParser;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link AccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Wim Vissers - Initial contribution
 * @author Peter Mietlowski - oAuth upgrade and additional maintenance
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler implements RingAccount {

    private @Nullable ScheduledFuture<?> jobTokenRefresh = null;
    private @Nullable ScheduledFuture<?> eventRefresh = null;
    private @Nullable Runnable runnableVideo = null;
    private @Nullable RingVideoServlet ringVideoServlet;
    private final HttpService httpService;
    private final String thingId;

    // Current status
    protected OnOffType status = OnOffType.OFF;
    protected OnOffType enabled = OnOffType.ON;
    protected final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    // Scheduler
    protected @Nullable ScheduledFuture<?> refreshJob;

    /**
     * The user profile retrieved when authenticating.
     */
    private Profile userProfile = new Profile();
    /**
     * The registry.
     */
    private final RingDeviceRegistry registry = RingDeviceRegistry.getInstance();
    /**
     * The RestClient is used to connect to the Ring Account.
     */
    private RestClient restClient = new RestClient();
    /**
     * The list with events.
     */
    private List<RingEventTO> lastEvents = List.of();
    /**
     * The index to the current event.
     */
    private int eventIndex = 0;

    private @Nullable ExecutorService videoExecutorService;

    /*
     * The number of video files to keep when auto-downloading
     */
    private int videoRetentionCount;

    /*
     * The path of where to save video files
     */
    private String videoStoragePath = "";

    private final NetworkAddressService networkAddressService;

    private final int httpPort;

    public AccountHandler(Bridge bridge, NetworkAddressService networkAddressService, HttpService httpService,
            int httpPort) {
        super(bridge);
        this.httpPort = httpPort;
        this.networkAddressService = networkAddressService;
        this.httpService = httpService;
        this.videoExecutorService = Executors.newCachedThreadPool();
        this.thingId = this.getThing().getUID().getId();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            boolean eventListOk = lastEvents.size() > eventIndex;
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
                        updateState(channelUID, lastEvents.get(eventIndex).getCreatedAt());
                    }
                    break;
                case CHANNEL_EVENT_KIND:
                    if (eventListOk) {
                        updateState(channelUID, new StringType(lastEvents.get(eventIndex).kind));
                    }
                    break;
                case CHANNEL_EVENT_DOORBOT_ID:
                    if (eventListOk) {
                        updateState(channelUID, new StringType(lastEvents.get(eventIndex).doorbot.id));
                    }
                    break;
                case CHANNEL_EVENT_DOORBOT_DESCRIPTION:
                    if (eventListOk) {
                        updateState(channelUID, new StringType(lastEvents.get(eventIndex).doorbot.description));
                    }
                    break;
                case CHANNEL_CONTROL_ENABLED:
                    updateState(channelUID, enabled);
                    break;
                default:
                    logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                    break;
            }
        } else if (command instanceof OnOffType xcommand) {
            switch (channelUID.getId()) {
                case CHANNEL_CONTROL_ENABLED:
                    if (!enabled.equals(xcommand)) {
                        enabled = xcommand;
                        updateState(channelUID, enabled);
                        if (enabled.equals(OnOffType.ON)) {
                            Configuration config = getThing().getConfiguration();
                            int refreshInterval = ConfigParser.valueAsOrElse(config.get("refreshInterval"),
                                    BigDecimal.class, BigDecimal.valueOf(500)).intValue();

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

    private void refresh() {
        try {
            minuteTick();
        } catch (final Exception e) {
            logger.debug("AbstractHandler - Exception occurred during execution of startAutomaticRefresh(): {}",
                    e.getMessage(), e);
        }
    }

    protected void startAutomaticRefresh(final int refreshInterval) {
        refreshJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, refreshInterval, TimeUnit.SECONDS);
    }

    protected void stopAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    private void saveRefreshTokenToFile(String refreshToken) {
        String folderName = OpenHAB.getUserDataFolder() + "/ring";
        String thingId = this.thingId;
        File folder = new File(folderName);
        String fileName = folderName + "/ring." + thingId + ".refreshToken";

        if (!folder.exists()) {
            logger.debug("Creating directory {}", folderName);
            folder.mkdirs();
        }
        try {
            Files.write(Paths.get(fileName), refreshToken.getBytes());
        } catch (IOException ex) {
            logger.debug("IOException when writing refreshToken to file {}", ex.getMessage());
        }
        logger.debug("saveRefreshTokenToFile Successful {}", RingUtils.sanitizeData(refreshToken));
    }

    private String getRefreshTokenFromFile() {
        String refreshToken = "";
        String folderName = OpenHAB.getUserDataFolder() + "/ring";
        String thingId = this.thingId;
        String fileName = folderName + "/ring." + thingId + ".refreshToken";
        File file = new File(fileName);
        if (!file.exists()) {
            return refreshToken;
        }
        try {
            final byte[] contents = Files.readAllBytes(Paths.get(fileName));
            refreshToken = new String(contents);
        } catch (IOException ex) {
            logger.debug("IOException when reading refreshToken from file {}", ex.getMessage());
        }
        logger.debug("getRefreshTokenFromFile successful {}", RingUtils.sanitizeData(refreshToken));
        return refreshToken;
    }

    public void doLogin(String username, String password, String twofactorCode) {
        logger.debug("doLogin U:{} P:{} 2:{}", RingUtils.sanitizeData(username), RingUtils.sanitizeData(password),
                RingUtils.sanitizeData(twofactorCode));
        String hardwareId = getHardwareId();
        String refreshToken = getRefreshTokenFromFile();
        logger.debug("doLogin H:{} RT:{}", hardwareId, RingUtils.sanitizeData(refreshToken));
        try {
            userProfile = restClient.getAuthenticatedProfile(username, password, refreshToken, twofactorCode,
                    hardwareId);
            saveRefreshTokenToFile(userProfile.getRefreshToken());
        } catch (AuthenticationException ex) {
            logger.debug("AuthenticationException when initializing Ring Account handler{}", ex.getMessage());
            String message = ex.getMessage();
            if ((message != null) && message.startsWith("Two factor")) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
            }
        } catch (JsonParseException e) {
            logger.debug("Invalid response from api.ring.com when initializing Ring Account handler{}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Invalid response from api.ring.com");
        }
        logger.debug("doLogin RT: {}", getRefreshTokenFromFile());
        try {
            refreshRegistry();
            updateStatus(ThingStatus.ONLINE);
        } catch (DuplicateIdException dup) {
            logger.debug("Ring device with duplicate id detected, ignoring device");
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException ae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "AuthenticationException response from ring.com");
            logger.debug("RestClient reported AuthenticationException in finally block: {}", ae.getMessage());
        } catch (JsonParseException pe1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "JsonParseException response from ring.com");
            logger.debug("RestClient reported JsonParseException in finally block: {}", pe1.getMessage());
        }
    }

    public String getHardwareId() {
        AccountConfiguration config = getConfigAs(AccountConfiguration.class);
        String hardwareId = config.hardwareId;
        logger.debug("getHardwareId H:{}", hardwareId);
        Configuration updatedConfiguration = getThing().getConfiguration();
        try {
            if (hardwareId.isEmpty()) {
                hardwareId = getLocalMAC();
                if (("".equals(hardwareId)) || hardwareId.isEmpty()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Hardware ID missing, check thing config");
                    return hardwareId;
                }
                logger.debug("getHardwareId getLocalMac H:{}", hardwareId);
                // write hardwareId to thing config
                config.hardwareId = hardwareId;
                updatedConfiguration.put("hardwareId", config.hardwareId);
                updateConfiguration(updatedConfiguration);
            }
        } catch (IOException e) {
            logger.debug("getHardwareId failed to get local mac address {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Initialization failed: " + e.getMessage());
        }
        return hardwareId;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Ring Account handler");

        AccountConfiguration config = getConfigAs(AccountConfiguration.class);
        int refreshInterval = config.refreshInterval;
        String username = config.username;
        String password = config.password;
        String hardwareId = getHardwareId();
        String refreshToken = getRefreshTokenFromFile();

        String twofactorCode = config.twofactorCode;
        videoRetentionCount = config.videoRetentionCount;
        videoStoragePath = !config.videoStoragePath.isEmpty() ? config.videoStoragePath
                : OpenHAB.getConfigFolder() + "/html/ring/video";

        logger.debug("AccountHandler - initialize - VSP: {} OH: {}", config.videoStoragePath,
                OpenHAB.getConfigFolder());

        restClient = new RestClient();

        if ((!refreshToken.isEmpty()) || !(username.isEmpty() && password.isEmpty())) {
            try {
                Configuration updatedConfiguration = getThing().getConfiguration();

                logger.debug("Logging in with refresh token: {}", RingUtils.sanitizeData(refreshToken));
                userProfile = restClient.getAuthenticatedProfile(username, password, refreshToken, twofactorCode,
                        hardwareId);
                saveRefreshTokenToFile(userProfile.getRefreshToken());
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Retrieving device list");
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
                logger.debug("AuthenticationException when initializing Ring Account handler {}", ex.getMessage());
                String message = ex.getMessage();
                if ((message != null) && message.startsWith("Two factor")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getMessage());
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                }
            } catch (JsonParseException e) {
                logger.debug("Invalid response from api.ring.com when initializing Ring Account handler {}",
                        e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid response from api.ring.com");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "Please login via CLI or by updating the Thing properties");
        }
    }

    private void refreshRegistry() throws JsonParseException, AuthenticationException, DuplicateIdException {
        logger.debug("AccountHandler - refreshRegistry");
        RingDevices ringDevices = restClient.getRingDevices(userProfile, this);
        registry.addRingDevices(ringDevices.getRingDevices());
    }

    protected void minuteTick() {
        try {
            // Init the devices
            refreshRegistry();
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException | JsonParseException e) {
            logger.debug(
                    "AuthenticationException in AccountHandler.minuteTick() when trying refreshRegistry, attempting to reconnect {}",
                    e.getMessage());
            AccountConfiguration config = getConfigAs(AccountConfiguration.class);
            String username = config.username;
            String password = config.password;
            String hardwareId = getHardwareId();
            String refreshToken = getRefreshTokenFromFile();
            if ((!refreshToken.isEmpty()) || !(username.isEmpty() && password.isEmpty())) {
                try {
                    userProfile = restClient.getAuthenticatedProfile(username, password, refreshToken, "", hardwareId);
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, "Retrieving device list");
                } catch (AuthenticationException ex) {
                    logger.debug("RestClient reported AuthenticationException trying getAuthenticatedProfile: {}",
                            ex.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid credentials");
                } catch (JsonParseException e1) {
                    logger.debug("RestClient reported JsonParseException trying getAuthenticatedProfile: {}",
                            e1.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Invalid response from api.ring.com");
                } finally {
                    try {
                        refreshRegistry();
                        updateStatus(ThingStatus.ONLINE);
                    } catch (DuplicateIdException ignored) {
                        updateStatus(ThingStatus.ONLINE);
                    } catch (AuthenticationException ae) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "AuthenticationException response from ring.com");
                        logger.debug("RestClient reported AuthenticationException in finally block: {}",
                                ae.getMessage());
                    } catch (JsonParseException pe1) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "JsonParseException response from ring.com");
                        logger.debug("RestClient reported JsonParseException in finally block: {}", pe1.getMessage());
                    }
                }
            }
        } catch (DuplicateIdException ignored) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    protected void getVideo(RingEventTO event) {
        logger.debug("AccountHandler - getVideo - Event id: {}", event.id);
        logger.debug("AccountHandler - getVideo - VSP: {}", videoStoragePath);
        String videoFile = restClient.downloadEventVideo(event, userProfile, videoStoragePath, videoRetentionCount);
        String localIP = networkAddressService.getPrimaryIpv4HostAddress();

        if (videoFile.endsWith(".mp4")) {
            updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_URL),
                    new StringType("http://" + localIP + ":" + httpPort + "/ring/video/" + videoFile));
        } else {
            updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_URL), new StringType(videoFile));
        }
    }

    protected void eventTick() {
        try {
            long id = lastEvents.isEmpty() ? 0 : lastEvents.get(0).id;
            lastEvents = restClient.getHistory(userProfile, 1);
            if (!lastEvents.isEmpty()) {
                logger.debug("AccountHandler - eventTick - Event id: {} lastEvents: {}", id,
                        lastEvents.get(0).id == id);
                if (lastEvents.get(0).id != id) {
                    logger.debug("AccountHandler - eventTick - New Event {}", lastEvents.get(0).id);
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_CREATED_AT),
                            lastEvents.get(0).getCreatedAt());
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_KIND),
                            new StringType(lastEvents.get(0).kind));
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_DOORBOT_ID),
                            new StringType(lastEvents.get(0).doorbot.id));
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_EVENT_DOORBOT_DESCRIPTION),
                            new StringType(lastEvents.get(0).doorbot.description));
                    runnableVideo = () -> getVideo(lastEvents.get(0));
                    ExecutorService service = videoExecutorService;
                    if (service != null) {
                        service.submit(runnableVideo);
                    }
                }
            } else {
                logger.debug("AccountHandler - eventTick - lastEvents null");
            }
        } catch (AuthenticationException ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "AuthenticationException response from ring.com");
            logger.debug(
                    "RestClient reported AuthenticationExceptionfrom api.ring.com when retrying refreshRegistry for the second time: {}",
                    ex.getMessage());
        } catch (JsonParseException ignored) {
            logger.debug(
                    "RestClient reported JsonParseException api.ring.com when retrying refreshRegistry for the second time: {}",
                    ignored.getMessage());

        }
    }

    private void refreshToken() {
        try {
            refreshRegistry();
            Configuration config = getThing().getConfiguration();
            String hardwareId = (String) config.get("hardwareId");
            userProfile = restClient.getAuthenticatedProfile("", "", userProfile.getRefreshToken(), "", hardwareId);
        } catch (AuthenticationException | DuplicateIdException e) {
            logger.debug(
                    "AccountHandler - startSessionRefresh - Exception occurred during execution of refreshRegistry(): {}",
                    e.getMessage(), e);
        }
    }

    private void refreshEvent() {
        try {
            eventTick();
        } catch (final Exception e) {
            logger.debug(
                    "AccountHandler - startSessionRefresh - Exception occurred during execution of eventTick(): {}",
                    e.getMessage(), e);
        }
    }

    /**
     * Refresh the profile every 20 minutes
     */
    protected void startSessionRefresh(int refreshInterval) {
        logger.debug("startSessionRefresh {}", refreshInterval);
        jobTokenRefresh = scheduler.scheduleWithFixedDelay(this::refreshToken, 90, 600, TimeUnit.SECONDS);
        eventRefresh = scheduler.scheduleWithFixedDelay(this::refreshEvent, refreshInterval, refreshInterval,
                TimeUnit.SECONDS);
    }

    protected void stopSessionRefresh() {
        ScheduledFuture<?> job = jobTokenRefresh;
        if (job != null) {
            job.cancel(true);
        }
        jobTokenRefresh = null;

        job = eventRefresh;
        if (job != null) {
            job.cancel(true);
        }
        eventRefresh = null;
    }

    String getLocalMAC() throws IOException {
        // get local ip from OH system settings
        String localIP = networkAddressService.getPrimaryIpv4HostAddress();
        if ((localIP == null) || (localIP.isBlank())) {
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
    public @Nullable RestClient getRestClient() {
        return restClient;
    }

    @Override
    public @Nullable Profile getProfile() {
        return userProfile;
    }

    @Override
    public String getThingId() {
        return thingId;
    }

    /**
     * Dispose of the refreshJob nicely.
     */
    @Override
    public void dispose() {
        stopSessionRefresh();
        stopAutomaticRefresh();
        ExecutorService service = this.videoExecutorService;
        if (service != null) {
            service.shutdownNow();
        }
        this.videoExecutorService = null;
        super.dispose();
    }
}
