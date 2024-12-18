/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal.throttler;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ChannelThrottler} defines the interface for to submit tasks to a
 * throttler
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public interface ChannelThrottler {
    @Nullable
    Future<?> submit(Runnable task);

    @Nullable
    Future<?> submit(Object channelKey, Runnable task);
}
