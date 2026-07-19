/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.protocols.raop;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.capability.Stream;
import org.openhab.binding.atv.internal.client.conf.BaseService;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.TakeoverMethod;
import org.openhab.binding.atv.internal.client.dto.MediaMetadata;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.protocols.airplay.AirPlayAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of stream functionality for RAOP.
 *
 * <p>
 * Acquires the playback manager (one stream at a time), sets up the session, opens the
 * audio source in the negotiated format, resolves metadata (from the file's tags, from the
 * caller, or merged) and hands off to {@link RaopStreamClient#sendAudio}.
 *
 * <p>
 * HTTP(S) sources are not supported yet.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopStream implements Stream, CapabilitySource {

    /** Option key that requests filling in missing metadata fields from the file's tags. */
    public static final String OPTION_OVERRIDE_MISSING_METADATA = "override_missing_metadata";

    /** Mark/read-ahead limit used when extracting metadata from buffered streams. */
    private static final int STREAM_METADATA_MARK_LIMIT = 8 * 1024 * 1024;

    private static final Logger LOGGER = LoggerFactory.getLogger(RaopStream.class);

    private final BaseService service;
    private final RaopListener listener;
    private final RaopAudio audio;
    private final RaopPlaybackManager playbackManager;
    private final TakeoverMethod takeover;

    /**
     * Creates a new stream instance.
     *
     * @param service the RAOP service (credentials, password and properties)
     * @param listener listener wired to the push updater
     * @param audio audio implementation used for the initial volume
     * @param playbackManager playback manager owning the session
     * @param takeover relay takeover method bound to RAOP; the audio, metadata, push
     *            updater and remote control relays are taken over for the duration of a stream
     */
    public RaopStream(BaseService service, RaopListener listener, RaopAudio audio, RaopPlaybackManager playbackManager,
            TakeoverMethod takeover) {
        this.service = service;
        this.listener = listener;
        this.audio = audio;
        this.playbackManager = playbackManager;
        this.takeover = takeover;
    }

    @Override
    public CompletableFuture<Void> streamFile(String source, @Nullable MediaMetadata metadata,
            Map<String, Object> options) {
        String lower = source.toLowerCase(Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return CompletableFuture.failedFuture(new NotSupportedError("streaming from HTTP is not supported"));
        }
        File file = new File(source);
        return stream(options, metadata, WavMetadata.parse(file), context -> JavaSoundAudioSource.open(file,
                context.sampleRate, context.channels, context.bytesPerChannel));
    }

    @Override
    public CompletableFuture<Void> streamFile(InputStream source, @Nullable MediaMetadata metadata,
            Map<String, Object> options) {
        BufferedInputStream buffered = source instanceof BufferedInputStream b ? b : new BufferedInputStream(source);
        buffered.mark(STREAM_METADATA_MARK_LIMIT);
        MediaMetadata fileMetadata = WavMetadata.parse(buffered, STREAM_METADATA_MARK_LIMIT / 2);
        try {
            buffered.reset();
        } catch (IOException e) {
            return CompletableFuture.failedFuture(new ProtocolError("failed to rewind stream", e));
        }
        return stream(options, metadata, fileMetadata, context -> JavaSoundAudioSource.open(buffered,
                context.sampleRate, context.channels, context.bytesPerChannel));
    }

    @FunctionalInterface
    private interface SourceOpener {
        AudioSource open(StreamContext context) throws IOException;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private CompletableFuture<Void> stream(Map<String, Object> options, @Nullable MediaMetadata metadata,
            MediaMetadata fileMetadata, SourceOpener opener) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("raop-stream").start(() -> {
            try {
                playbackManager.acquire();
                AudioSource audioFile = null;
                // Take over the capability interfaces RAOP controls while streaming
                Runnable takeoverRelease = takeover.takeover(Audio.class, Metadata.class, PushUpdater.class,
                        RemoteControl.class);
                try {
                    RaopStreamClient client = playbackManager.setup();
                    StreamContext context = playbackManager.context();
                    context.credentials = AirPlayAuth.extractCredentials(service);
                    context.password = service.password().orElse(null);

                    client.setListener(listener);
                    client.initialize(service.properties());

                    // After initialize has been called, all the audio properties are known
                    audioFile = opener.open(context);

                    // If no custom metadata is provided, use what the source provides. If
                    // it is, check whether metadata should be overridden or not.
                    MediaMetadata streamMetadata;
                    if (metadata == null) {
                        streamMetadata = fileMetadata;
                    } else if (isOverrideMissingMetadata(options)) {
                        streamMetadata = mergeInto(fileMetadata, metadata);
                    } else {
                        streamMetadata = metadata;
                    }

                    Double volume = prepareVolume(client, context, options);
                    client.sendAudio(audioFile, streamMetadata, volume);
                } finally {
                    takeoverRelease.run();
                    if (audioFile != null) {
                        try {
                            audioFile.close();
                        } catch (IOException e) {
                            LOGGER.debug("Failed to close audio source: {}", e.toString());
                        }
                    }
                    playbackManager.teardown();
                }
                RaopFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    /**
     * Resolves the volume handling before streaming: if the user never changed the volume
     * and the receiver reports an initial volume, use that; otherwise try to set our
     * volume, deferring to after stream start on failure.
     *
     * @return the volume in percent to set after streaming started, or {@code null}
     */
    private @Nullable Double prepareVolume(RaopStreamClient client, StreamContext context,
            Map<String, Object> options) {
        // A host-requested playback volume (e.g. the openHAB audio sink) takes priority over the
        // receiver's reported volume. It is returned as a deferred set so it is applied once
        // streaming has started, overriding both the receiver's initial volume and any volume the
        // receiver announces during setup. Deviation from pyatv, which always adopts the receiver's
        // initialVolume for fire-and-forget playback.
        if (options.get(OPTION_VOLUME) instanceof Number requested) {
            double pct = requested.doubleValue();
            context.volume = RaopVolume.pctToDbfs(pct);
            return pct;
        }
        if (!audio.hasChangedVolume() && client.info().containsKey("initialVolume")) {
            Object initialVolume = client.info().get("initialVolume");
            if (!(initialVolume instanceof Double dbfs)) {
                throw new ProtocolError("initial volume " + initialVolume + " has incorrect type");
            }
            context.volume = dbfs;
            return null;
        }
        // Try to set volume. If it fails, defer to setting it once streaming has started.
        try {
            RaopFutures.await(audio.setVolume(audio.volume(), null));
            return null;
        } catch (RuntimeException ex) {
            LOGGER.debug("Failed to set volume ({}), delaying call", ex.toString());
            return audio.volume();
        }
    }

    private static boolean isOverrideMissingMetadata(Map<String, Object> options) {
        return options != null && Boolean.TRUE.equals(options.get(OPTION_OVERRIDE_MISSING_METADATA));
    }

    /** Fills {@code null} fields of {@code base} from {@code other}. */
    static MediaMetadata mergeInto(MediaMetadata base, MediaMetadata other) {
        return new MediaMetadata(base.title() != null ? base.title() : other.title(),
                base.artist() != null ? base.artist() : other.artist(),
                base.album() != null ? base.album() : other.album(),
                base.artwork() != null ? base.artwork() : other.artwork(),
                base.duration() != null ? base.duration() : other.duration());
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.STREAM_STREAM_FILE, Capability.STREAM_STREAM_BUFFER);
    }
}
