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
 * Helper for completing plain (non-{@code @Nullable}) {@code CompletableFuture<T>} futures
 * declared by the capability interfaces with a value that is genuinely allowed to be
 * {@code null} per their javadoc (e.g. {@code Void}, which has no non-null instance at all,
 * or {@code ArtworkInfo} when no artwork is available). This isolates that single
 * unavoidable null assignment behind one helper instead of scattering unchecked casts
 * across every call site.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class MrpFutures {

    private MrpFutures() {
    }

    /** Completes {@code future} successfully. */
    static void completeVoid(CompletableFuture<Void> future) {
        completeNullable(future, null);
    }

    /** Completes {@code future} with a value that may legitimately be {@code null}. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static <T> void completeNullable(CompletableFuture<T> future, @Nullable T value) {
        ((CompletableFuture) future).complete(value);
    }
}
