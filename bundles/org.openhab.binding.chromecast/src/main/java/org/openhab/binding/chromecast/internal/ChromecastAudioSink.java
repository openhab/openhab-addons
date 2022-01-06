/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.chromecast.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the AudioSink portion of the Chromecast add-on.
 *
 * @author Jason Holmes - Initial contribution
 */
@NonNullByDefault
public class ChromecastAudioSink {
    private final Logger logger = LoggerFactory.getLogger(ChromecastAudioSink.class);

    private static final String MIME_TYPE_AUDIO_WAV = "audio/wav";
    private static final String MIME_TYPE_AUDIO_MPEG = "audio/mpeg";

    private final ChromecastCommander commander;
    private final AudioHTTPServer audioHTTPServer;
    private final @Nullable String callbackUrl;

    public ChromecastAudioSink(ChromecastCommander commander, AudioHTTPServer audioHTTPServer,
            @Nullable String callbackUrl) {
        this.commander = commander;
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
    }

    public void process(@Nullable AudioStream audioStream) throws UnsupportedAudioFormatException {
        if (audioStream == null) {
            // in case the audioStream is null, this should be interpreted as a request to end any currently playing
            // stream.
            logger.trace("Stop currently playing stream.");
            commander.handleStop(OnOffType.ON);
        } else {
            final String url;
            if (audioStream instanceof URLAudioStream) {
                // it is an external URL, the speaker can access it itself and play it.
                URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
                url = urlAudioStream.getURL();
            } else {
                if (callbackUrl != null) {
                    // we serve it on our own HTTP server
                    String relativeUrl;
                    if (audioStream instanceof FixedLengthAudioStream) {
                        relativeUrl = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 10);
                    } else {
                        relativeUrl = audioHTTPServer.serve(audioStream);
                    }
                    url = callbackUrl + relativeUrl;
                } else {
                    logger.warn("We do not have any callback url, so Chromecast cannot play the audio stream!");
                    return;
                }
            }
            commander.playMedia("Notification", url,
                    AudioFormat.MP3.isCompatible(audioStream.getFormat()) ? MIME_TYPE_AUDIO_MPEG : MIME_TYPE_AUDIO_WAV);
        }
    }
}
