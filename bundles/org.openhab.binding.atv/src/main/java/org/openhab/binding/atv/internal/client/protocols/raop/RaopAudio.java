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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.AudioListener;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.ProtocolStateDispatcher;
import org.openhab.binding.atv.internal.client.core.StateMessage;
import org.openhab.binding.atv.internal.client.core.UpdatedState;
import org.openhab.binding.atv.internal.client.dto.OutputDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of audio functionality for RAOP.
 *
 * <p>
 * The volume is tracked in the {@link StreamContext} as dBFS and exposed as percent;
 * when no volume has ever been set, {@link #INITIAL_VOLUME} is reported. Volume changes
 * dispatched by other protocols are blindly trusted and stored in the context.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopAudio implements Audio, CapabilitySource {

    /** Initial volume in percent. */
    public static final double INITIAL_VOLUME = 33.0;

    private static final Logger LOGGER = LoggerFactory.getLogger(RaopAudio.class);

    private final RaopPlaybackManager playbackManager;
    private final ProtocolStateDispatcher stateDispatcher;
    private final List<AudioListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a new audio instance.
     *
     * @param playbackManager playback manager holding the stream state
     * @param stateDispatcher dispatcher for internal state updates
     */
    public RaopAudio(RaopPlaybackManager playbackManager, ProtocolStateDispatcher stateDispatcher) {
        this.playbackManager = playbackManager;
        this.stateDispatcher = stateDispatcher;
        stateDispatcher.listenTo(UpdatedState.VOLUME, this::volumeChanged);
    }

    // Intercept volume changes by other protocols and update accordingly. We blindly
    // trust any volume we see here as it's a much better guess than we have.
    private void volumeChanged(StateMessage message) {
        double volume = ((Number) message.value()).doubleValue();
        LOGGER.debug("Protocol {} changed volume to {}", message.protocol(), volume);
        playbackManager.context().volume = RaopVolume.pctToDbfs(volume);
    }

    /** Returns whether the volume has changed from the default or not. */
    public boolean hasChangedVolume() {
        return playbackManager.context().volume != null;
    }

    @Override
    public double volume() {
        Double vol = playbackManager.context().volume;
        if (vol == null) {
            return INITIAL_VOLUME;
        }
        return RaopVolume.dbfsToPct(vol);
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Void> setVolume(double level, @Nullable OutputDevice outputDevice) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("raop-set-volume").start(() -> {
            try {
                RaopStreamClient raop = playbackManager.streamClient();
                double dbfsVolume = RaopVolume.pctToDbfs(level);

                if (raop != null) {
                    raop.setVolume(dbfsVolume);
                } else {
                    playbackManager.context().volume = dbfsVolume;
                }

                stateDispatcher.dispatch(UpdatedState.VOLUME, volume());
                RaopFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    @Override
    public CompletableFuture<Void> volumeUp() {
        return setVolume(Math.min(volume() + 5.0, 100.0), null);
    }

    @Override
    public CompletableFuture<Void> volumeDown() {
        return setVolume(Math.max(volume() - 5.0, 0.0), null);
    }

    @Override
    public void addListener(AudioListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(AudioListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.AUDIO_VOLUME, Capability.AUDIO_SET_VOLUME, Capability.AUDIO_VOLUME_UP,
                Capability.AUDIO_VOLUME_DOWN);
    }
}
