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
 * Interface for strategies implementing the retry behavior of requests against the Miele cloud.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public interface RetryStrategy {
    /**
     * Performs an operation which may be retried several times.
     *
     * If retrying fails or a critical error occurred, this method may throw {@link Exception}s of any type.
     *
     * @param operation The operation to perform. To signal that an error can be resolved by retrying this operation it
     *            should throw an {@link Exception}. Whether the operation is retried is up to the {@link RetryStrategy}
     *            implementation.
     * @param onException Handler to invoke when an {@link Exception} is handled by retrying the {@code operation}. This
     *            handler should at least log a message. It must not throw any exception.
     * @return The object returned by {@code operation} if it completed successfully.
     */
    <@Nullable T> T performRetryableOperation(Supplier<T> operation, Consumer<Exception> onException);

    /**
     * Performs an operation which may be retried several times.
     *
     * If retrying fails or a critical error occurred, this method may throw {@link Exception}s of any type.
     *
     * @param operation The operation to perform. To signal that an error can be resolved by retrying this operation it
     *            should throw an {@link Exception}. Whether the operation is retried is up to the {@link RetryStrategy}
     *            implementation
     * @param onException Handler to invoke when an {@link Exception} is handled by retrying the {@code operation}. This
     *            handler should at least log a message. It may not throw any exception.
     */
    default void performRetryableOperation(Runnable operation, Consumer<Exception> onException) {
        performRetryableOperation(new Supplier<@Nullable Void>() {
            @Override
            public @Nullable Void get() {
                operation.run();
                return null;
            }
        }, onException);
    }
}
