/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOParticipant;
import org.eclipse.smarthome.io.transport.upnp.UpnpIOService;
import org.openhab.binding.onkyo.OnkyoBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * * The {@link UpnpAudioSinkHandler} is a base class for ThingHandlers for devices which support UPnP playback. It
 * implements the AudioSink interface.
 * This will allow to register the derived ThingHandler to be registered as a AudioSink in the framework.
 *
 * @author Paul Frank - Initial contribution
 */
public abstract class UpnpAudioSinkHandler extends BaseThingHandler implements AudioSink, UpnpIOParticipant {

    private static final Set<AudioFormat> SUPPORTED_FORMATS = new HashSet<>();
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = new HashSet<>();

    static {
        SUPPORTED_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_FORMATS.add(AudioFormat.MP3);

        SUPPORTED_STREAMS.add(AudioStream.class);
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private AudioHTTPServer audioHTTPServer;
    private String callbackUrl;
    private UpnpIOService service;

    public UpnpAudioSinkHandler(Thing thing, UpnpIOService upnpIOService, AudioHTTPServer audioHTTPServer,
            String callbackUrl) {
        super(thing);
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
        if (upnpIOService != null) {
            this.service = upnpIOService;
        }
    }

    protected void handlePlayUri(Command command) {
        if (command != null && command instanceof StringType) {
            try {
                playMedia(command.toString());

            } catch (IllegalStateException e) {
                logger.warn("Cannot play URI ({})", e.getMessage());
            }
        }
    }

    private void playMedia(String url) {
        stop();
        removeAllTracksFromQueue();

        if (!url.startsWith("x-") && (!url.startsWith("http"))) {
            url = "x-file-cifs:" + url;
        }

        setCurrentURI(url, "");

        play();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    private void stop() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "Stop", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    private void play() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");
        inputs.put("Speed", "1");
        Map<String, String> result = service.invokeAction(this, "AVTransport", "Play", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    private void removeAllTracksFromQueue() {
        Map<String, String> inputs = new HashMap<>();
        inputs.put("InstanceID", "0");

        Map<String, String> result = service.invokeAction(this, "AVTransport", "RemoveAllTracksFromQueue", inputs);

        for (String variable : result.keySet()) {
            this.onValueReceived(variable, result.get(variable), "AVTransport");
        }
    }

    private void setCurrentURI(String uri, String uriMetaData) {
        if (uri != null && uriMetaData != null) {
            Map<String, String> inputs = new HashMap<>();

            try {
                inputs.put("InstanceID", "0");
                inputs.put("CurrentURI", uri);
                inputs.put("CurrentURIMetaData", uriMetaData);
            } catch (NumberFormatException ex) {
                logger.error("Action Invalid Value Format Exception {}", ex.getMessage());
            }

            Map<String, String> result = service.invokeAction(this, "AVTransport", "SetAVTransportURI", inputs);

            for (String variable : result.keySet()) {
                this.onValueReceived(variable, result.get(variable), "AVTransport");
            }
        }
    }

    @Override
    public String getId() {
        return getThing().getUID().toString();
    }

    @Override
    public String getLabel(Locale locale) {
        return getThing().getLabel();
    }

    @Override
    public void process(AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        String url = null;
        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, the speaker can access it itself and play it.
            URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            url = urlAudioStream.getURL();
        } else {
            if (callbackUrl != null) {
                String relativeUrl;
                if (audioStream instanceof FixedLengthAudioStream) {
                    // we serve it on our own HTTP server
                    relativeUrl = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 20);
                } else {
                    relativeUrl = audioHTTPServer.serve(audioStream);
                }
                url = callbackUrl + relativeUrl;
            } else {
                logger.warn("We do not have any callback url, so onkyo cannot play the audio stream!");
                return;
            }
        }
        playMedia(url);
    }

    @Override
    public String getUDN() {
        return (String) this.getConfig().get(OnkyoBindingConstants.UDN_PARAMETER);
    }

    @Override
    public void onValueReceived(String variable, String value, String service) {
        logger.debug("received variable {} with value {} from service {}", variable, value, service);
    }

    @Override
    public void onServiceSubscribed(String service, boolean succeeded) {

    }

    @Override
    public void onStatusChanged(boolean status) {
    }
}
