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
package org.openhab.binding.atv.internal.handler;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.exceptions.ConnectionLostError;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;

/**
 * What a failed connection attempt says about whether the device is on the network. Ordered from the
 * strongest evidence to the weakest.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
enum Reachability {
    /** The device took part in the exchange, so it is on the network even though the attempt failed. */
    ANSWERED,
    /** The device refused or dropped the connection, so its network stack is up. */
    HOST_UP,
    /** Nothing answered: a timeout, no route to the device, or a host name that no longer resolves. */
    NO_ANSWER;

    // Bounds the walk over causes and suppressed failures in case of a cycle.
    private static final int MAX_DEPTH = 8;

    /**
     * Classifies a failure. A connect that tried several protocols carries each protocol's failure as a
     * suppressed exception, and the strongest evidence among them wins. A network error or timeout that
     * carries suppressed exceptions is classified by itself, since those are usually failures to close it.
     *
     * @param failure the failure, possibly wrapped in an ExecutionException or CompletionException
     * @return the reachability it shows
     */
    static Reachability of(Throwable failure) {
        return of(failure, 0);
    }

    private static Reachability of(Throwable failure, int depth) {
        Throwable cause = failure.getCause();
        Throwable[] suppressed = failure.getSuppressed();
        if (depth >= MAX_DEPTH || (cause == null && (suppressed.length == 0 || ofRoot(failure) != ANSWERED))) {
            return ofRoot(failure);
        }
        Reachability best = cause != null ? of(cause, depth + 1) : NO_ANSWER;
        for (Throwable each : suppressed) {
            Reachability r = of(each, depth + 1);
            if (r.ordinal() < best.ordinal()) {
                best = r;
            }
        }
        return best;
    }

    private static Reachability ofRoot(@Nullable Throwable root) {
        if (root instanceof SocketTimeoutException || root instanceof NoRouteToHostException
                || root instanceof PortUnreachableException || root instanceof HttpTimeoutException
                || root instanceof UnknownHostException || root instanceof TimeoutException
                || root instanceof OperationTimeoutError) {
            return NO_ANSWER;
        }
        if (root instanceof IOException || root instanceof ConnectionLostError) {
            return HOST_UP;
        }
        return ANSWERED;
    }
}
