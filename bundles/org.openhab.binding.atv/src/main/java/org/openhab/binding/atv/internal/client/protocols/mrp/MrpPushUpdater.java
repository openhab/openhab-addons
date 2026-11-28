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

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.capability.PushListener;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.core.ProtocolStateDispatcher;
import org.openhab.binding.atv.internal.client.core.UpdatedState;
import org.openhab.binding.atv.internal.client.dto.Playing;
import org.openhab.binding.atv.internal.client.exceptions.NoAsyncListenerError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the push update API for MRP: a new state is only published when it
 * differs from the previous one, and every published state is also dispatched as
 * {@link UpdatedState#PLAYING} on the protocol state dispatcher.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpPushUpdater implements PushUpdater, PlayerStateManager.Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MrpPushUpdater.class);

    private final MrpMetadata metadata;
    private final PlayerStateManager psm;
    private final ProtocolStateDispatcher stateDispatcher;
    private final org.openhab.binding.atv.internal.client.core.DeviceLoop loop;
    private final List<PushListener> listeners = new CopyOnWriteArrayList<>();

    private volatile @Nullable Playing previousState;

    /**
     * Creates a new push updater.
     *
     * @param metadata metadata implementation used to build play states
     * @param psm player state manager to observe
     * @param stateDispatcher dispatcher for internal state updates
     * @param loop device loop the initial update runs on
     */
    public MrpPushUpdater(MrpMetadata metadata, PlayerStateManager psm, ProtocolStateDispatcher stateDispatcher,
            org.openhab.binding.atv.internal.client.core.DeviceLoop loop) {
        this.metadata = metadata;
        this.psm = psm;
        this.stateDispatcher = stateDispatcher;
        this.loop = loop;
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public boolean active() {
        return psm.listener() == this;
    }

    @Override
    public void start(Duration initialDelay) {
        if (listeners.isEmpty()) {
            throw new NoAsyncListenerError("no listener set");
        }
        if (active()) {
            return;
        }
        psm.setListener(this);
        // Run the initial update on the device loop; the routine push path (PSM listener
        // callbacks) already runs there, so all state reads stay loop-confined
        loop.execute(this::stateUpdated);
    }

    @Override
    public void stop() {
        psm.setListener(null);
    }

    @Override
    public void addListener(PushListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    @Override
    public void removeListener(PushListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void stateUpdated() {
        try {
            Playing playstatus = metadata.playing().join();
            postUpdate(playstatus);
        } catch (Exception ex) {
            LOGGER.debug("Playstatus error occurred: {}", ex.toString());
            for (PushListener listener : listeners) {
                listener.playstatusError(this, ex);
            }
        }
    }

    /** Publishes an update, but only if the state actually changed. */
    private void postUpdate(Playing playing) {
        if (!playing.equals(previousState)) {
            stateDispatcher.dispatch(UpdatedState.PLAYING, playing);
            for (PushListener listener : listeners) {
                listener.playstatusUpdate(this, playing);
            }
        }
        previousState = playing;
    }
}
