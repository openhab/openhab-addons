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
import java.util.Map;
import javazoom.spi.mpeg.sampled.convert.MpegFormatConversionProvider;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.SizeableAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 * This class convert a stream to the normalized pcm signed
 * format supported by the pulseaudio sink
 *
 * @author Gwendal Roulleau - Initial contribution
 * @author Miguel Álvarez Díez - Extend from AudioStream
 */
@NonNullByDefault
public class ConvertedInputStream extends AudioStream {

    private final Logger logger = LoggerFactory.getLogger(ConvertedInputStream.class);

    private static final AudioFormat TARGET_FORMAT = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, 44100 * 16 * 2, 44100L, 2);

    private static final javax.sound.sampled.AudioFormat J_TARGET_FORMAT = new javax.sound.sampled.AudioFormat(44100,
            16, 2, true, false);

    private final AudioFormat audioFormat;
    private final AudioInputStream pcmNormalizedInputStream;

    private long duration = -1;
    private long length = -1;

    public ConvertedInputStream(AudioStream innerInputStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioFileException, IOException {
        this.audioFormat = innerInputStream.getFormat();

        if (innerInputStream instanceof SizeableAudioStream sizeableAudioStream) {
            length = sizeableAudioStream.length();
        }

        pcmNormalizedInputStream = getPCMStreamNormalized(getPCMStream(new BufferedInputStream(innerInputStream)));
    }

    @Override
    public int read(byte @Nullable [] b) throws IOException {
        return pcmNormalizedInputStream.read(b);
    }

    @Override
    public int read(byte @Nullable [] b, int off, int len) throws IOException {
        return pcmNormalizedInputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return pcmNormalizedInputStream.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return pcmNormalizedInputStream.readNBytes(len);
    }

    @Override
    public int readNBytes(byte @Nullable [] b, int off, int len) throws IOException {
        return pcmNormalizedInputStream.readNBytes(b, off, len);
    }

    @Override
    public int read() throws IOException {
        return pcmNormalizedInputStream.read();
    }

    @Override
    public void close() throws IOException {
        pcmNormalizedInputStream.close();
    }

    /**
     * Ensure right PCM format by converting if needed (sample rate, channel)
     *
     * @param pcmInputStream A pcm signed input stream
     *
     * @return A PCM normalized stream (2 channel, 44100hz, 16 bit signed)
     */
    private AudioInputStream getPCMStreamNormalized(AudioInputStream pcmInputStream) {
        javax.sound.sampled.AudioFormat format = pcmInputStream.getFormat();
        if (format.getChannels() != 2
                || !format.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED)
                || Math.abs(format.getFrameRate() - 44100) > 1000) {
            logger.debug("Sound is not in the target format. Trying to reencode it");
            return AudioSystem.getAudioInputStream(J_TARGET_FORMAT, pcmInputStream);
        } else {
            return pcmInputStream;
        }
    }

    public long getDuration() {
        return duration;
    }

    /**
     * If necessary, this method convert MP3 to PCM, and try to
     * extract duration information.
     *
     * @param resetableInnerInputStream A stream supporting reset operation
     *            (reset is mandatory to parse formation without loosing data)
     *
     * @return PCM stream
     * @throws UnsupportedAudioFileException
     * @throws IOException
     * @throws UnsupportedAudioFormatException
     */
    private AudioInputStream getPCMStream(InputStream resetableInnerInputStream)
            throws UnsupportedAudioFileException, IOException, UnsupportedAudioFormatException {
        if (AudioFormat.MP3.isCompatible(audioFormat)) {
            MpegAudioFileReader mpegAudioFileReader = new MpegAudioFileReader();

            if (length > 0) { // compute duration if possible
                AudioFileFormat audioFileFormat = mpegAudioFileReader.getAudioFileFormat(resetableInnerInputStream);
                if (audioFileFormat instanceof TAudioFileFormat) {
                    Map<String, Object> taudioFileFormatProperties = ((TAudioFileFormat) audioFileFormat).properties();
                    if (taudioFileFormatProperties.containsKey("mp3.framesize.bytes")
                            && taudioFileFormatProperties.containsKey("mp3.framerate.fps")) {
                        Integer frameSize = (Integer) taudioFileFormatProperties.get("mp3.framesize.bytes");
                        Float frameRate = (Float) taudioFileFormatProperties.get("mp3.framerate.fps");
                        if (frameSize != null && frameRate != null) {
                            duration = Math.round((length / (frameSize * frameRate)) * 1000);
                            logger.debug("Duration of input stream : {}", duration);
                        }
                    }
                }
                resetableInnerInputStream.reset();
            }

            logger.debug("Sound is a MP3. Trying to reencode it");
            // convert MP3 to PCM :
            AudioInputStream sourceAIS = mpegAudioFileReader.getAudioInputStream(resetableInnerInputStream);
            javax.sound.sampled.AudioFormat sourceFormat = sourceAIS.getFormat();

            MpegFormatConversionProvider mpegconverter = new MpegFormatConversionProvider();
            javax.sound.sampled.AudioFormat convertFormat = new javax.sound.sampled.AudioFormat(
                    javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, sourceFormat.getSampleRate(), 16,
                    sourceFormat.getChannels(), sourceFormat.getChannels() * 2, sourceFormat.getSampleRate(), false);

            return mpegconverter.getAudioInputStream(convertFormat, sourceAIS);
        } else if (AudioFormat.WAV.isCompatible(audioFormat)) {
            // return the same input stream, but try to compute the duration first
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(resetableInnerInputStream);
            if (length > 0) {
                int frameSize = audioInputStream.getFormat().getFrameSize();
                float frameRate = audioInputStream.getFormat().getFrameRate();
                float durationInSeconds = (length / (frameSize * frameRate));
                duration = Math.round(durationInSeconds * 1000);
                logger.debug("Duration of input stream : {}", duration);
            }
            return audioInputStream;
        } else {
            throw new UnsupportedAudioFormatException("Pulseaudio audio sink can only play pcm or mp3 stream",
                    audioFormat);
        }
    }

    @Override
    public AudioFormat getFormat() {
        return TARGET_FORMAT;
    }
}
