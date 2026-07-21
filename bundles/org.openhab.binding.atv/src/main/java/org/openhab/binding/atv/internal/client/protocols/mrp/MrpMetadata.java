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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.dto.App;
import org.openhab.binding.atv.internal.client.dto.ArtworkInfo;
import org.openhab.binding.atv.internal.client.dto.Playing;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemMetadataOuterClass.ContentItemMetadata;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ContentItemOuterClass.ContentItem;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.SetStateMessageOuterClass.SetStateMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the metadata API for MRP.
 *
 * <p>
 * What is playing comes from the {@link PlayerStateManager}; artwork is fetched from the
 * iTunes artwork URL template ({@code artworkIdentifier}), the fixed {@code artworkURL},
 * or over MRP with a {@code PLAYBACK_QUEUE_REQUEST} carrying the artwork content property,
 * with a small LRU cache in front (a hit refreshes an entry's recency before eviction).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpMetadata implements Metadata, CapabilitySource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpMetadata.class);
    private static final int CACHE_LIMIT = 4;

    private final MrpProtocol protocol;
    private final PlayerStateManager psm;
    private final @Nullable String identifier;
    private final HttpClient httpClient;
    private final Clock clock;
    private final org.openhab.binding.atv.internal.client.core.DeviceLoop loop;

    // LRU cache: a hit refreshes recency before eviction
    private final org.openhab.binding.atv.internal.client.support.Cache<String, ArtworkInfo> artworkCache = new org.openhab.binding.atv.internal.client.support.Cache<>(
            CACHE_LIMIT);

    /**
     * Creates a new metadata instance.
     *
     * @param protocol protocol used for artwork requests
     * @param psm player state manager providing the current state
     * @param identifier unique identifier of the device, or {@code null}
     * @param clock wall clock used for position estimation
     * @param loop device loop on which all player state is confined
     */
    public MrpMetadata(MrpProtocol protocol, PlayerStateManager psm, @Nullable String identifier, Clock clock,
            org.openhab.binding.atv.internal.client.core.DeviceLoop loop) {
        this.protocol = protocol;
        this.psm = psm;
        this.identifier = identifier;
        this.clock = clock;
        this.loop = loop;
        this.httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    }

    /**
     * Runs a player-state read on the device loop (or inline when already on it); all
     * {@code PlayerStateManager} access must happen on that loop.
     */
    private <T> T onLoop(java.util.function.Supplier<T> task) {
        if (loop.inLoop()) {
            return task.get();
        }
        return loop.submit(task).join();
    }

    @Override
    public Optional<String> deviceId() {
        return Optional.ofNullable(identifier);
    }

    @Override
    public CompletableFuture<Playing> playing() {
        // PSM state is mutated on the device loop only; build the snapshot there too
        if (loop.inLoop()) {
            return CompletableFuture.completedFuture(MrpPlaying.buildPlayingInstance(psm.playing(), clock));
        }
        return loop.submit(() -> MrpPlaying.buildPlayingInstance(psm.playing(), clock));
    }

    @Override
    public Optional<App> app() {
        PlayerStateManager.Client client = onLoop(psm::client);
        if (client != null) {
            return Optional.of(new App(client.displayName(), client.bundleIdentifier()));
        }
        return Optional.empty();
    }

    @Override
    public @Nullable String artworkId() {
        return onLoop(() -> artworkIdFor(psm.playing()));
    }

    /** Computes the artwork identifier for a player state; must run on the device loop. */
    private static @Nullable String artworkIdFor(PlayerStateManager.PlayerState playing) {
        @Nullable
        ContentItemMetadata metadata = playing.metadata();
        if (metadata != null && (metadata.getArtworkAvailable() || metadata.hasArtworkURL())) {
            if (metadata.hasArtworkIdentifier()) {
                return metadata.getArtworkIdentifier();
            }
            if (metadata.hasContentIdentifier()) {
                return metadata.getContentIdentifier();
            }
            return playing.itemIdentifier();
        }
        return null;
    }

    /**
     * Immutable snapshot of everything the artwork fetch path needs, taken in one loop task
     * so the blocking fetch never touches loop-confined player state again.
     */
    private record ArtworkSnapshot(@Nullable String artworkId, @Nullable ContentItemMetadata metadata, int location) {
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<ArtworkInfo> artwork(@Nullable Integer width, @Nullable Integer height) {
        CompletableFuture<ArtworkInfo> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-artwork").start(() -> {
            try {
                // Metadata#artwork is declared to complete with a non-null ArtworkInfo, but
                // "no artwork available" is a legitimate outcome (see the capability javadoc);
                // completeNullable is the single unavoidable spot where that contradiction
                // surfaces.
                MrpFutures.completeNullable(result, artworkBlocking(width, height));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    private @Nullable ArtworkInfo artworkBlocking(@Nullable Integer width, @Nullable Integer height) {
        // Snapshot everything from loop-confined player state in a single loop task; the
        // artwork thread is a virtual thread, so joining the loop future is fine
        ArtworkSnapshot snapshot = onLoop(() -> {
            PlayerStateManager.PlayerState playing = psm.playing();
            return new ArtworkSnapshot(artworkIdFor(playing), playing.metadata(), playing.location());
        });
        @Nullable
        String artworkIdentifier = snapshot.artworkId();
        if (artworkIdentifier == null) {
            LOGGER.debug("No artwork available");
            return null;
        }

        if (artworkCache.contains(artworkIdentifier)) {
            LOGGER.debug("Retrieved artwork {} from cache", artworkIdentifier);
            return artworkCache.get(artworkIdentifier);
        }

        @Nullable
        ArtworkInfo artwork;
        try {
            artwork = fetchArtwork(snapshot, width == null ? 0 : width, height == null ? -1 : height);
        } catch (Exception e) {
            LOGGER.debug("Artwork not present in response");
            LOGGER.debug("Artwork fetch failed", e);
            return null;
        }
        if (artwork != null) {
            artworkCache.put(artworkIdentifier, artwork);
        }
        return artwork;
    }

    private @Nullable ArtworkInfo fetchArtwork(ArtworkSnapshot snapshot, int width, int height) {
        ArtworkInfo remote = fetchRemoteArtwork(snapshot.metadata(), width, height);
        if (remote != null) {
            return remote;
        }
        return fetchLocalArtwork(snapshot, width, height);
    }

    /** Fetches external artwork from a URL. */
    private @Nullable ArtworkInfo fetchRemoteArtwork(@Nullable ContentItemMetadata metadata, int width, int height) {
        if (metadata == null) {
            return null;
        }

        java.util.List<String> urls = new java.util.ArrayList<>();

        if (metadata.hasArtworkIdentifier()) {
            // Appears to be a template to iTunes artwork, but let's validate
            @Nullable
            String url = formatUrlTemplate(metadata.getArtworkIdentifier(),
                    // the iTunes image server preserves aspect ratio
                    width < 1 ? 999999 : width, height < 1 ? 999999 : height);
            if (url != null && isUrl(url)) {
                urls.add(url);
            }
        }

        if (metadata.hasArtworkURL()) {
            // artworkURL has fixed size and format, use it as a fallback
            urls.add(metadata.getArtworkURL());
        }

        for (String url : urls) {
            try {
                HttpResponse<byte[]> response = httpClient.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                if (response.statusCode() == 200) {
                    return new ArtworkInfo(response.body(), response.headers().firstValue("content-type").orElse(null),
                            width, height);
                }
            } catch (Exception e) {
                LOGGER.debug("Failed to fetch artwork from {}", url, e);
            }
        }
        return null;
    }

    /** Fetches artwork over MRP. */
    private @Nullable ArtworkInfo fetchLocalArtwork(ArtworkSnapshot snapshot, int width, int height) {
        int location = snapshot.location();
        ProtocolMessage response = protocol.sendAndReceive(MrpMessages.playbackQueueRequest(location, width, height))
                .join();
        if (!response.hasType()) {
            return null;
        }

        SetStateMessage inner = (SetStateMessage) MrpExtensions.extractInner(response);
        if (inner.getPlaybackQueue().getContentItemsCount() <= location) {
            // Indexing out of range here surfaces as an "artwork not present" error to
            // the caller, same as any other artwork fetch failure
            throw new IllegalStateException("no content item at location " + location);
        }
        ContentItem item = inner.getPlaybackQueue().getContentItems(location);
        ContentItemMetadata itemMetadata = snapshot.metadata();
        return new ArtworkInfo(item.getArtworkData().toByteArray(),
                itemMetadata == null ? null : itemMetadata.getArtworkMIMEType(), item.getArtworkDataWidth(),
                item.getArtworkDataHeight());
    }

    /**
     * Substitutes {@code {w}}, {@code {h}}, {@code {c}} and {@code {f}} placeholders in
     * the iTunes artwork URL template. Returns {@code null} if any placeholder is left
     * unresolved.
     */
    private static @Nullable String formatUrlTemplate(String template, int width, int height) {
        String url = template.replace("{w}", String.valueOf(width)).replace("{h}", String.valueOf(height))
                .replace("{c}", "bb").replace("{f}", "png");
        if (url.contains("{") && url.contains("}")) {
            return null;
        }
        return url;
    }

    /** Returns whether the string is an http(s) URL with a host. */
    private static boolean isUrl(@Nullable String url) {
        if (url == null) {
            return false;
        }
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.METADATA_DEVICE_ID, Capability.METADATA_ARTWORK, Capability.METADATA_ARTWORK_ID,
                Capability.METADATA_PLAYING, Capability.METADATA_APP);
    }
}
