/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.doorbird.internal.audio;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.doorbird.internal.handler.DoorbellHandler;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSinkSync;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.UnsupportedAudioStreamException;
import org.openhab.core.library.types.PercentType;

/**
 * The audio sink for doorbird
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
@NonNullByDefault
public class DoorbirdAudioSink extends AudioSinkSync {

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.WAV);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);

    private DoorbellHandler doorbellHandler;

    public DoorbirdAudioSink(DoorbellHandler doorbellHandler) {
        this.doorbellHandler = doorbellHandler;
    }

    @Override
    public String getId() {
        return doorbellHandler.getThing().getUID().toString();
    }

    @Override
    public @Nullable String getLabel(@Nullable Locale locale) {
        return doorbellHandler.getThing().getLabel();
    }

    @Override
    protected void processSynchronously(@Nullable AudioStream audioStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if (audioStream == null) {
            return;
        }
        try (ConvertedInputStream normalizedULAWStream = new ConvertedInputStream(audioStream)) {
            doorbellHandler.sendAudio(normalizedULAWStream);
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new UnsupportedAudioFormatException("Cannot send to the doorbird sink", audioStream.getFormat(), e);
        }
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
        return new PercentType(100);
    }

    @Override
    public void setVolume(PercentType volume) {
        throw new UnsupportedOperationException("Volume can not be set");
    }
}
