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
import java.io.InputStream;
import java.net.Socket;
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
import org.openhab.core.audio.UnsupportedAudioFormatException;
import org.openhab.core.audio.utils.AudioSinkUtils;
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

        ConvertedInputStream preparedInputStream = null;
        AcquireModuleResult acquireModuleResult = null;
        CompletableFuture<@Nullable Void> soundPlayed = new CompletableFuture<>();
        try {
            preparedInputStream = new ConvertedInputStream(audioStream);
            acquireModuleResult = acquireSimpleProtocolModule(preparedInputStream.getFormat());

            // final var needed to use inside lambda :
            final var finalPreparedInputStream = preparedInputStream;
            final var finalAcquireModuleResult = acquireModuleResult;
            scheduler.execute(() -> {
                Socket spSocket = null;
                try {
                    SimpleProtocolTCPModule spModule = finalAcquireModuleResult.module()
                            .orElseThrow(() -> new IOException("Unable to load new Simple Protocol module instance."));
                    spSocket = connectIfNeeded(spModule);
                    var moduleOutputStream = spSocket.getOutputStream();

                    Long timeStampEnded = audioSinkUtils.transferAndAnalyzeLength(finalPreparedInputStream,
                            moduleOutputStream, finalPreparedInputStream.getFormat());

                    long timeToWait = Optional.ofNullable(timeStampEnded)
                            .map(tse -> (tse - System.nanoTime()) / 1000000).orElse(0L);
                    if (timeToWait > 0) {
                        logger.debug("Some time to let the system play sound : {}", timeToWait);
                        scheduler
                                .schedule(
                                        () -> endStream(finalPreparedInputStream,
                                                finalAcquireModuleResult.releaseModule(), soundPlayed, null),
                                        timeToWait, TimeUnit.MILLISECONDS);
                    } else {
                        endStream(finalPreparedInputStream, finalAcquireModuleResult.releaseModule(), soundPlayed,
                                null);
                    }
                } catch (IOException e) {
                    if (spSocket != null) {
                        disconnect(spSocket);
                    }
                    endStream(finalPreparedInputStream, finalAcquireModuleResult.releaseModule(), soundPlayed, e);
                }
            });
        } catch (UnsupportedAudioFileException | UnsupportedAudioFormatException | IOException
                | InterruptedException e) {
            endStream(preparedInputStream, null, soundPlayed, new UnsupportedAudioFormatException(
                    "Cannot send sound to the pulseaudio sink", audioStream.getFormat(), e));
        }
        return soundPlayed;
    }

    private void endStream(@Nullable InputStream inputStream, @Nullable Runnable releaseModule,
            CompletableFuture<@Nullable Void> soundPlayed, @Nullable Exception sourceException) {
        if (releaseModule != null) {
            releaseModule.run();
        }
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException ignored) {
        }
        if (sourceException != null) {
            soundPlayed.completeExceptionally(sourceException);
        } else {
            soundPlayed.complete(null);
        }
        // if the stream is not needed anymore, then we should call back the AudioStream to let it a chance
        // to auto dispose.
        if (inputStream instanceof Disposable disposableAudioStream) {
            try {
                disposableAudioStream.dispose();
            } catch (IOException e) {
                String fileName = inputStream instanceof FileAudioStream file ? file.toString() : "unknown";
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot dispose of stream {}", fileName, e);
                } else {
                    logger.warn("Cannot dispose of stream {}, reason {}", fileName, e.getMessage());
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
