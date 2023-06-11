/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.handler;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Instead of continously rewriting the termination of future a small util method was added
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@NonNullByDefault
public class FutureUtil {
    private FutureUtil() {
        // this is a util no instances should be created
    }

    /**
     * Cancel the future
     *
     * - when it is not null
     * - and it is not already cancelled
     *
     * interrupt if still/already running
     *
     * @param future nullable future to be cancelled
     */
    public static void cancel(@Nullable Future<?> future) {
        cancel(future, true);
    }

    /**
     * Cancel the future
     *
     * - when it is not null
     * - and it is not already cancelled
     *
     * @param future nullable future to be cancelled
     * @param interruptIfRunning choose whether to interrupt a running future
     */
    public static void cancel(@Nullable Future<?> future, boolean interruptIfRunning) {
        if (future != null && !future.isCancelled()) {
            future.cancel(interruptIfRunning);
        }
    }
}
