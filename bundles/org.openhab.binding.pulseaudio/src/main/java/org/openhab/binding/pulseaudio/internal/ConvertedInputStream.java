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
package org.openhab.binding.pulseaudio.internal;

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
import org.openhab.core.audio.FixedLengthAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 * This class convert a stream to the normalized pcm
 * format wanted by the pulseaudio sink
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class ConvertedInputStream extends InputStream {

    private final Logger logger = LoggerFactory.getLogger(ConvertedInputStream.class);

    private static final javax.sound.sampled.AudioFormat TARGET_FORMAT = new javax.sound.sampled.AudioFormat(
            javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);

    private final AudioFormat audioFormat;
    private AudioInputStream pcmNormalizedInputStream;

    private long duration = -1;
    private long length = -1;

    public ConvertedInputStream(AudioStream innerInputStream)
            throws UnsupportedAudioFormatException, UnsupportedAudioFileException, IOException {

        this.audioFormat = innerInputStream.getFormat();

        if (innerInputStream instanceof FixedLengthAudioStream) {
            length = ((FixedLengthAudioStream) innerInputStream).length();
        }

        pcmNormalizedInputStream = getPCMStreamNormalized(getPCMStream(new ResetableInputStream(innerInputStream)));
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
     * @param pcmInputStream
     *
     * @return A PCM normalized stream (2 channel, 44100hz, 16 bit signed)
     */
    private AudioInputStream getPCMStreamNormalized(AudioInputStream pcmInputStream) {

        javax.sound.sampled.AudioFormat format = pcmInputStream.getFormat();
        if (format.getChannels() != 2
                || !format.getEncoding().equals(javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED)
                || Math.abs(format.getFrameRate() - 44100) > 1000) {
            logger.debug("Sound is not in the target format. Trying to reencode it");
            return AudioSystem.getAudioInputStream(TARGET_FORMAT, pcmInputStream);
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

    /**
     * This class add reset capability (on the first bytes only)
     * to an AudioStream. This is necessary for the parsing / format detection.
     *
     */
    public static class ResetableInputStream extends InputStream {

        private static final int BUFFER_LENGTH = 10000;

        private final InputStream originalInputStream;

        private int position = -1;
        private int markPosition = -1;
        private int maxPreviousPosition = -2;

        private byte[] startingBuffer = new byte[BUFFER_LENGTH + 1];

        public ResetableInputStream(InputStream originalInputStream) {
            this.originalInputStream = originalInputStream;
        }

        @Override
        public void close() throws IOException {
            originalInputStream.close();
        }

        @Override
        public int read() throws IOException {
            if (position >= BUFFER_LENGTH || originalInputStream.markSupported()) {
                return originalInputStream.read();
            } else {
                position++;
                if (position <= maxPreviousPosition) {
                    return Byte.toUnsignedInt(startingBuffer[position]);
                } else {
                    int currentByte = originalInputStream.read();
                    startingBuffer[position] = (byte) currentByte;
                    maxPreviousPosition = position;
                    return currentByte;
                }
            }
        }

        @Override
        public synchronized void mark(int readlimit) {
            if (originalInputStream.markSupported()) {
                originalInputStream.mark(readlimit);
            }
            markPosition = position;
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public synchronized void reset() throws IOException {
            if (originalInputStream.markSupported()) {
                originalInputStream.reset();
            } else if (position >= BUFFER_LENGTH) {
                throw new IOException("mark/reset not supported above " + BUFFER_LENGTH + " bytes");
            }
            position = markPosition;
        }
    }
}
