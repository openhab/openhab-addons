/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.upnpcontrol.internal.UpnpAudioSink;
import org.openhab.binding.upnpcontrol.internal.UpnpAudioSinkReg;
import org.openhab.binding.upnpcontrol.internal.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.UpnpXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpRendererHandler} is responsible for handling commands sent to the UPnP Renderer. It extends
 * {@link UpnpHandler} with UPnP renderer specific logic.
 *
 * @author Mark Herwege - Initial contribution
 * @author Karel Goderis - Based on UPnP logic in Sonos binding
 */
@NonNullByDefault
public class UpnpRendererHandler extends UpnpHandler {

    private final Logger logger = LoggerFactory.getLogger(UpnpRendererHandler.class);

    private static final int SUBSCRIPTION_DURATION = 3600;

    private volatile boolean audioSupport;
    protected volatile Set<AudioFormat> supportedAudioFormats = new HashSet<AudioFormat>();
    private volatile boolean audioSinkRegistered;

    private volatile UpnpAudioSinkReg audioSinkReg;

    private volatile boolean upnpSubscribed;

    private static final String UPNP_CHANNEL = "Master";

    private volatile OnOffType soundMute = OnOffType.OFF;
    private volatile PercentType soundVolume = new PercentType();
    private volatile List<String> sink = new ArrayList<>();

    private volatile LinkedList<UpnpEntry> currentQueue = new LinkedList<>();
    private volatile int queuePosition = -1;
    private volatile boolean playerStopped;
    private volatile boolean playing;
    private volatile String trackDuration = "00:00:00";
    private volatile @Nullable ScheduledFuture<?> trackPositionRefresh;

    private volatile @Nullable ScheduledFuture<?> subscriptionRefreshJob;
    private final Runnable subscriptionRefresh = () -> {
        removeSubscription("AVTransport");
        addSubscription("AVTransport", SUBSCRIPTION_DURATION);
    };

    public UpnpRendererHandler(Thing thing, UpnpIOService upnpIOService, UpnpAudioSinkReg audioSinkReg) {
        super(thing, upnpIOService);

        this.audioSinkReg = audioSinkReg;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for media renderer device {}", thing.getLabel());

        if (service.isRegistered(this)) {
            initRenderer();
        }
        super.initialize();
    }

    @Override
    public void dispose() {
        if (subscriptionRefreshJob != null) {
            subscriptionRefreshJob.cancel(true);
        }
        subscriptionRefreshJob = null;
        removeSubscription("AVTransport");
        upnpSubscribed = false;

        cancelTrackPositionRefresh();

        super.dispose();
    }

    private void initRenderer() {
        if (!upnpSubscribed) {
            addSubscription("AVTransport", SUBSCRIPTION_DURATION);
            upnpSubscribed = true;

            subscriptionRefreshJob = scheduler.scheduleWithFixedDelay(subscriptionRefresh, SUBSCRIPTION_DURATION / 2,
                    SUBSCRIPTION_DURATION / 2, TimeUnit.SECONDS);
        }
        getProtocolInfo();
        getTransportState();
    }

    /**
     * Invoke Stop on UPnP AV Transport.
     */
    public void stop() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Stop", inputs);
    }

    /**
     * Invoke Play on UPnP AV Transport.
     */
    public void play() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));
        inputs.put("Speed", "1");

        invokeAction("AVTransport", "Play", inputs);
    }

    /**
     * Invoke Pause on UPnP AV Transport.
     */
    public void pause() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Pause", inputs);
    }

    /**
     * Invoke Next on UPnP AV Transport.
     */
    protected void next() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Next", inputs);
    }

    /**
     * Invoke Previous on UPnP AV Transport.
     */
    protected void previous() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Previous", inputs);
    }

    /**
     * Invoke SetAVTransportURI on UPnP AV Transport.
     *
     * @param URI
     * @param URIMetaData
     */
    public void setCurrentURI(String URI, String URIMetaData) {
        Map<String, String> inputs = new HashMap<>();
        try {
            inputs.put("InstanceID", Integer.toString(avTransportId));
            inputs.put("CurrentURI", URI);
            inputs.put("CurrentURIMetaData", URIMetaData);
        } catch (NumberFormatException ex) {
            logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
        }

        invokeAction("AVTransport", "SetAVTransportURI", inputs);
    }

    /**
     * Invoke SetNextAVTransportURI on UPnP AV Transport.
     *
     * @param nextURI
     * @param nextURIMetaData
     */
    public void setNextURI(String nextURI, String nextURIMetaData) {
        Map<String, String> inputs = new HashMap<>();
        try {
            inputs.put("InstanceID", Integer.toString(avTransportId));
            inputs.put("NextURI", nextURI);
            inputs.put("NextURIMetaData", nextURIMetaData);
        } catch (NumberFormatException ex) {
            logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
        }

        invokeAction("AVTransport", "SetNextAVTransportURI", inputs);
    }

    /**
     * Retrieves the current audio channel ('Master' by default).
     *
     * @return current audio channel
     */
    public String getCurrentChannel() {
        return UPNP_CHANNEL;
    }

    /**
     * Retrieves the current volume known to the control point, gets updated by GENA events or after UPnP Rendering
     * Control GetVolume call. This method is used to retrieve volume by {@link UpnpAudioSink.getVolume}.
     *
     * @return current volume
     */
    public PercentType getCurrentVolume() {
        return soundVolume;
    }

    /**
     * Invoke GetVolume on UPnP Rendering Control.
     * Result is received in {@link onValueReceived}.
     *
     * @param channel
     */
    protected void getVolume(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", channel);

        invokeAction("RenderingControl", "GetVolume", inputs);
    }

    /**
     * Invoke SetVolume on UPnP Rendering Control.
     *
     * @param channel
     * @param volume
     */
    public void setVolume(String channel, PercentType volume) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", channel);
        inputs.put("DesiredVolume", String.valueOf(volume.intValue()));

        invokeAction("RenderingControl", "SetVolume", inputs);
    }

    /**
     * Invoke getMute on UPnP Rendering Control.
     * Result is received in {@link onValueReceived}.
     *
     * @param channel
     */
    protected void getMute(String channel) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", channel);

        invokeAction("RenderingControl", "GetMute", inputs);
    }

    /**
     * Invoke SetMute on UPnP Rendering Control.
     *
     * @param channel
     * @param mute
     */
    protected void setMute(String channel, OnOffType mute) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", channel);
        inputs.put("DesiredMute", mute == OnOffType.ON ? "1" : "0");

        invokeAction("RenderingControl", "SetMute", inputs);
    }

    /**
     * Invoke getPositionInfo on UPnP Rendering Control.
     * Result is received in {@link onValueReceived}.
     */
    protected void getPositionInfo() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));

        invokeAction("AVTransport", "GetPositionInfo", inputs);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {} on renderer {}", command, channelUID, thing.getLabel());

        String transportState;
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case VOLUME:
                    getVolume(getCurrentChannel());
                    break;
                case MUTE:
                    getMute(getCurrentChannel());
                    break;
                case CONTROL:
                    transportState = this.transportState;
                    State newState = UnDefType.UNDEF;
                    if (transportState.equals("PLAYING")) {
                        newState = PlayPauseType.PLAY;
                    } else if (transportState.equals("STOPPED")) {
                        newState = PlayPauseType.PAUSE;
                    } else if (transportState.equals("PAUSED_PLAYBACK")) {
                        newState = PlayPauseType.PAUSE;
                    }
                    updateState(channelUID, newState);
                    break;
            }
            return;
        } else {
            switch (channelUID.getId()) {
                case VOLUME:
                    setVolume(getCurrentChannel(), (PercentType) command);
                    break;
                case MUTE:
                    setMute(getCurrentChannel(), (OnOffType) command);
                    break;
                case STOP:
                    if (command == OnOffType.ON) {
                        updateState(CONTROL, PlayPauseType.PAUSE);
                        playerStopped = true;
                        stop();
                    }
                    break;
                case CONTROL:
                    updateState(STOP, OnOffType.OFF);
                    playerStopped = false;
                    if (command instanceof PlayPauseType) {
                        if (command == PlayPauseType.PLAY) {
                            play();
                        } else if (command == PlayPauseType.PAUSE) {
                            pause();
                        }
                    } else if (command instanceof NextPreviousType) {
                        if (command == NextPreviousType.NEXT) {
                            playerStopped = true;
                            serveNext();
                        } else if (command == NextPreviousType.PREVIOUS) {
                            playerStopped = true;
                            servePrevious();
                        }
                    } else if (command instanceof RewindFastforwardType) {
                    }
                    break;
            }

            return;
        }
    }

    @Override
    public void onStatusChanged(boolean status) {
        logger.debug("Renderer status changed to {}", status);
        if (status) {
            initRenderer();
        } else {
            if (subscriptionRefreshJob != null) {
                subscriptionRefreshJob.cancel(true);
            }
            subscriptionRefreshJob = null;
            upnpSubscribed = false;
        }
        super.onStatusChanged(status);
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        logger.debug("Upnp device {} received variable {} with value {} from service {}", thing.getLabel(), variable,
                value, service);
        if (variable == null) {
            return;
        }

        switch (variable) {
            case "CurrentMute":
                if (!((value == null) || (value.isEmpty()))) {
                    soundMute = (Boolean.valueOf(value) ? OnOffType.ON : OnOffType.OFF);
                    updateState(MUTE, soundMute);
                }
                break;
            case "CurrentVolume":
                if (!((value == null) || (value.isEmpty()))) {
                    soundVolume = PercentType.valueOf(value);
                    updateState(VOLUME, soundVolume);
                }
                break;
            case "Sink":
                if (!((value == null) || (value.isEmpty()))) {
                    updateProtocolInfo(value);
                }
                break;
            case "LastChange":
                // pre-process some variables, eg XML processing
                if (!((value == null) || value.isEmpty())) {
                    if ("AVTransport".equals(service) && "LastChange".equals(variable)) {
                        Map<String, String> parsedValues = UpnpXMLParser.getAVTransportFromXML(value);
                        for (String parsedValue : parsedValues.keySet()) {
                            // Update the transport state after the update of the media information
                            // to not break the notification mechanism
                            if (!parsedValue.equals("TransportState")) {
                                onValueReceived(parsedValue, parsedValues.get(parsedValue), service);
                            }
                            if (parsedValue.equals("AVTransportURI")) {
                                onValueReceived("CurrentTrackURI", parsedValues.get(parsedValue), service);
                            } else if (parsedValue.equals("AVTransportURIMetaData")) {
                                onValueReceived("CurrentTrackMetaData", parsedValues.get(parsedValue), service);
                            }
                        }
                        if (parsedValues.get("TransportState") != null) {
                            onValueReceived("TransportState", parsedValues.get("TransportState"), service);
                        }
                    }
                }
                break;
            case "TransportState":
                transportState = (value == null) ? "" : value;
                if ("STOPPED".equals(value)) {
                    // This allows us to identify if we played to the end of an entry. We should then move to the next
                    // entry if the queue is not at the end already.
                    if (playerStopped) {
                        playing = false;
                        // Stop command came from openHAB, so don't move to next
                        updateState(STOP, OnOffType.ON);
                        updateState(CONTROL, PlayPauseType.PAUSE);
                        cancelTrackPositionRefresh();
                    } else if (playing) {
                        // Only go to next for first STOP command, then wait until we received PLAYING before moving
                        // to next (avoids issues with renderers sending multiple stop states)
                        playing = false;
                        serveNext();
                    }
                } else if ("PLAYING".equals(value)) {
                    playerStopped = false;
                    playing = true;
                    updateState(STOP, OnOffType.OFF);
                    updateState(CONTROL, PlayPauseType.PLAY);
                }
                break;
            case "CurrentTrackURI":
                if ((queuePosition < (currentQueue.size() - 1)
                        && !currentQueue.get(queuePosition).getRes().equals(value)
                        && currentQueue.get(queuePosition + 1).getRes().equals(value))) {
                    // Renderer advanced to next entry independent of openHAB UPnP control point.
                    // Advance in the queue to keep proper position status.
                    // Make the next entry available to renderers that support it.
                    updateMetaDataState(currentQueue.get(queuePosition + 1));
                    if (++queuePosition < (currentQueue.size() - 1)) {
                        UpnpEntry next = currentQueue.get(queuePosition + 1);
                        setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
                    }
                }
                break;
            case "CurrentTrackMetaData":
                if (!((value == null) || (value.isEmpty()))) {
                    List<UpnpEntry> list = UpnpXMLParser.getEntriesFromXML(value);
                    if (list.size() > 0) {
                        updateMetaDataState(list.get(0));
                    }
                }
                break;
            case "CurrentTrackDuration":
                updateState(TRACK_DURATION, StringType.valueOf(value));
                if (value == null) {
                    trackDuration = "00:00:00";
                } else {
                    trackDuration = value;
                }
                scheduleTrackPositionRefresh();
            case "RelTime":
                updateState(TRACK_POSITION, StringType.valueOf(value));
            default:
                super.onValueReceived(variable, value, service);
                break;
        }

    }

    private void updateProtocolInfo(String value) {
        sink.clear();
        sink.addAll(Arrays.asList(value.split(",")));

        Pattern pattern = Pattern.compile("(?:.*):(?:.*):(.*):(?:.*)");
        for (String protocol : sink) {
            Matcher matcher = pattern.matcher(protocol);
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
                audioSupport = Pattern.matches("audio.*", format);
            }
        }

        if (audioSupport) {
            logger.debug("Device {} supports audio", thing.getLabel());
            registerAudioSink();
        }
    }

    private void registerAudioSink() {
        if (audioSinkRegistered) {
            logger.debug("Audio Sink already registered for renderer {}", thing.getLabel());
            return;
        } else if (!service.isRegistered(this)) {
            logger.debug("Audio Sink registration for renderer {} failed, no service", thing.getLabel());
            return;
        }
        logger.debug("Registering Audio Sink for renderer {}", thing.getLabel());
        audioSinkReg.registerAudioSink(this);
        audioSinkRegistered = true;
    }

    /**
     * Register a new queue with media entries to the renderer. Set the position before the first entry in the list.
     * Start serving the media queue.
     *
     * @param queue
     */
    public void registerQueue(LinkedList<UpnpEntry> queue) {
        logger.debug("Registering queue on renderer {}", thing.getLabel());
        currentQueue = queue;
        queuePosition = -1;
        serveNext();
    }

    /**
     * Move to next position in queue and start playing.
     */
    private void serveNext() {
        LinkedList<UpnpEntry> queue = currentQueue;
        if (queuePosition >= (queue.size() - 1)) {
            updateState(CONTROL, PlayPauseType.PAUSE);
            stop();
            updateState(TITLE, UnDefType.UNDEF);
            updateState(ALBUM, UnDefType.UNDEF);
            updateState(ALBUM_ART, UnDefType.UNDEF);
            updateState(CREATOR, UnDefType.UNDEF);
            updateState(ARTIST, UnDefType.UNDEF);
            updateState(PUBLISHER, UnDefType.UNDEF);
            updateState(GENRE, UnDefType.UNDEF);
            updateState(TRACK_NUMBER, UnDefType.UNDEF);
            updateState(TRACK_DURATION, UnDefType.UNDEF);
            updateState(TRACK_POSITION, UnDefType.UNDEF);

            currentQueue = new LinkedList<>();
            queuePosition = -1;
            logger.debug("Cannot serve next, end of queue on renderer {}", thing.getLabel());

            cancelTrackPositionRefresh();

            return;
        }

        UpnpEntry nextMedia = queue.get(++queuePosition);
        logger.debug("Serve next media {} from queue on renderer {}", nextMedia, thing.getLabel());
        serve(nextMedia);
        // make the next entry available to renderers that support it
        if (queuePosition < (queue.size() - 1)) {
            UpnpEntry next = queue.get(queuePosition + 1);
            setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
        }
    }

    /**
     * Move to previous position in queue and start playing.
     */
    private void servePrevious() {
        LinkedList<UpnpEntry> queue = currentQueue;
        if (queue.isEmpty()) {
            logger.debug("Cannot serve previous, empty queue on renderer {}", thing.getLabel());
            cancelTrackPositionRefresh();
            return;
        }
        if (queuePosition == 0) {
            serve(queue.get(0));
            logger.debug("Cannot serve previous, already at start of queue on renderer {}", thing.getLabel());
            cancelTrackPositionRefresh();
            return;
        }

        UpnpEntry previousMedia = queue.get(--queuePosition);
        logger.debug("Serve previous media {} from queue on renderer {}", previousMedia, thing.getLabel());
        serve(previousMedia);
    }

    /**
     * Play media.
     *
     * @param media
     */
    private void serve(UpnpEntry media) {
        updateMetaDataState(media);
        String res = media.getRes();
        if (res.isEmpty()) {
            logger.debug("Cannot serve media '{}', no URI", media);
            return;
        }
        setCurrentURI(media.getRes(), UpnpXMLParser.compileMetadataString(media));
        play();
        scheduleTrackPositionRefresh();
    }

    /**
     * Update the current track position every second it the channel is linked.
     */
    private void scheduleTrackPositionRefresh() {
        if (!isLinked(TRACK_POSITION)) {
            return;
        }
        if (trackDuration.equals("00:00:00") || trackDuration.equals("NOT_IMPLEMENTED")) {
            return;
        }
        if (trackPositionRefresh == null) {
            trackPositionRefresh = scheduler.scheduleWithFixedDelay(() -> {
                getPositionInfo();
            }, 1, 1, TimeUnit.SECONDS);
        }
    }

    private void cancelTrackPositionRefresh() {
        if (trackPositionRefresh != null) {
            trackPositionRefresh.cancel(true);
        }
        trackPositionRefresh = null;
    }

    /**
     * Update metadata channels for media with data received from the Media Server or AV Transport.
     *
     * @param media
     */
    private void updateMetaDataState(UpnpEntry media) {
        // The AVTransport passes the URI resource in the ID.
        // We don't want to update metadata if the metadata from the AVTransport is empty for the current entry.
        boolean currentEntry = (currentQueue.isEmpty() || (queuePosition < 0)) ? false
                : media.getId().equals(currentQueue.get(queuePosition).getRes());
        logger.trace("Media ID: {}", media.getId());
        logger.trace("Current queue res: {}",
                (currentQueue.isEmpty() || (queuePosition < 0)) ? "" : currentQueue.get(queuePosition).getRes());
        logger.trace("Updating current entry: {}", currentEntry);

        if (!(currentEntry && media.getTitle().isEmpty())) {
            updateState(TITLE, StringType.valueOf(media.getTitle()));
        }
        if (!(currentEntry && (media.getAlbum().isEmpty() || media.getAlbum().matches("Unknown.*")))) {
            updateState(ALBUM, StringType.valueOf(media.getAlbum()));
        }
        if (!(currentEntry
                && (media.getAlbumArtUri().isEmpty() || media.getAlbumArtUri().contains("DefaultAlbumCover")))) {
            if (media.getAlbumArtUri().isEmpty() || media.getAlbumArtUri().contains("DefaultAlbumCover")) {
                updateState(ALBUM_ART, UnDefType.UNDEF);
            } else {
                State albumArt = HttpUtil.downloadImage(media.getAlbumArtUri());
                if (albumArt == null) {
                    logger.debug("Failed to download the content of album art from URL {}", media.getAlbumArtUri());
                    if (!currentEntry) {
                        updateState(ALBUM_ART, UnDefType.UNDEF);
                    }
                } else {
                    updateState(ALBUM_ART, albumArt);
                }
            }
        }
        if (!(currentEntry && (media.getCreator().isEmpty() || media.getCreator().matches("Unknown.*")))) {
            updateState(CREATOR, StringType.valueOf(media.getCreator()));
        }
        if (!(currentEntry && (media.getArtist().isEmpty() || media.getArtist().matches("Unknown.*")))) {
            updateState(ARTIST, StringType.valueOf(media.getArtist()));
        }
        if (!(currentEntry && (media.getPublisher().isEmpty() || media.getPublisher().matches("Unknown.*")))) {
            updateState(PUBLISHER, StringType.valueOf(media.getPublisher()));
        }
        if (!(currentEntry && (media.getGenre().isEmpty() || media.getGenre().matches("Unknown.*")))) {
            updateState(GENRE, StringType.valueOf(media.getGenre()));
        }
        if (!(currentEntry && (media.getOriginalTrackNumber() == null))) {
            Integer trackNumber = media.getOriginalTrackNumber();
            State trackNumberState = (trackNumber != null) ? new DecimalType(trackNumber) : UnDefType.UNDEF;
            updateState(TRACK_NUMBER, trackNumberState);
        }
    }

    /**
     * @return Audio formats supported by the renderer.
     */
    public Set<AudioFormat> getSupportedAudioFormats() {
        return supportedAudioFormats;
    }

    /**
     * @return UPnP sink definitions supported by the renderer.
     */
    protected List<String> getSink() {
        return sink;
    }
}
