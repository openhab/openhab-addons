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
package org.openhab.voice.marytts.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

import javax.sound.sampled.AudioInputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;

/**
 * Implementation of the {@link AudioSource} interface for the {@link MaryTTSService}
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to updated APIs and moved to openHAB
 */
class MaryTTSAudioStream extends FixedLengthAudioStream {

    /**
     * {@link AudioFormat} of this {@link AudioSource}
     */
    private final AudioFormat audioFormat;

    /**
     * {@link InputStream} of this {@link AudioSource}
     */
    private InputStream inputStream;

    private final byte[] rawAudio;
    private final int length;

    /**
     * Constructs an instance with the passed properties
     *
     * @param inputStream The InputStream of this instance
     * @param audioFormat The AudioFormat of this instance
     * @throws IOException
     */
    public MaryTTSAudioStream(AudioInputStream inputStream, AudioFormat audioFormat) throws IOException {
        rawAudio = IOUtils.toByteArray(inputStream);
        this.length = rawAudio.length + 36;
        this.inputStream = new SequenceInputStream(getWavHeaderInputStream(length), new ByteArrayInputStream(rawAudio));
        this.audioFormat = audioFormat;
    }

    @Override
    public AudioFormat getFormat() {
        return this.audioFormat;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public long length() {
        return length;
    }

    private InputStream getWavHeaderInputStream(int length) throws IOException {
        // WAVE header
        // see http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
        byte[] header = new byte[44];

        byte format = 0x10; // PCM
        byte bits = 16;
        byte channel = 1;
        long srate = (this.audioFormat != null) ? this.audioFormat.getFrequency() : 48000l;
        long rawLength = length - 36;
        long bitrate = srate * channel * bits;

        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (length & 0xff);
        header[5] = (byte) ((length >> 8) & 0xff);
        header[6] = (byte) ((length >> 16) & 0xff);
        header[7] = (byte) ((length >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = format;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;
        header[21] = 0;
        header[22] = channel;
        header[23] = 0;
        header[24] = (byte) (srate & 0xff);
        header[25] = (byte) ((srate >> 8) & 0xff); 
        header[26] = (byte) ((srate >> 16) & 0xff); 
        header[27] = (byte) ((srate >> 24) & 0xff);
        header[28] = (byte) ((bitrate / 8) & 0xff);
        header[29] = (byte) (((bitrate / 8) >> 8) & 0xff);
        header[30] = (byte) (((bitrate / 8) >> 16) & 0xff);
        header[31] = (byte) (((bitrate / 8) >> 24) & 0xff);
        header[32] = (byte) ((channel * bits) / 8);
        header[33] = 0;
        header[34] = 16;
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (rawLength & 0xff);
        header[41] = (byte) ((rawLength >> 8) & 0xff);
        header[42] = (byte) ((rawLength >> 16) & 0xff);
        header[43] = (byte) ((rawLength >> 24) & 0xff);
        return new ByteArrayInputStream(header);
    }

    @Override
    public synchronized void reset() throws IOException {
        IOUtils.closeQuietly(inputStream);
        this.inputStream = new SequenceInputStream(getWavHeaderInputStream(length), new ByteArrayInputStream(rawAudio));
    }

    @Override
    public InputStream getClonedStream() throws AudioException {
        try {
            return new SequenceInputStream(getWavHeaderInputStream(length), new ByteArrayInputStream(rawAudio));
        } catch (IOException e) {
            throw new AudioException(e);
        }
    }
}
