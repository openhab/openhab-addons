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
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.Protocol;

/**
 * Protocol-scoped view of a {@link CoreStateDispatcher} that stamps every dispatched
 * message with the owning protocol.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class ProtocolStateDispatcher {

    private final Protocol protocol;
    private final CoreStateDispatcher coreDispatcher;

    /**
     * Creates a dispatcher stamping messages with the given protocol.
     *
     * @param protocol protocol stamped on dispatched messages
     * @param coreDispatcher underlying per-device dispatcher
     */
    public ProtocolStateDispatcher(Protocol protocol, CoreStateDispatcher coreDispatcher) {
        this.protocol = Objects.requireNonNull(protocol, "protocol");
        this.coreDispatcher = Objects.requireNonNull(coreDispatcher, "coreDispatcher");
    }

    /**
     * Returns a copy of this dispatcher stamping a different protocol. Used when one
     * protocol tunnels another (e.g. MRP over AirPlay).
     *
     * @param protocol protocol for the copy
     * @return new dispatcher sharing the same core dispatcher
     */
    public ProtocolStateDispatcher createCopy(Protocol protocol) {
        return new ProtocolStateDispatcher(protocol, coreDispatcher);
    }

    /**
     * Registers a listener for all updates of a state.
     *
     * @param state state to listen to
     * @param listener callback invoked on the device loop
     * @return registration handle for removal
     */
    public MessageDispatcher.Registration listenTo(UpdatedState state, Consumer<StateMessage> listener) {
        return coreDispatcher.listenTo(state, listener);
    }

    /**
     * Registers a listener for updates of a state matching a filter.
     *
     * @param state state to listen to
     * @param listener callback invoked on the device loop
     * @param filter predicate evaluated at dispatch time
     * @return registration handle for removal
     */
    public MessageDispatcher.Registration listenTo(UpdatedState state, Consumer<StateMessage> listener,
            Predicate<StateMessage> filter) {
        return coreDispatcher.listenTo(state, listener, filter);
    }

    /**
     * Dispatches a new value for a state, stamped with this dispatcher's protocol.
     *
     * @param state state that was updated
     * @param value new value
     * @return future completed once every matching listener has been invoked
     */
    public CompletableFuture<Void> dispatch(UpdatedState state, Object value) {
        return coreDispatcher.dispatch(state, new StateMessage(protocol, state, value));
    }
}
