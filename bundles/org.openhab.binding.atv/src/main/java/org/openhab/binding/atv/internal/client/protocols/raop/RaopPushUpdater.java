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

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.Metadata;
import org.openhab.binding.atv.internal.client.capability.PushListener;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.core.ProtocolStateDispatcher;
import org.openhab.binding.atv.internal.client.core.UpdatedState;
import org.openhab.binding.atv.internal.client.dto.Playing;
import org.openhab.binding.atv.internal.client.exceptions.NoAsyncListenerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of push update support for RAOP.
 *
 * <p>
 * A new state is only published when it differs from the previous one, and every
 * published state is also dispatched as {@link UpdatedState#PLAYING}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class RaopPushUpdater implements PushUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaopPushUpdater.class);

    private final Metadata metadata;
    private final ProtocolStateDispatcher stateDispatcher;
    private final List<PushListener> listeners = new CopyOnWriteArrayList<>();

    private volatile boolean activated;
    private volatile @Nullable Playing previousState;

    /**
     * Creates a new push updater.
     *
     * @param metadata metadata implementation used to build play states
     * @param stateDispatcher dispatcher for internal state updates
     */
    public RaopPushUpdater(Metadata metadata, ProtocolStateDispatcher stateDispatcher) {
        this.metadata = metadata;
        this.stateDispatcher = stateDispatcher;
    }

    @Override
    public boolean active() {
        return activated;
    }

    @Override
    public void start(Duration initialDelay) {
        if (listeners.isEmpty()) {
            throw new NoAsyncListenerError("no listener set");
        }
        activated = true;
        Thread.ofVirtual().name("raop-push-initial").start(this::stateUpdated);
    }

    @Override
    public void stop() {
        activated = false;
    }

    @Override
    public void addListener(PushListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    @Override
    public void removeListener(PushListener listener) {
        listeners.remove(listener);
    }

    /**
     * State was updated, call listeners.
     */
    public void stateUpdated() {
        try {
            Playing playing = metadata.playing().join();
            postUpdate(playing);
        } catch (Exception ex) {
            LOGGER.debug("Playstatus error occurred: {}", ex.toString());
        }
    }

    /** Publishes an update, but only if the state actually changed. */
    private synchronized void postUpdate(Playing playing) {
        if (!playing.equals(previousState)) {
            stateDispatcher.dispatch(UpdatedState.PLAYING, playing);
            for (PushListener listener : listeners) {
                listener.playstatusUpdate(this, playing);
            }
        }
        previousState = playing;
    }
}
