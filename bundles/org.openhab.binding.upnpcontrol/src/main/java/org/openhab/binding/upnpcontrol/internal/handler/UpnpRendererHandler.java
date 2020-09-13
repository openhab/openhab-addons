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
package org.openhab.binding.upnpcontrol.internal.handler;

import static org.openhab.binding.upnpcontrol.internal.UpnpControlBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
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

    private static final int SUBSCRIPTION_DURATION_SECONDS = 3600;

    // UPnP protocol pattern
    private static final Pattern PROTOCOL_PATTERN = Pattern.compile("(?:.*):(?:.*):(.*):(?:.*)");

    private volatile boolean audioSupport;
    protected volatile Set<AudioFormat> supportedAudioFormats = new HashSet<>();
    private volatile boolean audioSinkRegistered;

    private volatile UpnpAudioSinkReg audioSinkReg;

    private volatile boolean upnpSubscribed;

    private static final String UPNP_CHANNEL = "Master";

    private volatile OnOffType soundMute = OnOffType.OFF;
    private volatile PercentType soundVolume = new PercentType();
    private volatile List<String> sink = new ArrayList<>();

    private volatile ArrayList<UpnpEntry> currentQueue = new ArrayList<>();
    private volatile UpnpIterator<UpnpEntry> queueIterator = new UpnpIterator<>(currentQueue.listIterator());
    private volatile @Nullable UpnpEntry currentEntry = null;
    private volatile @Nullable UpnpEntry nextEntry = null;
    private volatile boolean playerStopped;
    private volatile boolean playing;
    private volatile @Nullable CompletableFuture<Boolean> isSettingURI;
    private volatile int trackDuration = 0;
    private volatile int trackPosition = 0;
    private volatile @Nullable ScheduledFuture<?> trackPositionRefresh;

    private volatile @Nullable ScheduledFuture<?> subscriptionRefreshJob;
    private final Runnable subscriptionRefresh = () -> {
        removeSubscription("AVTransport");
        addSubscription("AVTransport", SUBSCRIPTION_DURATION_SECONDS);
    };

    /**
     * The {@link ListIterator} class does not keep a cursor position and therefore will not give the previous element
     * when next was called before, or give the next element when previous was called before. This iterator will always
     * go to previous/next.
     */
    private static class UpnpIterator<T> {
        private final ListIterator<T> listIterator;

        private boolean nextWasCalled = false;
        private boolean previousWasCalled = false;

        public UpnpIterator(ListIterator<T> listIterator) {
            this.listIterator = listIterator;
        }

        public T next() {
            if (previousWasCalled) {
                previousWasCalled = false;
                listIterator.next();
            }
            nextWasCalled = true;
            return listIterator.next();
        }

        public T previous() {
            if (nextWasCalled) {
                nextWasCalled = false;
                listIterator.previous();
            }
            previousWasCalled = true;
            return listIterator.previous();
        }

        public boolean hasNext() {
            if (previousWasCalled) {
                return true;
            } else {
                return listIterator.hasNext();
            }
        }

        public boolean hasPrevious() {
            if (previousIndex() < 0) {
                return false;
            } else if (nextWasCalled) {
                return true;
            } else {
                return listIterator.hasPrevious();
            }
        }

        public int nextIndex() {
            if (previousWasCalled) {
                return listIterator.nextIndex() + 1;
            } else {
                return listIterator.nextIndex();
            }
        }

        public int previousIndex() {
            if (nextWasCalled) {
                return listIterator.previousIndex() - 1;
            } else {
                return listIterator.previousIndex();
            }
        }
    }

    public UpnpRendererHandler(Thing thing, UpnpIOService upnpIOService, UpnpAudioSinkReg audioSinkReg) {
        super(thing, upnpIOService);

        this.audioSinkReg = audioSinkReg;
    }

    @Override
    public void initialize() {
        super.initialize();

        logger.debug("Initializing handler for media renderer device {}", thing.getLabel());

        if (config.udn != null) {
            if (service.isRegistered(this)) {
                initRenderer();
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication cannot be established with " + thing.getLabel());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No UDN configured for " + thing.getLabel());
        }
    }

    @Override
    public void dispose() {
        cancelSubscriptionRefreshJob();
        removeSubscription("AVTransport");

        cancelTrackPositionRefresh();

        super.dispose();
    }

    private void cancelSubscriptionRefreshJob() {
        ScheduledFuture<?> refreshJob = subscriptionRefreshJob;

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        subscriptionRefreshJob = null;

        upnpSubscribed = false;
    }

    private void initRenderer() {
        if (!upnpSubscribed) {
            addSubscription("AVTransport", SUBSCRIPTION_DURATION_SECONDS);
            upnpSubscribed = true;

            subscriptionRefreshJob = scheduler.scheduleWithFixedDelay(subscriptionRefresh,
                    SUBSCRIPTION_DURATION_SECONDS / 2, SUBSCRIPTION_DURATION_SECONDS / 2, TimeUnit.SECONDS);
        }
        getProtocolInfo();
        getTransportState();

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Invoke Stop on UPnP AV Transport.
     */
    public void stop() {
        Map<String, String> inputs = Collections.singletonMap("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Stop", inputs);
    }

    /**
     * Invoke Play on UPnP AV Transport.
     */
    public void play() {
        CompletableFuture<Boolean> setting = isSettingURI;
        try {
            if ((setting == null) || (setting.get(2500, TimeUnit.MILLISECONDS))) {
                // wait for maximum 2.5s until the media URI is set before playing
                Map<String, String> inputs = new HashMap<>();
                inputs.put("InstanceID", Integer.toString(avTransportId));
                inputs.put("Speed", "1");

                invokeAction("AVTransport", "Play", inputs);
            } else {
                logger.debug("Cannot play, cancelled setting URI in the renderer");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Cannot play, media URI not yet set in the renderer");
        }
    }

    /**
     * Invoke Pause on UPnP AV Transport.
     */
    public void pause() {
        Map<String, String> inputs = Collections.singletonMap("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Pause", inputs);
    }

    /**
     * Invoke Next on UPnP AV Transport.
     */
    protected void next() {
        Map<String, String> inputs = Collections.singletonMap("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Next", inputs);
    }

    /**
     * Invoke Previous on UPnP AV Transport.
     */
    protected void previous() {
        Map<String, String> inputs = Collections.singletonMap("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Previous", inputs);
    }

    /**
     * Invoke SetAVTransportURI on UPnP AV Transport.
     *
     * @param URI
     * @param URIMetaData
     */
    public void setCurrentURI(String URI, String URIMetaData) {
        CompletableFuture<Boolean> setting = isSettingURI;
        if (setting != null) {
            setting.complete(false);
        }
        isSettingURI = new CompletableFuture<Boolean>(); // set this so we don't start playing when not finished setting
                                                         // URI
        Map<String, String> inputs = new HashMap<>();
        try {
            inputs.put("InstanceID", Integer.toString(avTransportId));
            inputs.put("CurrentURI", URI);
            inputs.put("CurrentURIMetaData", URIMetaData);

            invokeAction("AVTransport", "SetAVTransportURI", inputs);
        } catch (NumberFormatException ex) {
            logger.debug("Action Invalid Value Format Exception {}", ex.getMessage());
        }
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

            invokeAction("AVTransport", "SetNextAVTransportURI", inputs);
        } catch (NumberFormatException ex) {
            logger.debug("Action Invalid Value Format Exception {}", ex.getMessage());
        }
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
        Map<String, String> inputs = Collections.singletonMap("InstanceID", Integer.toString(rcsId));

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
                    if ("PLAYING".equals(transportState)) {
                        newState = PlayPauseType.PLAY;
                    } else if ("STOPPED".equals(transportState)) {
                        newState = PlayPauseType.PAUSE;
                    } else if ("PAUSED_PLAYBACK".equals(transportState)) {
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
                        updateState(TRACK_POSITION, new QuantityType<>(0, SmartHomeUnits.SECOND));
                    }
                    break;
                case CONTROL:
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
            cancelSubscriptionRefreshJob();

            updateState(CONTROL, PlayPauseType.PAUSE);
            cancelTrackPositionRefresh();

            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Communication lost with " + thing.getLabel());
        }
        super.onStatusChanged(status);
    }

    @Override
    public void onValueReceived(@Nullable String variable, @Nullable String value, @Nullable String service) {
        if (logger.isTraceEnabled()) {
            logger.trace("Upnp device {} received variable {} with value {} from service {}", thing.getLabel(),
                    variable, value, service);
        } else {
            if (logger.isDebugEnabled() && !("AbsTime".equals(variable) || "RelCount".equals(variable)
                    || "RelTime".equals(variable) || "AbsCount".equals(variable) || "Track".equals(variable)
                    || "TrackDuration".equals(variable))) {
                // don't log all variables received when updating the track position every second
                logger.debug("Upnp device {} received variable {} with value {} from service {}", thing.getLabel(),
                        variable, value, service);
            }
        }
        if (variable == null) {
            return;
        }

        switch (variable) {
            case "CurrentMute":
                if (!((value == null) || (value.isEmpty()))) {
                    soundMute = OnOffType.from(Boolean.parseBoolean(value));
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
                    if ("AVTransport".equals(service)) {
                        Map<String, String> parsedValues = UpnpXMLParser.getAVTransportFromXML(value);
                        for (Map.Entry<String, String> entrySet : parsedValues.entrySet()) {
                            // Update the transport state after the update of the media information
                            // to not break the notification mechanism
                            if (!"TransportState".equals(entrySet.getKey())) {
                                onValueReceived(entrySet.getKey(), entrySet.getValue(), service);
                            }
                            if ("AVTransportURI".equals(entrySet.getKey())) {
                                onValueReceived("CurrentTrackURI", entrySet.getValue(), service);
                            } else if ("AVTransportURIMetaData".equals(entrySet.getKey())) {
                                onValueReceived("CurrentTrackMetaData", entrySet.getValue(), service);
                            }
                        }
                        if (parsedValues.containsKey("TransportState")) {
                            onValueReceived("TransportState", parsedValues.get("TransportState"), service);
                        }
                    }
                }
                break;
            case "TransportState":
                transportState = (value == null) ? "" : value;
                if ("STOPPED".equals(value)) {
                    updateState(CONTROL, PlayPauseType.PAUSE);
                    cancelTrackPositionRefresh();
                    // playerStopped is true if stop came from openHAB. This allows us to identify if we played to the
                    // end of an entry. We should then move to the next entry if the queue is not at the end already.
                    if (playing && !playerStopped) {
                        // Only go to next for first STOP command, then wait until we received PLAYING before moving
                        // to next (avoids issues with renderers sending multiple stop states)
                        playing = false;
                        serveNext();
                    } else {
                        currentEntry = nextEntry; // Try to get the metadata for the next entry if controlled by an
                                                  // external control point
                        playing = false;
                    }
                } else if ("PLAYING".equals(value)) {
                    playerStopped = false;
                    playing = true;
                    updateState(CONTROL, PlayPauseType.PLAY);
                    scheduleTrackPositionRefresh();
                } else if ("PAUSED_PLAYBACK".contentEquals(value)) {
                    updateState(CONTROL, PlayPauseType.PAUSE);
                }
                break;
            case "CurrentTrackURI":
                UpnpEntry current = currentEntry;
                if (queueIterator.hasNext() && (current != null) && !current.getRes().equals(value)
                        && currentQueue.get(queueIterator.nextIndex()).getRes().equals(value)) {
                    // Renderer advanced to next entry independent of openHAB UPnP control point.
                    // Advance in the queue to keep proper position status.
                    // Make the next entry available to renderers that support it.
                    updateMetaDataState(currentQueue.get(queueIterator.nextIndex()));
                    logger.trace("Renderer moved from '{}' to next entry '{}' in queue", currentEntry,
                            currentQueue.get(queueIterator.nextIndex()));
                    currentEntry = queueIterator.next();
                    if (queueIterator.hasNext()) {
                        UpnpEntry next = currentQueue.get(queueIterator.nextIndex());
                        setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
                    }
                }
                if (isSettingURI != null) {
                    isSettingURI.complete(true); // We have received current URI, so can allow play to start
                }
                break;
            case "CurrentTrackMetaData":
                if (!((value == null) || (value.isEmpty()))) {
                    List<UpnpEntry> list = UpnpXMLParser.getEntriesFromXML(value);
                    if (!list.isEmpty()) {
                        updateMetaDataState(list.get(0));
                    }
                }
                break;
            case "NextAVTransportURIMetaData":
                if (!((value == null) || (value.isEmpty() || "NOT_IMPLEMENTED".equals(value)))) {
                    List<UpnpEntry> list = UpnpXMLParser.getEntriesFromXML(value);
                    if (!list.isEmpty()) {
                        nextEntry = list.get(0);
                    }
                }
                break;
            case "CurrentTrackDuration":
            case "TrackDuration":
                // track duration and track position have format H+:MM:SS[.F+] or H+:MM:SS[.F0/F1]. We are not
                // interested in the fractional seconds, so drop everything after . and calculate in seconds.
                if ((value == null) || ("NOT_IMPLEMENTED".equals(value))) {
                    trackDuration = 0;
                    updateState(TRACK_DURATION, UnDefType.UNDEF);
                } else {
                    trackDuration = Arrays.stream(value.split("\\.")[0].split(":")).mapToInt(n -> Integer.parseInt(n))
                            .reduce(0, (n, m) -> n * 60 + m);
                    updateState(TRACK_DURATION, new QuantityType<>(trackDuration, SmartHomeUnits.SECOND));
                }
                break;
            case "RelTime":
                if ((value == null) || ("NOT_IMPLEMENTED".equals(value))) {
                    trackPosition = 0;
                    updateState(TRACK_POSITION, UnDefType.UNDEF);
                } else {
                    trackPosition = Arrays.stream(value.split("\\.")[0].split(":")).mapToInt(n -> Integer.parseInt(n))
                            .reduce(0, (n, m) -> n * 60 + m);
                    updateState(TRACK_POSITION, new QuantityType<>(trackPosition, SmartHomeUnits.SECOND));
                }
                break;
            default:
                super.onValueReceived(variable, value, service);
                break;
        }
    }

    private void updateProtocolInfo(String value) {
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

    private void clearCurrentEntry() {
        updateState(TITLE, UnDefType.UNDEF);
        updateState(ALBUM, UnDefType.UNDEF);
        updateState(ALBUM_ART, UnDefType.UNDEF);
        updateState(CREATOR, UnDefType.UNDEF);
        updateState(ARTIST, UnDefType.UNDEF);
        updateState(PUBLISHER, UnDefType.UNDEF);
        updateState(GENRE, UnDefType.UNDEF);
        updateState(TRACK_NUMBER, UnDefType.UNDEF);
        trackDuration = 0;
        updateState(TRACK_DURATION, UnDefType.UNDEF);
        trackPosition = 0;
        updateState(TRACK_POSITION, UnDefType.UNDEF);

        currentEntry = null;
    }

    /**
     * Register a new queue with media entries to the renderer. Set the next position at the first entry in the list.
     * If the renderer is currently playing, set the first entry in the list as the next media. If not playing, set it
     * as current media.
     *
     * @param queue
     */
    public void registerQueue(ArrayList<UpnpEntry> queue) {
        logger.debug("Registering queue on renderer {}", thing.getLabel());
        currentQueue = queue;
        queueIterator = new UpnpIterator<>(currentQueue.listIterator());
        if (playing) {
            if (queueIterator.hasNext()) {
                // make the next entry available to renderers that support it
                logger.trace("Still playing, set new queue as next entry");
                UpnpEntry next = currentQueue.get(queueIterator.nextIndex());
                setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
            }
        } else {
            if (queueIterator.hasNext()) {
                UpnpEntry entry = queueIterator.next();
                updateMetaDataState(entry);
                setCurrentURI(entry.getRes(), UpnpXMLParser.compileMetadataString(entry));
                currentEntry = entry;
            } else {
                clearCurrentEntry();
            }
        }
    }

    /**
     * Move to next position in queue and start playing.
     */
    private void serveNext() {
        if (queueIterator.hasNext()) {
            currentEntry = queueIterator.next();
            logger.debug("Serve next media '{}' from queue on renderer {}", currentEntry, thing.getLabel());
            serve();
        } else {
            logger.debug("Cannot serve next, end of queue on renderer {}", thing.getLabel());
            cancelTrackPositionRefresh();
            stop();
            queueIterator = new UpnpIterator<>(currentQueue.listIterator()); // reset to beginning of queue
            if (currentQueue.isEmpty()) {
                clearCurrentEntry();
            } else {
                updateMetaDataState(currentQueue.get(queueIterator.nextIndex()));
                UpnpEntry entry = queueIterator.next();
                setCurrentURI(entry.getRes(), UpnpXMLParser.compileMetadataString(entry));
                currentEntry = entry;
            }
        }
    }

    /**
     * Move to previous position in queue and start playing.
     */
    private void servePrevious() {
        if (queueIterator.hasPrevious()) {
            currentEntry = queueIterator.previous();
            logger.debug("Serve previous media '{}' from queue on renderer {}", currentEntry, thing.getLabel());
            serve();
        } else {
            logger.debug("Cannot serve previous, already at start of queue on renderer {}", thing.getLabel());
            cancelTrackPositionRefresh();
            stop();
            queueIterator = new UpnpIterator<>(currentQueue.listIterator()); // reset to beginning of queue
            if (currentQueue.isEmpty()) {
                clearCurrentEntry();
            } else {
                updateMetaDataState(currentQueue.get(queueIterator.nextIndex()));
                UpnpEntry entry = queueIterator.next();
                setCurrentURI(entry.getRes(), UpnpXMLParser.compileMetadataString(entry));
                currentEntry = entry;
            }
        }
    }

    /**
     * Play media.
     *
     * @param media
     */
    private void serve() {
        UpnpEntry entry = currentEntry;
        if (entry != null) {
            logger.trace("Ready to play '{}' from queue", currentEntry);
            updateMetaDataState(entry);
            String res = entry.getRes();
            if (res.isEmpty()) {
                logger.debug("Cannot serve media '{}', no URI", currentEntry);
                return;
            }
            setCurrentURI(res, UpnpXMLParser.compileMetadataString(entry));
            play();

            // make the next entry available to renderers that support it
            if (queueIterator.hasNext()) {
                UpnpEntry next = currentQueue.get(queueIterator.nextIndex());
                setNextURI(next.getRes(), UpnpXMLParser.compileMetadataString(next));
            }
        }
    }

    /**
     * Update the current track position every second if the channel is linked.
     */
    private void scheduleTrackPositionRefresh() {
        cancelTrackPositionRefresh();
        if (!isLinked(TRACK_POSITION)) {
            return;
        }
        if (trackPositionRefresh == null) {
            trackPositionRefresh = scheduler.scheduleWithFixedDelay(this::getPositionInfo, 1, 1, TimeUnit.SECONDS);
        }
    }

    private void cancelTrackPositionRefresh() {
        ScheduledFuture<?> refresh = trackPositionRefresh;

        if (refresh != null) {
            refresh.cancel(true);
        }
        trackPositionRefresh = null;

        trackPosition = 0;
        updateState(TRACK_POSITION, new QuantityType<>(trackPosition, SmartHomeUnits.SECOND));
    }

    /**
     * Update metadata channels for media with data received from the Media Server or AV Transport.
     *
     * @param media
     */
    private void updateMetaDataState(UpnpEntry media) {
        // The AVTransport passes the URI resource in the ID.
        // We don't want to update metadata if the metadata from the AVTransport is empty for the current entry.
        boolean isCurrent;
        UpnpEntry entry = currentEntry;
        if (entry == null) {
            entry = new UpnpEntry(media.getId(), media.getId(), "", "object.item");
            currentEntry = entry;
            isCurrent = false;
        } else {
            isCurrent = media.getId().equals(entry.getRes());
        }
        logger.trace("Media ID: {}", media.getId());
        logger.trace("Current queue res: {}", entry.getRes());
        logger.trace("Updating current entry: {}", isCurrent);

        if (!(isCurrent && media.getTitle().isEmpty())) {
            updateState(TITLE, StringType.valueOf(media.getTitle()));
        }
        if (!(isCurrent && (media.getAlbum().isEmpty() || media.getAlbum().matches("Unknown.*")))) {
            updateState(ALBUM, StringType.valueOf(media.getAlbum()));
        }
        if (!(isCurrent
                && (media.getAlbumArtUri().isEmpty() || media.getAlbumArtUri().contains("DefaultAlbumCover")))) {
            if (media.getAlbumArtUri().isEmpty() || media.getAlbumArtUri().contains("DefaultAlbumCover")) {
                updateState(ALBUM_ART, UnDefType.UNDEF);
            } else {
                State albumArt = HttpUtil.downloadImage(media.getAlbumArtUri());
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
