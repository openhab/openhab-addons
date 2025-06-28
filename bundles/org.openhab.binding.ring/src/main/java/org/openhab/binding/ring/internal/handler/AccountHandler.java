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
package org.openhab.binding.ring.internal.handler;

import static org.openhab.binding.ring.RingBindingConstants.*;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.ring.internal.RestClient;
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.binding.ring.internal.RingDeviceRegistry;
import org.openhab.binding.ring.internal.RingVideoServlet;
import org.openhab.binding.ring.internal.api.ProfileTO;
import org.openhab.binding.ring.internal.api.RingDevicesTO;
import org.openhab.binding.ring.internal.api.RingEventTO;
import org.openhab.binding.ring.internal.config.AccountConfiguration;
import org.openhab.binding.ring.internal.data.Tokens;
import org.openhab.binding.ring.internal.device.RingDevice;
import org.openhab.binding.ring.internal.discovery.RingDiscoveryService;
import org.openhab.binding.ring.internal.errors.AuthenticationException;
import org.openhab.binding.ring.internal.utils.RingUtils;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
    private final RingVideoServlet servlet;
    private @Nullable ScheduledFuture<?> jobTokenRefresh = null;
    private @Nullable ScheduledFuture<?> eventRefresh = null;

    // Current status
    protected OnOffType status = OnOffType.OFF;
    protected OnOffType enabled = OnOffType.ON;
    protected final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    // Scheduler
    protected @Nullable ScheduledFuture<?> refreshJob;

    /**
     * The user profile retrieved when authenticating.
     */
    private Tokens tokens = new Tokens("", "");
    /**
     * The registry.
     */
    private final RingDeviceRegistry registry;
    /**
     * The RestClient is used to connect to the Ring Account.
     */
    private final RestClient restClient;
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

    private AccountConfiguration config = new AccountConfiguration();
    private long ownerId = 0;

    public AccountHandler(Bridge bridge, NetworkAddressService networkAddressService, RingVideoServlet ringVideoServlet,
            int httpPort, HttpClient httpClient) {
        super(bridge);
        this.httpPort = httpPort;
        this.networkAddressService = networkAddressService;
        this.registry = new RingDeviceRegistry();
        this.restClient = new RestClient(httpClient);
        this.servlet = ringVideoServlet;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            boolean eventListOk = lastEvents.size() > eventIndex;
            switch (channelUID.getId()) {
                case CHANNEL_EVENT_URL:
                    if (eventListOk) {
                        String videoFile = restClient.downloadEventVideo(lastEvents.get(eventIndex), tokens,
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
                            startAutomaticRefresh(config.refreshInterval);
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
        String thingId = getThing().getUID().getId();
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
        String thingId = getThing().getUID().getId();
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
            tokens = restClient.getTokens(username, password, refreshToken, twofactorCode, hardwareId);
            saveRefreshTokenToFile(tokens.refreshToken());
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
        String hardwareId = config.hardwareId;
        logger.debug("getHardwareId H:{}", hardwareId);
        Configuration updatedConfiguration = getThing().getConfiguration();
        try {
            if (hardwareId.isBlank()) {
                hardwareId = getLocalMAC();
                if (hardwareId.isBlank()) {
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

        config = getConfigAs(AccountConfiguration.class);
        int refreshInterval = config.refreshInterval;
        String hardwareId = getHardwareId();
        String refreshToken = getRefreshTokenFromFile();

        String twofactorCode = config.twofactorCode;
        videoRetentionCount = config.videoRetentionCount;
        videoStoragePath = !config.videoStoragePath.isEmpty() ? config.videoStoragePath
                : OpenHAB.getConfigFolder() + "/html/ring/video";
        servlet.addVideoStoragePath(thing.getUID(), videoStoragePath);

        logger.debug("AccountHandler - initialize - VSP: {} OH: {}", config.videoStoragePath,
                OpenHAB.getConfigFolder());

        if ((!refreshToken.isEmpty()) || !(config.username.isEmpty() && config.password.isEmpty())) {
            try {
                Configuration updatedConfiguration = getThing().getConfiguration();

                logger.debug("Logging in with refresh token: {}", RingUtils.sanitizeData(refreshToken));
                tokens = restClient.getTokens(config.username, config.password, refreshToken, twofactorCode,
                        hardwareId);
                saveRefreshTokenToFile(tokens.refreshToken());
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Retrieving device list");
                config.twofactorCode = "";
                updatedConfiguration.put("twofactorCode", config.twofactorCode);
                updateConfiguration(updatedConfiguration);

                String ownerIdString = thing.getProperties().get(THING_PROPERTY_OWNER_ID);
                if (ownerIdString != null && !ownerIdString.isEmpty()) {
                    ownerId = Long.parseLong(ownerIdString);
                } else {
                    ProfileTO profile = restClient.getProfile(hardwareId, tokens);
                    ownerId = profile.id;
                    Map<String, String> properties = editProperties();
                    properties.put(THING_PROPERTY_OWNER_ID, Long.toString(ownerId));
                    updateProperties(properties);
                }

                refreshRegistry();

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

    private void refreshRegistry() throws JsonParseException, AuthenticationException {
        logger.debug("AccountHandler - refreshRegistry");
        RingDevicesTO ringDevices = restClient.getRingDevices(tokens);
        registry.addOrUpdateRingDevices(ringDevices);
    }

    protected void minuteTick() {
        try {
            refreshRegistry();
            updateStatus(ThingStatus.ONLINE);
        } catch (AuthenticationException | JsonParseException e) {
            String refreshToken = getRefreshTokenFromFile();
            if ((!refreshToken.isEmpty()) || !(config.username.isEmpty() && config.password.isEmpty())) {
                try {
                    tokens = restClient.getTokens(config.username, config.password, refreshToken, "",
                            config.hardwareId);
                    refreshRegistry();
                    updateStatus(ThingStatus.ONLINE);
                } catch (AuthenticationException ex) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Invalid credentials");
                } catch (JsonParseException e1) {
                    logger.debug("RestClient reported JsonParseException trying to get tokens: {}", e1.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Invalid response from api.ring.com");
                }
            }
        }
    }

    protected void getVideo(RingEventTO event) {
        String videoFile = restClient.downloadEventVideo(event, tokens, videoStoragePath, videoRetentionCount);
        String localIP = networkAddressService.getPrimaryIpv4HostAddress();

        if (videoFile.endsWith(".mp4")) {
            updateState(CHANNEL_EVENT_URL,
                    new StringType("http://" + localIP + ":" + httpPort + "/ring/video/" + videoFile));
        } else {
            updateState(CHANNEL_EVENT_URL, new StringType(videoFile));
        }
    }

    protected void eventTick() {
        try {
            long id = lastEvents.isEmpty() ? 0 : lastEvents.getFirst().id;
            lastEvents = restClient.getHistory(tokens, 1);
            if (!lastEvents.isEmpty()) {
                logger.debug("AccountHandler - eventTick - Event id: {} lastEvents: {}", id,
                        lastEvents.getFirst().id == id);
                if (lastEvents.getFirst().id != id) {
                    logger.debug("AccountHandler - eventTick - New Event {}", lastEvents.getFirst().id);
                    updateState(CHANNEL_EVENT_CREATED_AT, lastEvents.getFirst().getCreatedAt());
                    updateState(CHANNEL_EVENT_KIND, new StringType(lastEvents.getFirst().kind));
                    updateState(CHANNEL_EVENT_DOORBOT_ID, new StringType(lastEvents.getFirst().doorbot.id));
                    updateState(CHANNEL_EVENT_DOORBOT_DESCRIPTION,
                            new StringType(lastEvents.getFirst().doorbot.description));
                    ExecutorService service = videoExecutorService;
                    if (service != null) {
                        service.submit(() -> getVideo(lastEvents.getFirst()));
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
            tokens = restClient.getTokens("", "", tokens.refreshToken(), "", config.hardwareId);
        } catch (AuthenticationException e) {
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
     * Refresh the tokens every 20 minutes
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

    private String getLocalMAC() throws IOException {
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

    /**
     * Dispose of the refreshJob nicely.
     */
    @Override
    public void dispose() {
        servlet.removeVideoStoragePath(thing.getUID());

        stopSessionRefresh();
        stopAutomaticRefresh();
        ExecutorService service = this.videoExecutorService;
        if (service != null) {
            service.shutdownNow();
        }
        this.videoExecutorService = null;
        super.dispose();
    }

    @Override
    public Collection<RingDevice> getAllDevices() {
        return registry.getRingDevices().stream()
                .filter(device -> !config.limitToOwner || device.getDeviceStatus().owner.id == ownerId).toList();
    }

    @Override
    public @Nullable RingDevice getDevice(String id) {
        return registry.getRingDevice(id);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(RingDiscoveryService.class);
    }
}
