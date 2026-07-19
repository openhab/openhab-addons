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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.PowerListener;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * Relay implementation for retrieving and changing the power state of an Apple TV.
 *
 * <p>
 * It uses an overridden priority order generally favoring Companion, as it implements power management better
 * than MRP. The relay also acts as {@link PowerListener} of the main registered power instance
 * ({@code AppleTVRelay.connect()} wires this up) and forwards state updates to its own listeners, deduplicating
 * repeated updates to the same state (protocol implementations already deduplicate; the relay also suppresses
 * cross-protocol repetition).
 *
 * <p>
 * The {@link CoreStateDispatcher} constructor parameter is unused here; it is kept for signature parity with the
 * other stateful relays.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class PowerRelay extends BaseRelay<Power> implements Power, PowerListener {

    /**
     * Priority override used by this relay.
     */
    public static final List<Protocol> OVERRIDE_PRIORITIES = List.of(Protocol.Companion, Protocol.MRP, Protocol.AirPlay,
            Protocol.RAOP);

    private final ListenerRegistry<PowerListener> listeners;
    private final AtomicReference<PowerState> lastReportedState = new AtomicReference<>();

    /**
     * Creates a new relay power instance.
     *
     * @param guard device guard blocking calls after close
     * @param coreDispatcher per-device state dispatcher (unused, kept for signature parity)
     * @param loop device loop used for listener notification
     */
    public PowerRelay(Guard guard, CoreStateDispatcher coreDispatcher, DeviceLoop loop) {
        super(new Relayer<>(Power.class, OVERRIDE_PRIORITIES), guard);
        this.listeners = new ListenerRegistry<>(loop);
    }

    /**
     * Subscribes this relay as listener of the main registered power instance so protocol power updates are
     * forwarded to the relay's listeners.
     *
     * @throws NotSupportedError if no power instance is registered
     */
    void wireMainInstance() {
        relayer.mainInstance().addListener(this);
    }

    @Override
    public void powerstateUpdate(PowerState oldState, PowerState newState) {
        // Do not forward duplicate updates to the same state
        PowerState last = lastReportedState.getAndSet(newState);
        if (last == newState) {
            return;
        }
        listeners.fire(listener -> listener.powerstateUpdate(oldState, newState));
    }

    @Override
    public PowerState powerState() {
        guard.requireNotBlocked("powerState");
        return relayer.relay(Capability.POWER_STATE, OVERRIDE_PRIORITIES).powerState();
    }

    @Override
    public CompletableFuture<Void> turnOn(boolean awaitNewState) {
        guard.requireNotBlocked("turnOn");
        try {
            return relayer.relay(Capability.POWER_TURN_ON, OVERRIDE_PRIORITIES).turnOn(awaitNewState);
        } catch (NotSupportedError e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public CompletableFuture<Void> turnOff(boolean awaitNewState) {
        guard.requireNotBlocked("turnOff");
        try {
            return relayer.relay(Capability.POWER_TURN_OFF, OVERRIDE_PRIORITIES).turnOff(awaitNewState);
        } catch (NotSupportedError e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public void addListener(PowerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(PowerListener listener) {
        listeners.remove(listener);
    }
}
