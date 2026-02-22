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
package org.openhab.binding.linkplay.internal.handler;

import static org.openhab.binding.linkplay.internal.LinkPlayBindingConstants.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.linkplay.internal.LinkPlayBindingConstants;
import org.openhab.binding.linkplay.internal.LinkPlayCommandDescriptionProvider;
import org.openhab.binding.linkplay.internal.LinkPlayConfiguration;
import org.openhab.binding.linkplay.internal.client.http.LinkPlayConnectionUtils;
import org.openhab.binding.linkplay.internal.client.http.LinkPlayHTTPClient;
import org.openhab.binding.linkplay.internal.client.http.dto.AudioOutputHardwareMode;
import org.openhab.binding.linkplay.internal.client.http.dto.DeviceStatus;
import org.openhab.binding.linkplay.internal.client.http.dto.PlayerStatus;
import org.openhab.binding.linkplay.internal.client.http.dto.PlayerStatus.PlaybackStatus;
import org.openhab.binding.linkplay.internal.client.http.dto.PresetList;
import org.openhab.binding.linkplay.internal.client.http.dto.Slave;
import org.openhab.binding.linkplay.internal.client.http.dto.SourceInputMode;
import org.openhab.binding.linkplay.internal.client.http.dto.TrackMetadata;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpClient;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpClientHandler;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpCommands;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpDeviceListener;
import org.openhab.binding.linkplay.internal.client.upnp.LinkPlayUpnpRegistry;
import org.openhab.binding.linkplay.internal.client.upnp.PlayMode;
import org.openhab.binding.linkplay.internal.client.upnp.PlayQueue;
import org.openhab.binding.linkplay.internal.client.upnp.TransportState;
import org.openhab.binding.linkplay.internal.client.upnp.UpnpEntry;
import org.openhab.binding.linkplay.internal.client.upnp.UpnpXMLParser;
import org.openhab.binding.linkplay.internal.group.LinkPlayGroupParticipant;
import org.openhab.binding.linkplay.internal.group.LinkPlayGroupService;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.io.transport.upnp.UpnpIOParticipant;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.CommandDescriptionBuilder;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkPlayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class LinkPlayHandler extends BaseThingHandler
        implements LinkPlayUpnpDeviceListener, UpnpIOParticipant, LinkPlayGroupParticipant, LinkPlayUpnpClientHandler {

    private final Logger logger = LoggerFactory.getLogger(LinkPlayHandler.class);

    private static final int RECONNECT_DELAY = 30;
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> upnpServiceCheck;
    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> positionJob;
    // Are we currently initializing the device?
    private final AtomicBoolean isInitializing = new AtomicBoolean(false);
    // Have we been disposed, prevent further calls to the device when shutting down
    private boolean disposed;
    private LinkPlayHTTPClient apiClient;
    private final LinkPlayUpnpClient upnpClient;
    private final LinkPlayNotificationHandler notificationHandler;
    private final LinkPlayUpnpRegistry linkPlayUpnpRegistry;
    private final UpnpIOService upnpIOService;
    private final LinkPlayGroupService linkPlayGroupService;
    private final LinkPlayCommandDescriptionProvider linkPlayCommandDescriptionProvider;
    private final Map<ChannelUID, State> stateCache = new HashMap<>();
    private Collection<LinkPlayGroupParticipant> allGroupParticipants = new ArrayList<>();
    private String groupName = "";
    private String udn = "";
    private String host = "";
    private boolean inGroup = false;
    private boolean isLeader = false;
    // Current album art URI so we can avoid updating the Image Data if the URI hasn't changed
    private @Nullable String currentAlbumArtUri;
    private int currentPosition = 0;
    private int currentDuration = 0;
    private int currentVolume = 0;
    private PresetList presetInfo = new PresetList();
    private TransportState currentTransportState = TransportState.STOPPED;

    public LinkPlayHandler(Thing thing, HttpClient httpClient, LinkPlayUpnpRegistry linkPlayUpnpRegistry,
            UpnpIOService upnpIOService, UpnpService upnpService, LinkPlayGroupService linkPlayGroupService,
            LinkPlayCommandDescriptionProvider linkPlayCommandDescriptionProvider) {
        super(thing);
        this.linkPlayUpnpRegistry = linkPlayUpnpRegistry;
        this.upnpIOService = upnpIOService;
        this.linkPlayGroupService = linkPlayGroupService;
        this.linkPlayCommandDescriptionProvider = linkPlayCommandDescriptionProvider;
        this.httpClient = httpClient;
        apiClient = new LinkPlayHTTPClient(httpClient);
        this.upnpClient = new LinkPlayUpnpClient(this, upnpIOService, upnpService, scheduler);
        this.notificationHandler = new LinkPlayNotificationHandler(this, upnpClient, scheduler);
    }

    @Override
    public void initialize() {
        isInitializing.set(false);
        disposed = false;
        inGroup = false;
        isLeader = false;
        LinkPlayConfiguration config = getConfigAs(LinkPlayConfiguration.class);
        udn = config.udn;
        logger.trace("initialize: {}", udn);
        updateStatus(ThingStatus.UNKNOWN);
        linkPlayUpnpRegistry.addDeviceListener(udn, this);
        upnpClient.initialize(udn);
        upnpIOService.registerParticipant(upnpClient);

        /**
         * kicks off a service check to discover the device (and continues to run every 15 seconds until the device
         * is discovered) this will be rescheduled once the device is discovered and we get a maxAgeSeconds value from
         * the device, eventually initFromUpnp or initFromGroup will be called and the device will be initialized
         */
        cancelUpnpServiceCheckJob();
        upnpServiceCheck = scheduler.scheduleWithFixedDelay(this::initializationCheck, 0, 15, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("{}: dispose", udn);
        disposed = true;
        linkPlayGroupService.unregisterParticipant(this);
        cancelReconnectJob();
        cancelUpnpServiceCheckJob();
        cancelPositionJob();
        notificationHandler.dispose();
        upnpClient.dispose();
        upnpIOService.removeStatusListener(upnpClient);
        upnpIOService.unregisterParticipant(upnpClient);
        linkPlayUpnpRegistry.removeDeviceListener(udn);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{}: handleCommand: {} {}", udn, channelUID, command);
        if (command instanceof RefreshType) {
            if (stateCache.get(channelUID) instanceof State state) {
                // bypass the state cache and update the state directly
                super.updateState(channelUID, state);
            } else {
                super.updateState(channelUID, UnDefType.UNDEF);
            }
            return;
        }
        try {
            switch (channelUID.getIdWithoutGroup()) {
                case LinkPlayBindingConstants.CHANNEL_PLAYER_CONTROL:
                    if (command instanceof PlayPauseType playPauseType) {
                        if (playPauseType == PlayPauseType.PLAY) {
                            apiClient.setPlayerCmdResume().get();
                        } else {
                            apiClient.setPlayerCmdPause().get();
                        }
                    }
                    if (command instanceof RewindFastforwardType rewindFastforwardType) {
                        switch (rewindFastforwardType) {
                            case REWIND:
                                apiClient.setPlayerCmdSeekPosition(Math.max(0, currentPosition - 10)).get();
                                break;
                            case FASTFORWARD:
                                apiClient.setPlayerCmdSeekPosition(Math.min(currentDuration, currentPosition + 10))
                                        .get();
                        }
                    }
                    if (command instanceof NextPreviousType nextPreviousType) {
                        switch (nextPreviousType) {
                            case NEXT:
                                apiClient.setPlayerCmdNext().get();
                                break;
                            case PREVIOUS:
                                apiClient.setPlayerCmdPrev().get();
                                break;
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_VOLUME:
                    if (command instanceof PercentType percentType) {
                        if (channelUID.getGroupId() instanceof String group) {
                            if (group.equals(LinkPlayBindingConstants.GROUP_MULTIROOM)) {
                                apiClient.setPlayerCmdSlaveVol(percentType.intValue()).get();
                            } else {
                                apiClient.setPlayerCmdVol(percentType.intValue()).get();
                            }
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_MUTE:
                    if (command instanceof OnOffType onOffType) {
                        if (channelUID.getGroupId() instanceof String group) {
                            if (group.equals(LinkPlayBindingConstants.GROUP_MULTIROOM)) {
                                if (onOffType == OnOffType.ON) {
                                    apiClient.setPlayerCmdSlaveMute().get();
                                } else {
                                    apiClient.setPlayerCmdSlaveUnmute().get();
                                }
                            } else {
                                apiClient.setPlayerCmdMute(onOffType == OnOffType.ON ? 1 : 0).get();
                            }
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_PRESET_PLAY:
                    if (channelUID.getGroupId() instanceof String group) {
                        if (group.equals(LinkPlayBindingConstants.GROUP_PRESETS)) {
                            if (command instanceof DecimalType decimalType) {
                                if (decimalType.intValue() > 0) {
                                    apiClient.mcuKeyShortClick(decimalType.intValue()).get();
                                }
                            }
                        } else {
                            if (command instanceof OnOffType onOffType) {
                                if (onOffType == OnOffType.ON) {
                                    try {
                                        int presetNum = Integer.parseInt(
                                                group.substring(LinkPlayBindingConstants.GROUP_PRESET.length()));
                                        apiClient.mcuKeyShortClick(presetNum).get();
                                    } catch (NumberFormatException e) {
                                        logger.debug("Invalid preset number: {}", group);
                                    }
                                }
                                updateState(channelUID, OnOffType.OFF);
                            }
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_MULTIROOM_JOIN:
                    if (command instanceof StringType stringType) {
                        if ("LEAVE".equals(stringType.toString())) {
                            linkPlayGroupService.unGroup(this);
                        } else {
                            ThingUID leaderUID = new ThingUID(stringType.toString());
                            linkPlayGroupService.joinGroup(this, leaderUID);
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_MULTIROOM_LEAVE:
                    linkPlayGroupService.unGroup(this);
                    break;
                case LinkPlayBindingConstants.CHANNEL_MULTIROOM_MANAGE:
                    if (command instanceof StringType stringType) {
                        switch (stringType.toString()) {
                            case "LEAVE":
                                linkPlayGroupService.unGroup(this);
                                break;
                            case "ADD_ALL":
                                linkPlayGroupService.addAllMembers(this);
                                break;
                            default:
                                ThingUID memberUID = new ThingUID(stringType.toString());
                                linkPlayGroupService.addOrMoveMember(this, memberUID);
                                break;
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_MULTIROOM_UNGROUP:
                    linkPlayGroupService.unGroup(this);
                    break;

                case LinkPlayBindingConstants.CHANNEL_REPEAT_SHUFFLE_MODE:
                    if (command instanceof DecimalType decimalType) {
                        apiClient.setPlayerCmdLoopmode(decimalType.intValue()).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_EQ_PRESET:
                    if (command instanceof StringType stringType) {
                        apiClient.loadEQByName(stringType.toString()).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_EQ_ENABLED:
                    if (command instanceof OnOffType onOffType) {
                        if (onOffType == OnOffType.ON) {
                            apiClient.setEQOn().get();
                        } else {
                            apiClient.setEQOff().get();
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_SOURCE_INPUT:
                    if (command instanceof StringType stringType) {
                        Optional<SourceInputMode> modeOpt = java.util.Arrays.stream(SourceInputMode.values())
                                .filter(m -> m.toString().equalsIgnoreCase(stringType.toString())).findFirst();
                        if (modeOpt.isPresent()) {
                            apiClient.setPlayerCmdSwitchMode(modeOpt.get()).get();
                        } else {
                            logger.debug("Unsupported source input mode: {}", stringType);
                        }
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_OUTPUT_HW_MODE:
                    if (command instanceof DecimalType decimalType) {
                        apiClient.setAudioOutputHardwareMode(decimalType.intValue()).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_CHANNEL_BALANCE:
                    if (command instanceof DecimalType decimalType) {
                        apiClient.setChannelBalance(decimalType.doubleValue()).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_SPDIF_DELAY:
                    if (command instanceof DecimalType decimalType) {
                        apiClient.setSpdifOutSwitchDelayMs(decimalType.intValue()).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_LED_ENABLED:
                    if (command instanceof OnOffType onOffType) {
                        apiClient.setLedSwitch(onOffType == OnOffType.ON ? 1 : 0).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_TOUCH_KEYS_ENABLED:
                    if (command instanceof OnOffType onOffType) {
                        apiClient.setTouchControls(onOffType == OnOffType.ON ? 1 : 0).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_SHUTDOWN_TIMER:
                    if (command instanceof DecimalType decimalType) {
                        apiClient.setShutdownTimer(decimalType.intValue()).get();
                    }
                    break;
                case LinkPlayBindingConstants.CHANNEL_REBOOT:
                    if (command instanceof OnOffType onOffType && onOffType == OnOffType.ON) {
                        apiClient.rebootDevice().get();
                        updateState(channelUID, OnOffType.OFF); // reset switch
                    }
                    break;
                default:
                    logger.debug("{}: No handler implemented for channel {}", udn, channelUID);
                    break;
            }
        } catch (ExecutionException | InterruptedException e) {
            logger.debug("Error while handling command: {}", e.getMessage(), e);
        }
    }

    @Override
    protected void updateState(String channelID, State state) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        updateState(channelUID, state);
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        State oldState = stateCache.put(channelUID, state);
        if (oldState != null && oldState.equals(state)) {
            return;
        }
        super.updateState(channelUID, state);
    }

    // UPnP IO Participant delegation

    @Override
    public synchronized void updateDeviceConfig(RemoteDevice device) {
        if (disposed) {
            return;
        }
        upnpClient.setRemoteDevice(device);
        if (ThingStatus.ONLINE != getThing().getStatus() || upnpClient.needsUpnpInitialization()) {
            scheduler.schedule(() -> initFromUpnp(device), 0, TimeUnit.MILLISECONDS);
        }
        int maxAgeSeconds = device.getIdentity().getMaxAgeSeconds();
        // if the ip has changed, which it seems to do more periodically then other devices for some reason, update the
        // API client host
        String host = device.getIdentity().getDescriptorURL().getHost();
        if (host != null && !host.equals(this.host)) {
            this.host = host;
            apiClient.setHost(host);
        }
        cancelUpnpServiceCheckJob();
        logger.debug("{}: updateDeviceConfig: maxAgeSeconds: {}", udn, maxAgeSeconds);
        if (maxAgeSeconds > 0) {
            upnpServiceCheck = scheduler.scheduleWithFixedDelay(this::upnpServiceCheck, maxAgeSeconds, maxAgeSeconds,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        upnpClient.onValueReceived(variable, value, service);
    }

    @Override
    public void onServiceSubscribed(@Nullable String service, boolean succeeded) {
        upnpClient.onServiceSubscribed(service, succeeded);
    }

    @Override
    public void onStatusChanged(boolean status) {
        upnpClient.onStatusChanged(status);
    }

    @Override
    public String getUDN() {
        return udn;
    }

    // LinkPlayGroupService methods

    @Override
    public ThingUID getThingUID() {
        return getThing().getUID();
    }

    @Override
    public String getIpAddress() {
        return host;
    }

    @Override
    public void addedToOrUpdatedGroup(LinkPlayGroupParticipant leader, List<Slave> slaves) {
        logger.debug("{}: multiroomAddedToGroup: {}", udn, leader.getIpAddress());
        inGroup = true;
        boolean oldLeader = isLeader;
        isLeader = leader.equals(this);
        if (!oldLeader && isLeader) {
            // we are now the leader
            stateCache.entrySet().forEach(entry -> {
                linkPlayGroupService.updateGroupState(this, entry.getKey().getId(), entry.getValue());
            });
            // Set the group volume to the current volume of the leader
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_VOLUME,
                    new PercentType(currentVolume));
        }
        updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_LEADER,
                isLeader ? OnOffType.ON : OnOffType.OFF);
        updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_ACTIVE,
                OnOffType.ON);
        updateJoinGroupCommandDescription();
        updateAddRemoveMemberCommandDescription(slaves);
    }

    @Override
    public void removedFromGroup(LinkPlayGroupParticipant leader) {
        logger.debug("{}: multiroomRemovedFromGroup: {}", udn, leader.getIpAddress());
        inGroup = false;
        isLeader = false;
        updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_LEADER,
                OnOffType.OFF);
        updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_ACTIVE,
                OnOffType.OFF);
        refreshPlayer();
        updateJoinGroupCommandDescription();
        updateAddRemoveMemberCommandDescription();
    }

    @Override
    public void groupParticipantsUpdated(Collection<LinkPlayGroupParticipant> participants) {
        allGroupParticipants = participants;
        updateJoinGroupCommandDescription();
        updateAddRemoveMemberCommandDescription();
    }

    @Override
    public void groupProxyUpdateState(String channelId, State state) {
        logger.debug("{}: groupProxyUpdateState: {} {} {}", udn, channelId, state, getGroupParticipantLabel());
        super.updateState(channelId, state);
    }

    @Override
    public LinkPlayHTTPClient getApiClient() {
        return apiClient;
    }

    @Override
    public String getGroupParticipantLabel() {
        return groupName;
    }

    // AudioSink methods

    public State getState(String groupId, String channelId) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), groupId, channelId);
        return stateCache.getOrDefault(channelUID, UnDefType.NULL);
    }

    public PercentType getVolume() throws InterruptedException, ExecutionException, TimeoutException {
        PlayerStatus playerStatus = apiClient.getPlayerStatus().get();
        String volume = playerStatus.volume != null ? playerStatus.volume : "0";
        return new PercentType(volume);
    }

    public void setVolume(PercentType volume) throws InterruptedException, ExecutionException, TimeoutException {
        apiClient.setPlayerCmdVol(volume.intValue()).get();
    }

    public void stopPlaying() throws InterruptedException, ExecutionException, TimeoutException {
        apiClient.setPlayerCmdStop().get();
    }

    /**
     * Play a notification on the device.
     * If the device is in a group and not the leader, the command will be forwarded to the leader.
     * If the device is currently in a playlist, the playlist will be paused while the notification is playing, and then
     * resumed after the notification playback is complete.
     * If the device is not currently in a playlist, the notification will be played as a single track.
     *
     * @param url The URL of the notification to play
     * @return A future that completes when the notification playback is complete
     */
    public CompletableFuture<@Nullable Void> playNotification(String url) {
        // if we are in a group and not the leader, forward the command to the leader
        if (inGroup && !isLeader) {
            return linkPlayGroupService.playNotification(this, url);
        }
        return notificationHandler.playNotification(url);
    }

    // Public methods for helper classes to access

    public String getUdn() {
        return udn;
    }

    // LinkPlayUpnpClientHandler interface implementation

    @Override
    public boolean shouldProcessUpnpEvents() {
        return getThing().getStatus() == ThingStatus.ONLINE;
    }

    @Override
    public void onUpnpSubscriptionStateChanged(boolean allSubscriptionsSuccessful) {
        // group members turn UPnP off when they are not the leader, so ignore UPnP checks
        if (inGroup && !isLeader) {
            return;
        }
        if (!allSubscriptionsSuccessful) {
            updateStatus(ThingStatus.INITIALIZING, ThingStatusDetail.NOT_YET_READY, "Waiting for UPnP subscriptions");
            return;
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void onUpnpServiceAvailable() {
        upnpServiceCheck();
    }

    @Override
    public void onUpnpServiceStatusChanged(boolean available) {
        if (available) {
            upnpServiceCheck();
        } else {
            // When a device is a member of a group and not the leader it shuts off UPnP communication.
            if (!inGroup || isLeader) {
                setOffline("UPnP connection lost");
            }
        }
    }

    @Override
    public void onAvTransportEvent(Map<String, String> avt) {
        TransportState transportState = TransportState.fromString(avt.get("TransportState"));
        if (transportState != null) {
            // ignore TRANSITIONING state for PlayerStatus update
            if (transportState != currentTransportState && transportState != TransportState.TRANSITIONING) {
                PlayPauseType playPauseType = transportState == TransportState.PLAYING ? PlayPauseType.PLAY
                        : PlayPauseType.PAUSE;

                currentTransportState = transportState;
                PlaybackStatus playbackStatus = switch (transportState) {
                    case PLAYING -> PlaybackStatus.PLAYING;
                    case PAUSED_PLAYBACK -> PlaybackStatus.PAUSED;
                    case STOPPED -> PlaybackStatus.STOPPED;
                    case TRANSITIONING -> PlaybackStatus.PLAYING;
                    default -> PlaybackStatus.STOPPED;
                };
                updateState(GROUP_PLAYBACK, CHANNEL_PLAYBACK_STATE, new StringType(playbackStatus.name()));
                updateState(GROUP_PLAYBACK, CHANNEL_PLAYER_CONTROL, playPauseType);
            }
            if (transportState == TransportState.PLAYING) {
                schedulePositionJob();
                refreshPlayListQueue();
            } else {
                cancelPositionJob();
            }
        }

        // if we're in a notification, we don't want to update any other info
        if (notificationHandler.isInNotification()) {
            return;
        }

        PlayMode playMode = PlayMode.fromString(avt.get("CurrentPlayMode"));
        if (playMode != null) {
            Integer loopCode = switch (playMode) {
                case NORMAL -> 4; // Off
                case REPEAT_ONE, REPEATONE -> 1; // Repeat One
                case REPEAT_ALL, REPEATALL, REPEAT -> 0; // Repeat All
                case SHUFFLE, SHUFFLE_NOREPEAT, RANDOM, SHUFFLE_ALL -> 3; // Shuffle only
            };
            updateState(GROUP_PLAYBACK, CHANNEL_REPEAT_SHUFFLE_MODE, new DecimalType(loopCode));
        }

        String relPos = avt.get("RelativeTimePosition");
        if (isValidUpnpResponse(relPos)) {
            int seconds = LinkPlayUpnpCommands.hhMmSsToSeconds(Objects.requireNonNull(relPos));
            if (seconds >= 0) {
                updateState(GROUP_PLAYBACK, CHANNEL_TRACK_POSITION, new DecimalType(seconds));
                currentPosition = seconds;
            }
        }

        String duration = avt.get("CurrentTrackDuration");
        if (isValidUpnpResponse(duration)) {
            int seconds = LinkPlayUpnpCommands.hhMmSsToSeconds(Objects.requireNonNull(duration));
            if (seconds >= 0) {
                updateState(GROUP_PLAYBACK, CHANNEL_TRACK_DURATION, new DecimalType(seconds));
                currentDuration = seconds;
            }
        }

        String mdXmlString = avt.get("CurrentTrackMetaData");
        if (isValidUpnpResponse(mdXmlString)) {
            String mdXml = Objects.requireNonNull(mdXmlString);
            List<UpnpEntry> entries = UpnpXMLParser.getEntriesFromXML(mdXml);
            if (!entries.isEmpty()) {
                UpnpEntry entry = entries.get(0);
                if (!entry.getTitle().isEmpty()) {
                    updateState(GROUP_METADATA, CHANNEL_TRACK_TITLE, new StringType(entry.getTitle()));
                }
                String artist = !entry.getArtist().isEmpty() ? entry.getArtist() : entry.getCreator();
                if (!artist.isEmpty()) {
                    updateState(GROUP_METADATA, CHANNEL_TRACK_ARTIST, new StringType(artist));
                }
                if (!entry.getAlbum().isEmpty()) {
                    updateState(GROUP_METADATA, CHANNEL_TRACK_ALBUM, new StringType(entry.getAlbum()));
                }
                if (!entry.getAlbumArtUri().isEmpty()) {
                    updateAlbumArtChannels(entry.getAlbumArtUri());
                }
            }
        }
        @Nullable
        String currentTrackUri = avt.get("AVTransportURI");
        if (currentTrackUri == null) {
            currentTrackUri = avt.get("CurrentTrackURI");
        }
        if (currentTrackUri != null) {
            updateState(GROUP_METADATA, CHANNEL_TRACK_URI, new StringType(currentTrackUri));
            refreshPlayListQueue();
        }

        if (avt.get("TrackSource") instanceof String trackSource) {
            updateState(GROUP_METADATA, CHANNEL_TRACK_SOURCE, new StringType(trackSource));
        }
    }

    @Override
    public void onRenderingControlEvent(Map<String, @Nullable String> rc) {
        for (Map.Entry<String, @Nullable String> e : rc.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            logger.debug("{}: handleRenderingControlEvent: {} {}", udn, key, value);
            if (value == null) {
                continue;
            }
            if (key.endsWith("Volume")) {
                try {
                    int volume = Integer.parseInt(value);
                    currentVolume = volume;
                    updateState(GROUP_PLAYBACK, CHANNEL_VOLUME, new PercentType(volume));
                } catch (NumberFormatException ignored) {
                    logger.debug("{}: Error parsing volume: {}", udn, value, ignored);
                }
            } else if (key.endsWith("Mute")) {
                OnOffType muteState = "1".equals(value) ? OnOffType.ON : OnOffType.OFF;
                updateState(GROUP_PLAYBACK, CHANNEL_MUTE, muteState);
            } else if ("Slave".equals(key)) {
                updateMultiroom();
            }
        }
    }

    // Helper methods for UPnP event processing

    private boolean isValidUpnpResponse(@Nullable String value) {
        return value != null && !value.isBlank() && !"NOT_IMPLEMENTED".equals(value);
    }

    // Public methods (not from interface)

    public boolean isInGroup() {
        return inGroup;
    }

    public boolean isGroupLeader() {
        return isLeader;
    }

    public TransportState getCurrentTransportState() {
        return currentTransportState;
    }

    public void setCurrentTransportState(TransportState state) {
        this.currentTransportState = state;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public void setCurrentDuration(int duration) {
        this.currentDuration = duration;
    }

    public void setCurrentVolume(int volume) {
        this.currentVolume = volume;
    }

    public PresetList getPresetInfo() {
        return presetInfo;
    }

    public void updateAlbumArtChannels(@Nullable String albumArtUri) {
        if (albumArtUri == null || !albumArtUri.trim().startsWith("http")) {
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_ALBUM_ART_URL,
                    UnDefType.NULL);
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_ALBUM_ART,
                    UnDefType.NULL);
            currentAlbumArtUri = null;
            return;
        }
        if (albumArtUri.equals(currentAlbumArtUri)) {
            return;
        }
        currentAlbumArtUri = albumArtUri;
        updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_ALBUM_ART_URL,
                new StringType(albumArtUri));
        try {
            State albumArt = HttpUtil.downloadImage(albumArtUri.trim());
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_ALBUM_ART,
                    albumArt != null ? albumArt : UnDefType.NULL);
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid album art URI: {}", albumArtUri, e);
        }
    }

    public void updateMultiroom() {
        linkPlayGroupService.refreshMemberSlaveList(this);
    }

    public void schedulePositionJob() {
        cancelPositionJob();
        if (!disposed) {
            positionJob = scheduler.scheduleWithFixedDelay(this::updatePosition, 0, 1, TimeUnit.SECONDS);
        } else {
            logger.warn("{}: Not scheduling position job, device is not online!", udn);
        }
    }

    public void cancelPositionJob() {
        cancelJob(positionJob);
        positionJob = null;
    }

    public void setOffline(String reason) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        RemoteDevice device = upnpClient.getRemoteDevice();
        upnpClient.clearSubscriptionState();
        if (device != null) {
            cancelReconnectJob();
            if (!disposed) {
                reconnectJob = scheduler.schedule(() -> initFromUpnp(device), RECONNECT_DELAY, TimeUnit.SECONDS);
            }
        }
        upnpClient.sendDeviceSearchRequest();
    }

    // Derives the host and port from the Upnp device and attempts to connect to the api
    private void initFromUpnp(RemoteDevice device) {
        if (ThingStatus.ONLINE == getThing().getStatus() && !upnpClient.needsUpnpInitialization()) {
            return;
        }
        logger.debug("{} Device Namespace: {}", udn, device.getType().getNamespace());
        // block multiple callers from initializing the device if already in progress
        if (!isInitializing.compareAndSet(false, true)) {
            return;
        }
        try {
            String host = device.getIdentity().getDescriptorURL().getHost();
            if (host == null || host.isEmpty()) {
                logger.debug("no host for device {}", device.getDetails().getFriendlyName());
                setOffline("Could not discover host for device " + device.getDetails().getFriendlyName());
                return;
            }
            Integer port = LinkPlayConnectionUtils.testConnection(httpClient, host);
            if (port == null) {
                logger.debug("{}: Could not validate device at {}", udn, host);
                // since we may be called from the reconnect job, set offline on a new cycle
                setOffline("Could not connect to device at " + host);
                return;
            }
            this.host = host;
            apiClient.setHost(host);
            apiClient.setPort(port);

            String friendlyName = device.getDetails().getFriendlyName();
            String manufacturer = device.getDetails().getManufacturerDetails().getManufacturer();
            String modelName = device.getDetails().getModelDetails().getModelName();

            Map<String, String> properties = new HashMap<>();
            properties.put(LinkPlayBindingConstants.PROPERTY_PORT, port.toString());
            properties.put(LinkPlayBindingConstants.PROPERTY_IP_ADDRESS, host);
            properties.put(LinkPlayBindingConstants.PROPERTY_DEVICE_NAME, friendlyName);
            properties.put(LinkPlayBindingConstants.PROPERTY_MODEL, modelName);
            properties.put(LinkPlayBindingConstants.PROPERTY_MANUFACTURER, manufacturer);
            getThing().setProperties(properties);
            groupName = friendlyName;
            linkPlayGroupService.registerParticipant(this);
            // set the group channels to off until we are added to a group
            updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_ACTIVE,
                    OnOffType.OFF);
            updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_LEADER,
                    OnOffType.OFF);
            refreshPlayer();
            upnpServiceCheck();
            onUpnpSubscriptionStateChanged(upnpClient.isFullySubscribed());
        } finally {
            isInitializing.set(false);
            upnpClient.setNeedsUpnpInitialization(false);
        }
    }

    // If the device is in a group when initialized, UPnP will not be available, so we need to initialize from the
    // group. Once ungrouped, it will call initFromUpnp when a UPnP device is discovered.
    private void initFromGroup(Slave slave) {
        if (ThingStatus.ONLINE == getThing().getStatus()) {
            return;
        }
        if (!isInitializing.compareAndSet(false, true)) {
            return;
        }
        try {
            logger.debug("{}: Initializing from group", udn);
            this.host = slave.ip;
            Integer port = LinkPlayConnectionUtils.testConnection(httpClient, host);
            if (port == null) {
                logger.debug("{}: Could not validate device at {}", udn, host);
                // since we may be called from the reconnect job, set offline on a new cycle
                setOffline("Could not connect to device at " + host);
                return;
            }
            inGroup = true;
            isLeader = false;
            apiClient.setHost(slave.ip);
            apiClient.setPort(port);
            linkPlayGroupService.registerParticipant(this);
            updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_ACTIVE,
                    OnOffType.ON);
            updateState(LinkPlayBindingConstants.GROUP_MULTIROOM, LinkPlayBindingConstants.CHANNEL_MULTIROOM_LEADER,
                    OnOffType.OFF);
            refreshPlayer();
            upnpServiceCheck();
            updateStatus(ThingStatus.ONLINE);
        } finally {
            isInitializing.set(false);
            upnpClient.setNeedsUpnpInitialization(true);
        }
    }

    /**
     * Checks if the device is online and initializes from UPnP or group if needed
     */
    private void initializationCheck() {
        if (isInitializing.get()) {
            return;
        }
        if (ThingStatus.ONLINE != getThing().getStatus()) {
            Slave slave = linkPlayGroupService.findSlaveByUDN(udn);
            if (slave != null) {
                initFromGroup(slave);
            } else {
                upnpClient.sendDeviceSearchRequest();
            }
        }
    }

    /**
     * Updates the position of the current track by sending a UPnP request to the device
     */
    private void updatePosition() {
        try {
            if (currentTransportState != TransportState.PLAYING) {
                return;
            }
            LinkPlayUpnpCommands commands = upnpClient.getCommands();
            Map<String, String> result = commands.getPositionInfo().get();
            if (result.get("RelTime") instanceof String track) {
                currentPosition = LinkPlayUpnpCommands.hhMmSsToSeconds(track);
                updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_TRACK_POSITION,
                        new DecimalType(currentPosition));
            }
            if (result.get("TrackDuration") instanceof String duration) {
                currentDuration = LinkPlayUpnpCommands.hhMmSsToSeconds(duration);
                updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_TRACK_DURATION,
                        new DecimalType(currentDuration));
            }
        } catch (InterruptedException | ExecutionException | IllegalArgumentException e) {
            logger.debug("{}: Error while retrieving position info: {}", udn, e.getMessage(), e);
        }
    }

    /**
     * Refreshes the player status from the device using the HTTP API
     */
    private void refreshPlayer() {
        try {
            updatePlayerStatus();
        } catch (InterruptedException | ExecutionException | TimeoutException | RejectedExecutionException e) {
            logger.debug("{}: Error while parsing player status: {}", udn, e.getMessage(), e);
        }
        if (isLeader || !inGroup) {
            try {
                updateTrackMetadata();
            } catch (InterruptedException | ExecutionException | TimeoutException | RejectedExecutionException e) {
                // if no track metadata, we're not playing anything
            }
        }
        updateEqAndDeviceSettings();
        updateMultiroom();
        refreshPlayListQueue();
    }

    /**
     * Updates the player status channels from the device using the HTTP API
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws RejectedExecutionException
     */
    private void updatePlayerStatus()
            throws InterruptedException, ExecutionException, TimeoutException, RejectedExecutionException {
        PlayerStatus playerStatus = apiClient.getPlayerStatus().get();
        logger.debug("{}: Player status: {}", udn, playerStatus);
        updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_PLAYBACK_STATE,
                playerStatus.status != null ? new StringType(playerStatus.status.name()) : UnDefType.NULL);
        updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_PLAYER_CONTROL,
                playerStatus.status != null
                        ? (playerStatus.status == PlaybackStatus.PLAYING
                                || playerStatus.status == PlaybackStatus.BUFFERING ? PlayPauseType.PLAY
                                        : PlayPauseType.PAUSE)
                        : UnDefType.NULL);
        currentVolume = playerStatus.volume != null ? Integer.parseInt(playerStatus.volume) : 0;
        updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_VOLUME,
                new PercentType(currentVolume));

        if (playerStatus.mute != null) {
            OnOffType muteState = "1".equals(playerStatus.mute) ? OnOffType.ON : OnOffType.OFF;
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_MUTE, muteState);
        }

        if (playerStatus.currentPosition != null) {
            int seconds = playerStatus.currentPosition / 1000;
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_TRACK_POSITION,
                    new DecimalType(seconds));
            currentPosition = seconds;
        } else {
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_TRACK_POSITION,
                    UnDefType.UNDEF);
        }

        if (playerStatus.totalLength != null) {
            int seconds = playerStatus.totalLength / 1000;
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_TRACK_DURATION,
                    new DecimalType(seconds));
            currentDuration = seconds;
        } else {
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_TRACK_DURATION,
                    UnDefType.UNDEF);
        }

        if (playerStatus.loop != null) {
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_REPEAT_SHUFFLE_MODE,
                    new DecimalType(playerStatus.loop.getCode()));
        } else {
            updateState(LinkPlayBindingConstants.GROUP_PLAYBACK, LinkPlayBindingConstants.CHANNEL_REPEAT_SHUFFLE_MODE,
                    UnDefType.NULL);
        }

        updateState(LinkPlayBindingConstants.GROUP_EQUALISER, LinkPlayBindingConstants.CHANNEL_EQ_PRESET,
                stateOrNull(playerStatus.eq, StringType.class));

        // Map PlayerMode to the source input value string when possible
        if (playerStatus.mode != null) {
            String mappedSource = playerStatus.mode.toSourceInputValue();
            if (mappedSource != null) {
                updateState(LinkPlayBindingConstants.GROUP_INPUT, LinkPlayBindingConstants.CHANNEL_SOURCE_INPUT,
                        new StringType(mappedSource));
            } else {
                updateState(LinkPlayBindingConstants.GROUP_INPUT, LinkPlayBindingConstants.CHANNEL_SOURCE_INPUT,
                        UnDefType.NULL);
            }
        } else {
            updateState(LinkPlayBindingConstants.GROUP_INPUT, LinkPlayBindingConstants.CHANNEL_SOURCE_INPUT,
                    UnDefType.NULL);
        }
    }

    /**
     * Updates the track metadata channels from the device using the HTTP API
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws RejectedExecutionException
     */
    private void updateTrackMetadata()
            throws InterruptedException, ExecutionException, TimeoutException, RejectedExecutionException {
        TrackMetadata trackMetadata = apiClient.getMetaInfo().get();
        logger.debug("{}: Track metadata: {}", udn, trackMetadata);
        if (trackMetadata.metaData != null) {
            var md = trackMetadata.metaData;
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_TRACK_TITLE,
                    stateOrNull(md.title, StringType.class));
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_TRACK_ARTIST,
                    stateOrNull(md.artist, StringType.class));
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_TRACK_ALBUM,
                    stateOrNull(md.album, StringType.class));
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_SAMPLE_RATE,
                    stateOrNull(md.sampleRate, DecimalType.class));
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_BIT_DEPTH,
                    stateOrNull(md.bitDepth, DecimalType.class));
            updateState(LinkPlayBindingConstants.GROUP_METADATA, LinkPlayBindingConstants.CHANNEL_BIT_RATE,
                    stateOrNull(md.bitRate, DecimalType.class));
            updateAlbumArtChannels(md.albumArtUri);
        }
    }

    /**
     * Updates equaliser on/off, output hardware mode, channel balance, SPDIF delay and shutdown timer
     * from the device using the HTTP API
     */
    private void updateEqAndDeviceSettings() {
        // EQ enabled state
        try {
            var eqStat = apiClient.getEQStat().get();
            updateState(LinkPlayBindingConstants.GROUP_EQUALISER, LinkPlayBindingConstants.CHANNEL_EQ_ENABLED,
                    eqStat.isOn() ? OnOffType.ON : OnOffType.OFF);
        } catch (InterruptedException | ExecutionException e) {
            logger.trace("{}: Unable to fetch EQ status: {}", udn, e.getMessage());
        }

        try {
            AudioOutputHardwareMode mode = apiClient.getNewAudioOutputHardwareMode().get();
            if (mode != null) {
                updateState(LinkPlayBindingConstants.GROUP_EQUALISER, LinkPlayBindingConstants.CHANNEL_OUTPUT_HW_MODE,
                        new DecimalType(mode.hardware));
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.trace("{}: Unable to fetch output hardware mode: {}", udn, e.getMessage());
        }

        try {
            Double balance = apiClient.getChannelBalance().get();
            if (balance != null) {
                updateState(LinkPlayBindingConstants.GROUP_EQUALISER, LinkPlayBindingConstants.CHANNEL_CHANNEL_BALANCE,
                        new DecimalType(balance));
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.trace("{}: Unable to fetch channel balance: {}", udn, e.getMessage());
        }

        try {
            Integer delay = apiClient.getSpdifOutSwitchDelayMs().get();
            if (delay != null) {
                updateState(LinkPlayBindingConstants.GROUP_EQUALISER, LinkPlayBindingConstants.CHANNEL_SPDIF_DELAY,
                        new DecimalType(delay));
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.trace("{}: Unable to fetch SPDIF delay: {}", udn, e.getMessage());
        }

        // Shutdown timer (seconds; -1 for disabled)
        try {
            Integer secs = apiClient.getShutdownTimer().get();
            if (secs != null && secs >= 0) {
                updateState(LinkPlayBindingConstants.GROUP_DEVICE, LinkPlayBindingConstants.CHANNEL_SHUTDOWN_TIMER,
                        new DecimalType(secs));
            } else {
                updateState(LinkPlayBindingConstants.GROUP_DEVICE, LinkPlayBindingConstants.CHANNEL_SHUTDOWN_TIMER,
                        UnDefType.UNDEF);
            }
        } catch (InterruptedException | ExecutionException e) {
            logger.trace("{}: Unable to fetch shutdown timer: {}", udn, e.getMessage());
        }
    }

    /**
     * Updates the preset info channels from the device using the HTTP API
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws RejectedExecutionException
     */
    private void updatePresetInfo()
            throws InterruptedException, ExecutionException, TimeoutException, RejectedExecutionException {
        PresetList presetInfo = apiClient.getPresetInfo().get();
        if (presetInfo.presetList != null) {
            this.presetInfo = presetInfo;
            updateState(LinkPlayBindingConstants.GROUP_PRESETS, LinkPlayBindingConstants.CHANNEL_PRESET_COUNT,
                    stateOrNull(presetInfo.presetNum, DecimalType.class));
            List<CommandOption> commandOptions = new ArrayList<>();
            for (PresetList.Preset p : presetInfo.presetList) {
                String groupId = "preset" + p.number; // preset1..preset12
                updateState(groupId, LinkPlayBindingConstants.CHANNEL_PRESET_NAME,
                        stateOrNull(p.name, StringType.class));
                updateState(groupId, LinkPlayBindingConstants.CHANNEL_PRESET_URL, stateOrNull(p.url, StringType.class));
                updateState(groupId, LinkPlayBindingConstants.CHANNEL_PRESET_SOURCE,
                        stateOrNull(p.source, StringType.class));
                updatePresetPicChannels(groupId, p.picUrl);
                commandOptions.add(new CommandOption(String.valueOf(p.number), p.name));
            }
            updateCommandDescription(new ChannelUID(getThing().getUID(), LinkPlayBindingConstants.GROUP_PRESETS,
                    LinkPlayBindingConstants.CHANNEL_PLAY_PRESET), commandOptions);
        }
    }

    /**
     * Updates the device status channels from the device using the HTTP API
     *
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws RejectedExecutionException
     */
    private void updateDeviceStatus()
            throws InterruptedException, ExecutionException, TimeoutException, RejectedExecutionException {
        DeviceStatus deviceStatus = apiClient.getStatusEx().get();
        if (deviceStatus.groupName != null && !deviceStatus.groupName.equals(groupName)) {
            getThing().setProperty(PROPERTY_GROUP_NAME, deviceStatus.groupName);
            groupName = deviceStatus.groupName;
            linkPlayGroupService.registerParticipant(this);
            updateJoinGroupCommandDescription();
            updateAddRemoveMemberCommandDescription();
        }
        logger.debug("Device status: {}", deviceStatus);
    }

    private void updatePresetPicChannels(String groupId, @Nullable String picUrl) {
        if (picUrl == null || !picUrl.trim().startsWith("http")) {
            updateState(groupId, LinkPlayBindingConstants.CHANNEL_PRESET_PIC_URL, UnDefType.NULL);
            updateState(groupId, LinkPlayBindingConstants.CHANNEL_PRESET_PIC, UnDefType.NULL);
        } else {
            updateState(groupId, LinkPlayBindingConstants.CHANNEL_PRESET_PIC_URL, new StringType(picUrl));
            try {
                State image = HttpUtil.downloadImage(picUrl.trim());
                updateState(groupId, LinkPlayBindingConstants.CHANNEL_PRESET_PIC,
                        image != null ? image : UnDefType.NULL);
            } catch (IllegalArgumentException e) {
                logger.debug("Invalid preset image URI: {}", picUrl, e);
            }
        }
    }

    /**
     * Refreshes the play list queue from the device and updates play list channels
     */
    private void refreshPlayListQueue() {
        logger.debug("{}: refreshPlayListQueue", udn);
        PlayQueue pq = upnpClient.getPlayListQueue();
        if (pq == null) {
            logger.debug("{}: Could not retrieve play list queue", udn);
            return;
        }
        String currentPlayListName = pq.getCurrentPlayListName();
        if (currentPlayListName.isBlank()) {
            updateState(GROUP_PLAYBACK, CHANNEL_CURRENT_PLAYLIST_NAME, UnDefType.UNDEF);
            updateState(GROUP_PLAYBACK, CHANNEL_PLAY_PRESET, UnDefType.UNDEF);
            return;
        }
        String playListName = currentPlayListName.split("_#")[0];
        updateState(GROUP_PLAYBACK, CHANNEL_CURRENT_PLAYLIST_NAME, new StringType(playListName));
        if (presetInfo.presetList != null) {
            for (int i = 0; i < presetInfo.presetList.size(); i++) {
                PresetList.Preset p = presetInfo.presetList.get(i);
                if (p.name.equals(playListName)) {
                    updateState(GROUP_PLAYBACK, CHANNEL_PLAY_PRESET, new DecimalType(i + 1));
                }
            }
        }
    }

    /**
     * Helper function to update a state channel for a group or device
     *
     * @param groupId The group ID
     * @param channelId The channel ID
     * @param state The state to update
     */
    public void updateState(String groupId, String channelId, State state) {
        boolean isGroupChannel = GROUP_PROXY_CHANNELS.contains(channelId);
        String groupIdChannel = groupId + "#" + channelId;
        if (inGroup && isGroupChannel) {
            if (isLeader) {
                linkPlayGroupService.updateGroupState(this, groupIdChannel, state);
            } else {
                // if we are not the leader, we these will come from the group proxy
                return;
            }
        }
        updateState(groupIdChannel, state);
    }

    /**
     * LinkPlay uses some strange return types when values are unknown. This tries to handle those when setting states.
     *
     * @param value
     * @param stateClass
     * @return State or UnDefType.NULL if the value is null or unknown
     */
    private State stateOrNull(@Nullable Object value, Class<? extends State> stateClass) {
        if (value == null) {
            return UnDefType.NULL;
        }
        String strValue = value.toString();
        if ("unknow".equalsIgnoreCase(strValue) || "un_known".equalsIgnoreCase(strValue)
                || "unknown".equalsIgnoreCase(strValue)) {
            return UnDefType.NULL;
        }
        try {
            // Try constructor that matches the value's class first
            try {
                Constructor<? extends State> ctor = stateClass.getConstructor(value.getClass());
                return ctor.newInstance(value);
            } catch (NoSuchMethodException ignored) {
                // Fallback to String constructor
                Constructor<? extends State> ctor = stateClass.getConstructor(String.class);
                return ctor.newInstance(strValue);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
                | InvocationTargetException e) {
            logger.debug("Failed to instantiate {} for value {}: {}", stateClass.getSimpleName(), value, e.getMessage(),
                    e);
            return UnDefType.NULL;
        }
    }

    /**
     * Updates the join group command description for the multiroom groups
     */
    private void updateJoinGroupCommandDescription() {
        logger.debug("{}: Updating join group command description for {} participants", udn,
                allGroupParticipants.size());
        List<CommandOption> commandOptions = new ArrayList<>();
        // filter out ourself and participants that are in a group
        allGroupParticipants.stream()
                .filter(participant -> !participant.equals(this) && linkPlayGroupService.getLeader(participant) == null)
                .forEach(participant -> commandOptions.add(new CommandOption(participant.getThingUID().toString(),
                        participant.getGroupParticipantLabel())));

        updateCommandDescription(new ChannelUID(getThing().getUID(), LinkPlayBindingConstants.GROUP_MULTIROOM,
                LinkPlayBindingConstants.CHANNEL_MULTIROOM_JOIN), commandOptions);
    }

    private void updateAddRemoveMemberCommandDescription() {
        updateAddRemoveMemberCommandDescription(null);
    }

    /**
     * Updates the add and remove member command description for the multiroom groups
     *
     * @param slaves The list of slaves in the group (LinkPlay terminology for members)
     */
    private void updateAddRemoveMemberCommandDescription(@Nullable List<Slave> slaves) {
        logger.debug("{}: Updating add member command description for {} participants {}", udn,
                allGroupParticipants.size(), slaves);
        List<CommandOption> commandOptions = new ArrayList<>();
        if (slaves == null) {
            slaves = linkPlayGroupService.getGroupListForLeader(this);
        }
        logger.debug("{}: Slaves: {}", udn, slaves);
        if (isLeader && !slaves.isEmpty()) {
            commandOptions.add(new CommandOption("LEAVE", "-- Remove all players --"));
        }
        commandOptions.add(new CommandOption("ADD_ALL", "-- Add all players --"));
        for (LinkPlayGroupParticipant participant : allGroupParticipants) {
            if (participant.equals(this)) {
                continue;
            }
            if (isLeader && slaves.stream().anyMatch(slave -> slave.ip.equals(participant.getIpAddress()))) {
                commandOptions.add(new CommandOption(participant.getThingUID().toString(),
                        "Remove: " + participant.getGroupParticipantLabel()));
            } else {
                commandOptions.add(new CommandOption(participant.getThingUID().toString(),
                        "Add: " + participant.getGroupParticipantLabel()));
            }
        }
        updateCommandDescription(new ChannelUID(getThing().getUID(), LinkPlayBindingConstants.GROUP_MULTIROOM,
                LinkPlayBindingConstants.CHANNEL_MULTIROOM_MANAGE), commandOptions);
    }

    private void updateCommandDescription(ChannelUID channelUID, List<CommandOption> commandOptionList) {
        CommandDescription commandDescription = CommandDescriptionBuilder.create().withCommandOptions(commandOptionList)
                .build();
        logger.trace("{}: Updating command description for {} {}", udn, channelUID, commandDescription);
        linkPlayCommandDescriptionProvider.setDescription(channelUID, commandDescription);
    }

    /**
     * Checks the UPnP service status of the device and updates the thing status accordingly. Also sends a device search
     * request to the device.
     */
    private void upnpServiceCheck() {
        upnpClient.sendDeviceSearchRequest();
        // group members turn UPnP off when they are not the leader, so ignore UPnP checks
        if (!upnpClient.isRegistered() && (!inGroup || isLeader)) {
            logger.debug("{}: UPnP device not yet registered", udn);
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                setOffline("UPnP device not yet registered");
            }
            upnpClient.removeSubscriptions();
        } else {
            upnpClient.addSubscriptions();
            try {
                updatePresetInfo();
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.trace("{}: Error while retrieving preset info: {}", udn, e.getMessage(), e);
            }
            try {
                updateDeviceStatus();
            } catch (InterruptedException | ExecutionException | TimeoutException | RejectedExecutionException e) {
                logger.trace("{}: Error while retrieving device status: {}", udn, e.getMessage(), e);
            }
        }
    }

    private void cancelReconnectJob() {
        cancelJob(reconnectJob);
        reconnectJob = null;
    }

    private void cancelUpnpServiceCheckJob() {
        cancelJob(upnpServiceCheck);
        upnpServiceCheck = null;
    }

    private void cancelJob(@Nullable ScheduledFuture<?> job) {
        if (job != null) {
            job.cancel(true);
        }
    }
}
