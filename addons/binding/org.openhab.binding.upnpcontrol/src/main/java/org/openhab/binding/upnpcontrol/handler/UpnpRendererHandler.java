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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
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

    private @Nullable Boolean audioSupport;
    private boolean audioSinkRegistered;

    private UpnpAudioSinkReg audioSinkReg;

    private String channel = "Master";
    private PercentType soundVolume = new PercentType();
    private OnOffType soundMute = OnOffType.OFF;
    private String sink = "";

    private @Nullable Queue<UpnpEntry> currentQueue;

    public UpnpRendererHandler(Thing thing, UpnpIOService upnpIOService, UpnpAudioSinkReg audioSinkReg) {
        super(thing, upnpIOService);
        service.addSubscription(this, "AVTransport", 3600);

        this.audioSinkReg = audioSinkReg;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for media renderer device");

        getProtocolInfo();
        registerAudioSink();
        super.initialize();
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
        inputs.put("InstanceID", Integer.toString(instanceId));

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Stop", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void play() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));
        inputs.put("Speed", "1");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Play", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void pause() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Pause", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    protected void next() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Next", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    protected void previous() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Previous", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public void setCurrentURI(String URI, String URIMetaData) {
        Map<String, String> inputs = new HashMap<>();
        try {
            inputs.put("InstanceID", Integer.toString(instanceId));
            inputs.put("CurrentURI", URI);
            inputs.put("CurrentURIMetaData", URIMetaData);
        } catch (NumberFormatException ex) {
            logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
        }

        Map<String, String> result = service.invokeAction(this, "AVTransport", "SetAVTransportURI", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    public PercentType getVolume() throws IOException {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));
        inputs.put("Channel", channel);

        Map<String, String> result = service.invokeAction(this, "RenderingControl", "GetVolume", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "RenderingControl");
        }
        return soundVolume;
    }

    public void setVolume(PercentType volume) throws IOException {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));
        inputs.put("Channel", channel);
        inputs.put("DesiredVolume", String.valueOf(volume.intValue()));

        Map<String, String> result = service.invokeAction(this, "RenderingControl", "SetVolume", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "RenderingControl");
        }
    }

    protected OnOffType getMute() throws IOException {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));
        inputs.put("Channel", channel);

        Map<String, String> result = service.invokeAction(this, "RenderingControl", "GetMute", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "RenderingControl");
        }
        return soundMute;
    }

    protected void setMute(OnOffType mute) throws IOException {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", Integer.toString(instanceId));
        inputs.put("Channel", channel);
        inputs.put("DesiredMute", mute == OnOffType.ON ? "1" : "0");

        Map<String, String> result = service.invokeAction(this, "RenderingControl", "SetMute", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "RenderingControl");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            String transportState;
            if (command instanceof RefreshType) {
                switch (channelUID.getId()) {
                    case VOLUME:
                        updateState(channelUID, getVolume());
                        break;
                    case MUTE:
                        updateState(channelUID, getMute());
                        break;
                    case CONTROL:
                        transportState = getTransportState();
                        State newState = UnDefType.UNDEF;
                        if (transportState.equals("PLAYING")) {
                            newState = PlayPauseType.PLAY;
                        } else if (transportState.equals("STOPPED")) {
                            newState = PlayPauseType.PLAY;
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
                            updateState(CONTROL, PlayPauseType.PLAY);
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
                                next();
                            } else if (command == NextPreviousType.PREVIOUS) {
                                previous();
                            }
                        } else if (command instanceof RewindFastforwardType) {
                        }
                        break;
                }

                return;

            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Could not communicate with " + getThing().getLabel());
        }
    }

    @Override
    public void onStatusChanged(boolean status) {
        if (status) {
            registerAudioSink();
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
                }
                break;
            case "CurrentVolume":
                if (!((value == null) || (value.isEmpty()))) {
                    soundVolume = PercentType.valueOf(value);
                }
                break;
            case "Sink":
                if (!((value == null) || (value.isEmpty()))) {
                    sink = value;
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
                }
                break;
            default:
                super.onValueReceived(variable, value, service);
                break;
        }
    }

    @Override
    public String getProtocolInfo() {
        Map<String, String> inputs = new HashMap<>();

        Map<String, String> result = service.invokeAction(this, "ConnectionManager", "GetProtocolInfo", inputs);

        for (String variable : result.keySet()) {
            onValueReceived(variable, result.get(variable), "ConnectionManager");
        }

        audioSupport = Boolean.valueOf(Pattern.matches(".*audio.*", sink));

        Pattern pattern = Pattern.compile("(?:.*):(?:.*):(.*):(?:.*)");
        for (String protocol : sink.split(",")) {
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
            }
        }
        return sink;
    }

    private void registerAudioSink() {
        if (audioSinkRegistered || !service.isRegistered(this)) {
            return;
        }
        if (audioSupport()) {
            logger.debug("Registering Audio Sink for renderer {}", thing.getLabel());
            audioSinkReg.registerAudioSink(this);
            audioSinkRegistered = true;
        }
    }

    public boolean audioSupport() {
        if (audioSupport == null) {
            getProtocolInfo();
        }
        if (audioSupport != null) {
            return audioSupport;
        }
        return false;
    }

    public void registerQueue(Queue<UpnpEntry> queue) {
        currentQueue = queue;
        serveNext();
    }

    public void serveNext() {
        if (currentQueue != null) {
            UpnpEntry nextMedia = currentQueue.poll();
            if (nextMedia != null) {
                setCurrentURI(nextMedia.getRes(), "");
            }
        }
    }
}
