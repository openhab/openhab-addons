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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;

/**
 * Common state shared by all capability interface implementations: the {@link Relayer} selecting the protocol instance
 * calls are forwarded to, and the device-wide {@link Guard} blocking access after close.
 *
 * <p>
 * Java's single inheritance means relays can't extend {@link Relayer} directly, so they extend this small base
 * class instead and hold the relayer by composition.
 *
 * @param <T> capability interface type relayed by the relay
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
abstract class BaseRelay<T> {

    final Relayer<T> relayer;
    final Guard guard;

    BaseRelay(Relayer<T> relayer, Guard guard) {
        this.relayer = Objects.requireNonNull(relayer, "relayer");
        this.guard = Objects.requireNonNull(guard, "guard");
    }

    /**
     * Registers a protocol instance with the underlying relayer (called by {@code AppleTVRelay.connect()} for the
     * interfaces contributed in {@link SetupData#interfaces()}).
     *
     * @param instance protocol implementation of this relay's interface
     * @param protocol protocol the instance belongs to
     * @throws ClassCastException if the instance does not implement the capability interface
     */
    void registerInstance(Object instance, Protocol protocol) {
        relayer.register(relayer.baseInterface().cast(instance), protocol);
    }

    /**
     * Performs a takeover of this relay by a protocol (see {@link Relayer#takeover(Protocol)}).
     *
     * @param protocol protocol taking over
     */
    void takeover(Protocol protocol) {
        relayer.takeover(protocol);
    }

    /**
     * Releases a takeover of this relay (see {@link Relayer#release()}).
     */
    void release() {
        relayer.release();
    }

    /**
     * Relays an asynchronous call, converting a synchronous {@link NotSupportedError} from instance selection into
     * a failed future (project convention for {@code CompletableFuture} APIs).
     *
     * @param <R> result type
     * @param capability capability to relay
     * @param call invocation applied to the selected instance
     * @return future from the selected instance, or a future failed with {@link NotSupportedError}
     */
    final <R> CompletableFuture<R> relayAsync(Capability capability, Function<T, CompletableFuture<R>> call) {
        T instance;
        try {
            instance = relayer.relay(capability);
        } catch (NotSupportedError e) {
            return CompletableFuture.failedFuture(e);
        }
        return call.apply(instance);
    }
}
