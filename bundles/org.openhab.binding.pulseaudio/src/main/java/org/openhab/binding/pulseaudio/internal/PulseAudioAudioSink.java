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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
                try {
                    audioStream.close();
                } catch (IOException ex) {
                    logger.warn("Error closing audio stream: {}", ex.getMessage());
                }
                return CompletableFuture.failedFuture(new UnsupportedAudioFormatException(
                        "Cannot send sound to the pulseaudio sink", audioStream.getFormat(), e));
            }
        }
        SimpleProtocolTCPModule spModule;
        Runnable releaseModule;
        try {
            var acquireModuleResult = acquireSimpleProtocolModule(pcmSignedStream.getFormat());
            if (acquireModuleResult.module().isEmpty()) {
                throw new IOException("Unable to load new Simple Protocol module instance.");
            }
            spModule = acquireModuleResult.module().get();
            releaseModule = acquireModuleResult.releaseModule();
        } catch (IOException | InterruptedException e) {
            try {
                pcmSignedStream.close();
            } catch (IOException ex) {
                logger.warn("IOException closing audio stream: {}", ex.getMessage());
            }
            return CompletableFuture.failedFuture(new UnsupportedAudioFormatException(
                    "Cannot send sound to the pulseaudio sink", audioStream.getFormat(), e));
        }
        // If piped stream, assume real time audio, do not measure its duration and transfer in background until closed
        if (pcmSignedStream instanceof PipedAudioStream pipedAudioStream) {
            CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
            final var module = spModule;
            final var canceled = new AtomicBoolean(false);
            scheduler.submit(() -> {
                try {
                    var moduleOutputStream = connectIfNeeded(module).getOutputStream();
                    int bufferSize = 8192;
                    byte[] buffer = new byte[bufferSize];
                    int read;
                    while (!canceled.get() && (read = pipedAudioStream.read(buffer, 0, bufferSize)) >= 0) {
                        moduleOutputStream.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    try {
                        pipedAudioStream.close();
                    } catch (IOException ignored) {
                        logger.warn("IOException closing piped audio stream: {}", e.getMessage());
                    }
                    soundPlayed.completeExceptionally(e);
                    return;
                } catch (InterruptedException ignored) {
                    // if interrupted complete normally
                }
                try {
                    pipedAudioStream.close();
                } catch (IOException e) {
                    logger.warn("IOException closing piped audio stream: {}", e.getMessage());
                }
                soundPlayed.complete(null);
            });
            pipedAudioStream.onClose(() -> {
                canceled.set(true);
                releaseModule.run();
            });
            return soundPlayed;
        }
        // If not piped stream, complete future after estimated playback time
        try (pcmSignedStream) {
            @Nullable
            Socket spSocket = null;
            try {
                spSocket = connectIfNeeded(spModule);
                Instant start = Instant.now();
                CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
                Runnable releaseAndComplete = () -> {
                    releaseModule.run();
                    soundPlayed.complete(null);
                };
                if (duration != -1) {
                    // ensure, if the sound has a duration
                    // that we let at least this time for the system to play
                    pcmSignedStream.transferTo(spSocket.getOutputStream());
                    Instant end = Instant.now();
                    long millisSecondTimedToSendAudioData = Duration.between(start, end).toMillis();
                    if (millisSecondTimedToSendAudioData < duration) {
                        long timeToWait = duration - millisSecondTimedToSendAudioData;
                        logger.debug("Some time to let the system play sound : {}", timeToWait);
                        scheduler.schedule(releaseAndComplete, timeToWait, TimeUnit.MILLISECONDS);
                        return soundPlayed;
                    } else {
                        releaseModule.run();
                        return CompletableFuture.completedFuture(null);
                    }
                } else {
                    // We have a second method available to guess the duration, and it is during transfer
                    Long timeStampEnd = audioSinkUtils.transferAndAnalyzeLength(pcmSignedStream,
                            spSocket.getOutputStream(), pcmSignedStream.getFormat());
                    if (timeStampEnd != null) {
                        long now = System.nanoTime();
                        long timeToWait = timeStampEnd - now;
                        if (timeToWait > 0) {
                            scheduler.schedule(releaseAndComplete, timeToWait, TimeUnit.NANOSECONDS);
                        }
                        return soundPlayed;
                    } else {
                        releaseModule.run();
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
                releaseModule.run();
                return CompletableFuture.completedFuture(null);
            } catch (InterruptedException ie) {
                logger.info("Interrupted during sink audio connection: {}", ie.getMessage());
                releaseModule.run();
                return CompletableFuture.completedFuture(null);
            }
        } catch (IOException e) {
            releaseModule.run();
            return CompletableFuture.failedFuture(new UnsupportedAudioFormatException(
                    "Cannot send sound to the pulseaudio sink", audioStream.getFormat(), e));
        } finally {
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
