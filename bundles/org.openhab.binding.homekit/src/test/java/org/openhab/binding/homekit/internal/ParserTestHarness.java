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
package org.openhab.binding.homekit.internal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser.HttpPayload;
import org.openhab.binding.homekit.internal.session.HttpReaderListener;

/**
 * Test helper class for {@link org.openhab.binding.homekit.internal.session.HttpPayloadParser}
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ParserTestHarness implements HttpReaderListener {

    private final AtomicReference<@Nullable CompletableFuture<HttpPayload>> nextPayload = new AtomicReference<>();

    @Override
    public void onHttpPayload(HttpPayload payload) {
        CompletableFuture<HttpPayload> future = nextPayload.getAndSet(null);
        if (future != null) {
            future.complete(payload);
        }
    }

    @Override
    public void onHttpReaderError(Throwable error) {
        CompletableFuture<HttpPayload> future = nextPayload.getAndSet(null);
        if (future != null) {
            future.completeExceptionally(error);
        }
    }

    @Override
    public void onHttpReaderClose(byte[] remainingData) {
        // no-op for tests
    }

    CompletableFuture<HttpPayload> expectPayload() {
        CompletableFuture<HttpPayload> future = new CompletableFuture<>();
        nextPayload.set(future);
        return future;
    }
}
