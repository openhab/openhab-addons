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

/**
 * Completes {@code CompletableFuture<Void>} futures, which can only hold {@code null}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class MrpFutures {

    private MrpFutures() {
    }

    /** Completes {@code future} successfully. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static void completeVoid(CompletableFuture<Void> future) {
        ((CompletableFuture) future).complete(null);
    }
}
