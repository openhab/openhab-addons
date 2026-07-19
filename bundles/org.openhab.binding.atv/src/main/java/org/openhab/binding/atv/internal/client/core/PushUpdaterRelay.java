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
package org.openhab.binding.atv.internal.client.core;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.PushListener;
import org.openhab.binding.atv.internal.client.capability.PushUpdater;
import org.openhab.binding.atv.internal.client.dto.Playing;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * Relay implementation for push/async updates from an Apple TV.
 *
 * <p>
 * Unlike the other relays, calls are not relayed per capability: {@link #start(Duration)} and {@link #stop()}
 * are applied to <em>all</em> registered updaters (otherwise updates would not be pushed after a takeover, as
 * the protocol taken over was never started),
 * with this relay registered as their listener. Incoming updates are only forwarded to the external listeners
 * when they originate from the main instance (takeover-aware highest priority), and consecutive identical
 * {@link Playing} states are deduplicated.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class PushUpdaterRelay extends BaseRelay<PushUpdater> implements PushUpdater, PushListener {

    private final ListenerRegistry<PushListener> listeners;
    private final AtomicReference<Playing> lastForwarded = new AtomicReference<>();

    /**
     * Creates a new relay push updater.
     *
     * @param guard device guard blocking calls after close
     * @param loop device loop used for listener notification
     */
    public PushUpdaterRelay(Guard guard, DeviceLoop loop) {
        // Push updaters are selected per instance (not capability-relayed), so instances are not required to
        // implement CapabilitySource
        super(new Relayer<>(PushUpdater.class, AppleTVRelay.DEFAULT_PRIORITIES, false), guard);
        this.listeners = new ListenerRegistry<>(loop);
    }

    /**
     * Returns the number of registered push updater instances; used by {@link FeaturesRelay} for the
     * {@code PushUpdates} special case.
     *
     * @return number of registered instances
     */
    int count() {
        return relayer.count();
    }

    @Override
    public boolean active() {
        guard.requireNotBlocked("active");
        return relayer.mainInstance().active();
    }

    @Override
    public void start(Duration initialDelay) {
        guard.requireNotBlocked("start");
        for (PushUpdater instance : relayer.instances()) {
            instance.removeListener(this);
            instance.addListener(this);
            instance.start(initialDelay);
        }
    }

    @Override
    public void stop() {
        guard.requireNotBlocked("stop");
        for (PushUpdater instance : relayer.instances()) {
            instance.removeListener(this);
            instance.stop();
        }
    }

    @Override
    public void playstatusUpdate(PushUpdater updater, Playing playstatus) {
        if (!isMainInstance(updater)) {
            return;
        }
        // Push updaters shall only publish updates when state actually changes; suppress duplicates that may
        // arise from switching between instances reporting the same state
        if (Objects.equals(lastForwarded.getAndSet(playstatus), playstatus)) {
            return;
        }
        listeners.fire(listener -> listener.playstatusUpdate(updater, playstatus));
    }

    @Override
    public void playstatusError(PushUpdater updater, Exception exception) {
        if (!isMainInstance(updater)) {
            return;
        }
        listeners.fire(listener -> listener.playstatusError(updater, exception));
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private boolean isMainInstance(PushUpdater updater) {
        try {
            return relayer.mainInstance() == updater;
        } catch (NotSupportedError e) {
            return false;
        }
    }

    @Override
    public void addListener(PushListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PushListener listener) {
        listeners.remove(listener);
    }
}
