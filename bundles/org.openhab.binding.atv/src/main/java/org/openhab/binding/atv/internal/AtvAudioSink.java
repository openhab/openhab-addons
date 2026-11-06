/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.handler.AtvHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSinkSync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;

/**
 * Exposes an Apple TV or AirPlay speaker Thing as an openHAB audio sink so text-to-speech,
 * {@code playSound} and notifications can be played on the device.
 *
 * <p>
 * Audio is streamed to the device over RAOP (AirPlay audio); both Apple TVs and speakers are RAOP
 * receivers. The stream is decoded locally, so the accepted formats are those the JVM's Java Sound SPIs
 * can read: WAV/PCM plus MP3 via the bundled decoder. Playback is synchronous:
 * {@link #processSynchronously} blocks until the device has finished playing.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AtvAudioSink extends AudioSinkSync {

    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);
    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.WAV, AudioFormat.PCM_SIGNED,
            AudioFormat.MP3);

    private final AtvHandler handler;

    /** Runtime volume override set by openHAB; when {@code null} the Thing's configured default is used. */
    private volatile @Nullable PercentType volumeOverride;

    /**
     * Creates a sink for a single device.
     *
     * @param handler the Thing handler owning the device connection
     */
    public AtvAudioSink(AtvHandler handler) {
        this.handler = handler;
    }

    @Override
    public String getId() {
        return handler.getThing().getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return handler.getThing().getLabel();
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_STREAMS;
    }

    @Override
    public PercentType getVolume() {
        PercentType override = volumeOverride;
        return override != null ? override : new PercentType(handler.getNotificationVolume());
    }

    @Override
    public void setVolume(PercentType volume) {
        // openHAB brackets a volume-carrying playback with setVolume(new) ... setVolume(previous), where
        // "previous" is whatever getVolume() reported - the Thing's configured default. Treating that
        // restore as "no override" keeps a later change to the configured volume effective.
        this.volumeOverride = volume.intValue() == handler.getNotificationVolume() ? null : volume;
    }

    @Override
    protected void processSynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            handler.stopAudio();
            return;
        }
        try {
            handler.streamAudio(audioStream, getVolume().doubleValue());
        } finally {
            try {
                audioStream.close();
            } catch (IOException ignored) {
            }
        }
    }
}
