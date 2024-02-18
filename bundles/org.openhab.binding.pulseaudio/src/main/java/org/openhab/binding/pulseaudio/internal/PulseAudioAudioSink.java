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

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.binding.pulseaudio.internal.items.SimpleProtocolTCPModule;
import org.openhab.core.audio.AudioFormat;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.AudioStream;
import org.openhab.core.audio.FileAudioStream;
import org.openhab.core.audio.PipedAudioStream;
import org.openhab.core.audio.SizeableAudioStream;
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.utils.AudioSinkUtils;
import org.openhab.core.audio.utils.AudioWaveUtils;
import org.openhab.core.common.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The audio sink for openhab, implemented by a connection to a pulseaudio sink
 *
 * @author Gwendal Roulleau - Initial contribution
 * @author Miguel Álvarez - Move some code to the PulseaudioSimpleProtocolStream class so sink and source can extend
 *         from it.
 * @author Miguel Álvarez - Use a socket per stream.
 *
 */
@NonNullByDefault
public class PulseAudioAudioSink extends PulseaudioSimpleProtocolStream implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(PulseAudioAudioSink.class);

    private final AudioSinkUtils audioSinkUtils;

    private static final Set<AudioFormat> SUPPORTED_FORMATS = Set.of(AudioFormat.PCM_SIGNED, AudioFormat.WAV,
            AudioFormat.MP3);
    private static final Set<Class<? extends AudioStream>> SUPPORTED_STREAMS = Set.of(AudioStream.class);

    public PulseAudioAudioSink(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler,
            AudioSinkUtils audioSinkUtils) {
        super(pulseaudioHandler, scheduler);
        this.audioSinkUtils = audioSinkUtils;
    }

    @Override
    public void process(@Nullable AudioStream audioStream) {
        processAndComplete(audioStream);
    }

    @Override
    public CompletableFuture<@Nullable Void> processAndComplete(@Nullable AudioStream audioStream) {
        if (audioStream == null) {
            return CompletableFuture.completedFuture(null);
        }
        AudioStream pcmSignedStream;
        long duration = -1;
        if (AudioFormat.CODEC_PCM_SIGNED.equals(audioStream.getFormat().getCodec())) {
            logger.debug("PCM Signed audio streaming directly");
            pcmSignedStream = audioStream;
            if (AudioFormat.CONTAINER_WAVE.equals(audioStream.getFormat().getContainer())) {
                logger.debug("Removing wav container from data");
                try {
                    AudioWaveUtils.removeFMT(pcmSignedStream);
                } catch (IOException e) {
                    logger.warn("IOException trying to remove wav header: {}", e.getMessage());
                }
            }
            if (pcmSignedStream instanceof SizeableAudioStream sizeableAudioStream) {
                long length = sizeableAudioStream.length();
                var audioFormat = pcmSignedStream.getFormat();
                long byteRate = (Objects.requireNonNull(audioFormat.getBitDepth()) / 8)
                        * Objects.requireNonNull(audioFormat.getFrequency())
                        * Objects.requireNonNull(audioFormat.getChannels());
                float durationInSeconds = (float) length / byteRate;
                duration = Math.round(durationInSeconds * 1000);
                logger.debug("Duration of input stream : {}", duration);
            }
        } else {
            try {
                var convertedInputStream = new ConvertedInputStream(audioStream);
                duration = convertedInputStream.getDuration();
                pcmSignedStream = convertedInputStream;
            } catch (UnsupportedAudioFileException | UnsupportedAudioFormatException | IOException e) {
                return CompletableFuture.failedFuture(new UnsupportedAudioFormatException(
                        "Cannot send sound to the pulseaudio sink", audioStream.getFormat(), e));
            }
        }
        Optional<SimpleProtocolTCPModule> spModule = Optional.empty();
        try (pcmSignedStream) {
            spModule = acquireSimpleProtocolModule(pcmSignedStream.getFormat());
            if (spModule.isEmpty()) {
                throw new IOException("Unable to load new Simple Protocol module instance.");
            }
            @Nullable
            Socket spSocket = null;
            try {
                final Socket finalSPSocket = spSocket = connectIfNeeded(spModule.get());
                // send raw audio to the socket and to pulse audio
                Instant start = Instant.now();
                if (audioStream instanceof PipedAudioStream pipedAudioStream) {
                    // Assuming real time stream and do not measure its duration and transfer in background
                    CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
                    final var asyncStream = scheduler.submit(() -> {
                        try {
                            pcmSignedStream.transferTo(finalSPSocket.getOutputStream());
                        } catch (IOException e) {
                            soundPlayed.completeExceptionally(e);
                            try {
                                pcmSignedStream.close();
                            } catch (IOException ignored) {
                                logger.warn("Error closing piped audio stream");
                            }
                        }
                    });
                    pipedAudioStream.onClose(() -> {
                        if (!asyncStream.isDone()) {
                            asyncStream.cancel(true);
                        }
                        soundPlayed.complete(null);
                    });
                    return soundPlayed;
                } else if (duration != -1) {
                    // ensure, if the sound has a duration
                    // that we let at least this time for the system to play
                    pcmSignedStream.transferTo(spSocket.getOutputStream());
                    Instant end = Instant.now();
                    long millisSecondTimedToSendAudioData = Duration.between(start, end).toMillis();
                    if (millisSecondTimedToSendAudioData < duration) {
                        CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
                        long timeToWait = duration - millisSecondTimedToSendAudioData;
                        logger.debug("Some time to let the system play sound : {}", timeToWait);
                        scheduler.schedule(() -> soundPlayed.complete(null), timeToWait, TimeUnit.MILLISECONDS);
                        return soundPlayed;
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                } else {
                    // We have a second method available to guess the duration, and it is during transfer
                    Long timeStampEnd = audioSinkUtils.transferAndAnalyzeLength(pcmSignedStream,
                            spSocket.getOutputStream(), pcmSignedStream.getFormat());
                    CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
                    if (timeStampEnd != null) {
                        long now = System.nanoTime();
                        long timeToWait = timeStampEnd - now;
                        if (timeToWait > 0) {
                            scheduler.schedule(() -> soundPlayed.complete(null), timeToWait, TimeUnit.NANOSECONDS);
                        }
                        return soundPlayed;
                    } else {
                        return CompletableFuture.completedFuture(null);
                    }
                }
            } catch (IOException e) {
                String port = "unknown";
                if (spSocket != null) {
                    port = Integer.toString(spSocket.getPort());
                    disconnect(spSocket);
                }
                logger.warn(
                        "Error while trying to send audio to pulseaudio audio sink. Cannot connect to {}:{}, error: {}",
                        pulseaudioHandler.getHost(), port, e.getMessage());
                return CompletableFuture.completedFuture(null);
            } catch (InterruptedException ie) {
                logger.info("Interrupted during sink audio connection: {}", ie.getMessage());
                return CompletableFuture.completedFuture(null);
            }
        } catch (IOException | InterruptedException e) {
            return CompletableFuture.failedFuture(new UnsupportedAudioFormatException(
                    "Cannot send sound to the pulseaudio sink", audioStream.getFormat(), e));
        } finally {
            spModule.ifPresent(simpleProtocolTCPModule -> releaseModule(audioStream, simpleProtocolTCPModule));
            // if the stream is not needed anymore, then we should call back the AudioStream to let it a chance
            // to auto dispose.
            if (audioStream instanceof Disposable disposableAudioStream) {
                try {
                    disposableAudioStream.dispose();
                } catch (IOException e) {
                    String fileName = audioStream instanceof FileAudioStream file ? file.toString() : "unknown";
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot dispose of stream {}", fileName, e);
                    } else {
                        logger.warn("Cannot dispose of stream {}, reason {}", fileName, e.getMessage());
                    }
                }
            }
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
}
