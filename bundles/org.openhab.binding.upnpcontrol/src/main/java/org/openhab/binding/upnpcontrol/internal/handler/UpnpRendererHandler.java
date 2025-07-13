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
package org.openhab.binding.upnpcontrol.internal.handler;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jupnp.UpnpService;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.upnpcontrol.internal.UpnpChannelName;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicCommandDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicStateDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.audiosink.UpnpAudioSinkReg;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlBindingConfiguration;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlRendererConfiguration;
import org.openhab.binding.upnpcontrol.internal.queue.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.queue.UpnpEntryQueue;
import org.openhab.binding.upnpcontrol.internal.queue.UpnpFavorite;
import org.openhab.binding.upnpcontrol.internal.services.UpnpRenderingControlConfiguration;
import org.openhab.binding.upnpcontrol.internal.util.UpnpControlUtil;
import org.openhab.binding.upnpcontrol.internal.util.UpnpXMLParser;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpRendererHandler} is responsible for handling commands sent to the UPnP Renderer. It extends
 * {@link UpnpHandler} with UPnP renderer specific logic. It implements UPnP AVTransport and RenderingControl service
 * actions.
 *
 * @author Mark Herwege - Initial contribution
 * @author Karel Goderis - Based on UPnP logic in Sonos binding
 */
@NonNullByDefault
public class UpnpRendererHandler extends UpnpHandler {

    private final Logger logger = LoggerFactory.getLogger(UpnpRendererHandler.class);

    // UPnP constants
    static final String RENDERING_CONTROL = "RenderingControl";
    static final String AV_TRANSPORT = "AVTransport";
    static final String INSTANCE_ID = "InstanceID";

    private volatile boolean audioSupport;
    protected volatile Set<AudioFormat> supportedAudioFormats = new HashSet<>();
    private volatile boolean audioSinkRegistered;

    private volatile UpnpAudioSinkReg audioSinkReg;

    private volatile Set<UpnpServerHandler> serverHandlers = ConcurrentHashMap.newKeySet();

    protected @NonNullByDefault({}) UpnpControlRendererConfiguration config;
    private UpnpRenderingControlConfiguration renderingControlConfiguration = new UpnpRenderingControlConfiguration();

    private volatile List<CommandOption> favoriteCommandOptionList = List.of();
    private volatile List<CommandOption> playlistCommandOptionList = List.of();

    private @NonNullByDefault({}) ChannelUID favoriteSelectChannelUID;
    private @NonNullByDefault({}) ChannelUID playlistSelectChannelUID;

    private volatile PercentType soundVolume = new PercentType();
    private @Nullable volatile PercentType notificationVolume;
    private volatile List<String> sink = new ArrayList<>();

    private volatile String favoriteName = ""; // Currently selected favorite

    private volatile boolean repeat;
    private volatile boolean shuffle;
    private volatile boolean onlyplayone; // Set to true if we only want to play one at a time

    // Queue as received from server and current and next media entries for playback
    private volatile UpnpEntryQueue currentQueue = new UpnpEntryQueue();
    volatile @Nullable UpnpEntry currentEntry = null;
    volatile @Nullable UpnpEntry nextEntry = null;

    // Group of fields representing current state of player
    private volatile String nowPlayingUri = ""; // Used to block waiting for setting URI when it is the same as current
                                                // as some players will not send URI update when it is the same as
                                                // previous
    private volatile String transportState = ""; // Current transportState to be able to refresh the control
    volatile boolean playerStopped; // Set if the player is stopped from OH command or code, allows to identify
                                    // if STOP came from other source when receiving STOP state from GENA event
    volatile boolean playing; // Set to false when a STOP is received, so we can filter two consecutive STOPs
                              // and not play next entry second time
    private volatile @Nullable ScheduledFuture<?> paused; // Set when a pause command is given, to compensate for
                                                          // renderers that cannot pause playback
    private volatile @Nullable CompletableFuture<Boolean> isSettingURI; // Set to wait for setting URI before starting
                                                                        // to play or seeking
    private volatile @Nullable CompletableFuture<Boolean> isStopping; // Set when stopping to be able to wait for stop
                                                                      // confirmation for subsequent actions that need
                                                                      // the player to be stopped
    volatile boolean registeredQueue; // Set when registering a new queue. This allows to decide if we just
                                      // need to play URI, or serve the first entry in a queue when a play
                                      // command is given.
    volatile boolean playingQueue; // Identifies if we are playing a queue received from a server. If so, a new
                                   // queue received will be played after the currently playing entry
    private volatile boolean oneplayed; // Set to true when the one entry is being played, allows to check if stop is
                                        // needed when only playing one
    volatile boolean playingNotification; // Set when playing a notification
    private volatile @Nullable ScheduledFuture<?> playingNotificationFuture; // Set when playing a notification, allows
                                                                             // timing out notification
    private volatile String notificationUri = ""; // Used to check if the received URI is from the notification
    private final Object notificationLock = new Object();

    // Track position and duration fields
    private volatile int trackDuration = 0;
    private volatile int trackPosition = 0;
    private volatile long expectedTrackend = 0;
    private volatile @Nullable ScheduledFuture<?> trackPositionRefresh;
    private volatile int posAtNotificationStart = 0;

    public UpnpRendererHandler(Thing thing, UpnpIOService upnpIOService, UpnpService upnpService,
            UpnpAudioSinkReg audioSinkReg, UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider,
            UpnpDynamicCommandDescriptionProvider upnpCommandDescriptionProvider,
            UpnpControlBindingConfiguration configuration) {
        super(thing, upnpIOService, upnpService, configuration, upnpStateDescriptionProvider,
                upnpCommandDescriptionProvider);

        serviceSubscriptions.add(AV_TRANSPORT);
        serviceSubscriptions.add(RENDERING_CONTROL);

        this.audioSinkReg = audioSinkReg;
    }

    @Override
    public void initialize() {
        super.initialize();
        config = getConfigAs(UpnpControlRendererConfiguration.class);
        if (config.seekStep < 1) {
            config.seekStep = 1;
        }
        logger.debug("Initializing handler for media renderer device {}", thing.getLabel());

        Channel favoriteSelectChannel = thing.getChannel(FAVORITE_SELECT);
        if (favoriteSelectChannel != null) {
            favoriteSelectChannelUID = favoriteSelectChannel.getUID();
        } else {
            String msg = String.format("@text/offline.channel-undefined [ \"%s\" ]", FAVORITE_SELECT);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }
        Channel playlistSelectChannel = thing.getChannel(PLAYLIST_SELECT);
        if (playlistSelectChannel != null) {
            playlistSelectChannelUID = playlistSelectChannel.getUID();
        } else {
            String msg = String.format("@text/offline.channel-undefined [ \"%s\" ]", PLAYLIST_SELECT);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return;
        }

        initDevice();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for media renderer device {}", thing.getLabel());

        cancelTrackPositionRefresh();
        resetPaused();
        CompletableFuture<Boolean> settingURI = isSettingURI;
        if (settingURI != null) {
            settingURI.complete(false);
        }

        super.dispose();
    }

    @Override
    protected void initJob() {
        synchronized (jobLock) {
            sendDeviceSearchRequest();

            if (!upnpIOService.isRegistered(this)) {
                String msg = String.format("@text/offline.device-not-registered [ \"%s\" ]", getUDN());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                return;
            }

            if (!ThingStatus.ONLINE.equals(thing.getStatus())) {
                getProtocolInfo();

                getCurrentConnectionInfo();
                if (!checkForConnectionIds()) {
                    String msg = String.format("@text/offline.no-connection-ids [ \"%s\" ]", getUDN());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
                    return;
                }

                getTransportState();

                updateFavoritesList();
                playlistsListChanged();

                RemoteDevice device = getDevice();
                if (device != null) { // The handler factory will update the device config later when it has not been
                                      // set yet
                    updateDeviceConfig(device);
                }

                updateStatus(ThingStatus.ONLINE);
            }

            if (!upnpSubscribed) {
                addSubscriptions();
            }
        }
    }

    @Override
    public void updateDeviceConfig(RemoteDevice device) {
        super.updateDeviceConfig(device);

        UpnpRenderingControlConfiguration config = new UpnpRenderingControlConfiguration(device);
        renderingControlConfiguration = config;
        for (String audioChannel : config.audioChannels) {
            createAudioChannels(audioChannel);
        }

        updateChannels();
    }

    private void createAudioChannels(String audioChannel) {
        UpnpRenderingControlConfiguration config = renderingControlConfiguration;
        if (config.volume && !UPNP_MASTER.equals(audioChannel)) {
            String name = audioChannel + "volume";
            if (UpnpChannelName.channelIdToUpnpChannelName(name) != null) {
                createChannel(UpnpChannelName.channelIdToUpnpChannelName(name));
            } else {
                String label = String.format("@text/channel.upnpcontrol.vendorvolume.label [ \"%s\" ]", audioChannel);
                createChannel(name, label, "@text/channel.upnpcontrol.vendorvolume.description", ITEM_TYPE_VOLUME,
                        CHANNEL_TYPE_VOLUME);
            }
        }
        if (config.mute && !UPNP_MASTER.equals(audioChannel)) {
            String name = audioChannel + "mute";
            if (UpnpChannelName.channelIdToUpnpChannelName(name) != null) {
                createChannel(UpnpChannelName.channelIdToUpnpChannelName(name));
            } else {
                String label = String.format("@text/channel.upnpcontrol.vendormute.label [ \"%s\" ]", audioChannel);
                createChannel(name, label, "@text/channel.upnpcontrol.vendormute.description", ITEM_TYPE_MUTE,
                        CHANNEL_TYPE_MUTE);
            }
        }
        if (config.loudness) {
            String name = (UPNP_MASTER.equals(audioChannel) ? "" : audioChannel) + "loudness";
            if (UpnpChannelName.channelIdToUpnpChannelName(name) != null) {
                createChannel(UpnpChannelName.channelIdToUpnpChannelName(name));
            } else {
                String label = String.format("@text/channel.upnpcontrol.vendorloudness.label [ \"%s\" ]", audioChannel);
                createChannel(name, label, "@text/channel.upnpcontrol.vendorloudness.description", ITEM_TYPE_LOUDNESS,
                        CHANNEL_TYPE_LOUDNESS);
            }
        }
    }

    /**
     * Invoke Stop on UPnP AV Transport.
     */
    public void stop() {
        playerStopped = true;

        if (playing) {
            CompletableFuture<Boolean> stopping = isStopping;
            if (stopping != null) {
                stopping.complete(false);
            }
            isStopping = new CompletableFuture<>(); // set this so we can check if stop confirmation has been
                                                    // received
        }

        Map<String, String> inputs = Map.of(INSTANCE_ID, Integer.toString(avTransportId));

        invokeAction(AV_TRANSPORT, "Stop", inputs);
    }

    /**
     * Invoke Play on UPnP AV Transport.
     */
    public void play() {
        CompletableFuture<Boolean> settingURI = isSettingURI;
        boolean uriSet = true;
        try {
            if (settingURI != null) {
                // wait for maximum 2.5s until the media URI is set before playing
                uriSet = settingURI.get(config.responseTimeout, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Timeout exception, media URI not yet set in renderer {}, trying to play anyway",
                    thing.getLabel());
        }

        if (uriSet) {
            Map<String, String> inputs = new HashMap<>();
            inputs.put(INSTANCE_ID, Integer.toString(avTransportId));
            inputs.put("Speed", "1");

            invokeAction(AV_TRANSPORT, "Play", inputs);
        } else {
            logger.debug("Cannot play, cancelled setting URI in the renderer {}", thing.getLabel());
        }
    }

    /**
     * Invoke Pause on UPnP AV Transport.
     */
    protected void pause() {
        Map<String, String> inputs = Map.of(INSTANCE_ID, Integer.toString(avTransportId));

        invokeAction(AV_TRANSPORT, "Pause", inputs);
    }

    /**
     * Invoke Next on UPnP AV Transport.
     */
    protected void next() {
        Map<String, String> inputs = Map.of(INSTANCE_ID, Integer.toString(avTransportId));

        invokeAction(AV_TRANSPORT, "Next", inputs);
    }

    /**
     * Invoke Previous on UPnP AV Transport.
     */
    protected void previous() {
        Map<String, String> inputs = Map.of(INSTANCE_ID, Integer.toString(avTransportId));

        invokeAction(AV_TRANSPORT, "Previous", inputs);
    }

    /**
     * Invoke Seek on UPnP AV Transport.
     *
     * @param seekTarget relative position in current track, format HH:mm:ss
     */
    protected void seek(String seekTarget) {
        CompletableFuture<Boolean> settingURI = isSettingURI;
        boolean uriSet = true;
        try {
            if (settingURI != null) {
                // wait for maximum 2.5s until the media URI is set before seeking
                uriSet = settingURI.get(config.responseTimeout, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Timeout exception, media URI not yet set in renderer {}, skipping seek", thing.getLabel());
            return;
        }

        if (uriSet) {
            Map<String, String> inputs = new HashMap<>();
            inputs.put(INSTANCE_ID, Integer.toString(avTransportId));
            inputs.put("Unit", "REL_TIME");
            inputs.put("Target", seekTarget);

            invokeAction(AV_TRANSPORT, "Seek", inputs);
        } else {
            logger.debug("Cannot seek, cancelled setting URI in the renderer {}", thing.getLabel());
        }
    }

    /**
     * Invoke SetAVTransportURI on UPnP AV Transport.
     *
     * @param URI
     * @param URIMetaData
     */
    public void setCurrentURI(String URI, String URIMetaData) {
        String uri = "";
        uri = URLDecoder.decode(URI.trim(), StandardCharsets.UTF_8);
        // Some renderers don't send a URI Last Changed event when the same URI is requested, so don't wait for it
        // before starting to play
        if (!uri.equals(nowPlayingUri) && !playingNotification) {
            CompletableFuture<Boolean> settingURI = isSettingURI;
            if (settingURI != null) {
                settingURI.complete(false);
            }
            isSettingURI = new CompletableFuture<>(); // set this so we don't start playing when not finished
                                                      // setting URI
        } else {
            logger.debug("New URI {} is same as previous on renderer {}", nowPlayingUri, thing.getLabel());
        }

        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(avTransportId));
        inputs.put("CurrentURI", uri);
        inputs.put("CurrentURIMetaData", URIMetaData);

        invokeAction(AV_TRANSPORT, "SetAVTransportURI", inputs);
    }

    /**
     * Invoke SetNextAVTransportURI on UPnP AV Transport.
     *
     * @param nextURI
     * @param nextURIMetaData
     */
    protected void setNextURI(String nextURI, String nextURIMetaData) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(avTransportId));
        inputs.put("NextURI", nextURI);
        inputs.put("NextURIMetaData", nextURIMetaData);

        invokeAction(AV_TRANSPORT, "SetNextAVTransportURI", inputs);
    }

    /**
     * Invoke GetTransportState on UPnP AV Transport.
     * Result is received in {@link #onValueReceived}.
     */
    protected void getTransportState() {
        Map<String, String> inputs = Map.of(INSTANCE_ID, Integer.toString(avTransportId));

        invokeAction(AV_TRANSPORT, "GetTransportInfo", inputs);
    }

    /**
     * Invoke getPositionInfo on UPnP AV Transport.
     * Result is received in {@link #onValueReceived}.
     */
    protected void getPositionInfo() {
        Map<String, String> inputs = Map.of(INSTANCE_ID, Integer.toString(avTransportId));

        invokeAction(AV_TRANSPORT, "GetPositionInfo", inputs);
    }

    /**
     * Invoke GetMediaInfo on UPnP AV Transport.
     * Result is received in {@link #onValueReceived}.
     */
    protected void getMediaInfo() {
        Map<String, String> inputs = Map.of(INSTANCE_ID, Integer.toString(avTransportId));

        invokeAction(AV_TRANSPORT, "smarthome:audio stream http://icecast.vrtcdn.be/stubru_tijdloze-high.mp3", inputs);
    }

    /**
     * Retrieves the current volume known to the control point, gets updated by GENA events or after UPnP Rendering
     * Control GetVolume call. This method is used to retrieve volume with the
     * {@link org.openhab.binding.upnpcontrol.internal.audiosink.UpnpAudioSink#getVolume UpnpAudioSink.getVolume}
     * method.
     *
     * @return current volume
     */
    public PercentType getCurrentVolume() {
        return soundVolume;
    }

    /**
     * Invoke GetVolume on UPnP Rendering Control.
     * Result is received in {@link #onValueReceived}.
     *
     * @param channel
     */
    protected void getVolume(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(rcsId));
        inputs.put("Channel", channel);

        invokeAction(RENDERING_CONTROL, "GetVolume", inputs);
    }

    /**
     * Invoke SetVolume on UPnP Rendering Control.
     *
     * @param channel
     * @param volume
     */
    protected void setVolume(String channel, PercentType volume) {
        UpnpRenderingControlConfiguration config = renderingControlConfiguration;

        long newVolume = volume.intValue() * config.maxvolume / 100;
        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(rcsId));
        inputs.put("Channel", channel);
        inputs.put("DesiredVolume", String.valueOf(newVolume));

        invokeAction(RENDERING_CONTROL, "SetVolume", inputs);
    }

    /**
     * Invoke SetVolume for Master channel on UPnP Rendering Control.
     *
     * @param volume
     */
    public void setVolume(PercentType volume) {
        setVolume(UPNP_MASTER, volume);
    }

    /**
     * Invoke getMute on UPnP Rendering Control.
     * Result is received in {@link #onValueReceived}.
     *
     * @param channel
     */
    protected void getMute(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(rcsId));
        inputs.put("Channel", channel);

        invokeAction(RENDERING_CONTROL, "GetMute", inputs);
    }

    /**
     * Invoke SetMute on UPnP Rendering Control.
     *
     * @param channel
     * @param mute
     */
    protected void setMute(String channel, OnOffType mute) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(rcsId));
        inputs.put("Channel", channel);
        inputs.put("DesiredMute", mute == OnOffType.ON ? "1" : "0");

        invokeAction(RENDERING_CONTROL, "SetMute", inputs);
    }

    /**
     * Invoke getMute on UPnP Rendering Control.
     * Result is received in {@link #onValueReceived}.
     *
     * @param channel
     */
    protected void getLoudness(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(rcsId));
        inputs.put("Channel", channel);

        invokeAction(RENDERING_CONTROL, "GetLoudness", inputs);
    }

    /**
     * Invoke SetMute on UPnP Rendering Control.
     *
     * @param channel
     * @param mute
     */
    protected void setLoudness(String channel, OnOffType mute) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put(INSTANCE_ID, Integer.toString(rcsId));
        inputs.put("Channel", channel);
        inputs.put("DesiredLoudness", mute == OnOffType.ON ? "1" : "0");

        invokeAction(RENDERING_CONTROL, "SetLoudness", inputs);
    }

    /**
     * Called from server handler for renderer to be able to send back status to server handler
     *
     * @param handler
     */
    protected void setServerHandler(UpnpServerHandler handler) {
        logger.debug("Set server handler {} on renderer {}", handler.getThing().getLabel(), thing.getLabel());
        serverHandlers.add(handler);
    }

    /**
     * Should be called from server handler when server stops serving this renderer
     */
    protected void unsetServerHandler() {
        logger.debug("Unset server handler on renderer {}", thing.getLabel());
        for (UpnpServerHandler handler : serverHandlers) {
            Thing serverThing = handler.getThing();
            Channel serverChannel;
            for (String channel : SERVER_CONTROL_CHANNELS) {
                if ((serverChannel = serverThing.getChannel(channel)) != null) {
                    handler.updateServerState(serverChannel.getUID(), UnDefType.UNDEF);
                }
            }

            serverHandlers.remove(handler);
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        // override to be able to propagate channel state updates to corresponding channels on the server
        if (SERVER_CONTROL_CHANNELS.contains(channelUID.getId())) {
            for (UpnpServerHandler handler : serverHandlers) {
                Thing serverThing = handler.getThing();
                Channel serverChannel = serverThing.getChannel(channelUID.getId());
                if (serverChannel != null) {
                    logger.debug("Update server {} channel {} with state {} from renderer {}", serverThing.getLabel(),
                            state, channelUID, thing.getLabel());
                    handler.updateServerState(serverChannel.getUID(), state);
                }
            }
        }
        super.updateState(channelUID, state);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {} on renderer {}", command, channelUID, thing.getLabel());

        String id = channelUID.getId();

        if (id.endsWith("volume")) {
            handleCommandVolume(command, id);
        } else if (id.endsWith("mute")) {
            handleCommandMute(command, id);
        } else if (id.endsWith("loudness")) {
            handleCommandLoudness(command, id);
        } else {
            switch (id) {
                case STOP:
                    handleCommandStop(command);
                    break;
                case CONTROL:
                    handleCommandControl(channelUID, command);
                    break;
                case REPEAT:
                    handleCommandRepeat(channelUID, command);
                    break;
                case SHUFFLE:
                    handleCommandShuffle(channelUID, command);
                    break;
                case ONLY_PLAY_ONE:
                    handleCommandOnlyPlayOne(channelUID, command);
                    break;
                case URI:
                    handleCommandUri(channelUID, command);
                    break;
                case FAVORITE_SELECT:
                    handleCommandFavoriteSelect(command);
                    break;
                case FAVORITE:
                    handleCommandFavorite(channelUID, command);
                    break;
                case FAVORITE_ACTION:
                    handleCommandFavoriteAction(command);
                    break;
                case PLAYLIST_SELECT:
                    handleCommandPlaylistSelect(command);
                    break;
                case TRACK_POSITION:
                    handleCommandTrackPosition(channelUID, command);
                    break;
                case REL_TRACK_POSITION:
                    handleCommandRelTrackPosition(channelUID, command);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleCommandVolume(Command command, String id) {
        if (command instanceof RefreshType) {
            getVolume("volume".equals(id) ? UPNP_MASTER : id.replace("volume", ""));
        } else if (command instanceof PercentType percentCommand) {
            setVolume("volume".equals(id) ? UPNP_MASTER : id.replace("volume", ""), percentCommand);
        }
    }

    private void handleCommandMute(Command command, String id) {
        if (command instanceof RefreshType) {
            getMute("mute".equals(id) ? UPNP_MASTER : id.replace("mute", ""));
        } else if (command instanceof OnOffType onOffCommand) {
            setMute("mute".equals(id) ? UPNP_MASTER : id.replace("mute", ""), onOffCommand);
        }
    }

    private void handleCommandLoudness(Command command, String id) {
        if (command instanceof RefreshType) {
            getLoudness("loudness".equals(id) ? UPNP_MASTER : id.replace("loudness", ""));
        } else if (command instanceof OnOffType onOffCommand) {
            setLoudness("loudness".equals(id) ? UPNP_MASTER : id.replace("loudness", ""), onOffCommand);
        }
    }

    private void handleCommandStop(Command command) {
        if (OnOffType.ON.equals(command)) {
            updateState(CONTROL, PlayPauseType.PAUSE);
            stop();
            updateState(TRACK_POSITION, new QuantityType<>(0, Units.SECOND));
        }
    }

    private void handleCommandControl(ChannelUID channelUID, Command command) {
        String state;
        if (command instanceof RefreshType) {
            state = transportState;
            State newState = UnDefType.UNDEF;
            if ("PLAYING".equals(state)) {
                newState = PlayPauseType.PLAY;
            } else if ("STOPPED".equals(state)) {
                newState = PlayPauseType.PAUSE;
            } else if ("PAUSED_PLAYBACK".equals(state)) {
                newState = PlayPauseType.PAUSE;
            }
            updateState(channelUID, newState);
        } else if (command instanceof PlayPauseType) {
            if (PlayPauseType.PLAY.equals(command)) {
                if (registeredQueue) {
                    registeredQueue = false;
                    playingQueue = true;
                    oneplayed = false;
                    serve();
                } else {
                    play();
                }
            } else if (PlayPauseType.PAUSE.equals(command)) {
                checkPaused();
                pause();
            }
        } else if (command instanceof NextPreviousType) {
            if (NextPreviousType.NEXT.equals(command)) {
                serveNext();
            } else if (NextPreviousType.PREVIOUS.equals(command)) {
                servePrevious();
            }
        } else if (command instanceof RewindFastforwardType) {
            int pos = 0;
            if (RewindFastforwardType.FASTFORWARD.equals(command)) {
                pos = Integer.min(trackDuration, trackPosition + config.seekStep);
            } else if (command == RewindFastforwardType.REWIND) {
                pos = Integer.max(0, trackPosition - config.seekStep);
            }
            seek(String.format("%02d:%02d:%02d", pos / 3600, (pos % 3600) / 60, pos % 60));
        }
    }

    private void handleCommandRepeat(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.from(repeat));
        } else {
            repeat = (OnOffType.ON.equals(command));
            currentQueue.setRepeat(repeat);
            updateState(channelUID, OnOffType.from(repeat));
            logger.debug("Repeat set to {} for {}", repeat, thing.getLabel());
        }
    }

    private void handleCommandShuffle(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.from(shuffle));
        } else {
            shuffle = (OnOffType.ON.equals(command));
            currentQueue.setShuffle(shuffle);
            if (!playing) {
                resetToStartQueue();
            }
            updateState(channelUID, OnOffType.from(shuffle));
            logger.debug("Shuffle set to {} for {}", shuffle, thing.getLabel());
        }
    }

    private void handleCommandOnlyPlayOne(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, OnOffType.from(onlyplayone));
        } else {
            onlyplayone = (OnOffType.ON.equals(command));
            oneplayed = (onlyplayone && playing) ? true : false;
            if (oneplayed) {
                setNextURI("", "");
            } else {
                UpnpEntry next = nextEntry;
                if (next != null) {
                    setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
                }
            }
            updateState(channelUID, OnOffType.from(onlyplayone));
            logger.debug("OnlyPlayOne set to {} for {}", onlyplayone, thing.getLabel());
        }
    }

    private void handleCommandUri(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, StringType.valueOf(nowPlayingUri));
        } else if (command instanceof StringType) {
            setCurrentURI(command.toString(), "");
            play();
        }
    }

    private void handleCommandFavoriteSelect(Command command) {
        if (command instanceof StringType) {
            favoriteName = command.toString();
            updateState(FAVORITE, StringType.valueOf(favoriteName));
            playFavorite();
        }
    }

    private void handleCommandFavorite(ChannelUID channelUID, Command command) {
        if (command instanceof StringType) {
            favoriteName = command.toString();
            if (favoriteCommandOptionList.contains(new CommandOption(favoriteName, favoriteName))) {
                playFavorite();
            }
        }
        updateState(channelUID, StringType.valueOf(favoriteName));
    }

    private void handleCommandFavoriteAction(Command command) {
        if (command instanceof StringType) {
            switch (command.toString()) {
                case SAVE:
                    handleCommandFavoriteSave();
                    break;
                case DELETE:
                    handleCommandFavoriteDelete();
                    break;
            }
        }
    }

    private void handleCommandFavoriteSave() {
        if (!favoriteName.isEmpty()) {
            UpnpFavorite favorite = new UpnpFavorite(favoriteName, nowPlayingUri, currentEntry);
            favorite.saveFavorite(favoriteName, bindingConfig.path);
            updateFavoritesList();
        }
    }

    private void handleCommandFavoriteDelete() {
        if (!favoriteName.isEmpty()) {
            UpnpControlUtil.deleteFavorite(favoriteName, bindingConfig.path);
            updateFavoritesList();
            updateState(FAVORITE, UnDefType.UNDEF);
        }
    }

    private void handleCommandPlaylistSelect(Command command) {
        if (command instanceof StringType) {
            String playlistName = command.toString();
            UpnpEntryQueue queue = new UpnpEntryQueue();
            queue.restoreQueue(playlistName, null, bindingConfig.path);
            registerQueue(queue);
            resetToStartQueue();
            playingQueue = true;
            serve();
        }
    }

    private void handleCommandTrackPosition(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateState(channelUID, new QuantityType<>(trackPosition, Units.SECOND));
        } else if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> position = quantityCommand.toUnit(Units.SECOND);
            if (position != null) {
                int pos = Integer.min(trackDuration, position.intValue());
                seek(String.format("%02d:%02d:%02d", pos / 3600, (pos % 3600) / 60, pos % 60));
            }
        }
    }

    private void handleCommandRelTrackPosition(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            int relPosition = (trackDuration != 0) ? (trackPosition * 100) / trackDuration : 0;
            updateState(channelUID, new PercentType(relPosition));
        } else if (command instanceof PercentType percentCommand) {
            int pos = percentCommand.intValue() * trackDuration / 100;
            seek(String.format("%02d:%02d:%02d", pos / 3600, (pos % 3600) / 60, pos % 60));
        }
    }

    /**
     * Set the volume for notifications.
     *
     * @param volume
     */
    public void setNotificationVolume(PercentType volume) {
        notificationVolume = volume;
    }

    /**
     * Play a notification. Previous state of the renderer will resume at the end of the notification, or after the
     * maximum notification duration as defined in the renderer parameters.
     *
     * @param URI for notification sound
     */
    public void playNotification(String URI) {
        synchronized (notificationLock) {
            if (URI.isEmpty()) {
                logger.debug("UPnP device {} received empty notification URI", thing.getLabel());
                return;
            }

            notificationUri = URI;

            logger.debug("UPnP device {} playing notification {}", thing.getLabel(), URI);

            cancelTrackPositionRefresh();
            getPositionInfo();

            cancelPlayingNotificationFuture();

            if (config.maxNotificationDuration > 0) {
                playingNotificationFuture = upnpScheduler.schedule(this::stop, config.maxNotificationDuration,
                        TimeUnit.SECONDS);
            }
            playingNotification = true;

            setCurrentURI(URI, "");
            setNextURI("", "");
            PercentType volume = notificationVolume;
            setVolume(volume == null
                    ? new PercentType(Math.min(100,
                            Math.max(0, (100 + config.notificationVolumeAdjustment) * soundVolume.intValue() / 100)))
                    : volume);

            CompletableFuture<Boolean> stopping = isStopping;
            try {
                if (stopping != null) {
                    // wait for maximum 2.5s until the renderer stopped before playing
                    stopping.get(config.responseTimeout, TimeUnit.MILLISECONDS);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                logger.debug("Timeout exception, renderer {} didn't stop yet, trying to play anyway", thing.getLabel());
            }
            play();
        }
    }

    private void cancelPlayingNotificationFuture() {
        ScheduledFuture<?> future = playingNotificationFuture;
        if (future != null) {
            future.cancel(true);
            playingNotificationFuture = null;
        }
    }

    private void resumeAfterNotification() {
        synchronized (notificationLock) {
            logger.debug("UPnP device {} resume after playing notification", thing.getLabel());

            setCurrentURI(nowPlayingUri, "");
            setVolume(soundVolume);

            cancelPlayingNotificationFuture();

            playingNotification = false;
            notificationVolume = null;
            notificationUri = "";

            if (playing) {
                int pos = posAtNotificationStart;
                seek(String.format("%02d:%02d:%02d", pos / 3600, (pos % 3600) / 60, pos % 60));
                play();
            }
            posAtNotificationStart = 0;
        }
    }

    private void playFavorite() {
        UpnpFavorite favorite = new UpnpFavorite(favoriteName, bindingConfig.path);
        String uri = favorite.getUri();
        UpnpEntry entry = favorite.getUpnpEntry();
        if (!uri.isEmpty()) {
            String metadata = "";
            if (entry != null) {
                metadata = UpnpXMLParser.compileMetadataString(entry);
            }
            setCurrentURI(uri, metadata);
            play();
        }
    }

    void updateFavoritesList() {
        favoriteCommandOptionList = UpnpControlUtil.favorites(bindingConfig.path).stream()
                .map(p -> (new CommandOption(p, p))).collect(Collectors.toList());
        updateCommandDescription(favoriteSelectChannelUID, favoriteCommandOptionList);
    }

    @Override
    public void playlistsListChanged() {
        playlistCommandOptionList = UpnpControlUtil.playlists().stream().map(p -> (new CommandOption(p, p)))
                .collect(Collectors.toList());
        updateCommandDescription(playlistSelectChannelUID, playlistCommandOptionList);
    }

    @Override
    public void onStatusChanged(boolean status) {
        if (!status) {
            removeSubscriptions();

            updateState(CONTROL, PlayPauseType.PAUSE);
            cancelTrackPositionRefresh();
        }
        super.onStatusChanged(status);
    }

    @Override
    protected @Nullable String preProcessValueReceived(Map<String, String> inputs, @Nullable String variable,
            @Nullable String value, @Nullable String service, @Nullable String action) {
        if (variable == null) {
            return null;
        } else {
            switch (variable) {
                case "CurrentVolume":
                    return (inputs.containsKey("Channel") ? inputs.get("Channel") : UPNP_MASTER) + "Volume";
                case "CurrentMute":
                    return (inputs.containsKey("Channel") ? inputs.get("Channel") : UPNP_MASTER) + "Mute";
                case "CurrentLoudness":
                    return (inputs.containsKey("Channel") ? inputs.get("Channel") : UPNP_MASTER) + "Loudness";
                default:
                    return variable;
            }
        }
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (logger.isTraceEnabled()) {
            logger.trace("UPnP device {} received variable {} with value {} from service {}", thing.getLabel(),
                    variable, value, service);
        } else {
            if (logger.isDebugEnabled() && !("AbsTime".equals(variable) || "RelCount".equals(variable)
                    || "RelTime".equals(variable) || "AbsCount".equals(variable) || "Track".equals(variable)
                    || "TrackDuration".equals(variable))) {
                // don't log all variables received when updating the track position every second
                logger.debug("UPnP device {} received variable {} with value {} from service {}", thing.getLabel(),
                        variable, value, service);
            }
        }
        if (variable == null) {
            return;
        }

        if (variable.endsWith("Volume")) {
            onValueReceivedVolume(variable, value);
        } else if (variable.endsWith("Mute")) {
            onValueReceivedMute(variable, value);
        } else if (variable.endsWith("Loudness")) {
            onValueReceivedLoudness(variable, value);
        } else {
            switch (variable) {
                case "LastChange":
                    onValueReceivedLastChange(value, service);
                    break;
                case "CurrentTransportState":
                case "TransportState":
                    onValueReceivedTransportState(value);
                    break;
                case "CurrentTrackURI":
                case "CurrentURI":
                    onValueReceivedCurrentURI(value);
                    break;
                case "CurrentTrackMetaData":
                case "CurrentURIMetaData":
                    onValueReceivedCurrentMetaData(value);
                    break;
                case "NextAVTransportURIMetaData":
                case "NextURIMetaData":
                    onValueReceivedNextMetaData(value);
                    break;
                case "CurrentTrackDuration":
                case "TrackDuration":
                    onValueReceivedDuration(value);
                    break;
                case "RelTime":
                    onValueReceivedRelTime(value);
                    break;
                default:
                    super.onValueReceived(variable, value, service);
                    break;
            }
        }
    }

    private void onValueReceivedVolume(String variable, @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            UpnpRenderingControlConfiguration config = renderingControlConfiguration;

            long volume = Long.valueOf(value);
            if (volume < 0) {
                logger.warn("UPnP device {} received invalid volume value {}", thing.getLabel(), value);
                return;
            }
            volume = volume * 100 / config.maxvolume;

            String upnpChannel = variable.replace("Volume", "volume").replace("Master", "");
            updateState(upnpChannel, new PercentType((int) volume));

            if (!playingNotification && "volume".equals(upnpChannel)) {
                soundVolume = new PercentType((int) volume);
            }
        }
    }

    private void onValueReceivedMute(String variable, @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            String upnpChannel = variable.replace("Mute", "mute").replace("Master", "");
            updateState(upnpChannel, OnOffType.from("1".equals(value) || "true".equalsIgnoreCase(value)));
        }
    }

    private void onValueReceivedLoudness(String variable, @Nullable String value) {
        if (value != null && !value.isEmpty()) {
            String upnpChannel = variable.replace("Loudness", "loudness").replace("Master", "");
            updateState(upnpChannel, OnOffType.from("1".equals(value) || "true".equalsIgnoreCase(value)));
        }
    }

    private void onValueReceivedLastChange(@Nullable String value, @Nullable String service) {
        // This is returned from a GENA subscription. The jupnp library does not allow receiving new GENA subscription
        // messages as long as this thread has not finished. As we may trigger long running processes based on this
        // result, we run it in a separate thread.
        upnpScheduler.submit(() -> {
            // pre-process some variables, eg XML processing
            if (value != null && !value.isEmpty()) {
                if (AV_TRANSPORT.equals(service)) {
                    Map<String, String> parsedValues = UpnpXMLParser.getAVTransportFromXML(value);
                    for (Map.Entry<String, String> entrySet : parsedValues.entrySet()) {
                        switch (entrySet.getKey()) {
                            case "TransportState":
                                // Update the transport state after the update of the media information
                                // to not break the notification mechanism
                                break;
                            case "AVTransportURI":
                                onValueReceived("CurrentTrackURI", entrySet.getValue(), service);
                                break;
                            case "AVTransportURIMetaData":
                                onValueReceived("CurrentTrackMetaData", entrySet.getValue(), service);
                                break;
                            default:
                                onValueReceived(entrySet.getKey(), entrySet.getValue(), service);
                        }
                    }
                    if (parsedValues.containsKey("TransportState")) {
                        onValueReceived("TransportState", parsedValues.get("TransportState"), service);
                    }
                } else if (RENDERING_CONTROL.equals(service)) {
                    Map<String, @Nullable String> parsedValues = UpnpXMLParser.getRenderingControlFromXML(value);
                    for (String parsedValue : parsedValues.keySet()) {
                        onValueReceived(parsedValue, parsedValues.get(parsedValue), RENDERING_CONTROL);
                    }
                }
            }
        });
    }

    private void onValueReceivedTransportState(@Nullable String value) {
        transportState = (value == null) ? "" : value;

        if ("STOPPED".equals(value)) {
            CompletableFuture<Boolean> stopping = isStopping;
            if (stopping != null) {
                stopping.complete(true); // We have received stop confirmation
                isStopping = null;
            }

            if (playingNotification) {
                resumeAfterNotification();
                return;
            }

            cancelCheckPaused();
            updateState(CONTROL, PlayPauseType.PAUSE);
            cancelTrackPositionRefresh();
            // Only go to next for first STOP command, then wait until we received PLAYING before moving
            // to next (avoids issues with renderers sending multiple stop states)
            if (playing) {
                playing = false;

                // playerStopped is true if stop came from openHAB. This allows us to identify if we played to the
                // end of an entry, because STOP would come from the player and not from openHAB. We should then
                // move to the next entry if the queue is not at the end already.
                if (!playerStopped) {
                    if (Instant.now().toEpochMilli() >= expectedTrackend) {
                        // If we are receiving track duration info, we know when the track is expected to end. If we
                        // received STOP before track end, and it is not coming from openHAB, it must have been stopped
                        // from the renderer directly, and we do not want to play the next entry.
                        if (playingQueue) {
                            serveNext();
                        }
                    }
                } else if (playingQueue) {
                    playingQueue = false;
                }
            }
        } else if ("PLAYING".equals(value)) {
            if (playingNotification) {
                return;
            }

            playerStopped = false;
            playing = true;
            registeredQueue = false; // reset queue registration flag as we are playing something
            updateState(CONTROL, PlayPauseType.PLAY);
            scheduleTrackPositionRefresh();
        } else if ("PAUSED_PLAYBACK".equals(value)) {
            cancelCheckPaused();
            updateState(CONTROL, PlayPauseType.PAUSE);
        } else if ("NO_MEDIA_PRESENT".equals(value)) {
            updateState(CONTROL, UnDefType.UNDEF);
        }
    }

    private void onValueReceivedCurrentURI(@Nullable String value) {
        CompletableFuture<Boolean> settingURI = isSettingURI;
        if (settingURI != null) {
            settingURI.complete(true); // We have received current URI, so can allow play to start
        }

        UpnpEntry current = currentEntry;
        UpnpEntry next = nextEntry;

        String uri = "";
        String currentUri = "";
        String nextUri = "";
        if (value != null) {
            uri = URLDecoder.decode(value.trim(), StandardCharsets.UTF_8);
        }
        if (current != null) {
            currentUri = URLDecoder.decode(current.getRes().trim(), StandardCharsets.UTF_8);
        }
        if (next != null) {
            nextUri = URLDecoder.decode(next.getRes(), StandardCharsets.UTF_8);
        }

        if (playingNotification && uri.equals(notificationUri)) {
            // No need to update anything more if this is for playing a notification
            return;
        }

        nowPlayingUri = uri;
        updateState(URI, StringType.valueOf(uri));

        logger.trace("Renderer {} received URI: {}", thing.getLabel(), uri);
        logger.trace("Renderer {} current URI: {}, equal to received URI {}", thing.getLabel(), currentUri,
                uri.equals(currentUri));
        logger.trace("Renderer {} next URI: {}", thing.getLabel(), nextUri);

        if (!uri.equals(currentUri)) {
            if ((next != null) && uri.equals(nextUri)) {
                // Renderer advanced to next entry independent of openHAB UPnP control point.
                // Advance in the queue to keep proper position status.
                // Make the next entry available to renderers that support it.
                logger.trace("Renderer {} moved from '{}' to next entry '{}' in queue", thing.getLabel(), current,
                        next);
                currentEntry = currentQueue.next();
                nextEntry = currentQueue.get(currentQueue.nextIndex());
                logger.trace("Renderer {} auto move forward, current queue index: {}", thing.getLabel(),
                        currentQueue.index());

                updateMetaDataState(next);

                // look one further to get next entry for next URI
                next = nextEntry;
                if ((next != null) && !onlyplayone) {
                    setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
                }
            } else {
                // A new entry is being served that does not match the next entry in the queue. This can be because a
                // sound or stream is being played through an action, or another control point started a new entry.
                // We should clear the metadata in this case and wait for new metadata to arrive.
                clearMetaDataState();
            }
        }
    }

    private void onValueReceivedCurrentMetaData(@Nullable String value) {
        if (playingNotification) {
            // Don't update metadata when playing notification
            return;
        }

        if (value != null && !value.isEmpty()) {
            List<UpnpEntry> list = UpnpXMLParser.getEntriesFromXML(value);
            if (!list.isEmpty()) {
                updateMetaDataState(list.get(0));
                return;
            }
        }
        clearMetaDataState();
    }

    private void onValueReceivedNextMetaData(@Nullable String value) {
        if (value != null && !value.isEmpty() && !"NOT_IMPLEMENTED".equals(value)) {
            List<UpnpEntry> list = UpnpXMLParser.getEntriesFromXML(value);
            if (!list.isEmpty()) {
                nextEntry = list.get(0);
            }
        }
    }

    private void onValueReceivedDuration(@Nullable String value) {
        // track duration and track position have format H+:MM:SS[.F+] or H+:MM:SS[.F0/F1]. We are not
        // interested in the fractional seconds, so drop everything after . and calculate in seconds.
        if (value == null || "NOT_IMPLEMENTED".equals(value)) {
            trackDuration = 0;
            updateState(TRACK_DURATION, UnDefType.UNDEF);
            updateState(REL_TRACK_POSITION, UnDefType.UNDEF);
        } else {
            try {
                trackDuration = Arrays.stream(value.split("\\.")[0].split(":")).mapToInt(n -> Integer.parseInt(n))
                        .reduce(0, (n, m) -> n * 60 + m);
                updateState(TRACK_DURATION, new QuantityType<>(trackDuration, Units.SECOND));
            } catch (NumberFormatException e) {
                logger.debug("Illegal format for track duration {}", value);
                return;
            }
        }
        setExpectedTrackend();
    }

    private void onValueReceivedRelTime(@Nullable String value) {
        if (value == null || "NOT_IMPLEMENTED".equals(value)) {
            trackPosition = 0;
            updateState(TRACK_POSITION, UnDefType.UNDEF);
            updateState(REL_TRACK_POSITION, UnDefType.UNDEF);
        } else {
            try {
                trackPosition = Arrays.stream(value.split("\\.")[0].split(":")).mapToInt(n -> Integer.parseInt(n))
                        .reduce(0, (n, m) -> n * 60 + m);
                updateState(TRACK_POSITION, new QuantityType<>(trackPosition, Units.SECOND));
                int relPosition = (trackDuration != 0) ? trackPosition * 100 / trackDuration : 0;
                updateState(REL_TRACK_POSITION, new PercentType(relPosition));
            } catch (NumberFormatException e) {
                logger.trace("Illegal format for track position {}", value);
                return;
            }
        }

        if (playingNotification) {
            posAtNotificationStart = trackPosition;
        }

        setExpectedTrackend();
    }

    @Override
    protected void updateProtocolInfo(String value) {
        sink.clear();
        supportedAudioFormats.clear();
        audioSupport = false;

        sink.addAll(Arrays.asList(value.split(",")));

        for (String protocol : sink) {
            Matcher matcher = PROTOCOL_PATTERN.matcher(protocol);
            if (matcher.find()) {
                String format = matcher.group(1);
                switch (format) {
                    case "audio/mpeg3":
                    case "audio/mp3":
                    case "audio/mpeg":
                        supportedAudioFormats.add(AudioFormat.MP3);
                        break;
                    case "audio/wav":
                    case "audio/wave":
                        supportedAudioFormats.add(AudioFormat.WAV);
                        break;
                }
                audioSupport = audioSupport || Pattern.matches("audio.*", format);
            }
        }

        if (audioSupport) {
            logger.debug("Renderer {} supports audio", thing.getLabel());
            registerAudioSink();
        }
    }

    private void clearCurrentEntry() {
        clearMetaDataState();

        trackDuration = 0;
        updateState(TRACK_DURATION, UnDefType.UNDEF);
        trackPosition = 0;
        updateState(TRACK_POSITION, UnDefType.UNDEF);
        updateState(REL_TRACK_POSITION, UnDefType.UNDEF);

        currentEntry = null;
    }

    /**
     * Register a new queue with media entries to the renderer. Set the next position at the first entry in the list.
     * If the renderer is currently playing, set the first entry in the list as the next media. If not playing, set it
     * as current media.
     *
     * @param queue
     */
    protected void registerQueue(UpnpEntryQueue queue) {
        if (currentQueue.equals(queue)) {
            // We get the same queue, so do nothing
            return;
        }

        logger.debug("Registering queue on renderer {}", thing.getLabel());

        registeredQueue = true;
        currentQueue = queue;
        currentQueue.setRepeat(repeat);
        currentQueue.setShuffle(shuffle);
        if (playingQueue) {
            nextEntry = currentQueue.get(currentQueue.nextIndex());
            UpnpEntry next = nextEntry;
            if ((next != null) && !onlyplayone) {
                // make the next entry available to renderers that support it
                logger.trace("Renderer {} still playing, set new queue as next entry", thing.getLabel());
                setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
            }
        } else {
            resetToStartQueue();
        }
    }

    /**
     * Move to next position in queue and start playing.
     */
    private void serveNext() {
        if (currentQueue.hasNext()) {
            currentEntry = currentQueue.next();
            nextEntry = currentQueue.get(currentQueue.nextIndex());
            logger.debug("Serve next media '{}' from queue on renderer {}", currentEntry, thing.getLabel());
            logger.trace("Serve next, current queue index: {}", currentQueue.index());

            serve();
        } else {
            logger.debug("Cannot serve next, end of queue on renderer {}", thing.getLabel());
            resetToStartQueue();
        }
    }

    /**
     * Move to previous position in queue and start playing.
     */
    private void servePrevious() {
        if (currentQueue.hasPrevious()) {
            currentEntry = currentQueue.previous();
            nextEntry = currentQueue.get(currentQueue.nextIndex());
            logger.debug("Serve previous media '{}' from queue on renderer {}", currentEntry, thing.getLabel());
            logger.trace("Serve previous, current queue index: {}", currentQueue.index());

            serve();
        } else {
            logger.debug("Cannot serve previous, already at start of queue on renderer {}", thing.getLabel());
            resetToStartQueue();
        }
    }

    private void resetToStartQueue() {
        logger.trace("Reset to start queue on renderer {}", thing.getLabel());

        playingQueue = false;
        registeredQueue = true;

        stop();

        currentQueue.resetIndex(); // reset to beginning of queue
        currentEntry = currentQueue.next();
        nextEntry = currentQueue.get(currentQueue.nextIndex());
        logger.trace("Reset queue, current queue index: {}", currentQueue.index());
        UpnpEntry current = currentEntry;
        if (current != null) {
            clearMetaDataState();
            updateMetaDataState(current);
            setCurrentURI(current.getRes(), UpnpXMLParser.compileMetadataString(current));
        } else {
            clearCurrentEntry();
        }

        UpnpEntry next = nextEntry;
        if (onlyplayone) {
            setNextURI("", "");
        } else if (next != null) {
            setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
        }
    }

    /**
     * Serve media from a queue and play immediately when already playing.
     *
     * @param media
     */
    private void serve() {
        logger.trace("Serve media on renderer {}", thing.getLabel());

        UpnpEntry entry = currentEntry;
        if (entry != null) {
            clearMetaDataState();
            String res = entry.getRes();
            if (res.isEmpty()) {
                logger.debug("Renderer {} cannot serve media '{}', no URI", thing.getLabel(), currentEntry);
                playingQueue = false;
                return;
            }
            updateMetaDataState(entry);
            setCurrentURI(res, UpnpXMLParser.compileMetadataString(entry));

            if ((playingQueue || playing) && !(onlyplayone && oneplayed)) {
                logger.trace("Ready to play '{}' from queue", currentEntry);

                trackDuration = 0;
                trackPosition = 0;
                expectedTrackend = 0;
                play();

                oneplayed = true;
                playingQueue = true;
            }

            // make the next entry available to renderers that support it
            if (!onlyplayone) {
                UpnpEntry next = nextEntry;
                if (next != null) {
                    setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
                }
            }
        }
    }

    /**
     * Called before handling a pause CONTROL command. If we do not received PAUSED_PLAYBACK or STOPPED back within
     * timeout, we will revert to playing state. This takes care of renderers that cannot pause playback.
     */
    private void checkPaused() {
        paused = upnpScheduler.schedule(this::resetPaused, config.responseTimeout, TimeUnit.MILLISECONDS);
    }

    private void resetPaused() {
        updateState(CONTROL, PlayPauseType.PLAY);
    }

    private void cancelCheckPaused() {
        ScheduledFuture<?> future = paused;
        if (future != null) {
            future.cancel(true);
            paused = null;
        }
    }

    private void setExpectedTrackend() {
        expectedTrackend = Instant.now().toEpochMilli() + (trackDuration - trackPosition) * 1000
                - config.responseTimeout;
    }

    /**
     * Update the current track position every second if the channel is linked.
     */
    private void scheduleTrackPositionRefresh() {
        if (playingNotification) {
            return;
        }

        cancelTrackPositionRefresh();
        if (!(isLinked(TRACK_POSITION) || isLinked(REL_TRACK_POSITION))) {
            // only get it once, so we can use the track end to correctly identify STOP pressed directly on renderer
            getPositionInfo();
        } else {
            if (trackPositionRefresh == null) {
                trackPositionRefresh = upnpScheduler.scheduleWithFixedDelay(this::getPositionInfo, 1, 1,
                        TimeUnit.SECONDS);
            }
        }
    }

    private void cancelTrackPositionRefresh() {
        ScheduledFuture<?> refresh = trackPositionRefresh;

        if (refresh != null) {
            refresh.cancel(true);
        }
        trackPositionRefresh = null;

        trackPosition = 0;
        updateState(TRACK_POSITION, new QuantityType<>(trackPosition, Units.SECOND));
        int relPosition = (trackDuration != 0) ? trackPosition / trackDuration : 0;
        updateState(REL_TRACK_POSITION, new PercentType(relPosition));
    }

    /**
     * Update metadata channels for media with data received from the Media Server or AV Transport.
     *
     * @param media
     */
    private void updateMetaDataState(UpnpEntry media) {
        // We don't want to update metadata if the metadata from the AVTransport is less complete than in the current
        // entry.
        boolean isCurrent = false;
        UpnpEntry entry = null;
        if (playingQueue) {
            entry = currentEntry;
        }

        logger.trace("Renderer {}, received media ID: {}", thing.getLabel(), media.getId());

        if ((entry != null) && entry.getId().equals(media.getId())) {
            logger.trace("Current ID: {}", entry.getId());

            isCurrent = true;
        } else {
            // Sometimes we receive the media URL without the ID, then compare on URL
            String mediaRes = media.getRes().trim();
            String entryRes = (entry != null) ? entry.getRes().trim() : "";

            String mediaUrl = URLDecoder.decode(mediaRes, StandardCharsets.UTF_8);
            String entryUrl = URLDecoder.decode(entryRes, StandardCharsets.UTF_8);
            isCurrent = mediaUrl.equals(entryUrl);

            logger.trace("Current queue res: {}", entryRes);
            logger.trace("Updated media res: {}", mediaRes);
        }

        logger.trace("Received meta data is for current entry: {}", isCurrent);

        if (!(isCurrent && media.getTitle().isEmpty())) {
            updateState(TITLE, StringType.valueOf(media.getTitle()));
        }
        if (!(isCurrent && (media.getAlbum().isEmpty() || media.getAlbum().matches("Unknown.*")))) {
            updateState(ALBUM, StringType.valueOf(media.getAlbum()));
        }
        if (!(isCurrent
                && (media.getAlbumArtUri().isEmpty() || media.getAlbumArtUri().contains("DefaultAlbumCover")))) {
            if (media.getAlbumArtUri().isBlank() || media.getAlbumArtUri().contains("DefaultAlbumCover")) {
                updateState(ALBUM_ART, UnDefType.UNDEF);
            } else {
                State albumArt = null;
                try {
                    albumArt = HttpUtil.downloadImage(media.getAlbumArtUri().trim());
                } catch (IllegalArgumentException e) {
                    logger.debug("Invalid album art URI: {}", media.getAlbumArtUri(), e);
                }
                if (albumArt == null) {
                    logger.debug("Failed to download the content of album art from URL {}", media.getAlbumArtUri());
                    if (!isCurrent) {
                        updateState(ALBUM_ART, UnDefType.UNDEF);
                    }
                } else {
                    updateState(ALBUM_ART, albumArt);
                }
            }
        }
        if (!(isCurrent && (media.getCreator().isEmpty() || media.getCreator().matches("Unknown.*")))) {
            updateState(CREATOR, StringType.valueOf(media.getCreator()));
        }
        if (!(isCurrent && (media.getArtist().isEmpty() || media.getArtist().matches("Unknown.*")))) {
            updateState(ARTIST, StringType.valueOf(media.getArtist()));
        }
        if (!(isCurrent && (media.getPublisher().isEmpty() || media.getPublisher().matches("Unknown.*")))) {
            updateState(PUBLISHER, StringType.valueOf(media.getPublisher()));
        }
        if (!(isCurrent && (media.getGenre().isEmpty() || media.getGenre().matches("Unknown.*")))) {
            updateState(GENRE, StringType.valueOf(media.getGenre()));
        }
        if (!(isCurrent && (media.getOriginalTrackNumber() == null))) {
            Integer trackNumber = media.getOriginalTrackNumber();
            State trackNumberState = (trackNumber != null) ? new DecimalType(trackNumber) : UnDefType.UNDEF;
            updateState(TRACK_NUMBER, trackNumberState);
        }
    }

    private void clearMetaDataState() {
        updateState(TITLE, UnDefType.UNDEF);
        updateState(ALBUM, UnDefType.UNDEF);
        updateState(ALBUM_ART, UnDefType.UNDEF);
        updateState(CREATOR, UnDefType.UNDEF);
        updateState(ARTIST, UnDefType.UNDEF);
        updateState(PUBLISHER, UnDefType.UNDEF);
        updateState(GENRE, UnDefType.UNDEF);
        updateState(TRACK_NUMBER, UnDefType.UNDEF);
    }

    /**
     * @return Audio formats supported by the renderer.
     */
    public Set<AudioFormat> getSupportedAudioFormats() {
        return supportedAudioFormats;
    }

    private void registerAudioSink() {
        if (audioSinkRegistered) {
            logger.debug("Audio Sink already registered for renderer {}", thing.getLabel());
            return;
        } else if (!upnpIOService.isRegistered(this)) {
            logger.debug("Audio Sink registration for renderer {} failed, no service", thing.getLabel());
            return;
        }
        logger.debug("Registering Audio Sink for renderer {}", thing.getLabel());
        audioSinkReg.registerAudioSink(this);
        audioSinkRegistered = true;
    }

    /**
     * @return UPnP sink definitions supported by the renderer.
     */
    protected List<String> getSink() {
        return sink;
    }
}
