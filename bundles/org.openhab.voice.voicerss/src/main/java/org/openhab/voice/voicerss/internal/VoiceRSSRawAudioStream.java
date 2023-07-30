/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.voice.voicerss.internal;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.SizeableAudioStream;

/**
 * Implementation of the {@link AudioStream} interface for the
 * {@link VoiceRSSTTSService}. It simply uses a {@link AudioStream}.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class VoiceRSSRawAudioStream extends AudioStream implements SizeableAudioStream {

    private InputStream inputStream;
    private AudioFormat format;
    private long length;

    public VoiceRSSRawAudioStream(InputStream inputStream, AudioFormat format, long length) {
        this.inputStream = inputStream;
        this.format = format;
        this.length = length;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
