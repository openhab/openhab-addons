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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;

/**
 * Small helper for the RAOP data plane, which runs on virtual threads and consumes the
 * {@code CompletableFuture} based RTSP APIs synchronously.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class RaopFutures {

    private RaopFutures() {
    }

    /**
     * Awaits a future, unwrapping {@link CompletionException} so the original exception
     * type (e.g. {@code AuthenticationError}) propagates to the caller.
     */
    static <T> T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new ProtocolError("operation failed", cause == null ? e : cause);
        } catch (CancellationException e) {
            throw new ProtocolError("operation cancelled", e);
        }
    }

    /**
     * Completes {@code future} successfully. {@code Void} has no non-null instance, so
     * completing such a future can only ever be done with {@code null}; the raw type cast
     * isolates that single unavoidable null assignment (which the null analysis cannot be
     * suppressed for, since it is promoted to an error) behind one helper instead of
     * scattering casts across every call site.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static void completeVoid(CompletableFuture<Void> future) {
        ((CompletableFuture) future).complete(null);
    }
}
