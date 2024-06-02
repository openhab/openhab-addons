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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class convert a stream to the normalized ulaw
 * format wanted by doorbird api
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class ConvertedInputStream extends InputStream {

    private static final javax.sound.sampled.AudioFormat INTERMEDIARY_PCM_FORMAT = new javax.sound.sampled.AudioFormat(
            javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 1, 2, 8000, false);
    private static final javax.sound.sampled.AudioFormat TARGET_ULAW_FORMAT = new javax.sound.sampled.AudioFormat(
            javax.sound.sampled.AudioFormat.Encoding.ULAW, 8000, 8, 1, 1, 8000, false);

    private AudioInputStream pcmUlawInputStream;

    public ConvertedInputStream(InputStream innerInputStream) throws UnsupportedAudioFileException, IOException {
        pcmUlawInputStream = getULAWStream(new BufferedInputStream(innerInputStream));
    }

    public AudioInputStream getAudioInputStream() {
        return pcmUlawInputStream;
    }

    @Override
    public int read(byte @Nullable [] b) throws IOException {
        return pcmUlawInputStream.read(b);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        return pcmUlawInputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return pcmUlawInputStream.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return pcmUlawInputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte @Nullable [] b, int off, int len) throws IOException {
        return pcmUlawInputStream.readNBytes(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return pcmUlawInputStream.read();
    }

    @Override
    public void close() throws IOException {
        pcmUlawInputStream.close();
    }

    /**
     * Ensure the right ULAW format by converting if necessary (two pass)
     *
     * @param originalInputStream a mark/reset compatible stream
     *
     * @return A ULAW stream (1 channel, 8000hz, 16 bit signed)
     * @throws IOException
     * @throws UnsupportedAudioFileException
     */
    private AudioInputStream getULAWStream(InputStream originalInputStream)
            throws UnsupportedAudioFileException, IOException {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(originalInputStream);
            AudioFormat format = audioInputStream.getFormat();

            boolean frameRateOk = Math.abs(format.getFrameRate() - 8000) < 1;
            boolean sampleRateOk = Math.abs(format.getSampleRate() - 8000) < 1;

            if (format.getEncoding().equals(Encoding.ULAW) && format.getChannels() == 1 && frameRateOk && sampleRateOk
                    && format.getFrameSize() == 1 && format.getSampleSizeInBits() == 8) {
                return audioInputStream;
            }

            // we have to use an intermediary format with 16 bits, even if the final target format is 8 bits
            // this is a limitation of the conversion library, which only accept 16 bits input to convert to ULAW.
            AudioInputStream targetPCMFormat = audioInputStream;
            if (format.getChannels() != 1 || !frameRateOk || !sampleRateOk || format.getFrameSize() != 2
                    || format.getSampleSizeInBits() != 16) {
                targetPCMFormat = AudioSystem.getAudioInputStream(INTERMEDIARY_PCM_FORMAT, audioInputStream);
            }

            return AudioSystem.getAudioInputStream(TARGET_ULAW_FORMAT, targetPCMFormat);
        } catch (IllegalArgumentException iarg) {
            throw new UnsupportedAudioFileException(
                    "Cannot convert audio input to ULAW target format. Cause: " + iarg.getMessage());
        }
    }
}
