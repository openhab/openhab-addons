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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A simple settable event: waiters obtain a future that completes when the event is set;
 * {@code set()} immediately followed by {@code clear()} acts as a pulse waking current
 * waiters only.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class AsyncEvent {

    private CompletableFuture<@Nullable Void> future = new CompletableFuture<>();

    /** Sets the event, waking all current waiters. */
    synchronized void set() {
        future.complete(null);
    }

    /** Clears the event so future waiters wait for the next {@link #set()}. */
    synchronized void clear() {
        if (future.isDone()) {
            future = new CompletableFuture<>();
        }
    }

    /**
     * Returns a future completing when the event is (or already was) set.
     */
    synchronized CompletableFuture<@Nullable Void> waitFuture() {
        return future;
    }
}
