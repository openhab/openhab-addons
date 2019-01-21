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
package org.openhab.io.webaudio.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an audio sink that publishes an event through SSE and temporarily serves the stream via HTTP for web players
 * to pick it up.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Christoph Weitkamp - Added getSupportedStreams() and UnsupportedAudioStreamException
 *
 */
@Component(service = AudioSink.class, immediate = true)
public class WebAudioAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(WebAudioAudioSink.class);

    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = Collections
            .unmodifiableSet(Stream.of(AudioFormat.MP3, AudioFormat.WAV).collect(Collectors.toSet()));
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = Collections
            .unmodifiableSet(Stream.of(FixedLengthAudioStream.class, URLAudioStream.class).collect(Collectors.toSet()));

    private AudioHTTPServer audioHTTPServer;

    private EventPublisher eventPublisher;

    @Override
    public void process(AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.debug("Web Audio sink does not support stopping the currently playing stream.");
            return;
        }
        logger.debug("Received audio stream of format {}", audioStream.getFormat());
        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, so we can directly pass this on.
            URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            sendEvent(urlAudioStream.getURL());
            IOUtils.closeQuietly(audioStream);
        } else if (audioStream instanceof FixedLengthAudioStream) {
            // we need to serve it for a while and make it available to multiple clients, hence only
            // FixedLengthAudioStreams are supported.
            sendEvent(audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 10).toString());
        } else {
            IOUtils.closeQuietly(audioStream);
            throw new UnsupportedAudioStreamException(
                    "Web audio sink can only handle FixedLengthAudioStreams and URLAudioStreams.",
                    audioStream.getClass());
        }
    }

    private void sendEvent(String url) {
        PlayURLEvent event = WebAudioEventFactory.createPlayURLEvent(url);
        eventPublisher.post(event);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_AUDIO_STREAMS;
    }

    @Override
    public String getId() {
        return "webaudio";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Web Audio";
    }

    @Override
    public PercentType getVolume() throws IOException {
        return PercentType.HUNDRED;
    }

    @Override
    public void setVolume(final PercentType volume) throws IOException {
        throw new IOException("Web Audio sink does not support volume level changes.");
    }

    @Reference
    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    @Reference
    protected void setAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = audioHTTPServer;
    }

    protected void unsetAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = null;
    }

}
