/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upnpcontrol.handler;

import static org.openhab.binding.upnpcontrol.UpnpControlBindingConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import org.openhab.binding.upnpcontrol.internal.UpnpAudioSinkReg;
import org.openhab.binding.upnpcontrol.internal.UpnpEntry;
import org.openhab.binding.upnpcontrol.internal.UpnpXMLParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpRendererHandler} is responsible for handling commands sent to the UPnP Renderer.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class UpnpRendererHandler extends UpnpHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SUBSCRIPTION_DURATION = 3600;

    private volatile boolean audioSupport;
    protected volatile Set<AudioFormat> supportedFormats = new HashSet<AudioFormat>();
    private volatile boolean audioSinkRegistered;

    private volatile UpnpAudioSinkReg audioSinkReg;

    private volatile boolean upnpSubscribed;

    private static final String UPNP_CHANNEL = "Master";

    private volatile OnOffType soundMute = OnOffType.OFF;
    private volatile PercentType soundVolume = new PercentType();
    private volatile List<String> sink = new ArrayList<>();

    private volatile @Nullable Queue<UpnpEntry> currentQueue;

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

    protected void handlePlayUri(Command command) {
        if ((command instanceof StringType)) {
            try {
                playMedia(command.toString());
            } catch (IllegalStateException e) {
                logger.warn("Cannot play URI ({})", e.getMessage());
            }
        }
    }

    private void playMedia(String url) {
        stop();

        String newUrl = url;
        if ((!url.startsWith("x-")) && (!url.startsWith("http"))) {
            newUrl = "x-file-cifs:" + url;
        }

        setCurrentURI(newUrl, "");

        play();
    }

    public void stop() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Stop", inputs);
    }

    public void play() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));
        inputs.put("Speed", "1");

        invokeAction("AVTransport", "Play", inputs);
    }

    public void pause() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Pause", inputs);
    }

    protected void next() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Next", inputs);
    }

    protected void previous() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(avTransportId));

        invokeAction("AVTransport", "Previous", inputs);
    }

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

    protected void getUpnpVolume() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", UPNP_CHANNEL);

        invokeAction("RenderingControl", "GetVolume", inputs);
    }

    public PercentType getVolume() {
        return soundVolume;
    }

    public void setVolume(PercentType volume) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", UPNP_CHANNEL);
        inputs.put("DesiredVolume", String.valueOf(volume.intValue()));

        invokeAction("RenderingControl", "SetVolume", inputs);
    }

    protected void getUpnpMute() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", UPNP_CHANNEL);

        invokeAction("RenderingControl", "GetMute", inputs);
    }

    protected void setMute(OnOffType mute) {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(rcsId));
        inputs.put("Channel", UPNP_CHANNEL);
        inputs.put("DesiredMute", mute == OnOffType.ON ? "1" : "0");

        invokeAction("RenderingControl", "SetMute", inputs);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} for channel {} on renderer {}", command, channelUID, thing.getLabel());

        String transportState;
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case VOLUME:
                    getUpnpVolume();
                    break;
                case MUTE:
                    getUpnpMute();
                    break;
                case CONTROL:
                    transportState = this.transportState;
                    State newState = UnDefType.UNDEF;
                    if (transportState.equals("PLAYING")) {
                        newState = PlayPauseType.PLAY;
                    } else if (transportState.equals("STOPPED")) {
                        newState = PlayPauseType.PAUSE;
                        updateState(TITLE, StringType.EMPTY);
                        updateState(ALBUM, StringType.EMPTY);
                        updateState(ALBUM_ART, UnDefType.NULL);
                        updateState(CREATOR, StringType.EMPTY);
                        updateState(TRACK_NUMBER, DecimalType.ZERO);
                        updateState(DESC, StringType.EMPTY);
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
                    setVolume((PercentType) command);
                    break;
                case MUTE:
                    setMute((OnOffType) command);
                    break;
                case STOP:
                    if (command == OnOffType.ON) {
                        updateState(CONTROL, PlayPauseType.PAUSE);
                        stop();
                    }
                    break;
                case CONTROL:
                    updateState(STOP, OnOffType.OFF);
                    if (command instanceof PlayPauseType) {
                        if (command == PlayPauseType.PLAY) {
                            play();
                        } else if (command == PlayPauseType.PAUSE) {
                            pause();
                        }
                    } else if (command instanceof NextPreviousType) {
                        if (command == NextPreviousType.NEXT) {
                            serveNext();
                        } else if (command == NextPreviousType.PREVIOUS) {
                            previous();
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
        logger.debug("Received variable {} with value {} from service {}", variable, value, service);
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
                if ("AVTransport".equals(service) && "LastChange".equals(variable)) {
                    Map<String, String> parsedValues = UpnpXMLParser.getAVTransportFromXML(value);
                    for (String parsedValue : parsedValues.keySet()) {
                        // Update the transport state after the update of the media information
                        // to not break the notification mechanism
                        if (!parsedValue.equals("TransportState")) {
                            onValueReceived(parsedValue, parsedValues.get(parsedValue), "AVTransport");
                        }
                        // Translate AVTransportURI/AVTransportURIMetaData to CurrentURI/CurrentURIMetaData
                        // for a compatibility with the result of the action GetMediaInfo
                        if (parsedValue.equals("AVTransportURI")) {
                            onValueReceived("CurrentURI", parsedValues.get(parsedValue), service);
                        } else if (parsedValue.equals("AVTransportURIMetaData")) {
                            onValueReceived("CurrentURIMetaData", parsedValues.get(parsedValue), service);
                        }
                    }
                    if (parsedValues.get("TransportState") != null) {
                        onValueReceived("TransportState", parsedValues.get("TransportState"), "AVTransport");
                    }
                }
                break;
            case "TransportState":
                if ("STOPPED".equals(value)) {
                    serveNext();
                } else if ("PLAYING".equals(value)) {
                    updateState(STOP, OnOffType.OFF);
                    updateState(CONTROL, PlayPauseType.PLAY);
                }
                break;
            case "CurrentURI":
                break;
            case "CurrentURIMetaData":
                List<UpnpEntry> list = UpnpXMLParser.getEntriesFromXML(value);
                if (list.size() > 0) {
                    UpnpEntry entry = list.get(0);
                    updateState(TITLE, StringType.valueOf(entry.getTitle()));
                    updateState(ALBUM, StringType.valueOf(entry.getAlbum()));
                    State albumArt;
                    try {
                        albumArt = HttpUtil.downloadImage(entry.getAlbumArtUri());
                    } catch (IllegalArgumentException e) {
                        albumArt = UnDefType.UNDEF;
                        logger.debug("Failed to download the content of URL {}", entry.getAlbumArtUri());
                    }
                    updateState(ALBUM_ART, albumArt);
                    updateState(CREATOR, StringType.valueOf(entry.getCreator()));
                    updateState(TRACK_NUMBER, new DecimalType(entry.getOriginalTrackNumber()));
                    updateState(DESC, StringType.valueOf(entry.getDesc()));
                }
                break;
            default:
                super.onValueReceived(variable, value, service);
                break;
        }
    }

    public void updateProtocolInfo(String value) {
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
                        supportedFormats.add(AudioFormat.MP3);
                        break;
                    case "audio/wav":
                    case "audio/wave":
                        supportedFormats.add(AudioFormat.WAV);
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

    public void registerQueue(Queue<UpnpEntry> queue) {
        logger.debug("Registering queue on renderer {}", thing.getLabel());
        currentQueue = queue;
        serveNext();
    }

    public void serveNext() {
        Queue<UpnpEntry> queue = currentQueue;
        if (queue != null) {
            UpnpEntry nextMedia = queue.poll();
            logger.debug("Serve next media {} from queue on renderer {}", nextMedia, thing.getLabel());
            if (nextMedia != null) {
                setCurrentURI(nextMedia.getRes(), "");
                updateState(TITLE, StringType.valueOf(nextMedia.getTitle()));
                updateState(ALBUM, StringType.valueOf(nextMedia.getAlbum()));
                State albumArt;
                try {
                    albumArt = HttpUtil.downloadImage(nextMedia.getAlbumArtUri());
                } catch (IllegalArgumentException e) {
                    albumArt = UnDefType.UNDEF;
                    logger.debug("Failed to download the content of URL {}", nextMedia.getAlbumArtUri());
                }
                updateState(ALBUM_ART, albumArt);
                updateState(CREATOR, StringType.valueOf(nextMedia.getCreator()));
                updateState(TRACK_NUMBER, new DecimalType(nextMedia.getOriginalTrackNumber()));
                updateState(DESC, StringType.valueOf(nextMedia.getDesc()));
            }
        } else {
            logger.debug("Queue empty on renderer {}", thing.getLabel());
        }
    }

    public Set<AudioFormat> getSupportedAudioFormats() {
        return supportedFormats;
    }

    protected List<String> getSink() {
        return sink;
    }
}
