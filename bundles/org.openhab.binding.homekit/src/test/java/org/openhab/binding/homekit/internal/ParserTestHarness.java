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
import org.openhab.binding.homekit.internal.session.HttpPayloadParser;
import org.openhab.binding.homekit.internal.session.HttpPayloadParser.HttpPayload;

/**
 * Test helper class for {@link HttpPayloadParser}
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ParserTestHarness {

    private final AtomicReference<@Nullable CompletableFuture<HttpPayload>> nextPayload = new AtomicReference<>();

    ParserTestHarness(HttpPayloadParser httpPayloadParser) {
        httpPayloadParser.setPayloadHandler(payload -> {
            CompletableFuture<HttpPayload> futureHttpPayload = nextPayload.getAndSet(null);
            if (futureHttpPayload != null) {
                futureHttpPayload.complete(payload);
            }
        });
        httpPayloadParser.setErrorHandler(error -> {
            CompletableFuture<HttpPayload> futureHttpPayload = nextPayload.getAndSet(null);
            if (futureHttpPayload != null) {
                futureHttpPayload.completeExceptionally(error);
            }
        });
    }

    CompletableFuture<HttpPayload> expectPayload() {
        CompletableFuture<HttpPayload> futureHttpPayload = new CompletableFuture<>();
        nextPayload.set(futureHttpPayload);
        return futureHttpPayload;
    }
}
