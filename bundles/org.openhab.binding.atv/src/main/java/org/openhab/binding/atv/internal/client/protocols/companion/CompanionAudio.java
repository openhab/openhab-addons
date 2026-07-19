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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.AudioListener;
import org.openhab.binding.atv.internal.client.core.Capability;
import org.openhab.binding.atv.internal.client.core.CapabilitySource;
import org.openhab.binding.atv.internal.client.core.Core;
import org.openhab.binding.atv.internal.client.core.ListenerRegistry;
import org.openhab.binding.atv.internal.client.core.UpdatedState;
import org.openhab.binding.atv.internal.client.dto.OutputDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the audio API for Companion.
 *
 * <p>
 * The volume is refreshed via {@code GetVolume} whenever an {@code _iMC} event announces
 * volume control availability; setting or nudging the volume waits for that refresh
 * (5 second timeout) before completing.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class CompanionAudio implements Audio, CapabilitySource {

    private static final Duration VOLUME_EVENT_TIMEOUT = Duration.ofSeconds(5);

    private static final Logger LOGGER = LoggerFactory.getLogger(CompanionAudio.class);

    private final CompanionApi api;
    private final Core core;
    private final ListenerRegistry<AudioListener> listeners;
    private final Object volumeEventLock = new Object();

    private CompletableFuture<@Nullable Void> volumeEvent = new CompletableFuture<>();
    private volatile double volume;

    /**
     * Creates a new instance.
     *
     * @param api Companion API
     * @param core protocol context
     */
    public CompanionAudio(CompanionApi api, Core core) {
        this.api = api;
        this.core = core;
        this.listeners = new ListenerRegistry<>(core.loop());
        api.listenTo("_iMC", this::handleControlFlagUpdate);
    }

    private void handleControlFlagUpdate(Map<String, Object> data) {
        Long controlFlagsValue = CompanionProtocol.toLong(data.get("_mcF"));
        long controlFlags = controlFlagsValue == null ? MediaControlFlags.NO_CONTROLS : controlFlagsValue;
        if ((controlFlags & MediaControlFlags.VOLUME) != 0) {
            LOGGER.debug("Volume control changed, updating volume");
            api.mediaControlCommand(MediaControlCommand.GetVolume, null).whenComplete((response, error) -> {
                if (error != null) {
                    LOGGER.warn("Failed to fetch volume", error);
                    return;
                }
                Map<String, Object> content = CompanionApi.content(response);
                if (content == null) {
                    LOGGER.debug("Missing content in GetVolume response");
                    return;
                }
                volume = ((Number) content.get("_vol")).doubleValue() * 100.0;
                LOGGER.debug("Volume changed to {}", volume);
                setVolumeEvent();
                core.stateDispatcher().dispatch(UpdatedState.VOLUME, volume);
            });
        } else {
            // No volume control means we know nothing about the volume
            volume = 0.0;
            core.stateDispatcher().dispatch(UpdatedState.VOLUME, volume);
        }
    }

    private void setVolumeEvent() {
        synchronized (volumeEventLock) {
            volumeEvent.complete(null);
        }
    }

    private CompletableFuture<@Nullable Void> clearVolumeEvent() {
        synchronized (volumeEventLock) {
            if (volumeEvent.isDone()) {
                volumeEvent = new CompletableFuture<>();
            }
            return volumeEvent;
        }
    }

    @Override
    public double volume() {
        return volume;
    }

    @Override
    public CompletableFuture<Void> setVolume(double level, @Nullable OutputDevice outputDevice) {
        CompletableFuture<@Nullable Void> event = clearVolumeEvent();
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("_vol", level / 100.0);
        return api.mediaControlCommand(MediaControlCommand.SetVolume, args).thenCompose(response -> awaitEvent(event));
    }

    @Override
    public CompletableFuture<Void> volumeUp() {
        CompletableFuture<@Nullable Void> event = clearVolumeEvent();
        return api.hidCommand(true, HidCommand.VolumeUp)
                .thenCompose(unused -> api.hidCommand(false, HidCommand.VolumeUp))
                .thenCompose(unused -> awaitEvent(event));
    }

    @Override
    public CompletableFuture<Void> volumeDown() {
        CompletableFuture<@Nullable Void> event = clearVolumeEvent();
        return api.hidCommand(true, HidCommand.VolumeDown)
                .thenCompose(unused -> api.hidCommand(false, HidCommand.VolumeDown))
                .thenCompose(unused -> awaitEvent(event));
    }

    private static CompletableFuture<Void> awaitEvent(CompletableFuture<@Nullable Void> event) {
        return event.orTimeout(VOLUME_EVENT_TIMEOUT.toNanos(), TimeUnit.NANOSECONDS).thenRun(() -> {
        });
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
