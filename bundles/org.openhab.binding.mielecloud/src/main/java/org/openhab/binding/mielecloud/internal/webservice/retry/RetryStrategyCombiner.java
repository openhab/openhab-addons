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
package org.openhab.binding.mielecloud.internal.webservice.retry;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link RetryStrategy} implementation wrapping the consecutive execution of two retry strategies.
 *
 * @author Björn Lange and Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public class RetryStrategyCombiner implements RetryStrategy {
    private final RetryStrategy first;
    private final RetryStrategy second;

    /**
     * Creates a new {@link RetryStrategy} combining the given ones.
     *
     * @param first First strategy to execute.
     * @param second Strategy to execute in each execution of {@code first}.
     */
    public RetryStrategyCombiner(RetryStrategy first, RetryStrategy second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public <@Nullable T> T performRetryableOperation(Supplier<T> operation, Consumer<Exception> onException) {
        return first.performRetryableOperation(() -> second.performRetryableOperation(operation, onException),
                onException);
    }
}
