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
package org.openhab.binding.pulseaudio.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class convert a stream to the pcm signed
 * format supported by the pulseaudio sink
 *
 * @author Gwendal Roulleau - Initial contribution
 * @author Miguel Álvarez Díez - Extend from AudioStream
 */
@NonNullByDefault
public class ConvertedInputStream extends AudioStream {

    private final Logger logger = LoggerFactory.getLogger(ConvertedInputStream.class);

    private AudioFormat originalAudioFormat;
    private final AudioFormat outputAudioFormat;
    private final InputStream pcmInnerInputStream;

    private static final Set<String> COMPATIBLE_CODEC = Set.of(AudioFormat.CODEC_PCM_ALAW, AudioFormat.CODEC_PCM_ULAW,
            AudioFormat.CODEC_PCM_UNSIGNED);

    public ConvertedInputStream(AudioStream innerInputStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioFileException, IOException {
        this.originalAudioFormat = innerInputStream.getFormat();

        String container = originalAudioFormat.getContainer();
        if (container == null) {
            throw new UnsupportedAudioFormatException("Unknown format, cannot process", innerInputStream.getFormat());
        }

        if (container.equals(AudioFormat.CONTAINER_WAVE)) {
            if (originalAudioFormat.getFrequency() == null || originalAudioFormat.getChannels() == null
                    || originalAudioFormat.getBitRate() == null || originalAudioFormat.getCodec() == null
                    || originalAudioFormat.getBitDepth() == null || originalAudioFormat.isBigEndian() == null) {
                // parse it by ourself to maybe get missing information :
                this.originalAudioFormat = AudioWaveUtils.parseWavFormat(innerInputStream);
            }
        }

        if (AudioFormat.CODEC_PCM_SIGNED.equals(originalAudioFormat.getCodec())) {
            outputAudioFormat = originalAudioFormat;
            pcmInnerInputStream = innerInputStream;
            if (container.equals(AudioFormat.CONTAINER_WAVE)) {
                AudioWaveUtils.removeFMT(innerInputStream);
            }
        } else {
            pcmInnerInputStream = getPCMStream(new BufferedInputStream(innerInputStream));
            var javaAudioFormat = ((AudioInputStream) pcmInnerInputStream).getFormat();
            int bitRate = (int) javaAudioFormat.getSampleRate() * javaAudioFormat.getSampleSizeInBits()
                    * javaAudioFormat.getChannels();
            outputAudioFormat = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED,
                    javaAudioFormat.isBigEndian(), javaAudioFormat.getSampleSizeInBits(), bitRate,
                    (long) javaAudioFormat.getSampleRate(), javaAudioFormat.getChannels());
        }
    }

    @Override
    public int read(byte @Nullable [] b) throws IOException {
        return pcmInnerInputStream.read(b);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        return pcmInnerInputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return pcmInnerInputStream.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return pcmInnerInputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte @Nullable [] b, int off, int len) throws IOException {
        return pcmInnerInputStream.readNBytes(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return pcmInnerInputStream.read();
    }

    @Override
    public void close() throws IOException {
        pcmInnerInputStream.close();
    }

    /**
     * If necessary, this method convert to target PCM
     *
     * @param resetableInnerInputStream A stream supporting reset operation
     *            (reset is mandatory to parse formation without loosing data)
     *
     * @return PCM stream
     * @throws UnsupportedAudioFileException
     * @throws UnsupportedAudioFormatException
     * @throws IOException
     */
    private AudioInputStream getPCMStream(InputStream resetableInnerInputStream)
            throws UnsupportedAudioFileException, IOException, UnsupportedAudioFormatException {
        if (AudioFormat.CODEC_MP3.equals(originalAudioFormat.getCodec())) {
            logger.debug("Sound is a MP3. Trying to reencode it");
            // convert MP3 to PCM :
            AudioInputStream sourceAIS = new MpegAudioFileReader().getAudioInputStream(resetableInnerInputStream);
            javax.sound.sampled.AudioFormat sourceFormat = sourceAIS.getFormat();

            MpegFormatConversionProvider mpegconverter = new MpegFormatConversionProvider();
            int bitDepth = sourceFormat.getSampleSizeInBits() != -1 ? sourceFormat.getSampleSizeInBits() : 16;
            javax.sound.sampled.AudioFormat convertFormat = new javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), bitDepth,
                    sourceFormat.getChannels(), 2 * sourceFormat.getChannels(), sourceFormat.getSampleRate(), false);

            return mpegconverter.getAudioInputStream(convertFormat, sourceAIS);
        } else if (COMPATIBLE_CODEC.contains(originalAudioFormat.getCodec())) {
            long frequency = Optional.ofNullable(originalAudioFormat.getFrequency()).orElse(44100L);
            int channel = Optional.ofNullable(originalAudioFormat.getChannels()).orElse(1);
            javax.sound.sampled.AudioFormat targetFormat = new javax.sound.sampled.AudioFormat(frequency, 16, channel,
                    true, false);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(targetFormat,
                    AudioSystem.getAudioInputStream(resetableInnerInputStream));
            return audioInputStream;
        } else {
            throw new UnsupportedAudioFormatException("Pulseaudio audio sink can only play pcm or mp3 stream",
                    originalAudioFormat);
        }
    }

    @Override
    public AudioFormat getFormat() {
        return outputAudioFormat;
    }
}
